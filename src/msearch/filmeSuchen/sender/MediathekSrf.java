/*
 * MediathekView
 * Copyright (C) 2008 W. Xaver
 * W.Xaver[at]googlemail.com
 * http://zdfmediathk.sourceforge.net/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package msearch.filmeSuchen.sender;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import msearch.daten.DatenFilm;
import msearch.tool.MSConfig;
import msearch.filmeSuchen.MSFilmeSuchen;
import msearch.filmeSuchen.MSGetUrl;
import msearch.tool.MSConst;
import msearch.tool.MSLog;
import msearch.tool.MSStringBuilder;
import org.apache.commons.lang3.StringEscapeUtils;

public class MediathekSrf extends MediathekReader implements Runnable {

    public final static String SENDERNAME = "SRF";
    private final static int MAX_FILME_THEMA = 5;
    private final static int URL_ENTRY = 0;
    private final static int THEME_ENTRY = 1;
    private final static int URL_THEME = 2;

    /**
     * Class for local Exceptions
     */
    static class SRFException extends Exception {

        public SRFException(String message) {
            super(message);
        }

        public SRFException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public MediathekSrf(MSFilmeSuchen ssearch, int startPrio) {
        super(ssearch, SENDERNAME,/* threads */ 3, /* urlWarten */ 400, startPrio);
    }

    /*
     * Pings a HTTP URL. This effectively sends a GET request (HEAD is blocked)
     * and returns <code>true</code> if the response code is in the 200-399
     * range. Response Codes >=400 are logged for debug purposes (except 404) If
     * the response code is 403, it will be pinged again with a differen
     * base-uri
     *
     */
    private static final String OLD_URL = "https://srfvodhd-vh.akamaihd.net";
    private static final String NEW_URL = "http://hdvodsrforigin-f.akamaihd.net";

    public static boolean ping(String url) throws SRFException {

        url = url.replaceFirst("https", "http"); // Otherwise an exception may be thrown on invalid SSL certificates.

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(1000); //1000ms timeout for connect, read timeout to infinity
            connection.setReadTimeout(0);
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            if (responseCode > 399 && responseCode != HttpURLConnection.HTTP_NOT_FOUND) {

                if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
                    throw new SRFException("TEST");
                }
                //MSLog.debugMeldung("SRF: " + responseCode + " + responseCode " + "Url " + url);
                return false;
            }
            return (200 <= responseCode && responseCode <= 399);

        } catch (IOException exception) {
            return false;
        }
    }

    @Override
    public void addToList() {
        //Liste von http://www.srf.ch/player/tv/sendungen?displayedKey=Alle holen
        //<a class="sendung_name" href="/player/tv/sendung/1-gegen-100?id=6fd27ab0-d10f-450f-aaa9-836f1cac97bd">1 gegen 100</a>
        //<a class="sendung_name" href="/play/tv/sendung/aeschbacher?id=0a7932df-dea7-4d8a-bd35-bba2fe2798b5">Aeschbacher</a></h3>
        // -> http://www.srf.ch/play/tv/sendung/aeschbacher?id=0a7932df-dea7-4d8a-bd35-bba2fe2798b5
        // http://www.srf.ch/play/tv/episodesfromshow?id=0a7932df-dea7-4d8a-bd35-bba2fe2798b5&pageNumber=1
        final String MUSTER = "sendung_name\" href=\"";
        final String PATTERN_END = "\"";
        final String THEME_PATTERN_START = ">";
        final String THEME_PATTERN_END = "<";
        final String URL_PREFIX = "http://srf.ch";
        MSStringBuilder seite = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        listeThemen.clear();
        meldungStart();
        seite = getUrlIo.getUri_Utf(SENDERNAME, "http://www.srf.ch/play/tv/sendungen?displayedKey=Alle", seite, "");
        int pos = 0;
        int pos1;
        String url, urlThema;
        String thema;

        while ((pos = seite.indexOf(MUSTER, pos)) != -1) {
            pos1 = pos;
            pos += MUSTER.length();

            urlThema = URL_PREFIX + seite.extract(MUSTER, PATTERN_END, pos1);
            String id = urlThema.substring(urlThema.indexOf("id="));
            url = "http://www.srf.ch/play/tv/episodesfromshow?" + id + "&pageNumber=1";
            thema = seite.extract(THEME_PATTERN_START, THEME_PATTERN_END, pos1);
            listeThemen.addUrl(new String[]{url, thema, urlThema});
        }

        if (MSConfig.getStop()) {
            meldungThreadUndFertig();
        } else if (listeThemen.size() == 0) {
            meldungThreadUndFertig();
        } else {
            meldungAddMax(listeThemen.size());
            for (int t = 0; t < maxThreadLaufen; ++t) {
                Thread th = new Thread(new ThemaLaden());
                th.setName(SENDERNAME + t);
                th.start();
            }
        }
    }

    private class ThemaLaden implements Runnable {

        MSGetUrl getUrl = new MSGetUrl(wartenSeiteLaden);

        private MSStringBuilder film_website = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        MSStringBuilder overviewPageFilm = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        private final static String PATTERN_URL = "\"url\":\"";
        private final static String PATTERN_URL_END = "\"";

        @Override
        public void run() {
            try {
                meldungAddThread();
                String link[];

                while (!MSConfig.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    meldungProgress(link[URL_ENTRY] /* url */);
                    addFilme(link[THEME_ENTRY], link[URL_ENTRY] /* url */, link[URL_THEME] /*urlThema*/);
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(-832002877, MSLog.FEHLER_ART_MREADER, "MediathekSf.SfThemaLaden.run", ex);
            }
            meldungThreadUndFertig();
        }

        private void addFilme(String thema, final String strUrlFeed, String urlThema) {

            meldung(strUrlFeed);
            try {
                overviewPageFilm = getUrl.getUri_Utf(SENDERNAME, strUrlFeed, overviewPageFilm, "");
                addFilmsFromPage(overviewPageFilm, thema, urlThema);
                if (MSConfig.senderAllesLaden) {
                    String url = strUrlFeed.substring(0, strUrlFeed.indexOf("&pageNumber=1"));
                    for (int i = 2; i <= MAX_FILME_THEMA; ++i) {
                        if (overviewPageFilm.indexOf("Mehr anzeigen") == -1) {
                            break;
                        } else {
                            // dann gibts weitere Seiten
                            overviewPageFilm = getUrl.getUri_Utf(SENDERNAME, url + "&pageNumber=" + i, overviewPageFilm, "");
                            addFilmsFromPage(overviewPageFilm, thema, urlThema);
                        }
                    }
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(-195926364, MSLog.FEHLER_ART_MREADER, "MediathekSrf.addFilme", ex);
            }
        }

        private void addFilmsFromPage(MSStringBuilder page, String thema, String themePageUrl) {
            // <ul class="contributions"><li class="border_bot_true"><a href="/play/tv/-/video/aeschbacher-vom-15-01-2015?id=6b5b8863-9528-4c70-85a8-1ee92b30a642">
            final String PATTERN_ID_START = "<ul class=\"contributions\"><li class=\"border_bot_true\"><a href=\"/play/tv";
            final String PATTERN_ID_END = "\">";
            final String BASE_URL_JSON = "http://srf.ch/webservice/cvis/segment/";
            final String END_URL_JSON = "/.json?nohttperr=1";
            int pos = 0;

            while (!MSConfig.getStop() && ((pos = page.indexOf(PATTERN_ID_START, pos)) != -1)) {
                String id = page.extract(PATTERN_ID_START, PATTERN_ID_END, pos);
                id = id.substring(id.indexOf("id=") + 3);
                pos += PATTERN_ID_START.length();

                String jsonMovieUrl = BASE_URL_JSON + id + END_URL_JSON;
                addFilms(jsonMovieUrl, themePageUrl, thema);
            }
        }

//        private void addFilmsFromPeriod(String urlThema, String thema, ArrayList<Date> dateList) {
//            Calendar c = Calendar.getInstance();
//            String themePageUrl;
//
//            for (Date d : dateList) {
//                if (MSConfig.getStop()) {
//                    break;
//                }
//                c.setTime(d);
//                String year = String.valueOf(c.get(Calendar.YEAR));
//                String month = String.valueOf(c.get(Calendar.MONTH) + 1);
//                String urlPart = getPeriodPartYearMonth(year, month);
//                themePageUrl = urlThema + urlPart;
//                periodPageFilm = getUrl.getUri_Utf(SENDERNAME, themePageUrl, periodPageFilm, "");
//                addFilmsFromPage(periodPageFilm, thema, themePageUrl);
//
//            }
//
//        }
//
//        private final DateFormat df = new SimpleDateFormat("yyyy-M");
//
//        /**
//         *
//         * @param jsonArray The jsonArray as String to parse
//         * @return Returns the parsed dates (year and month) from the Array
//         * @throws IOException
//         */
//        private ArrayList<Date> parseJsonArray(String jsonArray) throws IOException {
//            // var calendarGroupYearMonth = $.parseJSON('"2013":{"1":1,"2":1,"3":1,"4":1,"5":1,"6":1,"7":1,"8":1,"9":1,"10":1,"11":1,"12":1},"2014":{"1":1,"2":1,"3":1}}');
//            JsonParser parser = jf.createParser(jsonArray);
//            JsonToken currentToken = parser.nextToken();
//            ArrayList<Date> dateList = new ArrayList<>();
//            String month;
//            String year = "";
//            final int YEAR_LENGTH = 4;
//
//            while (parser.hasCurrentToken()) {
//                if (currentToken == JsonToken.FIELD_NAME) {  //JSON FieldNames are enclosed in ""
//                    String text = parser.getText();
//                    if (text.length() == YEAR_LENGTH) {
//                        year = text;
//                    } else {
//                        month = text;
//
//                        if (!month.isEmpty()) {
//
//                            //Ignoring the current year and month, because that is the same as the overview page
//                            if ((Integer.valueOf(year) != todayYear) || (Integer.parseInt(month) != todayMonth)) {
//
//                                String str_date = year + "-" + month;
//
//                                try {
//                                    Date d = df.parse(str_date);
//                                    dateList.add(d);
//                                } catch (ParseException ex) {
//                                    MSLog.fehlerMeldung(-102306547, MSLog.FEHLER_ART_MREADER, "MediathekSrf.parseJsonArray", ex);
//                                }
//                            } else {
////                                System.out.println("Dann wars das");
//                            }
//                        }
//                    }
//                }
//                currentToken = parser.nextToken();
//
//            }
//            return dateList;
//        }
//
//        private String getPeriodPartYearMonth(String year, String month) {
//            final String PERIOD = "&period=";
//            final String PERIOD_DELIM = "-";
//
//            return PERIOD + year + PERIOD_DELIM + month;
//        }
        /**
         * This method adds the films from the json file to the film list
         *
         * @param urlFilm the json url of the film
         * @param urlWebsite the website url of the film
         * @param theme the theme name of the film
         */
        private void addFilms(String urlFilm, String urlWebsite, String theme) {

            meldung(urlFilm);

            MSStringBuilder filmPage = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
            filmPage = getUrl.getUri_Utf(SENDERNAME, urlFilm, filmPage, "");
            try {

                String date_str = "";
                String time = "";
                Date date = extractDateAndTime(filmPage);
                if (date != null) {
                    DateFormat dfDayMonthYear = new SimpleDateFormat("dd.MM.yyyy");
                    date_str = dfDayMonthYear.format(date);
                    dfDayMonthYear = new SimpleDateFormat("HH:mm:ss");
                    time = dfDayMonthYear.format(date);
                }

                String[] keywords = extractKeywords(filmPage);
                long duration = extractDuration(filmPage);
                String description = extractDescription(filmPage);
                String title = extractTitle(filmPage);
                String urlHd = extractHdUrl(filmPage, urlWebsite);
                String url_normal = getNormalUrlFromM3u8(filmPage);
                String url_small = getSmallUrlFromM3u8(filmPage);

                urlHd = urlHd.isEmpty() ? getHdUrlFromM3u8(filmPage) : urlHd;
                url_normal = url_normal.isEmpty() ? extractUrl(filmPage) : url_normal;
                url_small = url_small.isEmpty() ? extractSmallUrl(filmPage) : url_small;

                if (url_normal.isEmpty()) {
                    if (!url_small.isEmpty()) {
                        url_normal = url_small;
                        url_small = "";
                    } else {
                        // dann gibst nix
                        MSLog.fehlerMeldung(-159873540, MSLog.FEHLER_ART_MREADER, "MediathekSRf.filmLaden", "keine NORMALE Url für: " + urlWebsite + " : " + url_normal);
                        return;
                    }
                }
                // https -> http
                if (url_normal.startsWith("https")) {
                    url_normal = url_normal.replaceFirst("https", "http");
                }
                if (url_small.startsWith("https")) {
                    url_small = url_small.replaceFirst("https", "http");
                }
                if (urlHd.startsWith("https")) {
                    urlHd = urlHd.replaceFirst("https", "http");
                }
                DatenFilm film = new DatenFilm(SENDERNAME, theme, urlWebsite, title, url_normal, ""/*rtmpURL*/, date_str, time, duration, description,
                        keywords);

                if (!url_small.isEmpty()) {
                    film.addUrlKlein(url_small, "");
                }
                if (!urlHd.isEmpty()) {
                    film.addUrlHd(urlHd, "");
                }
                addFilm(film);
            } catch (Exception ex) {
                MSLog.fehlerMeldung(-556320087, MSLog.FEHLER_ART_MREADER, "MediathekSf.addFilme2", ex);
            }
        }

        private String getSmallUrlFromM3u8(MSStringBuilder page) {
            final String PATTERN_QUALITY_100 = "\"quality\":\"100\",";
            final String PATTERN_RESOLUTION = "RESOLUTION=320x180"; //480x272

            final String INDEX_0 = "index_0_av.m3u8";
            final String INDEX_1 = "index_1_av.m3u8";

            String m3u8Url = normalizeJsonUrl(subString(PATTERN_QUALITY_100, PATTERN_URL, PATTERN_URL_END, page));
            if (!m3u8Url.isEmpty()) {
                String newResolutionUrl = getUrlFromM3u8(m3u8Url, PATTERN_RESOLUTION, INDEX_1);
                if (newResolutionUrl.isEmpty()) {
                    return getUrlFromM3u8(m3u8Url, PATTERN_RESOLUTION, INDEX_0);
                }
                return newResolutionUrl;
            }

            return "";
        }

        private String getNormalUrlFromM3u8(MSStringBuilder page) {
            final String PATTERN_QUALITY_100 = "\"quality\":\"100\",";
            final String PATTERN_RESOLUTION = "RESOLUTION=640x360";
            final String INDEX_3 = "index_3_av.m3u8";
            final String INDEX_2 = "index_2_av.m3u8";

            String m3u8Url = normalizeJsonUrl(subString(PATTERN_QUALITY_100, PATTERN_URL, PATTERN_URL_END, page));
            if (!m3u8Url.isEmpty()) {
                String higherQuality = getHiqherQualityUrl(page);
                if (higherQuality.isEmpty()) {
                    String newUrlResolution = getUrlFromM3u8(m3u8Url, PATTERN_RESOLUTION, INDEX_3);

                    if (newUrlResolution.isEmpty()) {
                        return getUrlFromM3u8(m3u8Url, PATTERN_RESOLUTION, INDEX_2);
                    }
                    return newUrlResolution;
                }
                return higherQuality;
            }
            return "";

        }

        private String getHiqherQualityUrl(MSStringBuilder page) {
            final String PATTERN_QUALITY_100 = "\"quality\":\"100\",";
            final String INDEX_4 = "index_4_av.m3u8";
            final String INDEX_3 = "index_3_av.m3u8";

            final String PATTERN_RESOLUTION = "RESOLUTION=960x544";
            if (isHigherResolutionAvaiable(page)) {
                String m3u8Url = normalizeJsonUrl(subString(PATTERN_QUALITY_100, PATTERN_URL, PATTERN_URL_END, page));
                m3u8Url = buildHqUrlM3u8(m3u8Url);

                if (!m3u8Url.isEmpty()) {
                    String newResolutionUrl = getUrlFromM3u8(m3u8Url, PATTERN_RESOLUTION, INDEX_4);
                    if (newResolutionUrl.isEmpty()) {
                        return getUrlFromM3u8(m3u8Url, PATTERN_RESOLUTION, INDEX_3);
                    }
                    return newResolutionUrl;
                }

            }
            return "";
        }

        /**
         * Builds the m3u8 Url for higher quality (960x...)
         *
         * @param m3u8Url The master m3u8Url
         * @return Returns the build hiqh quality url
         */
        private String buildHqUrlM3u8(String m3u8Url) {

            final String MP4 = ".mp4";
            final String QUALITY = "q50,";
            //final String Q10 = "q10,";
            final String Q20 = "q20,";
            //final String Q30 = "q30";
            String newUrl = "";
            if (m3u8Url.contains(Q20)) {

                int posMp4 = m3u8Url.indexOf(MP4);
                newUrl = m3u8Url.substring(0, posMp4);
                newUrl = newUrl + QUALITY + m3u8Url.substring(posMp4, m3u8Url.length());
            }
            return newUrl;
        }

        private boolean isHigherResolutionAvaiable(MSStringBuilder page) {
            final String PATTERN_WIDTH_960 = "\"frame_width\":960";
            //final String PATTERN_WIDTH_640 = "\"frame_width\":640";
            //final String PATTERN_QUALITY_100 = "\"quality\":\"100\",";

            return page.indexOf(PATTERN_WIDTH_960) != -1;

        }

        private String getHdUrlFromM3u8(MSStringBuilder page) {
            final String PATTERN_QUALITY_200 = "\"quality\":\"200\",";
            final String PATTERN_RESOLUTION = "RESOLUTION=1280x720";

            final String INDEX_5 = "index_5_av.m3u8";
            final String INDEX_4 = "index_4_av.m3u8";

            String m3u8Url = normalizeJsonUrl(subString(PATTERN_QUALITY_200, PATTERN_URL, PATTERN_URL_END, page));
            if (!m3u8Url.isEmpty()) {
                String hdUrl = getUrlFromM3u8(m3u8Url, PATTERN_RESOLUTION, INDEX_5);
                if (hdUrl.isEmpty()) //Sometimes some quality Index is missing, so we have Index4 instead of Index5
                {
                    hdUrl = getUrlFromM3u8(m3u8Url, PATTERN_RESOLUTION, INDEX_4);
                }

                return hdUrl;
            }

            return "";

        }

        private String getUrlFromM3u8(String m3u8Url, String resolutionPattern, String qualityIndex) {

            final String CSMIL = "csmil/";
            String url = m3u8Url.substring(0, m3u8Url.indexOf(CSMIL)) + CSMIL + qualityIndex;

            return checkPing(url);
        }

        private String checkPing(String url) {
            try {
                if (ping(url)) {
                    return url;
                }
            } catch (SRFException ex) {
                try {
                    url = url.replace(OLD_URL, NEW_URL);
                    if (ping(url)) {
                        return url;
                    }
                } catch (SRFException ex1) {
                    MSLog.fehlerMeldung(-646490237, MSLog.FEHLER_ART_FILME_SUCHEN, "MediathekSf.checkPing", ex);
                }
            }

            return "";
        }

        private String extractHdUrl(MSStringBuilder page, String urlWebsite) {

            final String PATTERN_DL_URL_START = "button_download_img offset\" href=\"";
            final String PATTERN_DL_URL_END = "\"";

            if (isHdAvailable(page)) {
                film_website = getUrl.getUri_Utf(SENDERNAME, urlWebsite, film_website, "");

                return subString(PATTERN_DL_URL_START, PATTERN_DL_URL_END, film_website);
            }
            return "";
        }

        private boolean isHdAvailable(MSStringBuilder page) {
            final String PATTERN_HD_WIDTH = "\"frame_width\":1280";
            final String PATTERN_QUALITY_200 = "\"quality\":\"200\",";

            return page.indexOf(PATTERN_HD_WIDTH) != -1 || page.indexOf(PATTERN_QUALITY_200) != -1;

        }

        private String extractUrl(MSStringBuilder page) {
            final String PATTERN_WIDTH_640 = "\"frame_width\":640";

            return normalizeJsonUrl(subString(PATTERN_WIDTH_640, PATTERN_URL, PATTERN_URL_END, page));
        }

        private String extractSmallUrl(MSStringBuilder page) {
            final String PATTERN_WIDTH_320 = "\"frame_width\":320";
            final String PATTERN_WIDTH_384 = "\"frame_width\":384";

            String url = subString(PATTERN_WIDTH_320, PATTERN_URL, PATTERN_URL_END, page);
            if (url.isEmpty()) {
                url = subString(PATTERN_WIDTH_384, PATTERN_URL, PATTERN_URL_END, page);
            }
            return normalizeJsonUrl(url);

        }

        private long extractDuration(MSStringBuilder page) {
            int pos1, pos2;
            long duration = 0;
            final String PATTERN_DURATION = "\"mark_out\":";

            if ((pos1 = page.indexOf(PATTERN_DURATION)) != -1) {
                pos1 += PATTERN_DURATION.length();
                if ((pos2 = page.indexOf(",", pos1)) != -1) {
                    int pos3 = page.indexOf(".", pos1);
                    if (pos3 != -1 && pos3 < pos2) {
                        // we need to strip the . decimal divider
                        pos2 = pos3;
                    }
                    try {
                        String d = page.substring(pos1, pos2);
                        if (!d.isEmpty()) {
                            duration = Long.parseLong(d);
                        }
                    } catch (NumberFormatException ex) {
                        MSLog.fehlerMeldung(-646490237, MSLog.FEHLER_ART_MREADER, "MediathekSf.extractDuration", ex);
                    }
                }
            }
            return duration;
        }

        private Date extractDateAndTime(MSStringBuilder page) {
            final String PATTERN_DATE_TIME = "\"time_published\":\"";
            final String PATTERN_END = "\"";
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            String date_str = page.extract(PATTERN_DATE_TIME, PATTERN_END);

            Date date = null;
            try {
                date = formatter.parse(date_str);
            } catch (ParseException ex) {
                MSLog.fehlerMeldung(-784512304, MSLog.FEHLER_ART_MREADER, "MediathekSrf.extractDateAndTime", ex, "DAte_STR " + date_str);
            }

            return date;
        }

        private String extractThumbnail(MSStringBuilder page) {

            final String PATTERN_ID = "\"id\":\"";
            final String PATTERN_ID_END = "\",";

            String id = subString(PATTERN_ID, PATTERN_ID_END, page);

            return "http://www.srf.ch/webservice/cvis/segment/thumbnail/" + id + "?width=150";
        }

        private String extractDescription(MSStringBuilder page) {
            final String PATTERN_DESCRIPTION = "\"description_lead\":\"";
            final String PATTERN_DESC_END = "\",";
            final String PATTERN_DESC_ALTERNATIVE = "\"description\":\"";

            String description = subString(PATTERN_DESCRIPTION, PATTERN_DESC_END, page);
            if (description.isEmpty()) {
                description = subString(PATTERN_DESC_ALTERNATIVE, PATTERN_DESC_END, page);
            }

            return StringEscapeUtils.unescapeJava(description).trim();
        }

        private String extractTitle(MSStringBuilder page) {

            final String PATTERN_TITLE = "\"description_title\":\"";
            final String PATTERN_TITLE_END = "\",";

            String title = subString(PATTERN_TITLE, PATTERN_TITLE_END, page);
            return StringEscapeUtils.unescapeJava(title).trim();
        }

        private String[] extractKeywords(MSStringBuilder string) {
            LinkedList<String> l = new LinkedList<>();

            /*	"tags": {
             "user": [],
             "editor": [{
             "name": "Show",
             "count": 1
             }, {
             "name": "Susanne Kunz",
             "count": 1
             }, {
             "name": "Quiz",
             "count": 1
             }, {
             "name": "1 gegen 100",
             "count": 1
             }]
             },*/
            final String PATTERN_TAGS_START = "\"tags\":{";
            final String PATTERN_TAGS_END = "]},";
            final String PATTERN_TAG_START = "\"name\":\"";

            int pos0 = string.indexOf(PATTERN_TAGS_START);
            if (pos0 != -1) {
                pos0 += PATTERN_TAGS_START.length();
                int pos1 = string.indexOf(PATTERN_TAGS_END, pos0);
                String tags = string.substring(pos0, pos1);
                pos0 = 0;
                while ((pos0 = tags.indexOf(PATTERN_TAG_START, pos0)) != -1) {
                    pos0 += PATTERN_TAG_START.length();
                    pos1 = tags.indexOf("\",", pos0);
                    if (pos1 != -1) {
                        String tag = tags.substring(pos0, pos1);
                        l.add(tag);
                    }
                }
            }
            return l.toArray(new String[l.size()]);
        }

        private String subString(String searchPattern, String patternStart, String patternEnd, MSStringBuilder page) {
            int posSearch, pos1, pos2;
            String extracted = "";
            if ((posSearch = page.indexOf(searchPattern)) != -1) {
                if ((pos1 = page.indexOf(patternStart, posSearch)) != -1) {
                    pos1 += patternStart.length();

                    if ((pos2 = page.indexOf(patternEnd, pos1)) != -1) {
                        extracted = page.substring(pos1, pos2);

                    }
                }
            }
            return extracted;
        }

        private String subString(String patternStart, String patternEnd, MSStringBuilder page) {
            int pos1, pos2;
            String extracted = "";
            if ((pos1 = page.indexOf(patternStart)) != -1) {
                pos1 += patternStart.length();
                if ((pos2 = page.indexOf(patternEnd, pos1)) != -1) {
                    extracted = page.substring(pos1, pos2);
                }
            }
            return extracted;
        }

        private String normalizeJsonUrl(String jsonurl) {
            final String SEARCH_PATTERN = "\\/";
            final String REPLACE_PATTERN = "/";

            return jsonurl.replace(SEARCH_PATTERN, REPLACE_PATTERN);
        }
    }

}
