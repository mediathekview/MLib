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
import java.util.ArrayList;
import java.util.Date;
import msearch.daten.DatenFilm;
import msearch.filmeSuchen.MSFilmeSuchen;
import msearch.filmeSuchen.MSGetUrl;
import msearch.tool.MSConfig;
import msearch.tool.MSConst;
import msearch.tool.MSLog;
import msearch.tool.MSStringBuilder;
import org.apache.commons.lang3.StringEscapeUtils;

public class MediathekSrf extends MediathekReader implements Runnable {

    public final static String SENDERNAME = "SRF";
    private final static int MAX_SEITEN_THEMA = 5;
    private final static int MAX_FILME_KURZ = 6;

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
        // data-teaser-title="1 gegen 100"
        // data-teaser-url="/sendungen/1gegen100"
        final String MUSTER = "{\"id\":\"";
        MSStringBuilder seite = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        listeThemen.clear();
        meldungStart();
        seite = getUrlIo.getUri_Utf(SENDERNAME, "http://www.srf.ch/play/tv/atozshows/list?layout=json", seite, "");
        int pos = 0;
        int pos1;
        String thema, id;

        while ((pos = seite.indexOf(MUSTER, pos)) != -1) {
            pos += MUSTER.length();
            if ((pos1 = seite.indexOf("\"", pos)) != -1) {
                id = seite.substring(pos, pos1);
                if (id.length() < 10) {
                    //{"id":"A","title":"A","contai....
                    continue;
                }
                if (!id.isEmpty()) {
                    thema = seite.extract("\"title\":\"", "\"", pos1);
                    thema = StringEscapeUtils.unescapeJava(thema).trim();
                    listeThemen.addUrl(new String[]{id, thema});
                }
            }
        }
////        ////////////////////
//        for (String[] s : listeThemen) {
//            System.out.print(s[0] + "       " + s[1] + "\n");
//        }

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
        private final ArrayList<String> urlList = new ArrayList<>();
        private final ArrayList<String> filmList = new ArrayList<>();

