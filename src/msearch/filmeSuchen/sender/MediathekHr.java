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
import java.util.ArrayList;
import java.util.Date;
import msearch.daten.DatenFilm;
import msearch.daten.MSConfig;
import msearch.filmeSuchen.MSFilmeSuchen;
import msearch.io.MSGetUrl;
import msearch.tool.MSConst;
import msearch.tool.MSFunktionen;
import msearch.tool.MSLog;
import msearch.tool.MSStringBuilder;

public class MediathekHr extends MediathekReader implements Runnable {

    public final static String SENDERNAME = "HR";
    private MSStringBuilder seite = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
    private MSStringBuilder rubrikSeite = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);

    /**
     *
     * @param ssearch
     * @param startPrio
     */
    public MediathekHr(MSFilmeSuchen ssearch, int startPrio) {
        super(ssearch, SENDERNAME, /* threads */ 2, /* urlWarten */ 500, startPrio);
    }

    /**
     *
     */
    @Override
    public void addToList() {
        final String MUSTER = "sendEvent('load','";
        meldungStart();
        seite = getUrlIo.getUri_Utf(SENDERNAME, "http://www.hr-online.de/website/fernsehen/sendungen/index.jsp", seite, "");

        //TH 7.8.2012 Erst suchen nach Rubrik-URLs, die haben Thema
        bearbeiteRubrik(seite);
        bearbeiteTage(seite);

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

    private void bearbeiteTage(MSStringBuilder seite) {
        // loadPlayItems('http://www.hr-online.de/website/includes/medianew-playlist.xml.jsp?logic=start_multimedia_document_logic_39004789&xsl=media2html5.xsl');

        final String TAGE_PREFIX = "http://www.hr-online.de/website/includes/medianew-playlist.xml.jsp?logic=start_multimedia_document_logic_";
        final String TAGE_MUSTER = "http://www.hr-online.de/website/includes/medianew-playlist.xml.jsp?logic=start_multimedia_document_logic_";
        ArrayList<String> erg = new ArrayList<>();
        seite.extractList(TAGE_MUSTER, "&", 0, TAGE_PREFIX, erg);
        for (String url : erg) {
            String[] add = new String[]{url, ""/*thema*/, "http://www.hr-online.de/website/fernsehen/sendungen/index.jsp"/*filmsite*/};
            if (!istInListe(listeThemen, url, 0)) {
                listeThemen.add(add);
            }
        }
    }

    //TH 7.8.2012 Suchen in Seite von Rubrik-URL
    // z.B. http://www.hr-online.de/website/fernsehen/sendungen/index.jsp?rubrik=2254
    private void bearbeiteRubrik(MSStringBuilder seite) {
        final String RUBRIK_PREFIX = "http://www.hr-online.de/website/fernsehen/sendungen/index.jsp?rubrik=";
        final String RUBRIK_MUSTER = "<option value=\"/website/fernsehen/sendungen/index.jsp?rubrik=";
        ArrayList<String> erg = new ArrayList<>();
        seite.extractList(RUBRIK_MUSTER, "\"", 0, RUBRIK_PREFIX, erg);
        for (String s : erg) {
            rubrik(s);
        }
    }

    private void rubrik(String rubrikUrl) {
        final String RUBRIK_PREFIX = "http://www.hr-online.de/website/fernsehen/sendungen/index.jsp?rubrik=";
        final String MUSTER = "/website/includes/medianew-playlist.xml.jsp?logic=start_multimedia_document_logic_";
        final String MUSTER_TITEL = "<meta property=\"og:title\" content=\"";

        rubrikSeite = getUrlIo.getUri_Iso(SENDERNAME, rubrikUrl, rubrikSeite, "");
        int pos = 0, pos2;
        String url, thema = "";

        // 1. Titel (= Thema) holen
        thema = rubrikSeite.extract(MUSTER_TITEL, "\""); // <meta property="og:title" content="Alle Wetter | Fernsehen | hr-online.de"/>
        if (thema.contains("|")) {
            thema = thema.substring(0, thema.indexOf("|")).trim();
        }

        // 2. suchen nach XML Liste       
        url = rubrikSeite.extract(MUSTER, "&");
        if (!url.equals("")) {
            url = "http://www.hr-online.de/website/includes/medianew-playlist.xml.jsp?logic=start_multimedia_document_logic_" + url;
            String[] add = new String[]{url, thema, rubrikUrl};
            if (!istInListe(listeThemen, url, 0)) {
                listeThemen.add(add);
            }
        } else {
            MSLog.fehlerMeldung(-653210697, MSLog.FEHLER_ART_MREADER, "MediathekHr.bearbeiteRubrik", "keine URL");
        }

        // gibts scheinbar nicht mehr
////        // 3. Test: Suchen nach extra-Seite "Videos"
////        final String MEDIA_MUSTER = "<li class=\"navi\"><a href=\"index.jsp?rubrik=";
////        String videoUrl = null;
////        pos = 0;
////        if ((pos = rubrikSeite.indexOf(MEDIA_MUSTER, pos)) != -1) {
////            pos += MEDIA_MUSTER.length();
////            pos2 = rubrikSeite.indexOf("\"", pos);
////            if (pos2 != -1) {
////                String key = rubrikSeite.substring(pos, pos2);
////                pos = pos2;
////                if (rubrikSeite.substring(pos, pos + 36).equals("\" class=\"navigation\" title=\"Videos\">")) {
////                    videoUrl = RUBRIK_PREFIX + key;
////                }
////            }
////        }
////
////        if (videoUrl == null) {
////            return;
////        }
////
////        // 4. dort Verweise auf XML Einträge finden
////        rubrikSeite = getUrlIo.getUri_Iso(SENDERNAME, videoUrl, rubrikSeite, "");
////        pos = 0;
////        final String PLAYER_MUSTER = "<a href=\"mediaplayer.jsp?mkey=";
////        while ((pos = rubrikSeite.indexOf(PLAYER_MUSTER, pos)) != -1) {
////            pos += PLAYER_MUSTER.length();
////            pos2 = rubrikSeite.indexOf("&", pos);
////            if (pos2 != -1) {
////                url = rubrikSeite.substring(pos, pos2);
////                if (!url.equals("") && url.matches("[0-9]*")) {
////                    url = "http://www.hr-online.de/website/includes/medianew.xml.jsp?key=" + url + "&xsl=media2rss-nocopyright.xsl";
////
////                    String[] add = new String[]{
////                        url, thema
////                    };
////                    if (!istInListe(listeThemen, url, 0)) {
////                        listeThemen.add(add);
////                    }
////                }
////                pos = pos2;
////            }
////        }
    }

    private class ThemaLaden implements Runnable {

        MSGetUrl getUrl = new MSGetUrl(wartenSeiteLaden);
        private MSStringBuilder seite1 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        //private MVStringBuilder seite2 = new MVStringBuilder();

        @Override
        public void run() {
            try {
                meldungAddThread();
                String link[];
                while (!MSConfig.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    meldungProgress(link[0] /*url*/);
                    seite.setLength(0);
                    addFilme(link[0]/*url*/, link[1]/*thema*/, link[2]/*filmsite*/);
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(-894330854, MSLog.FEHLER_ART_MREADER, "MediathekHr.ThemaLaden.run", ex, "");
            }
            meldungThreadUndFertig();
        }

        private void addFilme(String xmlWebsite, String thema_, String filmSite) {
            final String MUSTER_ITEM_1 = "<videos>";

            final String MUSTER_TITEL = "<title>"; //<title>Sonnenziel Valencia</title>
            final String MUSTER_URL = "<url type=\"mp4\">"; // <url type="mp4">http://www.hr.gl-systemhaus.de/video/fs/servicereisen/2014_11/141114214510_service_re_44765.mp4</url>
            final String MUSTER_URL_LOW = "<url type=\"mp4-small\">";
            final String MUSTER_DATUM = "<date>"; //<date>14.11.2014 18:50</date>
            final String MUSTER_THEMA = "<author>"; //<author>service: reisen</author>

            final String MUSTER_DURATION = "<duration>"; // <duration>00:43:32</duration>
            final String MUSTER_DESCRIPTION = "<description>";
            final String END = "</";
            meldung(xmlWebsite);
            seite1 = getUrl.getUri_Iso(SENDERNAME, xmlWebsite, seite1, "");
            try {
                int posItem1 = 0;
                String url = "", url_low;
                String datum, zeit = "";
                String titel, thema;
                long duration = 0;
                String description;
                while (!MSConfig.getStop() && (posItem1 = seite1.indexOf(MUSTER_ITEM_1, posItem1)) != -1) {
                    posItem1 += MUSTER_ITEM_1.length();

                    String d = seite1.extract(MUSTER_DURATION, END, posItem1);
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
                        MSLog.fehlerMeldung(-708096931, MSLog.FEHLER_ART_MREADER, "MediathekHr.addFilm", "d: " + d);
                    }
                    description = seite1.extract(MUSTER_DESCRIPTION, END, posItem1);
                    datum = seite1.extract(MUSTER_DATUM, END, posItem1);
                    if (datum.contains(" ")) {
                        zeit = datum.substring(datum.indexOf(" ")).trim() + ":00";
                        datum = datum.substring(0, datum.indexOf(" "));
                    }
                    titel = seite1.extract(MUSTER_TITEL, END, posItem1);

                    thema = seite1.extract(MUSTER_THEMA, END, posItem1);
                    if (thema.isEmpty()) {
                        thema = thema_;
                    }
                    if (thema.isEmpty()) {
                        thema = titel;
                    }
                    url = seite1.extract(MUSTER_URL, END, posItem1);
                    url_low = seite1.extract(MUSTER_URL_LOW, END, posItem1);
                    if (url.equals(url_low)) {
                        url_low = "";
                    }
                    if (!url.isEmpty()) {
                        if (datum.equals("")) {
                            datum = getDate(url);
                        }
                        // DatenFilm(String ssender, String tthema, String urlThema, String ttitel, String uurl, String uurlorg, String uurlRtmp, String datum, String zeit) {
                        //DatenFilm film = new DatenFilm(nameSenderMReader, thema, strUrlFeed, titel, url, furl, datum, "");
                        DatenFilm film = new DatenFilm(SENDERNAME, thema, filmSite, titel, url, "", datum, zeit, duration, description,
                                "", new String[]{});
                        if (!url_low.isEmpty()) {
                            film.addUrlKlein(url_low, "");
                        }
                        addFilm(film);
                    } else {
                        MSLog.fehlerMeldung(-649882036, MSLog.FEHLER_ART_MREADER, "MediathekHr.addFilme", "keine URL");
                    }
                }
                if (url.isEmpty()) {
                    MSLog.fehlerMeldung(-761236458, MSLog.FEHLER_ART_MREADER, "MediathekHr.addFilme", "keine URL für: " + xmlWebsite);
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(-487774126, MSLog.FEHLER_ART_MREADER, "MediathekHr.addFilme", ex, "");
            }
        }

        private String getDate(String url) {
            String ret = "";
            try {
                String tmp = MSFunktionen.getDateiName(url);
                if (tmp.length() > 8) {
                    tmp = tmp.substring(0, 8);
                    SimpleDateFormat sdfIn = new SimpleDateFormat("yyyyMMdd");
                    Date filmDate = sdfIn.parse(tmp);
                    SimpleDateFormat sdfOut;
                    sdfOut = new SimpleDateFormat("dd.MM.yyyy");
                    ret = sdfOut.format(filmDate);
                }
            } catch (Exception ex) {
                ret = "";
                MSLog.fehlerMeldung(-356408790, MSLog.FEHLER_ART_MREADER, "MediathekHr.getDate", "kein Datum");
            }
            return ret;
        }
    }
}
