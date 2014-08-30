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

import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.SimpleTimeZone;
import java.util.TreeSet;
import msearch.filmeSuchen.sender.Mediathek3Sat;
import msearch.filmeSuchen.sender.MediathekArd;
import msearch.filmeSuchen.sender.MediathekKika;
import msearch.filmeSuchen.sender.MediathekNdr;
import msearch.filmeSuchen.sender.MediathekRbb;
import msearch.filmeSuchen.sender.MediathekWdr;
import msearch.filmeSuchen.sender.MediathekZdf;
import msearch.tool.MSConst;
import msearch.tool.MSFunktionen;
import msearch.tool.MSGuiFunktionen;
import msearch.tool.MSLog;
import msearch.tool.MSUrlDateiGroesse;

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
    public static final String FILMLISTE_ID = "Filmliste-Id";
    public static final int FILMLISTE_ID_NR = 4;
    public static final int MAX_ELEM = 5;
    public static final String[] COLUMN_NAMES = {FILMLISTE_DATUM, FILMLISTE_DATUM_GMT, FILMLISTE_VERSION, FILMLISTE_PROGRAMM, FILMLISTE_ID};
    public int nr = 1;
    public String[] metaDaten = new String[]{"", "", "", "", ""};
    private final static String DATUM_ZEIT_FORMAT = "dd.MM.yyyy, HH:mm";
    private final static String DATUM_ZEIT_FORMAT_REV = "yyyy.MM.dd__HH:mm";
    SimpleDateFormat sdf = new SimpleDateFormat(DATUM_ZEIT_FORMAT);
    public String[] sender = {""};
    public String[][] themenPerSender = {{""}};
    public boolean neueFilme = false;

    public ListeFilme() {
    }

    //===================================
    // public
    //===================================
    public synchronized boolean addFilmVomSender(DatenFilm film) {
        // Filme die beim Sender gesucht wurden (und nur die) hier eintragen
        // nur für die MediathekReader
        // ist eine URL,Sender,Thema,Titel schon vorhanden, wird sie verworfen, 
        // der aktuellste Film (werden von jetzt in die Vergangenheit gesucht) bleibt erhalten
        MSFunktionen.unescape(film);
        // zur Sicherheit: http://sourceforge.net/apps/phpbb/zdfmediathk/viewtopic.php?f=1&t=1111
        film.arr[DatenFilm.FILM_IMAGE_URL_NR] = "";
        // erst mal schauen obs das schon gibt
        DatenFilm f;
        String idx = film.getIndex();
        for (DatenFilm datenFilm : this) {
            f = datenFilm;
            if (f.getIndex().equals(idx)) {
                return false;
            }
        }
        return addInit(film);
    }

    public synchronized void updateListe(ListeFilme listeEinsortieren, boolean index /* Vergleich über Index, sonst nur URL */, boolean ersetzen) {
        // in eine vorhandene Liste soll eine andere Filmliste einsortiert werden
        // es werden nur Filme die noch nicht vorhanden sind, einsortiert
        // "ersetzen": true: dann werden gleiche (index/URL) in der Liste durch neue ersetzt
        HashSet<String> hash = new HashSet<>();
        Iterator<DatenFilm> it;
        if (ersetzen) {
            // ==========================================
            it = listeEinsortieren.iterator();
            while (it.hasNext()) {
                DatenFilm f = it.next();
                if (f.arr[DatenFilm.FILM_SENDER_NR].equals(MediathekKika.SENDER)) {
                    // beim KIKA ändern sich die URLs laufend
                    hash.add(f.arr[DatenFilm.FILM_THEMA_NR] + f.arr[DatenFilm.FILM_TITEL_NR]);
                } else if (index) {
                    hash.add(f.getIndex());
                } else {
                    hash.add(DatenFilm.getUrl(f));
                }
            }
            it = this.iterator();
            while (it.hasNext()) {
                DatenFilm f = it.next();
                if (f.arr[DatenFilm.FILM_SENDER_NR].equals(MediathekKika.SENDER)) {
                    // beim KIKA ändern sich die URLs laufend
                    if (hash.contains(f.arr[DatenFilm.FILM_THEMA_NR] + f.arr[DatenFilm.FILM_TITEL_NR])) {
                        it.remove();
                    }
                } else if (index) {
                    if (hash.contains(f.getIndex())) {
                        it.remove();
                    }
                } else {
                    if (hash.contains(DatenFilm.getUrl(f))) {
                        it.remove();
                    }
                }
            }
            it = listeEinsortieren.iterator();
            while (it.hasNext()) {
                DatenFilm f = it.next();
                this.addInit(f);
            }
        } else {
            // ==============================================
            it = this.iterator();
            while (it.hasNext()) {
                DatenFilm f = it.next();
                if (f.arr[DatenFilm.FILM_SENDER_NR].equals(MediathekKika.SENDER)) {
                    // beim KIKA ändern sich die URLs laufend
                    hash.add(f.arr[DatenFilm.FILM_THEMA_NR] + f.arr[DatenFilm.FILM_TITEL_NR]);
                } else if (index) {
                    hash.add(f.getIndex());
                } else {
                    hash.add(DatenFilm.getUrl(f));
                }
            }
            it = listeEinsortieren.iterator();
            while (it.hasNext()) {
                DatenFilm f = it.next();
                if (f.arr[DatenFilm.FILM_SENDER_NR].equals(MediathekKika.SENDER)) {
                    if (!hash.contains(f.arr[DatenFilm.FILM_THEMA_NR] + f.arr[DatenFilm.FILM_TITEL_NR])) {
                        addInit(f);
                    }
                } else if (index) {
                    if (!hash.contains(f.getIndex())) {
                        addInit(f);
                    }
                } else {
                    if (!hash.contains(DatenFilm.getUrl(f))) {
                        addInit(f);
                    }
                }
            }
        }
        hash.clear();
    }

    public synchronized boolean importFilmliste(DatenFilm film) {
        // hier nur beim Laden aus einer fertigen Filmliste 
        // die Filme einsortieren, es werden alle Filme einsortiert
        film.nr = nr++;
        return addInit(film);
    }

    private boolean addInit(DatenFilm film) {
        if (film.arr[DatenFilm.FILM_GROESSE_NR].length() < 3) {
            film.arr[DatenFilm.FILM_GROESSE_NR] = film.arr[DatenFilm.FILM_GROESSE_NR].intern();
        }
        film.init();
        return add(film);
    }

    @Override
    public synchronized boolean add(DatenFilm film) {
        if (film.arr[DatenFilm.FILM_URL_KLEIN_NR].length() < 15) {
            film.arr[DatenFilm.FILM_URL_KLEIN_NR] = film.arr[DatenFilm.FILM_URL_KLEIN_NR].intern();
        }
        film.arr[DatenFilm.FILM_DATUM_NR] = film.arr[DatenFilm.FILM_DATUM_NR].intern();
        film.arr[DatenFilm.FILM_ZEIT_NR] = film.arr[DatenFilm.FILM_ZEIT_NR].intern();
        return super.add(film);
    }

    @Override
    public synchronized void clear() {
        nr = 1;
        neueFilme = false;
        super.clear();
    }

    public synchronized void check() {
        for (DatenFilm film : this) {
            film.arr[DatenFilm.FILM_THEMA_NR] = MSGuiFunktionen.cleanUnicode(film.arr[DatenFilm.FILM_THEMA_NR], "!!!!!!!!!!!!!");
            film.arr[DatenFilm.FILM_TITEL_NR] = MSGuiFunktionen.cleanUnicode(film.arr[DatenFilm.FILM_TITEL_NR], "!!!!!!!!!!!!!");
            if (film.arr[DatenFilm.FILM_URL_NR].contains(" ")) {
                System.out.println(film.arr[DatenFilm.FILM_URL_NR]);
            }
        }
    }

    public synchronized void nurDoppelteAnzeigen(boolean index) {
        // zum Debuggen: URLs die doppelt sind, in die History eintragen
        // damit sie markiert werden
        DatenFilm film;
        HashSet<String> hashDoppelt = new HashSet<>();
        HashSet<String> hash = new HashSet<>();
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

    public synchronized void sort() {
        Collections.sort(this);
        // und jetzt noch die Nummerierung in Ordnung bringen
        int i = 1;
        for (DatenFilm film : this) {
            film.nr = i++;
        }
    }

    public synchronized void setMeta(ListeFilme listeFilme) {
        System.arraycopy(listeFilme.metaDaten, 0, metaDaten, 0, MAX_ELEM);
    }

    public synchronized DatenFilm istInFilmListe(String sender, String thema, String titel) {
        // prüfen ob es den Film schon gibt
        // und sich evtl. nur die URL geändert hat
        for (DatenFilm film : this) {
            if (film.arr[DatenFilm.FILM_SENDER_NR].equals(sender)
                    && film.arr[DatenFilm.FILM_THEMA_NR].equalsIgnoreCase(thema)
                    && film.arr[DatenFilm.FILM_TITEL_NR].equalsIgnoreCase(titel)) {
                return film;
            }
        }
        return null;
    }

    public synchronized ListeFilme neueFilme(ListeFilme orgListe) {
        // Funktion liefert eine Liste mit Filmen
        // die im Vergleich zur Liste "orgListe"
        // neu sind, also ein Diff mit nur den neuen Filmen in DIESER Liste
        ListeFilme ret = new ListeFilme();
        HashSet<String> hashSet = new HashSet<>();
        Iterator<DatenFilm> it = orgListe.listIterator();
        while (it.hasNext()) {
            DatenFilm film = it.next();
            String s = film.arr[DatenFilm.FILM_SENDER_NR] + film.arr[DatenFilm.FILM_THEMA_NR] + film.arr[DatenFilm.FILM_TITEL_NR] + film.arr[DatenFilm.FILM_URL_NR];
            hashSet.add(s);
        }
        it = listIterator();
        while (it.hasNext()) {
            DatenFilm film = it.next();
            String s = film.arr[DatenFilm.FILM_SENDER_NR] + film.arr[DatenFilm.FILM_THEMA_NR] + film.arr[DatenFilm.FILM_TITEL_NR] + film.arr[DatenFilm.FILM_URL_NR];
            if (!hashSet.contains(s)) {
                ret.add(film);
            }
        }
        ret.metaDaten = metaDaten;
        return ret;
    }

    public synchronized String getDateiGroesse(String url, String sender) {
        // sucht in der Liste nach der URL und gibt die Dateigröße zurück
        // oder versucht sie übers Web zu ermitteln
        for (DatenFilm film : this) {
            if (film.arr[DatenFilm.FILM_URL_NR].equals(url)) {
                if (!film.arr[DatenFilm.FILM_GROESSE_NR].isEmpty()) {
                    return film.arr[DatenFilm.FILM_GROESSE_NR];
                } else {
                    return MSUrlDateiGroesse.laengeString(url, sender);
                }
            }
        }
        // dann ist der Film nicht in der Liste
        return MSUrlDateiGroesse.laengeString(url, sender);
    }

    /**
     * Count the number of films belonging to a sender.
     *
     * @param sender The sender name.
     * @return Number of films.
     */
    public synchronized int countSender(final String sender) {
        int ret = 0;
        for (DatenFilm film : this) {
            if (film.arr[DatenFilm.FILM_SENDER_NR].equalsIgnoreCase(sender)) {
                ret++;
            }
        }

        return ret;
    }

    /**
     * Delete all films from specified sender.
     *
     * @param sender Sender which films are to be deleted.
     */
    public synchronized void deleteAllFilms(String sender) {
        DatenFilm film;
        ListIterator<DatenFilm> it = this.listIterator(0);
        while (it.hasNext()) {
            film = it.next();
            if (film.arr[DatenFilm.FILM_SENDER_NR].equalsIgnoreCase(sender)) {
                it.remove();
            }
        }
    }

    public synchronized void liveStreamEintragen() {
        // Live-Stream eintragen
        //DatenFilm(Daten ddaten, String ssender, String tthema, String urlThema, String ttitel, String uurl, String datum, String zeit) {
        addFilmVomSender(new DatenFilm(MediathekNdr.SENDER, THEMA_LIVE, ""/* urlThema */,
                MediathekNdr.SENDER + " " + THEMA_LIVE,
                "http://ndr_fs-lh.akamaihd.net/i/ndrfs_nds@119224/master.m3u8", ""/*rtmpURL*/, ""/* datum */, ""/* zeit */, 0, "", "", new String[]{""}));
        addFilmVomSender(new DatenFilm(MediathekWdr.SENDER, THEMA_LIVE, ""/* urlThema */,
                MediathekWdr.SENDER + " " + THEMA_LIVE,
                "rtsp://rtspgen.api.core-cdn.net/10177xwdrsw04/sw04_ww_webl", ""/*rtmpURL*/, ""/* datum */, ""/* zeit */, 0, "", "", new String[]{""}));
        // die neuen Livestreams ARD
        addFilmVomSender(new DatenFilm(MediathekArd.SENDER, THEMA_LIVE, ""/* urlThema */,
                MediathekArd.SENDER + " Tagesschau " + THEMA_LIVE,
                "http://tagesschau-lh.akamaihd.net/i/tagesschau_1@119231/master.m3u8", ""/*rtmpURL*/, ""/* datum */, ""/* zeit */, 0, "", "", new String[]{""}));
        // ZDF
        addFilmVomSender(new DatenFilm(MediathekZdf.SENDER, THEMA_LIVE, ""/* urlThema */,
                MediathekZdf.SENDER + " " + THEMA_LIVE,
                "rtsp://3gp-livestreaming1.zdf.de/liveedge2/de14_v1_710.sdp", ""/*rtmpURL*/, ""/* datum */, ""/* zeit */, 0, "", "", new String[]{""}));
        addFilmVomSender(new DatenFilm(MediathekZdf.SENDER, THEMA_LIVE, ""/* urlThema */,
                MediathekZdf.SENDER + ".info " + THEMA_LIVE,
                "rtsp://3gp-livestreaming1.zdf.de/liveedge2/de12_v1_710.sdp", ""/*rtmpURL*/, ""/* datum */, ""/* zeit */, 0, "", "", new String[]{""}));
        addFilmVomSender(new DatenFilm(MediathekZdf.SENDER, THEMA_LIVE, ""/* urlThema */,
                MediathekZdf.SENDER + ".kultur " + THEMA_LIVE,
                "rtsp://3gp-livestreaming1.zdf.de/liveedge2/de11_v1_710.sdp", ""/*rtmpURL*/, ""/* datum */, ""/* zeit */, 0, "", "", new String[]{""}));
        addFilmVomSender(new DatenFilm(MediathekZdf.SENDER, THEMA_LIVE, ""/* urlThema */,
                MediathekZdf.SENDER + ".neo " + THEMA_LIVE,
                "rtsp://3gp-livestreaming1.zdf.de/liveedge2/de13_v1_710.sdp", ""/*rtmpURL*/, ""/* datum */, ""/* zeit */, 0, "", "", new String[]{""}));
        addFilmVomSender(new DatenFilm(MediathekZdf.SENDER, THEMA_LIVE, ""/* urlThema */,
                MediathekZdf.SENDER + ".heute " + THEMA_LIVE,
                "rtsp://3gp-livestreaming1.zdf.de/liveedge2/none01_v1_710.sdp", ""/*rtmpURL*/, ""/* datum */, ""/* zeit */, 0, "", "", new String[]{""}));

        addFilmVomSender(new DatenFilm(MediathekKika.SENDER, THEMA_LIVE, ""/* urlThema */,
                MediathekKika.SENDER + " " + THEMA_LIVE,
                "rtmp://85.239.122.162/live/mk3w-3faw-3rqf-enc0-kika", ""/*rtmpURL*/, ""/* datum */, ""/* zeit */, 0, "", "", new String[]{""}));

        // 3sat
        addFilmVomSender(new DatenFilm(Mediathek3Sat.SENDER, THEMA_LIVE, ""/* urlThema */,
                Mediathek3Sat.SENDER + " " + THEMA_LIVE,
                "http://zdf_hds_dach-f.akamaihd.net/i/dach10_v1@87031/master.m3u8?b=0-710&dw=0", ""/*rtmpURL*/, ""/* datum */, ""/* zeit */, 0, "", "", new String[]{""}));

        // RBB
        addFilmVomSender(new DatenFilm(MediathekRbb.SENDER, THEMA_LIVE, ""/* urlThema */,
                MediathekRbb.SENDER + " " + THEMA_LIVE,
                "http://rbb_live-lh.akamaihd.net/i/rbb_berlin@108248/master.m3u8", ""/*rtmpURL*/, ""/* datum */, ""/* zeit */, 0, "", "", new String[]{""}));
    }

    public synchronized DatenFilm getFilmByUrl(final String url) {
        for (DatenFilm film : this) {
            if (film.arr[DatenFilm.FILM_URL_NR].equalsIgnoreCase(url)) {
                return film;
            }
        }
        return null;
    }

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

    public synchronized String genDate() {
        // Tag, Zeit in lokaler Zeit wann die Filmliste erstellt wurde
        // in der Form "dd.MM.yyyy, HH:mm"
        String ret;
        SimpleDateFormat sdf_ = new SimpleDateFormat(DATUM_ZEIT_FORMAT);
        String date;
        if (metaDaten[ListeFilme.FILMLISTE_DATUM_GMT_NR].equals("")) {
            // noch eine alte Filmliste
            ret = metaDaten[ListeFilme.FILMLISTE_DATUM_NR];
        } else {
            date = metaDaten[ListeFilme.FILMLISTE_DATUM_GMT_NR];
            sdf_.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
            Date filmDate = null;
            try {
                filmDate = sdf_.parse(date);
            } catch (ParseException ignored) {
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

    public synchronized String getId() {
        // liefert die ID einer Filmliste
        return metaDaten[ListeFilme.FILMLISTE_ID_NR];
    }

    public synchronized String genDateRev() {
        // Tag, Zeit in lokaler Zeit wann die Filmliste erstellt wurde
        // in der Form "yyyy.MM.dd__HH:mm"
        String ret;
        SimpleDateFormat sdf_ = new SimpleDateFormat(DATUM_ZEIT_FORMAT);
        String date;
        if (metaDaten[ListeFilme.FILMLISTE_DATUM_GMT_NR].equals("")) {
            // noch eine alte Filmliste
            ret = metaDaten[ListeFilme.FILMLISTE_DATUM_NR];
        } else {
            date = metaDaten[ListeFilme.FILMLISTE_DATUM_GMT_NR];
            sdf_.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
            Date filmDate = null;
            try {
                filmDate = sdf_.parse(date);
            } catch (ParseException ignored) {
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

    /**
     * Get the age of the film list.
     *
     * @return Age in seconds.
     */
    public synchronized int getAge() {
        int ret = 0;
        Date now = new Date(System.currentTimeMillis());
        Date filmDate = getAgeAsDate();
        if (filmDate != null) {
            ret = Math.round((now.getTime() - filmDate.getTime()) / (1000));
            if (ret < 0) {
                ret = 0;
            }
        }
        return ret;
    }

    /**
     * Get the age of the film list.
     *
     * @return Age as a {@link java.util.Date} object.
     */
    public synchronized Date getAgeAsDate() {
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
        } catch (ParseException ignored) {
        }

        return filmDate;
    }

    /**
     * Check if available Filmlist is older than a specified value.
     *
     * @return true if too old or if the list is empty.
     */
    public synchronized boolean isTooOld() {
        return (isEmpty()) || (isOlderThan(MSConst.ALTER_FILMLISTE_SEKUNDEN_FUER_AUTOUPDATE));
    }

    /**
     * Check if Filmlist is too old for using a diff list.
     *
     * @return true if empty or too old.
     */
    public synchronized boolean isTooOldForDiff() {
        if (isEmpty()) {
            return true;
        }
        try {
            final String d = new SimpleDateFormat("yyyy.MM.dd__").format(new Date()) + MSConst.TIME_MAX_AGE_FOR_DIFF + ":00:00";
            final Date maxDiff = new SimpleDateFormat("yyyy.MM.dd__HH:mm:ss").parse(d);
            final Date filmliste = getAgeAsDate();
            if (filmliste != null) {
                return filmliste.getTime() < maxDiff.getTime();
            }
        } catch (Exception ignored) {
        }
        return true;
    }

    /**
     * Check if list is older than specified parameter.
     *
     * @param sekunden The age in seconds.
     * @return true if older.
     */
    public synchronized boolean isOlderThan(int sekunden) {
        int ret = getAge();
        if (ret != 0) {
            MSLog.systemMeldung("Die Filmliste ist " + ret / 60 + " Minuten alt");
        }
        return ret > sekunden;
    }

    public synchronized void writeMetaData() {
        for (int i = 0; i < metaDaten.length; ++i) {
            metaDaten[i] = "";
        }
        metaDaten[ListeFilme.FILMLISTE_DATUM_NR] = getJetzt_ddMMyyyy_HHmm();
        metaDaten[ListeFilme.FILMLISTE_DATUM_GMT_NR] = getJetzt_ddMMyyyy_HHmm_gmt();
        metaDaten[ListeFilme.FILMLISTE_ID_NR] = createChecksum(metaDaten[ListeFilme.FILMLISTE_DATUM_GMT_NR]);
        metaDaten[ListeFilme.FILMLISTE_VERSION_NR] = MSConst.VERSION_FILMLISTE;
        metaDaten[ListeFilme.FILMLISTE_PRGRAMM_NR] = MSConst.PROGRAMMNAME + MSFunktionen.getProgVersionString() + " - Compiled: " + MSFunktionen.getCompileDate();
    }

    /**
     * Create a checksum string as a unique identifier.
     *
     * @param input The base string for the checksum.
     * @return MD5-hashed checksum string.
     */
    private String createChecksum(String input) {
        StringBuilder sb = new StringBuilder();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes());
            byte[] digest = md.digest();
            for (byte b : digest) {
                sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
        } catch (Exception ignored) {
        }
        return sb.toString();
    }

    private String getJetzt_ddMMyyyy_HHmm() {
        SimpleDateFormat formatter = new SimpleDateFormat(DATUM_ZEIT_FORMAT);
        return formatter.format(new Date());
    }

    private String getJetzt_ddMMyyyy_HHmm_gmt() {
        SimpleDateFormat formatter = new SimpleDateFormat(DATUM_ZEIT_FORMAT);
        formatter.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
        return formatter.format(new Date());
    }

    /**
     * Erstellt ein StringArray der Themen eines Senders oder wenn "sender" leer, aller Sender.
     * Ist für die Filterfelder in GuiFilme.
     */
    @SuppressWarnings("unchecked")
    public synchronized void themenLaden() {
        TreeSet<String> senderSet = new TreeSet<>();
        // der erste Sender ist ""
        senderSet.add("");
        // Sendernamen gibts nur in einer Schreibweise
        // doppelte Einträge nicht hinzufügen.
        for (DatenFilm film : this) {
            final String str = film.arr[DatenFilm.FILM_SENDER_NR];
            if (!senderSet.contains(str)) {
                senderSet.add(str);
            }
        }
        sender = senderSet.toArray(new String[senderSet.size()]);
        senderSet.clear();

        //für den Sender "" sind alle Themen im themenPerSender[0]
        themenPerSender = new String[sender.length][];
        TreeSet<String>[] tree = new TreeSet[sender.length];
        HashSet<String>[] hashSet = new HashSet[sender.length];
        for (int i = 0; i < tree.length; ++i) {
            tree[i] = new TreeSet<>(msearch.tool.GermanStringSorter.getInstance());
            tree[i].add("");
            hashSet[i] = new HashSet<>();
        }

        //alle Themen
        String filmThema, filmSender;
        for (DatenFilm film : this) {
            filmSender = film.arr[DatenFilm.FILM_SENDER_NR];
            filmThema = film.arr[DatenFilm.FILM_THEMA_NR];
            //hinzufügen
            if (!hashSet[0].contains(filmThema)) {
                hashSet[0].add(filmThema);
                tree[0].add(filmThema);
            }
            for (int i = 1; i < sender.length; ++i) {
                if (filmSender.equals(sender[i])) {
                    if (!hashSet[i].contains(filmThema)) {
                        hashSet[i].add(filmThema);
                        tree[i].add(filmThema);
                    }
                }
            }
        }
        for (int i = 0; i < themenPerSender.length; ++i) {
            themenPerSender[i] = tree[i].toArray(new String[tree[i].size()]);
            tree[i].clear();
            hashSet[i].clear();
        }
    }
}
