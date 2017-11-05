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
package mSearch.daten;

import mSearch.Const;
import mSearch.tool.*;
import org.apache.commons.lang3.time.FastDateFormat;

import java.util.Date;

public class DatenFilm implements Comparable<DatenFilm> {

    public static final String AUFLOESUNG_NORMAL = "normal";
    public static final String AUFLOESUNG_HD = "hd";
    public static final String AUFLOESUNG_KLEIN = "klein";
    public static final String GEO_DE = "DE"; // nur in .. zu sehen
    public static final String GEO_AT = "AT";
    public static final String GEO_CH = "CH";
    public static final String GEO_EU = "EU";
    public static final String GEO_WELT = "WELT";
    //
    public static final int FILM_NR = 0; // wird vor dem Speichern gelöscht!
    public static final int FILM_SENDER = 1;
    public static final int FILM_THEMA = 2;
    public static final int FILM_TITEL = 3;
    public static final int FILM_ABSPIELEN = 4;
    public static final int FILM_AUFZEICHNEN = 5;
    public static final int FILM_DATUM = 6;
    public static final int FILM_ZEIT = 7;
    public static final int FILM_DAUER = 8;
    public static final int FILM_GROESSE = 9;
    public static final int FILM_HD = 10;
    public static final int FILM_UT = 11;
    public static final int FILM_BESCHREIBUNG = 12;
    public static final int FILM_GEO = 13;// Geoblocking
    public static final int FILM_URL = 14;
    public static final int FILM_WEBSEITE = 15; //URL der Website des Films beim Sender
    public static final int FILM_ABO_NAME = 16;// wird vor dem Speichern gelöscht!
    public static final int FILM_URL_SUBTITLE = 17;
    public static final int FILM_URL_RTMP = 18;
    public static final int FILM_URL_AUTH = 19;//frei für andere Sachen
    public static final int FILM_URL_KLEIN = 20;
    public static final int FILM_URL_RTMP_KLEIN = 21;
    public static final int FILM_URL_HD = 22;
    public static final int FILM_URL_RTMP_HD = 23;
    public static final int FILM_URL_HISTORY = 24;
    public static final int FILM_NEU = 25;
    public static final int FILM_DATUM_LONG = 26;// Datum als Long ABER Sekunden!!
    public static final int FILM_REF = 27;// Referenz auf this
    public static final int MAX_ELEM = 28;
    public static final String TAG = "Filme";
    public static final String TAG_JSON_LIST = "X";
    public static final String[] COLUMN_NAMES = {"Nr", "Sender", "Thema", "Titel",
            "", "", "Datum", "Zeit", "Dauer", "Größe [MB]", "HD", "UT",
            "Beschreibung", "Geo", "Url", "Website", "Abo",
            "Url Untertitel", "Url RTMP", "Url Auth", "Url Klein", "Url RTMP Klein", "Url HD", "Url RTMP HD", "Url History", "neu",
            "DatumL", "Ref"};
    // neue Felder werden HINTEN angefügt!!!!!
    public static final int[] JSON_NAMES = {FILM_SENDER, FILM_THEMA, FILM_TITEL,
            FILM_DATUM, FILM_ZEIT, FILM_DAUER, FILM_GROESSE,
            FILM_BESCHREIBUNG, FILM_URL, FILM_WEBSEITE,
            FILM_URL_SUBTITLE, FILM_URL_RTMP, FILM_URL_KLEIN, FILM_URL_RTMP_KLEIN, FILM_URL_HD, FILM_URL_RTMP_HD, FILM_DATUM_LONG,
            FILM_URL_HISTORY, FILM_GEO, FILM_NEU};
    private static final GermanStringSorter sorter = GermanStringSorter.getInstance();
    private static final FastDateFormat sdf_datum_zeit = FastDateFormat.getInstance("dd.MM.yyyyHH:mm:ss");
    private static final FastDateFormat sdf_datum = FastDateFormat.getInstance("dd.MM.yyyy");
    private static final String[] GERMAN_ONLY = {
            "+++ Aus rechtlichen Gründen ist der Film nur innerhalb von Deutschland abrufbar. +++",
            "+++ Aus rechtlichen Gründen ist diese Sendung nur innerhalb von Deutschland abrufbar. +++",
            "+++ Aus rechtlichen Gründen ist dieses Video nur innerhalb von Deutschland abrufbar. +++",
            "+++ Aus rechtlichen Gründen ist dieses Video nur innerhalb von Deutschland verfügbar. +++",
            "+++ Aus rechtlichen Gründen kann das Video nur innerhalb von Deutschland abgerufen werden. +++ Due to legal reasons the video is only available in Germany.+++",
            "+++ Aus rechtlichen Gründen kann das Video nur innerhalb von Deutschland abgerufen werden. +++",
            "+++ Due to legal reasons the video is only available in Germany.+++",
            "+++ Aus rechtlichen Gründen kann das Video nur in Deutschland abgerufen werden. +++",
            "[Aus rechtlichen Günden können wir die Partie nicht als Einzelclip anbieten.]",
            "+++ Aus rechtlichen Gründen ist das Video nur innerhalb von Deutschland abrufbar. +++",
            "+++Aus rechtlichen Gründen kann die Sendung nur innerhalb von Deutschland abgerufen werden. +++",
            "+++ Aus rechtlichen Gründen dürfen wir dieses Video nur innerhalb von Deutschland anbieten. +++",
            "+++Aus rechtlichen Gründen kann dieses Video nur innerhalb von Deutschland abgerufen werden.+++"
    };
    public static boolean[] spaltenAnzeigen = new boolean[MAX_ELEM];
    public final String[] arr = new String[]{
            "", "", "", "", "", "", "", "", "", "",
            "", "", "", "", "", "", "", "", "", "", "", "",
            "", "", "", "", "", ""}; //ist einen Tick schneller, hoffentlich :)
    public DatumFilm datumFilm = new DatumFilm(0);
    public long dauerL = 0; // Sekunden
    public Object abo = null;
    public MSLong dateigroesseL = new MSLong(0); // Dateigröße in MByte
    public int nr;
    private boolean neuerFilm = false;
    private Hash hashValueIndexAddOld = null;
    private Hash hashValueUrl = null;

