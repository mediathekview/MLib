package de.mediathekview.mlib.filmlisten;

import static java.time.format.FormatStyle.MEDIUM;

import com.google.gson.Gson;
import de.mediathekview.mlib.daten.AbstractMediaResource;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Podcast;
import de.mediathekview.mlib.daten.Resolution;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A helper class to generate the old fake json format for a {@link
 * de.mediathekview.mlib.daten.Film}.
 */
public class FilmToFakeJsonConverter {

  private static final Logger LOG = LogManager.getLogger(FilmToFakeJsonConverter.class);
  private static final String[] COLUMNNAMES =
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
      "\"X\": [\"%s\",%s,%s,\"%s\",\"%s\",\"%s\",\"%s\",%s,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"]";
  private static final String META_INFORMATION_PATTERN =
      "\"Filmliste\": [\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"]";
  private static final String COLUMNNAMES_PATTERN =
      "\"Filmliste\": [\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"]";
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

  public String toFakeJson(
      final List<AbstractMediaResource<?>> aResources,
      final String aFilmlisteDatum,
      final String aFilmlisteDatumGmt,
      final String aFilmlisteVersion,
      final String aFilmlisteProgramm,
      final String aFilmlisteId) {
    final StringBuilder fakeJsonBuilder = new StringBuilder();
    fakeJsonBuilder.append(FAKE_JSON_BEGIN);
    fakeJsonBuilder.append(System.lineSeparator());

    fakeJsonBuilder.append(
        String.format(
            META_INFORMATION_PATTERN,
            aFilmlisteDatum,
            aFilmlisteDatumGmt,
            aFilmlisteVersion,
            aFilmlisteProgramm,
            aFilmlisteId));
    appendEnd(fakeJsonBuilder, false);

    fakeJsonBuilder.append(String.format(COLUMNNAMES_PATTERN, (Object[]) COLUMNNAMES));
    appendEnd(fakeJsonBuilder, false);

    lastSender = "";
    lastThema = "";
    for (final AbstractMediaResource<?> mediaResource :
        aResources.stream().filter(Objects::nonNull).collect(Collectors.toList())) {
      try {
        resourceToFakeJson(
            fakeJsonBuilder,
            mediaResource,
            mediaResource.equals(aResources.get(aResources.size() - 1)));
      } catch (final Exception exception) {
        LOG.error("A film can't be converted to old json format.", exception);
        LOG.debug(String.format("The film which can't be converted: %s", mediaResource.toString()));
      }
    }

    fakeJsonBuilder.append(FAKE_JSON_END);
    return fakeJsonBuilder.toString();
  }

  private void appendEnd(final StringBuilder fakeJsonBuilder, final boolean aIsLastFilm) {
    if (!aIsLastFilm) {
      fakeJsonBuilder.append(SPLITTERATOR);
    }
    fakeJsonBuilder.append(System.lineSeparator());
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
    if (aMediaResource instanceof Film) {
      final Film film = (Film) aMediaResource;
      if (film.getSubtitles().isEmpty()) {
        return "";
      }

      // prefer ttml-files
      String url = "";
      for (URL subtitleUrl : film.getSubtitles()) {
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
    String result;
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

  private long convertDateTimeToLong(LocalDateTime dateTime) {
    if (dateTime == null) {
      return 0;
    }

    ZonedDateTime zonedDateTime = dateTime.atZone(ZONE_ID);
    return zonedDateTime.toEpochSecond();
  }

  private void resourceToFakeJson(
      final StringBuilder fakeJsonBuilder,
      final AbstractMediaResource<?> aMediaResource,
      final boolean aIsLastFilm) {

    String url = "";
    String urlKlein = "";
    String urlHd = "";

    // create film entry
    url = aMediaResource.getUrl(getDefaultResolution(aMediaResource)).toString();
    if (aMediaResource.getUrls().containsKey(Resolution.SMALL)) {
      urlKlein = aMediaResource.getUrl(Resolution.SMALL).toString();
    }
    if (aMediaResource.getUrls().containsKey(Resolution.HD)) {
      urlHd = aMediaResource.getUrl(Resolution.HD).toString();
    }

    final String title = aMediaResource.getTitel();
    appendMediaResource(fakeJsonBuilder, aMediaResource, title, urlKlein, url, urlHd, aIsLastFilm);
    appendAudioDescriptionEntry(aMediaResource, title, fakeJsonBuilder, aIsLastFilm);
    appendSignLanguageEntry(aMediaResource, title, fakeJsonBuilder, aIsLastFilm);
  }

  private void appendAudioDescriptionEntry(
      final AbstractMediaResource<?> aMediaResource,
      final String aTitle,
      final StringBuilder fakeJsonBuilder,
      final boolean aIsLastFilm) {
    String url;
    String urlSmall = "";
    String urlHd = "";

    if (aMediaResource instanceof Film) {
      Film film = (Film) aMediaResource;
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
            fakeJsonBuilder,
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
      final StringBuilder fakeJsonBuilder,
      final boolean aIsLastFilm) {
    String url;
    String urlSmall = "";
    String urlHd = "";

    if (aMediaResource instanceof Film) {

      Film film = (Film) aMediaResource;

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
            fakeJsonBuilder, aMediaResource, titleSignLanguage, urlSmall, url, urlHd, aIsLastFilm);
      }
    }
  }

  private void appendMediaResource(
      final StringBuilder fakeJsonBuilder,
      final AbstractMediaResource<?> aMediaResource,
      final String aTitle,
      String aUrlSmall,
      final String aUrlNormal,
      String aUrlHd,
      final boolean aIsLastFilm) {
    final Gson gson = new Gson();

    final String thema = setThema(aMediaResource);
    final String sender = setSender(aMediaResource);

    aUrlSmall = reduceUrl(aUrlNormal, aUrlSmall);
    aUrlHd = reduceUrl(aUrlNormal, aUrlHd);

    String website = "";
    if (aMediaResource.getWebsite().isPresent()) {
      website = aMediaResource.getWebsite().get().toString();
    }

    fakeJsonBuilder.append(
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
            aMediaResource instanceof Podcast
                ? durationToString(((Podcast) aMediaResource).getDuration())
                : "",
            aMediaResource instanceof Podcast
                ? ((Podcast) aMediaResource).getFileSize(getDefaultResolution(aMediaResource))
                : "",
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
            aMediaResource instanceof Podcast ? ((Podcast) aMediaResource).isNeu() : false));

    appendEnd(fakeJsonBuilder, aIsLastFilm);
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
