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

public class MediathekZdf extends MediathekReader implements Runnable {

    public final static String SENDERNAME = "ZDF";
    private MSStringBuilder seite = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
    private final static int ANZAHL_ZDF_ALLE = 500;
    private final static int ANZAHL_ZDF_MITTEL = 50;
    private final static int ANZAHL_ZDF_UPDATE = 20;
    private final static int ANZAHL_ZDF_KURZ = 10;

    public MediathekZdf(MSFilmeSuchen ssearch, int startPrio) {
        super(ssearch, SENDERNAME, 4 /* threads */, 250 /* urlWarten */, startPrio);
    }

    @Override
    public void addToList() {
        listeThemen.clear();
        meldungStart();
        // Liste von http://www.zdf.de/ZDFmediathek/hauptnavigation/sendung-a-bis-z/saz0 bis sat8 holen
        String addr = "http://www.zdf.de/ZDFmediathek/hauptnavigation/sendung-a-bis-z/saz";
        for (int i = 0; i <= 8; ++i) {
            addToList_addr(addr + i, MSConfig.senderAllesLaden ? ANZAHL_ZDF_ALLE : ANZAHL_ZDF_UPDATE);
        }
        // Spartenkanäle einfügen
        addToList_addr("http://www.zdf.de/ZDFmediathek/senderstartseite/sst1/1209122", MSConfig.senderAllesLaden ? ANZAHL_ZDF_ALLE : ANZAHL_ZDF_UPDATE); // zdf-neo
        addToList_addr("http://www.zdf.de/ZDFmediathek/senderstartseite/sst1/1209120", MSConfig.senderAllesLaden ? ANZAHL_ZDF_ALLE : ANZAHL_ZDF_UPDATE); // zdf-info
        addToList_addr("http://www.zdf.de/ZDFmediathek/senderstartseite/sst1/1317640", MSConfig.senderAllesLaden ? ANZAHL_ZDF_ALLE : ANZAHL_ZDF_UPDATE); // zdf-kultur
        //Rubriken einfügen
        if (MSConfig.senderAllesLaden) {
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
        if (MSConfig.senderAllesLaden) {
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
        if (MSConfig.getStop()) {
            meldungThreadUndFertig();
        } else if (listeThemen.size() == 0) {
            meldungThreadUndFertig();
        } else {
            meldungAddMax(listeThemen.size());
            //alles auswerten
            for (int t = 0; t < maxThreadLaufen; ++t) {
                //new Thread(new ThemaLaden()).start();
                Thread th = new Thread(new ThemaLaden());
                th.setName(SENDERNAME + t);
                th.start();
            }
        }
    }

    private void addToList_Rubrik(String addr) {
        final String MUSTER_URL = "<p><b><a href=\"/ZDFmediathek/kanaluebersicht/aktuellste/";
        //GetUrl(int ttimeout, long wwartenBasis) {
        MSGetUrl getUrl = new MSGetUrl(wartenSeiteLaden);
        MSStringBuilder seiteR = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        seiteR = getUrl.getUri(SENDERNAME, addr, MSConst.KODIERUNG_UTF, 6 /* versuche */, seiteR, "" /* Meldung */);
        if (seiteR.length() == 0) {
            MSLog.fehlerMeldung(774200364, "Leere Seite für URL: " + addr);
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
                MSLog.fehlerMeldung(754126900, "keine URL: " + addr);
            } else {
                url = "http://www.zdf.de/ZDFmediathek/kanaluebersicht/aktuellste/" + url + "?bc=rub";
                addToList_addr(url, ANZAHL_ZDF_UPDATE); // immer nur eine "kurz"
            }
        }
    }

    private void addToList_addr(String addr, int anz) {
        final String MUSTER_URL = "<p><b><a href=\"/ZDFmediathek/kanaluebersicht/aktuellste/";
        //GetUrl(int ttimeout, long wwartenBasis) {
        MSGetUrl getUrl = new MSGetUrl(wartenSeiteLaden);
        seite = getUrl.getUri(SENDERNAME, addr, MSConst.KODIERUNG_UTF, 6 /* versuche */, seite, "" /* Meldung */);
        if (seite.length() == 0) {
            MSLog.fehlerMeldung(596004563, "Leere Seite für URL: " + addr);
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
                MSLog.fehlerMeldung(946325890, "keine URL: " + addr);
            } else {
                url = "http://www.zdf.de/ZDFmediathek/kanaluebersicht/aktuellste/" + url;
                urlThema = url;
                url += "?teaserListIndex=" + anz;
                addThemenliste(url, urlThema, thema);
            }
        }
    }

