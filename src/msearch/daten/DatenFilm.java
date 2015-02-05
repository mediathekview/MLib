/*
 *   MediathekView
 *   Copyright (C) 2008 W. Xaver
 *   W.Xaver[at]googlemail.com
 *   http://zdfmediathk.sourceforge.net/
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package msearch.daten;

import java.text.SimpleDateFormat;
import java.util.Date;
import msearch.filmeSuchen.sender.Mediathek3Sat;
import msearch.filmeSuchen.sender.MediathekArd;
import msearch.filmeSuchen.sender.MediathekBr;
import msearch.filmeSuchen.sender.MediathekMdr;
import msearch.filmeSuchen.sender.MediathekNdr;
import msearch.filmeSuchen.sender.MediathekOrf;
import msearch.filmeSuchen.sender.MediathekSrfPod;
import msearch.filmeSuchen.sender.MediathekSwr;
import msearch.filmeSuchen.sender.MediathekZdf;
import msearch.filmeSuchen.sender.MediathekZdfTivi;
import msearch.tool.Datum;
import msearch.tool.GermanStringSorter;
import msearch.tool.MSConst;
import msearch.tool.MSFileSize;
import msearch.tool.MSLog;
import msearch.tool.MSLong;

public class DatenFilm implements Comparable<DatenFilm> {

    private static GermanStringSorter sorter = GermanStringSorter.getInstance();
    private static SimpleDateFormat sdf_datum_zeit = new SimpleDateFormat("dd.MM.yyyyHH:mm:ss");
    private static SimpleDateFormat sdf_datum = new SimpleDateFormat("dd.MM.yyyy");
    public static final String AUFLOESUNG_NORMAL = "normal";
    public static final String AUFLOESUNG_HD = "hd";
    public static final String AUFLOESUNG_KLEIN = "klein";
    public static final String GEO_DE = "DE"; // nur in .. zu sehen
    public static final String GEO_AT = "AT";
    public static final String GEO_CH = "CH";
    public static final String GEO_EU = "EU";
    public static final String GEO_WELT = "WELT";
    //Tags Filme
    public static final String FELD_INFO = "Feldinfo";
    public static final String FILME = "Filme";
    public static final String FILME_ = "X";
    //
    public static final String FILM_NR = "Nr"; // wird vor dem Speichern gelöscht!
    public static final String FILM_NR_ = "a";
    public static final int FILM_NR_NR = 0;
    public static final String FILM_SENDER = "Sender";
    public static final String FILM_SENDER_ = "b";
    public static final int FILM_SENDER_NR = 1;
    public static final String FILM_THEMA = "Thema";
    public static final String FILM_THEMA_ = "c";
    public static final int FILM_THEMA_NR = 2;
    public static final String FILM_TITEL = "Titel";
    public static final String FILM_TITEL_ = "d";
    public static final int FILM_TITEL_NR = 3;
    public static final String FILM_ABSPIELEN = "";
    public static final String FILM_ABSPIELEN_ = "v";
    public static final int FILM_ABSPIELEN_NR = 4;
    public static final String FILM_AUFZEICHNEN = "";
    public static final String FILM_AUFZEICHNEN_ = "w";
    public static final int FILM_AUFZEICHNEN_NR = 5;
    public static final String FILM_DATUM = "Datum";
    public static final String FILM_DATUM_ = "e";
    public static final int FILM_DATUM_NR = 6;
    public static final String FILM_ZEIT = "Zeit";
    public static final String FILM_ZEIT_ = "f";
    public static final int FILM_ZEIT_NR = 7;
    public static final String FILM_DAUER = "Dauer";
    public static final String FILM_DAUER_ = "m";
    public static final int FILM_DAUER_NR = 8;
    public static final String FILM_GROESSE = "Größe [MB]";
    public static final String FILM_GROESSE_ = "t";
    public static final int FILM_GROESSE_NR = 9;
    public static final String FILM_BESCHREIBUNG = "Beschreibung";
    public static final String FILM_BESCHREIBUNG_ = "n";
    public static final int FILM_BESCHREIBUNG_NR = 10;
    public static final String FILM_GEO = "Geo"; // Geoblocking
    public static final String FILM_GEO_ = "bb";
    public static final int FILM_GEO_NR = 11;
    public static final String FILM_URL = "Url";
    public static final String FILM_URL_ = "g";
    public static final int FILM_URL_NR = 12;
    public static final String FILM_WEBSEITE = "Website"; //URL der Website des Films beim Sender
    public static final String FILM_WEBSEITE_ = "k";
    public static final int FILM_WEBSEITE_NR = 13;
    public static final String FILM_ABO_NAME = "Aboname";// wird vor dem Speichern gelöscht!
    public static final String FILM_ABO_NAME_ = "l";
    public static final int FILM_ABO_NAME_NR = 14;

    public static final String FILM_IMAGE_URL = "Bild"; // wird nicht mehr benutzt!!
    public static final String FILM_IMAGE_URL_ = "o";
    public static final int FILM_IMAGE_URL_NR = 15;

    public static final String FILM_URL_RTMP = "UrlRTMP";
    public static final String FILM_URL_RTMP_ = "i";
    public static final int FILM_URL_RTMP_NR = 16;
    public static final String FILM_URL_AUTH = "UrlAuth";
    public static final String FILM_URL_AUTH_ = "j";
    public static final int FILM_URL_AUTH_NR = 17;
    public static final String FILM_URL_KLEIN = "Url_Klein";
    public static final String FILM_URL_KLEIN_ = "r";
    public static final int FILM_URL_KLEIN_NR = 18;
    public static final String FILM_URL_RTMP_KLEIN = "UrlRTMP_Klein";
    public static final String FILM_URL_RTMP_KLEIN_ = "s";
    public static final int FILM_URL_RTMP_KLEIN_NR = 19;
    public static final String FILM_URL_HD = "Url_HD";
    public static final String FILM_URL_HD_ = "t";
    public static final int FILM_URL_HD_NR = 20;
    public static final String FILM_URL_RTMP_HD = "UrlRTMP_HD";
    public static final String FILM_URL_RTMP_HD_ = "u";
    public static final int FILM_URL_RTMP_HD_NR = 21;
    public static final String FILM_URL_HISTORY = "Url_History";
    public static final String FILM_URL_HISTORY_ = "aa";
    public static final int FILM_URL_HISTORY_NR = 22;
    public static final String FILM_DATUM_LONG = "DatumL"; // Datum als Long ABER Sekunden!!
    public static final String FILM_DATUM_LONG_ = "y";
    public static final int FILM_DATUM_LONG_NR = 23;
    public static final String FILM_REF = "Ref"; // Referenz auf this
    public static final String FILM_REF_ = "z";
    public static final int FILM_REF_NR = 24;
    public static final int MAX_ELEM = 25;
    public String[] arr = new String[]{
        "", "", "", "", "", "", "", "", "", "",
        "", "", "", "", "", "", "", "", "", "",
        "", "", "", "", ""};

    public static final String[] COLUMN_NAMES = {FILM_NR, FILM_SENDER, FILM_THEMA, FILM_TITEL,
        FILM_ABSPIELEN, FILM_AUFZEICHNEN,
        FILM_DATUM, FILM_ZEIT, FILM_DAUER, FILM_GROESSE,
        FILM_BESCHREIBUNG, FILM_GEO,
        /*FILM_KEYWORDS,*/ FILM_URL, FILM_WEBSEITE, FILM_ABO_NAME,
        FILM_IMAGE_URL, FILM_URL_RTMP, FILM_URL_AUTH, FILM_URL_KLEIN, FILM_URL_RTMP_KLEIN, FILM_URL_HD, FILM_URL_RTMP_HD, FILM_URL_HISTORY,
        FILM_DATUM_LONG, FILM_REF};

    // für die alten 3.xxx Versionen auf den alten MACs :)
    public static final String[] COLUMN_NAMES_XML = {FILM_NR_, FILM_SENDER_, FILM_THEMA_, FILM_TITEL_,
        FILM_ABSPIELEN_, FILM_AUFZEICHNEN_,
        FILM_DATUM_, FILM_ZEIT_, FILM_DAUER_, FILM_GROESSE_,
        FILM_BESCHREIBUNG_, FILM_GEO_,
        /*FILM_KEYWORDS_,*/ FILM_URL_, FILM_WEBSEITE_, FILM_ABO_NAME_,
        FILM_IMAGE_URL_, FILM_URL_RTMP_, FILM_URL_AUTH_, FILM_URL_KLEIN_, FILM_URL_RTMP_KLEIN_, FILM_URL_HD_, FILM_URL_RTMP_HD_, FILM_URL_HISTORY_,
        FILM_DATUM_LONG_, FILM_REF_};

    // neue Felder werden HINTEN angefügt!!!!!
    public static final int[] COLUMN_NAMES_JSON = {FILM_SENDER_NR, FILM_THEMA_NR, FILM_TITEL_NR,
        FILM_DATUM_NR, FILM_ZEIT_NR, FILM_DAUER_NR, FILM_GROESSE_NR,
        FILM_BESCHREIBUNG_NR, FILM_URL_NR, FILM_WEBSEITE_NR,
        FILM_IMAGE_URL_NR, FILM_URL_RTMP_NR, FILM_URL_KLEIN_NR, FILM_URL_RTMP_KLEIN_NR, FILM_URL_HD_NR, FILM_URL_RTMP_HD_NR, FILM_DATUM_LONG_NR,
        FILM_URL_HISTORY_NR, FILM_GEO_NR};

    public Datum datumFilm = new Datum(0);
    public long dauerL = 0; // Sekunden
    public Object abo = null;
    public MSLong dateigroesseL; // Dateigröße in MByte
    public static boolean[] spaltenAnzeigen = new boolean[MAX_ELEM];
    public int nr;
    public boolean neuerFilm = false;

    public DatenFilm() {
        dateigroesseL = new MSLong(0); // Dateigröße in MByte
    }

    public DatenFilm(String ssender, String tthema, String filmWebsite, String ttitel, String uurl, String uurlRtmp,
            String datum, String zeit,
            long dauerSekunden, String description, String[] keywords) {
        // da werden die gefundenen Filme beim Absuchen der Senderwebsites erstellt, und nur die!!
        dateigroesseL = new MSLong(0); // Dateigröße in MByte
        arr[FILM_SENDER_NR] = ssender;
        arr[FILM_THEMA_NR] = tthema.isEmpty() ? ssender : tthema.trim();
        arr[FILM_TITEL_NR] = ttitel.isEmpty() ? tthema : ttitel.trim();
        arr[FILM_URL_NR] = uurl;
        arr[FILM_URL_RTMP_NR] = uurlRtmp;
        arr[FILM_WEBSEITE_NR] = filmWebsite;
        checkDatum(datum, arr[FILM_SENDER_NR] + " " + arr[FILM_THEMA_NR] + " " + arr[FILM_TITEL_NR]);
        checkZeit(arr[FILM_DATUM_NR], zeit, arr[FILM_SENDER_NR] + " " + arr[FILM_THEMA_NR] + " " + arr[FILM_TITEL_NR]);
        arr[FILM_BESCHREIBUNG_NR] = cleanDescription(description, tthema, ttitel);
        arr[FILM_IMAGE_URL_NR] = ""; // zur Sicherheit: http://sourceforge.net/apps/phpbb/zdfmediathk/viewtopic.php?f=1&t=1111

        // Filmlänge
        if (dauerSekunden <= 0 || dauerSekunden > 3600 * 5 /* Werte über 5 Stunden */) {
            arr[FILM_DAUER_NR] = "";
        } else {
            String hours = String.valueOf(dauerSekunden / 3600);
            dauerSekunden = dauerSekunden % 3600;
            String min = String.valueOf(dauerSekunden / 60);
            String seconds = String.valueOf(dauerSekunden % 60);
            arr[FILM_DAUER_NR] = fuellen(2, hours) + ":" + fuellen(2, min) + ":" + fuellen(2, seconds);
        }
    }

    public static DatenFilm getDatenFilmLiveStream(String ssender, String addTitle, String uurl) {
        return new DatenFilm(ssender, ListeFilme.THEMA_LIVE, ""/* urlThema */,
                ssender + addTitle + " " + ListeFilme.THEMA_LIVE,
                uurl, ""/*rtmpURL*/, ""/* datum */, ""/* zeit */, 0, "", new String[]{""});
    }

    public void addUrlKlein(String url, String urlRtmp) {
        arr[FILM_URL_KLEIN_NR] = url.isEmpty() ? "" : getKlein(arr[FILM_URL_NR], url);
        arr[FILM_URL_RTMP_KLEIN_NR] = urlRtmp.isEmpty() ? "" : getKlein(arr[FILM_URL_RTMP_NR], urlRtmp);
    }

    public void addUrlHd(String url, String urlRtmp) {
        arr[FILM_URL_HD_NR] = url.isEmpty() ? "" : getKlein(arr[FILM_URL_NR], url);
        arr[FILM_URL_RTMP_HD_NR] = urlRtmp.isEmpty() ? "" : getKlein(arr[FILM_URL_RTMP_NR], urlRtmp);
    }

    public String getUrlFuerAufloesung(String aufloesung) {
        if (aufloesung.equals(AUFLOESUNG_KLEIN)) {
            return getUrlNormalKlein();
        }
        if (aufloesung.equals(AUFLOESUNG_HD)) {
            return getUrlNormalHd();
        }
        return arr[DatenFilm.FILM_URL_NR];
    }

    public String getUrlRtmpFuerAufloesung(String aufloesung) {
        if (aufloesung.equals(AUFLOESUNG_KLEIN)) {
            return getUrlFlvstreamerKlein();
        }
        if (aufloesung.equals(AUFLOESUNG_HD)) {
            return getUrlFlvstreamerHd();
        }
        return getUrlFlvstreamer();
    }

    public String getDateigroesse(String url) {
        if (url.equals(arr[DatenFilm.FILM_URL_NR])) {
            return arr[DatenFilm.FILM_GROESSE_NR];
        } else {
            return MSFileSize.laengeString(url);
        }
    }

    public void setUrlHistory() {
        String u = getUrl(this);
        if (u.equals(arr[DatenFilm.FILM_URL_NR])) {
            arr[DatenFilm.FILM_URL_HISTORY_NR] = "";
        } else {
            arr[DatenFilm.FILM_URL_HISTORY_NR] = u;
        }
    }

    public void setGeo() {
        switch (arr[DatenFilm.FILM_SENDER_NR]) {
            case MediathekArd.SENDERNAME:
            case MediathekSwr.SENDERNAME:
            case MediathekMdr.SENDERNAME:
            case MediathekBr.SENDERNAME:
                if (arr[DatenFilm.FILM_URL_NR].startsWith("http://mvideos-geo.daserste.de/")
                        || arr[DatenFilm.FILM_URL_NR].startsWith("http://media.ndr.de/progressive_geo/")
                        || arr[DatenFilm.FILM_URL_NR].startsWith("http://cdn-storage.br.de/geo/")
                        || arr[DatenFilm.FILM_URL_NR].startsWith("http://cdn-sotschi.br.de/geo/b7/")
                        || arr[DatenFilm.FILM_URL_NR].startsWith("http://pd-ondemand.swr.de/geo/de/")
                        || arr[DatenFilm.FILM_URL_NR].startsWith("http://ondemandgeo.mdr.de/")
                        || arr[DatenFilm.FILM_URL_NR].startsWith("http://ondemand-de.wdr.de/")
                        ) {
                    arr[DatenFilm.FILM_GEO_NR] = GEO_DE;
                }
                break;
            case MediathekZdf.SENDERNAME:
            case MediathekZdfTivi.SENDERNAME:
            case Mediathek3Sat.SENDERNAME:
                if (arr[DatenFilm.FILM_URL_NR].startsWith("http://nrodl.zdf.de/de/")
                        || arr[DatenFilm.FILM_URL_NR].startsWith("http://rodl.zdf.de/de/")) {
                    arr[DatenFilm.FILM_GEO_NR] = GEO_DE;
                } else if (arr[DatenFilm.FILM_URL_NR].startsWith("http://nrodl.zdf.de/dach/")
                        || arr[DatenFilm.FILM_URL_NR].startsWith("http://rodl.zdf.de/dach/")) {
                    arr[DatenFilm.FILM_GEO_NR] = GEO_DE + "-" + GEO_AT + "-" + GEO_CH;
                } else if (arr[DatenFilm.FILM_URL_NR].startsWith("http://nrodl.zdf.de/ebu/")
                        || arr[DatenFilm.FILM_URL_NR].startsWith("http://rodl.zdf.de/ebu/")) {
                    arr[DatenFilm.FILM_GEO_NR] = GEO_DE + "-" + GEO_AT + "-" + GEO_CH + "-" + GEO_EU;
                }
                break;
            case MediathekOrf.SENDERNAME:
                if (arr[DatenFilm.FILM_URL_NR].startsWith("http://apasfpd.apa.at/cms-austria/")
                        || arr[DatenFilm.FILM_URL_NR].startsWith("rtmp://apasfw.apa.at/cms-austria/")) {
                    arr[DatenFilm.FILM_GEO_NR] = GEO_AT;
                }
                break;
            case MediathekSrfPod.SENDERNAME:
                if (arr[DatenFilm.FILM_URL_NR].startsWith("http://podcasts.srf.ch/ch/audio/")) {
                    arr[DatenFilm.FILM_GEO_NR] = GEO_CH;
                }
                break;
            case MediathekNdr.SENDERNAME:
                if (arr[DatenFilm.FILM_URL_NR].startsWith("http://media.ndr.de/progressive_geo")) {
                    arr[DatenFilm.FILM_GEO_NR] = GEO_DE;
                }
                break;
        }
    }

    public String getUrlHistory() {
        if (arr[DatenFilm.FILM_URL_HISTORY_NR].isEmpty()) {
            return arr[DatenFilm.FILM_URL_NR];
        } else {
            return arr[DatenFilm.FILM_URL_HISTORY_NR];
        }
    }

    public String getIndex() {
        // liefert einen eindeutigen Index für die Filmliste
        return arr[FILM_SENDER_NR].toLowerCase() + arr[FILM_THEMA_NR].toLowerCase() + DatenFilm.getUrl(this);
    }

    public static String getUrl(DatenFilm film) {
        return getUrl(film.arr[DatenFilm.FILM_SENDER_NR], film.arr[DatenFilm.FILM_URL_NR]);
    }

    private static String getUrl(String ssender, String uurl) {
        // liefert die URL zum VERGLEICHEN!!
        String url = "";
        if (ssender.equals(MediathekOrf.SENDERNAME)) {
            try {
                url = uurl.substring(uurl.indexOf("/online/") + "/online/".length());
                if (!url.contains("/")) {
                    MSLog.fehlerMeldung(915230478, MSLog.FEHLER_ART_PROG, "DatenFilm.getUrl-1", "Url: " + uurl);
                    return "";
                }
                url = url.substring(url.indexOf("/") + 1);
                if (!url.contains("/")) {
                    MSLog.fehlerMeldung(915230478, MSLog.FEHLER_ART_PROG, "DatenFilm.getUrl-2", "Url: " + uurl);
                    return "";
                }
                url = url.substring(url.indexOf("/") + 1);
                if (url.isEmpty()) {
                    MSLog.fehlerMeldung(915230478, MSLog.FEHLER_ART_PROG, "DatenFilm.getUrl-3", "Url: " + uurl);
                    return "";
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(915230478, MSLog.FEHLER_ART_PROG, "DatenFilm.getUrl-4", ex, "Url: " + uurl);
            }
            return MediathekOrf.SENDERNAME + "----" + url;
        } else {
            return uurl;
        }

    }

    public DatenFilm getCopy() {
        DatenFilm ret = new DatenFilm();
        System.arraycopy(this.arr, 0, ret.arr, 0, arr.length);
        ret.datumFilm = this.datumFilm;
        ret.nr = this.nr;
        ret.dateigroesseL = this.dateigroesseL;
        ret.dauerL = this.dauerL;
        ret.abo = this.abo;
        return ret;
    }

    @Override
    public int compareTo(DatenFilm arg0) {
        int ret;
        if ((ret = sorter.compare(arr[FILM_SENDER_NR], arg0.arr[FILM_SENDER_NR])) == 0) {
            return sorter.compare(arr[FILM_THEMA_NR], arg0.arr[FILM_THEMA_NR]);
        }
        return ret;
    }

    public void clean() {
        // vor dem Speichern nicht benötigte Felder löschen
        arr[FILM_NR_NR] = "";
        arr[FILM_ABO_NAME_NR] = "";
    }

    public void init() {
        try {
            //================================
            // Speicher sparen
            if (arr[DatenFilm.FILM_GROESSE_NR].length() < 3) {
                arr[DatenFilm.FILM_GROESSE_NR] = arr[DatenFilm.FILM_GROESSE_NR].intern();
            }
            if (arr[DatenFilm.FILM_URL_KLEIN_NR].length() < 15) {
                arr[DatenFilm.FILM_URL_KLEIN_NR] = arr[DatenFilm.FILM_URL_KLEIN_NR].intern();
            }
            arr[DatenFilm.FILM_DATUM_NR] = arr[DatenFilm.FILM_DATUM_NR].intern();
            arr[DatenFilm.FILM_ZEIT_NR] = arr[DatenFilm.FILM_ZEIT_NR].intern();

            //================================
            // Dateigröße
            dateigroesseL = new MSLong(this);

            //================================
            // Filmdauer
            try {
                if (!this.arr[DatenFilm.FILM_DAUER_NR].contains(":") && !this.arr[DatenFilm.FILM_DAUER_NR].isEmpty()) {
                    // nur als Übergang bis die Liste umgestellt ist
                    long l = Long.parseLong(this.arr[DatenFilm.FILM_DAUER_NR]);
                    dauerL = l;
                    if (l > 0) {
                        long hours = l / 3600;
                        l = l - (hours * 3600);
                        long min = l / 60;
                        l = l - (min * 60);
                        long seconds = l;
                        this.arr[DatenFilm.FILM_DAUER_NR] = fuellen(2, String.valueOf(hours)) + ":" + fuellen(2, String.valueOf(min)) + ":" + fuellen(2, String.valueOf(seconds));
                    } else {
                        this.arr[DatenFilm.FILM_DAUER_NR] = "";
                    }
                } else {
                    dauerL = 0;
                    if (!this.arr[DatenFilm.FILM_DAUER_NR].equals("")) {
                        String[] parts = this.arr[DatenFilm.FILM_DAUER_NR].split(":");
                        long power = 1;
                        for (int i = parts.length - 1; i >= 0; i--) {
                            dauerL += Long.parseLong(parts[i]) * power;
                            power *= 60;
                        }
                    }
                }
            } catch (Exception ex) {
                dauerL = 0;
                MSLog.fehlerMeldung(468912049, MSLog.FEHLER_ART_PROG, "DatenFilm.init", "Dauer: " + this.arr[DatenFilm.FILM_DAUER_NR]);
            }

            //================================
            // Datum
            if (!arr[DatenFilm.FILM_DATUM_NR].isEmpty()) {
                // nur dann gibts ein Datum
                try {
                    if (arr[DatenFilm.FILM_DATUM_LONG_NR].isEmpty()) {
                        if (arr[DatenFilm.FILM_ZEIT_NR].isEmpty()) {
                            datumFilm = new Datum(sdf_datum.parse(arr[DatenFilm.FILM_DATUM_NR]).getTime());
                        } else {
                            datumFilm = new Datum(sdf_datum_zeit.parse(arr[DatenFilm.FILM_DATUM_NR] + arr[DatenFilm.FILM_ZEIT_NR]).getTime());
                        }
                        arr[FILM_DATUM_LONG_NR] = String.valueOf(datumFilm.getTime() / 1000);
                    } else {
                        long l = Long.parseLong(arr[DatenFilm.FILM_DATUM_LONG_NR]);
                        datumFilm = new Datum(l * 1000 /* sind SEKUNDEN!!*/);
                    }
                } catch (Exception ex) {
                    MSLog.fehlerMeldung(915236701, MSLog.FEHLER_ART_PROG, "DatenFilm.getDatumForObject", ex,
                            new String[]{"Datum: " + arr[DatenFilm.FILM_DATUM_NR], "Zeit: " + arr[DatenFilm.FILM_ZEIT_NR]});
                    datumFilm = new Datum(0);
                    arr[DatenFilm.FILM_DATUM_NR] = "";
                    arr[DatenFilm.FILM_ZEIT_NR] = "";
                }
            }
        } catch (Exception ex) {
            MSLog.fehlerMeldung(715263987, MSLog.FEHLER_ART_PROG, DatenFilm.class.getName() + ".init()", ex);
        }
    }

    private String getKlein(String url1, String url2) {
        String ret = "";
        boolean diff = false;
        for (int i = 0; i < url2.length(); ++i) {
            if (url1.length() > i) {
                if (url1.charAt(i) != url2.charAt(i)) {
                    if (!diff) {
                        ret = i + "|";
                    }
                    diff = true;
                }
            } else {
                diff = true;
            }
            if (diff) {
                ret += url2.charAt(i);
            }
        }
        return ret;
    }

    private String getUrlNormalKlein() {
        // liefert die kleine normale URL
        int i;
        if (!arr[DatenFilm.FILM_URL_KLEIN_NR].isEmpty()) {
            try {
                i = Integer.parseInt(arr[DatenFilm.FILM_URL_KLEIN_NR].substring(0, arr[DatenFilm.FILM_URL_KLEIN_NR].indexOf("|")));
                return arr[DatenFilm.FILM_URL_NR].substring(0, i) + arr[DatenFilm.FILM_URL_KLEIN_NR].substring(arr[DatenFilm.FILM_URL_KLEIN_NR].indexOf("|") + 1);
            } catch (Exception ignored) {
            }
        }
        return arr[DatenFilm.FILM_URL_NR];
    }

    private String getUrlNormalHd() {
        // liefert die HD normale URL
        int i;
        if (!arr[DatenFilm.FILM_URL_HD_NR].isEmpty()) {
            try {
                i = Integer.parseInt(arr[DatenFilm.FILM_URL_HD_NR].substring(0, arr[DatenFilm.FILM_URL_HD_NR].indexOf("|")));
                return arr[DatenFilm.FILM_URL_NR].substring(0, i) + arr[DatenFilm.FILM_URL_HD_NR].substring(arr[DatenFilm.FILM_URL_HD_NR].indexOf("|") + 1);
            } catch (Exception ignored) {
            }
        }
        return arr[DatenFilm.FILM_URL_NR];
    }

    private String getUrlFlvstreamer() {
        String ret;
        if (!arr[DatenFilm.FILM_URL_RTMP_NR].isEmpty()) {
            ret = arr[DatenFilm.FILM_URL_RTMP_NR];
        } else {
            if (arr[DatenFilm.FILM_URL_NR].startsWith(MSConst.RTMP_PRTOKOLL)) {
                ret = MSConst.RTMP_FLVSTREAMER + arr[DatenFilm.FILM_URL_NR];
            } else {
                ret = arr[DatenFilm.FILM_URL_NR];
            }
        }
        return ret;
    }

    private String getUrlFlvstreamerKlein() {
        // liefert die kleine flvstreamer URL
        String ret;
        if (!arr[DatenFilm.FILM_URL_RTMP_KLEIN_NR].isEmpty()) {
            // es gibt eine kleine RTMP
            try {
                int i = Integer.parseInt(arr[DatenFilm.FILM_URL_RTMP_KLEIN_NR].substring(0, arr[DatenFilm.FILM_URL_RTMP_KLEIN_NR].indexOf("|")));
                return arr[DatenFilm.FILM_URL_RTMP_NR].substring(0, i) + arr[DatenFilm.FILM_URL_RTMP_KLEIN_NR].substring(arr[DatenFilm.FILM_URL_RTMP_KLEIN_NR].indexOf("|") + 1);
            } catch (Exception ignored) {
            }
        }
        // es gibt keine kleine RTMP
        if (!arr[DatenFilm.FILM_URL_RTMP_NR].equals("")) {
            // dann gibts keine kleine
            ret = arr[DatenFilm.FILM_URL_RTMP_NR];
        } else {
            // dann gibts überhaupt nur die normalen URLs
            ret = getUrlNormalKlein();
            // und jetzt noch "-r" davorsetzten wenn nötig
            if (ret.startsWith(MSConst.RTMP_PRTOKOLL)) {
                ret = MSConst.RTMP_FLVSTREAMER + ret;
            }
        }
        return ret;
    }

    private String getUrlFlvstreamerHd() {
        // liefert die HD flvstreamer URL
        if (!arr[DatenFilm.FILM_URL_RTMP_HD_NR].isEmpty()) {
            // es gibt eine HD RTMP
            try {
                int i = Integer.parseInt(arr[DatenFilm.FILM_URL_RTMP_HD_NR].substring(0, arr[DatenFilm.FILM_URL_RTMP_HD_NR].indexOf("|")));
                return arr[DatenFilm.FILM_URL_RTMP_NR].substring(0, i) + arr[DatenFilm.FILM_URL_RTMP_HD_NR].substring(arr[DatenFilm.FILM_URL_RTMP_HD_NR].indexOf("|") + 1);
            } catch (Exception ignored) {
            }
        }
        // es gibt keine HD RTMP
        return getUrlFlvstreamer();
    }

    private static final String[] GERMAN_ONLY = {
        "+++ Aus rechtlichen Gründen ist der Film nur innerhalb von Deutschland abrufbar. +++",
        "+++ Aus rechtlichen Gründen ist diese Sendung nur innerhalb von Deutschland abrufbar. +++",
        "+++ Aus rechtlichen Gründen ist dieses Video nur innerhalb von Deutschland abrufbar. +++",
        "+++ Aus rechtlichen Gründen ist dieses Video nur innerhalb von Deutschland verfügbar. +++",
        "+++ Aus rechtlichen Gründen kann das Video nur innerhalb von Deutschland abgerufen werden. +++ Due to legal reasons the video is only available in Germany.+++",
        "+++ Aus rechtlichen Gründen kann das Video nur innerhalb von Deutschland abgerufen werden. +++",
        "+++ Due to legal reasons the video is only available in Germany.+++",
        "+++ Aus rechtlichen Gründen kann das Video nur in Deutschland abgerufen werden. +++"
    };

    public static String cleanDescription(String s, String thema, String titel) {
        // die Beschreibung auf x Zeichen beschränken
        for (String g : GERMAN_ONLY) {
            if (s.contains(g)) {
                s = s.replace(g, ""); // steht auch mal in der Mitte
            }
        }
        if (s.startsWith(titel)) {
            s = s.substring(titel.length()).trim();
        }
        if (s.startsWith(thema)) {
            s = s.substring(thema.length()).trim();
        }
        if (s.startsWith("|")) {
            s = s.substring(1).trim();
        }
        if (s.startsWith("Video-Clip")) {
            s = s.substring("Video-Clip".length()).trim();
        }
        if (s.startsWith(titel)) {
            s = s.substring(titel.length()).trim();
        }
        if (s.startsWith(":")) {
            s = s.substring(1).trim();
        }
        if (s.startsWith(",")) {
            s = s.substring(1).trim();
        }
        if (s.startsWith("\n")) {
            s = s.substring(1).trim();
        }
        if (s.contains("\\\"")) { // wegen " in json-Files
            s = s.replace("\\\"", "\"");
        }
        if (s.length() > MSConst.MAX_BESCHREIBUNG) {
            return s.substring(0, MSConst.MAX_BESCHREIBUNG) + "\n.....";
        } else {
            return s;
        }
    }

    private void checkDatum(String datum, String fehlermeldung) {
        //Datum max. 100 Tage in der Zukunft
        final long MAX = 1000L * 60L * 60L * 24L * 100L;
        datum = datum.trim();
        if (datum.contains(".") && datum.length() == 10) {
            try {
                SimpleDateFormat sdfIn = new SimpleDateFormat("dd.MM.yyyy");
                Date filmDate = sdfIn.parse(datum);
                if (filmDate.getTime() < 0) {
                    //Datum vor 1970
                    MSLog.fehlerMeldung(923012125, MSLog.FEHLER_ART_PROG, "DatenFilm.CheckDatum-3 - Unsinniger Wert: [", datum + "] " + fehlermeldung);
                } else if ((new Date().getTime() + MAX) < filmDate.getTime()) {
                    MSLog.fehlerMeldung(121305469, MSLog.FEHLER_ART_PROG, "DatenFilm.CheckDatum-4 - Unsinniger Wert: [", datum + "] " + fehlermeldung);
                } else {
                    arr[FILM_DATUM_NR] = datum;
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(794630593, MSLog.FEHLER_ART_PROG, "DatenFilm.checkDatum-5", ex);
                MSLog.fehlerMeldung(946301596, MSLog.FEHLER_ART_PROG, "DatenFilm.CheckDatum-6 [", datum + "] " + fehlermeldung);
            }
        }
    }

    private void checkZeit(String datum, String zeit, String fehlermeldung) {
        zeit = zeit.trim();
        if (!datum.isEmpty() && !zeit.isEmpty()) {
            //wenn kein Datum, macht die Zeit auch keinen Sinn
            if (zeit.contains(":") && zeit.length() == 8) {
                arr[FILM_ZEIT_NR] = zeit;
            } else {
                MSLog.fehlerMeldung(159623647, MSLog.FEHLER_ART_PROG, "DatenFilm.checkZeit [", zeit + "] " + fehlermeldung);
            }
        }
    }

    private String fuellen(int anz, String s) {
        while (s.length() < anz) {
            s = "0" + s;
        }
        return s;
    }

}
