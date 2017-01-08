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
package mSearch.daten;

import mSearch.Const;
import mSearch.tool.Duration;
import mSearch.tool.FileSize;
import mSearch.tool.Functions;
import mSearch.tool.Log;

import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("serial")
public class ListeFilme extends ArrayList<DatenFilm> {
    public static final String THEMA_LIVE = "Livestream";
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

    public synchronized boolean importFilmliste(DatenFilm film) {
        // hier nur beim Laden aus einer fertigen Filmliste mit der GUI
        // die Filme sind schon sortiert, nur die Nummer muss noch ergänzt werden
        film.nr = nr++;
        return addInit(film);
    }

    public synchronized boolean addFilmVomSender(DatenFilm film) {
        // Filme die beim Sender gesucht wurden (und nur die) hier eintragen, nur für die MediathekReader!!
        // ist: "Sender-Thema-URL" schon vorhanden, wird sie verworfen

        Functions.unescape(film);

        // erst mal schauen obs das schon gibt
        final String idx = film.getIndex();
        for (DatenFilm datenFilm : this) {
            if (datenFilm.getIndex().equals(idx)) {
                return false;
            }
        }
        return addInit(film);
    }

    public synchronized void updateListe(ListeFilme listeEinsortieren, boolean index /* Vergleich über Index, sonst nur URL */, boolean ersetzen) {
        // in eine vorhandene Liste soll eine andere Filmliste einsortiert werden
        // es werden nur Filme die noch nicht vorhanden sind, einsortiert
        // "ersetzen": true: dann werden gleiche (index/URL) in der Liste durch neue ersetzt
        final HashSet<String> hash = new HashSet<>(listeEinsortieren.size() + 1, 1);

        if (ersetzen) {
            // ==========================================
            for (DatenFilm f : listeEinsortieren) {
                if (f.arr[DatenFilm.FILM_SENDER].equals(Const.KIKA)) {
                    // beim KIKA ändern sich die URLs laufend
                    hash.add(f.arr[DatenFilm.FILM_THEMA] + f.arr[DatenFilm.FILM_TITEL]);
                } else if (index) {
                    hash.add(f.getIndex());
                } else {
                    hash.add(DatenFilm.getUrl(f));
                }
            }

            Iterator<DatenFilm> it = this.iterator();
            while (it.hasNext()) {
                DatenFilm f = it.next();
                if (f.arr[DatenFilm.FILM_SENDER].equals(Const.KIKA)) {
                    // beim KIKA ändern sich die URLs laufend
                    if (hash.contains(f.arr[DatenFilm.FILM_THEMA] + f.arr[DatenFilm.FILM_TITEL])) {
                        it.remove();
                    }
                } else if (index) {
                    if (hash.contains(f.getIndex())) {
                        it.remove();
                    }
                } else if (hash.contains(DatenFilm.getUrl(f))) {
                    it.remove();
                }
            }

            listeEinsortieren.forEach(this::addInit);
        } else {
            // ==============================================
            for (DatenFilm f : this) {
                if (f.arr[DatenFilm.FILM_SENDER].equals(Const.KIKA)) {
                    // beim KIKA ändern sich die URLs laufend
                    hash.add(f.arr[DatenFilm.FILM_THEMA] + f.arr[DatenFilm.FILM_TITEL]);
                } else if (index) {
                    hash.add(f.getIndex());
                } else {
                    hash.add(DatenFilm.getUrl(f));
                }
            }

            for (DatenFilm f : listeEinsortieren) {
                if (f.arr[DatenFilm.FILM_SENDER].equals(Const.KIKA)) {
                    if (!hash.contains(f.arr[DatenFilm.FILM_THEMA] + f.arr[DatenFilm.FILM_TITEL])) {
                        addInit(f);
                    }
                } else if (index) {
                    if (!hash.contains(f.getIndex())) {
                        addInit(f);
                    }
                } else if (!hash.contains(DatenFilm.getUrl(f))) {
                    addInit(f);
                }
            }
        }
        hash.clear();
    }

//    public synchronized void addLive(ListeFilme listeEinsortieren) {
//        // live-streams einfügen, es werde die vorhandenen ersetzt!
//
//        if (listeEinsortieren.size() <= 0) {
//            //dann wars wohl nix
//            return;
//        }
//
//        Iterator<DatenFilm> it = this.iterator();
//        while (it.hasNext()) {
//            DatenFilm f = it.next();
//            if (f.arr[DatenFilm.FILM_THEMA].equals(ListeFilme.THEMA_LIVE)) {
//                it.remove();
//            }
//        }
//        listeEinsortieren.forEach(this::add);
//    }

//    final int COUNTER_MAX = 20;
//    int counter = 0;
//    int treffer = 0;
//
//    public synchronized int updateListeOld(ListeFilme vonListe, ListeFilme listeEinsortieren) {
//        // in eine vorhandene Liste soll eine andere Filmliste einsortiert werden
//        // es werden nur Filme die noch nicht vorhanden sind, einsortiert
//        counter = 0;
//        treffer = 0;
//        int size = listeEinsortieren.size();
//
//        HashSet<String> hash = new HashSet<>(listeEinsortieren.size() + 1, 1);
//
//        // ==============================================
//        // nach "Thema-Titel" suchen
//        vonListe.stream().forEach((f) -> hash.add(f.getIndexAddOld()));
//        listeEinsortieren.removeIf((f) -> hash.contains(f.getIndexAddOld()));
//        hash.clear();
//
//        Log.sysLog("===== Liste einsortieren Hash =====");
//        Log.sysLog("Liste einsortieren, Anzahl: " + size);
//        Log.sysLog("Liste einsortieren, entfernt: " + (size - listeEinsortieren.size()));
//        Log.sysLog("Liste einsortieren, noch einsortieren: " + listeEinsortieren.size());
//        Log.sysLog("");
//        size = listeEinsortieren.size();
//
//        // ==============================================
//        // nach "URL" suchen
//        vonListe.stream().forEach((f) -> hash.add(DatenFilm.getUrl(f)));
//        listeEinsortieren.removeIf((f) -> hash.contains(DatenFilm.getUrl(f)));
//        hash.clear();
//
//        Log.sysLog("===== Liste einsortieren URL =====");
//        Log.sysLog("Liste einsortieren, Anzahl: " + size);
//        Log.sysLog("Liste einsortieren, entfernt: " + (size - listeEinsortieren.size()));
//        Log.sysLog("Liste einsortieren, noch einsortieren: " + listeEinsortieren.size());
//        Log.sysLog("");
//        size = listeEinsortieren.size();
//
//        // Rest nehmen wir wenn noch online
//        for (int i = 0; i < COUNTER_MAX; ++i) {
//            new Thread(new AddOld(listeEinsortieren)).start();
//        }
//        int count = 0;
//        final int COUNT_MAX = 300; // 10 Minuten
//        stopOld = false;
//        while (!Config.getStop() && counter > 0) {
//            try {
//                System.out.println("s: " + 2 * (count++) + "  Liste: " + listeEinsortieren.size() + "  Treffer: " + treffer + "   Threads: " + counter);
//                if (count > COUNT_MAX) {
//                    // dann haben wir mehr als 10 Minuten und: Stop
//                    Log.sysLog("===== Liste einsortieren: ABBRUCH =====");
//                    Log.sysLog("COUNT_MAX erreicht [s]: " + COUNT_MAX * 2);
//                    Log.sysLog("");
//                    stopOld = true;
//                }
//                wait(2000);
//            } catch (InterruptedException ignored) {
//            }
//        }
//        Log.sysLog("===== Liste einsortieren: Noch online =====");
//        Log.sysLog("Liste einsortieren, Anzahl: " + size);
//        Log.sysLog("Liste einsortieren, entfernt: " + (size - treffer));
//        Log.sysLog("");
//        Log.sysLog("In Liste einsortiert: " + treffer);
//        Log.sysLog("");
//        return treffer;
//    }
//    private boolean stopOld = false;
//
//    private class AddOld implements Runnable {
//
//        private DatenFilm film;
//        private final ListeFilme listeOld;
//        private final int MIN_SIZE_ADD_OLD = 5; //REST eh nur Trailer
//
//        public AddOld(ListeFilme listeOld) {
//            this.listeOld = listeOld;
//            ++counter;
//        }
//
//        @Override
//        public void run() {
//            while (!stopOld && (film = popOld(listeOld)) != null) {
//                long size = FileSize.laengeLong(film.arr[DatenFilm.FILM_URL]);
//                if (size > MIN_SIZE_ADD_OLD) {
//                    addOld(film);
//                }
//            }
//            --counter;
//        }
//    }
//
//    private synchronized DatenFilm popOld(ListeFilme listeOld) {
//        if (listeOld.size() > 0) {
//            return listeOld.remove(0);
//        }
//        return null;
//    }
//
//    private synchronized boolean addOld(DatenFilm film) {
//        ++treffer;
//        film.init();
//        return add(film);
//    }
    private boolean addInit(DatenFilm film) {
        film.init();
        return add(film);
    }

