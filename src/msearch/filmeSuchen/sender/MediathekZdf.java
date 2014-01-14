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
import java.util.Locale;
import msearch.daten.DatenFilm;
import msearch.daten.MSearchConfig;
import msearch.filmeSuchen.MSearchFilmeSuchen;
import msearch.io.MSearchGetUrl;
import msearch.tool.MSearchConst;
import msearch.tool.MSearchLog;
import msearch.tool.MSearchStringBuilder;
import org.apache.commons.lang3.StringEscapeUtils;

public class MediathekZdf extends MediathekReader implements Runnable {

    public static final String SENDER = "ZDF";
    private MSearchStringBuilder seite = new MSearchStringBuilder(MSearchConst.STRING_BUFFER_START_BUFFER);
    private final int ANZAHL_ZDF_ALLE = 500;
    private final int ANZAHL_ZDF_MITTEL = 50;
    private final int ANZAHL_ZDF_UPDATE = 20;
    private final int ANZAHL_ZDF_KURZ = 10;
    private final SimpleDateFormat sdfIn = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private final SimpleDateFormat sdfOut_date = new SimpleDateFormat("dd.MM.yyyy");
    private final SimpleDateFormat sdfOut_time = new SimpleDateFormat("HH:mm:ss");

    public MediathekZdf(MSearchFilmeSuchen ssearch, int startPrio) {
        super(ssearch, /* name */ SENDER, 8 /* threads */, 500 /* urlWarten */, startPrio);
    }

    @Override
    public void addToList() {
        listeThemen.clear();
        meldungStart();
        addTivi();
        // Liste von http://www.zdf.de/ZDFmediathek/hauptnavigation/sendung-a-bis-z/saz0 bis sat8 holen
        String addr = "http://www.zdf.de/ZDFmediathek/hauptnavigation/sendung-a-bis-z/saz";
        for (int i = 0; i <= 8; ++i) {
            addToList_addr(addr + String.valueOf(i), MSearchConfig.senderAllesLaden ? ANZAHL_ZDF_ALLE : ANZAHL_ZDF_UPDATE);
        }
        // Spartenkanäle einfügen
        addToList_addr("http://www.zdf.de/ZDFmediathek/senderstartseite/sst1/1209122", MSearchConfig.senderAllesLaden ? ANZAHL_ZDF_ALLE : ANZAHL_ZDF_UPDATE); // zdf-neo
        addToList_addr("http://www.zdf.de/ZDFmediathek/senderstartseite/sst1/1209120", MSearchConfig.senderAllesLaden ? ANZAHL_ZDF_ALLE : ANZAHL_ZDF_UPDATE); // zdf-info
        addToList_addr("http://www.zdf.de/ZDFmediathek/senderstartseite/sst1/1317640", MSearchConfig.senderAllesLaden ? ANZAHL_ZDF_ALLE : ANZAHL_ZDF_UPDATE); // zdf-kultur
        //Rubriken einfügen
        if (MSearchConfig.senderAllesLaden) {
            // da sollte eigentlich nichts Neues sein
            addToList_Rubrik("http://www.zdf.de/ZDFmediathek/hauptnavigation/rubriken");
        }
        // letzte Woche einfügen
        addThemenliste("http://www.zdf.de/ZDFmediathek/hauptnavigation/sendung-verpasst/day0", "http://www.zdf.de/ZDFmediathek/hauptnavigation/sendung-verpasst/day0", "");
        addThemenliste("http://www.zdf.de/ZDFmediathek/hauptnavigation/sendung-verpasst/day1", "http://www.zdf.de/ZDFmediathek/hauptnavigation/sendung-verpasst/day1", "");
        addThemenliste("http://www.zdf.de/ZDFmediathek/hauptnavigation/sendung-verpasst/day2", "http://www.zdf.de/ZDFmediathek/hauptnavigation/sendung-verpasst/day2", "");
        addThemenliste("http://www.zdf.de/ZDFmediathek/hauptnavigation/sendung-verpasst/day3", "http://www.zdf.de/ZDFmediathek/hauptnavigation/sendung-verpasst/day3", "");
        addThemenliste("http://www.zdf.de/ZDFmediathek/hauptnavigation/sendung-verpasst/day4", "http://www.zdf.de/ZDFmediathek/hauptnavigation/sendung-verpasst/day4", "");
        addThemenliste("http://www.zdf.de/ZDFmediathek/hauptnavigation/sendung-verpasst/day5", "http://www.zdf.de/ZDFmediathek/hauptnavigation/sendung-verpasst/day5", "");
        addThemenliste("http://www.zdf.de/ZDFmediathek/hauptnavigation/sendung-verpasst/day6", "http://www.zdf.de/ZDFmediathek/hauptnavigation/sendung-verpasst/day6", "");
        addThemenliste("http://www.zdf.de/ZDFmediathek/hauptnavigation/sendung-verpasst/day7", "http://www.zdf.de/ZDFmediathek/hauptnavigation/sendung-verpasst/day7", "");
        // Spartenkanäle Übersicht
        if (MSearchConfig.senderAllesLaden) {
            addThemenliste("http://www.zdf.de/ZDFmediathek/senderstartseite/1209114", "http://www.zdf.de/ZDFmediathek/senderstartseite/1209114", ""); // ZDF
            addThemenliste("http://www.zdf.de/ZDFmediathek/senderstartseite/sst0/1209122?teaserListIndex=" + ANZAHL_ZDF_MITTEL,
                    "http://www.zdf.de/ZDFmediathek/senderstartseite/sst0/1209122?teaserListIndex=" + ANZAHL_ZDF_MITTEL, ""); // ZDF Neo
            addThemenliste("http://www.zdf.de/ZDFmediathek/senderstartseite/sst0/1317640?teaserListIndex=" + ANZAHL_ZDF_MITTEL,
                    "http://www.zdf.de/ZDFmediathek/senderstartseite/sst0/1317640?teaserListIndex=" + ANZAHL_ZDF_MITTEL, ""); // ZDF.kultur
            addThemenliste("http://www.zdf.de/ZDFmediathek/senderstartseite/sst0/1209120?teaserListIndex=" + ANZAHL_ZDF_MITTEL,
                    "http://www.zdf.de/ZDFmediathek/senderstartseite/sst0/1209120?teaserListIndex=" + ANZAHL_ZDF_MITTEL, ""); // ZDFinfo
        } else {
            addThemenliste("http://www.zdf.de/ZDFmediathek/senderstartseite/1209114", "http://www.zdf.de/ZDFmediathek/senderstartseite/1209114", ""); // ZDF
            addThemenliste("http://www.zdf.de/ZDFmediathek/senderstartseite/1209122", "http://www.zdf.de/ZDFmediathek/senderstartseite/1209122", ""); // ZDF Neo
            addThemenliste("http://www.zdf.de/ZDFmediathek/senderstartseite/1317640", "http://www.zdf.de/ZDFmediathek/senderstartseite/1317640", ""); // ZDF.kultur
            addThemenliste("http://www.zdf.de/ZDFmediathek/senderstartseite/1209120", "http://www.zdf.de/ZDFmediathek/senderstartseite/1209120", ""); // ZDFinfo
        }
        if (MSearchConfig.getStop()) {
            meldungThreadUndFertig();
        } else if (listeThemen.size() == 0) {
            meldungThreadUndFertig();
        } else {
            meldungAddMax(listeThemen.size());
            //alles auswerten
            for (int t = 0; t < maxThreadLaufen; ++t) {
                //new Thread(new ThemaLaden()).start();
                Thread th = new Thread(new ThemaLaden());
                th.setName(nameSenderMReader + t);
                th.start();
            }
        }
    }

