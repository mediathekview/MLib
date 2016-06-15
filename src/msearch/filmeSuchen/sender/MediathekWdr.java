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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import msearch.daten.DatenFilm;
import msearch.filmeSuchen.MSFilmeSuchen;
import msearch.filmeSuchen.MSGetUrl;
import msearch.tool.MSConfig;
import msearch.tool.MSConst;
import msearch.tool.MSLog;
import msearch.tool.MSStringBuilder;

public class MediathekWdr extends MediathekReader implements Runnable {

    public final static String SENDERNAME = "WDR";
    private final static String ROCKPALAST_URL = "http://www1.wdr.de/fernsehen/kultur/rockpalast/videos/rockpalastvideos_konzerte100.html";
    private final static String ROCKPALAST_FESTIVAL = "http://www1.wdr.de/fernsehen/kultur/rockpalast/videos/rockpalastvideos_festivals100.html";
    private final static String MAUS = "http://www.wdrmaus.de/lachgeschichten/spots.php5";
    private final static String ELEFANT = "http://www.wdrmaus.de/elefantenseite/data/tableOfContents.php5";
    private final ArrayList<String> listeFestival = new ArrayList<>();
    private final ArrayList<String> listeRochpalast = new ArrayList<>();
    private final ArrayList<String> listeMaus = new ArrayList<>();
    private final ArrayList<String> listeElefant = new ArrayList<>();
    private final LinkedList<String> listeTage = new LinkedList<>();
    private MSStringBuilder seite_1 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
    private MSStringBuilder seite_2 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);

    public MediathekWdr(MSFilmeSuchen ssearch, int startPrio) {
        super(ssearch, SENDERNAME,/* threads */ 3, /* urlWarten */ 500, startPrio);
    }

    //===================================
    // public
    //===================================
    @Override
    public synchronized void addToList() {
        //Theman suchen
        listeThemen.clear();
        listeTage.clear();
        listeFestival.clear();
        listeRochpalast.clear();
        listeMaus.clear();
        listeElefant.clear();
        meldungStart();
        addToList__();
        addTage();
        if (MSConfig.loadLongMax()) {
//////            maus();
//////            elefant();
//////            rockpalast();
//////            festival();
//////            // damit sie auch gestartet werden (im idealfall in unterschiedlichen Threads
//////            String[] add = new String[]{ROCKPALAST_URL, "Rockpalast"};
//////            listeThemen.addUrl(add);
//////            add = new String[]{ROCKPALAST_FESTIVAL, "Rockpalast"};
//////            listeThemen.addUrl(add);
//////            add = new String[]{MAUS, "Maus"};
//////            listeThemen.addUrl(add);
//////            add = new String[]{ELEFANT, "Elefant"};
//////            listeThemen.addUrl(add);
        }

        if (MSConfig.getStop()) {
            meldungThreadUndFertig();
        } else if (listeThemen.isEmpty() && listeTage.isEmpty() && listeFestival.isEmpty() && listeRochpalast.isEmpty() && listeMaus.isEmpty() && listeElefant.isEmpty()) {
            meldungThreadUndFertig();
        } else {
            meldungAddMax(listeThemen.size() + listeTage.size() + listeFestival.size() + listeRochpalast.size() + listeMaus.size() + listeElefant.size());
            for (int t = 0; t < maxThreadLaufen; ++t) {
                //new Thread(new ThemaLaden()).start();
                Thread th = new Thread(new ThemaLaden());
                th.setName(SENDERNAME + t);
                th.start();
            }
        }
    }

    //===================================
    // private
    //===================================
