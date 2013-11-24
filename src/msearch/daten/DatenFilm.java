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
import msearch.tool.Datum;
import msearch.tool.GermanStringSorter;
import msearch.tool.MSearchConst;
import msearch.tool.MSearchLog;
import msearch.tool.MSearchLong;
import msearch.tool.MSearchUrlDateiGroesse;

public class DatenFilm implements Comparable<DatenFilm> {

    private static GermanStringSorter sorter = GermanStringSorter.getInstance();
    private static SimpleDateFormat sdf_datum_zeit = new SimpleDateFormat("dd.MM.yyyyHH:mm:ss");
    private static SimpleDateFormat sdf_datum = new SimpleDateFormat("dd.MM.yyyy");
    public static final String AUFLOESUNG_NORMAL = "normal";
    public static final String AUFLOESUNG_HD = "hd";
    public static final String AUFLOESUNG_KLEIN = "klein";
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
//    public static final String FILM_KEYWORDS = "Keywords";
//    public static final String FILM_KEYWORDS_ = "q";
//    public static final int FILM_KEYWORDS_NR = 11;
    public static final String FILM_URL = "Url";
    public static final String FILM_URL_ = "g";
    public static final int FILM_URL_NR = 11;
    public static final String FILM_WEBSEITE = "Website"; //URL der Website des Films beim Sender
    public static final String FILM_WEBSEITE_ = "k";
    public static final int FILM_WEBSEITE_NR = 12;
    public static final String FILM_ABO_NAME = "Aboname";// wird vor dem Speichern gelöscht!
    public static final String FILM_ABO_NAME_ = "l";
    public static final int FILM_ABO_NAME_NR = 13;
    public static final String FILM_IMAGE_URL = "Bild";
    public static final String FILM_IMAGE_URL_ = "o";
    public static final int FILM_IMAGE_URL_NR = 14;
    public static final String FILM_URL_RTMP = "UrlRTMP";
    public static final String FILM_URL_RTMP_ = "i";
    public static final int FILM_URL_RTMP_NR = 15;
    public static final String FILM_URL_AUTH = "UrlAuth";
    public static final String FILM_URL_AUTH_ = "j";
    public static final int FILM_URL_AUTH_NR = 16;
    public static final String FILM_URL_KLEIN = "Url_Klein";
    public static final String FILM_URL_KLEIN_ = "r";
    public static final int FILM_URL_KLEIN_NR = 17;
    public static final String FILM_URL_RTMP_KLEIN = "UrlRTMP_Klein";
    public static final String FILM_URL_RTMP_KLEIN_ = "s";
    public static final int FILM_URL_RTMP_KLEIN_NR = 18;
    public static final String FILM_URL_HD = "Url_HD";
    public static final String FILM_URL_HD_ = "t";
    public static final int FILM_URL_HD_NR = 19;
    public static final String FILM_URL_RTMP_HD = "UrlRTMP_HD";
    public static final String FILM_URL_RTMP_HD_ = "u";
    public static final int FILM_URL_RTMP_HD_NR = 20;
    public static final String FILM_DATUM_LONG = "DatumL"; // Datum als Long ABER Sekunden!!
    public static final String FILM_DATUM_LONG_ = "y";
    public static final int FILM_DATUM_LONG_NR = 21;
    public static final String FILM_REF = "Ref"; // Referenz auf this
    public static final String FILM_REF_ = "z";
    public static final int FILM_REF_NR = 22;
    public static final int MAX_ELEM = 23;
    public static final String[] COLUMN_NAMES = {FILM_NR, FILM_SENDER, FILM_THEMA, FILM_TITEL,
        FILM_ABSPIELEN, FILM_AUFZEICHNEN,
        FILM_DATUM, FILM_ZEIT, FILM_DAUER, FILM_GROESSE,
        FILM_BESCHREIBUNG, /*FILM_KEYWORDS,*/ FILM_URL, FILM_WEBSEITE, FILM_ABO_NAME,
        FILM_IMAGE_URL, FILM_URL_RTMP, FILM_URL_AUTH, FILM_URL_KLEIN, FILM_URL_RTMP_KLEIN, FILM_URL_HD, FILM_URL_RTMP_HD, FILM_DATUM_LONG, FILM_REF};
    public static final String[] COLUMN_NAMES_XML = {FILM_NR_, FILM_SENDER_, FILM_THEMA_, FILM_TITEL_,
        FILM_ABSPIELEN_, FILM_AUFZEICHNEN_,
        FILM_DATUM_, FILM_ZEIT_, FILM_DAUER_, FILM_GROESSE_,
        FILM_BESCHREIBUNG_, /*FILM_KEYWORDS_,*/ FILM_URL_, FILM_WEBSEITE_, FILM_ABO_NAME_,
        FILM_IMAGE_URL_, FILM_URL_RTMP_, FILM_URL_AUTH_, FILM_URL_KLEIN_, FILM_URL_RTMP_KLEIN_, FILM_URL_HD_, FILM_URL_RTMP_HD_, FILM_DATUM_LONG_, FILM_REF_};
    public static final int[] COLUMN_NAMES_JSON = {FILM_SENDER_NR, FILM_THEMA_NR, FILM_TITEL_NR,
        FILM_DATUM_NR, FILM_ZEIT_NR, FILM_DAUER_NR, FILM_GROESSE_NR,
        FILM_BESCHREIBUNG_NR, FILM_URL_NR, FILM_WEBSEITE_NR,
        FILM_IMAGE_URL_NR, FILM_URL_RTMP_NR, FILM_URL_KLEIN_NR, FILM_URL_RTMP_KLEIN_NR, FILM_URL_HD_NR, FILM_URL_RTMP_HD_NR, FILM_DATUM_LONG_NR};
    public Datum datumFilm = new Datum(0);
    public long dauerL = 0; // Sekunden
    public Object abo = null;
    public MSearchLong dateigroesseL; // Dateigröße in MByte
    public static boolean[] spaltenAnzeigen = new boolean[MAX_ELEM];
    public String[] arr = new String[]{"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};
    public int nr;
    public boolean neuerFilm = false;

    public DatenFilm() {
        dateigroesseL = new MSearchLong(0); // Dateigröße in MByte
    }

    public DatenFilm(String ssender, String tthema, String filmWebsite, String ttitel, String uurl, String uurlRtmp,
            String datum, String zeit,
            long dauerSekunden, String description, String imageUrl, String[] keywords) {
        // da werden die gefundenen Filme beim Absuchen der Senderwebsites erstellt, und nur die!!
        dateigroesseL = new MSearchLong(0); // Dateigröße in MByte
        arr[FILM_SENDER_NR] = ssender;
        arr[FILM_THEMA_NR] = tthema.isEmpty() ? ssender : tthema;
        arr[FILM_TITEL_NR] = ttitel;
        arr[FILM_URL_NR] = uurl;
        arr[FILM_URL_RTMP_NR] = uurlRtmp;
        arr[FILM_WEBSEITE_NR] = filmWebsite;
        checkDatum(datum, arr[FILM_SENDER_NR] + " " + arr[FILM_THEMA_NR] + " " + arr[FILM_TITEL_NR]);
        checkZeit(arr[FILM_DATUM_NR], zeit, arr[FILM_SENDER_NR] + " " + arr[FILM_THEMA_NR] + " " + arr[FILM_TITEL_NR]);
        arr[FILM_BESCHREIBUNG_NR] = beschreibung(description, tthema, ttitel);
        arr[FILM_IMAGE_URL_NR] = imageUrl;
//        // Schlüsselwörter
//        arr[FILM_KEYWORDS_NR] = keywordsToString(keywords);
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

    public boolean addUrlKlein(String url, String urlRtmp) {
        boolean ret = true;
        arr[FILM_URL_KLEIN_NR] = url.isEmpty() ? "" : getKlein(arr[FILM_URL_NR], url);
        arr[FILM_URL_RTMP_KLEIN_NR] = urlRtmp.isEmpty() ? "" : getKlein(arr[FILM_URL_RTMP_NR], urlRtmp);
        return ret;
    }

    public boolean addUrlHd(String url, String urlRtmp) {
        boolean ret = true;
        arr[FILM_URL_HD_NR] = url.isEmpty() ? "" : getKlein(arr[FILM_URL_NR], url);
        arr[FILM_URL_RTMP_HD_NR] = urlRtmp.isEmpty() ? "" : getKlein(arr[FILM_URL_RTMP_NR], urlRtmp);
        return ret;
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
            return MSearchUrlDateiGroesse.laengeString(url);
        }
    }

    public String getIndex() {
        // liefert einen eindeutigen Index für die Filmliste
        return arr[FILM_SENDER_NR].toLowerCase() + arr[FILM_THEMA_NR].toLowerCase() + arr[FILM_URL_NR];
    }

    public DatenFilm getCopy() {
        DatenFilm ret = new DatenFilm();
        for (int i = 0; i < arr.length; ++i) {
            ret.arr[i] = new String(this.arr[i]);
        }
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
//        ///////////////////
//        if (this.nr % 2 == 0) {
//            this.neuerFilm = true;
//        }
        try {
            // Dateigröße
            dateigroesseL = new MSearchLong(this);
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
                MSearchLog.fehlerMeldung(468912049, MSearchLog.FEHLER_ART_PROG, "DatenFilm.init", "Dauer: " + this.arr[DatenFilm.FILM_DAUER_NR]);
            }
            // Datum
            if (!arr[DatenFilm.FILM_DATUM_NR].isEmpty()) {
                // nur dann gibts ein Datum
                try {
                    if (arr[DatenFilm.FILM_DATUM_LONG_NR].isEmpty()) {
                        if (arr[DatenFilm.FILM_ZEIT_NR].isEmpty()) {
                            datumFilm = new Datum(sdf_datum.parse(arr[DatenFilm.FILM_DATUM_NR]));
                        } else {
                            datumFilm = new Datum(sdf_datum_zeit.parse(arr[DatenFilm.FILM_DATUM_NR] + arr[DatenFilm.FILM_ZEIT_NR]));
                        }
                        arr[FILM_DATUM_LONG_NR] = String.valueOf(datumFilm.getTime() / 1000);
                    } else {
                        long l = Long.parseLong(arr[DatenFilm.FILM_DATUM_LONG_NR]);
                        datumFilm = new Datum(l * 1000 /* sind SEKUNDEN!!*/);
                    }
                } catch (Exception ex) {
                    MSearchLog.fehlerMeldung(915236701, MSearchLog.FEHLER_ART_PROG, "DatenFilm.getDatumForObject", ex,
                            new String[]{"Datum: " + arr[DatenFilm.FILM_DATUM_NR], "Zeit: " + arr[DatenFilm.FILM_ZEIT_NR]});
                    datumFilm = new Datum(0);
                    arr[DatenFilm.FILM_DATUM_NR] = "";
                    arr[DatenFilm.FILM_ZEIT_NR] = "";
                }
            }
        } catch (Exception ex) {
            MSearchLog.fehlerMeldung(715263987, MSearchLog.FEHLER_ART_PROG, DatenFilm.class.getName() + ".init()", ex);
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

//    public static boolean anzeigen(int i) {
//        if (spaltenAnzeigen == null) {
//            return true;
//        } else {
//            return spaltenAnzeigen[i];
//        }
//    }
    private String getUrlNormalKlein() {
        // liefert die kleine normale URL
        int i;
        if (!arr[DatenFilm.FILM_URL_KLEIN_NR].isEmpty()) {
            try {
                i = Integer.parseInt(arr[DatenFilm.FILM_URL_KLEIN_NR].substring(0, arr[DatenFilm.FILM_URL_KLEIN_NR].indexOf("|")));
                return arr[DatenFilm.FILM_URL_NR].substring(0, i) + arr[DatenFilm.FILM_URL_KLEIN_NR].substring(arr[DatenFilm.FILM_URL_KLEIN_NR].indexOf("|") + 1);
            } catch (Exception ex) {
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
            } catch (Exception ex) {
            }
        }
        return arr[DatenFilm.FILM_URL_NR];
    }

    private String getUrlFlvstreamer() {
        String ret;
        if (!arr[DatenFilm.FILM_URL_RTMP_NR].isEmpty()) {
            ret = arr[DatenFilm.FILM_URL_RTMP_NR];
        } else {
            if (arr[DatenFilm.FILM_URL_NR].startsWith(MSearchConst.RTMP_PRTOKOLL)) {
                ret = MSearchConst.RTMP_FLVSTREAMER + arr[DatenFilm.FILM_URL_NR];
            } else {
                ret = arr[DatenFilm.FILM_URL_NR];
            }
        }
        return ret;
    }

    private String getUrlFlvstreamerKlein() {
        // liefert die kleine flvstreamer URL
        String ret = "";
        if (!arr[DatenFilm.FILM_URL_RTMP_KLEIN_NR].isEmpty()) {
            // es gibt eine kleine RTMP
            try {
                int i = Integer.parseInt(arr[DatenFilm.FILM_URL_RTMP_KLEIN_NR].substring(0, arr[DatenFilm.FILM_URL_RTMP_KLEIN_NR].indexOf("|")));
                return arr[DatenFilm.FILM_URL_RTMP_NR].substring(0, i) + arr[DatenFilm.FILM_URL_RTMP_KLEIN_NR].substring(arr[DatenFilm.FILM_URL_RTMP_KLEIN_NR].indexOf("|") + 1);
            } catch (Exception ex) {
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
            if (ret.startsWith(MSearchConst.RTMP_PRTOKOLL)) {
                ret = MSearchConst.RTMP_FLVSTREAMER + ret;
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
            } catch (Exception ex) {
            }
        }
        // es gibt keine HD RTMP
        return getUrlFlvstreamer();
    }

    private String beschreibung(String s, String thema, String titel) {
        // die Beschreibung auf x Zeichen beschränken
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
        final int x = 250;
        if (s.length() > x) {
            return s.substring(0, x) + "\n.....";
        } else {
            return s;
        }
    }

//    private String keywordsToString(String[] keywords) {
//        final int x = 200;
//        String k = "";
//        for (String kk : keywords) {
//            if (k.length() + kk.length() > x) {
//                // nicht mehr als x zeichen lang!
//                break;
//            }
//            if (k.length() > 0) {
//                k += ",";
//            }
//            k += kk;
//        }
//        return k;
//    }
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
                    MSearchLog.debugMeldung("DatenFilm.CheckDatum-3 - " + "Unsinniger Wert: [" + datum + "] " + fehlermeldung);
                } else if ((new Date().getTime() + MAX) < filmDate.getTime()) {
                    MSearchLog.debugMeldung("DatenFilm.CheckDatum-4 - " + "Unsinniger Wert: [" + datum + "] " + fehlermeldung);
                } else {
                    arr[FILM_DATUM_NR] = datum;
                    arr[FILM_DATUM_LONG_NR] = String.valueOf(filmDate.getTime() / 1000);
                }
            } catch (Exception ex) {
                MSearchLog.fehlerMeldung(794630593, MSearchLog.FEHLER_ART_PROG, "DatenFilm.checkDatum-5", ex);
                MSearchLog.fehlerMeldung(946301596, MSearchLog.FEHLER_ART_PROG, "DatenFilm.CheckDatum-6 [", datum + "] " + fehlermeldung);
            }
        }
    }

    private void checkZeit(String datum, String zeit, String fehlermeldung) {
        zeit = zeit.trim();
        if (!datum.isEmpty()) {
            //wenn kein Datum, macht die Zeit auch keinen Sinn
            if (zeit.contains(":") && zeit.length() == 8) {
                arr[FILM_ZEIT_NR] = zeit;
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
