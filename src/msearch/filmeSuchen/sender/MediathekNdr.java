/*    
 *    MediathekView
 *    Copyright (C) 2008   W. Xaver
 *    W.Xaver[at]googlemail.com
 *    http://zdfmediathk.sourceforge.net/
 *    
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package msearch.filmeSuchen.sender;

import java.text.SimpleDateFormat;
import java.util.Date;
import msearch.daten.DatenFilm;
import msearch.filmeSuchen.MSFilmeSuchen;
import msearch.filmeSuchen.MSGetUrl;
import msearch.tool.MSConfig;
import msearch.tool.MSConst;
import msearch.tool.MSLog;
import msearch.tool.MSStringBuilder;

public class MediathekNdr extends MediathekReader implements Runnable {

    public final static String SENDERNAME = "NDR";
    private MSStringBuilder seiteAlle = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);

    public MediathekNdr(MSFilmeSuchen ssearch, int startPrio) {
        super(ssearch, SENDERNAME, /* threads */ 2, /* urlWarten */ 250, startPrio);
    }

    //-> erste Seite:
    // <h5><a href="/mediathek/mediatheksuche103_broadcast-30.html">Nordmagazin</a></h5>
    @Override
    void addToList() {
        //<broadcast id="1391" site="ndrfernsehen">45 Min</broadcast>
        final String ADRESSE = "http://www.ndr.de/mediathek/sendungen_a-z/index.html";
        final String MUSTER_URL1 = "<li><a href=\"/mediathek/mediatheksuche105_broadcast-";
        listeThemen.clear();
        meldungStart();
        MSStringBuilder seite = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        seite = getUrlIo.getUri(SENDERNAME, ADRESSE, MSConst.KODIERUNG_UTF, 5 /* versuche */, seite, ""/* meldung */);
        int pos = 0;
        int pos1;
        int pos2;
        String url = "";
        String thema = "";
        while ((pos = seite.indexOf(MUSTER_URL1, pos)) != -1) {
            try {
                pos += MUSTER_URL1.length();
                pos1 = pos;
                if ((pos2 = seite.indexOf("\"", pos)) != -1) {
                    url = seite.substring(pos1, pos2);
                }
                pos1 = seite.indexOf(">", pos);
                pos2 = seite.indexOf("<", pos);
                if (pos1 != -1 && pos2 != -1 && pos1 < pos2) {
                    thema = seite.substring(pos1 + 1, pos2);
                }
                if (url.equals("")) {
                    MSLog.fehlerMeldung(210367600,  "keine Url");
                    continue;
                }
                String url_ = "http://www.ndr.de/mediathek/mediatheksuche105_broadcast-" + url;
                String[] add = new String[]{url_, thema};
                if (MSConfig.senderAllesLaden) {
                    if (!alleSeiteSuchen(url_, thema)) {
                        // dann halt so versuchen
                        listeThemen.addUrl(add);
                    }
                } else {
                    listeThemen.addUrl(add);
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(332945670,  ex);
            }
        }
        // noch "Verpasst" für die letzten Tage einfügen
        // http://www.ndr.de/mediathek/sendung_verpasst/epg1490_date-2014-05-17.html
        // http://www.ndr.de/mediathek/sendung_verpasst/epg1490_date-2014-05-17_display-onlyvideo.html
        SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formatter2 = new SimpleDateFormat("dd.MM.yyyy");
        int maxTage = MSConfig.senderAllesLaden ? 30 : 20;
        for (int i = 0; i < maxTage; ++i) {
            // https://www.ndr.de/mediathek/sendung_verpasst/epg1490_date-2015-09-05_display-all.html
            final String URL = "http://www.ndr.de/mediathek/sendung_verpasst/epg1490_date-";
            String tag = formatter1.format(new Date().getTime() - (1000 * 60 * 60 * 24 * i));
            String date = formatter2.format(new Date().getTime() - (1000 * 60 * 60 * 24 * i));
            //String urlString = URL + tag + "_display-onlyvideo.html";
            String urlString = URL + tag + "_display-all.html";
            listeThemen.addUrl(new String[]{urlString, date});
        }
        if (MSConfig.getStop()) {
            meldungThreadUndFertig();
        } else if (listeThemen.size() == 0) {
            meldungThreadUndFertig();
        } else {
            meldungAddMax(listeThemen.size());
            for (int t = 0; t < maxThreadLaufen; ++t) {
                Thread th = new Thread(new ThemaLaden());
                th.setName(SENDERNAME + t);
                th.start();
            }
        }
    }

    private boolean alleSeiteSuchen(String strUrlFeed, String tthema) {
        boolean ret = false;
        seiteAlle = getUrlIo.getUri(SENDERNAME, strUrlFeed, MSConst.KODIERUNG_UTF, 3 /* versuche */, seiteAlle, "Thema: " + tthema/* meldung */);
        int pos1 = 0, pos2, anz1, anz2 = 0;
        try {
            // <a class="square button" href="/mediathek/mediatheksuche105_broadcast-1391_page-5.html" title="Zeige Seite 5">
            // http://www.ndr.de/mediathek/mediatheksuche105_broadcast-30_page-1.html
            final String WEITER = " title=\"Zeige Seite ";
            while ((pos1 = seiteAlle.indexOf(WEITER, pos1)) != -1) {
                pos1 += WEITER.length();
                if ((pos2 = seiteAlle.indexOf("\"", pos1)) != -1) {
                    String anz = seiteAlle.substring(pos1, pos2);
                    try {
                        anz1 = Integer.parseInt(anz);
                        if (anz2 < anz1) {
                            anz2 = anz1;
                        }
                    } catch (Exception ex) {
                        MSLog.fehlerMeldung(643208979, strUrlFeed);
                    }
                }
            }
            for (int i = 2; i <= anz2 && i <= 10; ++i) {
                // geht bei 2 los da das ja schon die erste Seite ist!
                //das:   http://www.ndr.de/mediathek/mediatheksuche105_broadcast-30.html
                // wird: http://www.ndr.de/mediathek/mediatheksuche105_broadcast-30_page-3.html
                String url_ = strUrlFeed.replace(".html", "_page-" + i + ".html");
                listeThemen.addUrl(new String[]{url_, tthema});
                ret = true;
            }
        } catch (Exception ex) {
            MSLog.fehlerMeldung(913047821, strUrlFeed);
        }
        return ret;
    }

    private class ThemaLaden implements Runnable {

        MSGetUrl getUrl = new MSGetUrl(wartenSeiteLaden);
        private MSStringBuilder seite1 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder seite2 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);

        @Override
        public synchronized void run() {
            try {
                meldungAddThread();
                String[] link;
                while (!MSConfig.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    try {
                        meldungProgress(link[1]);
                        feedEinerSeiteSuchen(link[0], link[1] /* thema */);
                    } catch (Exception ex) {
                        MSLog.fehlerMeldung(336901211, ex);
                    }
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(554632590, ex);
            }
            meldungThreadUndFertig();
        }

        void feedEinerSeiteSuchen(String strUrlFeed, String tthema) {
            final String MUSTER_URL = "<a href=\"/";
            seite1 = getUrlIo.getUri(SENDERNAME, strUrlFeed, MSConst.KODIERUNG_UTF, 3 /* versuche */, seite1, "Thema: " + tthema/* meldung */);
            int pos = 0;
            String url;
            String titel;
            String thema = tthema;
            String datum = "";
            String zeit = "";
            long durationInSeconds = 0;
            String tmp;
            boolean tage = false;
            try {
                meldung(strUrlFeed);
                String muster;
                if (seite1.indexOf("<strong class=\"time\">") != -1) {
                    muster = "<strong class=\"time\">";
                    tage = true;
                } else {
                    muster = "<span class=\"icon icon_video\"></span>";
                }
                while (!MSConfig.getStop() && (pos = seite1.indexOf(muster, pos)) != -1) {
                    pos += muster.length();
                    url = seite1.extract(MUSTER_URL, "\"", pos);
                    if (url.equals("")) {
                        MSLog.fehlerMeldung(659210274, "keine Url feedEinerSeiteSuchen" + strUrlFeed);
                        continue;
                    }
                    url = "http://www.ndr.de/" + url;
                    if (tage) {
                        // <h3><a href="/fernsehen/epg/import/Rote-Rosen,sendung64120.html" title="Rote Rosen"  >Rote Rosen (1725)</a></h3>
                        thema = seite1.extract(MUSTER_URL, " title=\"", "\"", pos);
                        titel = seite1.extract(MUSTER_URL, ">", "<", pos);
                        if (titel.contains("(Wdh.)")) {
                            // dann sollte der Beitrag schon in der Liste sein
                            continue;
                        }
                        if (thema.equals(titel) && thema.contains(" - ")) {
                            thema = thema.substring(0, thema.indexOf(" - ")).trim();
                            titel = titel.substring(titel.indexOf(" - "));
                            titel = titel.replace(" - ", "").trim();
                        }
                    } else {
                        titel = seite1.extract(" title=\"", "\"", pos);
                        titel = titel.replace("Zum Video:", "").trim();
                    }
                    if (tage) {
                        tmp = seite1.substring(pos, seite1.indexOf("<", pos));
                        datum = tthema;
                        try {
                            SimpleDateFormat sdfIn = new SimpleDateFormat("HH:mm");
                            Date filmDate = sdfIn.parse(tmp);
                            zeit = new SimpleDateFormat("HH:mm:ss").format(filmDate);
                        } catch (Exception ex) {
                            MSLog.fehlerMeldung(795623017, "convertDatum: " + strUrlFeed);
                        }
                    } else {
                        tmp = seite1.extract("<div class=\"subline\">", "<", pos);
                        tmp = tmp.replace("Uhr", "").trim();
                        try {
                            SimpleDateFormat sdfIn = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                            Date filmDate = sdfIn.parse(tmp);
                            datum = new SimpleDateFormat("dd.MM.yyyy").format(filmDate);
                            zeit = new SimpleDateFormat("HH:mm:ss").format(filmDate);
                        } catch (Exception ex) {
                            MSLog.fehlerMeldung(623657941, "convertDatum: " + strUrlFeed);
                        }
                    }
                    if (tage) {
                        //<span class="icon icon_video" aria-label="L&auml;nge"></span>29:59</div>
                        String duration = seite1.extract("\"L&auml;nge\"></span>", "<", pos).trim();
                        try {
                            if (!duration.equals("")) {
                                String[] parts = duration.split(":");
                                long power = 1;
                                durationInSeconds = 0;
                                for (int i = parts.length - 1; i >= 0; i--) {
                                    durationInSeconds += Long.parseLong(parts[i]) * power;
                                    power *= 60;
                                }
                            }
                        } catch (Exception ex) {
                            MSLog.fehlerMeldung(369015497, ex, strUrlFeed);
                        }
                    } else {
                        String duration = seite1.extract("Video (", ")", pos);
                        duration = duration.replace("min", "").trim();
                        try {
                            if (!duration.equals("")) {
                                String[] parts = duration.split(":");
                                long power = 1;
                                durationInSeconds = 0;
                                for (int i = parts.length - 1; i >= 0; i--) {
                                    durationInSeconds += Long.parseLong(parts[i]) * power;
                                    power *= 60;
                                }
                            }
                        } catch (Exception ex) {
                            MSLog.fehlerMeldung(369015497, ex, strUrlFeed);
                        }
                    }
                    filmSuchen(strUrlFeed, thema, titel, url, datum, zeit, durationInSeconds, tage);
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(693219870, strUrlFeed);
            }
        }

        void filmSuchen(String strUrlThema, String thema, String titel, String filmWebsite, String datum, String zeit,
                long durationInSeconds, boolean onlyUrl) {
            //playlist: [
            //{
            //1: {src:'http://hds.ndr.de/z/2013/0419/TV-20130419-1010-0801.,hi,hq,.mp4.csmil/manifest.f4m', type:"application/f4m+xml"},
            //2: {src:'http://hls.ndr.de/i/2013/0419/TV-20130419-1010-0801.,lo,hi,hq,.mp4.csmil/master.m3u8', type:"application/x-mpegURL"},
            //3: {src:'http://media.ndr.de/progressive/2013/0419/TV-20130419-1010-0801.hi.mp4', type:"video/mp4"},

            // http://media.ndr.de/progressive/2012/0820/TV-20120820-2300-0701.hi.mp4
            // rtmpt://cp160844.edgefcs.net/ondemand/mp4:flashmedia/streams/ndr/2012/0820/TV-20120820-2300-0701.hq.mp4
            final String MUSTER_URL = "3: {src:'http://";
            seite2 = getUrl.getUri_Utf(SENDERNAME, filmWebsite, seite2, "strUrlThema: " + strUrlThema);
            String description = extractDescription(seite2);
            String[] keywords = extractKeywords(seite2);
            String subtitle = seite2.extract(",tracks: [{ src: \"", "\""); //,tracks: [{ src: "/fernsehen/sendungen/45_min/video-podcast/ut20448.xml", srclang:"de"}]
            if (!subtitle.isEmpty()) {
                subtitle = "http://www.ndr.de" + subtitle;
//            } else {
//                System.out.println("Test");
            }
            meldung(filmWebsite);
            int pos1;
            int pos2;
            String url;
            try {
                if ((pos1 = seite2.indexOf(MUSTER_URL)) != -1) {
                    pos1 += MUSTER_URL.length();
                    if ((pos2 = seite2.indexOf("'", pos1)) != -1) {
                        url = seite2.substring(pos1, pos2);
                        if (!url.equals("")) {
                            url = "http://" + url;
                            if (url.contains("http://media.ndr.de/progressive")) {
                                if (url.contains("hi.mp4")) {
                                    url = url.replace("hi.mp4", "hq.mp4");
                                }
                            }
                            if (thema.equals("")) {
                                thema = seite2.extract("<h1>", "<div class=\"subline\">", "<");
                                if (thema.contains("|")) {
                                    thema = thema.substring(0, thema.lastIndexOf("|"));
                                    thema = thema.trim();
                                }
                                if (thema.contains("-")) {
                                    thema = thema.substring(0, thema.lastIndexOf("-"));
                                    thema = thema.trim();
                                }
                                if (thema.contains("Uhr")) {
                                    thema = "";
                                }
                                if (thema.equals("")) {
                                    thema = "NDR";
                                }
                            }
                            DatenFilm film = new DatenFilm(SENDERNAME, thema, filmWebsite, titel, url, ""/*rtmpURL*/, datum, zeit, durationInSeconds, description);
                            film.addUrlSubtitle(subtitle);
                            if (url.contains(".hq.")) {
                                String urlKlein = url.replace(".hq.", ".hi.");
                                film.addUrlKlein(urlKlein, "");
                            }
                            addFilm(film, onlyUrl);
                        } else {
                            MSLog.fehlerMeldung(623657941, "keine URL: " + filmWebsite);
                        }
                    }
                } else {
                    MSLog.fehlerMeldung(698970145, "keine Url: " + filmWebsite);
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(699830157, ex);
            }
        }

        private String extractDescription(MSStringBuilder page) {
            String desc = extractString(page, "<meta property=\"og:description\" content=\"", "\"");
            if (desc == null) {
                return "";
            }
            return desc;
        }

        private String[] extractKeywords(MSStringBuilder page) {
            String keywords = extractString(page, "<meta name=\"keywords\"  lang=\"de\" content=\"", "\"");
            if (keywords == null) {
                return new String[]{""};
            }
            String[] k = keywords.split(",");
            for (int i = 0; i < k.length; i++) {
                k[i] = k[i].trim();
            }
            return k;
        }

        private String extractString(MSStringBuilder source, String startMarker, String endMarker) {
            int start = source.indexOf(startMarker);
            if (start == -1) {
                return null;
            }
            start = start + startMarker.length();
            int end = source.indexOf(endMarker, start);
            if (end == -1) {
                return null;
            }
            return source.substring(start, end);
        }
    }
}