    @Override
    public synchronized void clear() {
        nr = 1;
        neueFilme = false;
        super.clear();
    }

    public void cleanList() {
        // für den BR: alle Filme mit Thema "BR" die es auch in einem anderen Thema gibt löschen
        // wird vorerst nicht verwendet: findet nur ~200 Filme von über 3000
        int count = 0;
        final SimpleDateFormat sdfClean = new SimpleDateFormat(DATUM_ZEIT_FORMAT);
        Log.sysLog("cleanList start: " + sdfClean.format(System.currentTimeMillis()));
        ListeFilme tmp = this.stream().filter(datenFilm -> datenFilm.arr[DatenFilm.FILM_SENDER].equals(Const.BR))
                .filter(datenFilm -> datenFilm.arr[DatenFilm.FILM_THEMA].equals(Const.BR))
                .collect(Collectors.toCollection(ListeFilme::new));

        for (DatenFilm tFilm : tmp) {
            for (DatenFilm datenFilm : this) {
                if (datenFilm.arr[DatenFilm.FILM_SENDER].equals(Const.BR)) {
                    if (!datenFilm.arr[DatenFilm.FILM_THEMA].equals(Const.BR)) {
                        if (datenFilm.arr[DatenFilm.FILM_URL].equals(tFilm.arr[DatenFilm.FILM_URL])) {
                            this.remove(tFilm);
                            ++count;
                            break;
                        }
                    }
                }
            }
        }

        tmp.clear();

        Log.sysLog("cleanList stop: " + sdfClean.format(System.currentTimeMillis()));
        Log.sysLog("cleanList count: " + count);
    }

