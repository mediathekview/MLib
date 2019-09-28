package de.mediathekview.mlib.filmlisten.reader;

import de.mediathekview.mlib.daten.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static java.time.format.FormatStyle.MEDIUM;

public class OldFilmlistEntryToFilmTask implements Callable<Film> {
  private static final Logger LOG = LogManager.getLogger(OldFilmlistEntryToFilmTask.class);
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofLocalizedDate(MEDIUM).withLocale(Locale.GERMANY);
  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofLocalizedTime(MEDIUM).withLocale(Locale.GERMANY);
  private static final char GEO_SPLITTERATOR = '-';
  private static final String URL_SPLITTERATOR = "\\|";
  private static final String EXCEPTION_TEXT_CANT_BUILD_FILM = "Can't build a Film from splits.";

  private final List<String> entrySplits;
  private final Future<Film> entryBefore;

  public OldFilmlistEntryToFilmTask(
      final List<String> aEntrySplits, final Future<Film> aFilmEntryBefore) {
    entrySplits = aEntrySplits;
    entryBefore = aFilmEntryBefore;
  }

  @Override
  public Film call() throws Exception {
    try {
      entrySplits.forEach(String::trim);
      final String senderText = entrySplits.get(1);
      final Sender sender;
      if (StringUtils.isBlank(senderText) && entryBefore != null) {
        sender = entryBefore.get().getSender();
      } else {
        sender = Sender.getSenderByName(senderText).orElse(null);
      }

      String thema = entrySplits.get(2);
      if (StringUtils.isBlank(thema) && entryBefore != null) {
        thema = entryBefore.get().getThema();
      }

      String titel = entrySplits.get(3);
      if (StringUtils.isBlank(titel) && entryBefore != null) {
        titel = entryBefore.get().getTitel();
      }

      if (sender == null) {
        throw new CantReadFilmException(
            String.format("Can't gather a Sender for the film \"%s\" - \"%s\".", thema, titel));
      }

      final String dateText = entrySplits.get(4);
      final LocalDate date;
      if (StringUtils.isNotBlank(dateText)) {
        date = LocalDate.parse(dateText, DATE_FORMATTER);
      } else {
        date = null;
        LOG.debug(String.format("Film ohne Datum \"%s %s - %s\".", sender.getName(), thema, titel));
      }

      final LocalTime time;
      final String timeText = entrySplits.get(5);
      if (StringUtils.isNotBlank(timeText)) {
        time = LocalTime.parse(timeText, TIME_FORMATTER);
      } else {
        time = LocalTime.MIDNIGHT;
      }

      final String durationText = entrySplits.get(6);
      final Duration dauer;
      if (StringUtils.isNotBlank(durationText)) {
        dauer = Duration.between(LocalTime.MIDNIGHT, LocalTime.parse(durationText));
      } else {
        dauer = Duration.ZERO;
        LOG.debug(String.format("Film ohne Dauer \"%s %s - %s\".", sender.getName(), thema, titel));
      }

      final String groesseText = entrySplits.get(7);

      final long groesse;
      if (StringUtils.isNotBlank(groesseText)) {
        groesse = Long.parseLong(groesseText);
      } else {
        groesse = 0l;
        LOG.debug(String.format("Film ohne Größe \"%s %s - %s\".", sender.getName(), thema, titel));
      }

      final String beschreibung = entrySplits.get(8);

      // Here we import also films without a download URL so the next entry can use the Sender,
      // Thema and Titel. After the import all films without download URLs will be removed.
      final Optional<URL> urlNormal = gatherNormalUrl();

      final Optional<URL> urlWebseite = gatherWebsiteUrl();

      final String urlTextUntertitel = entrySplits.get(11);

      final String urlTextKlein = entrySplits.get(13);
      final String urlTextHD = entrySplits.get(15);

      // Ignoring RTMP because can't find any usage.

      // Ignoring Film URL History because can't find any usage.

      final Collection<GeoLocations> geoLocations = readGeoLocations(entrySplits.get(19));

      final String neu = entrySplits.get(20);

      final Film film =
          new Film(
              UUID.randomUUID(),
              sender,
              titel,
              thema,
              date == null ? null : LocalDateTime.of(date, time),
              dauer);
      film.addAllGeoLocations(geoLocations);
      film.setWebsite(urlWebseite);

      if (StringUtils.isNotBlank(neu)) {
        film.setNeu(Boolean.parseBoolean(neu));
      }

      film.setBeschreibung(beschreibung);

      if (!urlTextUntertitel.isEmpty()) {
        film.addSubtitle(new URL(urlTextUntertitel));
      }

      if (urlNormal.isPresent()) {
        film.addUrl(Resolution.NORMAL, new FilmUrl(urlNormal.get(), groesse));
        if (!urlTextKlein.isEmpty()) {
          final FilmUrl urlKlein = urlTextToUri(urlNormal.get(), groesse, urlTextKlein);
          if (urlKlein != null) {
            film.addUrl(Resolution.SMALL, urlKlein);
          }
        }

        if (!urlTextHD.isEmpty()) {
          final FilmUrl urlHD = urlTextToUri(urlNormal.get(), groesse, urlTextHD);
          if (urlHD != null) {
            film.addUrl(Resolution.HD, urlHD);
          }
        }
      }

      return film;
    } catch (final Exception exception) {
      throw new Exception(EXCEPTION_TEXT_CANT_BUILD_FILM, exception);
    }
  }

