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

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Qualities;

/**
 * A helper class to generate the old fake json format for a
 * {@link de.mediathekview.mlib.daten.Film}.
 */
public class FilmToFakeJsonConverter
{

    private static final Logger LOG = LogManager.getLogger(FilmToFakeJsonConverter.class);
    private static final String[] COLUMNNAMES = new String[]
    { "Sender", "Thema", "Titel", "Datum", "Zeit", "Dauer", "Größe [MB]", "Beschreibung", "Url", "Website",
            "Url Untertitel", "Url RTMP", "Url Klein", "Url RTMP Klein", "Url HD", "Url RTMP HD", "DatumL",
            "Url History", "Geo", "neu" };
    private static final String OUTPUT_PATTERN =
            "\"X\": [\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"]";
    private static final String META_INFORMATION_PATTERN = "\"Filmliste\": [\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"]";
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

    public String toFakeJson(final List<Film> aFilme, final String aFilmlisteDatum, final String aFilmlisteDatumGmt,
            final String aFilmlisteVersion, final String aFilmlisteProgramm, final String aFilmlisteId)
    {
        final StringBuilder fakeJsonBuilder = new StringBuilder();
        fakeJsonBuilder.append(FAKE_JSON_BEGIN);
        fakeJsonBuilder.append(System.lineSeparator());

        fakeJsonBuilder.append(String.format(META_INFORMATION_PATTERN, aFilmlisteDatum, aFilmlisteDatumGmt,
                aFilmlisteVersion, aFilmlisteProgramm, aFilmlisteId));
        appendEnd(fakeJsonBuilder, false);

        fakeJsonBuilder.append(String.format(COLUMNNAMES_PATTERN, (Object[]) COLUMNNAMES));
        appendEnd(fakeJsonBuilder, false);

        lastSender = "";
        lastThema = "";
        for (final Film film : aFilme.stream().filter(Objects::nonNull).collect(Collectors.toList()))
        {
            try
            {
                filmToFakeJson(fakeJsonBuilder, film, film.equals(aFilme.get(aFilme.size() - 1)));
            }
            catch (final Exception exception)
            {
                LOG.error("A film can't be converted to old json format.", exception);
                LOG.debug(String.format("The film which can't be converted: %s", film.toString()));
            }
        }

        fakeJsonBuilder.append(FAKE_JSON_END);
        return fakeJsonBuilder.toString();
    }

    private void filmToFakeJson(final StringBuilder fakeJsonBuilder, final Film aFilm, final boolean aIsLastFilm)
    {
        final String sender = setSender(aFilm);

        final String thema = setThema(aFilm);

        final String url = aFilm.getUrl(getDefaultQuality(aFilm)).toString();
        String urlKlein = "";
        String urlHd = "";

        if (aFilm.getUrls().containsKey(Qualities.SMALL))
        {
            urlKlein = aFilm.getUrl(Qualities.SMALL).toString();
        }

        if (aFilm.getUrls().containsKey(Qualities.HD))
        {
            urlHd = aFilm.getUrl(Qualities.HD).toString();
        }

        urlKlein = reduceUrl(url, urlKlein);
        urlHd = reduceUrl(url, urlHd);

        fakeJsonBuilder.append(String.format(OUTPUT_PATTERN, sender, thema, aFilm.getTitel(),
                aFilm.getTime() == null ? "" : DATE_FORMATTER.format(aFilm.getTime().toLocalDate()),
                aFilm.getTime() == null ? "" : TIME_FORMATTER.format(aFilm.getTime().toLocalTime()),
                durationToString(aFilm.getDuration()), aFilm.getFileSize(getDefaultQuality(aFilm)),
                aFilm.getBeschreibung(), url, aFilm.getWebsite(),
                aFilm.getSubtitles().isEmpty() ? "" : aFilm.getSubtitles().iterator().next().toString(), "", urlKlein,
                "", urlHd, "",
                Timestamp.valueOf(aFilm.getTime() == null ? LocalDateTime.now() : aFilm.getTime()).toString(), "", // History
                geolocationsToStirng(aFilm.getGeoLocations()), aFilm.isNeu()));
        appendEnd(fakeJsonBuilder, aIsLastFilm);
    }

    private Qualities getDefaultQuality(final Film aFilm)
    {
        if (aFilm.getUrls().containsKey(Qualities.NORMAL))
        {
            return Qualities.NORMAL;
        }

        for (final Qualities quality : Qualities.getFromBestToLowest())
        {
            if (aFilm.getUrls().containsKey(quality))
            {
                return quality;
            }
        }
        return Qualities.VERY_SMALL;
    }

    private String setThema(final Film film)
    {
        String thema = film.getThema();
        if (lastThema.equals(thema))
        {
            thema = "";
        }
        else
        {
            lastThema = thema;
        }
        return thema;
    }

    private String setSender(final Film film)
    {
        String sender = film.getSender().getName();
        if (lastSender.equals(sender))
        {
            sender = "";
        }
        else
        {
            lastSender = sender;
        }
        return sender;
    }

    private String reduceUrl(final String aBaseUrl, final String aUrlToReduce)
    {
        final StringBuilder urlIntersectionBuilder = new StringBuilder();
        for (int i = 0; i < aBaseUrl.length() && i < aUrlToReduce.length()
                && aBaseUrl.charAt(i) == aUrlToReduce.charAt(i); i++)
        {
            urlIntersectionBuilder.append(aBaseUrl.charAt(i));
        }

        final String urlIntersection = urlIntersectionBuilder.toString();
        String result;
        if (urlIntersection.isEmpty())
        {
            result = aUrlToReduce;
        }
        else
        {
            result = aUrlToReduce.replace(urlIntersection,
                    String.format(URL_INTERSECTION_REDUCE_PATTERN, urlIntersection.length()));
        }
        return result;
    }

    private String geolocationsToStirng(final Collection<GeoLocations> aGeoLocations)
    {
        final StringBuilder geolocationsStringBuilder = new StringBuilder();
        if (!aGeoLocations.isEmpty())
        {
            for (final GeoLocations geoLocation : aGeoLocations)
            {
                geolocationsStringBuilder.append(geoLocation.getDescription());
                geolocationsStringBuilder.append(GEO_SPLITTERATOR);
            }
            geolocationsStringBuilder
                    .deleteCharAt(geolocationsStringBuilder.lastIndexOf(String.valueOf(GEO_SPLITTERATOR)));
        }
        return geolocationsStringBuilder.toString();
    }

    private String durationToString(final Duration aDuration)
    {
        return LocalTime.MIDNIGHT.plus(aDuration).format(DateTimeFormatter.ofPattern(DURATION_FORMAT));
    }

    private void appendEnd(final StringBuilder fakeJsonBuilder, final boolean aIsLastFilm)
    {
        if (!aIsLastFilm)
        {
            fakeJsonBuilder.append(SPLITTERATOR);
        }
        fakeJsonBuilder.append(System.lineSeparator());
    }
}
