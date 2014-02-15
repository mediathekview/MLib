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

import java.util.LinkedList;
import msearch.daten.DatenFilm;
import msearch.daten.MSearchConfig;
import msearch.filmeSuchen.MSearchFilmeSuchen;
import msearch.io.MSearchGetUrl;
import msearch.tool.MSearchConst;
import msearch.tool.MSearchLog;
import msearch.tool.MSearchStringBuilder;
import org.apache.commons.lang3.StringEscapeUtils;

public class MediathekSwr extends MediathekReader implements Runnable {

    private static final int wartenKurz = 2000;
    private static final int wartenLang = 4000;

    public static final String SENDER = "SWR";

    public MediathekSwr(MSearchFilmeSuchen ssearch, int startPrio) {
        super(ssearch, /* name */ SENDER, /* threads */ 2, /* urlWarten */ wartenLang, startPrio);
    }

    //===================================
    // public
    //===================================
    @Override
    public synchronized void addToList() {
        meldungStart();
        //Theman suchen
        listeThemen.clear();
        addToList__("http://swrmediathek.de/tvlist.htm");
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

    //===================================
    // private
    //===================================
    private void addToList__(String ADRESSE) {
        //Theman suchen
        final String MUSTER_URL = "<a href=\"tvshow.htm?show=";
        final String MUSTER_THEMA = "title=\"";
        MSearchStringBuilder strSeite = new MSearchStringBuilder(MSearchConst.STRING_BUFFER_START_BUFFER);
        strSeite = getUrlIo.getUri(nameSenderMReader, ADRESSE, MSearchConst.KODIERUNG_UTF, 2, strSeite, "");
        int pos = 0;
        int pos1;
        int pos2;
        String url;
        String thema = "";
        while (!MSearchConfig.getStop() && (pos = strSeite.indexOf(MUSTER_URL, pos)) != -1) {
            pos += MUSTER_URL.length();
            pos1 = pos;
            pos2 = strSeite.indexOf("\"", pos);
            if (pos1 != -1 && pos2 != -1 && pos1 != pos2) {
                url = strSeite.substring(pos1, pos2);
                pos = pos2;
                pos = strSeite.indexOf(MUSTER_THEMA, pos);
                pos += MUSTER_THEMA.length();
                pos1 = pos;
                pos2 = strSeite.indexOf("\"", pos);
                if (pos1 != -1 && pos2 != -1) {
                    thema = strSeite.substring(pos1, pos2);
                    thema = StringEscapeUtils.unescapeHtml4(thema.trim()); //wird gleich benutzt und muss dann schon stimmen
                }
                if (url.equals("")) {
                    MSearchLog.fehlerMeldung(-163255009, MSearchLog.FEHLER_ART_MREADER, "MediathekSwr.addToList__", "keine URL");
                } else {
                    //url = url.replace("&amp;", "&");
                    String[] add = new String[]{"http://swrmediathek.de/tvshow.htm?show=" + url, thema};
                    listeThemen.addUrl(add);
                }
            }
        }
    }

    private class ThemaLaden implements Runnable {

        MSearchGetUrl getUrlThemaLaden = new MSearchGetUrl(MSearchConfig.senderAllesLaden ? wartenLang : wartenKurz);
        private MSearchStringBuilder strSeite1 = new MSearchStringBuilder(MSearchConst.STRING_BUFFER_START_BUFFER);
        private MSearchStringBuilder strSeite2 = new MSearchStringBuilder(MSearchConst.STRING_BUFFER_START_BUFFER);

        public ThemaLaden() {
        }

        @Override
        public void run() {
            try {
                meldungAddThread();
                String[] link = null;
                while (!MSearchConfig.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    themenSeitenSuchen(link[0] /* url */, link[1] /* Thema */);
                    meldungProgress(link[0]);
                }
            } catch (Exception ex) {
                MSearchLog.fehlerMeldung(-739285690, MSearchLog.FEHLER_ART_MREADER, "MediathekSwr.SenderThemaLaden.run", ex);
            }
            meldungThreadUndFertig();
        }

        private void themenSeitenSuchen(String strUrlFeed, String thema) {
            final String MUSTER_URL = "<li><a class=\"plLink\" href=\"player.htm?show=";
            //strSeite1 = getUrl.getUri_Utf(nameSenderMReader, strUrlFeed, strSeite1, thema);
            strSeite1 = getUrlThemaLaden.getUri(nameSenderMReader, strUrlFeed, MSearchConst.KODIERUNG_UTF, 2 /* versuche */, strSeite1, thema);
            meldung(strUrlFeed);
            int pos1 = 0;
            int pos2;
            String url;
            int max = 0;
            while (!MSearchConfig.getStop() && (pos1 = strSeite1.indexOf(MUSTER_URL, pos1)) != -1) {
                if (!MSearchConfig.senderAllesLaden) {
                    ++max;
                    if (max > 2) {
                        break;
                    }
                } else {
                    ++max;
                    if (max > 20) {
                        break;
                    }
                }
                pos1 += MUSTER_URL.length();
                if ((pos2 = strSeite1.indexOf("\"", pos1)) != -1) {
                    url = strSeite1.substring(pos1, pos2);
                    if (url.equals("")) {
                        MSearchLog.fehlerMeldung(-875012369, MSearchLog.FEHLER_ART_MREADER, "MediathekSwr.addFilme2", "keine URL, Thema: " + thema);
                    } else {
                        url = "http://swrmediathek.de/AjaxEntry?callback=jsonp1347979401564&ekey=" + url;
                        json(strUrlFeed, thema, url);
                    }

                }

            }
        }

        private void json(String strUrlFeed, String thema, String urlJson) {
            //:"entry_media","attr":{"val0":"h264","val1":"3","val2":"rtmp://fc-ondemand.swr.de/a4332/e6/swr-fernsehen/landesschau-rp/aktuell/2012/11/582111.l.mp4",
            // oder
            // "entry_media":"http://mp4-download.swr.de/swr-fernsehen/zur-sache-baden-wuerttemberg/das-letzte-wort-podcast/20120913-2015.m.mp4"
            // oder
            // :"entry_media","attr":{"val0":"flashmedia","val1":"1","val2":"rtmp://fc-ondemand.swr.de/a4332/e6/swr-fernsehen/eisenbahn-romantik/381104.s.flv","val3":"rtmp://fc-ondemand.swr.de/a4332/e6/"},"sub":[]},{"name":"entry_media","attr":{"val0":"flashmedia","val1":"2","val2":"rtmp://fc-ondemand.swr.de/a4332/e6/swr-fernsehen/eisenbahn-romantik/381104.m.flv","val3":"rtmp://fc-ondemand.swr.de/a4332/e6/"},"sub":[]
            // "entry_title":"\"Troika-Tragödie - Verspielt die Regierung unser Steuergeld?\"
            try {
                strSeite2 = getUrlThemaLaden.getUri_Utf(nameSenderMReader, urlJson, strSeite2, "");
                if (strSeite2.length() == 0) {
                    MSearchLog.fehlerMeldung(-95623451, MSearchLog.FEHLER_ART_MREADER, "MediathekSwr.json", "Seite leer: " + urlJson);
                    return;
                }
                String title = getTitle();
                String date = getDate();
                String time = getTime();
                String description = getDescription();
                String thumbNailUrl = getThumbNailUrl();
                long duration = getDuration();
                String[] keywords = extractKeywords(strSeite2);
                String urldHd = getHDUrl();
                String normalUrl = getNormalUrl();
                String smallUrl = getSmallUrl();
                String rtmpUrl = getRtmpUrl();
                if (normalUrl.isEmpty() && smallUrl.isEmpty() && rtmpUrl.isEmpty()) {
                    MSearchLog.fehlerMeldung(-203690478, MSearchLog.FEHLER_ART_MREADER, "MediathekSwr.json", thema + " NO normal and small url:  " + urlJson);
                } else {
                    if (normalUrl.isEmpty() && !smallUrl.isEmpty()) {
                        normalUrl = smallUrl;
                    } else if (normalUrl.isEmpty()) {
                        normalUrl = rtmpUrl;
                    }
                    if (smallUrl.isEmpty()) {
                        smallUrl = getSuperSmalUrl();
                    }
                    DatenFilm film = new DatenFilm(nameSenderMReader, thema, strUrlFeed, title, normalUrl, ""/*rtmpURL*/, date, time, duration, description,
                            thumbNailUrl, keywords);

                    if (!urldHd.isEmpty()) {
                        film.addUrlHd(urldHd, "");
                    }
                    if (!smallUrl.isEmpty()) {
                        film.addUrlKlein(smallUrl, "");
                    }
                    addFilm(film);
                }
            } catch (Exception ex) {
                MSearchLog.fehlerMeldung(-939584720, MSearchLog.FEHLER_ART_MREADER, "MediathekSwr.json-3", thema + " " + urlJson);
            }
        }

        final String PATTERN_END = "\"";
        final String HTTP = "http";

        private String getTitle() {
            final String PATTERN_TITLE_START = "\"entry_title\":\"";
            final String MUSTER_TITEL_2 = "\"entry_title\":\"\\\"";
            String title = strSeite2.extract(PATTERN_TITLE_START, PATTERN_END);
            return title;
        }

        private String getDescription() {
            final String PATTERN_DESCRIPTION_START = "\"entry_descl\":\"";
            return strSeite2.extract(PATTERN_DESCRIPTION_START, PATTERN_END);
        }

        private String getThumbNailUrl() {
            final String PATTERN_THUMBNAIL_URL_START = "\"entry_image_16_9\":\"";
            return strSeite2.extract(PATTERN_THUMBNAIL_URL_START, PATTERN_END);
        }

        private String getDate() {
            final String PATTERN_DATE_START = "\"entry_pdatehd\":\"";
            String datum = strSeite2.extract(PATTERN_DATE_START, PATTERN_END);
            if (datum.length() < 10) {
                if (datum.contains(".")) {
                    if ((datum.substring(0, datum.indexOf("."))).length() != 2) {
                        datum = "0" + datum;
                    }
                }
                if (datum.indexOf(".") != datum.lastIndexOf(".")) {
                    if ((datum.substring(datum.indexOf(".") + 1, datum.lastIndexOf("."))).length() != 2) {
                        datum = datum.substring(0, datum.indexOf(".") + 1) + "0" + datum.substring(datum.indexOf(".") + 1);
                    }
                }
            }
            return datum;
        }

        private long getDuration() {
            final String PATTERN_DURATION_START = "\"entry_durat\":\"";
            String dur = null;
            long duration = 0;
            try {
                dur = strSeite2.extract(PATTERN_DURATION_START, PATTERN_END);
                if (!dur.isEmpty()) {
                    String[] parts = dur.split(":");
                    long power = 1;
                    for (int i = parts.length - 1; i >= 0; i--) {
                        duration += Long.parseLong(parts[i]) * power;
                        power *= 60;
                    }
                }
            } catch (NumberFormatException ex) {
                MSearchLog.fehlerMeldung(-679012497, MSearchLog.FEHLER_ART_MREADER, "MediathekSwr.json", "duration: " + (dur == null ? " " : duration));
            }
            return duration;
        }

        private String getTime() {
            final String PATTERN_TIME_START = "\"entry_pdateht\":\"";
            String tmp = "";
            String time = strSeite2.extract(PATTERN_TIME_START, PATTERN_END);
            if (time.length() <= 5) {
                time = time.trim() + ":00";
            }
            time = time.replace(".", ":");
            if (time.length() < 8) {
                if (time.contains(":")) {
                    if ((tmp = time.substring(0, time.indexOf(":"))).length() != 2) {
                        time = "0" + time;
                    }
                }
                if (time.indexOf(":") != time.lastIndexOf(":")) {
                    if ((tmp = time.substring(time.indexOf(":") + 1, time.lastIndexOf(":"))).length() != 2) {
                        time = time.substring(0, time.indexOf(":") + 1) + "0" + time + time.substring(time.lastIndexOf(":"));
                    }
                }
            }
            return time;
        }

        private String getHDUrl() {
            final String PATTTERN_PROT_HTTP_HD = "\"entry_media\",\"attr\":{\"val0\":\"h264\",\"val1\":\"4\",\"val2\":\"http";
            String urlWithOutprot = strSeite2.extract(PATTTERN_PROT_HTTP_HD, PATTERN_END);
            if (urlWithOutprot.isEmpty()) {
                return "";
            }
            String hdUrl = HTTP + urlWithOutprot;
            return hdUrl;
        }

        private String getNormalUrl() {
            final String PATTTERN_PROT_HTTP_L = "\"entry_media\",\"attr\":{\"val0\":\"h264\",\"val1\":\"3\",\"val2\":\"http";
            String urlWithOutprot = strSeite2.extract(PATTTERN_PROT_HTTP_L, PATTERN_END);
            if (urlWithOutprot.isEmpty()) {
                return "";
            }
            String normalUrl = HTTP + urlWithOutprot;
            return normalUrl;
        }

        private String getRtmpUrl() {
            final String PATTTERN_PROT_HTTP_L = "\"entry_media\",\"attr\":{\"val0\":\"h264\",\"val1\":\"3\",\"val2\":\"rtmp";
            String urlWithOutprot = strSeite2.extract(PATTTERN_PROT_HTTP_L, PATTERN_END);
            if (urlWithOutprot.isEmpty()) {
                return "";
            }
            String normalUrl = "rtmp" + urlWithOutprot;
            return normalUrl;
        }

        private String getSmallUrl() {
            final String PATTTERN_PROT_HTTP_M = "\"entry_media\",\"attr\":{\"val0\":\"h264\",\"val1\":\"2\",\"val2\":\"http";
            String urlWithOutprot = strSeite2.extract(PATTTERN_PROT_HTTP_M, PATTERN_END);
            if (urlWithOutprot.isEmpty()) {
                return "";
            }
            String smallUrl = HTTP + urlWithOutprot;
            return smallUrl;
        }

        private String getSuperSmalUrl() {
            final String PATTTERN_PROT_HTTP_S = "\"entry_media\",\"attr\":{\"val0\":\"h264\",\"val1\":\"1\",\"val2\":\"http";
            String urlWithOutprot = strSeite2.extract(PATTTERN_PROT_HTTP_S, PATTERN_END);
            String superSmallUrl = HTTP + urlWithOutprot;
            return superSmallUrl;
        }

        private String[] extractKeywords(MSearchStringBuilder strSeite2) {
            // {"name":"entry_keywd","attr":{"val":"Fernsehserie"},"sub":[]}
            final String MUSTER_KEYWORD_START = "{\"name\":\"entry_keywd\",\"attr\":{\"val\":\"";
            final String MUSTER_KEYWORD_END = "\"},\"sub\":[]}";

            LinkedList<String> keywords = new LinkedList<>();
            int pos = 0;
            while ((pos = strSeite2.indexOf(MUSTER_KEYWORD_START, pos)) != -1) {
                pos += MUSTER_KEYWORD_START.length();
                int end = strSeite2.indexOf(MUSTER_KEYWORD_END, pos);
                if (end != -1) {
                    String keyword = strSeite2.substring(pos, end);
                    keywords.add(keyword);
                    pos = end;
                }
            }
            return keywords.toArray(new String[keywords.size()]);
        }
    }
}
