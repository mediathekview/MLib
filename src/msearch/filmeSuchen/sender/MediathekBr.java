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
import java.util.Locale;
import msearch.daten.DatenFilm;
import msearch.daten.MSearchConfig;
import msearch.filmeSuchen.MSearchFilmeSuchen;
import msearch.io.MSearchGetUrl;
import msearch.tool.MSearchConst;
import msearch.tool.MSearchLog;
import msearch.tool.MSearchStringBuilder;

public class MediathekBr extends MediathekReader implements Runnable {

    public static final String SENDER = "BR";
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.ENGLISH);//08.11.2013, 18:00
    private final SimpleDateFormat sdfOutTime = new SimpleDateFormat("HH:mm:ss");
    private final SimpleDateFormat sdfOutDay = new SimpleDateFormat("dd.MM.yyyy");
    LinkedListUrl listeTage = new LinkedListUrl();

    public MediathekBr(MSearchFilmeSuchen ssearch, int startPrio) {
        super(ssearch, /* name */ SENDER, /* threads */ 3, /* urlWarten */ 100, startPrio);
    }

    @Override
    void addToList() {
        final String ADRESSE = "http://www.br.de/mediathek/video/sendungen/index.html";
        final String MUSTER_URL = "<a href=\"/mediathek/video/sendungen/";
        listeThemen.clear();
        MSearchStringBuilder seite = new MSearchStringBuilder(MSearchConst.STRING_BUFFER_START_BUFFER);
        meldungStart();
        seite = getUrlIo.getUri_Utf(nameSenderMReader, ADRESSE, seite, "");
        int pos1 = 0;
        int pos2;
        String url = "";
        if ((pos1 = seite.indexOf("<ul class=\"clearFix\">")) != -1) {
            while ((pos1 = seite.indexOf(MUSTER_URL, pos1)) != -1) {
                try {
                    pos1 += MUSTER_URL.length();
                    if ((pos2 = seite.indexOf("\"", pos1)) != -1) {
                        url = seite.substring(pos1, pos2);
                    }
                    if (url.equals("")) {
                        continue;
                    }
                    /// der BR ist etwas zu langsam dafür????
//                    // in die Liste eintragen
//                    String[] add;
//                    if (MSearchConfig.senderAllesLaden) {
//                        add = new String[]{"http://www.br.de/mediathek/video/sendungen/" + url + "#seriesMoreCount=10", ""};
//                    } else {
//                        add = new String[]{"http://www.br.de/mediathek/video/sendungen/" + url, ""};
//                    }
                    // in die Liste eintragen
                    String[] add = new String[]{"http://www.br.de/mediathek/video/sendungen/" + url, ""};
                    listeThemen.addUrl(add);
                } catch (Exception ex) {
                    MSearchLog.fehlerMeldung(-821213698, MSearchLog.FEHLER_ART_MREADER, this.getClass().getSimpleName(), ex);
                }
            }
        }
        getTage();
        if (MSearchConfig.senderAllesLaden) {
            Thread thArchiv;
            thArchiv = new Thread(new ArchivLaden(1, 100));
            thArchiv.setName(nameSenderMReader);
            thArchiv.start();
            thArchiv = new Thread(new ArchivLaden(101, 200));
            thArchiv.setName(nameSenderMReader);
            thArchiv.start();
            thArchiv = new Thread(new ArchivLaden(201, 300));
            thArchiv.setName(nameSenderMReader);
            thArchiv.start();
            thArchiv = new Thread(new ArchivLaden(301, 400));
            thArchiv.setName(nameSenderMReader);
            thArchiv.start();
        }
        if (MSearchConfig.getStop()) {
            meldungThreadUndFertig();
        } else if (listeThemen.isEmpty() && listeTage.isEmpty()) {
            meldungThreadUndFertig();
        } else {
            meldungAddMax(listeThemen.size() + listeTage.size());
            for (int t = 0; t < maxThreadLaufen; ++t) {
                Thread th = new Thread(new ThemaLaden());
                th.setName(nameSenderMReader + t);
                th.start();
            }
        }
    }

    private void getTage() {
        // <a href="/mediathek/video/programm/mediathek-programm-100~_date-2014-01-05_-fc34efea1ee1bee90b0dc7888e292676f347679c.html" class="dayChange link_indexPage contenttype_epg mediathek-programm-100" data-
        // <a href="/mediathek/video/stadtlapelle-frankenland-100.html" title="zur Video-Detailseite" class="link_video contenttype_standard stadtlapelle-frankenland-100">

        String date;
        final String ADRESSE = "http://www.br.de/mediathek/video/programm/index.html";
        final String MUSTER = "http://www.br.de/mediathek/video/programm/mediathek-programm-100~_date-";
        listeTage.clear();
        MSearchStringBuilder seite1 = new MSearchStringBuilder(MSearchConst.STRING_BUFFER_START_BUFFER);
        MSearchStringBuilder seite2 = new MSearchStringBuilder(MSearchConst.STRING_BUFFER_START_BUFFER);
        ArrayList<String> al = new ArrayList<>();
        try {
            seite1 = getUrlIo.getUri_Utf(nameSenderMReader, ADRESSE, seite1, "");
            String url;
            int max_;
            if (MSearchConfig.senderAllesLaden) {
                max_ = 21;
            } else {
                max_ = 7;
            }
            for (int i = 0; i < max_; ++i) {
                if ((MSearchConfig.getStop())) {
                    break;
                }
                date = new SimpleDateFormat("yyyy-MM-dd").format(new Date().getTime() - i * (1000 * 60 * 60 * 24));
                url = seite1.extract("/mediathek/video/programm/mediathek-programm-100~_date-" + date, "\"");
                if (url.equals("")) {
                    continue;
                }
                // in die Liste eintragen
                url = MUSTER + date + url;
                seite2 = getUrlIo.getUri_Utf(nameSenderMReader, url, seite2, "");
                //      public void extractList(String abMuster, String bisMuster, String musterStart, String musterEnde, String addUrl, ArrayList<String> result) {
                seite2.extractList("<div class=\"epgContainer\"", "<h3>Legende</h3>", "<a href=\"/mediathek/video/", "\"", "http://www.br.de/mediathek/video/", al);
            }
            for (String s : al) {
                String[] add = new String[]{s, ""};
                if (!istInListe(listeTage, add[0], 0)) {
                    listeTage.add(add);
                }
            }
        } catch (Exception ex) {
            MSearchLog.fehlerMeldung(-821213698, MSearchLog.FEHLER_ART_MREADER, this.getClass().getSimpleName(), ex);
        }
    }

    private class ThemaLaden implements Runnable {

        MSearchGetUrl getUrl = new MSearchGetUrl(wartenSeiteLaden);
        private MSearchStringBuilder seite1 = new MSearchStringBuilder(MSearchConst.STRING_BUFFER_START_BUFFER);
        private MSearchStringBuilder seite2 = new MSearchStringBuilder(MSearchConst.STRING_BUFFER_START_BUFFER);
        private MSearchStringBuilder seiteXml = new MSearchStringBuilder(MSearchConst.STRING_BUFFER_START_BUFFER);

        @Override
        public synchronized void run() {
            try {
                meldungAddThread();
                String[] link;
                while (!MSearchConfig.getStop() && (link = listeThemen.getListeThemen()) != null) {
                    meldungProgress(link[0]);
                    laden(link[0] /* url */, seite1, true);
                }
                while (!MSearchConfig.getStop() && (link = listeTage.getListeThemen()) != null) {
                    meldungProgress(link[0]);
                    laden(link[0] /* url */, seite1, false);
                }
            } catch (Exception ex) {
                MSearchLog.fehlerMeldung(-989632147, MSearchLog.FEHLER_ART_MREADER, "MediathekBr.ThemaLaden.run", ex);
            }
            meldungThreadUndFertig();
        }

        void laden(String urlThema, MSearchStringBuilder seite, boolean weitersuchen) {
            seite = getUrlIo.getUri_Utf(nameSenderMReader, urlThema, seite, "");
            String urlXml;
            String thema;
            String datum;
            String zeit = "";
            String dauer;
            long duration = 0;
            String description;
            String imageUrl;
            String titel;

            if (seite.indexOf("<p class=\"noVideo\">Zur Sendung \"") != -1
                    && seite.indexOf("\" liegen momentan keine Videos vor</p>") != -1) {
                // dann gibts keine Videos
                MSearchLog.fehlerMeldung(-978451236, MSearchLog.FEHLER_ART_MREADER, "MediathekBr.laden", "keine Videos: " + urlThema);
                return;
            }
            thema = seite.extract("<h3>", "<"); //<h3>Abendschau</h3>
            titel = seite.extract("<li class=\"title\">", "<"); //<li class="title">Spionageabwehr auf Bayerisch! - Folge 40</li>
            //<time class="start" datetime="2013-11-08T18:00:00+01:00">08.11.2013, 18:00 Uhr</time>
            datum = seite.extract("<time class=\"start\" datetime=\"", ">", "<");
            datum = datum.replace("Uhr", "").trim();
            if (!datum.isEmpty()) {
                zeit = convertTime(datum);
                datum = convertDatum(datum);
            }
            //<meta property="og:description" content="Aktuelle Berichte aus Bayern, Hintergründe zu brisanten Themen, Geschichten, die unter die Haut gehen - das ist die Abendschau. Sie sehen uns montags bis freitags von 18.00 bis 18.45 Uhr im Bayerischen Fernsehen."/>
            description = seite.extract("<meta property=\"og:description\" content=\"", "\"");
            //<img src="/fernsehen/bayerisches-fernsehen/sendungen/abendschau/der-grosse-max-spionageabwehr-bild-100~_h-180_v-image853_w-320_-84bf43942cbaa96151d5c125e27e60633b3a04c9.jpg?version=fe158" alt="Der große Max | Bild: Bayerischer Rundfunk" title="Der große Max | Bild: Bayerischer Rundfunk" />
            //http://www.br.de/fernsehen/bayerisches-fernsehen/sendungen/abendschau/der-grosse-max-spionageabwehr-bild-100~_h-180_v-image853_w-320_-84bf43942cbaa96151d5c125e27e60633b3a04c9.jpg?version=fe158
            imageUrl = seite.extract("<img src=\"/fernsehen/bayerisches-fernsehen/sendungen/", "\"");
            if (!imageUrl.isEmpty()) {
                imageUrl = "http://www.br.de/fernsehen/bayerisches-fernsehen/sendungen/" + imageUrl;
            }
            //<a href="#" onclick="return BRavFramework.register(BRavFramework('avPlayer_3f097ee3-7959-421b-b3f0-c2a249ad7c91').setup({dataURL:'/mediathek/video/sendungen/abendschau/der-grosse-max-spionageabwehr-100~meta_xsl-avtransform100_-daa09e70fbea65acdb1929dadbd4fc6cdb955b63.xml'}));" id="avPlayer_3f097ee3-7959-421b-b3f0-c2a249ad7c91">
            urlXml = seite.extract("{dataURL:'", "'");
            if (urlXml.isEmpty()) {
                MSearchLog.fehlerMeldung(-915263478, MSearchLog.FEHLER_ART_MREADER, "MediathekBr.laden", "keine URL: " + urlThema);
            } else {
                urlXml = "http://www.br.de" + urlXml;
                seiteXml = getUrlIo.getUri_Utf(nameSenderMReader, urlXml, seiteXml, "");

                try {
                    //<duration>00:03:07</duration>
                    dauer = seiteXml.extract("<duration>", "<");
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
                    MSearchLog.fehlerMeldung(-735216703, MSearchLog.FEHLER_ART_MREADER, "MediathekBr.laden", ex, urlThema);
                }
                //<asset type="STANDARD">
                //<asset type="LARGE">
                //<asset type="PREMIUM">
                //<readableSize>40 MB</readableSize>
                //<downloadUrl>http://cdn-storage.br.de/MUJIuUOVBwQIbtC2uKJDM6OhuLnC_2rc_H1S/_AiS/_yb69Ab6/3325d7de-b41b-49cc-8684-eb957219a070_C.mp4</downloadUrl>
                // oder
                //<serverPrefix>rtmp://cdn-vod-fc.br.de/ondemand/</serverPrefix>
                //<fileName>mp4:MUJIuUOVBwQIbtC2uKJDM6OhuLnC_2rc_H1S/_AiS/_yb69Ab6/3325d7de-b41b-49cc-8684-eb957219a070_B.mp4</fileName>

                String urlGanzKlein = seiteXml.extract("<asset type=\"STANDARD\">", "<downloadUrl>", "<");
                String urlKlein = seiteXml.extract("<asset type=\"LARGE\">", "<downloadUrl>", "<");
                String urlNormal = seiteXml.extract("<asset type=\"PREMIUM\">", "<downloadUrl>", "<");
                String urlHd = seiteXml.extract("<asset type=\"HD\">", "<downloadUrl>", "<");
                //public DatenFilm(String ssender, String tthema, String filmWebsite, String ttitel, String uurl, String uurlRtmp,
                //String datum, String zeit,
                //long dauerSekunden, String description, String imageUrl, String[] keywords) {
                if (urlNormal.isEmpty()) {
                    if (!urlKlein.isEmpty()) {
                        urlNormal = urlKlein;
                        urlKlein = "";
                    } else if (!urlGanzKlein.isEmpty()) {
                        urlNormal = urlGanzKlein;
                        urlGanzKlein = "";
                    }
                }
                if (urlKlein.isEmpty()) {
                    if (!urlGanzKlein.isEmpty()) {
                        urlKlein = urlGanzKlein;
                    }
                }
                if (!urlNormal.isEmpty()) {
                    DatenFilm film = new DatenFilm(nameSenderMReader, thema, urlThema, titel, urlNormal, "" /*urlRtmp*/,
                            datum, zeit,
                            duration, description, imageUrl, new String[]{});
                    if (!urlKlein.isEmpty()) {
                        film.addUrlKlein(urlKlein, "");
                    }
                    if (!urlHd.isEmpty()) {
                        film.addUrlHd(urlHd, "");
                    }
                    addFilm(film);
                    meldung(film.arr[DatenFilm.FILM_URL_NR]);
                } else {
                    MSearchLog.fehlerMeldung(-612136978, MSearchLog.FEHLER_ART_MREADER, "MediathekBr.laden", "keine URL: " + urlXml);
                }
            }
            if (!weitersuchen) {
                return;
            }
            // und jetzt noch nach weiteren Videos auf der Seite suchen
            // <h3>Mehr von <strong>Abendschau</strong></h3>
            // <a href="/mediathek/video/sendungen/abendschau/der-grosse-max-spionageabwehr-100.html" class="teaser link_video contenttype_podcast der-grosse-max-spionageabwehr-100" title="zur Detailseite">
            int pos1 = 0;
            int count = 0;
            int max = (MSearchConfig.senderAllesLaden ? 20 : 0);
            if (max > 0) {
                // dann mit der ganzen Seite arbeiten
                String u = seite.extract("<a class=\"button large\" href=\"", "\"");
                if (!u.isEmpty()) {
                    u = "http://www.br.de" + u;
                    seite = getUrlIo.getUri_Utf(nameSenderMReader, u, seite, "");
                }
            }
            final String MUSTER_URL = "<a href=\"/mediathek/video/sendungen/";
            if ((pos1 = seite.indexOf("<h3>Mehr von <strong>")) != -1) {
                while (!MSearchConfig.getStop()
                        && (pos1 = seite.indexOf(MUSTER_URL, pos1)) != -1) {
                    String urlWeiter = seite.extract(MUSTER_URL, "\"", pos1);
                    pos1 += MUSTER_URL.length();
                    if (!urlWeiter.isEmpty()) {
                        urlWeiter = "http://www.br.de/mediathek/video/sendungen/" + urlWeiter;
                        ++count;
                        if (count > max) {
//                            MSearchLog.debugMeldung("MediathekBr.laden" + " ------> count max erreicht: " + urlThema);
                            break;
                        }
                        laden(urlWeiter, seite2, false);
                    }
                }
            }
        }

        private String convertDatum(String datum) {
            //<time class="start" datetime="2013-11-08T18:00:00+01:00">08.11.2013, 18:00 Uhr</time>
            try {
                Date filmDate = sdf.parse(datum);
                datum = sdfOutDay.format(filmDate);
            } catch (Exception ex) {
                MSearchLog.fehlerMeldung(-915364789, MSearchLog.FEHLER_ART_PROG, "MediathekBr.convertDatum: " + datum, ex);
            }
            return datum;
        }

        private String convertTime(String zeit) {
            //<time class="start" datetime="2013-11-08T18:00:00+01:00">08.11.2013, 18:00 Uhr</time>
            try {
                Date filmDate = sdf.parse(zeit);
                zeit = sdfOutTime.format(filmDate);
            } catch (Exception ex) {
                MSearchLog.fehlerMeldung(-312154879, MSearchLog.FEHLER_ART_PROG, "MediathekBr.convertTime: " + zeit, ex);
            }
            return zeit;
        }
    }

    private class ArchivLaden implements Runnable {

        int anfang, ende;

        public ArchivLaden(int aanfang, int eende) {
            anfang = aanfang;
            ende = eende;
        }

        @Override
        public synchronized void run() {
            meldungAddMax(ende - anfang);
            meldungAddThread();
            try {
                archivSuchen(anfang, ende);
            } catch (Exception ex) {
                MSearchLog.fehlerMeldung(-203069877, MSearchLog.FEHLER_ART_MREADER, "MediathekBr.JsonLaden.run", ex, "");
            }
            meldungThreadUndFertig();
        }
    }

    private void archivSuchen(int start, int ende) {
        // http://www.br.de/service/suche/archiv102.html?documentTypes=video&page=1&sort=date
        final String MUSTER_ADRESSE_1 = "http://www.br.de/service/suche/archiv102.html?documentTypes=video&page=";
        final String MUSTER_ADRESSE_2 = "&sort=date";
        final String MUSTER_START = "<div class=\"teaser search_result\">";
        MSearchStringBuilder seiteArchiv1 = new MSearchStringBuilder(MSearchConst.STRING_BUFFER_START_BUFFER);
        MSearchStringBuilder seiteArchiv2 = new MSearchStringBuilder(MSearchConst.STRING_BUFFER_START_BUFFER);
        MSearchGetUrl getUrl = new MSearchGetUrl(wartenSeiteLaden);
        for (int i = start; i <= ende; ++i) {
            if (MSearchConfig.getStop()) {
                break;
            }
            String adresse = MUSTER_ADRESSE_1 + i + MUSTER_ADRESSE_2;
            meldungProgress(adresse);
            seiteArchiv1 = getUrl.getUri(nameSenderMReader, adresse, MSearchConst.KODIERUNG_UTF, 2 /* versuche */, seiteArchiv1, "" /* Meldung */);
            if (seiteArchiv1.length() == 0) {
                MSearchLog.fehlerMeldung(-912036478, MSearchLog.FEHLER_ART_MREADER, MediathekBr.class.getName() + ".addToList_addr", "Leere Seite für URL: " + adresse);
            }
            int pos = 0, stop = 0;
            String url, titel, thema, datum, beschreibung;
            while (!MSearchConfig.getStop() && (pos = seiteArchiv1.indexOf(MUSTER_START, pos)) != -1) {
                pos += MUSTER_START.length();
                stop = seiteArchiv1.indexOf(MUSTER_START, pos);
                url = seiteArchiv1.extract("<a href=\"", "\"", pos, stop);
                thema = seiteArchiv1.extract("teaser_overline\">", "<", pos, stop).trim();
                if (thema.endsWith(":")) {
                    thema = thema.substring(0, thema.lastIndexOf(":"));
                }
                titel = seiteArchiv1.extract("teaser_title\">", "<", pos, stop);
                // <p class="search_date">23.08.2013 | BR-alpha</p>
                datum = seiteArchiv1.extract("search_date\">", "<", pos, stop);
                if (datum.contains("|")) {
                    datum = datum.substring(0, datum.indexOf("|")).trim();
                }
                beschreibung = seiteArchiv1.extract("<p>", "<", pos, stop);
                if (url.equals("")) {
                    MSearchLog.fehlerMeldung(-636987451, MSearchLog.FEHLER_ART_MREADER, MediathekBr.class.getName() + ".addToList_addr", "keine URL: " + adresse);
                } else {
                    url = "http://www.br.de" + url;
                    archivAdd1(getUrl, seiteArchiv2, url, thema, titel, datum, beschreibung);
                }
            }
        }
    }

    private void archivAdd1(MSearchGetUrl getUrl, MSearchStringBuilder seiteArchiv2, String urlThema, String thema, String titel, String datum, String beschreibung) {
        // http://www.br.de/service/suche/archiv102.html?documentTypes=video&page=1&sort=date
        meldung(urlThema);
        seiteArchiv2 = getUrl.getUri(nameSenderMReader, urlThema, MSearchConst.KODIERUNG_UTF, 1 /* versuche */, seiteArchiv2, "" /* Meldung */);
        if (seiteArchiv2.length() == 0) {
            MSearchLog.fehlerMeldung(-912036478, MSearchLog.FEHLER_ART_MREADER, MediathekBr.class.getName() + ".addToList_addr", "Leere Seite für URL: " + urlThema);
        }
        String url, urlFilm = "", urlFilmKlein = "", groesse = "", duration = "", bild = "";
        long dauer = 0;
        url = seiteArchiv2.extract("setup({dataURL:'", "'");
        bild = seiteArchiv2.extract("setup({dataURL:'", "\" src=\"", "\"");
        if (!bild.isEmpty()) {
            bild = "http://www.br.de" + bild;
        }
        if (url.equals("")) {
            MSearchLog.fehlerMeldung(-834215987, MSearchLog.FEHLER_ART_MREADER, MediathekBr.class.getName() + ".archivAdd1", "keine URL: " + urlThema);
        } else {
            url = "http://www.br.de" + url;
            seiteArchiv2 = getUrl.getUri(nameSenderMReader, url, MSearchConst.KODIERUNG_UTF, 1 /* versuche */, seiteArchiv2, "" /* Meldung */);
            if (seiteArchiv2.length() == 0) {
                MSearchLog.fehlerMeldung(-397123654, MSearchLog.FEHLER_ART_MREADER, MediathekBr.class.getName() + ".addToList_addr", "Leere Seite für URL: " + urlThema);
            }
            // <asset type="STANDARD">
            int start;
            if ((start = seiteArchiv2.indexOf("<asset type=\"STANDARD\">")) != -1) {
                urlFilmKlein = seiteArchiv2.extract("<serverPrefix>", "<", start) + seiteArchiv2.extract("<fileName>", "<", start);
                // <readableSize>281 MB</readableSize>
                groesse = seiteArchiv2.extract("<readableSize>", "<", start);
            }
            if ((start = seiteArchiv2.indexOf("<asset type=\"PREMIUM\">")) != -1) {
                urlFilm = seiteArchiv2.extract("<serverPrefix>", "<", start) + seiteArchiv2.extract("<fileName>", "<", start);
                if (!urlFilm.isEmpty()) {
                    groesse = seiteArchiv2.extract("<readableSize>", "<", start);
                }
            }
            if (groesse.contains("MB")) {
                groesse = groesse.replace("MB", "").trim();
            }
            // <duration>00:44:15</duration>
            duration = seiteArchiv2.extract("<duration>", "<");
            if (!duration.equals("")) {
                try {
                    String[] parts = duration.split(":");
                    long power = 1;
                    for (int i = parts.length - 1; i >= 0; i--) {
                        dauer += Long.parseLong(parts[i]) * power;
                        power *= 60;
                    }
                } catch (Exception ex) {
                    MSearchLog.fehlerMeldung(-304973047, MSearchLog.FEHLER_ART_MREADER, "MediathekBR.jsonSuchen", ex, "duration: " + duration);
                }
            }
            if (urlFilm.isEmpty()) {
                urlFilm = urlFilmKlein;
            }
            if (urlFilm.equals("")) {
                MSearchLog.fehlerMeldung(-978451236, MSearchLog.FEHLER_ART_MREADER, MediathekBr.class.getName() + ".archivAdd1", "keine URL: " + urlThema);
            } else if (dauer == 0 || dauer > 600) {
                // nur anlegen, wenn länger als 10 Minuten, sonst nur Schnipsel
                //public DatenFilm(String ssender, String tthema, String filmWebsite, String ttitel, String uurl, String uurlRtmp,
                //String datum, String zeit, long dauerSekunden, String description, String thumbnailUrl, String imageUrl, String[] keywords) {
                DatenFilm film = new DatenFilm(nameSenderMReader, thema, urlThema, titel, urlFilm, "",
                        datum, "", dauer, beschreibung, bild, new String[]{});
                if (!urlFilmKlein.isEmpty()) {
                    film.addUrlKlein(urlFilmKlein, "");
                }
                try {
                    Integer.parseInt(groesse);
                    film.arr[DatenFilm.FILM_GROESSE_NR] = groesse;
                } catch (Exception ex) {
                    // dann wars nix
                }
                addFilm(film);
            }
        }
    }
}
