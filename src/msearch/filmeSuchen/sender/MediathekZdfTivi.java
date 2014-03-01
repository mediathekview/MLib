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

import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import msearch.daten.DatenFilm;
import msearch.daten.MSConfig;
import msearch.filmeSuchen.MSFilmeSuchen;
import msearch.io.MSGetUrl;
import msearch.tool.MSConst;
import msearch.tool.MSLog;
import msearch.tool.MSStringBuilder;

public class MediathekZdfTivi extends MediathekReader implements Runnable {

    public static final String SENDER = "ZDF-tivi";
    private final SimpleDateFormat sdfIn = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private final SimpleDateFormat sdfOut_date = new SimpleDateFormat("dd.MM.yyyy");
    private final SimpleDateFormat sdfOut_time = new SimpleDateFormat("HH:mm:ss");

    public MediathekZdfTivi(MSFilmeSuchen ssearch, int startPrio) {
        super(ssearch, /* name */ SENDER, 2 /* threads */, 500 /* urlWarten */, startPrio);
    }

    @Override
    public synchronized void addToList() {
        //Theman suchen
        listeThemen.clear();
        meldungStart();
        add_1();
        add_2();
        if (MSConfig.getStop()) {
            meldungThreadUndFertig();
        } else if (listeThemen.size() == 0) {
            meldungThreadUndFertig();
        } else {
            meldungAddMax(listeThemen.size());
            for (int t = 0; t < maxThreadLaufen; ++t) {
                Thread th = new Thread(new ThemaLaden());
                th.setName(nameSenderMReader + t);
                th.start();
            }
        }
    }

    private void add_1() {
        //<ns3:headline>Nachrichten</ns3:headline>
        //<ns3:image>/tiviVideos/contentblob/2063212/tivi9teaserbild/9050138</ns3:image>
        //<ns3:page>/tiviVideos/beitrag/pur%2B+Sendungen/895212/2063212?view=flashXml</ns3:page>
        //<ns3:text>Ich will die Wahrheit!</ns3:text>
        final String MUSTER_URL = "<ns3:page>/tiviVideos";
        MSStringBuilder seiteTivi_1 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        seiteTivi_1 = getUrlIo.getUri(nameSenderMReader, "http://www.tivi.de/tiviVideos/rueckblick?view=flashXml", MSConst.KODIERUNG_UTF, 6 /* versuche */, seiteTivi_1, "" /* Meldung */);
        if (seiteTivi_1.length() == 0) {
            MSLog.fehlerMeldung(-732323698, MSLog.FEHLER_ART_MREADER, "MediathekZdfTivi.add_1", "Leere Seite");
        }
        int pos = 0;
        int pos1;
        int pos2;
        String url;
        try {
            while ((pos = seiteTivi_1.indexOf(MUSTER_URL, pos)) != -1) {
                url = "";
                pos += MUSTER_URL.length();
                pos1 = pos;
                if ((pos2 = seiteTivi_1.indexOf("<", pos1)) != -1) {
                    url = seiteTivi_1.substring(pos1, pos2);
                    url = URLDecoder.decode(url, "UTF-8");
                }
                if (url.equals("")) {
                    MSLog.fehlerMeldung(-309075109, MSLog.FEHLER_ART_MREADER, "MediathekZdfTivi.add_1", "keine URL");
                } else {
                    url = "http://www.tivi.de/tiviVideos" + url;
                    listeThemen.addUrl(new String[]{url});
                }
            }
        } catch (Exception ex) {
            MSLog.fehlerMeldung(-302010498, MSLog.FEHLER_ART_PROG, "MediathekZdfTivi.add_1", ex);
        }
    }

    private void add_2() {
        //<ns3:headline>Nachrichten</ns3:headline>
        //<ns3:image>/tiviVideos/contentblob/2063212/tivi9teaserbild/9050138</ns3:image>
        //<ns3:page>/tiviVideos/beitrag/pur%2B+Sendungen/895212/2063212?view=flashXml</ns3:page>
        //<ns3:text>Ich will die Wahrheit!</ns3:text>
        final String MUSTER_URL = "<ns3:page>/tiviVideos/beitrag";
        MSStringBuilder seiteTivi_1 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        seiteTivi_1 = getUrlIo.getUri(nameSenderMReader, "http://www.tivi.de/tiviVideos/?view=flashXml", MSConst.KODIERUNG_UTF, 6 /* versuche */, seiteTivi_1, "" /* Meldung */);
        ///seiteTivi_1 = getUrl.getUri(nameSenderMReader, "http://www.tivi.de/tiviVideos/?view=xml", MSearchConst.KODIERUNG_UTF, 6 /* versuche */, seiteTivi_1, "" /* Meldung */);
        if (seiteTivi_1.length() == 0) {
            MSLog.fehlerMeldung(-645121326, MSLog.FEHLER_ART_MREADER, "MediathekZdfTivi.add_2", "Leere Seite");
        }
        int pos = 0;
        int pos1;
        int pos2;
        String url;
        try {
            while ((pos = seiteTivi_1.indexOf(MUSTER_URL, pos)) != -1) {
                url = "";
                pos += MUSTER_URL.length();
                pos1 = pos;
                if ((pos2 = seiteTivi_1.indexOf("<", pos1)) != -1) {
                    url = seiteTivi_1.substring(pos1, pos2);
                    url = URLDecoder.decode(url, "UTF-8");
                }
                if (url.equals("")) {
                    MSLog.fehlerMeldung(-915263985, MSLog.FEHLER_ART_MREADER, "MediathekZdfTivi.add_2", "keine URL");
                } else {
                    url = "http://www.tivi.de/tiviVideos/beitrag" + url;
                    listeThemen.addUrl(new String[]{url});
                }
            }
        } catch (Exception ex) {
            MSLog.fehlerMeldung(-730169702, MSLog.FEHLER_ART_PROG, "MediathekZdfTivi.add_2", ex);
        }
    }

