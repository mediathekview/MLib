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
import msearch.daten.MSConfig;
import msearch.filmeSuchen.MSFilmeSuchen;
import msearch.io.MSGetUrl;
import msearch.tool.MSConst;
import msearch.tool.MSLog;
import msearch.tool.MSStringBuilder;

/**
 *
 * @author
 */
public class MediathekArd extends MediathekReader implements Runnable {

    public static final String SENDER = "ARD";
    private static int wiederholungen1 = 0;
    private static int wiederholungen2 = 0;
    private static final int MAX_WIEDERHOLUNGEN = 50;
    MSStringBuilder seite_1 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);

    /**
     *
     * @param ssearch
     * @param startPrio
     */
    public MediathekArd(MSFilmeSuchen ssearch, int startPrio) {
        super(ssearch, /* name */ SENDER, /* threads */ 10, /* urlWarten */ 500, startPrio);
    }

    @Override
    void addToList() {
        wiederholungen1 = 0;
        wiederholungen2 = 0;
        final String ADRESSE = "http://www.ardmediathek.de/tv";
        final String MUSTER_URL = "<a href=\"/tv/sendungen-a-z?buchstabe=";
        listeThemen.clear();
        MSStringBuilder seite = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        meldungStart();
        seite = getUrlIo.getUri(nameSenderMReader, ADRESSE, MSConst.KODIERUNG_UTF, 5 /* versuche */, seite, "" /* Meldung */);
        if (seite.length() == 0) {
            MSLog.systemMeldung("ARD: Versuch 2");
            warten(2 * 60 /*Sekunden*/);
            seite = getUrlIo.getUri(nameSenderMReader, ADRESSE, MSConst.KODIERUNG_UTF, 5 /* versuche */, seite, "" /* Meldung */);
            if (seite.length() == 0) {
                MSLog.fehlerMeldung(-104689736, MSLog.FEHLER_ART_MREADER, "MediathekArd.addToList", "wieder nichts gefunden");
            }
        }
        int pos = 0;
        int pos1;
        int pos2;
        String url = "";
        while ((pos = seite.indexOf(MUSTER_URL, pos)) != -1) {
            try {
                pos += MUSTER_URL.length();
                pos1 = pos;
                pos2 = seite.indexOf("\"", pos);
                if (pos1 != -1 && pos2 != -1) {
                    url = seite.substring(pos1, pos2);
                }
                if (url.equals("")) {
                    continue;
                }
                url = "http://www.ardmediathek.de/tv/sendungen-a-z?buchstabe=" + url;
                feedSuchen1(url);
            } catch (Exception ex) {
                MSLog.fehlerMeldung(-698732167, MSLog.FEHLER_ART_MREADER, "MediathekArd.addToList", ex, "kein Thema");
            }
        }
        if (MSConfig.getStop()) {
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

    private void feedSuchen1(String strUrlFeed) {
        final String MUSTER = "<div class=\"media mediaA\">";
        seite_1 = getUrlIo.getUri(nameSenderMReader, strUrlFeed, MSConst.KODIERUNG_UTF, (wiederholungen1 < MAX_WIEDERHOLUNGEN ? 2 : 1)/*max Versuche*/, seite_1, "");
        if (seite_1.length() == 0) {
            MSLog.fehlerMeldung(-207956317, MSLog.FEHLER_ART_MREADER, "MediathekArd.feedSuchen", "Leere Seite: " + strUrlFeed);
            return;
        }
        int pos;
        String url, thema;
        long d = 0;
        pos = 0;
        pos = seite_1.indexOf(MUSTER);
        pos += MUSTER.length();
        while (!MSConfig.getStop() && (pos = seite_1.indexOf(MUSTER, pos)) != -1) {
            try {
                pos += MUSTER.length();
                url = seite_1.extract("<a href=\"/tv/", "\"", pos);
                if (url.equals("")) {
                    continue;
                }
                url = "http://www.ardmediathek.de/tv/" + url;
                thema = seite_1.extract("title=\"", "\"", pos);
                String[] add = new String[]{ url, thema};
                listeThemen.addUrl(add);
            } catch (Exception ex) {
                MSLog.fehlerMeldung(-732154698, MSLog.FEHLER_ART_MREADER, "MediathekArd.feedSuchen", ex, "Weitere Seiten suchen");
            }
        }
    }

    private synchronized void warten(int i) {
        // Sekunden warten
        try {
            // war wohl nix, warten und dann nochmal
            // timeout: the maximum time to wait in milliseconds.
            long warten = i * 1000;
            this.wait(warten);
        } catch (Exception ex) {
            MSLog.fehlerMeldung(-369502367, MSLog.FEHLER_ART_MREADER, "MediathekArd.warten", ex, "2. Versuch");
        }
    }

    private class ThemaLaden implements Runnable {

        MSGetUrl getUrl = new MSGetUrl(wartenSeiteLaden);

        public ThemaLaden() {
        }
        private MSStringBuilder seite1 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder seiteWeiter = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder seite2 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);
        private MSStringBuilder seite3 = new MSStringBuilder(MSConst.STRING_BUFFER_START_BUFFER);

        @Override
        public synchronized void run() {
            try {
                meldungAddThread();
                String[] link;
                while (!MSConfig.getStop() && (link = getListeThemen()) != null) {
                    meldungProgress(link[0]);
                    feedSuchen2(link[0] /* url */, link[1]);
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(-487326921, MSLog.FEHLER_ART_MREADER, "MediathekArdThemaLaden.run", ex);
            }
            meldungThreadUndFertig();
        }

//        private void feedSuchen1(String strUrlFeed) {
//            final String MUSTER = "<div class=\"media mediaA\">";
//            seite1 = getUrl.getUri(nameSenderMReader, strUrlFeed, MSConst.KODIERUNG_UTF, (wiederholungen1 < MAX_WIEDERHOLUNGEN ? 2 : 1)/*max Versuche*/, seite1, "");
//            if (seite1.length() == 0) {
//                MSLog.fehlerMeldung(-207956317, MSLog.FEHLER_ART_MREADER, "MediathekArd.feedSuchen", "Leere Seite: " + strUrlFeed);
//                return;
//            }
//            int pos;
//            String url, thema;
//            long d = 0;
//            pos = 0;
//            pos = seite1.indexOf(MUSTER);
//            pos += MUSTER.length();
//            while (!MSConfig.getStop() && (pos = seite1.indexOf(MUSTER, pos)) != -1) {
//                try {
//                    pos += MUSTER.length();
//                    url = seite1.extract("<a href=\"/tv/", "\"", pos);
//                    if (url.equals("")) {
//                        continue;
//                    }
//                    url = "http://www.ardmediathek.de/tv/" + url;
//                    thema = seite1.extract("title=\"", "\"", pos);
//                    feedSuchen2(url, thema);
//                } catch (Exception ex) {
//                    MSLog.fehlerMeldung(-732154698, MSLog.FEHLER_ART_MREADER, "MediathekArd.feedSuchen", ex, "Weitere Seiten suchen");
//                }
//            }
//        }

        private void feedSuchen2(String strUrlFeed, String thema) {
            final String MUSTER = "<div class=\"media mediaA\">";
            final String MUSTER_START = "<h2 class=\"modHeadline\">Videos und Audios der Sendung</h2>";
            seite2 = getUrl.getUri(nameSenderMReader, strUrlFeed, MSConst.KODIERUNG_UTF, (wiederholungen1 < MAX_WIEDERHOLUNGEN ? 2 : 1)/*max Versuche*/, seite2, "");
            if (seite2.length() == 0) {
                MSLog.fehlerMeldung(-765323214, MSLog.FEHLER_ART_MREADER, "MediathekArd.feedSuchen", "Leere Seite: " + strUrlFeed);
                return;
            }
            int pos;
            String url, datum, zeit = "", titel, dauer, urlSendung;
            long d = 0;
            pos = 0;
            pos = seite2.indexOf(MUSTER_START);
            pos += MUSTER_START.length();
            while (!MSConfig.getStop() && (pos = seite2.indexOf(MUSTER, pos)) != -1) {
                pos += MUSTER.length();
                url = seite2.extract("documentId=", "&", pos);
                if (url.equals("")) {
                    continue;
                }
                datum = seite2.extract("<p class=\"dachzeile\">", "<", pos);
                datum = datum.replace("Uhr", "").trim();
                if (datum.contains("|")) {
                    zeit = datum.substring(datum.indexOf("|") + 1).trim();
                    zeit = zeit + ":00";
                    datum = datum.substring(0, datum.indexOf("|")).trim();
                }
                titel = seite2.extract("<h4 class=\"headline\">", "<", pos);
                dauer = seite2.extract("<p class=\"subtitle\">", "<", pos);
                dauer = dauer.replace("min", "").trim();
                try {
                    if (dauer.contains(":")) {
                        String s = dauer.substring(0, dauer.indexOf(":"));
                        d = Long.parseLong(s);
                        d *= 60;
                        s = dauer.substring(dauer.indexOf(":") + 1);
                        d += Long.parseLong(s);
                    }
                } catch (Exception ex) {
                }
                urlSendung = seite2.extract("<a href=\"/tv/", "\"", pos);
                if (!urlSendung.isEmpty()) {
                    urlSendung = "http://www.ardmediathek.de/tv/" + urlSendung;
                }
                filmSuchen(url, thema, titel, d, datum, zeit, urlSendung);
            }
        }

        private void filmSuchen(String urlFilm_, String thema, String titel, long dauer, String datum, String zeit, String urlSendung) {
            // URL bauen: http://www.ardmediathek.de/play/media/21528242?devicetype=pc&features=flash
            String urlFilm = "http://www.ardmediathek.de/play/media/" + urlFilm_ + "?devicetype=pc&features=flash";
            meldung(urlFilm);
            seite3 = getUrl.getUri_Utf(nameSenderMReader, urlFilm, seite3, "");
            if (seite3.length() == 0) {
                MSLog.fehlerMeldung(-915263621, MSLog.FEHLER_ART_MREADER, "MediathekArd.feedSuchen", "Leere Seite: " + urlFilm);
                return;
            }
            String url, urlKl;
            urlKl = seite3.extract("flashUrl\":false,\"valid\":true,\"_quality\":1", "\"_stream\":\"", "\"");
            url = seite3.extract("flashUrl\":false,\"valid\":true,\"_quality\":3", "\"_stream\":\"", "\"");
            if (!url.isEmpty()) {
                DatenFilm f = new DatenFilm(nameSenderMReader, thema, urlSendung, titel, url, ""/*urlRtmp*/, datum, zeit, dauer, "" /*description*/,
                        "" /*imageUrl*/, new String[]{}/*keywords*/);
                if (!urlKl.isEmpty()) {
                    f.addUrlKlein(urlKl, "");
                }
                addFilm(f);
            } else {
                urlFilm = "http://www.ardmediathek.de/play/media/" + urlFilm_ + "?devicetype=pc&features";
                seite3 = getUrl.getUri_Utf(nameSenderMReader, urlFilm, seite3, "");
                if (seite3.length() == 0) {
                    MSLog.fehlerMeldung(-915263621, MSLog.FEHLER_ART_MREADER, "MediathekArd.feedSuchen", "Leere Seite: " + urlFilm);
                    return;
                }
                url = seite3.extract("_quality\":1", "_stream\":\"", "\"");
                if (!url.isEmpty()) {
                    DatenFilm f = new DatenFilm(nameSenderMReader, thema, urlSendung, titel, url, ""/*urlRtmp*/, datum, zeit, dauer, "" /*description*/,
                            "" /*imageUrl*/, new String[]{}/*keywords*/);
                    addFilm(f);
                } else {
                    MSLog.fehlerMeldung(-784512369, MSLog.FEHLER_ART_MREADER, "MediathekArd.feedSuchen", "keine URL: " + urlFilm);
                }
            }
        }

        private boolean feedEinerSeiteSuchen(MSStringBuilder seite, String strUrlFeed, String thema) {
            //url: http://www.ardmediathek.de/ard/servlet/ajax-cache/3516962/view=list/documentId=443668/index.html
            //Feed eines Themas laden
            //<h3 class="mt-title"><a href="/ard/servlet/content/3517136?documentId=3743644"

            //<a href="/ard/servlet/ajax-cache/3516938/view=switch/documentId=3398614/index.html" class="mt-box_preload mt-box-overflow"></a>
            //weitere Seiten:
            //wenn vorhanden: <option value="
            //<option value="/ard/servlet/ajax-cache/3516962/view=list/documentId=1175574/goto=2/index.html">2</option>
            //<span class="mt-airtime">22.11.10
            //<span class="mt-icon mt-icon-toggle_arrows"></span>              Sendung vom 22.11.10 | 23:00            </a>
            boolean ret = false;
            boolean gefunden = false;
            final String TITEL = "\">";
            //final String MUSTER = "<h3 class=\"mt-title\"><a href=\"";
            final String MUSTER = "<a href=\"";
            final String MUSTER_SET = "http://www.ardmediathek.de";
            final String MUSTER_DATUM_1 = "<span class=\"mt-icon mt-icon-toggle_arrows\"></span>";
            final String MUSTER_DATUM_2 = "</a>";
            int pos;
            int posDatum1 = 0;
            int posDatum2;
            int pos1;
            int pos2;
            String url = "";
            String datum = "";
            String zeit = "";
            String tmp;
            //erst mal nach weitern Seiten schauen
            while (!MSConfig.getStop() && (posDatum1 = seite.indexOf(MUSTER_DATUM_1, posDatum1)) != -1) {
                posDatum1 += MUSTER_DATUM_1.length();
                if ((pos1 = seite.indexOf(MUSTER_DATUM_2, posDatum1)) != -1) {
                    tmp = seite.substring(posDatum1, pos1).trim();
                    if (tmp.contains("Sendung vom")) {
                        tmp = tmp.replace("Sendung vom", "");
                    }
                    if (tmp.contains("|")) {
                        datum = tmp.substring(0, tmp.indexOf("|")).trim();
                        if (datum.length() == 8) {
                            datum = datum.substring(0, 6) + "20" + datum.substring(6);
                        }
                        zeit = tmp.substring(tmp.indexOf("|") + 1) + ":00";
                        zeit = zeit.replace("Uhr", "");
                        zeit = zeit.replace(" ", "");
                    }
                }
                pos = posDatum1;
                posDatum2 = seite.indexOf(MUSTER_DATUM_1, posDatum1);
                while (!MSConfig.getStop() && (pos = seite.indexOf(MUSTER, pos)) != -1) {
                    if (posDatum2 != -1) {
                        // nur im Bereich des Datums suchen
                        if (pos > posDatum2) {
                            break;
                        }
                    }
                    try {
                        pos += MUSTER.length();
                        pos1 = pos;
                        pos2 = seite.indexOf("\"", pos);
                        if (pos1 != -1 && pos2 != -1) {
                            url = seite.substring(pos1, pos2);
                        }
                        if (url.equals("")) {
                            continue;
                        }
                        gefunden = true;
                        ret = filmLaden(strUrlFeed, MUSTER_SET + url, thema, datum, zeit);
                    } catch (Exception ex) {
                        MSLog.fehlerMeldung(-321648296, MSLog.FEHLER_ART_MREADER, "MediathekArd.feedEinerSeiteSuchen-1", ex, "Thema hat keine Links");
                    }
                }
            }
            if (!gefunden) {
                //dann nochmal ohne Datum
                //07.10.10
                final String DAT = "<span class=\"mt-airtime\">";
                pos = 0;
                while (!MSConfig.getStop() && (pos = seite.indexOf(MUSTER, pos)) != -1) {
                    try {
                        pos += MUSTER.length();
                        pos1 = pos;
                        pos2 = seite.indexOf("\"", pos);
                        if (pos1 != -1 && pos2 != -1) {
                            url = seite.substring(pos1, pos2);
                        }
                        if (url.equals("")) {
                            continue;
                        }
                        // noch das Datum versuchen
                        if ((pos1 = seite.indexOf(DAT, pos)) != -1) {
                            pos1 += DAT.length();
                            if ((pos2 = seite.indexOf("<", pos1)) != -1) {
                                datum = seite.substring(pos1, pos2);
                                if (datum.length() > 10) {
                                    datum = datum.substring(0, 9);
                                    datum = datum.substring(0, 6) + "20" + datum.substring(6);
                                }
                            }
                        }
                        ret = filmLaden(strUrlFeed, MUSTER_SET + url, thema, datum, zeit);
                    } catch (Exception ex) {
                        MSLog.fehlerMeldung(-487369532, MSLog.FEHLER_ART_MREADER, "MediathekArd.feedEinerSeiteSuchen-2", ex, "Thema hat keine Links");
                    }
                }

            }
            return ret;
        }

        boolean filmLaden(String urlFeed, String filmWebsite, String thema, String datum, String zeit) {
            // mediaCollection.addMediaStream(0, 0, "", "http://http-ras.wdr.de/CMS2010/mdb/14/148362/ichwillnichtlaengerschweigen_1460800.mp4", "default");
            // mediaCollection.addMediaStream(0, 1, "rtmp://gffstream.fcod.llnwd.net/a792/e2/", "mp4:CMS2010/mdb/14/148362/ichwillnichtlaengerschweigen_1460799.mp4", "limelight");
            // mediaCollection.addMediaStream(0, 2, "rtmp://gffstream.fcod.llnwd.net/a792/e2/", "mp4:CMS2010/mdb/14/148362/ichwillnichtlaengerschweigen_1460798.mp4", "limelight");
            //
            // mediaCollection.addMediaStream(1, 0, "", "http://http-ras.wdr.de/CMS2010/mdb/14/148362/ichwillnichtlaengerschweigen_1460800.mp4", "default");
            // mediaCollection.addMediaStream(1, 1, "", "http://http-ras.wdr.de/CMS2010/mdb/14/148362/ichwillnichtlaengerschweigen_1460799.mp4", "default");
            // mediaCollection.addMediaStream(1, 2, "", "http://http-ras.wdr.de/CMS2010/mdb/14/148362/ichwillnichtlaengerschweigen_1460798.mp4", "default");
            // mediaCollection.addMediaStream(0, 1, "", "http://www.hr.gl-systemhaus.de/flash/fs/buchmesse2011/20111015_tilmankat.flv", "default");

            //flvstreamer --host vod.daserste.de --app ardfs --playpath mp4:videoportal/Film/c_100000/106579/format106899.f4v > bla.flv
            boolean flash;
            String protokoll = "";
            meldung(filmWebsite);
            seite3 = getUrl.getUri(nameSenderMReader, filmWebsite, MSConst.KODIERUNG_UTF, (wiederholungen2 < MAX_WIEDERHOLUNGEN ? 2 : 1)/*max Versuche*/, seite3, "urlFeed: " + urlFeed);
            if (seite3.length() == 0) {
                MSLog.fehlerMeldung(-201549307, MSLog.FEHLER_ART_MREADER, "MediathekArd.filmLaden", "leere Seite: " + filmWebsite);
                return false;
            }
            long durationInSeconds = extractDuration(seite3);
            String description = extractDescription(seite3);
            String[] keywords = extractKeywords(seite3);
            String thumbnailUrl = extractThumbnailURL(seite3);
            String imageUrl = extractImageURL(seite3);
            // String titel = seite2.extract("<meta property=\"og:title\" content=\"Video &#034;", "&#034;");
            // if (titel.isEmpty()) {
            //    titel = seite2.extract("<meta property=\"og:title\" content=\"Video &#034;&#034;", "&#034;");
            //    if (titel.isEmpty()) {
            //       Log.fehlerMeldung(-989301245, Log.FEHLER_ART_MREADER, "MediathekArd.filmLaden", "kein Titel: " + filmWebsite);
            //    }
            // }
            String titel = seite3.extract("<meta name=\"dcterms.title\" content=\"", "\"");
            if (titel.contains("&#034;")) {
                titel = titel.replaceAll("&#034;", "");
            }
            if (titel.isEmpty()) {
                MSLog.fehlerMeldung(-989301245, MSLog.FEHLER_ART_MREADER, "MediathekArd.filmLaden", "kein Titel: " + filmWebsite);
            }
            int pos1;
            DatenFilm f = new DatenFilm(nameSenderMReader, thema, filmWebsite, titel, ""/*urlOrg*/, ""/*urlRtmp*/, datum, zeit, durationInSeconds, description,
                    imageUrl.isEmpty() ? thumbnailUrl : imageUrl, keywords);
            // ###############################
            // reguläre Filme, kein Flash
            final String MUSTER_URL3a = "mediaCollection.addMediaStream(1, 3, \"\", \"http://";
            final String MUSTER_URL3b = "mediaCollection.addMediaStream(1, 2, \"\", \"http://";
            final String MUSTER_URL3c = "mediaCollection.addMediaStream(1, 1, \"\", \"http://";
            final String MUSTER_URL3d = "mediaCollection.addMediaStream(0, 1, \"\", \"http://";
            final String MUSTER_URL3e = "mediaCollection.addMediaStream(1, 0, \"\", \"http://";
            protokoll = "http://";
            flash = false;
            if ((pos1 = seite3.indexOf(MUSTER_URL3a)) != -1) {
                pos1 += MUSTER_URL3a.length();
                if (filmHolen(f, flash, pos1, protokoll, true /*noch 2. suchen*/)) {
                    addFilm(f);
                    return true;
                }
            }
            if ((pos1 = seite3.indexOf(MUSTER_URL3b)) != -1) {
                pos1 += MUSTER_URL3b.length();
                if (filmHolen(f, flash, pos1, protokoll, true /*noch 2. suchen*/)) {
                    addFilm(f);
                    return true;
                }
            }
            if ((pos1 = seite3.indexOf(MUSTER_URL3c)) != -1) {
                pos1 += MUSTER_URL3c.length();
                if (filmHolen(f, flash, pos1, protokoll, false /*noch 2. suchen*/)) {
                    addFilm(f);
                    return true;
                }
            }
            if ((pos1 = seite3.indexOf(MUSTER_URL3d)) != -1) {
                pos1 += MUSTER_URL3d.length();
                if (filmHolen(f, flash, pos1, protokoll, false /*noch 2. suchen*/)) {
                    addFilm(f);
                    return true;
                }
            }
            if ((pos1 = seite3.indexOf(MUSTER_URL3e)) != -1) {
                pos1 += MUSTER_URL3e.length();
                if (filmHolen(f, flash, pos1, protokoll, false /*noch 2. suchen*/)) {
                    addFilm(f);
                    return true;
                }
            }
            flash = true;
            protokoll = "rtmp"; // gibt "rtmp://" UND "rtmpt://"
            final String MUSTER_URL1a = "mediaCollection.addMediaStream(0, 3, \"rtmp";
            final String MUSTER_URL1b = "mediaCollection.addMediaStream(0, 2, \"rtmp";
            final String MUSTER_URL1c = "mediaCollection.addMediaStream(0, 1, \"rtmp";
            final String MUSTER_URL1d = "mediaCollection.addMediaStream(0, 0, \"rtmp";
            if ((pos1 = seite3.indexOf(MUSTER_URL1a)) != -1) {
                pos1 += MUSTER_URL1a.length();
                if (filmHolen(f, flash, pos1, protokoll, true /*noch 2. suchen*/)) {
                    addFilm(f);
                    return true;
                }
            }
            if ((pos1 = seite3.indexOf(MUSTER_URL1b)) != -1) {
                pos1 += MUSTER_URL1b.length();
                if (filmHolen(f, flash, pos1, protokoll, true /*noch 2. suchen*/)) {
                    addFilm(f);
                    return true;
                }
            }
            if ((pos1 = seite3.indexOf(MUSTER_URL1c)) != -1) {
                pos1 += MUSTER_URL1c.length();
                if (filmHolen(f, flash, pos1, protokoll, false /*noch 2. suchen*/)) {
                    addFilm(f);
                    return true;
                }
            }
            if ((pos1 = seite3.indexOf(MUSTER_URL1d)) != -1) {
                pos1 += MUSTER_URL1d.length();
                if (filmHolen(f, flash, pos1, protokoll, false /*noch 2. suchen*/)) {
                    addFilm(f);
                    return true;
                }
            }
            if (f.arr[DatenFilm.FILM_URL_NR].equals("")) {
                if (seite3.indexOf("Der Clip ist deshalb nur von 20 bis 6 Uhr verfügbar") == -1
                        && seite3.indexOf("Der Clip ist deshalb nur von 22 bis 6 Uhr verfügbar") == -1) {
                    MSLog.fehlerMeldung(-159873540, MSLog.FEHLER_ART_MREADER, "MediathekArd.filmLaden", "keine Url für: " + filmWebsite + " Flash: " + flash);
                }
            } else {
                // dann fehlt nur die kleine URL
                addFilm(f);
            }
            return false;
        }

        private boolean filmHolen(DatenFilm f, boolean flash, int pos1, String protokoll, boolean zweitenSuchen) {
            // mediaCollection.addMediaStream(0, 2, "rtmp://gffstream.fcod.llnwd.net/a792/e2/", "mp4:CMS2010/mdb/14/148362/ichwillnichtlaengerschweigen_1460798.mp4", "limelight");
            // mediaCollection.addMediaStream(1, 0, "", "http://http-ras.wdr.de/CMS2010/mdb/14/148362/ichwillnichtlaengerschweigen_1460800.mp4", "default");
            int pos2, pos3;
            String url1a, url1b = "", url = "";
            String urlOrg = "", urlRtmp = "";
            if (flash) {
                if ((pos2 = seite3.indexOf("\"", pos1)) != -1) {
                    url1a = seite3.substring(pos1, pos2);
                    if (!url1a.equals("")) {
                        if (url1a.contains("//")) {
                            protokoll = protokoll + url1a.substring(0, url1a.indexOf("//") + 2);
                            url1a = url1a.substring(url1a.indexOf("//") + 2);
                        }
                        url1b = url1a.substring(url1a.indexOf("/") + 1);
                        url1a = url1a.substring(0, url1a.indexOf("/"));
                        urlOrg = addsUrl(protokoll + url1a, url1b);
                    }
                    pos1 = pos2 + 1;
                    //wenn eine url gefunden, dann ...
                    pos1 = seite3.indexOf("\"", pos1) + 1; //Anfang
                    pos2 = seite3.indexOf("?", pos1); //entweder Ende
                    pos3 = seite3.indexOf("\"", pos1); // oder da zu Ende
                    if (pos2 < pos3) {
                        if (pos1 > 1 && pos2 != -1) {
                            url = seite3.substring(pos1, pos2);
                        }
                    } else {
                        if (pos1 > 1 && pos3 != -1) {
                            url = seite3.substring(pos1, pos3);
                        }
                    }
                    urlOrg = addsUrl(urlOrg, url);
                    //gibt immer wieder URLs mit Leerzeichen
                    url1a = url1a.replace(" ", "");
                    url1b = url1b.replace(" ", "");
                    url = url.replace(" ", "");
                    urlRtmp = "--host " + url1a + " --app " + url1b + " --playpath " + url;
                    //flvstreamer --host vod.daserste.de --app ardfs --playpath mp4:videoportal/Film/c_100000/106579/format106899.f4v > bla.flv
                    //DatenFilm(Daten ddaten, String ssender, String tthema, String urlThema, String ttitel, String uurl, String uurlorg, String uurlRtmp, String zziel)
                }
            } else {
                if ((pos2 = seite3.indexOf("\"", pos1)) != -1) {
                    url = seite3.substring(pos1, pos2);
                    if (!url.equals("")) {
                        urlOrg = protokoll + url;
                    }
                }
                if (urlOrg.contains(".csmil/master.m3u8")) {
                    // m3uUrlHolen(GetUrl getUrl, String sender, MVStringBuilder strBuffer, String urlM3u) {
                    urlOrg = m3uUrlHolen(getUrl, nameSenderMReader, seite3, urlOrg);
//                    f.arr[DatenFilm.FILM_TITEL_NR] = f.arr[DatenFilm.FILM_TITEL_NR];
                }
            }
            urlOrg = urlOrg.replace(" ", "");
            if (flash && !urlOrg.equals("") && !urlRtmp.equals("") || !flash && !urlOrg.equals("")) {
                if (f.arr[DatenFilm.FILM_URL_NR].equals("")) {
                    // dann zuerst die normale URL füllen
                    f.arr[DatenFilm.FILM_URL_NR] = urlOrg;
                    f.arr[DatenFilm.FILM_URL_RTMP_NR] = urlRtmp;
                    if (zweitenSuchen) {
                        return false;
                    } else {
                        return true;
                    }
                } else {
                    // als 2. URL einfügen
                    f.addUrlKlein(urlOrg, urlRtmp);
                    return true;
                }
            }
            return false;
        }

        private synchronized String[] getListeThemen() {
            return listeThemen.pollFirst();
        }

        private long extractDuration(MSStringBuilder page) {
            String duration = extractString(page, "<meta property=\"video:duration\" content=\"", "\"");
            if (duration == null) {
                return 0;
            }
            try {
                return Long.parseLong(duration);
            } catch (Exception ex) {
                return 0;
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
            String keywords = extractString(page, "<meta name=\"keywords\" content=\"", "\"");
            if (keywords == null) {
                return new String[]{""};
            }

            return keywords.split(", ");
        }

        private String extractThumbnailURL(MSStringBuilder page) {
            return extractString(page, "<meta itemprop=\"thumbnailURL\" content=\"", "\"");
        }

        private String extractImageURL(MSStringBuilder page) {
            return extractString(page, "<meta property=\"og:image\" content=\"", "\"");
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

        private String m3uUrlHolen(MSGetUrl getUrl, String sender, MSStringBuilder strBuffer, String urlM3u) {
            // #EXTM3U
            // #EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=208000,RESOLUTION=288x216,CODECS="avc1.66.30, mp4a.40.2"
            // index_1_av.m3u8?e=b471643725c47acd
            // #EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=146000,RESOLUTION=192x144,CODECS="avc1.66.30, mp4a.40.2"
            // index_0_av.m3u8?e=b471643725c47acd
            // #EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=883000,RESOLUTION=384x288,CODECS="avc1.77.30, mp4a.40.2"
            // index_2_av.m3u8?e=b471643725c47acd
            // #EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=45000,CODECS="mp4a.40.2"
            // index_0_a.m3u8?e=b471643725c47acd

            // m3u-URL:
            // http://cdn-vod-ios.br.de/i/mir-live/bw1XsLz.......A,B,.mp4.csmil/master.m3u8?__b__=200
            final String URL = "index_2_av";
            final String CSMIL = "csmil/";
            String url = "";
            if (urlM3u.contains(CSMIL)) {
                url = urlM3u.substring(0, urlM3u.indexOf(CSMIL)) + CSMIL;
            } else {
                MSLog.fehlerMeldung(-730976432, MSLog.FEHLER_ART_MREADER, MediathekArd.class.getName() + ".m3uUrlHolen", "url: " + urlM3u);
                return "";
            }
            strBuffer = getUrl.getUri_Utf(sender, urlM3u, strBuffer, "url: " + urlM3u);
            if (strBuffer.length() == 0) {
                MSLog.fehlerMeldung(-963215478, MSLog.FEHLER_ART_MREADER, MediathekArd.class.getName() + ".m3uUrlHolen", "url: " + urlM3u);
                return "";
            }
            url = url + URL + strBuffer.extract(URL, "\n");
            return url;
        }
    }

    @Override
    void addFilm(DatenFilm film) {
        if (film.arr[DatenFilm.FILM_URL_NR].endsWith("?null=")) {
            film.arr[DatenFilm.FILM_URL_NR] = film.arr[DatenFilm.FILM_URL_NR].substring(0, film.arr[DatenFilm.FILM_URL_NR].indexOf("?null="));
        }
        if (film.arr[DatenFilm.FILM_URL_KLEIN_NR].endsWith("?null=")) {
            film.arr[DatenFilm.FILM_URL_KLEIN_NR] = film.arr[DatenFilm.FILM_URL_KLEIN_NR].substring(0, film.arr[DatenFilm.FILM_URL_KLEIN_NR].indexOf("?null="));
        }
        super.addFilm(film);
    }

}
