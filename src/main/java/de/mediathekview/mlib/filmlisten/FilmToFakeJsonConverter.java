package de.mediathekview.mlib.filmlisten;

import com.google.gson.Gson;
import de.mediathekview.mlib.daten.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.URL;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static java.time.format.FormatStyle.MEDIUM;

/**
 * A helper class to generate the old fake json format for a {@link
 * de.mediathekview.mlib.daten.Film}.
 */
public class FilmToFakeJsonConverter {
  private static final Logger LOG = LogManager.getLogger(FilmToFakeJsonConverter.class);
  private static final Object[] COLUMNNAMES =
      new String[] {
        "Sender",
        "Thema",
        "Titel",
        "Datum",
        "Zeit",
        "Dauer",
        "Größe [MB]",
        "Beschreibung",
        "Url",
        "Website",
        "Url Untertitel",
        "Url RTMP",
        "Url Klein",
        "Url RTMP Klein",
        "Url HD",
        "Url RTMP HD",
        "DatumL",
        "Url History",
        "Geo",
        "neu"
      };
  private static final String OUTPUT_PATTERN =
      "\"X\":[\"%s\",%s,%s,\"%s\",\"%s\",\"%s\",\"%s\",%s,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"]";
  private static final String META_INFORMATION_PATTERN =
      "\"Filmliste\":[\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"]";
  private static final String COLUMNNAMES_PATTERN =
      "\"Filmliste\":[\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"]";
  private static final char SPLITTERATOR = ',';
  private static final char FAKE_JSON_BEGIN = '{';
  private static final char FAKE_JSON_END = '}';
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofLocalizedDate(MEDIUM).withLocale(Locale.GERMANY);
  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofLocalizedTime(MEDIUM).withLocale(Locale.GERMANY);
  private static final ZoneId ZONE_ID = ZoneId.of("Europe/Berlin");
  private static final char GEO_SPLITTERATOR = '-';
  private static final String URL_INTERSECTION_REDUCE_PATTERN = "%d|";
  private static final String DURATION_FORMAT = "HH:mm:ss";
  private String lastSender;
  private String lastThema;

  public void toFakeJson(
      final List<AbstractMediaResource<?>> aResources,
      final OutputStreamWriter outputstream,
      final String aFilmlisteDatum,
      final String aFilmlisteDatumGmt,
      final String aFilmlisteVersion,
      final String aFilmlisteProgramm,
      final String aFilmlisteId) throws IOException {
    outputstream.write(FAKE_JSON_BEGIN);
    outputstream.write(
        String.format(
            META_INFORMATION_PATTERN,
            aFilmlisteDatum,
            aFilmlisteDatumGmt,
            aFilmlisteVersion,
            aFilmlisteProgramm,
            aFilmlisteId));
    appendEnd(outputstream, false);

    outputstream.write(String.format(COLUMNNAMES_PATTERN, COLUMNNAMES));
    appendEnd(outputstream, false);

    lastSender = "";
    lastThema = "";
    for (final AbstractMediaResource<?> mediaResource :
        aResources.stream().filter(Objects::nonNull).toList()) {
      try {
        resourceToFakeJson(
            outputstream,
            mediaResource,
            mediaResource.equals(aResources.get(aResources.size() - 1)));
      } catch (final Exception exception) {
        LOG.error("A film can't be converted to old json format.", exception);
        LOG.debug(String.format("The film which can't be converted: %s", mediaResource.toString()));
      }
    }

    outputstream.write(FAKE_JSON_END);

  }

  private void appendEnd(final OutputStreamWriter outputstream, final boolean aIsLastFilm) throws IOException {
    if (!aIsLastFilm) {
      outputstream.write(SPLITTERATOR);
    }
  }

  private String durationToString(final Duration aDuration) {
    // there is no duration for a few films => use duration of 0 instead
    if (aDuration == null) {
      return "00:00:00";
    }
    return LocalTime.MIDNIGHT.plus(aDuration).format(DateTimeFormatter.ofPattern(DURATION_FORMAT));
  }

  private String geolocationsToStirng(final Collection<GeoLocations> aGeoLocations) {
    final StringBuilder geolocationsStringBuilder = new StringBuilder();
    if (!aGeoLocations.isEmpty()) {
      for (final GeoLocations geoLocation : aGeoLocations) {
        geolocationsStringBuilder.append(geoLocation.getDescription());
        geolocationsStringBuilder.append(GEO_SPLITTERATOR);
      }
      geolocationsStringBuilder.deleteCharAt(
          geolocationsStringBuilder.lastIndexOf(String.valueOf(GEO_SPLITTERATOR)));
    }
    return geolocationsStringBuilder.toString();
  }

  private Resolution getDefaultResolution(final AbstractMediaResource<?> aMediaResource) {
    if (aMediaResource.getUrls().containsKey(Resolution.NORMAL)) {
      return Resolution.NORMAL;
    }

    for (final Resolution quality : Resolution.getFromBestToLowest()) {
      if (aMediaResource.getUrls().containsKey(quality)) {
        return quality;
      }
    }
    return Resolution.VERY_SMALL;
  }

  private String getSubtitles(final AbstractMediaResource<?> aMediaResource) {
    if (aMediaResource instanceof Film film) {
      if (film.getSubtitles().isEmpty()) {
        return "";
      }

      // prefer ttml-files
      String url = "";
      for (final URL subtitleUrl : film.getSubtitles()) {
        if (url.isEmpty() || subtitleUrl.getFile().endsWith(".ttml")) {
          url = subtitleUrl.toString();
        }
      }
      return url;
    }

    return "";
  }

  private String reduceUrl(final String aBaseUrl, final String aUrlToReduce) {
    final StringBuilder urlIntersectionBuilder = new StringBuilder();
    for (int i = 0;
        i < aBaseUrl.length()
            && i < aUrlToReduce.length()
            && aBaseUrl.charAt(i) == aUrlToReduce.charAt(i);
        i++) {
      urlIntersectionBuilder.append(aBaseUrl.charAt(i));
    }

    final String urlIntersection = urlIntersectionBuilder.toString();
    final String result;
    if (urlIntersection.isEmpty()) {
      result = aUrlToReduce;
    } else {
      result =
          aUrlToReduce.replace(
              urlIntersection,
              String.format(URL_INTERSECTION_REDUCE_PATTERN, urlIntersection.length()));
    }
    return result;
  }

  private long convertDateTimeToLong(final LocalDateTime dateTime) {
    if (dateTime == null) {
      return 0;
    }

    final ZonedDateTime zonedDateTime = dateTime.atZone(ZONE_ID);
    return zonedDateTime.toEpochSecond();
  }

  private void resourceToFakeJson(
      final OutputStreamWriter outputstream,
      final AbstractMediaResource<?> aMediaResource,
      final boolean aIsLastFilm) throws IOException {

    final String title = aMediaResource.getTitel();
    appendDefaultEntry(aMediaResource, title, outputstream, aIsLastFilm);
    appendAudioDescriptionEntry(aMediaResource, title, outputstream, aIsLastFilm);
    appendSignLanguageEntry(aMediaResource, title, outputstream, aIsLastFilm);
  }

  private void appendDefaultEntry(AbstractMediaResource<?> aMediaResource, String title, OutputStreamWriter outputstream, boolean aIsLastFilm) throws IOException {
    final String url;
    String urlKlein = "";
    String urlHd = "";

    final Serializable mediaUrl = aMediaResource.getUrl(getDefaultResolution(aMediaResource));
    if (mediaUrl != null) {
      // create film entry
      url = mediaUrl.toString();
      if (aMediaResource.getUrls().containsKey(Resolution.SMALL)) {
        urlKlein = aMediaResource.getUrl(Resolution.SMALL).toString();
      }
      if (aMediaResource.getUrls().containsKey(Resolution.HD)) {
        urlHd = aMediaResource.getUrl(Resolution.HD).toString();
      }

      appendMediaResource(
          outputstream, aMediaResource, title, urlKlein, url, urlHd, aIsLastFilm);
    }
  }

  private void appendAudioDescriptionEntry(
      final AbstractMediaResource<?> aMediaResource,
      final String aTitle,
      final OutputStreamWriter outputstream,
      final boolean aIsLastFilm) throws IOException {
    final String url;
    String urlSmall = "";
    String urlHd = "";

    if (aMediaResource instanceof Film film) {
      FilmUrl filmUrl = film.getAudioDescription(Resolution.NORMAL);
      if (filmUrl != null) {
        url = filmUrl.toString();

        filmUrl = film.getAudioDescription(Resolution.SMALL);
        if (filmUrl != null) {
          urlSmall = filmUrl.toString();
        }

        filmUrl = film.getAudioDescription(Resolution.HD);
        if (filmUrl != null) {
          urlHd = filmUrl.toString();
        }

        final String titleAudioDescription = aTitle + " (Audiodeskription)";
        appendMediaResource(
            outputstream,
            aMediaResource,
            titleAudioDescription,
            urlSmall,
            url,
            urlHd,
            aIsLastFilm);
      }
    }
  }

  private void appendSignLanguageEntry(
      final AbstractMediaResource<?> aMediaResource,
      final String aTitle,
      final OutputStreamWriter outputstream,
      final boolean aIsLastFilm) throws IOException {
    final String url;
    String urlSmall = "";
    String urlHd = "";

    if (aMediaResource instanceof Film film) {
      FilmUrl filmUrl = film.getSignLanguage(Resolution.NORMAL);
      if (filmUrl != null) {
        url = filmUrl.toString();

        filmUrl = film.getSignLanguage(Resolution.SMALL);
        if (filmUrl != null) {
          urlSmall = filmUrl.toString();
        }

        filmUrl = film.getSignLanguage(Resolution.HD);
        if (filmUrl != null) {
          urlHd = filmUrl.toString();
        }

        final String titleSignLanguage = aTitle + " (Gebärdensprache)";
        appendMediaResource(
            outputstream, aMediaResource, titleSignLanguage, urlSmall, url, urlHd, aIsLastFilm);
      }
    }
  }

  private void appendMediaResource(
      final OutputStreamWriter outputstream,
      final AbstractMediaResource<?> aMediaResource,
      final String aTitle,
      String aUrlSmall,
      final String aUrlNormal,
      String aUrlHd,
      final boolean aIsLastFilm) throws IOException {
    final Gson gson = new Gson();

    final String thema = setThema(aMediaResource);
    final String sender = setSender(aMediaResource);

    aUrlSmall = reduceUrl(aUrlNormal, aUrlSmall);
    aUrlHd = reduceUrl(aUrlNormal, aUrlHd);

    final String website = aMediaResource.getWebsite().map(URL::toString).orElse("");

    outputstream.write(
        String.format(
            OUTPUT_PATTERN,
            sender,
            gson.toJson(thema),
            gson.toJson(aTitle),
            aMediaResource.getTime() == null
                ? ""
                : DATE_FORMATTER.format(aMediaResource.getTime().toLocalDate()),
            aMediaResource.getTime() == null
                ? ""
                : TIME_FORMATTER.format(aMediaResource.getTime().toLocalTime()),
            aMediaResource instanceof Podcast podcast
                ? durationToString(podcast.getDuration()) : "",
            aMediaResource instanceof Podcast podcast
                ? (podcast.getFileSizeKB(getDefaultResolution(aMediaResource))/1024) : "",
            gson.toJson(aMediaResource.getBeschreibung()),
            aUrlNormal,
            website,
            getSubtitles(aMediaResource),
            "",
            aUrlSmall,
            "",
            aUrlHd,
            "",
            convertDateTimeToLong(aMediaResource.getTime()),
            "", // History
            geolocationsToStirng(aMediaResource.getGeoLocations()),
            aMediaResource instanceof Podcast podcast && podcast.isNeu()));

    appendEnd(outputstream, aIsLastFilm);
  }

  private String setSender(final AbstractMediaResource<?> aMediaResource) {
    String sender = aMediaResource.getSenderName();
    if (lastSender.equals(sender)) {
      sender = "";
    } else {
      lastSender = sender;
    }
    return sender;
  }

  private String setThema(final AbstractMediaResource<?> aMediaResource) {
    String thema = aMediaResource.getThema();
    if (lastThema.equals(thema)) {
      thema = "";
    } else {
      lastThema = thema;
    }
    return thema;
  }
}
