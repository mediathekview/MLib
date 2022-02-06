package de.mediathekview.mlib.filmlisten.reader;

import de.mediathekview.mlib.daten.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static java.time.format.FormatStyle.MEDIUM;

@Mapper
public interface RawFilmToFilmMapper {
  Logger LOG = LogManager.getLogger(RawFilmToFilmMapper.class);
  DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofLocalizedDate(MEDIUM).withLocale(Locale.GERMANY);
  DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofLocalizedTime(MEDIUM).withLocale(Locale.GERMANY);
  RawFilmToFilmMapper INSTANCE = Mappers.getMapper(RawFilmToFilmMapper.class);
  String URL_SPLITERATOR = "\\|";

  @Mapping(source = "urlUntertitel", target = "subtitles", ignore = true)
  @Mapping(source = "website", target = "website", qualifiedByName = "websiteToWebsiteUrl")
  @Mapping(source = "sender", target = "sender", qualifiedByName = "senderTextToSender")
  Film rawFilmToFilm(RawFilm rawFilm, @Context RawFilm rawFilmContext);

  @Named("websiteToWebsiteUrl")
  default URL websiteToWebsiteUrl(String website, @Context RawFilm rawFilmContext) {
    try {
      return new URL(StringEscapeUtils.unescapeJava(website));
    } catch (MalformedURLException malformedURLException) {
      LOG.debug(
          "The film \"{} {} - {}\" has a invalid website URL \"{}\".",
          rawFilmContext.getSender(),
          rawFilmContext.getThema(),
          rawFilmContext.getTitel(),
          malformedURLException);
      return null;
    }
  }

  @Named("senderTextToSender")
  default Sender senderTextToSender(String senderText) {
    return Sender.getSenderByName(senderText)
        .orElseThrow(
            () ->
                new RawFilmToFilmException(
                    String.format("The sender \"%s\" is unknown!", senderText)));
  }

  @AfterMapping
  default void complexMappings(RawFilm rawFilm, @MappingTarget Film film) {
    film.setUuid(UUID.randomUUID());

    final Optional<LocalDate> optionalData = gatherDate(film, rawFilm.getDatum());
    final LocalTime time = gatherTime(rawFilm.getZeit());
    optionalData.map(date -> LocalDateTime.of(date, time)).ifPresent(film::setTime);

    GeoLocations.find(rawFilm.getGeo()).map(List::of).ifPresent(film::setGeoLocations);

    film.setDuration(gatherDuration(film, rawFilm.getDauer()));
    readSubtitleUrl(film, rawFilm.getUrlUntertitel()).ifPresent(film::addSubtitle);
    final long groesse = gatherGroesse(film, rawFilm.getGroesseMb());

    final Optional<URL> optionalUrlNormal = gatherNormalUrl(rawFilm.getUrl());
    if (optionalUrlNormal.isPresent()) {
      final URL urlNormal = optionalUrlNormal.get();
      film.addUrl(Resolution.NORMAL, new FilmUrl(urlNormal, groesse));

      buildAlternativeUrl(film, groesse, urlNormal, rawFilm.getUrlKlein())
          .ifPresent(url -> film.addUrl(Resolution.SMALL, url));
      buildAlternativeUrl(film, groesse, urlNormal, rawFilm.getUrlHd())
          .ifPresent(url -> film.addUrl(Resolution.HD, url));
    }
  }

  private Optional<URL> readSubtitleUrl(Film film, final String untertitelUrl) {
    if (!untertitelUrl.isEmpty()) {
      try {
        return Optional.of(new URL(untertitelUrl));
      } catch (MalformedURLException malformedURLException) {
        LOG.debug(
            "The film \"{} {} - {}\" has a invalid subtitle URL \"{}\".",
            film.getSender().getName(),
            film.getThema(),
            film.getTitel(),
            malformedURLException);
      }
    }
    return Optional.empty();
  }

  private Optional<URL> gatherNormalUrl(String url) {
    try {
      return Optional.of(new URL(StringEscapeUtils.unescapeJava(url)));
    } catch (final MalformedURLException malformedURLException) {
      LOG.debug("The normal download URL \"{}\" can't be parsed.", url, malformedURLException);
      return Optional.empty();
    }
  }

  private Optional<FilmUrl> buildAlternativeUrl(
      Film film, final long groesse, final URL urlNormal, final String url) {
    if (url.isEmpty()) {
      return Optional.empty();
    }

    final String[] splittedUrlText = url.split(URL_SPLITERATOR);
    if (splittedUrlText.length == 2) {
      final int lengthOfOld = Integer.parseInt(splittedUrlText[0]);

      final String newUrl = urlNormal.toString().substring(0, lengthOfOld) + splittedUrlText[1];
      try {
        return Optional.of(new FilmUrl(new URL(newUrl), groesse));
      } catch (MalformedURLException malformedURLException) {
        LOG.debug(
            "The film \"{} {} - {}\" has a invalid film URL \"{}\".",
            film.getSender().getName(),
            film.getThema(),
            film.getTitel(),
            malformedURLException);
      }
    }
    return Optional.empty();
  }

  private Duration gatherDuration(Film film, String dauer) {
    final Duration duration;
    if (StringUtils.isNotBlank(dauer)) {
      duration = Duration.between(LocalTime.MIDNIGHT, LocalTime.parse(dauer));
    } else {
      duration = Duration.ZERO;
      if (LOG.isDebugEnabled()) {
        LOG.debug(
            "A film without duration \"{} {} - {}\".",
            film.getSender().getName(),
            film.getThema(),
            film.getTitel());
      }
    }
    return duration;
  }

  private LocalTime gatherTime(String zeit) {
    final LocalTime time;
    if (StringUtils.isNotBlank(zeit)) {
      time = LocalTime.parse(zeit, TIME_FORMATTER);
    } else {
      time = LocalTime.MIDNIGHT;
    }
    return time;
  }

  private long gatherGroesse(Film film, final String groesseText) {

    final long groesse;
    if (StringUtils.isNotBlank(groesseText)) {
      groesse = Long.parseLong(groesseText);
    } else {
      groesse = 0L;
      if (LOG.isDebugEnabled()) {
        LOG.debug(
            "A film without a size \"{} {} - {}\".",
            film.getSender().getName(),
            film.getThema(),
            film.getTitel());
      }
    }
    return groesse;
  }

  private Optional<LocalDate> gatherDate(Film film, String datum) {
    if (StringUtils.isNotBlank(datum)) {
      return Optional.of(LocalDate.parse(datum, DATE_FORMATTER));
    } else {
      if (LOG.isDebugEnabled()) {
        LOG.debug(
            "A film without date \"{} {} - {}\".",
            film.getSender().getName(),
            film.getThema(),
            film.getTitel());
      }
      return Optional.empty();
    }
  }
}
