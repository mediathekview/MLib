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
package msearch.daten;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.SimpleTimeZone;
import msearch.filmeSuchen.sender.MediathekArd;
import msearch.filmeSuchen.sender.MediathekKika;
import msearch.filmeSuchen.sender.MediathekNdr;
import msearch.filmeSuchen.sender.MediathekWdr;
import msearch.filmeSuchen.sender.MediathekZdf;
import msearch.tool.Funktionen;
import msearch.tool.GuiFunktionen;
import msearch.tool.MSearchConst;
import msearch.tool.MSearchLog;
import msearch.tool.MSearchUrlDateiGroesse;
import org.apache.commons.lang3.StringEscapeUtils;

public class ListeFilme extends ArrayList<DatenFilm> {

    public static final String THEMA_LIVE = "Livestream";
    //Tags Infos Filmliste, erste Zeile der .filme-Datei
    public static final String FILMLISTE = "Filmliste";
    public static final String FILMLISTE_DATUM = "Filmliste-Datum";
    public static final int FILMLISTE_DATUM_NR = 0;
    public static final String FILMLISTE_DATUM_GMT = "Filmliste-Datum-GMT";
    public static final int FILMLISTE_DATUM_GMT_NR = 1;
    public static final String FILMLISTE_VERSION = "Filmliste-Version";
    public static final int FILMLISTE_VERSION_NR = 2;
    public static final String FILMLISTE_PROGRAMM = "Filmliste-Programm";
    public static final int FILMLISTE_PRGRAMM_NR = 3;
    public static final int MAX_ELEM = 4;
    public static final String[] COLUMN_NAMES = {FILMLISTE_DATUM, FILMLISTE_DATUM_GMT, FILMLISTE_VERSION, FILMLISTE_PROGRAMM};
    public int nr = 0;
    public boolean listeClean = false;
    public String[] metaDaten = new String[]{"", "", "", ""};
    public HashSet<String> hashSet = new HashSet<>();
    final String DATUM_ZEIT_FORMAT = "dd.MM.yyyy, HH:mm";
    final String DATUM_ZEIT_FORMAT_REV = "yyyy.MM.dd__HH:mm";

    public ListeFilme() {
    }

    //===================================
    // public
    //===================================
    @JsonIgnore
    @Override
    public synchronized void clear() {
        hashSet.clear();
        nr = 0;
        super.clear();
    }

    @JsonIgnore
    public void check() {
        Iterator<DatenFilm> it = this.iterator();
        DatenFilm film;
        while (it.hasNext()) {
            film = it.next();
            film.arr[DatenFilm.FILM_THEMA_NR] = GuiFunktionen.cleanUnicode(film.arr[DatenFilm.FILM_THEMA_NR], "!!!!!!!!!!!!!");
            film.arr[DatenFilm.FILM_TITEL_NR] = GuiFunktionen.cleanUnicode(film.arr[DatenFilm.FILM_TITEL_NR], "!!!!!!!!!!!!!");
            if (film.arr[DatenFilm.FILM_URL_NR].contains(" ")) {
                System.out.println(film.arr[DatenFilm.FILM_URL_NR]);
            }
        }
    }

    @JsonIgnore
    public void sort() {
        Collections.<DatenFilm>sort(this);
        // und jetzt noch die Nummerierung in Ordnung bringen
        Iterator<DatenFilm> it = this.iterator();
        DatenFilm film;
        int i = 0;
        while (it.hasNext()) {
            film = it.next();
            film.arr[DatenFilm.FILM_NR_NR] = getNr(i++);
        }
    }

    @JsonIgnore
    public synchronized void setMeta(String[] mmeta) {
        for (int i = 0; i < MAX_ELEM; ++i) {
            metaDaten[i] = mmeta[i].toString();
        }
    }

    @JsonIgnore
    public synchronized DatenFilm istInFilmListe(String sender, String thema, String titel) {
        Iterator<DatenFilm> it = listIterator();
        while (it.hasNext()) {
            DatenFilm film = it.next();
            if (film.arr[DatenFilm.FILM_SENDER_NR].equals(sender)
                    && film.arr[DatenFilm.FILM_THEMA_NR].equalsIgnoreCase(thema)
                    && film.arr[DatenFilm.FILM_TITEL_NR].equalsIgnoreCase(titel)) {
                return film;
            }
        }
        return null;
    }

