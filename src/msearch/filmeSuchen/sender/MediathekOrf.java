/*
 * MediathekView
 * Copyright (C) 2008 W. Xaver
 * W.Xaver[at]googlemail.com
 *
 * thausherr
 *
 *
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
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import msearch.daten.DatenFilm;
import msearch.daten.MSearchConfig;
import msearch.filmeSuchen.MSearchFilmeSuchen;
import msearch.io.MSearchGetUrl;
import msearch.tool.MSearchConst;
import msearch.tool.MSearchLog;
import msearch.tool.MSearchStringBuilder;

/**
 *
 * @author
 */
public class MediathekOrf extends MediathekReader implements Runnable {

    public static final String SENDER = "ORF";

    /**
     *
     * @param ddaten
     */
    public MediathekOrf(MSearchFilmeSuchen ssearch, int startPrio) {
        super(ssearch, /* name */ SENDER, /* threads */ 2, /* urlWarten */ 500, startPrio);
    }

    @Override
    void addToList() {
        MSearchStringBuilder seite = new MSearchStringBuilder(MSearchConst.STRING_BUFFER_START_BUFFER);
        listeThemen.clear();
        meldungStart();
//        if (!MSearchConfig.senderAllesLaden) {
//            for (int i = 0; i < 5; ++i) {
//                // 4 Tage zurück
//                String vorTagen = getGestern(i).toLowerCase();
//                bearbeiteAdresseTag("http://tvthek.orf.at/schedule/last/" + vorTagen, seite);
//            }
//        } else {
//            bearbeiteAdresse(TOPICURL, seite);
//            bearbeiteAdresseTag("http://tvthek.orf.at/schedule/last/monday", seite);
//            bearbeiteAdresseTag("http://tvthek.orf.at/schedule/last/tuesday", seite);
//            bearbeiteAdresseTag("http://tvthek.orf.at/schedule/last/wednesday", seite);
//            bearbeiteAdresseTag("http://tvthek.orf.at/schedule/last/thursday", seite);
//            bearbeiteAdresseTag("http://tvthek.orf.at/schedule/last/friday", seite);
//            bearbeiteAdresseTag("http://tvthek.orf.at/schedule/last/saturday", seite);
//            bearbeiteAdresseTag("http://tvthek.orf.at/schedule/last/sunday", seite);
//            bearbeiteAdresseTag("http://tvthek.orf.at/schedule/last/archiv", seite);
//            bearbeiteAdresseTag("http://tvthek.orf.at/schedule/last/monday_prev", seite);
//            bearbeiteAdresseTag("http://tvthek.orf.at/schedule/last/tuesday_prev", seite);
//            bearbeiteAdresseTag("http://tvthek.orf.at/schedule/last/wednesday_prev", seite);
//            bearbeiteAdresseTag("http://tvthek.orf.at/schedule/last/thursday_prev", seite);
//            bearbeiteAdresseTag("http://tvthek.orf.at/schedule/last/friday_prev", seite);
//            bearbeiteAdresseTag("http://tvthek.orf.at/schedule/last/saturday_prev", seite);
//            bearbeiteAdresseTag("http://tvthek.orf.at/schedule/last/sunday_prev", seite);
//        }
        bearbeiteAdresseKey(seite);
        if (MSearchConfig.getStop()) {
            meldungThreadUndFertig();
        } else if (listeThemen.size() == 0) {
            meldungThreadUndFertig();
        } else {
            meldungAddMax(listeThemen.size());
            listeSort(listeThemen, 1);
            for (int t = 0; t < maxThreadLaufen; ++t) {
                //new Thread(new ThemaLaden()).start();
                Thread th = new Thread(new ThemaLaden());
                th.setName(nameSenderMReader + t);
                th.start();
            }
        }
    }

//    /**
//     * @param adresse Starter-URL von dem aus Sendungen gefunden werden
//     */
//    private void bearbeiteAdresse(String adresse, MSearchStringBuilder seite) {
//        //System.out.println("bearbeiteAdresse: " + adresse);
//        final String MUSTER_URL1 = "<a href=\""; //TH
//        final String MUSTER_URL2 = "/programs/";
//        final String MUSTER_URL2b = "/topics/"; //TH
//        seite = getUrlIo.getUri(nameSenderMReader, adresse, MSearchConst.KODIERUNG_UTF, 3, seite, "");
//        int pos = 0;
//        int pos1;
//        int pos2;
//        String url = "";
//        String thema = "";
//        //Podcasts auslesen
//        while ((pos = seite.indexOf(MUSTER_URL1, pos)) != -1) {
//            try {
//                pos += MUSTER_URL1.length();
//                //TH
//                String m = MUSTER_URL2;
//                int p = seite.indexOf(m, pos);
//                if (p == -1) {
//                    // Plan B
//                    m = MUSTER_URL2b;
//                    p = seite.indexOf(m, pos);
//                }
//                pos = p;
//                //TH ende
//                if (pos != -1) {
//                    pos += m.length(); //TH
//                    pos1 = pos;
//                    pos2 = seite.indexOf("\"", pos);
//                    if (pos1 != -1 && pos2 != -1) {
//                        url = seite.substring(pos1, pos2);
//                    }
//                    //TH neu: " title="ZIB 24: Spott aus Litauen">
//                    pos1 = seite.indexOf("title=\"", pos) + 6; //TH
//                    pos2 = seite.indexOf("\">", pos); //TH
//                    if (pos1 != -1 && pos2 != -1 && pos1 < pos2) {
//                        thema = seite.substring(pos1 + 1, pos2);
//                        //TH
//                        if (thema.endsWith(" aufrufen...")) {
//                            thema = thema.replace(" aufrufen...", "");
//                        }
//                        //TH 31.7.2012
//                        if (thema.endsWith(" ansehen...")) {
//                            thema = thema.replace(" ansehen...", "");
//                        }
//                    }
//                    if (url.equals("")) {
//                        continue;
//                    }
//                    String[] add = new String[]{
//                        ROOTURL + m + url, thema //TH
//                    };
//                    if (!istInListe(listeThemen, add[0], 0)) {
//                        //System.out.println ("URL: " + add[0] + ", Thema: " + add[1]);
//                        listeThemen.add(add);
//                    }
//                } else {
//                    break; //TH muss sein da muster 2 manchmal nicht fündig - dann Endlosschleife
//                }
//            } catch (Exception ex) {
//                MSearchLog.fehlerMeldung(-896234580, MSearchLog.FEHLER_ART_MREADER, "MediathekOrf.addToList", ex);
//            }
//        }
//
//        //TH 31.7.2012 Rekursive Sonderrunde für weitere topics ("alle Anzeigen")
//        if (adresse.equals(TOPICURL)) {
//            final String MUSTERURL_MORE = "<a class=\"more\" href=\"";
//            pos = 0;
//            MSearchStringBuilder s2 = new MSearchStringBuilder(MSearchConst.STRING_BUFFER_START_BUFFER); // zum Reduzieren der MVStringBuilder
//            while ((pos = seite.indexOf(MUSTERURL_MORE, pos)) != -1) {
//                try {
//                    pos += MUSTERURL_MORE.length();
//                    pos1 = pos;
//                    pos2 = seite.indexOf("\"", pos);
//                    if (pos1 != -1 && pos2 != -1) {
//                        url = ROOTURL + seite.substring(pos1, pos2);
//                        if (!url.equals(adresse)) {
//                            bearbeiteAdresse(url, s2);
//                        }
//                    }
//                } catch (Exception ex) {
//                    MSearchLog.fehlerMeldung(-468320478, MSearchLog.FEHLER_ART_MREADER, "MediathekOrf.addToList", ex);
//                }
//            }
//        }
//    }
//    private void bearbeiteAdresseTag(String adresse, MSearchStringBuilder seite) {
//        //<td class="episode"><div><a href="/programs/4660163-heute-oesterreich/episodes/7103017-heute-oesterreich" title="heute österreich ansehen...">
//        final String MUSTER_URL = "<td class=\"episode\"><div><a href=\"";
//        seite = getUrlIo.getUri(nameSenderMReader, adresse, MSearchConst.KODIERUNG_UTF, 3, seite, "");
//        int pos = 0;
//        String url, thema;
//        while ((pos = seite.indexOf(MUSTER_URL, pos)) != -1) {
//            try {
//                pos += MUSTER_URL.length();
//                url = seite.extract("/programs/", "\"", pos);
//                thema = seite.extract("title=\"", "\"", pos);
//                if (thema.endsWith(" aufrufen...")) {
//                    thema = thema.replace(" aufrufen...", "").trim();
//                }
//                if (thema.endsWith(" ansehen...")) {
//                    thema = thema.replace(" ansehen...", "").trim();
//                }
//                if (url.equals("")) {
//                    continue;
//                }
//                String[] add = new String[]{"http://tvthek.orf.at/programs/" + url, thema};
//                if (!istInListe(listeThemen, add[0], 0)) {
//                    listeThemen.add(add);
//                }
//            } catch (Exception ex) {
//                MSearchLog.fehlerMeldung(-896234580, MSearchLog.FEHLER_ART_MREADER, "MediathekOrf.addToList", ex);
//            }
//        }
//    }
    private void bearbeiteAdresseKey(MSearchStringBuilder seite) {
        // <a class="base_list_item_inner" href="http://tvthek.orf.at/programs/letter/R">
        seite = getUrlIo.getUri(nameSenderMReader, "http://tvthek.orf.at/programs", MSearchConst.KODIERUNG_UTF, 3, seite, "");
        ArrayList<String> al = new ArrayList<>();
        try {
            seite.extractList("href=\"http://tvthek.orf.at/programs/letter/", "\"", 0, "http://tvthek.orf.at/programs/letter/", al);
            for (String s : al) {
                String[] add = new String[]{s, ""};
                if (!istInListe(listeThemen, add[0], 0)) {
                    listeThemen.add(add);
                }
            }
        } catch (Exception ex) {
            MSearchLog.fehlerMeldung(-826341789, MSearchLog.FEHLER_ART_MREADER, "MediathekOrf.bearbeiteAdresseKey", ex);
        }
    }

