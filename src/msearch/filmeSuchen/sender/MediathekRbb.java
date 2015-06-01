/*
 *    MediathekView
 *    Copyright (C) 2008 - 2012     W. Xaver
 *                              &   thausherr
 * 
 *    W.Xaver[at]googlemail.com
 *    http://zdfmediathk.sourceforge.net/
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

public class MediathekRbb extends MediathekReader implements Runnable {

    public final static String SENDERNAME = "RBB";
    final static String ROOTADR = "http://mediathek.rbb-online.de";

    public MediathekRbb(MSFilmeSuchen ssearch, int startPrio) {
        super(ssearch, SENDERNAME,/* threads */ 2, /* urlWarten */ 500, startPrio);
    }

    @Override
    void addToList() {
        MSStringBuilder seite = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        // <a href="/tv/kurz-vor-5/Sendung?documentId=16272574&amp;bcastId=16272574" class="textLink">
        ArrayList<String> liste = new ArrayList<>();
        final String ADRESSE_1 = "http://mediathek.rbb-online.de/tv/sendungen-a-z?cluster=a-k";
        final String ADRESSE_2 = "http://mediathek.rbb-online.de/tv/sendungen-a-z?cluster=l-z";
        final String URL = "<a href=\"/tv/";
        meldungStart();
        try {
            seite = getUrlIo.getUri(SENDERNAME, ADRESSE_1, MSConst.KODIERUNG_UTF, 5 /* versuche */, seite, "" /* Meldung */);
            seite.extractList(URL, "\"", 0, "", liste);
            seite = getUrlIo.getUri(SENDERNAME, ADRESSE_2, MSConst.KODIERUNG_UTF, 5 /* versuche */, seite, "" /* Meldung */);
            seite.extractList(URL, "\"", 0, "", liste);
            for (String s : liste) {
                if (s.isEmpty() || !s.contains("documentId=")) {
                    continue;
                }
                s = "http://mediathek.rbb-online.de/tv/" + s;
                listeThemen.addUrl(new String[]{s});
            }
        } catch (Exception ex) {
            MSLog.fehlerMeldung(398214058, ex);
        }
        if (MSConfig.getStop()) {
            meldungThreadUndFertig();
        } else if (listeThemen.size() == 0) {
            meldungThreadUndFertig();
        } else {
            meldungAddMax(listeThemen.size());
            for (int t = 0; t < maxThreadLaufen; ++t) {
                //new Thread(new ThemaLaden()).start();
                Thread th = new Thread(new ThemaLaden(false /*addTage*/));
                th.setName(SENDERNAME + t);
                th.start();
            }
            meldungAddMax(7 /* Tage */);
            Thread th = new Thread(new ThemaLaden(true /*addTage*/));
            th.setName(SENDERNAME + "_Tage");
            th.start();
        }
    }

    private class ThemaLaden implements Runnable {

        boolean addTage = false;
        MSGetUrl getUrl = new MSGetUrl(wartenSeiteLaden);
        private MSStringBuilder seite1 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder seite2 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder seite3 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);

        public ThemaLaden(boolean addTage) {
            this.addTage = addTage;
        }

        @Override
        public void run() {
            String link[];
            try {
                meldungAddThread();
                if (addTage) {
                    addTage();
                } else {
                    while (!MSConfig.getStop() && (link = listeThemen.getListeThemen()) != null) {
                        meldungProgress(link[0]);
                        addThema(link[0] /* url */, true);
                    }
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(794625882, ex);
            }
            meldungThreadUndFertig();
        }

        void addTage() {
            // http://mediathek.rbb-online.de/tv/sendungVerpasst?topRessort=tv&kanal=5874&tag=0
            final String MUSTER_START = "<h2 class=\"modHeadline\">7 Tage Rückblick</h2>";
            final String MUSTER_URL = "<div class=\"media mediaA\">";
            final String URL = "<a href=\"/tv/";
            String urlTage;
            for (int i = 0; i <= 6; ++i) {
                urlTage = "http://mediathek.rbb-online.de/tv/sendungVerpasst?topRessort=tv&kanal=5874&tag=" + i;
                meldungProgress(urlTage);
                seite1 = getUrlIo.getUri_Utf(SENDERNAME, urlTage, seite1, "");
                int pos1 = seite1.indexOf(MUSTER_START);
                while (!MSConfig.getStop() && (pos1 = seite1.indexOf(MUSTER_URL, pos1)) != -1) {
                    pos1 += MUSTER_URL.length();
                    String urlSeite = seite1.extract(URL, "\"", pos1);
                    if (!urlSeite.isEmpty()) {
                        urlSeite = "http://mediathek.rbb-online.de/tv/" + urlSeite;
                        addFilme(urlSeite);
                    } else {
                        MSLog.fehlerMeldung(751203697, "keine URL für: " + urlSeite);
                    }
                }

            }
        }

        void addThema(String url, boolean weiter) {
            try {
                final String URL = "<a href=\"/tv/";
                final String MUSTER_URL = "<div class=\"media mediaA\">";
                seite1 = getUrlIo.getUri_Utf(SENDERNAME, url, seite1, "");
                int startPos = seite1.indexOf("<div class=\"entry\">");
                int pos1 = startPos;
                while (!MSConfig.getStop() && (pos1 = seite1.indexOf(MUSTER_URL, pos1)) != -1) {
                    pos1 += MUSTER_URL.length();
                    String urlSeite = seite1.extract(URL, "\"", pos1);
                    if (!urlSeite.isEmpty()) {
                        urlSeite = "http://mediathek.rbb-online.de/tv/" + urlSeite;
                        addFilme(urlSeite);
                    } else {
                        MSLog.fehlerMeldung(751203697, "keine URL für: " + url);
                    }
                }

                // noch nach weiteren Seiten suchen
                if (weiter && MSConfig.senderAllesLaden) {
                    for (int i = 2; i < 10; ++i) {
                        if (seite1.indexOf("mcontents=page." + i) != -1) {
                            // dann gibts weiter Seiten
                            addThema(url + "&mcontents=page." + i, false);
                        }
                    }
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(541236987, ex);
            }
        }

        void addFilme(String urlSeite) {
            try {
                meldung(urlSeite);
                String datum = "", zeit = "", thema, title, description, durationInSeconds;
                seite2 = getUrlIo.getUri_Utf(SENDERNAME, urlSeite, seite2, "");
                description = seite2.extract("<meta name=\"description\" content=\"", "\"");
                durationInSeconds = seite2.extract("<meta property=\"video:duration\" content=\"", "\"");
                long duration = 0;
                if (!durationInSeconds.isEmpty()) {
                    try {
                        duration = Long.parseLong(durationInSeconds);
                    } catch (Exception ex) {
                        MSLog.fehlerMeldung(200145787, ex);
                        duration = 0;
                    }
                }
                title = seite2.extract("<meta name=\"dcterms.title\" content=\"", "\"");
                thema = seite2.extract("<meta name=\"dcterms.isPartOf\" content=\"", "\"");
                String sub = seite2.extract("<p class=\"subtitle\">", "<");
                if (sub.contains("|")) {
                    datum = sub.substring(0, sub.indexOf("|") - 1);
                    datum = datum.substring(datum.indexOf(" ")).trim();
                    zeit = datum.substring(datum.indexOf(" ")).trim();
                    if (zeit.length() == 5) {
                        zeit = zeit + ":00";
                    }
                    datum = datum.substring(0, datum.indexOf(" ")).trim();
                    if (datum.length() == 8) {
                        datum = datum.substring(0, 6) + "20" + datum.substring(6);
                    }
                }

                String urlFilm = urlSeite.substring(urlSeite.indexOf("documentId=") + "documentId=".length(), urlSeite.indexOf("&"));
                // http://mediathek.rbb-online.de/play/media/24938774?devicetype=pc&features=hls
                urlFilm = "http://mediathek.rbb-online.de/play/media/" + urlFilm + "?devicetype=pc&features=hls";
                seite3 = getUrlIo.getUri_Utf(SENDERNAME, urlFilm, seite3, "");
                String urlNormal = "", urlLow = "";
                urlLow = seite3.extract("\"_quality\":1,\"_server\":\"\",\"_cdn\":\"akamai\",\"_stream\":\"http://", "\"");
                urlNormal = seite3.extract("\"_quality\":3,\"_server\":\"\",\"_cdn\":\"akamai\",\"_stream\":\"http://", "\"");
                if (urlNormal.isEmpty()) {
                    if (!urlLow.isEmpty()) {
                        urlNormal = urlLow;
                        urlLow = "";
                    }
                }

                // ,"_subtitleUrl":"/subtitle/19088","_subtitleOffset":0,
                // http://mediathek.rbb-online.de/subtitle/19088
                String subtitle = seite3.extract("subtitleUrl\":\"", "\"");
                if (!subtitle.isEmpty()) {
                    subtitle = "http://mediathek.rbb-online.de" + subtitle;
                }
                if (datum.isEmpty() || zeit.isEmpty() || thema.isEmpty() || title.isEmpty() || description.isEmpty() || durationInSeconds.isEmpty()) {
                    MSLog.fehlerMeldung(912012036, "empty für: " + urlSeite);
                }
                if (!urlNormal.isEmpty()) {
                    urlNormal = "http://" + urlNormal;
                    DatenFilm film = new DatenFilm(SENDERNAME, thema, urlSeite, title, urlNormal, "" /*urlRtmp*/,
                            datum, zeit/* zeit */, duration, description);
                    addFilm(film);
                    if (!urlLow.isEmpty()) {
                        urlLow = "http://" + urlLow;
                        film.addUrlKlein(urlLow, "");
                    }
                    if (!subtitle.isEmpty()){
                        film.addUrlSubtitle(subtitle);
                    }
                } else {
                    MSLog.fehlerMeldung(302014569, "keine URL für: " + urlSeite);
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(541236987, ex);
            }
        }

    }
}
