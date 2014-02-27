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
        super(ssearch, /* name */ SENDER, 1 /* threads */, 500 /* urlWarten */, startPrio);
    }

    @Override
    public void addToList() {
        listeThemen.clear();
        meldungStart();
        addTivi();
        meldungThreadUndFertig();
    }

    private void addTivi() {
        //<ns3:headline>Nachrichten</ns3:headline>
        //<ns3:image>/tiviVideos/contentblob/2063212/tivi9teaserbild/9050138</ns3:image>
        //<ns3:page>/tiviVideos/beitrag/pur%2B+Sendungen/895212/2063212?view=flashXml</ns3:page>
        //<ns3:text>Ich will die Wahrheit!</ns3:text>
        final String MUSTER_URL = "<ns3:page>/tiviVideos/beitrag";
        MSGetUrl getUrl = new MSGetUrl(wartenSeiteLaden);
        MSStringBuilder seiteTivi_1 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        MSStringBuilder seiteTivi_2 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        seiteTivi_1 = getUrl.getUri(nameSenderMReader, "http://www.tivi.de/tiviVideos/?view=flashXml", MSConst.KODIERUNG_UTF, 6 /* versuche */, seiteTivi_1, "" /* Meldung */);
        ///seiteTivi_1 = getUrl.getUri(nameSenderMReader, "http://www.tivi.de/tiviVideos/?view=xml", MSearchConst.KODIERUNG_UTF, 6 /* versuche */, seiteTivi_1, "" /* Meldung */);
        if (seiteTivi_1.length() == 0) {
            MSLog.fehlerMeldung(-302132654, MSLog.FEHLER_ART_MREADER, "MediathekZdf.addTivi", "Leere Seite Tivi-1");
        }
        int pos = 0;
        int pos1;
        int pos2;
        int pos3 = 0;
        String url = "";
        long dauerL = 0;
        String titel, thema, urlFilm = "", datum, zeit = "", bild, website, dauer, text;
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
                    MSLog.fehlerMeldung(-754126900, MSLog.FEHLER_ART_MREADER, "MediathekZdfaddTivi", "keine URL");
                } else {
                    urlFilm = "";
                    url = "http://www.tivi.de/tiviVideos/beitrag" + url;
                    // Film laden
                    meldung(url);
                    seiteTivi_2 = getUrl.getUri_Utf(nameSenderMReader, url, seiteTivi_2, "" /* Meldung */);
                    if (seiteTivi_2.length() == 0) {
                        MSLog.fehlerMeldung(-798956231, MSLog.FEHLER_ART_MREADER, "MediathekZdf.addTivi", "Leere Seite Tivi-2: " + url);
                        continue;
                    }
                    thema = seiteTivi_2.extract("<title>", "<");
                    titel = seiteTivi_2.extract("<subtitle>", "<");
                    text = seiteTivi_2.extract("<text>", "<");
                    bild = seiteTivi_2.extract("<image>", "<");
                    if (!bild.isEmpty()) {
                        bild = "http://www.tivi.de" + bild;
                    }
                    website = seiteTivi_2.extract("<link>", "<");
                    dauer = seiteTivi_2.extract("<ns3:duration>", "<"); //<ns3:duration>P0Y0M0DT0H24M9.000S</ns3:duration>
                    if (dauer.isEmpty()) {
                        //<duration>P0Y0M0DT0H1M55.000S</duration>
                        dauer = seiteTivi_2.extract("<duration>", "<"); //<duration>P0Y0M0DT0H11M0.000S</duration>
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
                        MSLog.fehlerMeldung(-349761012, MSLog.FEHLER_ART_PROG, "MediathekZdfaddTivi, Dauer: " + url, ex);
                    }
                    zeit = "";
                    datum = seiteTivi_2.extract("<airTime>", "<");
                    //<airTime>2014-01-19T08:35:00.000+01:00</airTime>
                    try {
                        Date filmDate = sdfIn.parse(datum);
                        datum = sdfOut_date.format(filmDate);
                        zeit = sdfOut_time.format(filmDate);
                    } catch (Exception ex) {
                        MSLog.fehlerMeldung(-649600299, MSLog.FEHLER_ART_PROG, "MediathekZdfaddTivi, Datum: " + url, ex);
                    }
                    pos3 = 0;
                    while ((pos3 = seiteTivi_2.indexOf("<ns4:quality>veryhigh</ns4:quality>", pos3)) != -1) {
                        pos3 += 5;
                        urlFilm = "";
                        urlFilm = seiteTivi_2.extract("<ns4:url>", "<", pos3);
                        if (urlFilm.startsWith("http") && urlFilm.endsWith("mp4") && !urlFilm.contains("metafilegenerator")) {
                            break;
                        }
                    }
                    if (urlFilm.isEmpty()) {
                        MSLog.fehlerMeldung(-159876234, MSLog.FEHLER_ART_MREADER, "MediathekZdfaddTivi", "kein Film: " + url);
                    } else {
                        // public DatenFilm(String ssender, String tthema, String filmWebsite, String ttitel, String uurl, String uurlRtmp,
                        //        String datum, String zeit,
                        //        long dauerSekunden, String description, String imageUrl, String[] keywords) {
                        DatenFilm film = new DatenFilm(nameSenderMReader, thema, website, titel, urlFilm, "" /*urlRtmp*/,
                                datum, zeit,
                                dauerL, text, bild, new String[]{""});
                        addFilm(film);
                    }
                }
            }
        } catch (Exception ex) {
            MSLog.fehlerMeldung(-454123698, MSLog.FEHLER_ART_MREADER, "MediathekZdf.addTivi", ex);
        }
    }

}