    public DatenFilm() {
        dateigroesseL = new MSLong(0); // Dateigröße in MByte
    }

    public DatenFilm(String ssender, String tthema, String filmWebsite, String ttitel, String uurl, String uurlRtmp,
                     String datum, String zeit,
                     long dauerSekunden, String description) {
        // da werden die gefundenen Filme beim Absuchen der Senderwebsites erstellt, und nur die!!
        arr[FILM_SENDER] = ssender;
        arr[FILM_THEMA] = tthema.isEmpty() ? ssender : tthema.trim();
        arr[FILM_TITEL] = ttitel.isEmpty() ? tthema : ttitel.trim();
        arr[FILM_URL] = uurl;
        arr[FILM_URL_RTMP] = uurlRtmp;
        arr[FILM_WEBSEITE] = filmWebsite;
        checkDatum(datum, arr[FILM_SENDER] + ' ' + arr[FILM_THEMA] + ' ' + arr[FILM_TITEL]);
        checkZeit(arr[FILM_DATUM], zeit, arr[FILM_SENDER] + ' ' + arr[FILM_THEMA] + ' ' + arr[FILM_TITEL]);
        arr[FILM_BESCHREIBUNG] = cleanDescription(description, tthema, ttitel);

        // Filmlänge
        checkFilmDauer(dauerSekunden);
    }

    /**
     * Determine file size from remote location.
     */
    public void setFileSize() {
        if (arr[DatenFilm.FILM_GROESSE].isEmpty())
            arr[DatenFilm.FILM_GROESSE] = FileSize.laengeString(arr[DatenFilm.FILM_URL]);
    }

    public static String cleanDescription(String s, String thema, String titel) {
        // die Beschreibung auf x Zeichen beschränken

        s = Functions.removeHtml(s); // damit die Beschreibung nicht unnötig kurz wird wenn es erst später gemacht wird

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
        if (s.startsWith(":") || s.startsWith(",") || s.startsWith("\n")) {
            s = s.substring(1).trim();
        }

        if (s.contains("\\\"")) { // wegen " in json-Files
            s = s.replace("\\\"", "\"");
        }
        if (s.length() > Const.MAX_BESCHREIBUNG) {
            return s.substring(0, Const.MAX_BESCHREIBUNG) + "\n.....";
        } else {
            return s;
        }
    }

    public boolean isNew() {
        return neuerFilm;
    }

    public void setNew(final boolean newFilm) {
        neuerFilm = newFilm;
    }

    private void checkFilmDauer(long dauerSekunden) {
        if (dauerSekunden <= 0 || dauerSekunden > 3600 * 5 /* Werte über 5 Stunden */) {
            arr[FILM_DAUER] = "";
        } else {
            String hours = String.valueOf(dauerSekunden / 3600);
            dauerSekunden = dauerSekunden % 3600;
            String min = String.valueOf(dauerSekunden / 60);
            String seconds = String.valueOf(dauerSekunden % 60);
            arr[FILM_DAUER] = fuellen(2, hours) + ':' + fuellen(2, min) + ':' + fuellen(2, seconds);
        }
    }