    private class ThemaLaden implements Runnable {

        MSearchGetUrl getUrl = new MSearchGetUrl(wartenSeiteLaden);
        private MSearchStringBuilder seite1 = new MSearchStringBuilder(MSearchConst.STRING_BUFFER_START_BUFFER);
        private MSearchStringBuilder seite2 = new MSearchStringBuilder(MSearchConst.STRING_BUFFER_START_BUFFER);
        private ArrayList<String> al = new ArrayList<>();

        @Override
        public synchronized void run() {
            try {
                meldungAddThread();
                String[] link;
                while (!MSearchConfig.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    try {
                        meldungProgress(link[0]);
                        sendungen(link[0] /* url */);
                    } catch (Exception ex) {
                        MSearchLog.fehlerMeldung(-795633581, MSearchLog.FEHLER_ART_MREADER, "MediathekOrf.OrfThemaLaden.run", ex);
                    }
                }
            } catch (Exception ex) {
                MSearchLog.fehlerMeldung(-554012398, MSearchLog.FEHLER_ART_MREADER, "MediathekOrf.OrfThemaLaden.run", ex);
            }
            meldungThreadUndFertig();
        }

        private void sendungen(String url) {
            // <a class="base_list_item_inner" href="http://tvthek.orf.at/programs/letter/R">
            // <a href="http://tvthek.orf.at/program/ZIB-13/71280/ZIB-13/7151714" class="base_list_item_inner icon_align">
            seite1 = getUrlIo.getUri(nameSenderMReader, url, MSearchConst.KODIERUNG_UTF, 2, seite1, "");
            int start = seite1.indexOf("<h3 class=\"subheadline\">Verfügbare Sendungen</h3>");
            int stop;
            int pos, pos1, pos2, count = 0;
            String urlFeed;
            if (start > 0) {
                pos = start;
                while ((pos = seite1.indexOf("<h4 class=\"base_list_item_headline\">", pos)) != -1) {
                    // über alle Sendungen eines Buchstabens
                    count = 0;
                    pos += "<h4 class=\"base_list_item_headline\">".length();
                    stop = seite1.indexOf("<!-- ende latest:", pos);
                    pos1 = pos;
                    while ((pos1 = seite1.indexOf("<a href=\"http://tvthek.orf.at/program/", pos1)) != -1) {
                        // über die eine jeweilige Sendung
                        if (!MSearchConfig.senderAllesLaden) {
                            if (count > 3) {
                                break;
                            }
                        }
                        if (pos1 >= stop) {
                            break;
                        }
                        ++count;
                        pos1 += "<a href=\"http://tvthek.orf.at/program/".length();
                        if ((pos2 = seite1.indexOf("\"", pos1)) != -1) {
                            urlFeed = seite1.substring(pos1, pos2);
                            if (urlFeed.isEmpty()) {
                                MSearchLog.fehlerMeldung(-915263654, MSearchLog.FEHLER_ART_MREADER, "MediathekOrf.sendungen", "keine Url: " + url);
                            } else {
                                feedEinerSeiteSuchen("http://tvthek.orf.at/program/" + urlFeed);
                            }
                        }

                    }
                }
            }
        }

//        private void sendungen(String url) {
//            // <a class="base_list_item_inner" href="http://tvthek.orf.at/programs/letter/R">
//            // <a href="http://tvthek.orf.at/program/ZIB-11/71276/ZIB-11/7140823" class="base_list_item_inner icon_align">  
//            seite1 = getUrlIo.getUri(nameSenderMReader, url, MSearchConst.KODIERUNG_UTF, 2, seite1, "");
//            seite1.extractList("<a href=\"http://tvthek.orf.at/program/", "\"", 0, "http://tvthek.orf.at/program/", al);
//            if (al.isEmpty()) {
//                MSearchLog.fehlerMeldung(-915263654, MSearchLog.FEHLER_ART_MREADER, "MediathekOrf.sendungen", "keine Url: " + url);
//            } else {
//                for (String s : al) {
//                    feedEinerSeiteSuchen(s);
//                }
//            }
//        }
        private void feedEinerSeiteSuchen(String strUrlFeed) {
            //<title> ORF TVthek: a.viso - 28.11.2010 09:05 Uhr</title>
            seite2 = getUrl.getUri_Utf(nameSenderMReader, strUrlFeed, seite2, "");
            String datum = "";
            String zeit = "";
            long duration = 0;
            String description = "";
            String thumbnail = "";
            String tmp;
            String urlRtmpKlein = "", urlRtmp = "", url, urlKlein;
            String titel = "", thema = "";
            meldung(strUrlFeed);
            thumbnail = seite2.extract("<meta property=\"og:image\" content=\"", "\"");
            thumbnail = thumbnail.replace("&amp;", "&");
            thema = seite2.extract("<title>", "vom"); //<title>ABC Bär vom 17.11.2013 um 07.35 Uhr / ORF TVthek</title>
            if (thema.isEmpty()) {
                thema = nameSenderMReader;
            }
            datum = seite2.extract("<span class=\"meta meta_date\">", "<");
            if (datum.contains(",")) {
                datum = datum.substring(datum.indexOf(",") + 1).trim();
            }
            zeit = seite2.extract("<span class=\"meta meta_time\">", "<");
            zeit = zeit.replace("Uhr", "").trim();
            if (zeit.length() == 5) {
                zeit = zeit.replace(".", ":") + ":00";
            }
            description = seite2.extract("<div class=\"details_description\">", "<");
            tmp = seite2.extract("\"duration\":\"", "\"");
            try {
                duration = Long.parseLong(tmp) / 1000; // time in milliseconds
            } catch (Exception ex) {
            }
            url = seite2.extract("quality\":\"Q6A\",\"quality_string\":\"hoch\",\"src\":\"rtmp", "\"");
            url = url.replace("\\/", "/");
            if (!url.isEmpty()) {
                url = "rtmp" + url;
                int mpos = url.indexOf("mp4:");
                if (mpos != -1) {
                    urlRtmp = "-r " + url + " -y " + url.substring(mpos) + " --flashVer WIN11,4,402,265 --swfUrl http://tvthek.orf.at/flash/player/TVThekPlayer_9_ver18_1.swf";
                }
            }
            urlKlein = seite2.extract("quality\":\"Q4A\",\"quality_string\":\"mittel\",\"src\":\"rtmp", "\"");
            urlKlein = urlKlein.replace("\\/", "/");
            if (!urlKlein.isEmpty()) {
                urlKlein = "rtmp" + urlKlein;
                int mpos = urlKlein.indexOf("mp4:");
                if (mpos != -1) {
                    urlRtmpKlein = "-r " + urlKlein + " -y " + urlKlein.substring(mpos) + " --flashVer WIN11,4,402,265 --swfUrl http://tvthek.orf.at/flash/player/TVThekPlayer_9_ver18_1.swf";
                }
            }
            //rtmp://apasfw.apa.at/cms-worldwide/mp4:2012-09-09_1305_tl_23_UNGARISCHES-MAGAZIN_Beszelgetes-Szabo-Er__4582591__o__0000214447__s4588253___n__BHiRes_13241400P_13280400P_Q6A.mp4
            //flvr=WIN11,4,402,265
            //app=cms-worldwide/
            //swfUrl=http://tvthek.orf.at/flash/player/TVThekPlayer_9_ver18_1.swf
            //tcUrl=rtmp://apasfw.apa.at/%app%
            //play=mp4:1950-01-01_1200_in_00_Ungarnkrise-1956_____3230831__o__0000936285__s3230833___Q6A.mp4
            //flvstreamer --resume --rtmp %tcUrl% --flashVer %flvr% --app %app% --swfUrl %swfUrl% --playpath %play% --flv %Ziel%

            //public DatenFilm(String ssender, String tthema, String filmWebsite, String ttitel, String uurl, String uurlRtmp,
            //        String datum, String zeit,
            //        long dauerSekunden, String description, String imageUrl, String[] keywords) {

            if (!url.isEmpty()) {
                DatenFilm film = new DatenFilm(nameSenderMReader, thema, strUrlFeed, titel, url, urlRtmp, datum, zeit, duration, description,
                        thumbnail, new String[]{});
                if (!urlKlein.isEmpty()) {
                    film.addUrlKlein(urlKlein, urlRtmpKlein);
                }
                addFilm(film);
            } else {
                MSearchLog.fehlerMeldung(-102365478, MSearchLog.FEHLER_ART_MREADER, "MediathekOrf.feedEinerSeiteSuchen", "keine Url: " + strUrlFeed);
            }


//            //TH ggf. Trennen in Thema und Titel
//            int dp = thema.indexOf(": ");
//            if (dp != -1) {
//                thema = thema.substring(0, dp);
//            }//TH titel und thema getrennt
//
//            //TH 27.8.2012 JS XML Variable auswerten
//            final String MUSTER_FLASH = "ORF.flashXML = '";
//            if ((pos = seite2.indexOf(MUSTER_FLASH)) != -1) {
//                if ((pos2 = seite2.indexOf("'", pos + MUSTER_FLASH.length())) != -1) {
//                    String xml = seite2.substring(pos + MUSTER_FLASH.length(), pos2);
//                    try {
//                        xml = URLDecoder.decode(xml, "UTF-8");
//                        DocumentBuilder docBuilder = null;
//                        docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//                        Document doc = docBuilder.parse(new InputSource(new StringReader(xml)));
//                        Node rootNode = doc.getDocumentElement();
//                        String alternateDescription = extractAlternateDescriptionText(rootNode);
//                        NodeList nodeList = rootNode.getChildNodes();
//                        for (int i = 0; i < nodeList.getLength(); ++i) {
//                            Node Item = nodeList.item(i);
//                            if ("Playlist".equals(Item.getNodeName())) {
//                                NodeList childNodeList = Item.getChildNodes();
//                                for (int j = 0; j < childNodeList.getLength(); ++j) {
//                                    Node childItem = childNodeList.item(j);
//                                    if ("Items".equals(childItem.getNodeName())) {
//                                        NodeList childNodeList2 = childItem.getChildNodes();
//                                        for (int k = 0; k < childNodeList2.getLength(); ++k) {
//                                            Node childItem2 = childNodeList2.item(k);
//                                            if ("Item".equals(childItem2.getNodeName())) {
//                                                String url = "", url_klein = "";
//                                                String titel = "";
//                                                duration = 0;
//                                                description = "";
//                                                descriptionParsed = false;
//                                                NodeList childNodeList3 = childItem2.getChildNodes();
//                                                for (int l = 0; l < childNodeList3.getLength(); ++l) {
//                                                    Node childItem3 = childNodeList3.item(l);
//                                                    if ("Title".equals(childItem3.getNodeName())) {
//                                                        titel = childItem3.getTextContent();
//                                                    }
//                                                    if ("VideoUrl".equals(childItem3.getNodeName())) {
//                                                        String quality = null;
//                                                        NamedNodeMap namedNodeMap = childItem3.getAttributes();
//                                                        if (namedNodeMap != null) {
//                                                            Node node = namedNodeMap.getNamedItem("quality");
//                                                            if (node != null) {
//                                                                quality = node.getNodeValue();
//                                                            }
//                                                        }
//                                                        // "Q1A"-Qualität nehmen
//                                                        if ("Q1A".equals(quality)) {
//                                                            url_klein = childItem3.getTextContent();
//                                                        }
//                                                        // "SMIL"-Qualität nehmen, oder "keine Qualität"
//                                                        if ((quality == null && titel.isEmpty()) || "SMIL".equals(quality)) {
//                                                            url = childItem3.getTextContent();
//                                                        }
//                                                    }
//                                                    if (("Duration").equals(childItem3.getNodeName())) {
//                                                        String d = childItem3.getTextContent();
//                                                        try {
//                                                            if (!d.equals("")) {
//                                                                duration = Long.parseLong(d) / 1000; // time in milliseconds
//                                                            }
//                                                        } catch (Exception ex) {
//                                                            MSearchLog.fehlerMeldung(-918593079, MSearchLog.FEHLER_ART_MREADER, "MediathekOrf.feedEinerSeiteSuchen", ex);
//                                                        }
//                                                    }
//
//                                                    if ("Description".equals(childItem3.getNodeName())) {
//                                                        description = childItem3.getTextContent();
//                                                        description = StringEscapeUtils.unescapeJava(description).trim();
//
//                                                        // Some items do not contain a description (the Description tag is
//                                                        // empty but their normally contain a description in the Text tag in
//                                                        // embeded inside the AdditionalInfo tag in the root element.
//                                                        if (description.length() == 0) {
//                                                            description = alternateDescription;
//                                                        }
//                                                    }
//                                                }
//                                                if (!url.isEmpty() && !titel.isEmpty()) {
//                                                    String urlRtmp = "", urlRtmp_klein = "";
//                                                    int mpos = url.indexOf("mp4:");
//                                                    if (mpos != -1) {
//                                                        urlRtmp = "-r " + url + " -y " + url.substring(mpos) + " --flashVer WIN11,4,402,265 --swfUrl http://tvthek.orf.at/flash/player/TVThekPlayer_9_ver18_1.swf";
//                                                    }
//                                                    if (!url_klein.isEmpty()) {
//                                                        mpos = url_klein.indexOf("mp4:");
//                                                        if (mpos != -1) {
//                                                            urlRtmp_klein = "-r " + url_klein + " -y " + url_klein.substring(mpos) + " --flashVer WIN11,4,402,265 --swfUrl http://tvthek.orf.at/flash/player/TVThekPlayer_9_ver18_1.swf";
//                                                        }
//
//                                                    }
//                                                    //rtmp://apasfw.apa.at/cms-worldwide/mp4:2012-09-09_1305_tl_23_UNGARISCHES-MAGAZIN_Beszelgetes-Szabo-Er__4582591__o__0000214447__s4588253___n__BHiRes_13241400P_13280400P_Q6A.mp4
//                                                    //flvr=WIN11,4,402,265
//                                                    //app=cms-worldwide/
//                                                    //swfUrl=http://tvthek.orf.at/flash/player/TVThekPlayer_9_ver18_1.swf
//                                                    //tcUrl=rtmp://apasfw.apa.at/%app%
//                                                    //play=mp4:1950-01-01_1200_in_00_Ungarnkrise-1956_____3230831__o__0000936285__s3230833___Q6A.mp4
//                                                    //flvstreamer --resume --rtmp %tcUrl% --flashVer %flvr% --app %app% --swfUrl %swfUrl% --playpath %play% --flv %Ziel%
//
//                                                    //addFilm(new DatenFilm(senderName, thema, strUrlFeed, titel, url, datum, zeit));
////                                                    addFilm(new DatenFilm(nameSenderMReader, thema, strUrlFeed, titel, url, urlRtmp, datum, zeit));
//                                                    DatenFilm film = new DatenFilm(nameSenderMReader, thema, strUrlFeed, titel, url, urlRtmp, datum, zeit, duration, description,
//                                                            thumbnail, new String[]{});
//                                                    film.addUrlKlein(url_klein, urlRtmp_klein);
//                                                    addFilm(film);
//
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    } catch (SAXException ex) {
//                        MSearchLog.fehlerMeldung(-643206531, MSearchLog.FEHLER_ART_MREADER, "MediathekOrf.feedEinerSeiteSuchen", ex);
//                    } catch (IOException ex) {
//                        MSearchLog.fehlerMeldung(-201456987, MSearchLog.FEHLER_ART_MREADER, "MediathekOrf.feedEinerSeiteSuchen", ex);
//                    } catch (ParserConfigurationException ex) {
//                        MSearchLog.fehlerMeldung(-121036907, MSearchLog.FEHLER_ART_MREADER, "MediathekOrf.feedEinerSeiteSuchen", ex);
//                    }


        }

    }

    public static String getGestern(int tage) {
        try {
            SimpleDateFormat sdfOut = new SimpleDateFormat("EEEE", Locale.US);
            return sdfOut.format(new Date(new Date().getTime() - tage * (1000 * 60 * 60 * 24)));
        } catch (Exception ex) {
            return "";
        }
    }
}
