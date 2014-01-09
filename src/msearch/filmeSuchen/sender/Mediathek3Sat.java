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

import java.text.SimpleDateFormat;
import java.util.Date;
import msearch.filmeSuchen.MSearchFilmeSuchen;
import msearch.io.MSearchGetUrl;
import msearch.daten.MSearchConfig;
import msearch.daten.DatenFilm;
import static msearch.filmeSuchen.sender.MediathekZdf.filmHolenId;
import msearch.tool.MSearchConst;
import msearch.tool.MSearchLog;
import msearch.tool.MSearchStringBuilder;

public class Mediathek3Sat extends MediathekReader implements Runnable {

    public static final String SENDER = "3Sat";
    private final String MUSTER_ALLE = "http://www.3sat.de/mediathek/rss/mediathek.xml";

    public Mediathek3Sat(MSearchFilmeSuchen ssearch, int startPrio) {
        super(ssearch, /* name */ SENDER, /* threads */ 3, /* urlWarten */ 500, startPrio);
    }

    @Override
    void addToList() {
        listeThemen.clear();
        meldungStart();
        sendungenLaden();
        tageLaden();
        if (MSearchConfig.getStop()) {
            meldungThreadUndFertig();
        } else if (listeThemen.size() == 0) {
            meldungThreadUndFertig();
        } else {
            listeSort(listeThemen, 1);
            meldungAddMax(listeThemen.size());
            for (int t = 0; t < maxThreadLaufen; ++t) {
                //new Thread(new ThemaLaden()).start();
                Thread th = new Thread(new ThemaLaden());
                th.setName(nameSenderMReader + t);
                th.start();
            }
        }
    }

    private void tageLaden() {
        // http://www.3sat.de/mediathek/?datum=20140105&cx=108
        String date;
        for (int i = 0; i < (MSearchConfig.senderAllesLaden ? 21 : 7); ++i) {
            date = new SimpleDateFormat("yyyyMMdd").format(new Date().getTime() - i * (1000 * 60 * 60 * 24));
            String url = "http://www.3sat.de/mediathek/?datum=" + date + "&cx=108";
            listeThemen.add(new String[]{url, ""});
        }
    }

    private void sendungenLaden() {
        // ><a class="SubItem" href="?red=kulturzeit">Kulturzeit</a>
        final String ADRESSE = "http://www.3sat.de/mediathek/";
        final String MUSTER_URL = "<a class=\"SubItem\" href=\"?red=";
        MSearchStringBuilder seite = new MSearchStringBuilder(MSearchConst.STRING_BUFFER_START_BUFFER);
        seite = getUrlIo.getUri_Utf(nameSenderMReader, ADRESSE, seite, "");
        int pos1 = 0;
        int pos2;
        String url = "", thema = "";
        while ((pos1 = seite.indexOf(MUSTER_URL, pos1)) != -1) {
            try {
                pos1 += MUSTER_URL.length();
                if ((pos2 = seite.indexOf("\"", pos1)) != -1) {
                    url = seite.substring(pos1, pos2);
                }
                if (url.equals("")) {
                    continue;
                }
                if ((pos1 = seite.indexOf(">", pos1)) != -1) {
                    pos1 += 1;
                    if ((pos2 = seite.indexOf("<", pos1)) != -1) {
                        thema = seite.substring(pos1, pos2);
                    }
                }
                // in die Liste eintragen
                // http://www.3sat.de/mediathek/?red=nano&type=1
                String[] add = new String[]{"http://www.3sat.de/mediathek/?red=" + url + "&type=1", thema};
                listeThemen.addUrl(add);
            } catch (Exception ex) {
                MSearchLog.fehlerMeldung(-915237874, MSearchLog.FEHLER_ART_MREADER, "Mediathek3sat.sendungenLaden", ex);
            }
        }

    }

    private class ThemaLaden implements Runnable {

        MSearchGetUrl getUrl = new MSearchGetUrl(wartenSeiteLaden);
        private MSearchStringBuilder seite1 = new MSearchStringBuilder(MSearchConst.STRING_BUFFER_START_BUFFER);
        private MSearchStringBuilder seite2 = new MSearchStringBuilder(MSearchConst.STRING_BUFFER_START_BUFFER);

        @Override
        public synchronized void run() {
            try {
                meldungAddThread();
                String[] link;
                while (!MSearchConfig.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    meldungProgress(link[0]);
                    laden(link[0] /* url */, link[1] /* Thema */);
                }
            } catch (Exception ex) {
                MSearchLog.fehlerMeldung(-987452384, MSearchLog.FEHLER_ART_MREADER, "Mediathek3Sat.ThemaLaden.run", ex);
            }
            meldungThreadUndFertig();
        }

        void laden(String urlThema, String thema) {

            final String MUSTER_START = "<div class=\"BoxPicture MediathekListPic\">";
            String url;
            for (int i = 0; i < (MSearchConfig.senderAllesLaden ? 40 : 5); ++i) {
                //http://www.3sat.de/mediathek/?type=1&red=nano&mode=verpasst3
                if (thema.isEmpty()) {
                    // dann ist es aus "TAGE"
                    // und wird auch nur einmanl durchlaufen
                    url = urlThema;
                    i = 9999;
                } else {
                    url = urlThema + "&mode=verpasst" + i;
                }
                meldung(url);
                seite1 = getUrlIo.getUri_Utf(nameSenderMReader, url, seite1, "");
                if (seite1.indexOf(MUSTER_START) == -1) {
                    // dann gibts keine weiteren
                    break;
                }
                int pos1 = 0;
                boolean ok;
                String titel, id, urlFilm;
                while ((pos1 = seite1.indexOf(MUSTER_START, pos1)) != -1) {
                    pos1 += MUSTER_START.length();
                    ok = false;
                    // <a class="MediathekLink"  title='Video abspielen: nano vom 8. Januar 2014' href="?mode=play&amp;obj=40860">
                    titel = seite1.extract("<a class=\"MediathekLink\"  title='Video abspielen:", "'", pos1).trim();
                    // ID
                    // http://www.3sat.de/mediathek/?mode=play&obj=40860
                    id = seite1.extract("href=\"?mode=play&amp;obj=", "\"", pos1);
                    if (id.isEmpty()) {
                        //href="?obj=24138"
                        id = seite1.extract("href=\"?obj=", "\"", pos1);
                    }
                    urlFilm = "http://www.3sat.de/mediathek/?mode=play&obj=" + id;
                    if (!id.isEmpty()) {
                        //http://www.3sat.de/mediathek/xmlservice/web/beitragsDetails?ak=web&id=40860
                        id = "http://www.3sat.de/mediathek/xmlservice/web/beitragsDetails?ak=web&id=" + id;
                        //meldung(id);
                        DatenFilm film = filmHolenId(getUrl, seite2, nameSenderMReader, thema, titel, urlFilm, id);
                        if (film != null) {
                            // dann wars gut
                            addFilm(film);
                            ok = true;
                        }
                    }
                    if (!ok) {
                        // dann mit der herkömmlichen Methode versuchen
                        MSearchLog.fehlerMeldung(-462313269, MSearchLog.FEHLER_ART_MREADER, "Mediathek3sat.laden", "Thema: " + url);
                    }
                }
            }
        }
    }
}