    private void addToList_Rubrik(String addr) {
        final String MUSTER_URL = "<p><b><a href=\"/ZDFmediathek/kanaluebersicht/aktuellste/";
        //GetUrl(int ttimeout, long wwartenBasis) {
        MSearchGetUrl getUrl = new MSearchGetUrl(wartenSeiteLaden);
        MSearchStringBuilder seiteR = new MSearchStringBuilder(MSearchConst.STRING_BUFFER_START_BUFFER);
        seiteR = getUrl.getUri(nameSenderMReader, addr, MSearchConst.KODIERUNG_UTF, 6 /* versuche */, seiteR, "" /* Meldung */);
        if (seiteR.length() == 0) {
            MSearchLog.fehlerMeldung(-774200364, MSearchLog.FEHLER_ART_MREADER, "MediathekZdf.addToList_addr", "Leere Seite für URL: " + addr);
        }
        int pos = 0;
        int pos1;
        int pos2;
        int pos3;
        String url = "";
        while ((pos = seiteR.indexOf(MUSTER_URL, pos)) != -1) {
            pos += MUSTER_URL.length();
            pos1 = pos;
            pos2 = seiteR.indexOf("?", pos);
            pos3 = seiteR.indexOf("\"", pos);
            if (pos1 != -1 && pos2 != -1 && pos3 != -1 && pos2 < pos3) {
                //pos2 > pos3 dann hat der Link kein ?
                url = seiteR.substring(pos1, pos2);
            }
            if (url.equals("")) {
                MSearchLog.fehlerMeldung(-754126900, MSearchLog.FEHLER_ART_MREADER, "MediathekZdf.addToList_addr", "keine URL: " + addr);
            } else {
                url = "http://www.zdf.de/ZDFmediathek/kanaluebersicht/aktuellste/" + url + "?bc=rub";
                addToList_addr(url, ANZAHL_ZDF_UPDATE); // immer nur eine "kurz"
            }
        }
    }

