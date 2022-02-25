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
import java.util.*;

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

  @Mapping(target = "urls", ignore = true)
  @Mapping(target = "signLanguages", ignore = true)
  @Mapping(target = "merge", ignore = true)
  @Mapping(target = "audioDescriptions", ignore = true)
  @Mapping(target = "uuid", expression = "java(java.util.UUID.randomUUID())")
  @Mapping(target = "website", expression = "java(websiteToWebsiteUrl(rawFilm))")
  @Mapping(target = "sender", source = "sender", qualifiedByName = "senderTextToSender")
  @Mapping(target = "time", expression = "java(mapDateTime(rawFilm))")
  @Mapping(target = "geoLocations", expression = "java(mapGeolocation(rawFilm))")
  @Mapping(target = "duration", expression = "java(mapDuration(rawFilm))")
  @Mapping(target = "subtitles", expression = "java(mapSubtitleUrl(rawFilm))")
  Film rawFilmToFilm(RawFilm rawFilm);

  default List<GeoLocations> mapGeolocation(RawFilm rawFilm) {
    return GeoLocations.find(rawFilm.getGeo()).map(List::of).orElse(new ArrayList<>());
  }

  default LocalDateTime mapDateTime(RawFilm rawFilm) {
    final Optional<LocalDate> optionalDate = gatherDate(rawFilm);

    final LocalTime time = gatherTime(rawFilm.getZeit());
    return optionalDate.map(date -> LocalDateTime.of(date, time)).orElse(null);
  }

  default URL websiteToWebsiteUrl(RawFilm rawFilm) {
    try {
      return new URL(StringEscapeUtils.unescapeJava(rawFilm.getWebsite()));
    } catch (MalformedURLException malformedURLException) {
      LOG.debug(
          "The film \"{} {} - {}\" has a invalid website URL \"{}\".",
          rawFilm.getSender(),
          rawFilm.getThema(),
          rawFilm.getTitel(),
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
    long groesse = mapSize(rawFilm);
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

  default Set<URL> mapSubtitleUrl(RawFilm rawFilm) {
    String untertitelUrl = rawFilm.getUrlUntertitel();
    if (untertitelUrl != null && !untertitelUrl.isEmpty()) {
      try {
        return Set.of(new URL(untertitelUrl));
      } catch (MalformedURLException malformedURLException) {
        LOG.debug(
            "The film \"{} {} - {}\" has a invalid subtitle URL \"{}\".",
            rawFilm.getSender(),
            rawFilm.getThema(),
            rawFilm.getTitel(),
            malformedURLException);
      }
    }
    return new HashSet<>();
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

  default Duration mapDuration(RawFilm rawFilm) {
    final Duration duration;
    String dauer = rawFilm.getDauer();
    if (StringUtils.isNotBlank(dauer)) {
      duration = Duration.between(LocalTime.MIDNIGHT, LocalTime.parse(dauer));
    } else {
      duration = Duration.ZERO;
      if (LOG.isDebugEnabled()) {
        LOG.debug(
            "A film without duration \"{} {} - {}\".",
            rawFilm.getSender(),
            rawFilm.getThema(),
            rawFilm.getTitel());
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

  private long mapSize(RawFilm rawFilm) {
    String groesseText = rawFilm.getGroesseMb();
    if (StringUtils.isNotBlank(groesseText)) {
      return Long.parseLong(groesseText);
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug(
          "A film without a size \"{} {} - {}\".",
          rawFilm.getSender(),
          rawFilm.getThema(),
          rawFilm.getTitel());
    }

    return 0L;
  }

  private Optional<LocalDate> gatherDate(RawFilm rawFilm) {
    String datum = rawFilm.getDatum();
    if (StringUtils.isNotBlank(datum)) {
      return Optional.of(LocalDate.parse(datum, DATE_FORMATTER));
    } else {
      if (LOG.isDebugEnabled()) {
        LOG.debug(
            "A film without date \"{} {} - {}\".",
            rawFilm.getSender(),
            rawFilm.getThema(),
            rawFilm.getTitel());
      }
      return Optional.empty();
    }
  }
}