//    private void rockpalast() {
//        final String ROOTADR = "http://www.wdr.de/fernsehen/kultur/rockpalast/videos/";
//        final String ITEM_1 = "<a href=\"/fernsehen/kultur/rockpalast/videos/";
//        seite_1 = getUrlIo.getUri(SENDERNAME, ROCKPALAST_URL, MSConst.KODIERUNG_UTF, 3 /* versuche */, seite_1, "");
//        try {
//            seite_1.extractList(ITEM_1, "\"", 0, ROOTADR, listeRochpalast);
//        } catch (Exception ex) {
//            MSLog.fehlerMeldung(915423698, ex);
//        }
//    }
//
//    private void maus() {
//        // http://www.wdrmaus.de/lachgeschichten/mausspots/achterbahn.php5
//        final String ROOTADR = "http://www.wdrmaus.de/lachgeschichten/";
//        final String ITEM_1 = "<li class=\"filmvorschau\"><a href=\"../lachgeschichten/";
//        seite_1 = getUrlIo.getUri(SENDERNAME, MAUS, MSConst.KODIERUNG_UTF, 3 /* versuche */, seite_1, "");
//        try {
//            seite_1.extractList(ITEM_1, "\"", 0, ROOTADR, listeMaus);
//        } catch (Exception ex) {
//            MSLog.fehlerMeldung(975456987, ex);
//        }
//    }
//
//    private void elefant() {
//        // http://www.wdrmaus.de/lachgeschichten/mausspots/achterbahn.php5
//        // http://www.wdrmaus.de/elefantenseite/data/xml/ganze_sendung/folge_elefantenkonzert_2012.xml
//        final String ITEM_1 = "<xmlPath><![CDATA[data/xml/adventskalender/filme/";
//        final String ITEM_2 = "<xmlPath><![CDATA[data/xml/filme/";
//        final String ITEM_3 = "<xmlPath><![CDATA[data/xml/ganze_sendung/";
//        seite_1 = getUrlIo.getUri(SENDERNAME, ELEFANT, MSConst.KODIERUNG_UTF, 3 /* versuche */, seite_1, "");
//        try {
//            seite_1.extractList(ITEM_1, "]", 0, "http://www.wdrmaus.de/elefantenseite/data/xml/adventskalender/filme/", listeElefant);
//            seite_1.extractList(ITEM_2, "]", 0, "http://www.wdrmaus.de/elefantenseite/data/xml/filme/", listeElefant);
//            seite_1.extractList(ITEM_3, "]", 0, "http://www.wdrmaus.de/elefantenseite/data/xml/ganze_sendung/", listeElefant);
//        } catch (Exception ex) {
//            MSLog.fehlerMeldung(975456987, ex);
//        }
//    }
//
//    private void festival() {
//        // http://www1.wdr.de/fernsehen/kultur/rockpalast/videos/rockpalastvideos_festivals100.html
//        final String ROOTADR = "http://www1.wdr.de/fernsehen/kultur/rockpalast/videos/";
//        final String ITEM_1 = "<a href=\"/fernsehen/kultur/rockpalast/videos/";
//        seite_1 = getUrlIo.getUri(SENDERNAME, ROCKPALAST_FESTIVAL, MSConst.KODIERUNG_UTF, 3 /* versuche */, seite_1, "");
//        try {
//            seite_1.extractList(ITEM_1, "\"", 0, ROOTADR, listeFestival);
//        } catch (Exception ex) {
//            MSLog.fehlerMeldung(432365698, ex);
//        }
//    }
    private void addTage() {
        // Sendung verpasst, da sind einige die nicht in einer "Sendung" enthalten sind
        // URLs nach dem Muster bauen:
        // http://www1.wdr.de/mediathek/video/sendungverpasst/sendung-verpasst-100~_tag-27022016.html
        SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy");
        String tag;
        for (int i = 1; i < 14; ++i) {
            final String URL = "http://www1.wdr.de/mediathek/video/sendungverpasst/sendung-verpasst-100~_tag-";
            tag = formatter.format(new Date().getTime() - (1000 * 60 * 60 * 24 * i));
            String urlString = URL + tag + ".html";
            listeTage.add(urlString);
        }
    }

    private void addToList__() {
        // http://www1.wdr.de/mediathek/video/sendungen/abisz-b100.html
        //Theman suchen
        final String URL = "http://www1.wdr.de/mediathek/video/sendungen-a-z/index.html";
        final String MUSTER_URL = "<a href=\"/mediathek/video/sendungen-a-z/";
        seite_1 = getUrlIo.getUri_Iso(SENDERNAME, URL, seite_1, "");
        int pos1;
        int pos2;
        String url;
        themenSeitenSuchen(URL); // ist die erste Seite: "a"
        pos1 = seite_1.indexOf("<strong>A</strong>");
        while (!MSConfig.getStop() && (pos1 = seite_1.indexOf(MUSTER_URL, pos1)) != -1) {
            pos1 += MUSTER_URL.length();
            if ((pos2 = seite_1.indexOf("\"", pos1)) != -1) {
                url = seite_1.substring(pos1, pos2);
                if (url.equals("index.html")) {
                    continue;
                }
                if (url.equals("")) {
                    MSLog.fehlerMeldung(995122047, "keine URL");
                } else {
                    url = "http://www1.wdr.de/mediathek/video/sendungen-a-z/" + url;
                    themenSeitenSuchen(url);
                }
            }
        }
    }

    private void themenSeitenSuchen(String strUrlFeed) {
        final String MUSTER_URL = "<a href=\"/mediathek/video/sendungen/";
        int pos1 = 0;
        int pos2;
        String url;
        seite_2 = getUrlIo.getUri_Iso(SENDERNAME, strUrlFeed, seite_2, "");
        meldung(strUrlFeed);
        while (!MSConfig.getStop() && (pos1 = seite_2.indexOf(MUSTER_URL, pos1)) != -1) {
            pos1 += MUSTER_URL.length();
            if ((pos2 = seite_2.indexOf("\"", pos1)) != -1) {
                url = seite_2.substring(pos1, pos2).trim();
                if (!url.equals("")) {
                    url = "http://www1.wdr.de/mediathek/video/sendungen/" + url;
                    //weiter gehts
                    String[] add;
                    add = new String[]{url, ""};
                    listeThemen.addUrl(add);
                }
            } else {
                MSLog.fehlerMeldung(375862100, "keine Url" + strUrlFeed);
            }
        }
    }

    private class ThemaLaden implements Runnable {

        MSGetUrl getUrl = new MSGetUrl(wartenSeiteLaden);
        private MSStringBuilder sendungsSeite1 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder sendungsSeite2 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder sendungsSeite3 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder sendungsSeite4 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        MSStringBuilder m3u8Page = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        private final ArrayList<String> liste_1 = new ArrayList<>();
        private final ArrayList<String> liste_2 = new ArrayList<>();

        @Override
        public void run() {
            try {
                meldungAddThread();
                String[] link;
                while (!MSConfig.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    if (null != link[0]) switch (link[0]) {
                        case ROCKPALAST_URL:
                            themenSeiteRockpalast();
                            break;
                        case ROCKPALAST_FESTIVAL:
                            themenSeiteFestival();
                            break;
                        case MAUS:
                            addFilmeMaus();
                            break;
                        case ELEFANT:
                            addFilmeElefant();
                            break;
                        default:
                            sendungsSeitenSuchen1(link[0] /* url */);
                            break;
                    }
                    meldungProgress(link[0]);
                }
                String url;
                while (!MSConfig.getStop() && (url = getListeTage()) != null) {
                    meldungProgress(url);
                    sendungsSeitenSuchen2(url);
                }

            } catch (Exception ex) {
                MSLog.fehlerMeldung(633250489, ex);
            }
            meldungThreadUndFertig();
        }

        private void sendungsSeitenSuchen1(String strUrl) {
            meldung(strUrl);
            // Sendungen auf der Seite
            liste_1.clear();
            liste_1.add(strUrl);
            if (MSConfig.loadLongMax()) {
                // sonst wars das
                sendungsSeite1 = getUrl.getUri_Utf(SENDERNAME, strUrl, sendungsSeite1, "");
                sendungsSeite1.extractList("<ul class=\"pageCounterNavi\">", "</ul>", "<a href=\"/mediathek/video/sendungen/", "\"", "http://www1.wdr.de/mediathek/video/sendungen/", liste_1);
            }
            for (String u : liste_1) {
                if (MSConfig.getStop()) {
                    break;
                }
                sendungsSeitenSuchen2(u);
            }
        }

        private void sendungsSeitenSuchen2(String strUrl) {
            final String MUSTER_URL = "<div class=\"teaser hideTeasertext\">";
            int pos;
            String url;
            String titel;
            String dauer;
            String datum;
            String thema;
            long duration = 0;

            if (strUrl.startsWith("http://www1.wdr.de/mediathek/video/sendungen/lokalzeit/uebersicht-lokalzeiten100_tag")) {
                // brauchts nicht
                return;
            }
            sendungsSeite2 = getUrl.getUri_Utf(SENDERNAME, strUrl, sendungsSeite2, "");
            if (sendungsSeite2.length() == 0) {
                return;
            }
            meldung(strUrl);

            thema = sendungsSeite2.extract("<title>", "<");
            thema = thema.replace("- Sendung - Video - Mediathek - WDR", "").trim();
            if (thema.startsWith("Unser Sendungsarchiv")) {
                thema = "";
            }

            //Lokalzeit, ..
            String u = sendungsSeite2.extract("data-extension=\"{ 'mediaObj': { 'url': '", "'");
            if (!u.isEmpty()) {
                sendungsSeitenSuchenNeu(strUrl, sendungsSeite2, thema);
            }

            pos = 0;
            while (!MSConfig.getStop() && (pos = sendungsSeite2.indexOf(MUSTER_URL, pos)) != -1) {
                pos += MUSTER_URL.length();
                url = sendungsSeite2.extract("<a href=\"/mediathek/video/sendungen/", "\"", pos);
                if (!url.equals("")) {
                    url = "http://www1.wdr.de/mediathek/video/sendungen/" + url;

                    titel = sendungsSeite2.extract("<span class=\"hidden\">Video:</span>", "<", pos).trim();
                    titel = titel.replace("\n", "");

                    datum = sendungsSeite2.extract("<p class=\"programInfo\">", "|", pos).trim();
                    dauer = sendungsSeite2.extract("<span class=\"hidden\">L&auml;nge: </span>", "<", pos).trim();
                    try {
                        if (!dauer.equals("")) {
                            String[] parts = dauer.split(":");
                            duration = 0;
                            long power = 1;
                            for (int i = parts.length - 1; i >= 0; i--) {
                                duration += Long.parseLong(parts[i]) * power;
                                power *= 60;
                            }
                        }
                    } catch (Exception ex) {
                        MSLog.fehlerMeldung(306597519, ex, strUrl);
                    }

                    //weiter gehts
                    addFilm1(thema, titel, url, duration, datum);
                } else {
                    MSLog.fehlerMeldung(646432970, "keine Url" + strUrl);
                }
            }
        }

        private void sendungsSeitenSuchenNeu(String strUrl, MSStringBuilder seite, String thema) {
            //Lokalzeit, ..
            String u = seite.extract("data-extension=\"{ 'mediaObj': { 'url': '", "'");
            if (!u.isEmpty()) {
                addFilm2(strUrl, thema, "", u, 0, "", "");
            }

            liste_2.clear();
            seite.extractList("Letzte Sendungen", "Neuer Abschnitt", "<a href=\"", "\"", "http://www1.wdr.de", liste_2);
            for (String ur : liste_2) {
                if (MSConfig.getStop()) {
                    break;
                }

                seite = getUrl.getUri_Utf(SENDERNAME, ur, seite, "");
                if (seite.length() == 0) {
                    continue;
                }
                meldung(strUrl);

                thema = seite.extract("<title>", "<");
                thema = thema.replace("- Sendung - Video - Mediathek - WDR", "").trim();
                if (thema.startsWith("Unser Sendungsarchiv")) {
                    thema = "";
                }

                u = seite.extract("data-extension=\"{ 'mediaObj': { 'url': '", "'");
                if (!u.isEmpty()) {
                    addFilm2(strUrl, thema, "", u, 0, "", "");
                }
            }
        }

        private void addFilm1(String thema, String titel, String filmWebsite, long dauer, String datum) {
            meldung(filmWebsite);
            sendungsSeite3 = getUrl.getUri_Utf(SENDERNAME, filmWebsite, sendungsSeite3, "");
            if (sendungsSeite3.length() == 0) {
                return;
            }
            if (sendungsSeite3.length() == 0) {
                MSLog.fehlerMeldung(751236547, new String[]{"leere Seite: " + filmWebsite});
            }
            String description = sendungsSeite3.extract("<p class=\"text\">", "<");
            if (thema.isEmpty()) {
                thema = sendungsSeite3.extract("{ 'offset': '0' }}\" title=\"", "\"");
                thema = thema.replace(", WDR", "");
                if (thema.contains(":")) {
                    thema = thema.substring(0, thema.indexOf(":"));
                }
                if (thema.contains(" -")) {
                    thema = thema.substring(0, thema.indexOf(" -"));
                }
            }
            // URL suchen
            String url = sendungsSeite3.extract("mediaObj': { 'url': '", "'");
            if (!url.equals("")) {
                addFilm2(filmWebsite, thema, titel, url, dauer, datum, description);
            } else {
                MSLog.fehlerMeldung(763299001, new String[]{"keine Url: " + filmWebsite});
            }

        }

        private void addFilm2(String filmWebsite, String thema, String titel, String urlFilmSuchen, long dauer, String datum, String beschreibung) {
            final String INDEX_0 = "index_0_av.m3u8"; //kleiner
            final String INDEX_1 = "index_1_av.m3u8"; //klein
            final String INDEX_2 = "index_2_av.m3u8"; //hohe Auflösung
            meldung(urlFilmSuchen);
            sendungsSeite4 = getUrl.getUri_Utf(SENDERNAME, urlFilmSuchen, sendungsSeite4, "");
            if (sendungsSeite4.length() == 0) {
                return;
            }
            String urlNorm, urlHd = "", urlKlein = "";
            String zeit = "";

            // URL suchen
            urlNorm = sendungsSeite4.extract("\"alt\":{\"videoURL\":\"", "\"");
            String f4m = sendungsSeite4.extract("\"dflt\":{\"videoURL\":\"", "\"");

            if (urlNorm.endsWith(".m3u8")) {
                final String urlM3 = urlNorm;
                m3u8Page = getUrl.getUri_Utf(SENDERNAME, urlNorm, m3u8Page, "");
                if (m3u8Page.indexOf(INDEX_2) != -1) {
                    urlNorm = getUrlFromM3u8(urlM3, INDEX_2);
                } else if (m3u8Page.indexOf(INDEX_1) != -1) {
                    urlNorm = getUrlFromM3u8(urlM3, INDEX_1);
                }
                if (m3u8Page.indexOf(INDEX_0) != -1) {
                    urlKlein = getUrlFromM3u8(urlM3, INDEX_0);
                } else if (m3u8Page.indexOf(INDEX_1) != -1) {
                    urlKlein = getUrlFromM3u8(urlM3, INDEX_1);
                }

                if (urlNorm.isEmpty() && !urlKlein.isEmpty()) {
                    urlNorm = urlKlein;
                }
                if (urlNorm.equals(urlKlein)) {
                    urlKlein = "";
                }
            }

            if (!f4m.isEmpty() && urlNorm.contains("_") && urlNorm.endsWith(".mp4")) {
                // http://adaptiv.wdr.de/z/medp/ww/fsk0/104/1048369/,1048369_11885064,1048369_11885062,1048369_11885066,.mp4.csmil/manifest.f4m
                // http://ondemand-ww.wdr.de/medp/fsk0/104/1048369/1048369_11885062.mp4
                String s1 = urlNorm.substring(urlNorm.lastIndexOf("_") + 1, urlNorm.indexOf(".mp4"));
                String s2 = urlNorm.substring(0, urlNorm.lastIndexOf("_") + 1);
                try {
                    int nr = Integer.parseInt(s1);
                    if (f4m.contains(nr + 2 + "")) {
                        urlHd = s2 + (nr + 2) + ".mp4";
                    }
                    if (f4m.contains(nr + 4 + "")) {
                        urlKlein = s2 + (nr + 4) + ".mp4";
                    }
                } catch (Exception ignore) {
                }
                if (!urlHd.isEmpty()) {
                    if (urlKlein.isEmpty()) {
                        urlKlein = urlNorm;
                    }
                    urlNorm = urlHd;
                }
            }

            if (titel.isEmpty()) {
                titel = sendungsSeite4.extract("\"trackerClipTitle\":\"", "\"");
            }

            String subtitle = sendungsSeite4.extract("\"captionURL\":\"", "\"");

            if (datum.isEmpty()) {
                String d = sendungsSeite4.extract("\"trackerClipAirTime\":\"", "\"");
                if (d.contains(" ")) {
                    zeit = d.substring(d.indexOf(" ")) + ":00";
                    datum = d.substring(0, d.indexOf(" "));
                }
            } else {
                String d = sendungsSeite4.extract("\"trackerClipAirTime\":\"", "\"");
                if (d.contains(" ")) {
                    zeit = d.substring(d.indexOf(" ")) + ":00";
                } else {
                    System.out.println("Zeit");
                }
            }

            if (!urlNorm.isEmpty()) {

                DatenFilm film = new DatenFilm(SENDERNAME, thema, filmWebsite, titel, urlNorm, ""/*rtmpURL*/, datum, zeit,
                        dauer, beschreibung);
                if (!subtitle.isEmpty()) {
                    film.addUrlSubtitle(subtitle);
                }
                if (!urlKlein.isEmpty()) {
                    film.addUrlKlein(urlKlein, "");
                }
                addFilm(film);
            } else {
                MSLog.fehlerMeldung(978451239, new String[]{"keine Url: " + urlFilmSuchen, "UrlThema: " + filmWebsite});
            }
        }

        private String getUrlFromM3u8(String m3u8Url, String qualityIndex) {
            final String CSMIL = "csmil/";
            String url = m3u8Url.substring(0, m3u8Url.indexOf(CSMIL)) + CSMIL + qualityIndex;
            return url;
        }

        private void themenSeiteRockpalast() {
            try {
                for (String s : listeRochpalast) {
                    meldungProgress(s);
                    if (MSConfig.getStop()) {
                        break;
                    }
                    if (s.endsWith("/fernsehen/kultur/rockpalast/videos/index.html")) {
                        continue;
                    }
                    if (s.contains("/fernsehen/kultur/rockpalast/videos/uebersicht_Konzerte10")) {
                        continue;
                    }
                    // Konzerte suchen
                    addFilmeRockpalast(s, "Rockpalast");
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(696963025, ex);
            }
        }

        private void themenSeiteFestival() {
            try {
                for (String s : listeFestival) {
                    meldungProgress(s);
                    if (MSConfig.getStop()) {
                        break;
                    }
                    if (s.endsWith("/fernsehen/kultur/rockpalast/videos/index.html")) {
                        continue;
                    }
                    if (s.contains("/fernsehen/kultur/rockpalast/videos/uebersicht_Festivals116.html")) {
                        continue;
                    }
                    if (s.endsWith("/uebersicht_Festival100.html")) {
                        continue;
                    }
                    if (s.endsWith("/uebersicht_Festivals100.html")) {
                        continue;
                    }
                    // Konzerte suchen
                    addFilmeRockpalast(s, "Rockpalast - Festival");
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(915263698, ex);
            }
        }

        private void addFilmeMaus() {
            try {
                for (String filmWebsite : listeMaus) {
                    meldungProgress(filmWebsite);
                    if (MSConfig.getStop()) {
                        break;
                    }
                    sendungsSeite1 = getUrl.getUri_Utf(SENDERNAME, filmWebsite, sendungsSeite1, "");
                    String url;
                    String description;

                    String titel = sendungsSeite1.extract("<title>", "<"); //<title>Achterbahn - MausSpots - Lachgeschichten - Die Seite mit der Maus - WDR Fernsehen</title>
                    titel = titel.replace("\n", "");
                    if (titel.contains("-")) {
                        titel = titel.substring(0, titel.indexOf("-")).trim();
                    }
                    description = sendungsSeite1.extract("<div class=\"videotext\">", "<"); // hat nur ein Filme??
                    String datum = sendungsSeite1.extract("<div class=\"sendedatum\"><p>Sendedatum: ", "<").trim();

                    //                  http://http-ras.wdr.de/CMS2010/mdb/ondemand/weltweit/fsk0/22/222944/222944_6188265.mp4
                    // rtmp://gffstream.fcod.llnwd.net/a792/e2/CMS2010/mdb/ondemand/weltweit/fsk0/22/222944/222944_6188265.mp4
                    url = sendungsSeite1.extract("firstVideo=rtmp://", ".mp4");
                    if (url.isEmpty()) {
                        MSLog.fehlerMeldung(730215698, "keine URL: " + filmWebsite);
                    } else {
                        url = "http://http-ras.wdr.de/CMS2010/mdb" + url.substring(url.indexOf("/ondemand/")) + ".mp4";
                        DatenFilm film = new DatenFilm(SENDERNAME, "MausSpots", filmWebsite, titel, url, ""/*rtmpURL*/, datum, ""/* zeit */,
                                0, description);
                        addFilm(film);
                    }
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(915263698, ex);
            }
        }

        private void addFilmeElefant() {
            try {
                for (String filmWebsite : listeElefant) {
                    meldungProgress(filmWebsite);
                    if (MSConfig.getStop()) {
                        break;
                    }
                    sendungsSeite1 = getUrl.getUri_Utf(SENDERNAME, filmWebsite, sendungsSeite1, "");
                    String description;
                    long duration = 0;

                    String titel = sendungsSeite1.extract("<title><![CDATA[", "]");
                    description = sendungsSeite1.extract("<text><![CDATA[", "]");
                    String datum = sendungsSeite1.extract("<sendedatum><![CDATA[", "]"); //  <sendedatum><![CDATA[2014-07-30 00:00:00]]></sendedatum>
                    if (datum.isEmpty()) {
                        datum = sendungsSeite1.extract("<pubstart><![CDATA[", "]");
                    }

                    try {
                        final SimpleDateFormat sdfIn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        final SimpleDateFormat sdfOut = new SimpleDateFormat("dd.MM.yyyy");
                        datum = sdfOut.format(sdfIn.parse(datum));
                    } catch (Exception ex) {
                        datum = "";
                        MSLog.fehlerMeldung(945214787, "kein Datum");
                    }

                    String d = sendungsSeite1.extract("<duration><![CDATA[", "]");
                    try {
                        if (!d.equals("")) {
                            duration = Long.parseLong(d);
                        }
                    } catch (Exception ex) {
                        MSLog.fehlerMeldung(732659874, ex, "duration: " + d);
                    }

                    String url = sendungsSeite1.extract("<file_xl><![CDATA[", "]");
                    String urlKlein = sendungsSeite1.extract("<file><![CDATA[", "]");
//                    if (url.isEmpty() || datum.isEmpty() || titel.isEmpty() || description.isEmpty()) {
//                        System.out.println("Test");
//                    }
                    if (url.isEmpty()) {
                        url = urlKlein;
                        urlKlein = "";
                    }
                    if (url.isEmpty()) {
                        MSLog.fehlerMeldung(632012541, "keine URL: " + filmWebsite);
                    } else {
                        url = "http://http-ras.wdr.de/mediendb/elefant_online" + url;
                        DatenFilm film = new DatenFilm(SENDERNAME, "Elefantenkino", "http://www.wdrmaus.de/elefantenseite/", titel, url, ""/*rtmpURL*/, datum, ""/* zeit */,
                                duration, description);
                        if (!urlKlein.isEmpty()) {
                            urlKlein = "http://http-ras.wdr.de/mediendb/elefant_online/" + urlKlein;
                            film.addUrlKlein(urlKlein, "");
                        }
                        addFilm(film);
                    }
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(747586936, ex);
            }
        }

        private void addFilmeRockpalast(String filmWebsite, String thema) {
            meldung(filmWebsite);
            sendungsSeite1 = getUrl.getUri_Utf(SENDERNAME, filmWebsite, sendungsSeite1, "");
            String url;
            long duration = 0;
            String description;

            String titel = sendungsSeite1.extract("headline: \"", "\"");
            titel = titel.replace("\n", "");

            String d = sendungsSeite1.extract("length: \"(", ")");
            try {
                if (!d.equals("") && d.length() <= 8 && d.contains(":")) {
                    String[] parts = d.split(":");
                    long power = 1;
                    for (int i = parts.length - 1; i >= 0; i--) {
                        duration += Long.parseLong(parts[i]) * power;
                        power *= 60;
                    }
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(915263625, ex, "duration: " + d);
            }

            description = sendungsSeite1.extract("<meta name=\"Description\" content=\"", "\"");

            String datum = sendungsSeite1.extract("Konzert vom", "\"").trim();
            if (datum.isEmpty()) {
                datum = sendungsSeite1.extract("Sendung vom ", "\"").trim();
            }
            url = sendungsSeite1.extract("<a href=\"/fernsehen/kultur/rockpalast/videos/av/", "\"");
            if (url.isEmpty()) {
                MSLog.fehlerMeldung(915236547, "keine URL: " + filmWebsite);
            } else {
                url = "http://www1.wdr.de/fernsehen/kultur/rockpalast/videos/av/" + url;
                addFilm2(filmWebsite, thema, titel, url, duration, datum, description);
            }
        }
    }

    private synchronized String getListeTage() {
        return listeTage.pollFirst();
    }

}