    private void addTivi() {
        //<ns3:headline>Nachrichten</ns3:headline>
        //<ns3:image>/tiviVideos/contentblob/2063212/tivi9teaserbild/9050138</ns3:image>
        //<ns3:page>/tiviVideos/beitrag/pur%2B+Sendungen/895212/2063212?view=flashXml</ns3:page>
        //<ns3:text>Ich will die Wahrheit!</ns3:text>
        final String MUSTER_URL = "<ns3:page>/tiviVideos/beitrag";
        MSearchGetUrl getUrl = new MSearchGetUrl(wartenSeiteLaden);
        MSearchStringBuilder seiteTivi_1 = new MSearchStringBuilder(MSearchConst.STRING_BUFFER_START_BUFFER);
        MSearchStringBuilder seiteTivi_2 = new MSearchStringBuilder(MSearchConst.STRING_BUFFER_START_BUFFER);
        seiteTivi_1 = getUrl.getUri(nameSenderMReader, "http://www.tivi.de/tiviVideos/?view=flashXml", MSearchConst.KODIERUNG_UTF, 6 /* versuche */, seiteTivi_1, "" /* Meldung */);
        if (seiteTivi_1.length() == 0) {
            MSearchLog.fehlerMeldung(-302132654, MSearchLog.FEHLER_ART_MREADER, "MediathekZdf.addTivi", "Leere Seite Tivi");
        }
        int pos = 0;
        int pos1;
        int pos2;
        int pos3 = 0;
        String url = "";
        long dauerL = 0;
        String titel, thema, urlFilm = "", datum, zeit = "", bild, website, dauer;
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
                    MSearchLog.fehlerMeldung(-754126900, MSearchLog.FEHLER_ART_MREADER, "MediathekZdfaddTivi", "keine URL");
                } else {
                    urlFilm = "";
                    url = "http://www.tivi.de/tiviVideos/beitrag" + url;
                    // Film laden
                    meldung(url);
                    seiteTivi_2 = getUrl.getUri_Utf(nameSenderMReader, url, seiteTivi_2, "" /* Meldung */);
                    titel = seiteTivi_2.extract("<title>", "<");
                    thema = seiteTivi_2.extract("<subtitle>", "<");
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
                        String s = dauer.substring(dauer.indexOf("M")+1, dauer.indexOf("."));
                        int is = Integer.parseInt(s);
                        dauerL = ih * 60 * 60 + im * 60 + is;
                    } catch (Exception ex) {
                        dauerL = 0;
                        MSearchLog.fehlerMeldung(-349761012, MSearchLog.FEHLER_ART_PROG, "DatumDatum.convertDatum", ex);
                    }
                    zeit = "";
                    datum = seiteTivi_2.extract("<airTime>", "<");
                    //<airTime>2014-01-19T08:35:00.000+01:00</airTime>
                    try {
                        Date filmDate = sdfIn.parse(datum);
                        datum = sdfOut_date.format(filmDate);
                        zeit = sdfOut_time.format(filmDate);
                    } catch (Exception ex) {
                        MSearchLog.fehlerMeldung(-649600299, MSearchLog.FEHLER_ART_PROG, "DatumDatum.convertDatum", ex);
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
                        MSearchLog.fehlerMeldung(-159876234, MSearchLog.FEHLER_ART_MREADER, "MediathekZdfaddTivi", "kein Film: " + url);
                    } else {
                        DatenFilm film = new DatenFilm(nameSenderMReader, thema, website, titel, urlFilm, "" /*urlRtmp*/, datum, zeit,
                                dauerL, "", bild, new String[]{""});
                        addFilm(film);
                    }
                }
            }
        } catch (Exception ex) {
            MSearchLog.fehlerMeldung(-454123698, MSearchLog.FEHLER_ART_MREADER, "MediathekZdf.addTivi", ex);
        }
    }

    private void addToList_addr(String addr, int anz) {
        final String MUSTER_URL = "<p><b><a href=\"/ZDFmediathek/kanaluebersicht/aktuellste/";
        //GetUrl(int ttimeout, long wwartenBasis) {
        MSearchGetUrl getUrl = new MSearchGetUrl(wartenSeiteLaden);
        seite = getUrl.getUri(nameSenderMReader, addr, MSearchConst.KODIERUNG_UTF, 6 /* versuche */, seite, "" /* Meldung */);
        if (seite.length() == 0) {
            MSearchLog.fehlerMeldung(-596004563, MSearchLog.FEHLER_ART_MREADER, "MediathekZdf.addToList_addr", "Leere Seite für URL: " + addr);
        }
        int pos = 0;
        int pos1;
        int pos2;
        int pos3;
        String url = "";
        String urlThema;
        String thema = "";
        while ((pos = seite.indexOf(MUSTER_URL, pos)) != -1) {
            pos += MUSTER_URL.length();
            pos1 = pos;
            pos2 = seite.indexOf("?", pos);
            pos3 = seite.indexOf("\"", pos);
            if (pos1 != -1 && pos2 != -1 && pos3 != -1 && pos2 < pos3) {
                //pos2 > pos3 dann hat der Link kein ?
                url = seite.substring(pos1, pos2);
            } else {
                pos2 = seite.indexOf("\"", pos);
                pos3 = seite.indexOf("<", pos);
                if (pos1 != -1 && pos2 != -1 && pos3 != -1 && pos2 < pos3) {
                    //pos2 > pos3 dann hat der Link kein ? zB bei "Rubiken"
                    url = seite.substring(pos1, pos2);
                }
            }
            pos1 = seite.indexOf("\">", pos);
            pos2 = seite.indexOf("<", pos);
            if (pos1 != -1 && pos2 != -1) {
                thema = seite.substring(pos1 + 2, pos2);
            }
            if (url.equals("")) {
                MSearchLog.fehlerMeldung(-946325890, MSearchLog.FEHLER_ART_MREADER, "MediathekZdf.addToList_addr", "keine URL: " + addr);
            } else {
                url = "http://www.zdf.de/ZDFmediathek/kanaluebersicht/aktuellste/" + url;
                urlThema = url;
                url += "?teaserListIndex=" + String.valueOf(anz);
                addThemenliste(url, urlThema, thema);
            }
        }
    }

    private synchronized void addThemenliste(String url, String urlThema, String thema) {
        String[] add = new String[]{url, urlThema, thema};
        listeThemen.addUrl(add);
    }

    private class ThemaLaden implements Runnable {

        MSearchGetUrl getUrl = new MSearchGetUrl(wartenSeiteLaden);
        private MSearchStringBuilder seite1 = new MSearchStringBuilder(MSearchConst.STRING_BUFFER_START_BUFFER);
        private MSearchStringBuilder seite2 = new MSearchStringBuilder(MSearchConst.STRING_BUFFER_START_BUFFER);

        @Override
        public void run() {
            try {
                String link[];
                meldungAddThread();
                while (!MSearchConfig.getStop() && (link = getListeThemen()) != null) {
                    seite1.setLength(0);
                    addFilme(link[0]/* url */, link[1]/* urlThema */, link[2]/* Thema */);
                    meldungProgress(link[0]);
                }
            } catch (Exception ex) {
                MSearchLog.fehlerMeldung(-496583200, MSearchLog.FEHLER_ART_MREADER, "MediathekZdf.ZdfThemaLaden.run", ex);
            }
            meldungThreadUndFertig();
        }

        private void addFilme(String url, String urlThema, String thema) {
            final String MUSTER_URL_1 = "<p><b><a href=\"/ZDFmediathek/beitrag/video/";
            String titel = "";
            String urlFilm = "";
            boolean ok = false;
            int pos = 0;
            int pos1 = 0;
            int pos2 = 0;
            int pos3 = 0;
            int anz = 0;
            try {
                //seite1 = getUrl.getUri(urlThema + "?bc=saz", seite1);
                seite1 = getUrl.getUri_Utf(nameSenderMReader, url, seite1, "Thema: " + thema);
                while (!MSearchConfig.getStop() && (pos = seite1.indexOf(MUSTER_URL_1, pos)) != -1) {
                    ok = false;
                    ++anz;
                    if (!MSearchConfig.senderAllesLaden) {
                        if (anz > ANZAHL_ZDF_KURZ) {
                            // dann reichts
                            break;
                        }
                    }
                    pos += MUSTER_URL_1.length();
                    pos1 = pos;
                    pos2 = seite1.indexOf("?", pos);
                    pos3 = seite1.indexOf("\"", pos);
                    if (pos1 != -1 && pos2 != -1 && pos3 != -1 && pos2 < pos3) {
                        //pos2 > pos3 dann hat der Link kein ?
                        urlFilm = seite1.substring(pos1, pos2);
                    } else {
                        urlFilm = seite1.substring(pos1, pos3);
                    }
                    pos1 = seite1.indexOf("\">", pos);
                    pos2 = seite1.indexOf("<", pos);
                    if (pos1 != -1 && pos2 != -1) {
                        titel = seite1.substring(pos1 + 2, pos2);
                    }
                    if (urlFilm.isEmpty()) {
                        MSearchLog.fehlerMeldung(-643269690, MSearchLog.FEHLER_ART_MREADER, "MediathekZdf.addFilme", "keine URL: " + url);
                    } else {
                        // über die ID versuchen
                        urlFilm = "http://www.zdf.de/ZDFmediathek/beitrag/video/" + urlFilm;
                        String id = "";
                        if ((pos1 = urlFilm.indexOf("/ZDFmediathek/beitrag/video/")) != -1) {
                            pos1 += "/ZDFmediathek/beitrag/video/".length();
                            if ((pos2 = urlFilm.indexOf("/", pos1)) != -1) {
                                id = urlFilm.substring(pos1, pos2);
                                // System.out.println(id);
                            }
                        }
                        if (!id.isEmpty()) {
                            id = "http://www.zdf.de/ZDFmediathek/xmlservice/web/beitragsDetails?ak=web&id=" + id;
                            meldung(id);
                            DatenFilm film = filmHolenId(getUrl, seite2, nameSenderMReader, thema, titel, urlFilm, id);
                            if (film != null) {
                                // dann wars gut
                                addFilm(film);
                                ok = true;
                            }
                        }
                        if (!ok) {
                            // dann mit der herkömmlichen Methode versuchen
                            MSearchLog.fehlerMeldung(-398012379, MSearchLog.FEHLER_ART_MREADER, "MediathekZdf.filmHolen", "auf die alte Art: " + urlFilm);
                        }
                    }
                }
            } catch (Exception ex) {
                MSearchLog.fehlerMeldung(-796325800, MSearchLog.FEHLER_ART_MREADER, "MediathekZdf.addFilme", ex, url);
            }
        }

        private synchronized String[] getListeThemen() {
            return listeThemen.pollFirst();
        }

    }

    public static DatenFilm filmHolenId(MSearchGetUrl getUrl, MSearchStringBuilder strBuffer, String sender, String thema, String titel, String filmWebsite, String urlId) {
        //<teaserimage alt="Harald Lesch im Studio von Abenteuer Forschung" key="298x168">http://www.zdf.de/ZDFmediathek/contentblob/1909108/timg298x168blob/8081564</teaserimage>
        //<detail>Möchten Sie wissen, was Sie in der nächsten Sendung von Abenteuer Forschung erwartet? Harald Lesch informiert Sie.</detail>
        //<length>00:00:34.000</length>
        //<airtime>02.07.2013 23:00</airtime>
        final String BILD = "<teaserimage";
        final String BILD_ = "key=\"2";
        final String BESCHREIBUNG = "<detail>";
        final String LAENGE = "<length>";
        final String DATUM = "<airtime>";
        final String THEMA = "<originChannelTitle>";
        int pos1, pos2;
        String bild = "", beschreibung = "", laenge = "", datum = "", zeit = "", url = "", urlKlein = "", urlHd = "", urlF4m = "";
        strBuffer = getUrl.getUri_Utf(sender, urlId, strBuffer, "url: " + filmWebsite);
        if (strBuffer.length() == 0) {
            MSearchLog.fehlerMeldung(-398745601, MSearchLog.FEHLER_ART_MREADER, "MediathekZdf.filmHolen", "url: " + urlId);
            return null;
        }
        bild = strBuffer.extract(BILD, BILD_, "<");
        if (bild.contains(">")) {
            bild = bild.substring(bild.indexOf(">") + 1);
        }
        beschreibung = strBuffer.extract(BESCHREIBUNG, "<");
        if (thema.isEmpty()) {
            thema = strBuffer.extract(THEMA, "<");
        }
        laenge = strBuffer.extract(LAENGE, "<");
        if (laenge.contains(".")) {
            laenge = laenge.substring(0, laenge.indexOf("."));
        }
        datum = strBuffer.extract(DATUM, "<");
        if (datum.contains(" ")) {
            zeit = datum.substring(datum.lastIndexOf(" ")).trim() + ":00";
            datum = datum.substring(0, datum.lastIndexOf(" ")).trim();
        }
        // erst mal URL in besserer Auflösung
        // <formitaet basetype="h264_aac_f4f_http_f4m_http" isDownload="false">
        // <quality>high</quality>
        // <url>http://fstreaming.zdf.de/3sat/300/13/07/130714_zkm_bonus_rundgang_museumscheck.f4m</url>
        // wenns das gibt --> bessere Auflösung
        final String QUALITAET = "<quality>high</quality>";
        final String URL_F4M_ANFANG = "<formitaet basetype=\"h264_aac_f4f_http_f4m_http\"";
        final String URL_F4M_ENDE = "</formitaet>";
        final String URL_F4M = "<url>";
        final String URL_ANFANG = "<formitaet basetype=\"h264_aac_mp4_http_na_na\"";
        final String URL_ANFANG_HD = "<formitaet basetype=\"wmv3_wma9_asf_mms_asx_http\"";
        final String URL_ENDE = "</formitaet>";
        final String URL = "<url>";
        int posAnfang = 0, posEnde = 0;
        posAnfang = 0;
        posEnde = 0;
        while (true) {
            if ((posAnfang = strBuffer.indexOf(URL_F4M_ANFANG, posAnfang)) == -1) {
                break;
            }
            posAnfang += URL_F4M_ANFANG.length();
            if ((posEnde = strBuffer.indexOf(URL_F4M_ENDE, posAnfang)) == -1) {
                break;
            }
            if ((pos1 = strBuffer.indexOf(QUALITAET, posAnfang)) != -1) {
                if (pos1 < posEnde) {
                    if ((pos1 = strBuffer.indexOf(URL_F4M, posAnfang)) != -1) {
                        pos1 += URL_F4M.length();
                        if ((pos2 = strBuffer.indexOf("<", pos1)) != -1) {
                            if (pos2 < posEnde) {
                                urlF4m = strBuffer.substring(pos1, pos2);
                                break;
                            }
                        }
                    }
                }
            }
        }
        // und noch die URL
        // <formitaet basetype="h264_aac_mp4_http_na_na" isDownload="false">
        //    <quality>veryhigh</quality>
        //    <url>http://nrodl.zdf.de/none/zdf/13/05/130528_vorschau_afo_1596k_p13v9.mp4</url>
        // </formitaet>
        posAnfang = 0;
        posEnde = 0;
        while (true) {
            if ((posAnfang = strBuffer.indexOf(URL_ANFANG, posAnfang)) == -1) {
                break;
            }
            posAnfang += URL_ANFANG.length();
            if ((posEnde = strBuffer.indexOf(URL_ENDE, posAnfang)) == -1) {
                break;
            }
            if ((pos1 = strBuffer.indexOf(QUALITAET, posAnfang)) != -1) {
                if (pos1 < posEnde) {
                    if (!urlKlein.isEmpty() && !urlKlein.contains("metafilegenerator")) {
                        continue;
                    }
                    if ((pos1 = strBuffer.indexOf(URL, posAnfang)) != -1) {
                        pos1 += URL.length();
                        if ((pos2 = strBuffer.indexOf("<", pos1)) != -1) {
                            if (pos2 < posEnde) {
                                urlKlein = strBuffer.substring(pos1, pos2);
                            }
                        }
                    }
                }
            }
            if ((pos1 = strBuffer.indexOf("<quality>veryhigh</quality>", posAnfang)) != -1) {
                if (pos1 < posEnde) {
                    if (!url.isEmpty() && !url.contains("metafilegenerator") && !url.contains("podfiles")) {
                        continue;
                    }
                    if ((pos1 = strBuffer.indexOf(URL, posAnfang)) != -1) {
                        pos1 += URL.length();
                        if ((pos2 = strBuffer.indexOf("<", pos1)) != -1) {
                            if (pos2 < posEnde) {
                                url = strBuffer.substring(pos1, pos2);
                            }
                        }
                    }
                }
            }
        }
        // und jetzt nochmal für HD
        posAnfang = 0;
        posEnde = 0;
        while (true) {
            if ((posAnfang = strBuffer.indexOf(URL_ANFANG_HD, posAnfang)) == -1) {
                break;
            }
            posAnfang += URL_ANFANG_HD.length();
            if ((posEnde = strBuffer.indexOf(URL_ENDE, posAnfang)) == -1) {
                break;
            }
            if ((pos1 = strBuffer.indexOf("<quality>hd</quality>", posAnfang)) != -1) {
                if (pos1 > posEnde) {
                    break;
                }
                if ((pos1 = strBuffer.indexOf(URL, posAnfang)) != -1) {
                    pos1 += URL.length();
                    if ((pos2 = strBuffer.indexOf("<", pos1)) != -1) {
                        if (pos2 < posEnde) {
                            urlHd = strBuffer.substring(pos1, pos2);
                            break;
                        }
                    }
                }
            }
        }
        if (url.isEmpty() && !urlKlein.isEmpty()) {
            url = urlKlein;
            urlKlein = "";
        }
        if (!urlF4m.isEmpty()) {
            String u = f4mUrlHolen(getUrl, sender, strBuffer, urlF4m);
            if (!u.isEmpty()) {
                url = u;
            }
        }
        if (urlHd.endsWith("asx")) {
            if (!url.isEmpty() && url.endsWith("vh.mp4")) {
                urlHd = url.replace("vh.mp4", "hd.mp4");
                if (urlHd.startsWith("http://nrodl.zdf.de")) {
                    urlHd = urlHd.replaceFirst("http://nrodl.zdf.de", "http://rodl.zdf.de");
                }
            } else {
                MSearchLog.fehlerMeldung(-915230647, MSearchLog.FEHLER_ART_MREADER, "MediathekZdf.filmHolen", "asx: " + filmWebsite);
                // "http://rodl.zdf.de/none/zdf/10/06/100601_dvoteil3_tex_vh.mp4"
                // wird
                // "http://rodl.zdf.de/none/zdf/10/06/100601_dvoteil3_tex_hd.mp4"
//                String[] u = flash(getUrl, strBuffer, urlHd, sender);
//                if (u != null) {
//                    if (!u[2].isEmpty()) {
//                        urlHd = u[2];
//                    }
//                }
            }
        }
        if (url.isEmpty()) {
            MSearchLog.fehlerMeldung(-397002891, MSearchLog.FEHLER_ART_MREADER, "MediathekZdf.filmHolen", "keine URL: " + filmWebsite);
            return null;
        } else {
            DatenFilm film = new DatenFilm(sender, thema, filmWebsite, titel, url, "" /*urlRtmp*/, datum, zeit,
                    extractDuration(laenge), beschreibung, bild, new String[]{""});
            film.addUrlKlein(urlKlein, "");
            film.addUrlHd(urlHd, "");
            return film;
        }
    }

    public static String f4mUrlHolen(MSearchGetUrl getUrl, String sender, MSearchStringBuilder strBuffer, String urlf4m) {
        //<manifest xmlns="http://ns.adobe.com/f4m/2.0">
        //    <baseURL>http://zdf_hdflash_none-f.akamaihd.net/z/</baseURL>
        //    <media href="mp4/none/3sat/13/07/130714_zkm_bonus_rundgang_museumscheck_736k_p11v11.mp4/manifest.f4m?hdcore" bitrate="680000"/>
        //    <media href="mp4/none/3sat/13/07/130714_zkm_bonus_rundgang_museumscheck_1056k_p12v11.mp4/manifest.f4m?hdcore" bitrate="1000000"/>
        //    <media href="mp4/none/3sat/13/07/130714_zkm_bonus_rundgang_museumscheck_2256k_p14v11.mp4/manifest.f4m?hdcore" bitrate="2200000"/>
        //</manifest>
        final String URL = "<media href=\"mp4";
        String url = "";
        int pos1 = 0, pos2;
        strBuffer = getUrl.getUri_Utf(sender, urlf4m, strBuffer, "url: " + urlf4m);
        if (strBuffer.length() == 0) {
            MSearchLog.fehlerMeldung(-610123987, MSearchLog.FEHLER_ART_MREADER, "MediathekZdf.f4mUrlHolen", "url: " + urlf4m);
            return "";
        }
        while (true) {
            if ((pos1 = strBuffer.indexOf(URL, pos1)) == -1) {
                break;
            } else {
                pos1 += URL.length();
                if ((pos2 = strBuffer.indexOf("?", pos1)) == -1) {
                    break;
                } else {
                    url = strBuffer.substring(pos1, pos2);
                    if (url.contains("2256k") && url.contains("mp4")) {
                        // das draus bauen:
                        // http://rodl.zdf.de/none/3sat/13/07/130714_zkm_bonus_rundgang_museumscheck_2256k_p14v11.mp4
                        url = "http://rodl.zdf.de" + url.substring(0, url.indexOf("mp4")) + "mp4";
                        return url;
                    }
                }
            }
        }
        return "";
    }

