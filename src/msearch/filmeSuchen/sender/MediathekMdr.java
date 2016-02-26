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
import java.util.Iterator;
import java.util.LinkedList;
import msearch.daten.DatenFilm;
import msearch.filmeSuchen.MSFilmeSuchen;
import msearch.filmeSuchen.MSGetUrl;
import msearch.tool.MSConfig;
import msearch.tool.MSConst;
import msearch.tool.MSLog;
import msearch.tool.MSStringBuilder;

/**
 *
 * @author
 */
public class MediathekMdr extends MediathekReader implements Runnable {

    public final static String SENDERNAME = "MDR";
    private final LinkedList<String> listeTage = new LinkedList<>();
    private final LinkedList<String[]> listeGesucht = new LinkedList<>(); //thema,titel,datum,zeit

    /**
     *
     * @param ssearch
     * @param startPrio
     */
    public MediathekMdr(MSFilmeSuchen ssearch, int startPrio) {
        super(ssearch, SENDERNAME, /* threads */ 3, /* urlWarten */ 500, startPrio);
    }

    /**
     *
     */
    @Override
    public void addToList() {
        // <a href="/mediathek/fernsehen/a-z/sendungenabisz100_inheritancecontext-header_letter-A_numberofelements-1_zc-ef89b6fa.html#letternavi

        final String URL_SENDUNGEN = "http://www.mdr.de/mediathek/fernsehen/a-z/index.html";
        final String MUSTER = "<a href=\"/mediathek/fernsehen/a-z/sendungenabisz100_inheritancecontext-header_letter";
        final String MUSTER_ADD = "http://www.mdr.de/mediathek/fernsehen/a-z/sendungenabisz100_inheritancecontext-header_letter";

        final String URL_TAGE = "http://www.mdr.de/mediathek/fernsehen/index.html";
        final String MUSTER_TAGE = "<a href=\"/mediathek/fernsehen/sendungverpasst100-multiGroupClosed_boxIndex-";
        final String MUSTER_ADD_TAGE = "http://www.mdr.de/mediathek/fernsehen/sendungverpasst100-multiGroupClosed_boxIndex-";

        MSStringBuilder seite = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        listeThemen.clear();
        listeTage.clear();
        listeGesucht.clear();
        meldungStart();
        seite = getUrlIo.getUri_Utf(SENDERNAME, URL_SENDUNGEN, seite, "");
        int pos = 0;
        int pos1;
        int pos2;
        String url = "";
        while ((pos = seite.indexOf(MUSTER, pos)) != -1) {
            pos += MUSTER.length();
            pos1 = pos;
            pos2 = seite.indexOf("\"", pos);
            if (pos1 != -1 && pos2 != -1) {
                url = seite.substring(pos1, pos2);
            }
            if (url.equals("")) {
                MSLog.fehlerMeldung(889216307, "keine URL");
            } else {
                url = MUSTER_ADD + url;
                if (url.contains("#")) {
                    url = url.substring(0, url.indexOf("#"));
                }
                listeThemen.addUrl(new String[]{url});
            }
        }
//        seite = getUrlIo.getUri_Utf(SENDERNAME, URL_TAGE, seite, "");
//        pos = 0;
//        url = "";
//        while ((pos = seite.indexOf(MUSTER_TAGE, pos)) != -1) {
//            pos += MUSTER_TAGE.length();
//            pos1 = pos;
//            pos2 = seite.indexOf("\"", pos);
//            if (pos1 != -1 && pos2 != -1) {
//                url = seite.substring(pos1, pos2);
//            }
//            if (url.equals("")) {
//                MSLog.fehlerMeldung(461225808, "keine URL");
//            } else {
//                url = MUSTER_ADD_TAGE + url;
//                if (!istInListe(listeTage, url)) {
//                    listeTage.add(url);
//                }
//            }
//        }
        if (MSConfig.getStop()) {
            meldungThreadUndFertig();
        } else if (listeThemen.size() == 0 && listeTage.size() == 0) {
            meldungThreadUndFertig();
        } else {
            meldungAddMax(listeThemen.size() + listeTage.size());
            listeSort(listeThemen, 0);
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
        private MSStringBuilder seite3 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder seite33 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder seite4 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder seite5 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);

        @Override
        public void run() {
            try {
                meldungAddThread();
                String[] link;
                while (!MSConfig.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    meldungProgress(link[0]);
                    addThema(link[0]);
                }
                String url;
                while (!MSConfig.getStop() && (url = getListeTage()) != null) {
                    meldungProgress(url);
                    addTage(url);
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(115896304, ex);
            }
            meldungThreadUndFertig();
        }

        void addTage(String urlSeite) {
            final String MUSTER_START_1 = "<div class=\"teaserImage\">";
            final String MUSTER_START_2 = "<h3>";
            final String MUSTER_THEMA = "title=\"Zu den Inhalten der Sendung\">";
            final String MUSTER_URL = "<a href=\"/mediathek/fernsehen/";
            final String MUSTER_ADD = "http://www.mdr.de/mediathek/fernsehen/";
            int pos = 0, posStop;
            String url;
            String thema;
            try {
                seite1 = getUrl.getUri_Utf(SENDERNAME, urlSeite, seite1, "");
                posStop = seite1.indexOf("title=\"Was ist das?\">Empfehlen</a>");
                while (!MSConfig.getStop() && (pos = seite1.indexOf(MUSTER_START_1, pos)) != -1) {
                    if (posStop > 0 && pos > posStop) {
                        break;
                    }
                    url = "";
                    thema = "";
                    pos += MUSTER_START_1.length();
                    if ((pos = seite1.indexOf(MUSTER_START_2, pos)) == -1) {
                        break;
                    }
                    pos += MUSTER_START_2.length();
                    thema = seite1.extract(MUSTER_THEMA, "<", pos);
                    url = seite1.extract(MUSTER_URL, "\"", pos);
                    if (url.equals("")) {
                        MSLog.fehlerMeldung(392854069, new String[]{"keine URL: " + urlSeite});
                    } else {
                        url = MUSTER_ADD + url;
                        meldung(url);
                        addSendugTage(urlSeite, thema, url);
                    }
                }// while
            } catch (Exception ex) {
                MSLog.fehlerMeldung(556320478, ex);
            }
        }

        private void addSendugTage(String strUrlFeed, String thema, String urlThema) {
            final String MUSTER_ADD = "http://www.mdr.de/mediathek/fernsehen/";
            seite5 = getUrl.getUri_Utf(SENDERNAME, urlThema, seite5, "Thema: " + thema);
            String url = seite5.extract("dataURL:'/mediathek/fernsehen/", "'");
            if (url.equals("")) {
                MSLog.fehlerMeldung(701025498, new String[]{"keine URL: " + urlThema, "Thema: " + thema, "UrlFeed: " + strUrlFeed});
            } else {
                url = MUSTER_ADD + url;
            }
            if (!MSConfig.getStop()) {
                addXml(strUrlFeed, thema, url, urlThema);
            }
        }

        void addThema(String strUrlFeed) {
            final String MUSTER = "<div class=\"media mediaA \">";

            int pos = 0;
            String thema, url = "";
            try {
                seite2 = getUrl.getUri(SENDERNAME, strUrlFeed, MSConst.KODIERUNG_UTF, 2 /* versuche */, seite2, "");
                while (!MSConfig.getStop() && (pos = seite2.indexOf(MUSTER, pos)) != -1) {
                    pos += MUSTER.length();
                    url = seite2.extract("<a href=\"/mediathek/fernsehen/", "\"", pos);
                    thema = seite2.extract(" class=\"headline\" title=\"\">", "<", pos);
                    if (url.equals("")) {
                        MSLog.fehlerMeldung(952136547, "keine URL: " + strUrlFeed);
                    } else {
                        meldung(url);
                        url = "http://www.mdr.de/mediathek/fernsehen/" + url;
                        addSendugen(strUrlFeed, thema, url);
                    }
                }
                if (url.equals("")) {
                    MSLog.fehlerMeldung(766250249, "keine URL: " + strUrlFeed);
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(316874602, ex);
            }
        }

        private void addSendugen(String strUrlFeed, String thema, String urlThema) {
            seite3 = getUrl.getUri(SENDERNAME, urlThema, MSConst.KODIERUNG_UTF, 2 /* versuche */, seite3, "Thema: " + thema);
            final String muster;
            if (seite3.indexOf("div class=\"media mediaA \">") != -1) {
                muster = "div class=\"media mediaA \">";
            } else {
                muster = "<span class=\"broadcastSeriesTitle\">";
            }
            int pos = 0, count = 0;
            String url = "";
            while ((pos = seite3.indexOf(muster, pos)) != -1) {
                ++count;
                if (!MSConfig.loadLongMax()) {
                    if (count > 5) {
                        return;
                    }
                }
                pos += muster.length();
                url = seite3.extract("<a href=\"/mediathek/fernsehen/a-z", "\"", pos);
                if (url.equals("")) {
                    MSLog.fehlerMeldung(915263421, new String[]{"keine URL: " + urlThema, "Thema: " + thema, "UrlFeed: " + strUrlFeed});
                } else {
                    url = "http://www.mdr.de/mediathek/fernsehen/a-z" + url;
                    addSendug(strUrlFeed, thema, url);
                }
            }

            if (url.equals("")) {
                MSLog.fehlerMeldung(765213014, new String[]{"keine URL: " + urlThema, "Thema: " + thema, "UrlFeed: " + strUrlFeed});
            }
        }

        private void addSendug(String strUrlFeed, String thema, String urlSendung) {
            final String MUSTER_XML = "'playerXml':'";
            final String MUSTER_ADD = "http://www.mdr.de";
            seite33 = getUrl.getUri_Utf(SENDERNAME, urlSendung, seite33, "Thema: " + thema);
            int pos = 0;
            int pos1;
            int pos2;
            String url = "";
            while ((pos = seite33.indexOf(MUSTER_XML, pos)) != -1) {
                pos += MUSTER_XML.length();
                pos1 = pos;
                if ((pos2 = seite33.indexOf("'", pos)) != -1) {
                    url = seite33.substring(pos1, pos2);
                }
                if (url.equals("")) {
                    MSLog.fehlerMeldung(256987304, new String[]{"keine URL: " + urlSendung, "Thema: " + thema, "UrlFeed: " + strUrlFeed});
                } else {
                    url = url.replace("\\", "");
                    url = MUSTER_ADD + url;
                    addXml(strUrlFeed, thema, url, urlSendung);
                }
            }
            if (url.equals("")) {
                MSLog.fehlerMeldung(256987304, new String[]{"keine URL: " + urlSendung, "Thema: " + thema, "UrlFeed: " + strUrlFeed});
            }
        }

        void addXml(String strUrlFeed, String thema, String xmlSite, String filmSite) {
            final String MUSTER_URL_MP4 = "<progressiveDownloadUrl>";
            String titel, datum, zeit, urlMp4, urlMp4_klein, urlHD, urlSendung, description;
            long duration;

            try {
                seite4 = getUrl.getUri_Utf(SENDERNAME, xmlSite, seite4, "Thema: " + thema);
                if (seite4.length() == 0) {
                    MSLog.fehlerMeldung(903656532, xmlSite);
                    return;
                }

                duration = 0;
                try {
                    String d = seite4.extract("<duration>", "<");
                    if (!d.equals("")) {
                        String[] parts = d.split(":");
                        duration = 0;
                        long power = 1;
                        for (int i = parts.length - 1; i >= 0; i--) {
                            duration += Long.parseLong(parts[i]) * power;
                            power *= 60;
                        }
                    }
                } catch (Exception ex) {
                    MSLog.fehlerMeldung(313698749, ex, xmlSite);
                }

                titel = seite4.extract("<title>", "<");
                description = seite4.extract("<teaserText>", "<");
                String subtitle = seite4.extract("<videoSubtitleUrl>", "<");
                datum = seite4.extract("<broadcastStartDate>", "<");
                if (datum.isEmpty()) {
                    datum = seite4.extract("<datetimeOfBroadcasting>", "<");
                }
                if (datum.isEmpty()) {
                    datum = seite4.extract("<webTime>", "<");
                }
                zeit = convertZeitXml(datum);
                datum = convertDatumXml(datum);
                urlSendung = seite4.extract("<htmlUrl>", "<");
                if (urlSendung.isEmpty()) {
                    urlSendung = filmSite;
                }

                // Film-URLs suchen
                urlHD = seite4.extract("| MP4 Web XL |", MUSTER_URL_MP4, "<");
                urlMp4 = seite4.extract("| MP4 Web L |", MUSTER_URL_MP4, "<");
                if (urlMp4.isEmpty()) {
                    urlMp4 = seite4.extract("| MP4 Web L+ |", MUSTER_URL_MP4, "<");
                }
                urlMp4_klein = seite4.extract("| MP4 Web M |", MUSTER_URL_MP4, "<");

                if (urlMp4.isEmpty()) {
                    urlMp4 = urlMp4_klein;
                    urlMp4_klein = "";
                }

                if (urlMp4.equals("")) {
                    MSLog.fehlerMeldung(326541230, new String[]{"keine URL: " + xmlSite, "Thema: " + thema, " UrlFeed: " + strUrlFeed});
                } else {
                    if (!existiertSchon(thema, titel, datum, zeit)) {
                        meldung(urlMp4);

                        DatenFilm film = new DatenFilm(SENDERNAME, thema, urlSendung, titel, urlMp4, ""/*rtmpUrl*/, datum, zeit, duration, description);
                        film.addUrlKlein(urlMp4_klein, "");
                        film.addUrlHd(urlHD, "");
                        film.addUrlSubtitle(subtitle);
                        addFilm(film);
                    }
                }

            } catch (Exception ex) {
                MSLog.fehlerMeldung(446286970, ex);
            }
        }
    }

    private String convertDatumXml(String datum) {
        //<broadcastStartDate>23.08.2012 22:05</broadcastStartDate>
        try {
            SimpleDateFormat sdfIn = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            Date filmDate = sdfIn.parse(datum);
            SimpleDateFormat sdfOut;
            sdfOut = new SimpleDateFormat("dd.MM.yyyy");
            datum = sdfOut.format(filmDate);
        } catch (Exception ex) {
            MSLog.fehlerMeldung(435209987, ex);
        }
        return datum;
    }

    private String convertZeitXml(String datum) {
        //<broadcastStartDate>23.08.2012 22:05</broadcastStartDate>
        try {
            SimpleDateFormat sdfIn = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            Date filmDate = sdfIn.parse(datum);
            SimpleDateFormat sdfOut;
            sdfOut = new SimpleDateFormat("HH:mm:ss");
            datum = sdfOut.format(filmDate);
        } catch (Exception ex) {
            MSLog.fehlerMeldung(102658736, ex);
        }
        return datum;
    }

    private synchronized String getListeTage() {
        return listeTage.pollFirst();
    }

    private synchronized boolean existiertSchon(String thema, String titel, String datum, String zeit) {
        // liefert true wenn schon in der Liste, ansonsten fügt es ein
        boolean gefunden = false;
        Iterator<String[]> it = listeGesucht.iterator();
        while (it.hasNext()) {
            String[] k = it.next();
            if (k[0].equalsIgnoreCase(thema) && k[1].equalsIgnoreCase(titel) && k[2].equalsIgnoreCase(datum) && k[3].equalsIgnoreCase(zeit)) {
                gefunden = true;
            }
        }
        if (!gefunden) {
            listeGesucht.add(new String[]{thema, titel, datum, zeit});
        }
        return gefunden;
    }
}