    @JsonIgnore
    public String getDateiGroesse(String url, String sender) {
        // sucht in der Liste nach der URL und gibt die Dateigröße zurück
        // oder versucht sie übers Web zu ermitteln
        Iterator<DatenFilm> it = listIterator();
        while (it.hasNext()) {
            DatenFilm film = it.next();
            if (film.arr[DatenFilm.FILM_URL_NR].equals(url)) {
                if (!film.arr[DatenFilm.FILM_GROESSE_NR].isEmpty()) {
                    return film.arr[DatenFilm.FILM_GROESSE_NR];
                } else {
                    return MSearchUrlDateiGroesse.laengeString(url, sender);
                }
            }
        }
        // dann ist der Film nicht in der Liste
        return MSearchUrlDateiGroesse.laengeString(url, sender);
    }

    @JsonIgnore
    public synchronized boolean addFilmVomSender(DatenFilm film) {
        // Filme die beim Sender gesucht wurden (und nur die) hier eintragen
        // nur für die MediathekReader
        // ist eine URL,Sender,Thema,Titel schon vorhanden, wird sie verworfen, 
        // der aktuellste Film (werden von jetzt in die Vergangenheit gesucht) bleibt erhalten
        film.arr[DatenFilm.FILM_THEMA_NR] = StringEscapeUtils.unescapeXml(film.arr[DatenFilm.FILM_THEMA_NR].trim());
        film.arr[DatenFilm.FILM_THEMA_NR] = StringEscapeUtils.unescapeHtml4(film.arr[DatenFilm.FILM_THEMA_NR].trim());

        // Beschreibung
        film.arr[DatenFilm.FILM_BESCHREIBUNG_NR] = StringEscapeUtils.unescapeXml(film.arr[DatenFilm.FILM_BESCHREIBUNG_NR].trim());
        film.arr[DatenFilm.FILM_BESCHREIBUNG_NR] = StringEscapeUtils.unescapeHtml4(film.arr[DatenFilm.FILM_BESCHREIBUNG_NR].trim());

        // Titel
        film.arr[DatenFilm.FILM_TITEL_NR] = StringEscapeUtils.unescapeXml(film.arr[DatenFilm.FILM_TITEL_NR].trim());
        film.arr[DatenFilm.FILM_TITEL_NR] = StringEscapeUtils.unescapeHtml4(film.arr[DatenFilm.FILM_TITEL_NR].trim());
        // erst mal schauen obs das schon gibt
        DatenFilm f;
        String idx = film.getIndex();
        Iterator<DatenFilm> it = this.iterator();
        while (it.hasNext()) {
            f = it.next();
            if (f.getIndex().equals(idx)) {
                return false;
            }
        }
        return addInit(film);
    }

    @JsonIgnore
    public synchronized void updateListe(ListeFilme listeEinsortieren, boolean index /* Vergleich über Index, sonst nur URL */) {
        // in eine vorhandene Liste soll eine andere Filmliste einsortiert werden
        // es werden nur Filme die noch nicht vorhanden sind, einsortiert
        DatenFilm film;
        HashSet<String> hash = new HashSet<>();
        Iterator<DatenFilm> it = this.iterator();
        while (it.hasNext()) {
            if (index) {
                hash.add(it.next().getIndex());
            } else {
                hash.add(it.next().arr[DatenFilm.FILM_URL_NR]);
            }
        }
        it = listeEinsortieren.iterator();
        while (it.hasNext()) {
            film = it.next();
            if (index) {
                if (!hash.contains(film.getIndex())) {
                    addInit(film);
                }
            } else {
                if (!hash.contains(film.arr[DatenFilm.FILM_URL_NR])) {
                    addInit(film);
                }
            }
        }
        hash.clear();
    }