    private class ThemaLaden implements Runnable {

        MSGetUrl getUrl = new MSGetUrl(wartenSeiteLaden);
        MSStringBuilder seite1 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);

        @Override
        public void run() {
            try {
                meldungAddThread();
                String[] link;
                while (!MSConfig.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    addTivi_(link[0] /* url */);
                    meldungProgress(link[0]);
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(-731214569, MSLog.FEHLER_ART_MREADER, "MediathekZdfTivi.ThemaLaden.run", ex);
            }
            meldungThreadUndFertig();
        }

        private void addTivi_(String url) {
            int pos3 = 0;
            long dauerL = 0;
            String titel, thema, urlFilm = "", datum, zeit = "", bild, website, dauer, text;
            try {
                urlFilm = "";
                // Film laden
                meldung(url);
                seite1 = getUrl.getUri_Utf(nameSenderMReader, url, seite1, "" /* Meldung */);
                if (seite1.length() == 0) {
                    MSLog.fehlerMeldung(-301649897, MSLog.FEHLER_ART_MREADER, "MediathekZdfTivi.addTivi_2", "Leere Seite Tivi-2: " + url);
                    return;
                }
                thema = seite1.extract("<title>", "<");
                titel = seite1.extract("<subtitle>", "<");
                text = seite1.extract("<text>", "<");
                bild = seite1.extract("<image>", "<");
                if (!bild.isEmpty()) {
                    bild = "http://www.tivi.de" + bild;
                }
                website = seite1.extract("<link>", "<");
                dauer = seite1.extract("<ns3:duration>", "<"); //<ns3:duration>P0Y0M0DT0H24M9.000S</ns3:duration>
                if (dauer.isEmpty()) {
                    //<duration>P0Y0M0DT0H1M55.000S</duration>
                    dauer = seite1.extract("<duration>", "<"); //<duration>P0Y0M0DT0H11M0.000S</duration>
                }
                try {
                    dauer = dauer.replace("P0Y0M0DT", "");
                    String h = dauer.substring(0, dauer.indexOf("H"));
                    int ih = Integer.parseInt(h);
                    String m = dauer.substring(dauer.indexOf("H") + 1, dauer.indexOf("M"));
                    int im = Integer.parseInt(m);
                    String s = dauer.substring(dauer.indexOf("M") + 1, dauer.indexOf("."));
                    int is = Integer.parseInt(s);
                    dauerL = ih * 60 * 60 + im * 60 + is;
                } catch (Exception ex) {
                    dauerL = 0;
                    MSLog.fehlerMeldung(-349761012, MSLog.FEHLER_ART_PROG, "MediathekZdfTivi.addTivi_2, Dauer: " + url, ex);
                }
                zeit = "";
                datum = seite1.extract("<airTime>", "<");
                //<airTime>2014-01-19T08:35:00.000+01:00</airTime>
                try {
                    Date filmDate = sdfIn.parse(datum);
                    datum = sdfOut_date.format(filmDate);
                    zeit = sdfOut_time.format(filmDate);
                } catch (Exception ex) {
                    MSLog.fehlerMeldung(-649600299, MSLog.FEHLER_ART_PROG, "MediathekZdfTivi.addTivi_2, Datum: " + url, ex);
                }
                pos3 = 0;
                while ((pos3 = seite1.indexOf("<ns4:quality>veryhigh</ns4:quality>", pos3)) != -1) {
                    pos3 += 5;
                    urlFilm = "";
                    urlFilm = seite1.extract("<ns4:url>", "<", pos3);
                    if (urlFilm.startsWith("http") && urlFilm.endsWith("mp4") && !urlFilm.contains("metafilegenerator")) {
                        break;
                    }
                }
                if (urlFilm.isEmpty()) {
                    MSLog.fehlerMeldung(-159876234, MSLog.FEHLER_ART_MREADER, "MediathekZdfTivi.addTivi_2", "kein Film: " + url);
                } else {
                    // public DatenFilm(String ssender, String tthema, String filmWebsite, String ttitel, String uurl, String uurlRtmp,
                    //        String datum, String zeit,
                    //        long dauerSekunden, String description, String imageUrl, String[] keywords) {
                    meldung(titel);
                    DatenFilm film = new DatenFilm(nameSenderMReader, thema, website, titel, urlFilm, "" /*urlRtmp*/,
                            datum, zeit,
                            dauerL, text, bild, new String[]{""});
                    addFilm(film);
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(-454123698, MSLog.FEHLER_ART_MREADER, "MediathekZdf.addTivi", ex);
            }
        }

    }
}
