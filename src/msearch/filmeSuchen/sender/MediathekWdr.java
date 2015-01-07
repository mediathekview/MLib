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
    private MSStringBuilder strSeite = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);

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
        meldungStart();
////////////        addToList__("http://www1.wdr.de/mediathek/video/sendungen/index.html");
        if (MSConfig.senderAllesLaden) {
            //TH Rockpalast hinzu
            String[] add = new String[]{ROCKPALAST_URL, "Rockpalast"};
            listeThemen.addUrl(add);
        }
////////////        // Sendung verpasst, da sind einige die nicht in einer "Sendung" enthalten sind
////////////        // URLs nach dem Muster bauen:
////////////        // http://www1.wdr.de/mediathek/video/sendungverpasst/sendung-verpasst-alles100_tag-03062013.html
////////////        SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy");
////////////        String tag;
////////////        for (int i = 1; i < 14; ++i) {
////////////            final String URL = "http://www1.wdr.de/mediathek/video/sendungverpasst/sendung-verpasst-alles100_tag-";
////////////            tag = formatter.format(new Date().getTime() - (1000 * 60 * 60 * 24 * i));
////////////            String urlString = URL + tag + ".html";
////////////            listeThemen.addUrl(new String[]{urlString, ""});
////////////        }
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

    //===================================
    // private
    //===================================
    private void addToList__(String ADRESSE) {
        // http://www1.wdr.de/mediathek/video/sendungen/abisz-b100.html
        //Theman suchen
        final String MUSTER_URL = "<a href=\"/mediathek/video/sendungen/abisz-";
        MSStringBuilder strSeite_ = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        strSeite_ = getUrlIo.getUri_Iso(SENDERNAME, ADRESSE, strSeite_, "");
        int pos1 = 0;
        int pos2;
        String url;
        themenSeitenSuchen(ADRESSE); // ist die erste Seite: "a"
        while (!MSConfig.getStop() && (pos1 = strSeite_.indexOf(MUSTER_URL, pos1)) != -1) {
            pos1 += MUSTER_URL.length();
            if ((pos2 = strSeite_.indexOf("\"", pos1)) != -1) {
                url = strSeite_.substring(pos1, pos2);
                if (url.equals("")) {
                    MSLog.fehlerMeldung(-995122047, MSLog.FEHLER_ART_MREADER, "MediathekWdr.addToList__", "keine URL");
                } else {
                    url = "http://www1.wdr.de/mediathek/video/sendungen/abisz-" + url;
                    themenSeitenSuchen(url);
                }
            }
        }
    }

    private void themenSeitenSuchen(String strUrlFeed) {
        //<ul class="linkList pictured">
        //<li class="neutral" >
        //<img src="/mediathek/video/sendungen/abenteuer_erde/logo-abenteuer-erde100_v-ARDGrosserTeaser.jpg"    title="Bildrechte: wdr" alt="Logo Abenteuer Erde"   />
        //<a href="/mediathek/video/sendungen/abenteuer_erde/filterseite-abenteuer-erde100.html" >
        //<strong>
        //<strong>Abenteuer Erde</strong>: Die Sendungen im Überblick
        //</strong>
        //</a>

        // url:
        // http://www1.wdr.de/mediathek/video/sendungen/dittsche/videonichtimpapamobil100.html
        final String MUSTER_START = "<ul class=\"linkList pictured\">";
        final String MUSTER_URL = "<a href=\"/mediathek/video/sendungen/";
        int pos1;
        int pos2;
        String url;
        strSeite = getUrlIo.getUri_Iso(SENDERNAME, strUrlFeed, strSeite, "");
        meldung(strUrlFeed);
        if ((pos1 = strSeite.indexOf(MUSTER_START)) == -1) {
            MSLog.fehlerMeldung(-460857479, MSLog.FEHLER_ART_MREADER, "MediathekWdr.themenSeiteSuchen", "keine Url" + strUrlFeed);
            return;
        }
        while (!MSConfig.getStop() && (pos1 = strSeite.indexOf(MUSTER_URL, pos1)) != -1) {
            pos1 += MUSTER_URL.length();
            if ((pos2 = strSeite.indexOf("\"", pos1)) != -1) {
                url = strSeite.substring(pos1, pos2).trim();
                if (!url.equals("")) {
                    url = "http://www1.wdr.de/mediathek/video/sendungen/" + url;
                    //weiter gehts
                    String[] add;
                    add = new String[]{url, ""};
                    listeThemen.addUrl(add);
                }
            } else {
                MSLog.fehlerMeldung(-375862100, MSLog.FEHLER_ART_MREADER, "MediathekWdr.themenSeiteSuchen", "keine Url" + strUrlFeed);
            }
        }
    }

    private class ThemaLaden implements Runnable {

        MSGetUrl getUrl = new MSGetUrl(wartenSeiteLaden);
        private MSStringBuilder strSeite1 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder strSeite2 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder strSeite3 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder strSeite4 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder strVideoSeite = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);

        @Override
        public void run() {
            try {
                meldungAddThread();
                String[] link;
                while (!MSConfig.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    //TH Weiche für Rockpalast
                    if (ROCKPALAST_URL.equals(link[0])) {
                        themenSeiteRockpalast();
                    } else {
                        sendungsSeitenSuchen1(link[0] /* url */);
                    }
                    meldungProgress(link[0]);
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(-633250489, MSLog.FEHLER_ART_MREADER, "MediathekWdr.SenderThemaLaden.run", ex);
            }
            meldungThreadUndFertig();
        }

        private void sendungsSeitenSuchen1(String strUrl) {
            // http://www1.wdr.de/mediathek/video/sendungen/ein_fall_fuer_die_anrheiner/filterseite-ein-fall-fuer-die-anrheiner100_compage-2_paginationId-picturedList0.html#picturedList0
            int pos1;
            int pos2;
            int ende;
            meldung(strUrl);
            // Sendungen auf der Seite
            sendungsSeitenSuchen2(strUrl);
            if (!MSConfig.senderAllesLaden) {
                // dann wars das
                return;
            }
            strSeite1 = getUrl.getUri_Utf(SENDERNAME, strUrl, strSeite1, "");
            // weitere Seiten suchen
            if ((pos1 = strSeite1.indexOf("<ul class=\"pageCounterNavi\">")) == -1) {
                return;
            }
            if ((ende = strSeite1.indexOf("</ul>", pos1)) == -1) {
                return;
            }
            while ((pos1 = strSeite1.indexOf("<a href=\"/mediathek/video/sendungen/", pos1)) != -1) {
                if (pos1 > ende) {
                    // dann wars das
                    return;
                }
                pos1 += "<a href=\"/mediathek/video/sendungen/".length();
                if ((pos2 = strSeite1.indexOf("\"", pos1)) != -1) {
                    String urlWeiter = strSeite1.substring(pos1, pos2);
                    if (!urlWeiter.equals("")) {
                        // Sendungen auf der Seite
                        sendungsSeitenSuchen2("http://www1.wdr.de/mediathek/video/sendungen/" + urlWeiter);
                    }
                }
            }
        }

        private void sendungsSeitenSuchen2(String strUrl) {
            //<div class="pictureCont" id="picturedList0">
            //<ul class="linkList pictured">
            //<li class="mediathekvideo" >
            //<img src="/mediathek/video/sendungen/der_vorkoster/dervorkosterspeisesalzimqualitaetsvergleich100_v-ARDGrosserTeaser.jpg"    title="Bildrechte: wdr" alt="Zwei verschiedene Salzsorten"   />
            //<a href="/mediathek/video/sendungen/der_vorkoster/videodervorkosterspeisesalzimqualitaetsvergleich100.html" >
            //<strong>
            //Der Vorkoster - Speisesalz im Qualitätsvergleich: Sendung vom 03.05.2013
            //</strong>
            //<span class="supplementary">
            //<span class="mediaLength">
            //<span class="hidden">L&auml;nge: </span>00:44:27 <abbr title="Minuten">Min.</abbr>
            //</span>

            // url:
            // http://www1.wdr.de/mediathek/video/sendungen/der_vorkoster/videodervorkosterspeisesalzimqualitaetsvergleich100.html
            final String MUSTER_START_1 = "<ul class=\"linkList pictured\">";
            final String MUSTER_START_2 = "<div id=\"pageLeadIn\">";

            final String MUSTER_URL = "<a href=\"/mediathek/video/sendungen/";
            final String MUSTER_TITEL = "<strong>";
            final String MUSTER_DAUER = "<span class=\"hidden\">L&auml;nge: </span>";
            final String MUSTER_THEMA = "<title>";
            int pos;
            int pos1;
            int pos2;
            int ende;
            String url;
            String titel = "";
            String dauer;
            String datum = "";
            String thema = "";
            long duration = 0;
            boolean verpasst = false;
            pos = 0;
            if (strUrl.startsWith("http://www1.wdr.de/mediathek/video/sendungen/lokalzeit/uebersicht-lokalzeiten100_tag")) {
                // brauchts nicht
                return;
            }
            strSeite2 = getUrl.getUri_Utf(SENDERNAME, strUrl, strSeite2, "");
            meldung(strUrl);
            // Thema suchen
            // <title>Lokalzeit aus Bonn - WDR MEDIATHEK</title>
            if ((pos1 = strSeite2.indexOf(MUSTER_THEMA, pos)) != -1) {
                pos1 += MUSTER_THEMA.length();
                if ((pos2 = strSeite2.indexOf("<", pos1)) != -1) {
                    thema = strSeite2.substring(pos1, pos2);
                    // putzen
                    thema = thema.replace("- WDR MEDIATHEK", "").trim();
                }
            }
            if (thema.startsWith("Sendung verpasst ")) {
                verpasst = true;
            }
            // und jetzt die Beiträge
            if ((pos = strSeite2.indexOf(MUSTER_START_1)) == -1) {
                if ((pos = strSeite2.indexOf(MUSTER_START_2)) == -1) {
                    MSLog.fehlerMeldung(-765323079, MSLog.FEHLER_ART_MREADER, "MediathekWdr.sendungsSeiteSuchen", "keine Url" + strUrl);
                    return;
                }
            }
            if ((ende = strSeite2.indexOf("<ul class=\"pageCounterNavi\">", pos)) == -1) {
                if ((ende = strSeite2.indexOf("<div id=\"socialBookmarks\">", pos)) == -1) {
                    if ((ende = strSeite2.indexOf("<span>Hilfe zur Steuerung der \"Sendung verpasst\"")) == -1) {
                        MSLog.fehlerMeldung(-646897321, MSLog.FEHLER_ART_MREADER, "MediathekWdr.sendungsSeiteSuchen", "keine Url" + strUrl);
                        return;
                    }
                }
            }
            while (!MSConfig.getStop() && (pos = strSeite2.indexOf(MUSTER_URL, pos)) != -1) {
                if (pos > ende) {
                    break;
                }
                pos += MUSTER_URL.length();
                pos1 = pos;
                if ((pos2 = strSeite2.indexOf("\"", pos)) != -1) {
                    url = strSeite2.substring(pos1, pos2).trim();
                    if (!url.equals("")) {
                        url = "http://www1.wdr.de/mediathek/video/sendungen/" + url;
                        if ((pos1 = strSeite2.indexOf(MUSTER_TITEL, pos)) != -1) {
                            pos1 += MUSTER_TITEL.length();
                            if ((pos2 = strSeite2.indexOf("<", pos1)) != -1) {
                                titel = strSeite2.substring(pos1, pos2).trim();
                                if (verpasst) {
                                    thema = "";
                                    // dann Thema aus dem Titel
                                    if (titel.contains(":")) {
                                        thema = titel.substring(0, titel.indexOf(":")).trim();
                                        if (thema.contains(" - ")) {
                                            thema = thema.substring(0, thema.indexOf(" - ")).trim();
                                        }
                                    }
                                }
                                // putzen
                                titel = titel.replace("\n", "");
                                if (titel.contains("-")) {
                                    titel = titel.substring(titel.indexOf("-") + 1, titel.length());
                                }
                                if (titel.contains(":")) {
                                    datum = titel.substring(titel.lastIndexOf(":") + 1, titel.length()).trim();
                                    if (datum.contains(" vom")) {
                                        datum = datum.substring(datum.indexOf(" vom") + " vom".length()).trim();
                                    }
                                    titel = titel.substring(0, titel.lastIndexOf(":")).trim();
                                }
                            }
                        }
                        if ((pos1 = strSeite2.indexOf(MUSTER_DAUER, pos)) != -1) {
                            pos1 += MUSTER_DAUER.length();
                            if ((pos2 = strSeite2.indexOf("<", pos1)) != -1) {
                                dauer = strSeite2.substring(pos1, pos2).trim();
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
                                    MSLog.fehlerMeldung(-306597519, MSLog.FEHLER_ART_MREADER, "MediathekWdr.sendungsSeiteSuchen", ex, strUrl);
                                }
                            }
                        }
//                        if (thema.equals("") || datum.equals("") || titel.equals("") || duration == 0) {
//                            Log.fehlerMeldung(-323569701, Log.FEHLER_ART_MREADER, "MediathekWdr.sendungsSeiteSuchen", strUrl);
//                        }
                        //weiter gehts
                        addFilm1(thema, titel, url, duration, datum);
                    } else {
                        MSLog.fehlerMeldung(-646432970, MSLog.FEHLER_ART_MREADER, "MediathekWdr.sendungsSeiteSuchen-1", "keine Url" + strUrl);
                    }
                }
            }
        }

        private void addFilm1(String thema, String titel, String filmWebsite, long dauer, String datum) {
            // http://www1.wdr.de/mediathek/video/sendungen/die_story/videopharmasklaven100-videoplayer_size-L.html

            final String MUSTER_URL_START = "<span class=\"videoLink\"";
            final String MUSTER_URL = "<a href=\"/mediathek/video/sendungen/";
            final String MUSTER_DESCRIPTION = "<meta name=\"Description\" content=\"";

            final String MUSTER_KEYWORDS = "<meta name=\"Keywords\" content=\"";
            meldung(filmWebsite);
            strSeite3 = getUrl.getUri_Utf(SENDERNAME, filmWebsite, strSeite3, "");
            int pos1;
            int pos2;
            String url = "";
            String description = "";
            String[] keywords = new String[]{};

            if ((pos1 = strSeite3.indexOf(MUSTER_DESCRIPTION)) != -1) {
                pos1 += MUSTER_DESCRIPTION.length();
                if ((pos2 = strSeite3.indexOf("\"", pos1)) != -1) {
                    description = strSeite3.substring(pos1, pos2);
                }
            }

            if ((pos1 = strSeite3.indexOf(MUSTER_KEYWORDS)) != -1) {
                pos1 += MUSTER_KEYWORDS.length();
                if ((pos2 = strSeite3.indexOf("\"", pos1)) != -1) {
                    String k = strSeite3.substring(pos1, pos2);
                    keywords = k.split(", ");
                }
            }
            // URL suchen
            if ((pos1 = strSeite3.indexOf(MUSTER_URL_START)) != -1) {
                pos1 += MUSTER_URL_START.length();
                if ((pos1 = strSeite3.indexOf(MUSTER_URL, pos1)) != -1) {
                    pos1 += MUSTER_URL.length();
                    if ((pos2 = strSeite3.indexOf("\"", pos1)) != -1) {
                        url = strSeite3.substring(pos1, pos2);
                    }
                }
            } else if ((pos1 = strSeite3.indexOf("<li class=\"mediathekvideo\" >")) != -1) {
                //pos1 += "<li class=\"mediathekvideo\" >".length();
                if ((pos1 = strSeite3.indexOf(MUSTER_URL, pos1)) != -1) {
                    pos1 += MUSTER_URL.length();
                    if ((pos2 = strSeite3.indexOf("\"", pos1)) != -1) {
                        url = strSeite3.substring(pos1, pos2);
                    }
                }
            }

//            if (description.equals("") || image.equals("") || keywords.equals("")) {
//                Log.fehlerMeldung(-649830789, Log.FEHLER_ART_MREADER, "MediathekWdr.addFilm1", new String[]{filmWebsite});
//            }
            if (!url.equals("")) {
                addFilm2(filmWebsite, thema, titel, "http://www1.wdr.de/mediathek/video/sendungen/" + url, dauer, datum, description, keywords);
            } else {
                MSLog.fehlerMeldung(-763299001, MSLog.FEHLER_ART_MREADER, "MediathekWdr.addFilme1", new String[]{"keine Url: " + filmWebsite});
            }

        }

        private void addFilm2(String filmWebsite, String thema, String titel, String urlFilmSuchen, long dauer, String datum, String beschreibung, String[] keyword) {
            // ;dslSrc=rtmp://gffstream.fcod.llnwd.net/a792/e1/media/video/2009/02/14/20090214_a40_komplett_big.flv&amp;isdnSrc=rtm
            // <p class="wsArticleAutor">Ein Beitrag von Heinke Schröder, 24.11.2010	</p>
            final String MUSTER_URL_L = "<a rel=\"webL\"  href=\"";
            final String MUSTER_URL_M = "<a rel=\"webM\"  href=\"";
            final String MUSTER_URL_S = "<a rel=\"webS\"  href=\"";
            meldung(urlFilmSuchen);
            strSeite4 = getUrl.getUri_Utf(SENDERNAME, urlFilmSuchen, strSeite4, "");
            String url;
            String urlKlein;

            // URL suchen
            url = strSeite4.extract(MUSTER_URL_L, "\"");
            urlKlein = strSeite4.extract(MUSTER_URL_M, "\"");
            if (url.equals("")) {
                url = urlKlein;
                urlKlein = "";
            }
            if (url.equals("")) {
                url = strSeite4.extract(MUSTER_URL_S, "\"");
            } else if (urlKlein.equals("")) {
                urlKlein = strSeite4.extract(MUSTER_URL_S, "\"");
            }

            if (!url.isEmpty()) {
                // URL bauen von
                final String mobileUrl = "http://mobile-ondemand.wdr.de/";
                final String replaceUrl = "http://http-ras.wdr.de/";
                url = url.replace(mobileUrl, replaceUrl);
                urlKlein = urlKlein.replace(mobileUrl, replaceUrl);

                DatenFilm film = new DatenFilm(SENDERNAME, thema, filmWebsite, titel, url, ""/*rtmpURL*/, datum, ""/* zeit */,
                        dauer, beschreibung, keyword);
                film.addUrlKlein(urlKlein, "");
                addFilm(film);
            } else {
                MSLog.fehlerMeldung(-978451239, MSLog.FEHLER_ART_MREADER, "MediathekWdr.addFilme2", new String[]{"keine Url: " + urlFilmSuchen, "UrlThema: " + filmWebsite});
            }
        }

        private void themenSeiteRockpalast() {
            final String ROOTADR = "http://www.wdr.de/fernsehen/kultur/rockpalast/videos/";
            final String ITEM_1 = "<a href=\"/fernsehen/kultur/rockpalast/videos/";
            strVideoSeite = getUrl.getUri_Utf(SENDERNAME, ROCKPALAST_URL, strVideoSeite, "");
            try {
                ArrayList<String> liste = new ArrayList<>();
                strVideoSeite.extractList(ITEM_1, "\"", 0, ROOTADR, liste);
                for (String s : liste) {
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
                    addFilmeRockpalast(s);
                }

            } catch (Exception ex) {
                MSLog.fehlerMeldung(-696963025, MSLog.FEHLER_ART_MREADER, "MediathekWdr.themenSeiteRockpalast", ex);
            }
        }

        private void addFilmeRockpalast(String filmWebsite) {
            meldung(filmWebsite);
            strSeite2 = getUrl.getUri_Utf(SENDERNAME, filmWebsite, strSeite2, "");
            String url;
            String datum;
            long duration = 0;
            String description;
            String[] keywords;

            String thema = "Rockpalast";
            String titel = strSeite2.extract("headline: \"", "\"");
            titel = titel.replace("\n", "");

            String d = strSeite2.extract("length: \"(", ")");
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
                MSLog.fehlerMeldung(-302058974, MSLog.FEHLER_ART_MREADER, "MediathekWdr.addFilme2-1", ex, "duration: " + d);
            }

            description = strSeite2.extract("<meta name=\"Description\" content=\"", "\"");
            //description = description.replace("Rockpalast, das Kult-Programm mit festem w&ouml;chentlichen\nSendeplatz und regelm&auml;&szlig;igen zus&auml;tzlichen\nRockn&auml;chten\" ", "");

            String k = strSeite2.extract("<meta name=\"Keywords\" content=\"", "\"");
            keywords = k.split(", ");

            // Datum suchen
            // <li><a href="/tv/rockpalast/sendungsbeitraege/2014/0317/index.jsp">Rockpalast am 17.03.2014, 00:15 Uhr</a></li>
            datum = strSeite2.extract("<h3>Konzert vom", "<").trim();
            url = strSeite2.extract("<a href=\"/fernsehen/kultur/rockpalast/videos/av/", "\"");
            if (url.isEmpty()) {
                MSLog.fehlerMeldung(-915236547, MSLog.FEHLER_ART_MREADER, "MediathekWdr.addFilmeRockpalast", "keine URL: " + filmWebsite);
            } else {
                url = "http://www1.wdr.de/fernsehen/kultur/rockpalast/videos/av/" + url;
                addFilm2(filmWebsite, thema, titel, url, duration, datum, description, keywords);
            }
        }
    }
}
