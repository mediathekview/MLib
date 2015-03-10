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
                MSLog.fehlerMeldung(104689736,   "wieder nichts gefunden");
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
                MSLog.fehlerMeldung(698732167,  ex, "kein Thema");
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
                MSLog.fehlerMeldung(732154698,  ex, "Weitere Seiten suchen");
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
            MSLog.fehlerMeldung(369502367,   ex, "2. Versuch");
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
                MSLog.fehlerMeldung(487326921,   ex);
            }
            meldungThreadUndFertig();
        }

        private void filmSuchen1(String strUrlFeed, String thema, boolean weiter) {
            final String MUSTER = "<div class=\"mediaCon\">";
            final String MUSTER_START = "Videos und Audios der Sendung";
            seite1 = getUrl.getUri_Utf(SENDERNAME, strUrlFeed, seite1, "");
            if (seite1.length() == 0) {
                MSLog.fehlerMeldung(765323214,   "Leere Seite: " + strUrlFeed);
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
                if (!MSConfig.senderAllesLaden) {
                    if (count > 5 && !thema.equalsIgnoreCase("FIFA WM 2014")) {
                        break;
                    }
                }
                pos += MUSTER.length();
                url = seite1.extract("documentId=", "&", pos);
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
                dauer = dauer.replace("min", "").trim();
                try {
                    if (dauer.contains(":")) {
                        String s = dauer.substring(0, dauer.indexOf(":"));
                        d = Long.parseLong(s);
                        d *= 60;
                        s = dauer.substring(dauer.indexOf(":") + 1);
                        d += Long.parseLong(s);
                    }
                } catch (Exception ex) {
                }
                urlSendung = seite1.extract("<a href=\"/tv/", "\"", pos);
                if (!urlSendung.isEmpty()) {
                    urlSendung = "http://www.ardmediathek.de/tv/" + urlSendung;
                    urlSendung = urlSendung.replace("&amp;", "&");
                }
                filmSuchen2(url, thema, titel, d, datum, zeit, urlSendung);
            }
            if (weiter && MSConfig.senderAllesLaden || weiter && thema.equalsIgnoreCase("FIFA WM 2014")) {
                // dann gehts weiter
                int maxWeiter = 0;
                int maxTh = 10;
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
                    MSLog.fehlerMeldung(915263621,  "Leere Seite: " + urlFilm);
                    return;
                }
                String url = "", urlMid = "", urlKl = "", urlHD = "";
                liste.clear();
                seite2.extractList("\"_quality\":3", "\"_stream\":\"", "\"", liste);
                seite2.extractList("\"_quality\":3", "\"_stream\":[\"", "\"", liste);
                for (String s : liste) {
                    if (s.startsWith("http")) {
                        if (s.endsWith("X.mp4")) {
                            urlHD = s;
                            url = s; // vorsichtshalber
                            continue;
                        }
                        url = s;
                        break;
                    }
                }
                liste.clear();
                seite2.extractList("\"_quality\":2", "\"_stream\":\"", "\"", liste);
                seite2.extractList("\"_quality\":2", "\"_stream\":[\"", "\"", liste);
                for (String s : liste) {
                    if (s.startsWith("http")) {
                        urlMid = s;
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

//                if (url.isEmpty()) {
//                    System.out.println("test");
//                }
//                if (urlMid.isEmpty()) {
//                    System.out.println("test");
//                }
//                if (urlKl.isEmpty()) {
//                    System.out.println("test");
//                }
                if (url.isEmpty()) {
                    url = urlMid;
                    urlMid = "";
                }
                if (url.isEmpty()) {
                    url = urlKl;
                    urlKl = "";
                }
                if (urlKl.isEmpty()) {
                    urlKl = urlMid;
                }
                if (!url.isEmpty()) {
                    String beschreibung = beschreibung(urlSendung);
                    DatenFilm f = new DatenFilm(SENDERNAME, thema, urlSendung, titel, url, ""/*urlRtmp*/, datum, zeit, dauer, beschreibung,
                            new String[]{}/*keywords*/);
                    if (!urlKl.isEmpty()) {
                        f.addUrlKlein(urlKl, "");
                    }
                    if (!urlHD.isEmpty() && !urlHD.equals(url)) {
                        f.addUrlHd(urlHD, "");
                    }
                    addFilm(f);
                } else {
                    MSLog.fehlerMeldung(784512369,   "keine URL: " + urlFilm);
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(762139874,   ex);
            }
        }

        private String beschreibung(String strUrlFeed) {
            seite3 = getUrl.getUri_Utf(SENDERNAME, strUrlFeed, seite3, "");
            if (seite3.length() == 0) {
                MSLog.fehlerMeldung(784512036,   "Leere Seite: " + strUrlFeed);
                return "";
            }
            return seite3.extract("<p class=\"subtitle\">", "<p class=\"teasertext\">", "<");
        }

    }

}
