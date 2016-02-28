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

import java.util.ArrayList;
import msearch.daten.DatenFilm;
import msearch.filmeSuchen.MSFilmeSuchen;
import msearch.filmeSuchen.MSGetUrl;
import msearch.tool.MSConfig;
import msearch.tool.MSConst;
import msearch.tool.MSLog;
import msearch.tool.MSStringBuilder;

public class MediathekArd extends MediathekReader implements Runnable {

    public final static String SENDERNAME = "ARD";
    MSStringBuilder seiteFeed = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);

    /**
     *
     * @param ssearch
     * @param startPrio
     */
    public MediathekArd(MSFilmeSuchen ssearch, int startPrio) {
        super(ssearch, SENDERNAME,/* threads */ 5, /* urlWarten */ 250, startPrio);
    }

    @Override
    void addToList() {
        final String ADRESSE = "http://www.ardmediathek.de/tv";
        final String MUSTER_URL = "<a href=\"/tv/sendungen-a-z?buchstabe=";
        listeThemen.clear();
        MSStringBuilder seite = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        meldungStart();
        seite = getUrlIo.getUri(SENDERNAME, ADRESSE, MSConst.KODIERUNG_UTF, 5 /* versuche */, seite, "" /* Meldung */);
        if (seite.length() == 0) {
            MSLog.systemMeldung("ARD: Versuch 2");
            warten(2 * 60 /*Sekunden*/);
            seite = getUrlIo.getUri(SENDERNAME, ADRESSE, MSConst.KODIERUNG_UTF, 5 /* versuche */, seite, "" /* Meldung */);
            if (seite.length() == 0) {
                MSLog.fehlerMeldung(104689736, "wieder nichts gefunden");
            }
        }
        int pos = 0;
        int pos1;
        int pos2;
        String url = "";
        while ((pos = seite.indexOf(MUSTER_URL, pos)) != -1) {
            try {
                pos += MUSTER_URL.length();
                pos1 = pos;
                pos2 = seite.indexOf("\"", pos);
                if (pos1 != -1 && pos2 != -1) {
                    url = seite.substring(pos1, pos2);
                }
                if (url.equals("")) {
                    continue;
                }
                url = "http://www.ardmediathek.de/tv/sendungen-a-z?buchstabe=" + url;
                feedSuchen1(url);
            } catch (Exception ex) {
                MSLog.fehlerMeldung(698732167, ex, "kein Thema");
            }
        }
        if (MSConfig.getStop()) {
            meldungThreadUndFertig();
        } else if (listeThemen.size() == 0) {
            meldungThreadUndFertig();
        } else {
            meldungAddMax(listeThemen.size());
            listeSort(listeThemen, 1);
            for (int t = 0; t < maxThreadLaufen; ++t) {
                Thread th = new Thread(new ThemaLaden());
                th.setName(SENDERNAME + t);
                th.start();
            }
        }
    }

    private void feedSuchen1(String strUrlFeed) {
        final String MUSTER = "<div class=\"media mediaA\">";
        seiteFeed = getUrlIo.getUri(SENDERNAME, strUrlFeed, MSConst.KODIERUNG_UTF, 2/*max Versuche*/, seiteFeed, "");
        if (seiteFeed.length() == 0) {
            MSLog.fehlerMeldung(207956317, "Leere Seite: " + strUrlFeed);
            return;
        }
        int pos;
        String url, thema;
        pos = seiteFeed.indexOf(MUSTER);
        pos += MUSTER.length();
        while (!MSConfig.getStop() && (pos = seiteFeed.indexOf(MUSTER, pos)) != -1) {
            try {
                pos += MUSTER.length();
                url = seiteFeed.extract("<a href=\"/tv/", "\"", pos);
                if (url.equals("")) {
                    continue;
                }
                url = "http://www.ardmediathek.de/tv/" + url;
                thema = seiteFeed.extract("<h4 class=\"headline\">", "<", pos);
                if (thema.isEmpty()) {
                    thema = seiteFeed.extract("title=\"", "\"", pos);
                }
                if (thema.isEmpty()) {
                    MSLog.fehlerMeldung(132326564, "Thema: " + strUrlFeed);
                }
                String[] add = new String[]{url, thema};
                listeThemen.addUrl(add);
            } catch (Exception ex) {
                MSLog.fehlerMeldung(732154698, ex, "Weitere Seiten suchen");
            }
        }
    }

    private synchronized void warten(int i) {
        // Sekunden warten
        try {
            // war wohl nix, warten und dann nochmal
            // timeout: the maximum time to wait in milliseconds.
            long warten = i * 1000;
            this.wait(warten);
        } catch (Exception ex) {
            MSLog.fehlerMeldung(369502367, ex, "2. Versuch");
        }
    }

    private class ThemaLaden implements Runnable {

        MSGetUrl getUrl = new MSGetUrl(wartenSeiteLaden);
        ArrayList<String> liste = new ArrayList<>();
        private MSStringBuilder seite1 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder seite2 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder seite3 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);

        @Override
        public synchronized void run() {
            try {
                meldungAddThread();
                String[] link;
                while (!MSConfig.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    meldungProgress(link[0]);
                    filmSuchen1(link[0] /* url */, link[1], true);
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(487326921, ex);
            }
            meldungThreadUndFertig();
        }

        private void filmSuchen1(String strUrlFeed, String thema, boolean weiter) {
            final String MUSTER = "<div class=\"mediaCon\">";
            final String MUSTER_START = "Videos und Audios der Sendung";
            seite1 = getUrl.getUri_Utf(SENDERNAME, strUrlFeed, seite1, "");
            if (seite1.length() == 0) {
                MSLog.fehlerMeldung(765323214, "Leere Seite: " + strUrlFeed);
                return;
            }
            int pos = 0;
            String url, datum, zeit = "", titel, dauer, urlSendung;
            long d = 0;
            int count = 0;
            if ((pos = seite1.indexOf(MUSTER_START)) != -1) {
                pos += MUSTER_START.length();
            } else {
                return;
            }
            while (!MSConfig.getStop() && (pos = seite1.indexOf(MUSTER, pos)) != -1) {
                ++count;
                if (!MSConfig.loadLongMax()) {
//                    if (thema.equalsIgnoreCase("Eurovision Song Contest 2015")){
//                        System.out.println("Treffer");
//                    }
                    if (count > 5 && !thema.equalsIgnoreCase("Eurovision Song Contest 2015")
                            && !thema.equalsIgnoreCase("alpha-Centauri")) {
                        break;
                    }
                }
                pos += MUSTER.length();
                url = seite1.extract("documentId=", "&", pos);
//                if (!url.contains("\"")) {
//                    System.out.println("soso");
//                }
                if (url.contains("\"")) {
                    url = url.substring(0, url.indexOf("\""));
                }
                if (url.equals("")) {
                    continue;
                }
                url = url.replace("&amp;", "&");
                datum = seite1.extract("<p class=\"dachzeile\">", "<", pos);
                datum = datum.replace("Uhr", "").trim();
                if (datum.contains("|")) {
                    zeit = datum.substring(datum.indexOf("|") + 1).trim();
                    zeit = zeit + ":00";
                    datum = datum.substring(0, datum.indexOf("|")).trim();
                }
                titel = seite1.extract("<h4 class=\"headline\">", "<", pos);
                dauer = seite1.extract("<p class=\"subtitle\">", "<", pos);
                try {
                    dauer = dauer.replace("Min.", "").trim();
                    dauer = dauer.replace("| UT", "").trim();
                    d = Long.parseLong(dauer) * 60;
                } catch (Exception ex) {
                }
                if (d == 0) {
                    MSLog.fehlerMeldung(915263621, "Dauer==0: " + strUrlFeed);
                }
                urlSendung = seite1.extract("<a href=\"/tv/", "\"", pos);
                if (!urlSendung.isEmpty()) {
                    urlSendung = "http://www.ardmediathek.de/tv/" + urlSendung;
                    urlSendung = urlSendung.replace("&amp;", "&");
                }

                filmSuchen2(url, thema, titel, d, datum, zeit, urlSendung);
            }
            if (weiter && MSConfig.loadLongMax() || weiter && thema.equalsIgnoreCase("Eurovision Song Contest 2015")
                    || weiter && thema.equalsIgnoreCase("alpha-Centauri")) {
                // dann gehts weiter
                int maxWeiter = 0;
                int maxTh = 10;
                if (thema.equalsIgnoreCase("Eurovision Song Contest 2015")) {
                    maxTh = 20;
                }
                String urlWeiter = strUrlFeed + "&mcontents=page.";
                for (int i = 2; i < maxTh; ++i) {
                    ///tv/Abendschau/Sendung?documentId=14913430&amp;bcastId=14913430&amp;mcontents=page.2"
                    if (seite1.indexOf("&amp;mcontents=page." + i) != -1) {
                        maxWeiter = i;
                    } else {
                        break;
                    }
                }
                for (int i = 2; i < maxTh; ++i) {
                    if (i <= maxWeiter) {
                        filmSuchen1(urlWeiter + i, thema, false);
                    } else {
                        break;
                    }

                }
            }
        }

        private void filmSuchen2(String urlFilm_, String thema, String titel, long dauer, String datum, String zeit, String urlSendung) {
            // URL bauen: http://www.ardmediathek.de/play/media/21528242?devicetype=pc&features=flash
            try {
                String urlFilm = "http://www.ardmediathek.de/play/media/" + urlFilm_ + "?devicetype=pc&features=flash";
                meldung(urlFilm);
                seite2 = getUrl.getUri_Utf(SENDERNAME, urlFilm, seite2, "");
                if (seite2.length() == 0) {
                    MSLog.fehlerMeldung(915263621, "Leere Seite: " + urlFilm);
                    return;
                }
                String url = "", urlMid = "", urlKl = "", urlHD = "";
                String urlTest = "";
                liste.clear();

                url = getUrl(seite2, 2); // neuer Weg
                seite2.extractList("{\"_quality\":3,\"_server\":\"\",\"_cdn\":\"default\",\"_stream\":\"", "\"", liste);
                seite2.extractList("_quality\":3,\"_stream\":[\"", "\"", liste);
                seite2.extractList("\"_quality\":3,\"_server\":\"\",\"_cdn\":\"akamai\",\"_stream\":\"", "\"", liste);
                if (seite2.indexOf("quality\":3") >= 0) {
                    if (liste.size() <= 0) {
                        // Fehler
//                        System.out.println("Test");
                    }
                }
                for (String s : liste) {
                    if (s.startsWith("http")) {
                        urlHD = s;
                        break;
                    }
                }
                liste.clear();

                seite2.extractList("\"_quality\":2", "\"_stream\":\"", "\"", liste);
                seite2.extractList("\"_quality\":2", "\"_stream\":[\"", "\"", liste);
                for (String s : liste) {
                    if (s.startsWith("http")) {
                        if (url.isEmpty()) {
                            url = s;
                        } else {
                            urlMid = s;
                        }
                        break;
                    }
                }
                liste.clear();
                seite2.extractList("\"_quality\":1", "\"_stream\":\"", "\"", liste);
                seite2.extractList("\"_quality\":1", "\"_stream\":[\"", "\"", liste);
                for (String s : liste) {
                    if (s.startsWith("http")) {
                        urlKl = s;
                        break;
                    }
                }
                if (!urlTest.isEmpty() && (urlTest.equals(url) || urlTest.equals(urlKl))) {
                    urlHD = ""; //dann ists kein HD
//                    System.out.println("q3 test: " + urlFilm);
                }
                if (!urlTest.isEmpty() && !(urlTest.equals(url) || urlTest.equals(urlMid))) {
//                    System.out.println("q3 test: " + urlFilm);
                }
                if (url.isEmpty() && urlMid.isEmpty() && urlKl.isEmpty() && !thema.equals("alpha-Centauri")) {
//                    System.out.println("q3 test: " + urlFilm);
                }
                if (urlMid.isEmpty()) {
//                    System.out.println("q2test: " + urlFilm);
                }
                if (urlKl.isEmpty()) {
//                    System.out.println("q1 test: " + urlFilm);
                }
                if (url.isEmpty()) {
                    url = urlMid;
                    urlMid = "";
                }
                if (url.isEmpty()) {
                    url = urlKl;
                    urlKl = "";
                }
                if (url.isEmpty() && !urlHD.isEmpty()) {
                    url = urlHD;
                    urlHD = "";
                }
                if (urlKl.isEmpty()) {
                    urlKl = urlMid;
                }
                String subtitle = seite2.extract("subtitleUrl\":\"", "\"");
                if (!subtitle.isEmpty()) {
                    if (!subtitle.startsWith("http://www.ardmediathek.de")) {
                        subtitle = "http://www.ardmediathek.de" + subtitle;
                    }
                }
                if (!url.isEmpty()) {

                    //http://http-stream.rbb-online.de/rbb/rbbreporter/rbbreporter_20151125_solange_ich_tanze_lebe_ich_WEB_L_16_9_960x544.mp4?url=5
                    if (url.contains("?url=")) {
                        url = url.substring(0, url.indexOf("?url="));
                    }
                    if (urlKl.contains("?url=")) {
                        urlKl = url.substring(0, urlKl.indexOf("?url="));
                    }
                    if (urlHD.contains("?url=")) {
                        urlHD = url.substring(0, urlHD.indexOf("?url="));
                    }

                    String beschreibung = beschreibung(urlSendung);
                    DatenFilm f = new DatenFilm(SENDERNAME, thema, urlSendung, titel, url, ""/*urlRtmp*/, datum, zeit, dauer, beschreibung);
                    if (!urlKl.isEmpty()) {
                        f.addUrlKlein(urlKl, "");
                    }
                    if (!urlHD.isEmpty() && !urlHD.equals(url)) {
                        f.addUrlHd(urlHD, "");
                    }
                    if (!subtitle.isEmpty()) {
                        f.addUrlSubtitle(subtitle);
                    }
                    addFilm(f);
                } else {
                    filmSuchen_old(urlSendung, thema, titel, dauer, datum, zeit);
//                    MSLog.fehlerMeldung(784512369, "keine URL: " + urlFilm);
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(762139874, ex);
            }
        }

        private String getUrl(MSStringBuilder seite, int q) {
            String ret = "";

            seite.extractList("\"_quality\":2,\"_stream\":[", "]", liste);
            for (String s : liste) {
                //"http://mvideos.daserste.de/videoportal/Film/c_550000/557945/format653790.mp4","http://mvideos.daserste.de/videoportal/Film/c_550000/557945/format653793.mp4"
                s = s.replace("\"", "");
                if (s.contains(",")) {
                    String[] ar = s.split(",");
                    for (String ss : ar) {
                        if (ss.startsWith("http")) {
                            ret = ss;
                        }
                    }
                }
                if (!ret.isEmpty()) {
                    break;
                }
            }
//            if (!ret.isEmpty()) {
//                System.out.println("gefunden!!");
//            }
            liste.clear();
            return ret;
        }

        private void filmSuchen_old(String urlSendung, String thema, String titel, long dauer, String datum, String zeit) {
            // für ganz alte Sachen:
                /*
             </li><li data-ctrl-21282662-28534346-trigger="{&#039;xt_obj&#039;:{&#039;href&#039;
             :&#039;http://cdn-vod-ios.br.de/i/mir-live/bw1XsLzS/bLQH/bLOliLioMXZhiKT1/uLoXb69zbX06/MUJIuUOVBwQIb71S/bLWCMUJIuUOVBwQIb71S/_2rp9U1S/_-J
             S/_-4y5H1S/bc3757a9-1cfd-4aaa-a963-51e9fd0095f2_,0,A,B,E,C,.mp4.csmil/master.m3u8?__b__=200&#039;,&#039;target&#039;:&#039;_self&#039;},&#039;redirect&#0
             39;:{&#039;name&#039;:&#039;br&#039;}}">
             */
            try {
                meldung(urlSendung);
                seite2 = getUrl.getUri_Utf(SENDERNAME, urlSendung, seite2, "");
                if (seite2.length() == 0) {
                    MSLog.fehlerMeldung(612031478, "Leere Seite: " + urlSendung);
                    return;
                }
                String url = seite2.extract("</li><li data-ctrl-", "http://", ".m3u8");
                if (!url.isEmpty()) {
                    url = "http://" + url + ".m3u8";
//                    System.out.println(url);
                }
                if (!url.isEmpty()) {
                    String beschreibung = beschreibung(urlSendung);
                    DatenFilm f = new DatenFilm(SENDERNAME, thema, urlSendung, titel, url, ""/*urlRtmp*/, datum, zeit, dauer, beschreibung);
                    addFilm(f);
                } else {
                    MSLog.fehlerMeldung(974125698, "keine URL: " + urlSendung);
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(102054784, ex);
            }
        }

        private String beschreibung(String strUrlFeed) {
            seite3 = getUrl.getUri_Utf(SENDERNAME, strUrlFeed, seite3, "");
            if (seite3.length() == 0) {
                MSLog.fehlerMeldung(784512036, "Leere Seite: " + strUrlFeed);
                return "";
            }
            return seite3.extract("<p class=\"subtitle\">", "<p class=\"teasertext\" itemprop=\"description\">", "<");
        }

    }

}
