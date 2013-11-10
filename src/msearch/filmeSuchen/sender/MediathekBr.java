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

    public MediathekBr(MSearchFilmeSuchen ssearch, int startPrio) {
        super(ssearch, /* name */ SENDER, /* threads */ 3, /* urlWarten */ 500, startPrio);
    }

    @Override
    void addToList() {
        final String ADRESSE = "http://www.br.de/mediathek/video/sendungen/index.html";
        final String MUSTER_URL = "<a href=\"/mediathek/video/sendungen/";
        listeThemen.clear();
        MSearchStringBuilder seite = new MSearchStringBuilder(MSearchConst.STRING_BUFFER_START_BUFFER);
        meldungStart();
        //seite = new GetUrl(daten).getUriArd(ADRESSE, seite, "");
        seite = getUrlIo.getUri_Iso(nameSenderMReader, ADRESSE, seite, "");
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
                    // in die Liste eintragen
                    String[] add = new String[]{"http://www.br.de/mediathek/video/sendungen/" + url, ""};
                    listeThemen.addUrl(add);
                } catch (Exception ex) {
                    MSearchLog.fehlerMeldung(-821213698, MSearchLog.FEHLER_ART_MREADER, this.getClass().getSimpleName(), ex);
                }
            }
        }
        if (MSearchConfig.getStop()) {
            meldungThreadUndFertig();
        } else if (listeThemen.size() == 0) {
            meldungThreadUndFertig();
        } else {
            meldungAddMax(listeThemen.size());
            for (int t = 0; t < maxThreadLaufen; ++t) {
                Thread th = new Thread(new ThemaLaden());
                th.setName(nameSenderMReader + t);
                th.start();
            }
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
                MSearchLog.fehlerMeldung(-978451236, MSearchLog.FEHLER_ART_MREADER, "MediathekBr.laden", "keine Videos" + urlThema);
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
                MSearchLog.fehlerMeldung(-915263478, MSearchLog.FEHLER_ART_MREADER, "MediathekBr.laden", "keine URL " + urlThema);
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

                //=====> Klein
                String urlGanzKlein = seiteXml.extract("<asset type=\"STANDARD\">", "<downloadUrl>", "<");
//            if (urlKlein.isEmpty()) {
//                String urlKlein1 = seite3.extract("<asset type=\"STANDARD\">", "<serverPrefix>", "<");
//                String urlKlein2 = seite3.extract("<asset type=\"STANDARD\">", "<fileName>", "<");
//                urlKlein = urlKlein1 + urlKlein2;
//            }
//            String groesseKlein = seite3.extract("<asset type=\"STANDARD\">", "<readableSize>", "<");
//            groesseKlein = groesseKlein.replace("MB", "").trim();

                //=====> Normal
                String urlKlein = seiteXml.extract("<asset type=\"LARGE\">", "<downloadUrl>", "<");
//            if (url.isEmpty()) {
//                String url1 = seite3.extract("<asset type=\"LARGE\">", "<serverPrefix>", "<");
//                String url2 = seite3.extract("<asset type=\"LARGE\">", "<fileName>", "<");
//                url = url1 + url2;
//            }
//            String groesse = seite3.extract("<asset type=\"LARGE\">", "<readableSize>", "<");
//            groesse = groesse.replace("MB", "").trim();

                //=====> HD
                String urlNormal = seiteXml.extract("<asset type=\"PREMIUM\">", "<downloadUrl>", "<");
//            if (urlHd.isEmpty()) {
//                String urlHd1 = seite3.extract("<asset type=\"PREMIUM\">", "<serverPrefix>", "<");
//                String urlHd2 = seite3.extract("<asset type=\"PREMIUM\">", "<fileName>", "<");
//                urlHd = urlHd1 + urlHd2;
//            }
//            String groesseHd = seite3.extract("<asset type=\"PREMIUM\">", "<readableSize>", "<");
//            groesseHd = groesseHd.replace("MB", "").trim();

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
                    addFilm(film);
                    meldung(film.arr[DatenFilm.FILM_URL_NR]);
                } else {
                    MSearchLog.fehlerMeldung(-612136978, MSearchLog.FEHLER_ART_MREADER, "MediathekBr.laden", "keine URL " + urlXml);
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
            int max = (MSearchConfig.senderAllesLaden ? 20 : 4);
            final String MUSTER_URL = "<a href=\"/mediathek/video/sendungen/";
            if ((pos1 = seite.indexOf("<h3>Mehr von <strong>")) != -1) {
                while ((pos1 = seite.indexOf(MUSTER_URL, pos1)) != -1) {
                    String urlWeiter = seite.extract(MUSTER_URL, "\"", pos1);
                    pos1 += MUSTER_URL.length();
                    if (!urlWeiter.isEmpty()) {
                        urlWeiter = "http://www.br.de/mediathek/video/sendungen/" + urlWeiter;
                        ++count;
                        laden(urlWeiter, seite2, false);
                        if (count > max) {
                            MSearchLog.fehlerMeldung(-945120378, MSearchLog.FEHLER_ART_MREADER, "MediathekBr.laden", "count max erreicht" + urlThema);
                            break;
                        }
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
}
