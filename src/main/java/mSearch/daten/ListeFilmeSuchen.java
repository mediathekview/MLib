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

import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import mSearch.filmeSuchen.sender.*;
import mSearch.Config;
import mSearch.Const;
import mSearch.tool.Duration;
import mSearch.tool.FileSize;
import mSearch.tool.Functions;
import mSearch.tool.Log;

public class ListeFilmeSuchen extends ListeFilme {

    private static final long serialVersionUID = 1L;

    public synchronized void updateListe(ListeFilme listeEinsortieren, boolean index /* Vergleich über Index, sonst nur URL */, boolean ersetzen) {
        // in eine vorhandene Liste soll eine andere Filmliste einsortiert werden
        // es werden nur Filme die noch nicht vorhanden sind, einsortiert
        // "ersetzen": true: dann werden gleiche (index/URL) in der Liste durch neue ersetzt
        final HashSet<String> hash = new HashSet<>(listeEinsortieren.size() + 1, 1);

        if (ersetzen) {
            // ==========================================
            for (DatenFilm f : listeEinsortieren) {
                if (f.arr[DatenFilm.FILM_SENDER].equals(MediathekKika.SENDERNAME)) {
                    // beim KIKA ändern sich die URLs laufend
                    hash.add(f.arr[DatenFilm.FILM_THEMA] + f.arr[DatenFilm.FILM_TITEL]);
                }
            }

            Iterator<DatenFilm> it = this.iterator();
            while (it.hasNext()) {
                DatenFilm f = it.next();
                if (f.arr[DatenFilm.FILM_SENDER].equals(MediathekKika.SENDERNAME)) {
                    // beim KIKA ändern sich die URLs laufend
                    if (hash.contains(f.arr[DatenFilm.FILM_THEMA] + f.arr[DatenFilm.FILM_TITEL])) {
                        it.remove();
                    }
                }
            }

            //listeEinsortieren.forEach((e) -> liste.addInit(e));
            listeEinsortieren.forEach(this::addInit);
        } else {
            // ==============================================
            for (DatenFilm f : this) {
                if (f.arr[DatenFilm.FILM_SENDER].equals(MediathekKika.SENDERNAME)) {
                    // beim KIKA ändern sich die URLs laufend
                    hash.add(f.arr[DatenFilm.FILM_THEMA] + f.arr[DatenFilm.FILM_TITEL]);
                }
            }

            for (DatenFilm f : listeEinsortieren) {
                if (f.arr[DatenFilm.FILM_SENDER].equals(MediathekKika.SENDERNAME)) {
                    if (!hash.contains(f.arr[DatenFilm.FILM_THEMA] + f.arr[DatenFilm.FILM_TITEL])) {
                        this.addInit(f);
                    }
                }
            }
        }
        hash.clear();

        super.updateListe(listeEinsortieren, index, ersetzen);

    }