    private synchronized void addThemenliste(String url, String urlThema, String thema) {
        String[] add = new String[]{url, urlThema, thema};
        listeThemen.addUrl(add);
    }

    private class ThemaLaden implements Runnable {

        MSGetUrl getUrl = new MSGetUrl(wartenSeiteLaden);
        private MSStringBuilder seite1 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        private final MSStringBuilder seite2 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);

        @Override
        public void run() {
            try {
                String link[];
                meldungAddThread();
                while (!MSConfig.getStop() && (link = getListeThemen()) != null) {
                    seite1.setLength(0);
                    addFilme(link[0]/* url */, link[1]/* urlThema */, link[2]/* Thema */);
                    meldungProgress(link[0]);
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(496583200, ex);
            }
            meldungThreadUndFertig();
        }

        private void addFilme(String url, String urlThema, String thema) {
            final String MUSTER_URL_1 = "<p><b><a href=\"/ZDFmediathek/beitrag/video/";
            String titel = "";
            String urlFilm;
            boolean ok;
            int pos = 0;
            int pos1;
            int pos2;
            int pos3;
            int anz = 0;
            try {
                seite1 = getUrl.getUri_Utf(SENDERNAME, url, seite1, "Thema: " + thema);
                while (!MSConfig.getStop() && (pos = seite1.indexOf(MUSTER_URL_1, pos)) != -1) {
                    ok = false;
                    ++anz;
                    if (!MSConfig.senderAllesLaden) {
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
                        MSLog.fehlerMeldung(643269690, "keine URL: " + url);
                    } else {
                        // über die ID versuchen
                        urlFilm = "http://www.zdf.de/ZDFmediathek/beitrag/video/" + urlFilm;
                        String urlId = "";
                        if ((pos1 = urlFilm.indexOf("/ZDFmediathek/beitrag/video/")) != -1) {
                            pos1 += "/ZDFmediathek/beitrag/video/".length();
                            if ((pos2 = urlFilm.indexOf("/", pos1)) != -1) {
                                urlId = urlFilm.substring(pos1, pos2);
                                // System.out.println(id);
                            }
                        }
                        if (!urlId.isEmpty()) {
                            urlId = "http://www.zdf.de/ZDFmediathek/xmlservice/web/beitragsDetails?ak=web&id=" + urlId;
                            meldung(urlId);
                            DatenFilm film = filmHolenId(getUrl, seite2, SENDERNAME, thema, titel, urlFilm, urlId);
                            if (film != null) {
                                // dann wars gut
                                // jetzt noch manuell die Auflösung hochsetzen
                                urlTauschen(film, url, mSearchFilmeSuchen);
                                addFilm(film);
                                ok = true;
                            }
                        }
                        if (!ok) {
                            // dann mit der herkömmlichen Methode versuchen
                            MSLog.fehlerMeldung(398012379, "auf die alte Art: " + urlFilm);
                        }
                    }
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(796325800, ex, url);
            }
        }

        private synchronized String[] getListeThemen() {
            return listeThemen.pollFirst();
        }

    }

    public static void urlTauschen(DatenFilm film, String urlSeite, MSFilmeSuchen mSFilmeSuchen) {
        // manuell die Auflösung hochsetzen
        if (film.arr[DatenFilm.FILM_URL_NR].endsWith("1456k_p13v11.mp4")) {
            String url_ = film.arr[DatenFilm.FILM_URL_NR].substring(0, film.arr[DatenFilm.FILM_URL_NR].lastIndexOf("1456k_p13v11.mp4")) + "2256k_p14v11.mp4";
            String l = mSFilmeSuchen.listeFilmeAlt.getDateiGroesse(url_, film.arr[DatenFilm.FILM_SENDER_NR]);
            // zum Testen immer machen!!
            film.arr[DatenFilm.FILM_GROESSE_NR] = l;
            film.arr[DatenFilm.FILM_URL_NR] = url_;
        }

        // manuell die Auflösung hochsetzen
        if (film.arr[DatenFilm.FILM_URL_NR].endsWith("1456k_p13v12.mp4")) {
            String url_ = film.arr[DatenFilm.FILM_URL_NR].substring(0, film.arr[DatenFilm.FILM_URL_NR].lastIndexOf("1456k_p13v12.mp4")) + "2256k_p14v12.mp4";
            String l = mSFilmeSuchen.listeFilmeAlt.getDateiGroesse(url_, film.arr[DatenFilm.FILM_SENDER_NR]);
            // zum Testen immer machen!!
            film.arr[DatenFilm.FILM_GROESSE_NR] = l;
            film.arr[DatenFilm.FILM_URL_NR] = url_;
            if (!l.isEmpty()) {
                film.arr[DatenFilm.FILM_GROESSE_NR] = l;
                film.arr[DatenFilm.FILM_URL_NR] = url_;
            } else if (urlExists(url_)) {
                // dann wars wohl nur ein "403er"
                film.arr[DatenFilm.FILM_URL_NR] = url_;
            } else {
                MSLog.fehlerMeldung(945120369, "urlTauschen: " + urlSeite);
            }
        }
    }

    public static DatenFilm filmHolenId(MSGetUrl getUrl, MSStringBuilder strBuffer, String sender, String thema, String titel, String filmWebsite, String urlId) {
        //<teaserimage alt="Harald Lesch im Studio von Abenteuer Forschung" key="298x168">http://www.zdf.de/ZDFmediathek/contentblob/1909108/timg298x168blob/8081564</teaserimage>
        //<detail>Möchten Sie wissen, was Sie in der nächsten Sendung von Abenteuer Forschung erwartet? Harald Lesch informiert Sie.</detail>
        //<length>00:00:34.000</length>
        //<airtime>02.07.2013 23:00</airtime>
        final String BESCHREIBUNG = "<detail>";
        final String LAENGE_SEC = "<lengthSec>";
        final String LAENGE = "<length>";
        final String DATUM = "<airtime>";
        final String THEMA = "<originChannelTitle>";
        long laengeL;

        String beschreibung, subtitle, laenge, datum, zeit = "";

        strBuffer = getUrl.getUri_Utf(sender, urlId, strBuffer, "URL-Filmwebsite: " + filmWebsite);
        if (strBuffer.length() == 0) {
            MSLog.fehlerMeldung(398745601, "url: " + urlId);
            return null;
        }

        subtitle = strBuffer.extract("<caption>", "<url>http://", "<", "http://");
        beschreibung = strBuffer.extract(BESCHREIBUNG, "<");
        if (beschreibung.isEmpty()) {
            beschreibung = strBuffer.extract(BESCHREIBUNG, "</");
            beschreibung = beschreibung.replace("<![CDATA[", "");
            beschreibung = beschreibung.replace("]]>", "");
            if (beschreibung.isEmpty()) {
                MSLog.fehlerMeldung(945123074, "url: " + urlId);
            }
        }
        if (thema.isEmpty()) {
            thema = strBuffer.extract(THEMA, "<");
        }

        laenge = strBuffer.extract(LAENGE_SEC, "<");
        if (!laenge.isEmpty()) {
            laengeL = extractDurationSec(laenge);
        } else {
            laenge = strBuffer.extract(LAENGE, "<");
            if (laenge.contains(".")) {
                laenge = laenge.substring(0, laenge.indexOf("."));
            }
            laengeL = extractDuration(laenge);
        }

        datum = strBuffer.extract(DATUM, "<");
        if (datum.contains(" ")) {
            zeit = datum.substring(datum.lastIndexOf(" ")).trim() + ":00";
            datum = datum.substring(0, datum.lastIndexOf(" ")).trim();
        }

        //============================================================================
        // und jetzt die FilmURLs
        final String[] QU_WIDTH_HD = {"1280"};
        final String[] QU_WIDTH = {"1024", "852", "720", "688", "480", "432", "320"};
        final String[] QU_WIDTH_KL = {"688", "480", "432", "320"};
        String url, urlKlein, urlHd, tmp = "";

        urlHd = getUrl(strBuffer, QU_WIDTH_HD, tmp, true);
        url = getUrl(strBuffer, QU_WIDTH, tmp, true);
        urlKlein = getUrl(strBuffer, QU_WIDTH_KL, tmp, false);

        if (url.equals(urlKlein)) {
            urlKlein = "";
        }
        if (url.isEmpty()) {
            url = urlKlein;
            urlKlein = "";
        }

        //===================================================
        if (urlHd.isEmpty()) {
//            MSLog.fehlerMeldung(912024587, "keine URL: " + filmWebsite);
        }
        if (urlKlein.isEmpty()) {
//            MSLog.fehlerMeldung(310254698, "keine URL: " + filmWebsite);
        }
        if (url.isEmpty()) {
            MSLog.fehlerMeldung(397002891, "keine URL: " + filmWebsite);
            return null;
        } else {
            DatenFilm film = new DatenFilm(sender, thema, filmWebsite, titel, url, "" /*urlRtmp*/, datum, zeit,
                    laengeL, beschreibung);
            if (!subtitle.isEmpty()) {
                film.addUrlSubtitle(subtitle);
            }
            film.addUrlKlein(urlKlein, "");
            film.addUrlHd(urlHd, "");
            return film;
        }
    }

    private static String getUrl(MSStringBuilder strBuffer, String[] arr, String tmp, boolean hd) {
        final String URL_ANFANG = "<formitaet basetype=\"h264_aac_mp4_http_na_na\"";
        final String URL_ENDE = "</formitaet>";
        final String URL = "<url>";
        final String WIDTH = "<width>";

        String ret = "";
        tmp = "";
        int posAnfang, posEnde;
        for (String qual : arr) {
            posAnfang = 0;
            while (true) {
                if ((posAnfang = strBuffer.indexOf(URL_ANFANG, posAnfang)) == -1) {
                    break;
                }
                posAnfang += URL_ANFANG.length();
                if ((posEnde = strBuffer.indexOf(URL_ENDE, posAnfang)) == -1) {
                    break;
                }

                tmp = strBuffer.extract(URL, "<", posAnfang, posEnde);
                if (strBuffer.extract(WIDTH, "<", posAnfang, posEnde).equals(qual)) {
                    if (hd) {
                        ret = checkUrlHD(tmp);
                    } else {
                        ret = checkUrl(tmp);
                    }
                    if (!ret.isEmpty()) {
                        return ret;
                    }
                }
            }
        }
        return ret;
    }

    private static String checkUrlHD(String url) {
        String ret = "";
        if (url.startsWith("http") && url.endsWith("mp4")) {
            ret = url;
            if (ret.startsWith("http://www.metafilegenerator.de/ondemand/zdf/hbbtv/")) {
                ret = ret.replaceFirst("http://www.metafilegenerator.de/ondemand/zdf/hbbtv/", "http://nrodl.zdf.de/");
            }
        }
        return ret;
    }

    private static String checkUrl(String url) {
        String ret = "";
        if (url.startsWith("http") && url.endsWith("mp4")) {
            if (!url.startsWith("http://www.metafilegenerator.de/")) {
                ret = url;
            }
        }
        return ret;
    }

////    public static DatenFilm filmHolenId(MSGetUrl getUrl, MSStringBuilder strBuffer, String sender, String thema, String titel, String filmWebsite, String urlId) {
////        //<teaserimage alt="Harald Lesch im Studio von Abenteuer Forschung" key="298x168">http://www.zdf.de/ZDFmediathek/contentblob/1909108/timg298x168blob/8081564</teaserimage>
////        //<detail>Möchten Sie wissen, was Sie in der nächsten Sendung von Abenteuer Forschung erwartet? Harald Lesch informiert Sie.</detail>
////        //<length>00:00:34.000</length>
////        //<airtime>02.07.2013 23:00</airtime>
////        final String BESCHREIBUNG = "<detail>";
////        final String LAENGE_SEC = "<lengthSec>";
////        final String LAENGE = "<length>";
////        final String DATUM = "<airtime>";
////        final String THEMA = "<originChannelTitle>";
////        int pos1, pos2;
////        long laengeL;
////
////        String beschreibung, subtitle, laenge, datum;
////        String zeit = "", url = "", urlKlein = "", urlHd = "", urlF4m = "";
////
////        strBuffer = getUrl.getUri_Utf(sender, urlId, strBuffer, "URL-Filmwebsite: " + filmWebsite);
////        if (strBuffer.length() == 0) {
////            MSLog.fehlerMeldung(398745601, "url: " + urlId);
////            return null;
////        }
////
////        subtitle = strBuffer.extract("<caption>", "<url>http://", "<");
////        beschreibung = strBuffer.extract(BESCHREIBUNG, "<");
////        if (beschreibung.isEmpty()) {
////            beschreibung = strBuffer.extract(BESCHREIBUNG, "</");
////            beschreibung = beschreibung.replace("<![CDATA[", "");
////            beschreibung = beschreibung.replace("]]>", "");
////            if (beschreibung.isEmpty()) {
////                MSLog.fehlerMeldung(945123074, "url: " + urlId);
////            }
////        }
////        if (thema.isEmpty()) {
////            thema = strBuffer.extract(THEMA, "<");
////        }
////
////        laenge = strBuffer.extract(LAENGE_SEC, "<");
////        if (!laenge.isEmpty()) {
////            laengeL = extractDurationSec(laenge);
////        } else {
////            laenge = strBuffer.extract(LAENGE, "<");
////            if (laenge.contains(".")) {
////                laenge = laenge.substring(0, laenge.indexOf("."));
////            }
////            laengeL = extractDuration(laenge);
////        }
////
////        datum = strBuffer.extract(DATUM, "<");
////        if (datum.contains(" ")) {
////            zeit = datum.substring(datum.lastIndexOf(" ")).trim() + ":00";
////            datum = datum.substring(0, datum.lastIndexOf(" ")).trim();
////        }
////
////        //============================================================================
////        // und jetzt die FilmURLs
////        // erst mal URL in besserer Auflösung
////        // <formitaet basetype="h264_aac_f4f_http_f4m_http" isDownload="false">
////        // <quality>high</quality>
////        // <url>http://fstreaming.zdf.de/3sat/300/13/07/130714_zkm_bonus_rundgang_museumscheck.f4m</url>
////        // wenns das gibt --> bessere Auflösung
////        final String QUALITAET = "<quality>high</quality>";
////        final String URL_F4M_ANFANG = "<formitaet basetype=\"h264_aac_f4f_http_f4m_http\"";
////        final String URL_F4M_ENDE = "</formitaet>";
////        final String URL_F4M = "<url>";
////        final String URL_ANFANG = "<formitaet basetype=\"h264_aac_mp4_http_na_na\"";
////        final String URL_ANFANG_HD = "<formitaet basetype=\"wmv3_wma9_asf_mms_asx_http\"";
////        final String URL_ENDE = "</formitaet>";
////        final String URL = "<url>";
////        int posAnfang, posEnde;
////
////        posAnfang = 0;
////        while (true) {
////            if ((posAnfang = strBuffer.indexOf(URL_F4M_ANFANG, posAnfang)) == -1) {
////                break;
////            }
////            posAnfang += URL_F4M_ANFANG.length();
////            if ((posEnde = strBuffer.indexOf(URL_F4M_ENDE, posAnfang)) == -1) {
////                break;
////            }
////            if ((pos1 = strBuffer.indexOf(QUALITAET, posAnfang)) != -1) {
////                if (pos1 < posEnde) {
////                    if ((pos1 = strBuffer.indexOf(URL_F4M, posAnfang)) != -1) {
////                        pos1 += URL_F4M.length();
////                        if ((pos2 = strBuffer.indexOf("<", pos1)) != -1) {
////                            if (pos2 < posEnde) {
////                                urlF4m = strBuffer.substring(pos1, pos2);
////                                break;
////                            }
////                        }
////                    }
////                }
////            }
////        }
////
////        posAnfang = 0;
////        while (true) {
////            if ((posAnfang = strBuffer.indexOf(URL_ANFANG, posAnfang)) == -1) {
////                break;
////            }
////            posAnfang += URL_ANFANG.length();
////            if ((posEnde = strBuffer.indexOf(URL_ENDE, posAnfang)) == -1) {
////                break;
////            }
////            if ((pos1 = strBuffer.indexOf(QUALITAET, posAnfang)) != -1) {
////                if (pos1 < posEnde) {
////                    if (!urlKlein.isEmpty() && !urlKlein.contains("metafilegenerator")) {
////                        continue;
////                    }
////                    urlKlein = strBuffer.extract(URL, "<", posAnfang, posEnde);
////                }
////            }
////            if ((pos1 = strBuffer.indexOf("<quality>veryhigh</quality>", posAnfang)) != -1) {
////                if (pos1 < posEnde) {
////                    if (!url.isEmpty() && !url.contains("metafilegenerator") && !url.contains("podfiles")) {
////                        continue;
////                    }
////                    url = strBuffer.extract(URL, "<", posAnfang, posEnde);
////                }
////            }
////        }
////
////        // und jetzt nochmal für HD
////        posAnfang = 0;
////        while (true) {
////            if ((posAnfang = strBuffer.indexOf(URL_ANFANG_HD, posAnfang)) == -1) {
////                break;
////            }
////            posAnfang += URL_ANFANG_HD.length();
////            if ((posEnde = strBuffer.indexOf(URL_ENDE, posAnfang)) == -1) {
////                break;
////            }
////            if ((pos1 = strBuffer.indexOf("<quality>hd</quality>", posAnfang)) != -1) {
////                if (pos1 > posEnde) {
////                    break;
////                }
////                urlHd = strBuffer.extract(URL, "<", posAnfang, posEnde);
////            }
////        }
////        if (url.isEmpty() && !urlKlein.isEmpty()) {
////            url = urlKlein;
////            urlKlein = "";
////        }
////
////        if (!urlF4m.isEmpty()) {
////
////            String u = f4mUrlHolen(getUrl, sender, strBuffer, urlF4m);
////            if (!u.isEmpty()) {
////                ++count_f4m;
////                if (url.contains("/zdf/")) {
////                    url = url.substring(0, url.indexOf("/zdf/")) + "/" + u;
////                } else if (url.contains("/3sat/")) {
////                    url = url.substring(0, url.indexOf("/3sat/")) + "/" + u;
////                }
////            }
////        }
////
////        if (urlHd.endsWith("asx")) {
////            ++count_asx;
////
////            if (!url.isEmpty() && url.endsWith("vh.mp4")) {
////                urlHd = url.replace("vh.mp4", "hd.mp4");
////                if (urlHd.startsWith("http://nrodl.zdf.de")) {
////                    urlHd = urlHd.replaceFirst("http://nrodl.zdf.de", "http://rodl.zdf.de");
////                }
////            } else if (!url.isEmpty() && url.endsWith("1596k_p15v9.mp4")) {
////                // Entferne das 1596k_p15v9.mp4 und ersetzte das Ende mit: 3056k_p15v9.mp4
////                urlHd = url.replace("1596k_p15v9.mp4", "3056k_p15v9.mp4");
////            } else if (!url.isEmpty() && url.endsWith("1596k_p13v9.mp4")) {
////                // Entferne das 1596k_p15v9.mp4 und ersetzte das Ende mit: 3056k_p15v9.mp4
////                urlHd = url.replace("1596k_p13v9.mp4", "3056k_p15v9.mp4");
////            } else {
////                MSLog.fehlerMeldung(915230647, "asx: " + filmWebsite);
////            }
////        }
////        //===================================================
////        if (url.isEmpty()) {
////            MSLog.fehlerMeldung(397002891, "keine URL: " + filmWebsite);
////            return null;
////        } else {
////            System.out.println("ASX: " + count_asx);
////            System.out.println("f4m: " + count_f4m);
////
////            DatenFilm film = new DatenFilm(sender, thema, filmWebsite, titel, url, "" /*urlRtmp*/, datum, zeit,
////                    laengeL, beschreibung);
////            if (!subtitle.isEmpty()) {
////                subtitle = "http://" + subtitle;
////                film.addUrlSubtitle(subtitle);
////            }
////            film.addUrlKlein(urlKlein, "");
////            film.addUrlHd(urlHd, "");
////            return film;
////        }
////    }
////    public static String f4mUrlHolen(MSGetUrl getUrl, String sender, MSStringBuilder strBuffer, String urlf4m) {
////        //<manifest xmlns="http://ns.adobe.com/f4m/2.0">
////        //    <baseURL>http://zdf_hdflash_none-f.akamaihd.net/z/</baseURL>
////        //    <media href="mp4/none/3sat/13/07/130714_zkm_bonus_rundgang_museumscheck_736k_p11v11.mp4/manifest.f4m?hdcore" bitrate="680000"/>
////        //    <media href="mp4/none/3sat/13/07/130714_zkm_bonus_rundgang_museumscheck_1056k_p12v11.mp4/manifest.f4m?hdcore" bitrate="1000000"/>
////        //    <media href="mp4/none/3sat/13/07/130714_zkm_bonus_rundgang_museumscheck_2256k_p14v11.mp4/manifest.f4m?hdcore" bitrate="2200000"/>
////        //</manifest>
////        final String URL = "<media href=\"";
////        String url;
////        int pos1 = 0, pos2;
////        strBuffer = getUrl.getUri_Utf(sender, urlf4m, strBuffer, "" /* Meldung */);
////        if (strBuffer.length() == 0) {
////            // MSLog.fehlerMeldung(610123987, MSLog.FEHLER_ART_MREADER, "MediathekZdf.f4mUrlHolen", "url: " + urlf4m);
////            return "";
////        }
////        while (true) {
////            if ((pos1 = strBuffer.indexOf(URL, pos1)) == -1) {
////                break;
////            } else {
////                pos1 += URL.length();
////                if ((pos2 = strBuffer.indexOf("?", pos1)) == -1) {
////                    break;
////                } else {
////                    url = strBuffer.substring(pos1, pos2);
////                    if (url.contains("2256k") && url.contains("mp4")) {
////                        // das draus bauen:
////                        // http://rodl.zdf.de/none/3sat/13/07/130714_zkm_bonus_rundgang_museumscheck_2256k_p14v11.mp4
////                        url = url.substring(0, url.indexOf("mp4")) + "mp4";
////                        return url;
////                    }
////                }
////            }
////        }
////        return "";
////    }
}
