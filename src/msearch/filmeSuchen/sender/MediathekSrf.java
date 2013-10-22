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
import msearch.filmeSuchen.MSearchFilmeSuchen;
import java.util.LinkedList;
import msearch.io.MSearchGetUrl;
import msearch.daten.MSearchConfig;
import msearch.daten.DatenFilm;
import msearch.tool.MSearchConst;
import msearch.tool.MSearchLog;
import msearch.tool.MSearchStringBuilder;
import org.apache.commons.lang3.StringEscapeUtils;

public class MediathekSrf extends MediathekReader implements Runnable {

    public static final String SENDER = "SRF";
    private final int MAX_FILME_THEMA = 5;

    public MediathekSrf(MSearchFilmeSuchen ssearch, int startPrio) {
        super(ssearch, /* name */ SENDER, /* threads */ 2, /* urlWarten */ 1000, startPrio);
    }

    /**
     * Pings a HTTP URL. This effectively sends a GET request (HEAD is blocked) and returns
     * <code>true</code> if the response code is in
     * the 200-399 range.
     * Response Codes >=400 are logged for debug purposes (except 404)
     *
     * @param url The HTTP URL to be pinged
     * @return <code>true</code> if the given HTTP URL has returned response code 200-399 on a GET request within the
     * given timeout, otherwise <code>false</code>.
     */
    public static boolean ping(String url) {
        url = url.replaceFirst("https", "http"); // Otherwise an exception may be thrown on invalid SSL certificates.

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(1000); //1000ms timeout for connect, read timeout to infinity
            connection.setReadTimeout(0);
            int responseCode = connection.getResponseCode();
            if (responseCode > 399 && responseCode != 404) {
                System.out.println(responseCode + " + responseCode " + "Url " + url);
                MSearchLog.debugMeldung("SRF: " + responseCode + " + responseCode " + "Url " + url);
                return false;
            }
            return (200 <= responseCode);

        } catch (IOException exception) {
            return false;
        }
    }

    @Override
    public void addToList() {
        //Liste von http://www.videoportal.sf.tv/sendungen holen
        //<a class="sendung_name" href="/player/tv/sendung/1-gegen-100?id=6fd27ab0-d10f-450f-aaa9-836f1cac97bd">1 gegen 100</a>
        final String MUSTER = "sendung_name\" href=\"/player/tv";
        final String MUSTER_ID = "?id=";
        MSearchStringBuilder seite = new MSearchStringBuilder(MSearchConst.STRING_BUFFER_START_BUFFER);
        listeThemen.clear();
        meldungStart();
        seite = getUrlIo.getUri_Utf(nameSenderMReader, "http://www.srf.ch/player/sendungen", seite, "");
        int pos = 0;
        int pos1;
        int pos2;
        String url;
        String thema = "";
        while ((pos = seite.indexOf(MUSTER, pos)) != -1) {
            pos += MUSTER.length();
            pos1 = pos;
            pos2 = seite.indexOf("\"", pos);
            if (pos1 != -1 && pos2 != -1) {
                url = seite.substring(pos1, pos2);
                if (url.contains(MUSTER_ID)) {
                    url = url.substring(url.indexOf(MUSTER_ID) + MUSTER_ID.length());
                } else {
                    url = "";
                }
                if (!url.equals("")) {
                    pos1 = seite.indexOf(">", pos);
                    pos2 = seite.indexOf("</a>", pos);
                    if (pos1 != -1 && pos2 != -1) {
                        thema = seite.substring(pos1 + 1, pos2);
                    }
                    String[] add = new String[]{"http://www.videoportal.sf.tv/rss/sendung?id=" + url, thema};
                    listeThemen.addUrl(add);
                } else {
                    MSearchLog.fehlerMeldung(-198620778, MSearchLog.FEHLER_ART_MREADER, "MediathekSf.addToList", "keine URL");
                }
            }
        }
        if (MSearchConfig.getStop()) {
            meldungThreadUndFertig();
        } else if (listeThemen.size() == 0) {
            meldungThreadUndFertig();
        } else {
            meldungAddMax(listeThemen.size());
            for (int t = 0; t < maxThreadLaufen; ++t) {
                //new Thread(new ThemaLaden()).start();
                Thread th = new Thread(new ThemaLaden());
                th.setName(nameSenderMReader + t);
                th.start();
            }
        }
    }

    private class ThemaLaden implements Runnable {

        MSearchGetUrl getUrl = new MSearchGetUrl(wartenSeiteLaden);
        private MSearchStringBuilder seite1 = new MSearchStringBuilder(MSearchConst.STRING_BUFFER_START_BUFFER);
        private MSearchStringBuilder seite2 = new MSearchStringBuilder(MSearchConst.STRING_BUFFER_START_BUFFER);
        private MSearchStringBuilder film_website = new MSearchStringBuilder(MSearchConst.STRING_BUFFER_START_BUFFER);
        private final String PATTERN_URL = "\"url\":\"";
        private final String PATTERN_URL_END = "\"";

        @Override
        public void run() {
            try {
                meldungAddThread();
                String link[];
                while (!MSearchConfig.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    meldungProgress(link[0] /* url */);
                    addFilme(link[1], link[0] /* url */);
                }
            } catch (Exception ex) {
                MSearchLog.fehlerMeldung(-832002877, MSearchLog.FEHLER_ART_MREADER, "MediathekSf.SfThemaLaden.run", ex);
            }
            meldungThreadUndFertig();
        }

        private void addFilme(String thema, String strUrlFeed) {



            // href="/player/tv/die-groessten-schweizer-talente/video/andrea-sutter-der-weg-ins-finale?id=06411758-1bd6-42ea-bc0e-cb8ebde3dfaa"
            // <title>Die grössten Schweizer Talente vom 17.03.2012, 20:11</title>
            // href="/player/tv/die-groessten-schweizer-talente/video/andrea-sutter-der-weg-ins-finale?id=06411758-1bd6-42ea-bc0e-cb8ebde3dfaa"&gt;Andrea Sutter - der Weg ins Finale&lt;/a&gt;&lt;/li&gt;
            // oder <link>http://www.srf.ch/player/tv/dok-panamericana/video/panamericana-vom-machu-piccu-in-peru-nach-bolivien-67?id=09f2cb4d-c5be-4809-9c9c-2d4cc703ad00</link>
            final String MUSTER_TITEL = "&gt;"; //bis zum &
            final String MUSTER_URL = "href=\"/player/tv"; //bis zum ;
            final String MUSTER_URL_NEU = "<link>http://www.srf.ch/player/tv"; //bis zum <
            final String MUSTER_ID = "?id=";
            final String MUSTER_ITEM_1 = "<item>";
            final String MUSTER_ITEM_2 = "</item>";
            final String MUSTER_DATUM = "<title>";
            final String BASE_URL_JSON = "http://srf.ch/webservice/cvis/segment/";
            meldung(strUrlFeed);
            seite1 = getUrl.getUri_Utf(nameSenderMReader, strUrlFeed, seite1, "");
//            String s = seite.toString();
//            s = s.replace("&lt;", "<");
//            s = s.replace("&gt;", ">");
//            seite.setLength(0);
//            seite.append(s);
            try {
                int counter = 0;
                int posItem1 = 0;
                int posItem2 = 0;
                int pos = 0;
                int pos1;
                int pos2;
                String url;
                String urlWebsite;
                String datum = "";
                String zeit = "";
                String titel;
                String tmp;
                while (!MSearchConfig.getStop() && (MSearchConfig.senderAllesLaden || counter < MAX_FILME_THEMA) && (posItem1 = seite1.indexOf(MUSTER_ITEM_1, posItem1)) != -1) {
                    posItem1 += MUSTER_ITEM_1.length();
//                    posItem2 = seite1.indexOf(MUSTER_ITEM_2, posItem1);
                    ++counter;
                    titel = "";
                    if ((pos1 = seite1.indexOf(MUSTER_DATUM, posItem1)) != -1) {
                        pos1 += MUSTER_DATUM.length();
                        if ((pos2 = seite1.indexOf("<", pos1)) != -1) {
                            tmp = seite1.substring(pos1, pos2);
                            if (tmp.contains("vom")) {
                                titel = tmp.substring(0, tmp.indexOf("vom")).trim();
                                tmp = tmp.substring(tmp.indexOf("vom") + 3);
                                if (tmp.contains(",")) {
                                    datum = tmp.substring(0, tmp.indexOf(",")).trim();
                                    zeit = tmp.substring(tmp.indexOf(",") + 1).trim() + ":00";
                                    titel = titel + " vom: " + datum;
                                }
                            }
                        }
                    }
                    if ((pos1 = seite1.indexOf(MUSTER_URL_NEU, posItem1)) != -1) {
                        // neu
                        pos1 += MUSTER_URL_NEU.length();
                        if ((pos2 = seite1.indexOf("<", pos1)) != -1) {
                            url = seite1.substring(pos1, pos2);
                            if (url.contains(MUSTER_ID)) {
                                urlWebsite = "http://www.srf.ch/player/tv" + url;
                                url = url.substring(url.indexOf(MUSTER_ID) + MUSTER_ID.length());
                                if (!url.equals("")) {
                                    if (titel.equals("")) {
                                        titel = thema;
                                    }
                                    addFilme2(thema, urlWebsite, BASE_URL_JSON + url + "/.json", titel, datum, zeit);
                                } else {
                                    MSearchLog.fehlerMeldung(-499556023, MSearchLog.FEHLER_ART_MREADER, "MediathekSf.addFilme", "keine URL: " + strUrlFeed);
                                }
                            }
                        }
                    } else {
                        System.out.println("RelativeUrls");
                    }
                }
            } catch (Exception ex) {
                MSearchLog.fehlerMeldung(-795638103, MSearchLog.FEHLER_ART_MREADER, "MediathekSf.addFilme", ex);
            }
        }

        private void addFilme2(String thema, String urlWebsite, String urlFilm, String titel, String datum, String zeit) {

            meldung(urlFilm);
            seite2 = getUrl.getUri_Utf(nameSenderMReader, urlFilm, seite2, "");
            try {

                String[] keywords = extractKeywords(seite2);
                String thumbOrImage = extractThumbnail(seite2);
                long duration = extractDuration(seite2);
                String description = extractDescription(seite2);
                String title = extractTitle(seite2);
                String urlHd = extractHdUrl(seite2, urlWebsite);
                String url_normal = getNormalUrlFromM3u8(seite2);
                String url_small = getSmallUrlFromM3u8(seite2);

                urlHd = urlHd.isEmpty() ? getHdUrlFromM3u8(seite2) : urlHd;
                url_normal = url_normal.isEmpty() ? extractUrl(seite2) : url_normal;
                url_small = url_small.isEmpty() ? extractSmallUrl(seite2) : url_small;

                if (url_normal.isEmpty()) {
                    if (!url_small.isEmpty()) {
                        url_normal = url_small;
                        url_small = "";
                    } else {
                        // dann gibst nix
                        MSearchLog.fehlerMeldung(-159873540, MSearchLog.FEHLER_ART_MREADER, "MediathekSRf.filmLaden", "keine NORMALE Url für: " + urlWebsite + " : " + url_normal);
                        return;
                    }
                }

                DatenFilm film = new DatenFilm(nameSenderMReader, thema, urlWebsite, title, url_normal, ""/*rtmpURL*/, datum, zeit, duration, description,
                        thumbOrImage, keywords);

                if (!url_small.isEmpty()) {
                    film.addUrlKlein(url_small, "");
                } else {
                    MSearchLog.fehlerMeldung(-159873540, MSearchLog.FEHLER_ART_MREADER, "MediathekArd.SRF", "keine kleine Url für: " + urlWebsite + " : " + url_normal);
                }
                if (!urlHd.isEmpty()) {
                    film.addUrlHd(urlHd, "");
                }
                addFilm(film);
            } catch (Exception ex) {
                MSearchLog.fehlerMeldung(-556320087, MSearchLog.FEHLER_ART_MREADER, "MediathekSf.addFilme2", ex);
            }
        }

        private String getSmallUrlFromM3u8(MSearchStringBuilder page) {
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

        private String getNormalUrlFromM3u8(MSearchStringBuilder page) {
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

        private String getHiqherQualityUrl(MSearchStringBuilder page) {
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
            final String Q10 = "q10,";
            final String Q20 = "q20,";
            final String Q30 = "q30";
            String newUrl = "";
            if (m3u8Url.indexOf(Q20) != -1) {

                int posMp4 = m3u8Url.indexOf(MP4);
                newUrl = m3u8Url.substring(0, posMp4);
                newUrl = newUrl + QUALITY + m3u8Url.substring(posMp4, m3u8Url.length());
            }
            return newUrl;
        }

        private boolean isHigherResolutionAvaiable(MSearchStringBuilder page) {
            final String PATTERN_WIDTH_960 = "\"frame_width\":960";
            final String PATTERN_WIDTH_640 = "\"frame_width\":640";
            final String PATTERN_QUALITY_100 = "\"quality\":\"100\",";

            return page.indexOf(PATTERN_WIDTH_960) != -1;

        }

        private String getHdUrlFromM3u8(MSearchStringBuilder page) {
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

            if (ping(url)) {
                return url;
            }

            return "";
        }

        private String extractHdUrl(MSearchStringBuilder page, String urlWebsite) {

            final String PATTERN_DL_URL_START = "button_download_img offset\" href=\"";
            final String PATTERN_DL_URL_END = "\"";

            if (isHdAvailable(page)) {
                film_website = getUrl.getUri_Utf(nameSenderMReader, urlWebsite, film_website, "");

                String dlUrl = subString(PATTERN_DL_URL_START, PATTERN_DL_URL_END, film_website);
                return dlUrl;
            }
            return "";
        }

        private boolean isHdAvailable(MSearchStringBuilder page) {
            final String PATTERN_HD_WIDTH = "\"frame_width\":1280";
            final String PATTERN_QUALITY_200 = "\"quality\":\"200\",";

            return page.indexOf(PATTERN_HD_WIDTH) != -1 || page.indexOf(PATTERN_QUALITY_200) != -1;

        }

        private String extractUrl(MSearchStringBuilder page) {
            final String PATTERN_WIDTH_640 = "\"frame_width\":640";

            return normalizeJsonUrl(subString(PATTERN_WIDTH_640, PATTERN_URL, PATTERN_URL_END, page));
        }

        private String extractSmallUrl(MSearchStringBuilder page) {
            final String PATTERN_WIDTH_320 = "\"frame_width\":320";
            final String PATTERN_WIDTH_384 = "\"frame_width\":384";

            String url = subString(PATTERN_WIDTH_320, PATTERN_URL, PATTERN_URL_END, page);
            if (url.isEmpty()) {
                url = subString(PATTERN_WIDTH_384, PATTERN_URL, PATTERN_URL_END, page);
            }
            return normalizeJsonUrl(url);

        }

        private long extractDuration(MSearchStringBuilder page) {
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
                        MSearchLog.fehlerMeldung(-646490237, MSearchLog.FEHLER_ART_MREADER, "MediathekSf.extractDuration", ex);
                    }
                }
            }
            return duration;
        }

        private String extractThumbnail(MSearchStringBuilder page) {

            final String PATTERN_ID = "\"id\":\"";
            final String PATTERN_ID_END = "\",";

            String id = subString(PATTERN_ID, PATTERN_ID_END, page);
            String thumbnail = "http://www.srf.ch/webservice/cvis/segment/thumbnail/" + id + "?width=150";

            return thumbnail;
        }

        private String extractDescription(MSearchStringBuilder page) {
            final String PATTERN_DESCRIPTION = "\"description_lead\":\"";
            final String PATTERN_DESC_END = "\",";
            final String PATTERN_DESC_ALTERNATIVE = "\"description\":\"";

            String description = subString(PATTERN_DESCRIPTION, PATTERN_DESC_END, page);
            if (description.isEmpty()) {
                description = subString(PATTERN_DESC_ALTERNATIVE, PATTERN_DESC_END, page);
            }

            return StringEscapeUtils.unescapeJava(description).trim();
        }

        private String extractTitle(MSearchStringBuilder page) {

            final String PATTERN_TITLE = "\"description_title\":\"";
            final String PATTERN_TITLE_END = "\",";

            String title = subString(PATTERN_TITLE, PATTERN_TITLE_END, page);
            return StringEscapeUtils.unescapeJava(title).trim();
        }

        private String[] extractKeywords(MSearchStringBuilder string) {
            LinkedList<String> l = new LinkedList<String>();

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

        private String subString(String searchPattern, String patternStart, String patternEnd, MSearchStringBuilder page) {
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

        private String subString(String patternStart, String patternEnd, MSearchStringBuilder page) {
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