        @Override
        public void run() {
            try {
                meldungAddThread();
                String link[];

                while (!MSConfig.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    meldungProgress(link[0] /* url */);
                    addFilme(link[0]/*url*/, link[1]/*thema*/);
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(832002877, ex);
            }
            meldungThreadUndFertig();
        }

        private void addFilme(String urlThema, String thema) {

            try {
                String urlFeed = "http://www.srf.ch/play/tv/episodesfromshow?id=" + urlThema + "&pageNumber=1&layout=json";
                overviewPageFilm = getUrl.getUri_Utf(SENDERNAME, urlFeed, overviewPageFilm, "");
                addFilmsFromPage(overviewPageFilm, thema, urlFeed);
                if (MSConfig.senderAllesLaden) {
                    String url = urlFeed.substring(0, urlFeed.indexOf("&pageNumber=1"));
                    for (int i = 2; i <= MAX_SEITEN_THEMA; ++i) {
                        if (overviewPageFilm.indexOf("pageNumber=" + i) == -1) {
                            break;
                        } else {
                            // dann gibts weitere Seiten
                            overviewPageFilm = getUrl.getUri_Utf(SENDERNAME, url + "&pageNumber=" + i + "&layout=json", overviewPageFilm, "");
                            addFilmsFromPage(overviewPageFilm, thema, urlThema);
                        }
                    }
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(195926364, ex);
            }
        }

        private void addFilmsFromPage(MSStringBuilder page, String thema, String themePageUrl) {
            final String BASE_URL_JSON = "http://il.srgssr.ch/integrationlayer/1.0/ue/srf/video/play/";
            //final String END_URL_JSON = ".jsonp?callback=_jqjsp";
            final String END_URL_JSON = ".jsonp";
            int count = 0;
            filmList.clear();
            page.extractList("{\"id\":\"", "?id=", "\"", filmList);

            for (String id : filmList) {
                if (MSConfig.getStop()) {
                    break;
                }
                ++count;
                if (!MSConfig.senderAllesLaden && count > MAX_FILME_KURZ) {
                    break;
                }
                //http://www.srf.ch/play/tv/episodesfromshow?id=c38cc259-b5cd-4ac1-b901-e3fddd901a3d&pageNumber=1&layout=json
                String jsonMovieUrl = BASE_URL_JSON + id + END_URL_JSON;
                addFilms(jsonMovieUrl, themePageUrl, thema);
            }
        }

        /**
         * This method adds the films from the json file to the film list
         *
         * @param jsonMovieUrl the json url of the film
         * @param themePageUrl the website url of the film
         * @param theme the theme name of the film
         */
        private void addFilms(String jsonMovieUrl, String themePageUrl, String theme) {

            meldung(jsonMovieUrl);

            MSStringBuilder filmPage = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
            filmPage = getUrl.getUri_Utf(SENDERNAME, jsonMovieUrl, filmPage, "");
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

                long duration = extractDuration(filmPage);
                String description = filmPage.extract("\"description\": \"", "\"");
                description = StringEscapeUtils.unescapeJava(description).trim();
                String title = filmPage.extract("AssetMetadatas", "\"title\": \"", "\"");

                String urlThema = filmPage.extract("\"homepageUrl\": \"", "\"");
                if (urlThema.isEmpty()) {
                    urlThema = "http://www.srf.ch/play/tv/sendungen";
                }
                String subtitle = filmPage.extract("\"TTMLUrl\": \"", "\"");

                String urlHD = getUrl(filmPage, "\"@quality\": \"HD\"", "\"text\": \"");
                String url_normal = getUrl(filmPage, "\"@quality\": \"SD\"", "\"text\": \"");

                // https -> http
                if (url_normal.startsWith("https")) {
                    url_normal = url_normal.replaceFirst("https", "http");
                }
                if (urlHD.startsWith("https")) {
                    urlHD = urlHD.replaceFirst("https", "http");
                }
                if (url_normal.isEmpty()) {
                    MSLog.fehlerMeldung(962101451, "Keine URL: " + jsonMovieUrl);
                } else {
                    DatenFilm film = new DatenFilm(SENDERNAME, theme, urlThema, title, url_normal, ""/*rtmpURL*/, date_str, time, duration, description);

                    if (!urlHD.isEmpty()) {
                        film.addUrlHd(urlHD, "");
                    }
                    if (!subtitle.isEmpty()) {
                        film.addUrlSubtitle(subtitle);
                    }
                    addFilm(film);
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(556320087, ex);
            }
        }

        private String getUrl(MSStringBuilder filmPage, String s1, String s2) {
            String url = "";
            String m3u8 = "";
            urlList.clear();
            filmPage.extractList(s1, s2, "\"", urlList);

            for (String u : urlList) {
                if (!u.endsWith("m3u8") && !u.endsWith("mp4")) {
                    continue;
                }
                if (u.endsWith("m3u8")) {
                    m3u8 = u;
                }
                if (u.endsWith("mp4")) {
                    url = u;
                    break;
                }
            }
            if (url.isEmpty()) {
                url = m3u8;
            }
            return url;
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
                    MSLog.fehlerMeldung(646490237, ex);
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
            long duration = 0;
            final String PATTERN_DURATION = "\"duration\":";
            String d = page.extract(PATTERN_DURATION, ",").trim();
            try {
                if (!d.isEmpty()) {
                    duration = Long.parseLong(d);
                    duration = duration/1000; //ms
                }
            } catch (NumberFormatException ex) {
                MSLog.fehlerMeldung(646490237, ex);
            }
            return duration;
        }

        private Date extractDateAndTime(MSStringBuilder page) {
            final String PATTERN_DATE_TIME = "\"publishedDate\": \"";
            final String PATTERN_END = "\"";
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");//2014-12-12T09:45:00+02:00

            String date_str = page.extract("\"AssetMetadatas\"", PATTERN_DATE_TIME, PATTERN_END);

            Date date = null;
            try {
                date = formatter.parse(date_str);
            } catch (ParseException ex) {
                MSLog.fehlerMeldung(784512304, ex, "Date_STR " + date_str);
            }

            return date;
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
