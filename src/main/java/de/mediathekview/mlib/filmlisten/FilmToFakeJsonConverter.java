package de.mediathekview.mlib.filmlisten;

import static java.time.format.FormatStyle.MEDIUM;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import de.mediathekview.mlib.daten.AbstractMediaResource;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Podcast;
import de.mediathekview.mlib.daten.Resolution;

/**
 * A helper class to generate the old fake json format for a
 * {@link de.mediathekview.mlib.daten.Film}.
 */
public class FilmToFakeJsonConverter {

  private static final Logger LOG = LogManager.getLogger(FilmToFakeJsonConverter.class);
  private static final String[] COLUMNNAMES =
      new String[] {"Sender", "Thema", "Titel", "Datum", "Zeit", "Dauer", "Größe [MB]",
          "Beschreibung", "Url", "Website", "Url Untertitel", "Url RTMP", "Url Klein",
          "Url RTMP Klein", "Url HD", "Url RTMP HD", "DatumL", "Url History", "Geo", "neu"};
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
  private static final char GEO_SPLITTERATOR = '-';
  private static final String URL_INTERSECTION_REDUCE_PATTERN = "%d|";
  private static final String DURATION_FORMAT = "HH:mm:ss";
  private String lastSender;
  private String lastThema;

  public String toFakeJson(
      @SuppressWarnings("rawtypes") final List<AbstractMediaResource> aResources,
      final String aFilmlisteDatum, final String aFilmlisteDatumGmt, final String aFilmlisteVersion,
      final String aFilmlisteProgramm, final String aFilmlisteId) {
    final StringBuilder fakeJsonBuilder = new StringBuilder();
    fakeJsonBuilder.append(FAKE_JSON_BEGIN);
    fakeJsonBuilder.append(System.lineSeparator());

    fakeJsonBuilder.append(String.format(META_INFORMATION_PATTERN, aFilmlisteDatum,
        aFilmlisteDatumGmt, aFilmlisteVersion, aFilmlisteProgramm, aFilmlisteId));
    appendEnd(fakeJsonBuilder, false);

    fakeJsonBuilder.append(String.format(COLUMNNAMES_PATTERN, (Object[]) COLUMNNAMES));
    appendEnd(fakeJsonBuilder, false);

    lastSender = "";
    lastThema = "";
    for (final AbstractMediaResource<?> mediaResource : aResources.stream().filter(Objects::nonNull)
        .collect(Collectors.toList())) {
      try {
        resourceToFakeJson(fakeJsonBuilder, mediaResource,
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
    return LocalTime.MIDNIGHT.plus(aDuration).format(DateTimeFormatter.ofPattern(DURATION_FORMAT));
  }

  private String geolocationsToStirng(final Collection<GeoLocations> aGeoLocations) {
    final StringBuilder geolocationsStringBuilder = new StringBuilder();
    if (!aGeoLocations.isEmpty()) {
      for (final GeoLocations geoLocation : aGeoLocations) {
        geolocationsStringBuilder.append(geoLocation.getDescription());
        geolocationsStringBuilder.append(GEO_SPLITTERATOR);
      }
      geolocationsStringBuilder
          .deleteCharAt(geolocationsStringBuilder.lastIndexOf(String.valueOf(GEO_SPLITTERATOR)));
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
      return film.getSubtitles().isEmpty() ? "" : film.getSubtitles().iterator().next().toString();
    } else {
      return "";
    }
  }

  private String reduceUrl(final String aBaseUrl, final String aUrlToReduce) {
    final StringBuilder urlIntersectionBuilder = new StringBuilder();
    for (int i = 0; i < aBaseUrl.length() && i < aUrlToReduce.length()
        && aBaseUrl.charAt(i) == aUrlToReduce.charAt(i); i++) {
      urlIntersectionBuilder.append(aBaseUrl.charAt(i));
    }

    final String urlIntersection = urlIntersectionBuilder.toString();
    String result;
    if (urlIntersection.isEmpty()) {
      result = aUrlToReduce;
    } else {
      result = aUrlToReduce.replace(urlIntersection,
          String.format(URL_INTERSECTION_REDUCE_PATTERN, urlIntersection.length()));
    }
    return result;
  }

  private void resourceToFakeJson(final StringBuilder fakeJsonBuilder,
      final AbstractMediaResource<?> aMediaResource, final boolean aIsLastFilm) {
    final String sender = setSender(aMediaResource);

    final String thema = setThema(aMediaResource);

    final String url = aMediaResource.getUrl(getDefaultResolution(aMediaResource)).toString();
    String urlKlein = "";
    String urlHd = "";

    if (aMediaResource.getUrls().containsKey(Resolution.SMALL)) {
      urlKlein = aMediaResource.getUrl(Resolution.SMALL).toString();
    }

    if (aMediaResource.getUrls().containsKey(Resolution.HD)) {
      urlHd = aMediaResource.getUrl(Resolution.HD).toString();
    }

    urlKlein = reduceUrl(url, urlKlein);
    urlHd = reduceUrl(url, urlHd);

    final Gson gson = new Gson();

    fakeJsonBuilder.append(String.format(OUTPUT_PATTERN, sender, gson.toJson(thema),
        gson.toJson(aMediaResource.getTitel()),
        aMediaResource.getTime() == null ? ""
            : DATE_FORMATTER.format(aMediaResource.getTime().toLocalDate()),
        aMediaResource.getTime() == null ? ""
            : TIME_FORMATTER.format(aMediaResource.getTime().toLocalTime()),
        aMediaResource instanceof Podcast
            ? durationToString(((Podcast) aMediaResource).getDuration())
            : "",
        aMediaResource instanceof Podcast
            ? ((Podcast) aMediaResource).getFileSize(getDefaultResolution(aMediaResource))
            : "",
        gson.toJson(aMediaResource.getBeschreibung()), url, aMediaResource.getWebsite(),
        getSubtitles(aMediaResource), "", urlKlein, "", urlHd, "",
        Timestamp
            .valueOf(
                aMediaResource.getTime() == null ? LocalDateTime.now() : aMediaResource.getTime())
            .toString(),
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