    public String getUrlSubtitle() {
        return arr[FILM_URL_SUBTITLE];
    }

    public boolean hasUT() {
        //Film hat Untertitel
        return !arr[DatenFilm.FILM_URL_SUBTITLE].isEmpty();
    }

    public String getUrlFuerAufloesung(String aufloesung) {
        if (aufloesung.equals(AUFLOESUNG_KLEIN)) {
            return getUrlNormalOrRequested(DatenFilm.FILM_URL_KLEIN);
        }
        if (aufloesung.equals(AUFLOESUNG_HD)) {
            return getUrlNormalOrRequested(DatenFilm.FILM_URL_HD);
        }
        return arr[DatenFilm.FILM_URL];
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
        if (url.equals(arr[DatenFilm.FILM_URL])) {
            return arr[DatenFilm.FILM_GROESSE];
        } else {
            return FileSize.laengeString(url);
        }
    }

    public void setUrlHistory() {
        String u = getUrl();
        if (u.equals(arr[DatenFilm.FILM_URL])) {
            arr[DatenFilm.FILM_URL_HISTORY] = "";
        } else {
            arr[DatenFilm.FILM_URL_HISTORY] = u;
        }
    }

    public String getUrlHistory() {
        if (arr[DatenFilm.FILM_URL_HISTORY].isEmpty()) {
            return arr[DatenFilm.FILM_URL];
        } else {
            return arr[DatenFilm.FILM_URL_HISTORY];
        }
    }

    public String getIndex() {
        // liefert einen eindeutigen Index für die Filmliste
        // URL beim KiKa und ORF ändern sich laufend!
        return (arr[FILM_SENDER] + arr[FILM_THEMA]).toLowerCase() + getUrl();
    }

    public String getIndexAddOld() {
        // liefert einen eindeutigen Index zum Anhängen einer alten Liste
        return arr[FILM_SENDER] + repl(arr[FILM_THEMA]) + repl(arr[FILM_TITEL]);
    }

    private String repl(String s) {
        return s.replace("-", "").replace("_", "").replace(".", "").replace(" ", "").replace(",", "").toLowerCase();
    }

    public Hash getHashValueIndexAddOld() {
        if (hashValueIndexAddOld == null)
            hashValueIndexAddOld = new Hash(getIndexAddOld());

        return hashValueIndexAddOld;
    }

    public Hash getHashValueUrl() {
        if (hashValueUrl == null)
            hashValueUrl = new Hash(getUrl());

        return hashValueUrl;
    }

    public String getUrl() {
        // liefert die URL zum VERGLEICHEN!!
        String url = "";
        if (arr[DatenFilm.FILM_SENDER].equals(Const.ORF)) {
            final String uurl = arr[DatenFilm.FILM_URL];
            try {
                final String online = "/online/";
                url = uurl.substring(uurl.indexOf(online) + online.length());
                if (!url.contains("/")) {
                    Log.errorLog(915230478, "Url: " + uurl);
                    return "";
                }
                url = url.substring(url.indexOf('/') + 1);
                if (!url.contains("/")) {
                    Log.errorLog(915230478, "Url: " + uurl);
                    return "";
                }
                url = url.substring(url.indexOf('/') + 1);
                if (url.isEmpty()) {
                    Log.errorLog(915230478, "Url: " + uurl);
                    return "";
                }
            } catch (Exception ex) {
                Log.errorLog(915230478, ex, "Url: " + uurl);
            }
            return Const.ORF + "----" + url;
        } else {
            return arr[DatenFilm.FILM_URL];
        }

    }

