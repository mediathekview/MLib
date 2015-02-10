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

public class MediathekSr extends MediathekReader implements Runnable {

    public final static String SENDERNAME = "SR";

    /**
     *
     * @param ssearch
     * @param startPrio
     */
    public MediathekSr(MSFilmeSuchen ssearch, int startPrio) {
        super(ssearch, SENDERNAME, /* threads */ 2, /* urlWarten */ 500, startPrio);
    }

    /**
     *
     */
    @Override
    public void addToList() {
        meldungStart();
        // seite1: http://sr-mediathek.sr-online.de/index.php?seite=2&f=v&s=1&o=d
        // seite2: http://sr-mediathek.sr-online.de/index.php?seite=2&f=v&s=2&o=d
        // seite3: http://sr-mediathek.sr-online.de/index.php?seite=2&f=v&s=3&o=d
        int maxSeiten = 15;
        if (MSConfig.senderAllesLaden) {
            maxSeiten = 50;
        }
        for (int i = 1; i < maxSeiten; ++i) {
            String[] add = new String[]{"http://sr-mediathek.sr-online.de/index.php?seite=2&f=v&s=" + i + "&o=d", ""/*thema*/};
            listeThemen.add(add);
        }

        if (MSConfig.getStop()) {
            meldungThreadUndFertig();
        } else if (listeThemen.size() == 0) {
            meldungThreadUndFertig();
        } else {
            meldungAddMax(listeThemen.size());
            for (int t = 0; t < maxThreadLaufen; ++t) {
                //new Thread(new ThemaLaden()).start();
                Thread th = new Thread(new ThemaLaden());
                th.setName(SENDERNAME + t);
                th.start();
            }
        }
    }

    private class ThemaLaden implements Runnable {

        MSGetUrl getUrl = new MSGetUrl(wartenSeiteLaden);
        private MSStringBuilder seite1 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder seite2 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        ArrayList<String> erg = new ArrayList<>();

        @Override
        public void run() {
            try {
                meldungAddThread();
                String link[];
                while (!MSConfig.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    meldungProgress(link[0] /*url*/);
                    bearbeiteTage(link[0]/*url*/);
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(-951236547, MSLog.FEHLER_ART_MREADER, "MediathekSr.ThemaLaden.run", ex, "");
            }
            meldungThreadUndFertig();
        }

        private void bearbeiteTage(String urlSeite) {
            seite1 = getUrlIo.getUri_Utf(SENDERNAME, urlSeite, seite1, "");
            seite1.extractList("<div class=\"list_mi_mi\">", "<a href=\"index.php?seite=", "\"", erg);
            for (String url : erg) {
                addFilme("http://sr-mediathek.sr-online.de/index.php?seite=" + url);
            }
            erg.clear();
        }

        private void addFilme(String urlSeite) {
            meldung(urlSeite);
            seite2 = getUrl.getUri_Utf(SENDERNAME, urlSeite, seite2, "");
            try {
                String url;
                String datum;
                String titel, thema = SENDERNAME;
                long duration = 0;
                String description;

                String d = seite2.extract("| Dauer: ", "|").trim();
                try {
                    if (!d.equals("")) {
                        duration = 0;
                        String[] parts = d.split(":");
                        long power = 1;
                        for (int i = parts.length - 1; i >= 0; i--) {
                            duration += Long.parseLong(parts[i]) * power;
                            power *= 60;
                        }
                    }
                } catch (Exception ex) {
                    MSLog.fehlerMeldung(-732012546, MSLog.FEHLER_ART_MREADER, "MediathekSr.addFilm", "d: " + d);
                }
                description = seite2.extract("<meta name=\"description\" content=\"", ">");
                datum = seite2.extract("Video | ", "|").trim();
                titel = seite2.extract("<title>", "<");
                if (titel.contains(" ::")) {
                    titel = titel.substring(titel.indexOf("::") + 2).trim();
                }
                if (titel.contains("-")) {
                    thema = titel.substring(0, titel.indexOf("-")).trim();
                    titel = titel.substring(titel.indexOf("-") + 1).trim();
                }

                url = seite2.extract("var mediaURLs = ['", "'");
                if (url.isEmpty()) {
                    MSLog.fehlerMeldung(-301245789, MSLog.FEHLER_ART_MREADER, "MediathekSr.addFilme", "keine URL für: " + urlSeite);
                } else {
                    // DatenFilm(String ssender, String tthema, String urlThema, String ttitel, String uurl, String uurlorg, String uurlRtmp, String datum, String zeit) {
                    //DatenFilm film = new DatenFilm(nameSenderMReader, thema, strUrlFeed, titel, url, furl, datum, "");
                    DatenFilm film = new DatenFilm(SENDERNAME, thema, urlSeite, titel, url, "", datum, "" /*Zeit*/, duration, description, new String[]{});
                    addFilm(film);
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(-402583366, MSLog.FEHLER_ART_MREADER, "MediathekSr.addFilme", ex, "");
            }
        }

    }
}