//    public static DatenFilm flash(MSearchGetUrl getUrl, MSearchStringBuilder seiteFlash, String senderName, String thema, String titel,
//            String filmWebsite, String urlFilm, String datum, String zeit, long durationInSeconds, String description,
//            String imageUrl, String[] keywords) {
//        //<param name="app" value="ondemand" />
//        //<param name="host" value="cp125301.edgefcs.net" />
//        //<param name="protocols" value="rtmp,rtmpt" />
//        //<video dur="00:29:33" paramGroup="gl-vod-rtmp" src="mp4:zdf/12/07/120724_mann_bin_ich_schoen_37g_l.mp4" system-bitrate="62000">
//        //<param name="quality" value="low" />
//        //</video>
//        //
//        //<video dur="00:29:33" paramGroup="gl-vod-rtmp" src="mp4:zdf/12/07/120724_mann_bin_ich_schoen_37g_h.mp4" system-bitrate="700000">
//        //<param name="quality" value="high" />
//        //</video>
//        //
//        //<video dur="00:29:33" paramGroup="gl-vod-rtmp" src="mp4:zdf/12/07/120724_mann_bin_ich_schoen_37g_vh.mp4" system-bitrate="1700000">
//        //<param name="quality" value="veryhigh" />
//        //</video>
//
//        //http://wstreaming.zdf.de/3sat/veryhigh/ ... _hitec.asx
//        //http://fstreaming.zdf.de/3sat/veryhigh/ ... hitec.smil
//        //rtmpt://cp125301.edgefcs.net/ondemand/mp4:zdf/12/07/120724_mann_bin_ich_schoen_37g_vh.mp4
//        DatenFilm ret = null;
//        final String MUSTER_HOST = "<param name=\"host\" value=\"";
//        final String MUSTER_APP = "<param name=\"app\" value=\"";
//        final String MUSTER_URL = "src=\"";
//        final String MUSTER_URL_L = "l.mp4";
//        final String MUSTER_URL_H = "h.mp4";
//        final String MUSTER_URL_VH = "vh.mp4";
//        String orgUrl = urlFilm;
//        String host = "";
//        String app = "";
//        String url = "", tmpUrl = "";
//        int pos1;
//        int pos2;
//        try {
//            orgUrl = orgUrl.replace("http://wstreaming.zdf.de", "http://fstreaming.zdf.de");
//            orgUrl = orgUrl.replace("http://wgeostreaming.zdf.de", "http://fgeostreaming.zdf.de");
//            orgUrl = orgUrl.replace(".asx", ".smil");
//            seiteFlash = getUrl.getUri_Utf(senderName, orgUrl, seiteFlash, "filmWebsite: " + filmWebsite);
//            String strSeiteFlash = seiteFlash.toString();
//            if ((pos1 = strSeiteFlash.indexOf(MUSTER_HOST, 0)) != -1) {
//                pos1 += MUSTER_HOST.length();
//                if ((pos2 = strSeiteFlash.indexOf("\"", pos1)) != -1) {
//                    host = strSeiteFlash.substring(pos1, pos2);
//                }
//            }
//            if ((pos1 = strSeiteFlash.indexOf(MUSTER_APP, 0)) != -1) {
//                pos1 += MUSTER_APP.length();
//                if ((pos2 = strSeiteFlash.indexOf("\"", pos1)) != -1) {
//                    app = strSeiteFlash.substring(pos1, pos2);
//                }
//            }
//            pos1 = 0;
//            boolean gefunden = false;
//            while ((pos1 = strSeiteFlash.indexOf(MUSTER_URL, pos1)) != -1) {
//                pos1 += MUSTER_URL.length();
//                if ((pos2 = strSeiteFlash.indexOf("\"", pos1)) != -1) {
//                    tmpUrl = strSeiteFlash.substring(pos1, pos2);
//                }
//                if (url.equals("")) {
//                    url = tmpUrl;
//                }
//                if (!url.contains(MUSTER_URL_VH) && tmpUrl.contains(MUSTER_URL_H)) {
//                    url = tmpUrl;
//                    gefunden = true;
//                }
//                if (tmpUrl.contains(MUSTER_URL_VH)) {
//                    url = tmpUrl;
//                    gefunden = true;
//                }
//            }
//            if (!gefunden) {
//                //<video dur="00:08:02" paramGroup="gl-vod-rtmp" src="mp4:zdf/12/09/120919_westerwelle_mom_51k_p7v9.mp4" system-bitrate="62000">
//                //<param name="quality" value="low" />
//                //</video>
//                //<video dur="00:08:02" paramGroup="gl-vod-rtmp" src="mp4:zdf/12/09/120919_westerwelle_mom_536k_p9v9.mp4" system-bitrate="700000">
//                //<param name="quality" value="high" />
//                //</video>
//                //<video dur="00:08:02" paramGroup="gl-vod-rtmp" src="mp4:zdf/12/09/120919_westerwelle_mom_1596k_p13v9.mp4" system-bitrate="1700000">
//                //<param name="quality" value="veryhigh" />
//                //</video>
//                pos1 = 0;
//                while ((pos1 = strSeiteFlash.indexOf(MUSTER_URL, pos1)) != -1) {
//                    int max = 0;
//                    pos1 += MUSTER_URL.length();
//                    if ((pos2 = strSeiteFlash.indexOf("\"", pos1)) != -1) {
//                        tmpUrl = strSeiteFlash.substring(pos1, pos2);
//                    }
//                    if (url.equals("")) {
//                        url = tmpUrl;
//                    }
//                    if (tmpUrl.contains("k_")) {
//                        String tmp = tmpUrl.substring(0, tmpUrl.lastIndexOf("k_"));
//                        if (tmp.contains("_")) {
//                            tmp = tmp.substring(tmp.lastIndexOf("_") + 1);
//                            try {
//                                int i = Integer.parseInt(tmp);
//                                if (i > max) {
//                                    max = i;
//                                    url = tmpUrl;
//                                    gefunden = true;
//                                }
//                            } catch (Exception e) {
//                            }
//                        }
//                    }
//                }
//            }
//            if (!gefunden) {
//                MSearchLog.fehlerMeldung(-302125078, MSearchLog.FEHLER_ART_MREADER, "MediathekZdf.flash-1 " + senderName, "!gefunden: " + urlFilm);
//            }
//            if (url.equals("")) {
//                ret = null;
//                MSearchLog.fehlerMeldung(-783012580, MSearchLog.FEHLER_ART_MREADER, "MediathekZdf.flash-2 " + senderName, "keine URL: " + urlFilm);
//            } else if (host.equals("")) {
//                ret = null;
//                MSearchLog.fehlerMeldung(-356047809, MSearchLog.FEHLER_ART_MREADER, "MediathekZdf.flash-3 " + senderName, "kein Host: " + urlFilm);
//            } else {
//                url = "rtmpt://" + host + "/" + app + "/" + url;
//                //ret = new DatenFilm(senderName, thema, urlThema, titel, url, ""/* urlRtmp */, datum, zeit);
//                ret = new DatenFilm(senderName, thema, filmWebsite, titel, url, ""/* urlRtmp */, datum, zeit, durationInSeconds, description,
//                        imageUrl, keywords);
//            }
//        } catch (Exception ex) {
//            MSearchLog.fehlerMeldung(-265847128, MSearchLog.FEHLER_ART_MREADER, "MediathekZdf.flash" + senderName, ex, urlFilm);
//        }
//        return ret;
//    }
//    public static String[] flash(MSearchGetUrl getUrl, MSearchStringBuilder seiteFlash, String urlFilm, String sender) {
//        //<param name="app" value="ondemand" />
//        //<param name="host" value="cp125301.edgefcs.net" />
//        //<param name="protocols" value="rtmp,rtmpt" />
//        //<video dur="00:29:33" paramGroup="gl-vod-rtmp" src="mp4:zdf/12/07/120724_mann_bin_ich_schoen_37g_l.mp4" system-bitrate="62000">
//        //<param name="quality" value="low" />
//        //</video>
//        //
//        //<video dur="00:29:33" paramGroup="gl-vod-rtmp" src="mp4:zdf/12/07/120724_mann_bin_ich_schoen_37g_h.mp4" system-bitrate="700000">
//        //<param name="quality" value="high" />
//        //</video>
//        //
//        //<video dur="00:29:33" paramGroup="gl-vod-rtmp" src="mp4:zdf/12/07/120724_mann_bin_ich_schoen_37g_vh.mp4" system-bitrate="1700000">
//        //<param name="quality" value="veryhigh" />
//        //</video>
//
//        //http://wstreaming.zdf.de/3sat/veryhigh/ ... _hitec.asx
//        //http://fstreaming.zdf.de/3sat/veryhigh/ ... hitec.smil
//        //rtmpt://cp125301.edgefcs.net/ondemand/mp4:zdf/12/07/120724_mann_bin_ich_schoen_37g_vh.mp4
//        final String MUSTER_HOST = "<param name=\"host\" value=\"";
//        final String MUSTER_APP = "<param name=\"app\" value=\"";
//        final String MUSTER_URL = "src=\"";
//        final String MUSTER_URL_L = "l.mp4";
//        final String MUSTER_URL_H = "h.mp4";
//        final String MUSTER_URL_VH = "vh.mp4";
//        String[] ret = new String[]{"", "", ""};
//        String orgUrl = urlFilm;
//        String host = "";
//        String app = "";
//        String url = "", urlLow = "", urlHd = "", tmpUrl = "";
//        int pos1;
//        int pos2;
//        try {
//            orgUrl = orgUrl.replace("http://wstreaming.zdf.de", "http://fstreaming.zdf.de");
//            orgUrl = orgUrl.replace("http://wgeostreaming.zdf.de", "http://fgeostreaming.zdf.de");
//            orgUrl = orgUrl.replace(".asx", ".smil");
//            seiteFlash = getUrl.getUri_Utf(sender, orgUrl, seiteFlash, "");
//            String strSeiteFlash = seiteFlash.toString();
//            if ((pos1 = strSeiteFlash.indexOf(MUSTER_HOST, 0)) != -1) {
//                pos1 += MUSTER_HOST.length();
//                if ((pos2 = strSeiteFlash.indexOf("\"", pos1)) != -1) {
//                    host = strSeiteFlash.substring(pos1, pos2);
//                }
//            }
//            if ((pos1 = strSeiteFlash.indexOf(MUSTER_APP, 0)) != -1) {
//                pos1 += MUSTER_APP.length();
//                if ((pos2 = strSeiteFlash.indexOf("\"", pos1)) != -1) {
//                    app = strSeiteFlash.substring(pos1, pos2);
//                }
//            }
//            pos1 = 0;
//            boolean gefunden = false;
//            while ((pos1 = strSeiteFlash.indexOf(MUSTER_URL, pos1)) != -1) {
//                pos1 += MUSTER_URL.length();
//                if ((pos2 = strSeiteFlash.indexOf("\"", pos1)) != -1) {
//                    tmpUrl = strSeiteFlash.substring(pos1, pos2);
//                }
//                if (tmpUrl.contains("hd")) {
//                    urlHd = tmpUrl;
//                    tmpUrl = "";
//                }
//                if (url.equals("")) {
//                    url = tmpUrl;
//                }
//                if (!url.contains(MUSTER_URL_VH) && tmpUrl.contains(MUSTER_URL_H)) {
//                    if (urlLow.isEmpty()) {
//                        urlLow = url;
//                    }
//                    url = tmpUrl;
//                    gefunden = true;
//                }
//                if (tmpUrl.contains(MUSTER_URL_VH)) {
//                    if (urlLow.isEmpty()) {
//                        urlLow = url;
//                    }
//                    url = tmpUrl;
//                    gefunden = true;
//                }
//            }
//            if (!gefunden) {
//                MSearchLog.fehlerMeldung(-945236478, MSearchLog.FEHLER_ART_MREADER, "MediathekZdf.flash-1 " + sender, "!gefunden: " + urlFilm);
//            }
//            if (url.equals("")) {
//                ret = null;
//                MSearchLog.fehlerMeldung(-714253698, MSearchLog.FEHLER_ART_MREADER, "MediathekZdf.flash-2 " + sender, "keine URL: " + urlFilm);
//            } else if (host.equals("")) {
//                ret = null;
//                MSearchLog.fehlerMeldung(-978451236, MSearchLog.FEHLER_ART_MREADER, "MediathekZdf.flash-3 " + sender, "kein Host: " + urlFilm);
//            } else {
//                ret[0] = "rtmpt://" + host + "/" + app + "/" + urlLow;
//                ret[1] = "rtmpt://" + host + "/" + app + "/" + url;
//                ret[2] = "rtmpt://" + host + "/" + app + "/" + urlHd;
//            }
//        } catch (Exception ex) {
//            MSearchLog.fehlerMeldung(-802030478, MSearchLog.FEHLER_ART_MREADER, "MediathekZdf.flash" + sender, ex, urlFilm);
//        }
//        return ret;
//    }
//    public static DatenFilm quicktime(MSearchGetUrl getUrl, MSearchStringBuilder seiteQuicktime, String senderName, String thema, String titel, String filmWebsite, String urlFilm, String datum, String zeit, long durationInSeconds, String description, String thumbnailUrl, String imageUrl, String[] keywords) {
//        // RTSPtext
//        // rtsp://a1966.v1252936.c125293.g.vq.akamaistream.net/7/1966/125293/v0001/mp4.od.origin.zdf.de.gl-systemhaus.de/none/3sat/13/06/130614_meyer_kuz_1596k_p13v9.mp4
//        // 
//        // daraus wird:
//        // http://rodl.zdf.de / none/3sat/13/06/130621_news_kuz_436k_p9v11.mp4
//        DatenFilm ret = null;
//        final String MUSTER_URL = "rtsp://";
//        final String MUSTER_TAUSCH = "gl-systemhaus.de";
//        final String MUSTER_ENDE = ".mp4";
//        String orgUrl = urlFilm;
//        String url, tmpUrl = "";
//        int pos1;
//        int pos2;
//        try {
//            seiteQuicktime = getUrl.getUri_Utf(senderName, orgUrl, seiteQuicktime, "filmWebsite: " + filmWebsite);
//            String strSeiteQuicktime = seiteQuicktime.toString();
//            if ((pos1 = strSeiteQuicktime.indexOf(MUSTER_URL, 0)) != -1) {
//                pos1 += MUSTER_URL.length();
//                if ((pos2 = strSeiteQuicktime.indexOf(MUSTER_ENDE, pos1)) != -1) {
//                    tmpUrl = strSeiteQuicktime.substring(pos1, pos2);
//                }
//            }
//            if (tmpUrl.equals("")) {
//                MSearchLog.fehlerMeldung(-679893014, MSearchLog.FEHLER_ART_MREADER, "Mediathek3sat.quicktime", "!gefunden: " + urlFilm);
//            } else {
//                tmpUrl = MUSTER_URL + tmpUrl + MUSTER_ENDE;
//                if ((pos1 = tmpUrl.indexOf(MUSTER_TAUSCH)) != -1) {
//                    pos1 += MUSTER_TAUSCH.length();
//                    url = "http://rodl.zdf.de" + tmpUrl.substring(pos1);
//                    //ret = new DatenFilm(senderName, thema, urlThema, titel, url, ""/* urlRtmp */, datum, zeit);
//                    ret = new DatenFilm(senderName, thema, filmWebsite, titel, url, ""/* urlRtmp */, datum, zeit, durationInSeconds, description,
//                            imageUrl.isEmpty() ? thumbnailUrl : imageUrl, keywords);
//                } else {
//                    MSearchLog.fehlerMeldung(-918596307, MSearchLog.FEHLER_ART_MREADER, "Mediathek3sat.quicktime", "url passt nicht: " + urlFilm);
//                }
//            }
//        } catch (Exception ex) {
//            MSearchLog.fehlerMeldung(-265847128, MSearchLog.FEHLER_ART_MREADER, "MediathekZdf.flash" + senderName, ex, urlFilm);
//        }
//        return ret;
//    }
}