    public boolean isHD() {
        //Film gibts in HD
        return !arr[DatenFilm.FILM_URL_HD].isEmpty() || !arr[DatenFilm.FILM_URL_RTMP_HD].isEmpty();
    }

//    public void clean() {
//        // vor dem Speichern nicht benötigte Felder löschen
//        arr[FILM_NR] = "";
//        arr[FILM_ABO_NAME] = "";
//    }

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
        if ((ret = sorter.compare(arr[FILM_SENDER], arg0.arr[FILM_SENDER])) == 0) {
            return sorter.compare(arr[FILM_THEMA], arg0.arr[FILM_THEMA]);
        }
        return ret;
    }

    private void preserveMemory() {
//================================
        // Speicher sparen
        if (arr[DatenFilm.FILM_GROESSE].length() < 3) {
            arr[DatenFilm.FILM_GROESSE] = arr[DatenFilm.FILM_GROESSE].intern();
        }
        if (arr[DatenFilm.FILM_URL_KLEIN].length() < 15) {
            arr[DatenFilm.FILM_URL_KLEIN] = arr[DatenFilm.FILM_URL_KLEIN].intern();
        }

        arr[DatenFilm.FILM_DATUM] = arr[DatenFilm.FILM_DATUM].intern();
        arr[DatenFilm.FILM_ZEIT] = arr[DatenFilm.FILM_ZEIT].intern();
    }

    private void setFilmdauer() {
        try {
            if (!this.arr[DatenFilm.FILM_DAUER].contains(":") && !this.arr[DatenFilm.FILM_DAUER].isEmpty()) {
                // nur als Übergang bis die Liste umgestellt ist
                long l = Long.parseLong(this.arr[DatenFilm.FILM_DAUER]);
                dauerL = l;
                if (l > 0) {
                    long hours = l / 3600;
                    l = l - (hours * 3600);
                    long min = l / 60;
                    l = l - (min * 60);
                    long seconds = l;
                    this.arr[DatenFilm.FILM_DAUER] = fuellen(2, String.valueOf(hours)) + ':' + fuellen(2, String.valueOf(min)) + ':' + fuellen(2, String.valueOf(seconds));
                } else {
                    this.arr[DatenFilm.FILM_DAUER] = "";
                }
            } else {
                dauerL = 0;
                if (!this.arr[DatenFilm.FILM_DAUER].isEmpty()) {
                    String[] parts = this.arr[DatenFilm.FILM_DAUER].split(":");
                    long power = 1;
                    for (int i = parts.length - 1; i >= 0; i--) {
                        dauerL += Long.parseLong(parts[i]) * power;
                        power *= 60;
                    }
                }
            }
        } catch (Exception ex) {
            dauerL = 0;
            Log.errorLog(468912049, "Dauer: " + this.arr[DatenFilm.FILM_DAUER]);
        }
    }

    private void setDatum() {
        if (!arr[DatenFilm.FILM_DATUM].isEmpty()) {
            // nur dann gibts ein Datum
            try {
                if (arr[DatenFilm.FILM_DATUM_LONG].isEmpty()) {
                    if (arr[DatenFilm.FILM_ZEIT].isEmpty()) {
                        datumFilm = new DatumFilm(sdf_datum.parse(arr[DatenFilm.FILM_DATUM]).getTime());
                    } else {
                        datumFilm = new DatumFilm(sdf_datum_zeit.parse(arr[DatenFilm.FILM_DATUM] + arr[DatenFilm.FILM_ZEIT]).getTime());
                    }
                    arr[FILM_DATUM_LONG] = String.valueOf(datumFilm.getTime() / 1000);
                } else {
                    long l = Long.parseLong(arr[DatenFilm.FILM_DATUM_LONG]);
                    datumFilm = new DatumFilm(l * 1000 /* sind SEKUNDEN!!*/);
                }
            } catch (Exception ex) {
                Log.errorLog(915236701, ex, new String[]{"Datum: " + arr[DatenFilm.FILM_DATUM], "Zeit: " + arr[DatenFilm.FILM_ZEIT]});
                datumFilm = new DatumFilm(0);
                arr[DatenFilm.FILM_DATUM] = "";
                arr[DatenFilm.FILM_ZEIT] = "";
            }
        }
    }

    public void init() {
        preserveMemory();

        //================================
        // Dateigröße
        dateigroesseL = new MSLong(this);

        //================================
        // Filmdauer
        setFilmdauer();

        //================================
        // Datum
        setDatum();
    }

    private String getUrlNormalOrRequested(int indexUrl) {
        // liefert die kleine normale URL
        if (!arr[indexUrl].isEmpty()) {
            try {
                // Prüfen, ob Pipe auch in URL enthalten ist. Beim ZDF ist das nicht der Fall.
                final int indexPipe = arr[indexUrl].indexOf('|');
                if (indexPipe < 0) {
                  return arr[indexUrl];
                }
                
                final int i = Integer.parseInt(arr[indexUrl].substring(0, indexPipe));
                return arr[DatenFilm.FILM_URL].substring(0, i) + arr[indexUrl].substring(arr[indexUrl].indexOf('|') + 1);
            } catch(Exception e) {
              Log.errorLog(915236703, e, arr[indexUrl]);
            }
        }
        return arr[DatenFilm.FILM_URL];
    }

    private String getUrlFlvstreamer() {
        String ret;
        if (!arr[DatenFilm.FILM_URL_RTMP].isEmpty()) {
            ret = arr[DatenFilm.FILM_URL_RTMP];
        } else if (arr[DatenFilm.FILM_URL].startsWith(Const.RTMP_PRTOKOLL)) {
            ret = Const.RTMP_FLVSTREAMER + arr[DatenFilm.FILM_URL];
        } else {
            ret = arr[DatenFilm.FILM_URL];
        }
        return ret;
    }

    private String getUrlFlvstreamerKlein() {
        // liefert die kleine flvstreamer URL
        String ret;
        if (!arr[DatenFilm.FILM_URL_RTMP_KLEIN].isEmpty()) {
            // es gibt eine kleine RTMP
            try {
                int i = Integer.parseInt(arr[DatenFilm.FILM_URL_RTMP_KLEIN].substring(0, arr[DatenFilm.FILM_URL_RTMP_KLEIN].indexOf('|')));
                return arr[DatenFilm.FILM_URL_RTMP].substring(0, i) + arr[DatenFilm.FILM_URL_RTMP_KLEIN].substring(arr[DatenFilm.FILM_URL_RTMP_KLEIN].indexOf('|') + 1);
            } catch (Exception ignored) {
            }
        }
        // es gibt keine kleine RTMP
        if (!arr[DatenFilm.FILM_URL_RTMP].isEmpty()) {
            // dann gibts keine kleine
            ret = arr[DatenFilm.FILM_URL_RTMP];
        } else {
            // dann gibts überhaupt nur die normalen URLs
            ret = getUrlNormalOrRequested(DatenFilm.FILM_URL_KLEIN);
            // und jetzt noch "-r" davorsetzten wenn nötig
            if (ret.startsWith(Const.RTMP_PRTOKOLL)) {
                ret = Const.RTMP_FLVSTREAMER + ret;
            }
        }
        return ret;
    }

    private String getUrlFlvstreamerHd() {
        // liefert die HD flvstreamer URL
        if (!arr[DatenFilm.FILM_URL_RTMP_HD].isEmpty()) {
            // es gibt eine HD RTMP
            try {
                final int i = Integer.parseInt(arr[DatenFilm.FILM_URL_RTMP_HD].substring(0, arr[DatenFilm.FILM_URL_RTMP_HD].indexOf('|')));
                return arr[DatenFilm.FILM_URL_RTMP].substring(0, i) + arr[DatenFilm.FILM_URL_RTMP_HD].substring(arr[DatenFilm.FILM_URL_RTMP_HD].indexOf('|') + 1);
            } catch (Exception ignored) {
            }
        }
        // es gibt keine HD RTMP
        return getUrlFlvstreamer();
    }

    private void checkDatum(String datum, String fehlermeldung) {
        //Datum max. 100 Tage in der Zukunft
        final long MAX = 1000L * 60L * 60L * 24L * 100L;
        datum = datum.trim();
        if (datum.contains(".") && datum.length() == 10) {
            try {
                Date filmDate = FastDateFormat.getInstance("dd.MM.yyyy").parse(datum);
                if (filmDate.getTime() < 0) {
                    //Datum vor 1970
                    Log.errorLog(923012125, "Unsinniger Wert: [" + datum + "] " + fehlermeldung);
                } else if ((new Date().getTime() + MAX) < filmDate.getTime()) {
                    Log.errorLog(121305469, "Unsinniger Wert: [" + datum + "] " + fehlermeldung);
                } else {
                    arr[FILM_DATUM] = datum;
                }
            } catch (Exception ex) {
                Log.errorLog(794630593, ex);
                Log.errorLog(946301596, '[' + datum + "] " + fehlermeldung);
            }
        }
    }

    private void checkZeit(String datum, String zeit, String fehlermeldung) {
        zeit = zeit.trim();
        if (!datum.isEmpty() && !zeit.isEmpty()) {
            //wenn kein Datum, macht die Zeit auch keinen Sinn
            if (zeit.contains(":") && zeit.length() == 8) {
                arr[FILM_ZEIT] = zeit;
            } else {
                Log.errorLog(159623647, '[' + zeit + "] " + fehlermeldung);
            }
        }
    }

    private String fuellen(int anz, String s) {
        while (s.length() < anz) {
            s = '0' + s;
        }
        return s;
    }

}