    @JsonIgnore
    public synchronized void nurDoppelteAnzeigen(boolean index) {
        // zum Debuggen: URLs die doppelt sind, in die History eintragen
        // damit sie markiert werden
        DatenFilm film;
        HashSet<String> hashDoppelt = new HashSet<String>();
        HashSet<String> hash = new HashSet<String>();
        Iterator<DatenFilm> it = this.iterator();
        while (it.hasNext()) {
            film = it.next();
            if (index) {
                if (!hash.contains(film.getIndex())) {
                    hash.add(film.getIndex());
                } else {
                    // dann ist er mind. doppelt in der Liste
                    hashDoppelt.add(film.arr[DatenFilm.FILM_URL_NR]);
                }
            } else {
                if (!hash.contains(film.arr[DatenFilm.FILM_URL_NR])) {
                    hash.add(film.arr[DatenFilm.FILM_URL_NR]);
                } else {
                    // dann ist er mind. doppelt in der Liste
                    hashDoppelt.add(film.arr[DatenFilm.FILM_URL_NR]);
                }
            }
        }
        it = this.iterator();
        while (it.hasNext()) {
            if (!hashDoppelt.contains(it.next().arr[DatenFilm.FILM_URL_NR])) {
                it.remove();
            }
        }
        hash.clear();
        hashDoppelt.clear();
    }

    @JsonIgnore
    private boolean addInit(DatenFilm film) {
        if (film.arr[DatenFilm.FILM_GROESSE_NR].length() < 3) {
            film.arr[DatenFilm.FILM_GROESSE_NR] = film.arr[DatenFilm.FILM_GROESSE_NR].intern();
        }
        film.init();
        return add(film);
    }

    @JsonIgnore
    public void init() {
        // es werden die gelöschten Felder nach "clean" wieder
        // erstellt
        Iterator<DatenFilm> it = iterator();
        while (it.hasNext()) {
            it.next().init();
        }
        listeClean = false;
    }

    @JsonIgnore
    public void clean() {
        // zum Speichern werden alle nicht notwendigen Felder
        // gelöscht
        ListIterator<DatenFilm> it = this.listIterator(0);
        while (it.hasNext()) {
            it.next().clean();
        }
        listeClean = true;
    }

    @JsonIgnore
    @Override
    public boolean add(DatenFilm film) {
        if (film.arr[DatenFilm.FILM_URL_KLEIN_NR].length() < 15) {
            film.arr[DatenFilm.FILM_URL_KLEIN_NR] = film.arr[DatenFilm.FILM_URL_KLEIN_NR].intern();
        }
        film.arr[DatenFilm.FILM_DATUM_NR] = film.arr[DatenFilm.FILM_DATUM_NR].intern();
        film.arr[DatenFilm.FILM_ZEIT_NR] = film.arr[DatenFilm.FILM_ZEIT_NR].intern();
        if (film.arr[DatenFilm.FILM_KEYWORDS_NR].equals("video")) {
            film.arr[DatenFilm.FILM_KEYWORDS_NR] = "";
        }
        return super.add(film);
    }

    @JsonIgnore
    public synchronized boolean addWithNr(DatenFilm film) {
        // hier nur beim Laden von der Filmliste
        film.arr[DatenFilm.FILM_NR_NR] = getNr(nr++);
        return addInit(film);
    }

    @JsonIgnore
    private String getNr(int nr) {
        final int MAX_STELLEN = 5;
        final String FUELL_ZEICHEN = "0";
        String str = String.valueOf(nr);
        while (str.length() < MAX_STELLEN) {
            str = FUELL_ZEICHEN + str;
        }
        return str;
    }

    @JsonIgnore
    public synchronized int countSender(String sender) {
        int ret = 0;
        ListIterator<DatenFilm> it = this.listIterator(0);
        while (it.hasNext()) {
            if (it.next().arr[DatenFilm.FILM_SENDER_NR].equalsIgnoreCase(sender)) {
                ++ret;
            }
        }
        return ret;
    }

    @JsonIgnore
    public synchronized void delSender(String sender) {
        // alle Filme VOM SENDER löschen
        DatenFilm film;
        ListIterator<DatenFilm> it = this.listIterator(0);
        while (it.hasNext()) {
            film = it.next();
            if (film.arr[DatenFilm.FILM_SENDER_NR].equalsIgnoreCase(sender)) {
                it.remove();
            }
        }
    }

