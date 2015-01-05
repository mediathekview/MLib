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

public class MediathekKika extends MediathekReader implements Runnable {

    public final static String SENDERNAME = "KiKA";
    private MSStringBuilder seite = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);

    public MediathekKika(MSFilmeSuchen ssearch, int startPrio) {
        super(ssearch, SENDERNAME, /* threads */ 2, /* urlWarten */ 500, startPrio);
    }

    @Override
    void addToList() {
        meldungStart();
        addToListNormal();
        if (MSConfig.getStop()) {
            meldungThreadUndFertig();
        } else if (listeThemen.size() == 0) {
            meldungThreadUndFertig();
        } else {
            // dann den Sender aus der alten Liste löschen
            // URLs laufen nur begrenzte Zeit
            delSenderInAlterListe(SENDERNAME);
            meldungAddMax(listeThemen.size());
            for (int t = 0; t < maxThreadLaufen; ++t) {
                //new Thread(new ThemaLaden()).start();
                Thread th = new Thread(new ThemaLaden());
                th.setName(SENDERNAME + t);
                th.start();
            }

        }
    }

    void addToListNormal() {
        final String ADRESSE = "http://www.kika.de/sendungen/sendungenabisz100.html";
        final String MUSTER_URL = "<a href=\"/sendungen/sendungenabisz100_";
        ArrayList<String> liste1 = new ArrayList<>();
        ArrayList<String> liste2 = new ArrayList<>();

        listeThemen.clear();
        try {
            seite = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
            seite = getUrlIo.getUri(SENDERNAME, ADRESSE, MSConst.KODIERUNG_UTF, 3, seite, "KiKA: Startseite");
            seite.extractList(MUSTER_URL, "\"", 0, "http://www.kika.de/sendungen/sendungenabisz100_", liste1);

            for (String s : liste1) {
                seite = getUrlIo.getUri_Utf(sendername, s, seite, "KiKa-Sendungen");
                final String MUSTER_SENDUNGEN_1 = "<h4 class=\"headline\">";
                final String MUSTER_SENDUNGEN_2 = "<a href=\"/";
                seite.extractList("", "<!--The bottom navigation -->", MUSTER_SENDUNGEN_1, MUSTER_SENDUNGEN_2, "\"", "http://www.kika.de/", liste2);
            }

            for (String ss : liste2) {
                listeThemen.add(new String[]{ss});
            }
        } catch (Exception ex) {
            MSLog.fehlerMeldung(-302025469, MSLog.FEHLER_ART_MREADER, "MediathekKiKA.addToList", ex, "");
        }
    }

    private class ThemaLaden implements Runnable {

        MSGetUrl getUrl = new MSGetUrl(wartenSeiteLaden);
        private MSStringBuilder seite1 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder seite2 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");//2014-12-12T09:45:00.000+0100
        private final SimpleDateFormat sdfOutTime = new SimpleDateFormat("HH:mm:ss");
        private final SimpleDateFormat sdfOutDay = new SimpleDateFormat("dd.MM.yyyy");

        @Override
        public synchronized void run() {
            try {
                meldungAddThread();
                String[] link;
                while (!MSConfig.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    meldungProgress(link[0]);
                    laden(link[0] /* url */);
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(-915236791, MSLog.FEHLER_ART_MREADER, MediathekKika.class.getName() + ".ThemaLaden.run", ex, "");
            }
            meldungThreadUndFertig();
        }

        void laden(String filmWebsite) {
            final String MUSTER = "<div class=\"av-playerContainer\"";
            seite1 = getUrlIo.getUri(SENDERNAME, filmWebsite, MSConst.KODIERUNG_UTF, 1, seite1, "Themenseite");
            String thema = seite1.extract("<title>", "<");
            thema = thema.replace("KiKA -", "").trim();
            int pos = 0;

            while ((pos = seite1.indexOf(MUSTER, pos)) != -1) {
                pos += MUSTER.length();
                String xml = seite1.extract("setup({dataURL:'", "'", pos);
                if (xml.isEmpty()) {
                    MSLog.fehlerMeldung(-701025987, MSLog.FEHLER_ART_MREADER, "MediathekKika", "keine XML: " + filmWebsite);
                } else {
                    xml = "http://www.kika.de/" + xml;
                    seite2 = getUrlIo.getUri_Utf(sendername, xml, seite2, xml);

                    String titel = seite2.extract("<title>", "<");
                    String beschreibung = seite2.extract("<broadcastDescription>", "<");
                    String date = seite2.extract("<broadcastDate>", "<");
                    String datum = "";
                    String zeit = "";
                    if (!date.isEmpty()) {
                        datum = convertDatum(date);
                        zeit = convertTime(date);
                    } else {
                        date = seite2.extract("<webTime>", "<"); // <webTime>08.12.2014 13:16</webTime>
                        if (!date.isEmpty()) {
                            datum = date.substring(0, date.indexOf(" ")).trim();
                            zeit = date.substring(date.indexOf(" ")).trim() + ":00";
                        }
                    }
                    String urlSendung = seite2.extract("<broadcastURL>", "<");
                    if (urlSendung.isEmpty()) {
                        urlSendung = seite2.extract("<htmlUrl>", "<");
                    }
                    long duration = 0;
                    try {
                        //<duration>00:03:07</duration>
                        String dauer = seite2.extract("<duration>", "<");
                        if (!dauer.equals("")) {
                            String[] parts = dauer.split(":");
                            long power = 1;
                            for (int i = parts.length - 1; i >= 0; i--) {
                                duration += Long.parseLong(parts[i]) * power;
                                power *= 60;
                            }
                        }
                    } catch (NumberFormatException ex) {
                        MSLog.fehlerMeldung(-201036547, MSLog.FEHLER_ART_MREADER, "MediathekKiKa.laden", ex, xml);
                    }
                    // Film-URLs suchen
                    final String MUSTER_URL_MP4 = "<progressiveDownloadUrl>";
                    String urlHD = seite2.extract("| MP4 Web XL |", MUSTER_URL_MP4, "<");
                    String urlMp4 = seite2.extract("| MP4 Web L |", MUSTER_URL_MP4, "<");
                    if (urlMp4.isEmpty()) {
                        urlMp4 = seite2.extract("| MP4 Web L+ |", MUSTER_URL_MP4, "<");
                    }
                    String urlMp4_klein = seite2.extract("| MP4 Web M |", MUSTER_URL_MP4, "<");

                    if (urlMp4.isEmpty()) {
                        urlMp4 = urlMp4_klein;
                        urlMp4_klein = "";
                    }

                    if (thema.isEmpty() || urlSendung.isEmpty() || titel.isEmpty() || urlMp4.isEmpty() || date.isEmpty() || zeit.isEmpty() || duration == 0 || beschreibung.isEmpty()) {
                        MSLog.fehlerMeldung(-735216987, MSLog.FEHLER_ART_MREADER, "MediathekKika", "leer: " + xml);
                    }

                    if (!urlMp4.equals("")) {
                        meldung(urlMp4);
                        DatenFilm film = new DatenFilm(SENDERNAME, thema, urlSendung, titel, urlMp4, ""/*rtmpUrl*/, datum, zeit, duration, beschreibung, new String[]{});
                        film.addUrlKlein(urlMp4_klein, "");
                        film.addUrlHd(urlHD, "");
                        addFilm(film);
                    } else {
                        MSLog.fehlerMeldung(-912036789, MSLog.FEHLER_ART_MREADER, "MediathekKika", "keine URL: " + filmWebsite + " xml: " + xml);
                    }
                }
            }
        }

        private String convertDatum(String datum) {
            //<broadcastDate>2014-12-12T09:45:00.000+0100</broadcastDate>
            // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            try {
                Date filmDate = sdf.parse(datum);
                datum = sdfOutDay.format(filmDate);
            } catch (ParseException ex) {
                MSLog.fehlerMeldung(-731025789, MSLog.FEHLER_ART_PROG, "MediathekKika.convertDatum: " + datum, ex);
            }
            return datum;
        }

        private String convertTime(String zeit) {
            //<broadcastDate>2014-12-12T09:45:00.000+0100</broadcastDate>
            // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            try {
                Date filmDate = sdf.parse(zeit);
                zeit = sdfOutTime.format(filmDate);
            } catch (ParseException ex) {
                MSLog.fehlerMeldung(-915423687, MSLog.FEHLER_ART_PROG, "MediathekKika.convertTime: " + zeit, ex);
            }
            return zeit;
        }
    }

}
