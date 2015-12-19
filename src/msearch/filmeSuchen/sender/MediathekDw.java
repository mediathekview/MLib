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

import msearch.daten.DatenFilm;
import msearch.filmeSuchen.MSFilmeSuchen;
import msearch.filmeSuchen.MSGetUrl;
import msearch.tool.MSConfig;
import msearch.tool.MSConst;
import msearch.tool.MSLog;
import msearch.tool.MSStringBuilder;

public class MediathekDw extends MediathekReader implements Runnable {

    public final static String SENDERNAME = "DW";

    public MediathekDw(MSFilmeSuchen ssearch, int startPrio) {
        super(ssearch, SENDERNAME, /* threads */ 4, /* urlWarten */ 500, startPrio);
    }

    @Override
    void addToList() {
        listeThemen.clear();
        meldungStart();
        sendungenLaden();
        if (MSConfig.getStop()) {
            meldungThreadUndFertig();
        } else if (listeThemen.size() == 0) {
            meldungThreadUndFertig();
        } else {
            listeSort(listeThemen, 1);
            meldungAddMax(listeThemen.size());
            for (int t = 0; t < maxThreadLaufen; ++t) {
                //new Thread(new ThemaLaden()).start();
                Thread th = new Thread(new ThemaLaden());
                th.setName(SENDERNAME + t);
                th.start();
            }
        }
    }

    private void sendungenLaden() {
        final String ADRESSE = "http://www.dw.com/de/media-center/alle-inhalte/s-100814";
        final String MUSTER_URL = "value=\"";
        final String MUSTER_START = "<div class=\"label\">Sendungen</div>";
        MSStringBuilder seite = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        seite = getUrlIo.getUri_Utf(SENDERNAME, ADRESSE, seite, "");
        int pos1, pos2;
        String url = "", thema = "";
        pos1 = seite.indexOf(MUSTER_START);
        if (pos1 == -1) {
            MSLog.fehlerMeldung(915230456, "Nichts gefunden");
            return;
        }
        pos1 += MUSTER_START.length();
        int stop = seite.indexOf("</div>", pos1);
        while ((pos1 = seite.indexOf(MUSTER_URL, pos1)) != -1) {
            if (pos1 > stop) {
                break;
            }
            try {
                pos1 += MUSTER_URL.length();
                if ((pos2 = seite.indexOf("\"", pos1)) != -1) {
                    url = seite.substring(pos1, pos2);
                }
                if (url.equals("")) {
                    continue;
                }
                if (MSConfig.loadLongMax()) {
                    //http://www.dw.com/de/media-center/alle-inhalte/s-100814/filter/programs/3204/sort/date/results/16/
                    url = "http://www.dw.com/de/media-center/alle-inhalte/s-100814/filter/programs/" + url + "/sort/date/results/100/";
                } else {
                    url = "http://www.dw.com/de/media-center/alle-inhalte/s-100814/filter/programs/" + url + "/sort/date/results/16/";
                }
                if ((pos1 = seite.indexOf(">", pos1)) != -1) {
                    pos1 += 1;
                    if ((pos2 = seite.indexOf("<", pos1)) != -1) {
                        thema = seite.substring(pos1, pos2);
                    }
                }
                // in die Liste eintragen
                String[] add = new String[]{url, thema};
                listeThemen.addUrl(add);
            } catch (Exception ex) {
                MSLog.fehlerMeldung(731245970, ex);
            }
        }

    }

    private class ThemaLaden implements Runnable {

        MSGetUrl getUrl = new MSGetUrl(wartenSeiteLaden);
        private MSStringBuilder seite1 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder seite2 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);

        @Override
        public synchronized void run() {
            try {
                meldungAddThread();
                String[] link;
                while (!MSConfig.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    meldungProgress(link[0]);
                    laden(link[0] /* url */, link[1] /* Thema */);
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(915423640, ex);
            }
            meldungThreadUndFertig();
        }

        void laden(String urlThema, String thema) {

            final String MUSTER_START = "<div class=\"news searchres hov\">";
            String urlSendung;
            meldung(urlThema);
            seite1 = getUrlIo.getUri_Utf(SENDERNAME, urlThema, seite1, "");
            int pos1 = 0;
            String titel;
            while (!MSConfig.getStop() && (pos1 = seite1.indexOf(MUSTER_START, pos1)) != -1) {
                pos1 += MUSTER_START.length();
                urlSendung = seite1.extract("<a href=\"", "\"", pos1);
                titel = seite1.extract("<h2>", "<", pos1).trim();
                if (!urlSendung.isEmpty()) {
                    laden2(urlThema, thema, titel, "http://www.dw.com" + urlSendung);
                }
            }
        }

        void laden2(String urlThema, String thema, String titel, String urlSendung) {

            final String MUSTER_START = "%22file%22%3A%22";
            String url;
            meldung(urlThema);
            seite2 = getUrlIo.getUri_Utf(SENDERNAME, urlSendung, seite2, "");
            int pos1 = 0, pos2;
            while ((pos1 = seite2.indexOf(MUSTER_START, pos1)) != -1) {
                pos1 += MUSTER_START.length();
                pos2 = seite2.indexOf("%22%7D", pos1);
                if (pos2 != -1) {
                    url = seite2.substring(pos1, pos2);
                    if (url.endsWith(".mp4")) {
                        String description = seite2.extract("<meta name=\"description\" content=\"", "\"");
                        String datum = seite2.extract("| DW.COM | ", "\"");
                        String dur = seite2.extract("<strong>Dauer</strong>", "Min.");
                        dur = dur.replace("\n", "");
                        dur = dur.replace("\r", "");
                        long duration = 0;
                        try {
                            if (!dur.equals("")) {
                                String[] parts = dur.split(":");
                                long power = 1;
                                for (int i = parts.length - 1; i >= 0; i--) {
                                    duration += Long.parseLong(parts[i]) * power;
                                    power *= 60;
                                }
                            }
                        } catch (Exception ex) {
                            MSLog.fehlerMeldung(912034567, "duration: " + dur);
                        }

                        url = url.replace("%2F", "/");
                        url = "http://tv-download.dw.com/dwtv_video/flv/" + url;
                        // DatenFilm(String ssender, String tthema, String filmWebsite, String ttitel, String uurl, String uurlRtmp,
                        // String datum, String zeit, long dauerSekunden, String description) {

                        DatenFilm film = new DatenFilm(SENDERNAME, thema, urlSendung, titel, url, "", datum, ""/*Zeit*/, duration, description);
                        addFilm(film);
                        return;
                    }
                }
            }
        }
    }
}