    public synchronized void check() {
        // zum Debuggen
        for (DatenFilm film : this) {
//            film.arr[DatenFilm.FILM_THEMA_NR] = FilenameUtils.cleanUnicode(film.arr[DatenFilm.FILM_THEMA_NR], "!!!!!!!!!!!!!");
//            film.arr[DatenFilm.FILM_TITEL_NR] = FilenameUtils.cleanUnicode(film.arr[DatenFilm.FILM_TITEL_NR], "!!!!!!!!!!!!!");
            String s = film.arr[DatenFilm.FILM_BESCHREIBUNG];
            film.arr[DatenFilm.FILM_BESCHREIBUNG] = Functions.removeHtml(film.arr[DatenFilm.FILM_BESCHREIBUNG]);
            if (!s.equals(film.arr[DatenFilm.FILM_BESCHREIBUNG])) {
                System.out.println("---------------------");
                System.out.println(s);
                System.out.println(film.arr[DatenFilm.FILM_BESCHREIBUNG]);
            }
            s = film.arr[DatenFilm.FILM_THEMA];
            film.arr[DatenFilm.FILM_THEMA] = Functions.removeHtml(film.arr[DatenFilm.FILM_THEMA]);
            if (!s.equals(film.arr[DatenFilm.FILM_THEMA])) {
                System.out.println("---------------------");
                System.out.println(s);
                System.out.println(film.arr[DatenFilm.FILM_THEMA]);
            }
            s = film.arr[DatenFilm.FILM_TITEL];
            film.arr[DatenFilm.FILM_TITEL] = Functions.removeHtml(film.arr[DatenFilm.FILM_TITEL]);
            if (!s.equals(film.arr[DatenFilm.FILM_TITEL])) {
                System.out.println("---------------------");
                System.out.println(s);
                System.out.println(film.arr[DatenFilm.FILM_TITEL]);
            }
            if (film.arr[DatenFilm.FILM_URL].contains(" ")) {
                System.out.println(film.arr[DatenFilm.FILM_URL]);
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
                    hashDoppelt.add(film.arr[DatenFilm.FILM_URL]);
                }
            } else if (!hash.contains(film.arr[DatenFilm.FILM_URL])) {
                hash.add(film.arr[DatenFilm.FILM_URL]);
            } else {
                // dann ist er mind. doppelt in der Liste
                hashDoppelt.add(film.arr[DatenFilm.FILM_URL]);
            }
        }
        it = this.iterator();
        while (it.hasNext()) {
            if (!hashDoppelt.contains(it.next().arr[DatenFilm.FILM_URL])) {
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
            if (film.arr[DatenFilm.FILM_SENDER].equals(sender)
                    && film.arr[DatenFilm.FILM_THEMA].equalsIgnoreCase(thema)
                    && film.arr[DatenFilm.FILM_TITEL].equalsIgnoreCase(titel)) {
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
        final HashSet<String> hashSet = new HashSet<>(orgListe.size() + 1, 1);

        for (DatenFilm film : orgListe) {
            final String s = film.arr[DatenFilm.FILM_SENDER] + film.arr[DatenFilm.FILM_THEMA] + film.arr[DatenFilm.FILM_TITEL] + film.arr[DatenFilm.FILM_URL];
            hashSet.add(s);
        }

        for (DatenFilm film : this) {
            final String s = film.arr[DatenFilm.FILM_SENDER] + film.arr[DatenFilm.FILM_THEMA] + film.arr[DatenFilm.FILM_TITEL] + film.arr[DatenFilm.FILM_URL];
            if (!hashSet.contains(s)) {
                ret.add(film);
            }
        }

        hashSet.clear();

        ret.metaDaten = metaDaten;
        return ret;
    }

    public synchronized String getFileSizeUrl(String url, String sender) {
        // ist deutlich schneller als
        // return this.parallelStream().filter(f -> f.arr[DatenFilm.FILM_URL_NR].equals(url)).filter(f -> !f.arr[DatenFilm.FILM_GROESSE_NR].isEmpty()).findAny().get().arr[DatenFilm.FILM_GROESSE_NR];
        for (DatenFilm film : this) {
            if (film.arr[DatenFilm.FILM_URL].equals(url)) {
                if (!film.arr[DatenFilm.FILM_GROESSE].isEmpty()) {
                    return film.arr[DatenFilm.FILM_GROESSE];
                } else {
                    break;
                }
            }
        }
        // dann ist der Film nicht in der Liste
        return FileSize.laengeString(url, sender);
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
            if (film.arr[DatenFilm.FILM_SENDER].equalsIgnoreCase(sender)) {
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
        Iterator<DatenFilm> it = this.iterator();
        while (it.hasNext()) {
            film = it.next();
            if (film.arr[DatenFilm.FILM_SENDER].equalsIgnoreCase(sender)) {
                it.remove();
            }
        }
    }

//    public synchronized void liveStreamEintragen() {
//        // ARD
//        addFilmVomSender(DatenFilm.getDatenFilmLiveStream(Const.ARD, "", "http://daserste_live-lh.akamaihd.net/i/daserste_de@91204/master.m3u8", "http://www.ardmediathek.de/tv/live?kanal=Alle"));
//        addFilmVomSender(DatenFilm.getDatenFilmLiveStream(Const.ARD, " Alpha", "http://livestreams.br.de/i/bralpha_germany@119899/master.m3u8", "http://www.ardmediathek.de/tv/live?kanal=Alle"));
//        addFilmVomSender(DatenFilm.getDatenFilmLiveStream(Const.ARD, " Tagesschau", "http://tagesschau-lh.akamaihd.net/i/tagesschau_1@119231/master.m3u8", "http://www.ardmediathek.de/tv/live?kanal=Alle"));
//
//        // BR
//        addFilmVomSender(DatenFilm.getDatenFilmLiveStream(Const.BR, "", "http://livestreams.br.de/i/bfsnord_germany@119898/master.m3u8", "http://www.ardmediathek.de/tv/live?kanal=Alle"));
//        // ARTE
//        addFilmVomSender(DatenFilm.getDatenFilmLiveStream(Const.ARTE_DE, "", "http://delive.artestras.cshls.lldns.net/artestras/contrib/delive.m3u8", "http://www.ardmediathek.de/tv/live?kanal=Alle"));
//        // HR
//        addFilmVomSender(DatenFilm.getDatenFilmLiveStream(Const.HR, "", "http://live1_hr-lh.akamaihd.net/i/hr_fernsehen@75910/master.m3u8", "http://www.ardmediathek.de/tv/live?kanal=Alle"));
//        // KiKa
//        addFilmVomSender(DatenFilm.getDatenFilmLiveStream(Const.KIKA, "", "http://kika_geo-lh.akamaihd.net/i/livetvkika_de@75114/master.m3u8", "http://www.ardmediathek.de/tv/live?kanal=Alle"));
//        // MDR
//        addFilmVomSender(DatenFilm.getDatenFilmLiveStream(Const.MDR, "", "http://mdr_th_hls-lh.akamaihd.net/i/livetvmdrthueringen_de@106903/master.m3u8", "http://www.ardmediathek.de/tv/live?kanal=Alle"));
//        // NDR
//        addFilmVomSender(DatenFilm.getDatenFilmLiveStream(Const.NDR, "", "http://ndr_fs-lh.akamaihd.net/i/ndrfs_nds@119224/master.m3u8", "http://www.ardmediathek.de/tv/live?kanal=Alle"));
//        // RBB
//        addFilmVomSender(DatenFilm.getDatenFilmLiveStream(Const.RBB, "", "http://rbb_live-lh.akamaihd.net/i/rbb_brandenburg@107638/master.m3u8", "http://www.ardmediathek.de/tv/live?kanal=Alle"));
//        // SR
//        addFilmVomSender(DatenFilm.getDatenFilmLiveStream(Const.SR, "", "http://live2_sr-lh.akamaihd.net/i/sr_universal02@107595/master.m3u8", "http://www.ardmediathek.de/tv/live?kanal=Alle"));
//        // SWR
//        addFilmVomSender(DatenFilm.getDatenFilmLiveStream(Const.SWR, "", "http://swrbw-lh.akamaihd.net/i/swrbw_live@196738/master.m3u8", "http://www.ardmediathek.de/tv/live?kanal=Alle"));
//        // WDR
//        addFilmVomSender(DatenFilm.getDatenFilmLiveStream(Const.WDR, "", "http://wdr_fs_geo-lh.akamaihd.net/i/wdrfs_geogeblockt@112044/master.m3u8", "http://www.ardmediathek.de/tv/live?kanal=Alle"));
//        // 3sat
//        addFilmVomSender(DatenFilm.getDatenFilmLiveStream(Const.DREISAT, "", "http://zdf0910-lh.akamaihd.net/i/dach10_v1@392872/master.m3u8", "http://www.zdf.de/ZDFmediathek/hauptnavigation/live"));
//
//        // ZDF
//        addFilmVomSender(DatenFilm.getDatenFilmLiveStream(Const.ZDF, "", "http://zdf1314-lh.akamaihd.net/i/de14_v1@392878/master.m3u8", "http://www.zdf.de/ZDFmediathek/hauptnavigation/live"));
//        addFilmVomSender(DatenFilm.getDatenFilmLiveStream(Const.ZDF, ".info", "http://zdf1112-lh.akamaihd.net/i/de12_v1@392882/master.m3u8", "http://www.zdf.de/ZDFmediathek/hauptnavigation/live"));
//        addFilmVomSender(DatenFilm.getDatenFilmLiveStream(Const.ZDF, ".neo", "http://zdf1314-lh.akamaihd.net/i/de13_v1@392877/master.m3u8", "http://www.zdf.de/ZDFmediathek/hauptnavigation/live"));
//        addFilmVomSender(DatenFilm.getDatenFilmLiveStream(Const.ZDF, ".kultur", "http://zdf1112-lh.akamaihd.net/i/de11_v1@392881/master.m3u8", "http://www.zdf.de/ZDFmediathek/hauptnavigation/live"));
//        addFilmVomSender(DatenFilm.getDatenFilmLiveStream(Const.ZDF, ".heute", "http://zdf0102-lh.akamaihd.net/i/none01_v1@392849/master.m3u8", "http://www.zdf.de/ZDFmediathek/hauptnavigation/live"));
//
//        // ORF
//        addFilmVomSender(DatenFilm.getDatenFilmLiveStream(Const.ORF, "-1", "http://apasfiisl.apa.at/ipad/orf1_q4a/orf.sdp/playlist.m3u8", "http://tvthek.orf.at/live"));
//        addFilmVomSender(DatenFilm.getDatenFilmLiveStream(Const.ORF, "-2", "http://apasfiisl.apa.at/ipad/orf2_q4a/orf.sdp/playlist.m3u8", "http://tvthek.orf.at/live"));
//        addFilmVomSender(DatenFilm.getDatenFilmLiveStream(Const.ORF, "-3", "http://apasfiisl.apa.at/ipad/orf3_q4a/orf.sdp/playlist.m3u8", "http://tvthek.orf.at/live"));
//
//        addFilmVomSender(DatenFilm.getDatenFilmLiveStream(Const.ORF, "-Sport", "http://apasfiisl.apa.at/ipad/orfs_q4a/orf.sdp/playlist.m3u8", "http://tvthek.orf.at/live"));
//
//    }

    public synchronized DatenFilm getFilmByUrl(final String url) {
        for (DatenFilm film : this) {
            if (film.arr[DatenFilm.FILM_URL].equalsIgnoreCase(url)) {
                return film;
            }
        }
        return null;
    }

    public synchronized void checkThema(String sender, LinkedList<String> liste, String thema) {
        this.stream().filter(film -> film.arr[DatenFilm.FILM_SENDER].equals(sender))
                .filter(film -> !film.arr[DatenFilm.FILM_THEMA].equals(ListeFilme.THEMA_LIVE)
                        && !liste.contains(film.arr[DatenFilm.FILM_THEMA]))
                .forEach(film -> film.arr[DatenFilm.FILM_THEMA] = thema);
    }

    public synchronized void getThema(String sender, LinkedList<String> liste) {
        this.stream().filter(film -> film.arr[DatenFilm.FILM_SENDER].equals(sender))
                .filter(film -> !liste.contains(film.arr[DatenFilm.FILM_THEMA]))
                .forEach(film -> liste.add(film.arr[DatenFilm.FILM_THEMA]));
    }

    public synchronized DatenFilm getFilmByUrl_klein_hoch_hd(String url) {
        // Problem wegen gleicher URLs
        // wird versucht, einen Film mit einer kleinen/Hoher/HD-URL zu finden
        DatenFilm ret = null;
        for (DatenFilm f : this) {
            if (f.arr[DatenFilm.FILM_URL].equals(url)) {
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
        if (metaDaten[ListeFilme.FILMLISTE_DATUM_GMT_NR].isEmpty()) {
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
        if (metaDaten[ListeFilme.FILMLISTE_DATUM_GMT_NR].isEmpty()) {
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
        if (!metaDaten[ListeFilme.FILMLISTE_DATUM_GMT_NR].isEmpty()) {
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
        return (isEmpty()) || (isOlderThan(Const.ALTER_FILMLISTE_SEKUNDEN_FUER_AUTOUPDATE));
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
            final String dateMaxDiff_str = new SimpleDateFormat("yyyy.MM.dd__").format(new Date()) + Const.TIME_MAX_AGE_FOR_DIFF + ":00:00";
            final Date dateMaxDiff = new SimpleDateFormat("yyyy.MM.dd__HH:mm:ss").parse(dateMaxDiff_str);
            final Date dateFilmliste = getAgeAsDate();
            if (dateFilmliste != null) {
                return dateFilmliste.getTime() < dateMaxDiff.getTime();
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
            Log.sysLog("Die Filmliste ist " + ret / 60 + " Minuten alt");
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
        metaDaten[ListeFilme.FILMLISTE_VERSION_NR] = Const.VERSION_FILMLISTE;
        metaDaten[ListeFilme.FILMLISTE_PRGRAMM_NR] = Const.PROGRAMMNAME + Functions.getProgVersionString(); //  + " - Compiled: " + Functions.getCompileDate();
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

    public synchronized long countNewFilms() {
        return this.stream().filter(DatenFilm::isNew).count();
    }

    private static final String THEME_SEARCH_TEXT = "Themen in Filmliste suchen";
    /**
     * Erstellt ein StringArray der Themen eines Senders oder wenn "sender" leer, aller Sender.
     * Ist für die Filterfelder in GuiFilme.
     */
    @SuppressWarnings("unchecked")
    public synchronized void themenLaden() {
        Duration.counterStart(THEME_SEARCH_TEXT);
        LinkedHashSet<String> senderSet = new LinkedHashSet<>(21);
        // der erste Sender ist ""
        senderSet.add("");

        for (DatenFilm film : this) {
            senderSet.add(film.arr[DatenFilm.FILM_SENDER]);
        }
        sender = senderSet.toArray(new String[senderSet.size()]);
        senderSet.clear();

        //für den Sender "" sind alle Themen im themenPerSender[0]
        final int senderLength = sender.length;
        themenPerSender = new String[senderLength][];
        TreeSet<String>[] tree = (TreeSet<String>[]) new TreeSet<?>[senderLength];
        HashSet<String>[] hashSet = (HashSet<String>[]) new HashSet<?>[senderLength];
        for (int i = 0; i < tree.length; ++i) {
            tree[i] = new TreeSet<>(mSearch.tool.GermanStringSorter.getInstance());
            tree[i].add("");
            hashSet[i] = new HashSet<>();
        }

        //alle Themen
        String filmThema, filmSender;
        for (DatenFilm film : this) {
            filmSender = film.arr[DatenFilm.FILM_SENDER];
            filmThema = film.arr[DatenFilm.FILM_THEMA];
            //hinzufügen
            if (!hashSet[0].contains(filmThema)) {
                hashSet[0].add(filmThema);
                tree[0].add(filmThema);
            }
            for (int i = 1; i < senderLength; ++i) {
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
        Duration.counterStop(THEME_SEARCH_TEXT);
    }
}