    public synchronized void cleanList() {
        // für den BR: alle Filme mit Thema "BR" die es auch in einem anderen Thema gibt löschen
        // wird vorerst nicht verwendet: findet nur ~200 Filme von über 3000
        int count = 0;
        final SimpleDateFormat sdfClean = new SimpleDateFormat(ListeFilme.DATUM_ZEIT_FORMAT);
        Log.sysLog("cleanList start: " + sdfClean.format(System.currentTimeMillis()));
        ListeFilme tmp = this.stream().filter(datenFilm -> datenFilm.arr[DatenFilm.FILM_SENDER].equals(MediathekBr.SENDERNAME))
                .filter(datenFilm -> datenFilm.arr[DatenFilm.FILM_THEMA].equals(MediathekBr.SENDERNAME))
                .collect(Collectors.toCollection(ListeFilme::new));

        for (DatenFilm tFilm : tmp) {
            for (DatenFilm datenFilm : this) {
                if (datenFilm.arr[DatenFilm.FILM_SENDER].equals(MediathekBr.SENDERNAME)) {
                    if (!datenFilm.arr[DatenFilm.FILM_THEMA].equals(MediathekBr.SENDERNAME)) {
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

    public synchronized void liveStreamEintragen() {
        // ARD
        this.addFilmVomSender(DatenFilm.getDatenFilmLiveStream(MediathekArd.SENDERNAME, "", "http://daserste_live-lh.akamaihd.net/i/daserste_de@91204/master.m3u8", "http://www.ardmediathek.de/tv/live?kanal=Alle"));
        this.addFilmVomSender(DatenFilm.getDatenFilmLiveStream(MediathekArd.SENDERNAME, " Alpha", "http://livestreams.br.de/i/bralpha_germany@119899/master.m3u8", "http://www.ardmediathek.de/tv/live?kanal=Alle"));
        this.addFilmVomSender(DatenFilm.getDatenFilmLiveStream(MediathekArd.SENDERNAME, " Tagesschau", "http://tagesschau-lh.akamaihd.net/i/tagesschau_1@119231/master.m3u8", "http://www.ardmediathek.de/tv/live?kanal=Alle"));

        // BR
        this.addFilmVomSender(DatenFilm.getDatenFilmLiveStream(MediathekBr.SENDERNAME, "", "http://livestreams.br.de/i/bfsnord_germany@119898/master.m3u8", "http://www.ardmediathek.de/tv/live?kanal=Alle"));
        // ARTE
        this.addFilmVomSender(DatenFilm.getDatenFilmLiveStream(MediathekArte_de.SENDERNAME, "", "http://delive.artestras.cshls.lldns.net/artestras/contrib/delive.m3u8", "http://www.ardmediathek.de/tv/live?kanal=Alle"));
        // HR
        this.addFilmVomSender(DatenFilm.getDatenFilmLiveStream(MediathekHr.SENDERNAME, "", "http://live1_hr-lh.akamaihd.net/i/hr_fernsehen@75910/master.m3u8", "http://www.ardmediathek.de/tv/live?kanal=Alle"));
        // KiKa
        this.addFilmVomSender(DatenFilm.getDatenFilmLiveStream(MediathekKika.SENDERNAME, "", "http://kika_geo-lh.akamaihd.net/i/livetvkika_de@75114/master.m3u8", "http://www.ardmediathek.de/tv/live?kanal=Alle"));
        // MDR
        this.addFilmVomSender(DatenFilm.getDatenFilmLiveStream(MediathekMdr.SENDERNAME, "", "http://mdr_th_hls-lh.akamaihd.net/i/livetvmdrthueringen_de@106903/master.m3u8", "http://www.ardmediathek.de/tv/live?kanal=Alle"));
        // NDR
        this.addFilmVomSender(DatenFilm.getDatenFilmLiveStream(MediathekNdr.SENDERNAME, "", "http://ndr_fs-lh.akamaihd.net/i/ndrfs_nds@119224/master.m3u8", "http://www.ardmediathek.de/tv/live?kanal=Alle"));
        // RBB
        this.addFilmVomSender(DatenFilm.getDatenFilmLiveStream(MediathekRbb.SENDERNAME, "", "http://rbb_live-lh.akamaihd.net/i/rbb_brandenburg@107638/master.m3u8", "http://www.ardmediathek.de/tv/live?kanal=Alle"));
        // SR
        this.addFilmVomSender(DatenFilm.getDatenFilmLiveStream(MediathekSr.SENDERNAME, "", "http://live2_sr-lh.akamaihd.net/i/sr_universal02@107595/master.m3u8", "http://www.ardmediathek.de/tv/live?kanal=Alle"));
        // SWR
        this.addFilmVomSender(DatenFilm.getDatenFilmLiveStream(MediathekSwr.SENDERNAME, "", "http://swrbw-lh.akamaihd.net/i/swrbw_live@196738/master.m3u8", "http://www.ardmediathek.de/tv/live?kanal=Alle"));
        // WDR
        this.addFilmVomSender(DatenFilm.getDatenFilmLiveStream(MediathekWdr.SENDERNAME, "", "http://wdr_fs_geo-lh.akamaihd.net/i/wdrfs_geogeblockt@112044/master.m3u8", "http://www.ardmediathek.de/tv/live?kanal=Alle"));
        // 3sat
        this.addFilmVomSender(DatenFilm.getDatenFilmLiveStream(Mediathek3Sat.SENDERNAME, "", "http://zdf0910-lh.akamaihd.net/i/dach10_v1@392872/master.m3u8", "http://www.zdf.de/ZDFmediathek/hauptnavigation/live"));

        // ZDF
        this.addFilmVomSender(DatenFilm.getDatenFilmLiveStream(MediathekZdf.SENDERNAME, "", "http://zdf1314-lh.akamaihd.net/i/de14_v1@392878/master.m3u8", "http://www.zdf.de/ZDFmediathek/hauptnavigation/live"));
        this.addFilmVomSender(DatenFilm.getDatenFilmLiveStream(MediathekZdf.SENDERNAME, ".info", "http://zdf1112-lh.akamaihd.net/i/de12_v1@392882/master.m3u8", "http://www.zdf.de/ZDFmediathek/hauptnavigation/live"));
        this.addFilmVomSender(DatenFilm.getDatenFilmLiveStream(MediathekZdf.SENDERNAME, ".neo", "http://zdf1314-lh.akamaihd.net/i/de13_v1@392877/master.m3u8", "http://www.zdf.de/ZDFmediathek/hauptnavigation/live"));
        this.addFilmVomSender(DatenFilm.getDatenFilmLiveStream(MediathekZdf.SENDERNAME, ".kultur", "http://zdf1112-lh.akamaihd.net/i/de11_v1@392881/master.m3u8", "http://www.zdf.de/ZDFmediathek/hauptnavigation/live"));
        this.addFilmVomSender(DatenFilm.getDatenFilmLiveStream(MediathekZdf.SENDERNAME, ".heute", "http://zdf0102-lh.akamaihd.net/i/none01_v1@392849/master.m3u8", "http://www.zdf.de/ZDFmediathek/hauptnavigation/live"));

        // ORF
        this.addFilmVomSender(DatenFilm.getDatenFilmLiveStream(MediathekOrf.SENDERNAME, "-1", "http://apasfiisl.apa.at/ipad/orf1_q4a/orf.sdp/playlist.m3u8", "http://tvthek.orf.at/live"));
        this.addFilmVomSender(DatenFilm.getDatenFilmLiveStream(MediathekOrf.SENDERNAME, "-2", "http://apasfiisl.apa.at/ipad/orf2_q4a/orf.sdp/playlist.m3u8", "http://tvthek.orf.at/live"));
        this.addFilmVomSender(DatenFilm.getDatenFilmLiveStream(MediathekOrf.SENDERNAME, "-3", "http://apasfiisl.apa.at/ipad/orf3_q4a/orf.sdp/playlist.m3u8", "http://tvthek.orf.at/live"));

        this.addFilmVomSender(DatenFilm.getDatenFilmLiveStream(MediathekOrf.SENDERNAME, "-Sport", "http://apasfiisl.apa.at/ipad/orfs_q4a/orf.sdp/playlist.m3u8", "http://tvthek.orf.at/live"));
    }

}