    @JsonIgnore
    public void liveStreamEintragen() {
        // Live-Stream eintragen
        //DatenFilm(Daten ddaten, String ssender, String tthema, String urlThema, String ttitel, String uurl, String datum, String zeit) {
        addFilmVomSender(new DatenFilm(MediathekNdr.SENDER, THEMA_LIVE, ""/* urlThema */,
                MediathekNdr.SENDER + " " + THEMA_LIVE,
                "http://www.ndr.de/resources/metadaten/ndr_fs_nds_hi_wmv.asx", ""/*rtmpURL*/, ""/* datum */, ""/* zeit */, 0, "", "", new String[]{""}));
        addFilmVomSender(new DatenFilm(MediathekWdr.SENDER, THEMA_LIVE, ""/* urlThema */,
                MediathekWdr.SENDER + " " + THEMA_LIVE,
                "http://www.wdr.de/wdrlive/media/wdr-fernsehen_web-l.asx", ""/*rtmpURL*/, ""/* datum */, ""/* zeit */, 0, "", "", new String[]{""}));
        // die neuen Livestreams ARD
        addFilmVomSender(new DatenFilm(MediathekArd.SENDER, THEMA_LIVE, ""/* urlThema */,
                MediathekArd.SENDER + " Small " + THEMA_LIVE,
                "rtsp://daserste.edges.wowza.gl-systemhaus.de/live/mp4:daserste_int_320", ""/*rtmpURL*/, ""/* datum */, ""/* zeit */, 0, "", "", new String[]{""}));
        addFilmVomSender(new DatenFilm(MediathekArd.SENDER, THEMA_LIVE, ""/* urlThema */,
                MediathekArd.SENDER + " Medium " + THEMA_LIVE,
                "rtsp://daserste.edges.wowza.gl-systemhaus.de/live/mp4:daserste_int_576", ""/*rtmpURL*/, ""/* datum */, ""/* zeit */, 0, "", "", new String[]{""}));
        addFilmVomSender(new DatenFilm(MediathekArd.SENDER, THEMA_LIVE, ""/* urlThema */,
                MediathekArd.SENDER + " Big " + THEMA_LIVE,
                "rtsp://daserste.edges.wowza.gl-systemhaus.de/live/mp4:daserste_int_1600", ""/*rtmpURL*/, ""/* datum */, ""/* zeit */, 0, "", "", new String[]{""}));
        // ZDF
        addFilmVomSender(new DatenFilm(MediathekZdf.SENDER, THEMA_LIVE, ""/* urlThema */,
                MediathekZdf.SENDER + " " + THEMA_LIVE,
                "rtsp://3gp-livestreaming1.zdf.de/liveedge2/de10_v1_710.sdp", ""/*rtmpURL*/, ""/* datum */, ""/* zeit */, 0, "", "", new String[]{""}));
        addFilmVomSender(new DatenFilm(MediathekZdf.SENDER, THEMA_LIVE, ""/* urlThema */,
                MediathekZdf.SENDER + ".info " + THEMA_LIVE,
                "rtsp://3gp-livestreaming1.zdf.de/liveedge2/de08_v1_710.sdp", ""/*rtmpURL*/, ""/* datum */, ""/* zeit */, 0, "", "", new String[]{""}));
        addFilmVomSender(new DatenFilm(MediathekZdf.SENDER, THEMA_LIVE, ""/* urlThema */,
                MediathekZdf.SENDER + ".kultur " + THEMA_LIVE,
                "rtsp://3gp-livestreaming1.zdf.de/liveedge2/de07_v1_710.sdp", ""/*rtmpURL*/, ""/* datum */, ""/* zeit */, 0, "", "", new String[]{""}));
        addFilmVomSender(new DatenFilm(MediathekZdf.SENDER, THEMA_LIVE, ""/* urlThema */,
                MediathekZdf.SENDER + ".neo " + THEMA_LIVE,
                "rtsp://3gp-livestreaming1.zdf.de/liveedge2/de09_v1_710.sdp", ""/*rtmpURL*/, ""/* datum */, ""/* zeit */, 0, "", "", new String[]{""}));
        // KIKA
        addFilmVomSender(new DatenFilm(MediathekKika.SENDER, THEMA_LIVE, ""/* urlThema */,
                MediathekKika.SENDER + " " + THEMA_LIVE,
                "http://kikaplus.net/clients/kika/player/myplaylist.php?channel=1&programm=1&videoid=1", ""/*rtmpURL*/, ""/* datum */, ""/* zeit */, 0, "", "", new String[]{""}));
    }

