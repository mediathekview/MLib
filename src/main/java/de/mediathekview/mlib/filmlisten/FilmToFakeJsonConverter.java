package de.mediathekview.mlib.filmlisten;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Qualities;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import static java.time.format.FormatStyle.*;

/**
 * A helper class to generate the old fake json format for a {@link de.mediathekview.mlib.daten.Film}.
 */
public class FilmToFakeJsonConverter
{
    private static final String[] COLUMNNAMES = new String[]{"Sender", "Thema", "Titel", "Datum", "Zeit", "Dauer", "Größe [MB]", "Beschreibung", "Url", "Website", "Url Untertitel", "Url RTMP", "Url Klein", "Url RTMP Klein", "Url HD", "Url RTMP HD", "DatumL", "Url History", "Geo", "neu"};
    private static final String OUTPUT_PATTERN = "\"X\": [\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"]";
    private static final String META_INFORMATION_PATTERN = "\"Filmliste\": [\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"]";
    private static final String COLUMNNAMES_PATTERN = "\"Filmliste\": [\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"]";
    private static final char SPLITTERATOR = ',';
    private static final char FAKE_JSON_BEGIN = '{';
    private static final char FAKE_JSON_END = '}';
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofLocalizedDate(MEDIUM).withLocale(Locale.GERMANY);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofLocalizedTime(SHORT).withLocale(Locale.GERMANY);
    private static final String RTMP = "rtmp";
    private static final char GEO_SPLITTERATOR = '-';
    private static final String URL_INTERSECTION_REDUCE_PATTERN = "%d|";
    private static final String DURATION_FORMAT = "HH:mm:ss";
    private String lastSender;
    private String lastThema;


    public String toFakeJson(List<Film> aFilme, String aFilmlisteDatum, String aFilmlisteDatumGmt, String aFilmlisteVersion, String aFilmlisteProgramm, String aFilmlisteId)
    {
        StringBuilder fakeJsonBuilder = new StringBuilder();
        fakeJsonBuilder.append(FAKE_JSON_BEGIN);
        fakeJsonBuilder.append(System.lineSeparator());

        fakeJsonBuilder.append(String.format(META_INFORMATION_PATTERN, aFilmlisteDatum, aFilmlisteDatumGmt, aFilmlisteVersion, aFilmlisteProgramm, aFilmlisteId));
        appendEnd(fakeJsonBuilder,false);

        fakeJsonBuilder.append(String.format(COLUMNNAMES_PATTERN, COLUMNNAMES));
        appendEnd(fakeJsonBuilder,false);

        lastSender = "";
        lastThema = "";
        for (Film film : aFilme)
        {
            filmToFakeJson(fakeJsonBuilder, film, film.equals(aFilme.get(aFilme.size()-1)));
        }


        fakeJsonBuilder.append(FAKE_JSON_END);
        return fakeJsonBuilder.toString();
    }

    private void filmToFakeJson(final StringBuilder fakeJsonBuilder, final Film film, boolean aIsLastFilm)
    {
        String sender = setSender(film);

        String thema = setThema(film);

        String url = url = film.getUrl(Qualities.NORMAL).toString();
        String urlKlein = "";
        String urlHd = "";

        if (film.getUrls().containsKey(Qualities.SMALL))
        {
                urlKlein = film.getUrl(Qualities.SMALL).toString();
        }

        if (film.getUrls().containsKey(Qualities.HD))
        {
                urlHd = film.getUrl(Qualities.HD).toString();
        }

        urlKlein = reduceUrl(url, urlKlein);
        urlHd = reduceUrl(url, urlHd);

        fakeJsonBuilder.append(String.format(OUTPUT_PATTERN, sender,
                thema,
                film.getTitel(),
                film.getTime() == null ? "" :DATE_FORMATTER.format(film.getTime().toLocalDate()),
                film.getTime() == null ? "" :TIME_FORMATTER.format(film.getTime().toLocalTime()),
                durationToString(film.getDuration()),
                film.getFileSize(Qualities.NORMAL),
                film.getBeschreibung(),
                url,
                film.getWebsite(),
                film.getSubtitles().isEmpty() ? "" : film.getSubtitles().iterator().next().toString(),
                "",
                urlKlein,
                "",
                urlHd,
                "",
                Timestamp.valueOf(film.getTime()).toString(),
                "", //History
                geolocationsToStirng(film.getGeoLocations()),
                film.isNeu()
        ));
        appendEnd(fakeJsonBuilder,aIsLastFilm);
    }

    private String setThema(final Film film)
    {
        String thema = film.getThema();
        if (lastThema.equals(thema))
        {
            thema = "";
        } else
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
        } else
        {
            lastSender = sender;
        }
        return sender;
    }

    private String reduceUrl(String aBaseUrl, String aUrlToReduce)
    {
        StringBuilder urlIntersectionBuilder = new StringBuilder();
        for (int i = 0; i < aBaseUrl.length() && i < aUrlToReduce.length() && aBaseUrl.charAt(i) == aUrlToReduce.charAt(i); i++)
        {
            urlIntersectionBuilder.append(aBaseUrl.charAt(i));
        }

        String urlIntersection = urlIntersectionBuilder.toString();
        String result;
        if (urlIntersection.isEmpty())
        {
            result = aUrlToReduce;
        } else
        {
            result = aUrlToReduce.replace(urlIntersection, String.format(URL_INTERSECTION_REDUCE_PATTERN, urlIntersection.length()));
        }
        return result;
    }

    private String geolocationsToStirng(Collection<GeoLocations> aGeoLocations)
    {
        StringBuilder geolocationsStringBuilder = new StringBuilder();
        if (!aGeoLocations.isEmpty())
        {
            for (GeoLocations geoLocation : aGeoLocations)
            {
                geolocationsStringBuilder.append(geoLocation.getDescription());
                geolocationsStringBuilder.append(GEO_SPLITTERATOR);
            }
            geolocationsStringBuilder.deleteCharAt(geolocationsStringBuilder.lastIndexOf(String.valueOf(GEO_SPLITTERATOR)));
        }
        return geolocationsStringBuilder.toString();
    }

    private String durationToString(final Duration aDuration)
    {
        return LocalTime.MIDNIGHT.plus(aDuration).format(DateTimeFormatter.ofPattern(DURATION_FORMAT));
    }

    private void appendEnd(final StringBuilder fakeJsonBuilder, boolean aIsLastFilm)
    {
        if(!aIsLastFilm)
        {
            fakeJsonBuilder.append(SPLITTERATOR);
        }
        fakeJsonBuilder.append(System.lineSeparator());
    }
}
