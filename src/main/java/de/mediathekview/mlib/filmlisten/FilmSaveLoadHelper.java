package de.mediathekview.mlib.filmlisten;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Qualities;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Locale;

/**
 * A helper class to generate the old fake json format for a {@link de.mediathekview.mlib.daten.Film}.
 */
public final class FilmSaveLoadHelper
{
    private static final String[] COLUMNNAMES = new String[]{"Sender", "Thema", "Titel", "Datum", "Zeit", "Dauer", "Größe [MB]", "Beschreibung", "Url", "Website", "Url Untertitel", "Url RTMP", "Url Klein", "Url RTMP Klein", "Url HD", "Url RTMP HD", "DatumL", "Url History", "Geo", "neu"};
    private static final String OUTPUT_PATTERN = "\"X\" : [\"%s\", \"%s\", \"%s\", \"%s\", \"%s\",\"%s\", \"%s\", \"%s\", \"%s\", \"%s\",\"%s\", \"%s\", \"%s\", \"%s\", \"%s\",\"%s\", \"%s\", \"%s\", \"%s\", \"%s\"]";
    private static final String META_INFORMATION_PATTERN = "\"Filmliste\" : [\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"]";
    private static final String COLUMNNAMES_PATTERN = "\"Filmliste\" : [\"%s\", \"%s\", \"%s\", \"%s\", \"%s\",\"%s\", \"%s\", \"%s\", \"%s\", \"%s\",\"%s\", \"%s\", \"%s\", \"%s\", \"%s\",\"%s\", \"%s\", \"%s\", \"%s\", \"%s\"]";
    private static final char SPLITTERATOR = ',';
    private static final char FAKE_JSON_BEGIN = '{';
    private static final char FAKE_JSON_END = '}';
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT).withLocale(Locale.GERMANY);
    private static final String ZERO_TEXT = "0";
    private static final String RTMP = "rtmp";
    private static final char GEO_SPLITTERATOR = '-';
    private static final String URL_INTERSECTION_REDUCE_PATTERN = "%d|";
    private static final String DURATION_FORMAT = "%02d:%02d:%02d";

    private FilmSaveLoadHelper()
    {
        super();
    }

    public static String toFakeJson(Collection<Film> aFilme, String aFilmlisteDatum, String aFilmlisteDatumGmt, String aFilmlisteVersion, String aFilmlisteProgramm, String aFilmlisteId)
    {
        StringBuilder fakeJsonBuilder = new StringBuilder();
        fakeJsonBuilder.append(FAKE_JSON_BEGIN);
        fakeJsonBuilder.append(System.lineSeparator());

        fakeJsonBuilder.append(String.format(META_INFORMATION_PATTERN, aFilmlisteDatum, aFilmlisteDatumGmt, aFilmlisteVersion, aFilmlisteProgramm, aFilmlisteId));
        appendEnd(fakeJsonBuilder);

        fakeJsonBuilder.append(String.format(COLUMNNAMES_PATTERN, COLUMNNAMES));
        appendEnd(fakeJsonBuilder);

        String lastSender = "";
        String lastThema = "";
        for (Film film : aFilme)
        {
            String sender = film.getSender().getName();
            String thema = film.getThema();
            if (lastSender.equals(sender))
            {
                sender = "";
            }

            if (lastThema.equals(thema))
            {
                thema = "";
            }

            String url="";
            String urlKlein="";
            String urlHd="";
            String urlRtmp="";
            String urlRtmpKlein="";
            String urlRtmpHd="";

            if(film.getUrl(Qualities.NORMAL).toString().startsWith(RTMP))
            {
                urlRtmp = film.getUrl(Qualities.NORMAL).toString();
            }else {
                url = film.getUrl(Qualities.NORMAL).toString();
            }

            if(film.getUrls().containsKey(Qualities.SMALL))
            {
                if(film.getUrl(Qualities.SMALL).toString().startsWith(RTMP))
                {
                    urlRtmpKlein = film.getUrl(Qualities.SMALL).toString();
                }else {
                    urlKlein = film.getUrl(Qualities.SMALL).toString();
                }
            }

            if(film.getUrls().containsKey(Qualities.HD))
            {
                if(film.getUrl(Qualities.HD).toString().startsWith(RTMP))
                {
                    urlRtmpHd = film.getUrl(Qualities.HD).toString();
                }else {
                    urlHd = film.getUrl(Qualities.HD).toString();
                }
            }

            urlKlein = reduceUrl(url,urlKlein);
            urlHd = reduceUrl(url,urlHd);
            urlRtmp = reduceUrl(url,urlRtmp);
            urlRtmpKlein=reduceUrl(url,urlRtmpKlein);
            urlRtmpHd = reduceUrl(url,urlRtmpHd);

            fakeJsonBuilder.append(String.format(OUTPUT_PATTERN, sender,
                    thema,
                    film.getTitel(),
                    FORMATTER.format(film.getTime().toLocalDate()),
                    FORMATTER.format(film.getTime().toLocalTime()),
                    durationToString(film.getDuration()),
                    film.getSize(film.getUrl(Qualities.NORMAL)),
                    film.getBeschreibung(),
                    url,
                    film.getWebsite(),
                    film.getSubtitles().isEmpty() ? "" : film.getSubtitles().iterator().next().toString(),
                    urlRtmp,
                    urlKlein,
                    urlRtmpKlein,
                    urlHd,
                    urlRtmpHd,
                    Timestamp.valueOf(film.getTime()).toString(),
                    "", //History
                    geolocationsToStirng(film.getGeoLocations()),
                    film.isNeu()
                    ));
            appendEnd(fakeJsonBuilder);
        }


        fakeJsonBuilder.append(FAKE_JSON_END);
        return fakeJsonBuilder.toString();
    }

    private static String reduceUrl(String aBaseUrl, String aUrlToReduce)
    {
        StringBuilder urlIntersectionBuilder = new StringBuilder();
        for(int i=0; i<aBaseUrl.length() && i<aUrlToReduce.length() && aBaseUrl.charAt(i)==aUrlToReduce.charAt(i); i++)
        {
            urlIntersectionBuilder.append(aBaseUrl.charAt(i));
        }

        String urlIntersection = urlIntersectionBuilder.toString();
        String result;
        if(urlIntersection.isEmpty())
        {
            result = aUrlToReduce;
        }else {
            result = aUrlToReduce.replace(urlIntersection,String.format(URL_INTERSECTION_REDUCE_PATTERN,urlIntersection.length()));
        }
        return result;
    }

    private static String geolocationsToStirng(Collection<GeoLocations> aGeoLocations)
    {
        StringBuilder geolocationsStringBuilder = new StringBuilder();
        for(GeoLocations geoLocation : aGeoLocations)
        {
            geolocationsStringBuilder.append(geoLocation.getDescription());
            geolocationsStringBuilder.append(GEO_SPLITTERATOR);
        }
        geolocationsStringBuilder.deleteCharAt(geolocationsStringBuilder.lastIndexOf(String.valueOf(GEO_SPLITTERATOR)));
        return geolocationsStringBuilder.toString();
    }

    private static String durationToString(final Duration aDuration)
    {
        return String.format(DURATION_FORMAT, aDuration.get(ChronoUnit.HOURS), aDuration.get(ChronoUnit.MINUTES), aDuration.get(ChronoUnit.SECONDS));
    }

    private static void appendEnd(final StringBuilder fakeJsonBuilder)
    {
        fakeJsonBuilder.append(SPLITTERATOR);
        fakeJsonBuilder.append(System.lineSeparator());
    }
}