    @JsonIgnore
    public synchronized DatenFilm getFilmByUrl(String url) {
        // Problem wegen gleicher URLs
        DatenFilm ret = null;
        ListIterator<DatenFilm> it = this.listIterator(0);
        while (it.hasNext()) {
            DatenFilm f = it.next();
            if (f.arr[DatenFilm.FILM_URL_NR].equals(url)) {
                ret = f;
                break;
            }
        }
        return ret;
    }

    @JsonIgnore
    public synchronized DatenFilm getFilmByUrl_klein_hoch_hd(String url) {
        // Problem wegen gleicher URLs
        // wird versucht, einen Film mit einer kleinen/Hoher/HD-URL zu finden
        DatenFilm ret = null;
        ListIterator<DatenFilm> it = this.listIterator(0);
        while (it.hasNext()) {
            DatenFilm f = it.next();
            if (f.arr[DatenFilm.FILM_URL_NR].equals(url)) {
                ret = f;
                break;
            } else if (f.getUrlFuerAufloesung(DatenFilm.AUFLOESUNG_HD).equals(url)) {
                ret = f;
                break;
            } else if (f.getUrlFuerAufloesung(DatenFilm.AUFLOESUNG_KLEIN).equals(url)) {
                ret = f;
                break;
            }
        }
        return ret;
    }

    @JsonIgnore
    public synchronized DatenFilm getFilmByNr(String nr) {
        int n = 0;
        try {
            n = Integer.parseInt(nr);
        } catch (Exception ex) {
            MSearchLog.fehlerMeldung(936254978, MSearchLog.FEHLER_ART_PROG, "ListeFilme.getFilmByNr", "Nr: " + nr);
            return null;
        }
        return this.get(n);
    }

//    public synchronized DatenFilm getFilmByNr(String nr) {
//        DatenFilm ret = null;
//        ListIterator<DatenFilm> it = this.listIterator(0);
//        while (it.hasNext()) {
//            DatenFilm f = it.next();
//            if (f.arr[DatenFilm.FILM_NR_NR].equals(nr)) {
//                ret = f;
//                break;
//            }
//        }
//        return ret;
//    }
    @JsonIgnore
    public String genDate() {
        // Tag, Zeit in lokaler Zeit wann die Filmliste erstellt wurde
        // in der Form "dd.MM.yyyy, HH:mm"
        String ret;
        SimpleDateFormat sdf = new SimpleDateFormat(DATUM_ZEIT_FORMAT);
        String date;
        if (metaDaten[ListeFilme.FILMLISTE_DATUM_GMT_NR].equals("")) {
            // noch eine alte Filmliste
            ret = metaDaten[ListeFilme.FILMLISTE_DATUM_NR];
        } else {
            date = metaDaten[ListeFilme.FILMLISTE_DATUM_GMT_NR];
            sdf.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
            Date filmDate = null;
            try {
                filmDate = sdf.parse(date);
            } catch (ParseException ex) {
            }
            if (filmDate == null) {
                ret = metaDaten[ListeFilme.FILMLISTE_DATUM_GMT_NR];
            } else {
                SimpleDateFormat formatter = new SimpleDateFormat(DATUM_ZEIT_FORMAT);
                ret = formatter.format(filmDate);
            }
        }
        return ret;
    }