  private Optional<URL> gatherNormalUrl() {
    final String urlNormalText = entrySplits.get(9).trim();
    try {
      return Optional.of(new URL(StringEscapeUtils.unescapeJava(urlNormalText)));
    } catch (final MalformedURLException malformedURLException) {
      LOG.debug(
          String.format("The normal download URL \"%s\" can't be prased.", urlNormalText),
          malformedURLException);
      return Optional.empty();
    }
  }

  private Optional<URL> gatherWebsiteUrl() {
    final String websiteUrlText = entrySplits.get(10).trim();
    try {
      return Optional.of(new URL(StringEscapeUtils.unescapeJava(websiteUrlText)));
    } catch (final MalformedURLException malformedURLException) {
      LOG.debug(
          String.format("The website URL \"%s\" can't be prased.", websiteUrlText),
          malformedURLException);
      return Optional.empty();
    }
  }

  private Collection<GeoLocations> readGeoLocations(final String aGeoText) {
    final Collection<GeoLocations> geoLocations = new ArrayList<>();

    final GeoLocations singleGeoLocation = GeoLocations.getFromDescription(aGeoText);
    if (singleGeoLocation == null) {
      for (final String geoText : aGeoText.split(String.valueOf(GEO_SPLITTERATOR))) {
        final GeoLocations geoLocation = GeoLocations.getFromDescription(geoText);
        if (geoLocation != null) {
          geoLocations.add(geoLocation);
        }
      }
    } else {
      geoLocations.add(singleGeoLocation);
    }

    return geoLocations;
  }

  private FilmUrl urlTextToUri(final URL aUrlNormal, final long aGroesse, final String aUrlText)
      throws MalformedURLException {
    FilmUrl filmUrl = null;

    final String[] splittedUrlText = aUrlText.split(URL_SPLITTERATOR);
    if (splittedUrlText.length == 2) {
      final int lengthOfOld = Integer.parseInt(splittedUrlText[0]);

      final StringBuilder newUrlBuilder = new StringBuilder();
      newUrlBuilder.append(aUrlNormal.toString(), 0, lengthOfOld);
      newUrlBuilder.append(splittedUrlText[1]);

      filmUrl = new FilmUrl(new URL(newUrlBuilder.toString()), aGroesse);
    }
    return filmUrl;
  }
}