    @JsonIgnore
    public String genDateRev() {
        // Tag, Zeit in lokaler Zeit wann die Filmliste erstellt wurde
        // in der Form "yyyy.MM.dd__HH:mm"
        String ret;
        SimpleDateFormat sdf = new SimpleDateFormat(DATUM_ZEIT_FORMAT);
        String date;
        if (metaDaten[ListeFilme.FILMLISTE_DATUM_GMT_NR].equals("")) {
            // noch eine alte Filmliste
            ret = metaDaten[ListeFilme.FILMLISTE_DATUM_NR];
        } else {
            date = metaDaten[ListeFilme.FILMLISTE_DATUM_GMT_NR];
            sdf.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
            Date filmDate = null;
            try {
                filmDate = sdf.parse(date);
            } catch (ParseException ex) {
            }
            if (filmDate == null) {
                ret = metaDaten[ListeFilme.FILMLISTE_DATUM_GMT_NR];
            } else {
                SimpleDateFormat formatter = new SimpleDateFormat(DATUM_ZEIT_FORMAT_REV);
                ret = formatter.format(filmDate);
            }
        }
        return ret;
    }

    @JsonIgnore
    public int alterFilmlisteSek() {
        // Alter der Filmliste in Sekunden
        int ret = 0;
        Date jetzt = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat(DATUM_ZEIT_FORMAT);
        String date;
        if (!metaDaten[ListeFilme.FILMLISTE_DATUM_GMT_NR].equals("")) {
            date = metaDaten[ListeFilme.FILMLISTE_DATUM_GMT_NR];
            sdf.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
        } else {
            date = metaDaten[ListeFilme.FILMLISTE_DATUM_NR];
        }
        Date filmDate = null;
        try {
            filmDate = sdf.parse(date);
        } catch (ParseException ex) {
        }
        if (filmDate != null) {
            ret = Math.round((jetzt.getTime() - filmDate.getTime()) / (1000));
            if (ret < 0) {
                ret = 0;
            }
        }
        return ret;
    }

    @JsonIgnore
    public boolean filmlisteZuAlt() {
        if (this.size() == 0) {
            return true;
        }
        // Filmliste ist älter als: FilmeLaden.ALTER_FILMLISTE_SEKUNDEN_FUER_AUTOUPDATE
        return filmlisteIstAelter(MSearchConst.ALTER_FILMLISTE_SEKUNDEN_FUER_AUTOUPDATE);
    }

    @JsonIgnore
    public boolean filmlisteIstAelter(int sekunden) {
        int ret = alterFilmlisteSek();
        if (ret != 0) {
            MSearchLog.systemMeldung("Die Filmliste ist " + ret / 60 + " Minuten alt");
        }
        return ret > sekunden;
    }

    @JsonIgnore
    public void metaDatenSchreiben() {
        // FilmlisteMetaDaten
        for (int i = 0; i < metaDaten.length; ++i) {
            metaDaten[i] = "";
        }
        if (!MSearchConfig.getStop() /* löschen */) {
            metaDaten[ListeFilme.FILMLISTE_DATUM_NR] = getJetzt_ddMMyyyy_HHmm();
            metaDaten[ListeFilme.FILMLISTE_DATUM_GMT_NR] = getJetzt_ddMMyyyy_HHmm_gmt();
        } else {
            metaDaten[ListeFilme.FILMLISTE_DATUM_NR] = "";
            metaDaten[ListeFilme.FILMLISTE_DATUM_GMT_NR] = "";
        }
        metaDaten[ListeFilme.FILMLISTE_VERSION_NR] = MSearchConst.VERSION_FILMLISTE;
        metaDaten[ListeFilme.FILMLISTE_PRGRAMM_NR] = Funktionen.getProgVersionString() + " - Compiled: " + Funktionen.getCompileDate();
    }

    @JsonIgnore
    String getJetzt_ddMMyyyy_HHmm() {
        SimpleDateFormat formatter = new SimpleDateFormat(DATUM_ZEIT_FORMAT);
        return formatter.format(new Date());
    }

    @JsonIgnore
    String getJetzt_ddMMyyyy_HHmm_gmt() {
        SimpleDateFormat formatter = new SimpleDateFormat(DATUM_ZEIT_FORMAT);
        formatter.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
        return formatter.format(new Date());
    }
}
