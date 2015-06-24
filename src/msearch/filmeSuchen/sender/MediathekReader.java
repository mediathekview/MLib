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

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import msearch.daten.DatenFilm;
import msearch.filmeSuchen.MSFilmeSuchen;
import msearch.filmeSuchen.MSGetUrl;
import msearch.tool.GermanStringSorter;
import msearch.tool.MSConfig;
import msearch.tool.MSLog;
import org.apache.commons.lang3.StringEscapeUtils;

public class MediathekReader implements Runnable {

    public String sendername = ""; // ist der Name, den der Mediathekreader hat, der ist eindeutig
    int maxThreadLaufen = 4;
    long wartenSeiteLaden = 500; //ms, Basiswert zu dem dann der Faktor multipliziert wird
    boolean updateOn = false;
    int threads = 0;
    int max = 0;
    int progress = 0;
    int startPrio = 1; // es gibt die Werte: 0->startet sofort, 1->später und 2->zuletzt
    LinkedListUrl listeThemen = new LinkedListUrl();
    LinkedList<String> listeAllThemen = new LinkedList<>();
    MSGetUrl getUrlIo;
    MSFilmeSuchen mSearchFilmeSuchen;

    public MediathekReader(MSFilmeSuchen mmSearchFilmeSuchen, String name, int ssenderMaxThread, int ssenderWartenSeiteLaden, int sstartPrio) {
        mSearchFilmeSuchen = mmSearchFilmeSuchen;
        wartenSeiteLaden = ssenderWartenSeiteLaden;
        getUrlIo = new MSGetUrl(ssenderWartenSeiteLaden);
        sendername = name;
        maxThreadLaufen = ssenderMaxThread;
        startPrio = sstartPrio;
    }

    //===================================
    // public 
    //===================================
    class LinkedListUrl extends LinkedList<String[]> {

        synchronized boolean addUrl(String[] e) {
            // e[0] ist immer die URL
            if (!istInListe(this, e[0], 0)) {
                return super.add(e);
            }
            return false;
        }

        synchronized String[] getListeThemen() {
            return this.pollFirst();
        }
    }

    public int getStartPrio() {
        return startPrio;
    }

    public int getThreads() {
        return maxThreadLaufen;
    }

    public long getWaitTime() {
        return wartenSeiteLaden;
    }

    public boolean checkNameSenderFilmliste(String name) {
        // ist der Name der in der Tabelle Filme angezeigt wird
        return sendername.equalsIgnoreCase(name);
    }

    public String getNameSender() {
        return sendername;
    }

    public void delSenderInAlterListe(String sender) {
        mSearchFilmeSuchen.listeFilmeAlt.deleteAllFilms(sender);
    }

    @Override
    public void run() {
        //alles laden
        try {
            updateOn = false;
            threads = 0;
            addToList();
        } catch (Exception ex) {
            MSLog.fehlerMeldung(397543600, ex, sendername);
        }
    }

    void addToList() {
        //wird überschrieben, hier werden die Filme gesucht
    }

    void addFilm(DatenFilm film, boolean urlPruefen) {
        if (urlPruefen) {
            if (mSearchFilmeSuchen.listeFilmeNeu.getFilmByUrl(film.arr[DatenFilm.FILM_URL_NR]) == null) {
                addFilm(film);
            }
        } else {
            addFilm(film);
        }
    }

    void addFilm(DatenFilm film) {
        if (film.arr[DatenFilm.FILM_GROESSE_NR].isEmpty()) {
            film.arr[DatenFilm.FILM_GROESSE_NR] = mSearchFilmeSuchen.listeFilmeAlt.getDateiGroesse(film.arr[DatenFilm.FILM_URL_NR], film.arr[DatenFilm.FILM_SENDER_NR]);
        }
        film.setUrlHistory();
        film.setGeo();
        mSearchFilmeSuchen.listeFilmeNeu.addFilmVomSender(film);
    }

    DatenFilm istInFilmListe(String sender, String thema, String titel) {
        return mSearchFilmeSuchen.listeFilmeNeu.istInFilmListe(sender, thema, titel);
    }

    boolean istInListe(LinkedList<String[]> liste, String str, int nr) {
        boolean ret = false;
        Iterator<String[]> it = liste.listIterator();
        while (it.hasNext()) {
            if (it.next()[nr].equals(str)) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    boolean istInListe(LinkedList<String> liste, String str) {
        boolean ret = false;
        Iterator<String> it = liste.listIterator();
        while (it.hasNext()) {
            if (it.next().equals(str)) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    String checkThema(String thema) {
        thema = StringEscapeUtils.unescapeXml(thema.trim());
        thema = StringEscapeUtils.unescapeHtml4(thema.trim());
        if (listeAllThemen.contains(thema)) {
            return thema;
        } else {
            return sendername;
        }
    }

    // Meldungen
    synchronized void meldungStart() {
        max = 0;
        progress = 0;
        MSLog.systemMeldung("===============================================================");
        MSLog.systemMeldung("Starten[" + ((MSConfig.senderAllesLaden) ? "alles" : "update") + "] " + sendername + ": " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
        MSLog.systemMeldung("   maxThreadLaufen: " + maxThreadLaufen);
        MSLog.systemMeldung("   wartenSeiteLaden: " + wartenSeiteLaden);
        MSLog.systemMeldung("");
        mSearchFilmeSuchen.melden(sendername, max, progress, "" /* text */);
    }

    synchronized void meldungAddMax(int mmax) {
        max += mmax;
        mSearchFilmeSuchen.melden(sendername, max, progress, "" /* text */);
    }

    synchronized void meldungAddThread() {
        ++threads;
        mSearchFilmeSuchen.melden(sendername, max, progress, "" /* text */);
    }

    synchronized void meldungProgress(String text) {
        ++progress;
        mSearchFilmeSuchen.melden(sendername, max, progress, text);
    }

    synchronized void meldung(String text) {
        mSearchFilmeSuchen.melden(sendername, max, progress, text);
    }

    synchronized void meldungThreadUndFertig() {
        --threads;
        if (threads <= 0) {
            //wird erst ausgeführt wenn alle Threads beendet sind
            mSearchFilmeSuchen.meldenFertig(sendername);
        } else {
            // läuft noch was
            mSearchFilmeSuchen.melden(sendername, max, progress, "" /* text */);
        }
    }

    final static int TIMEOUT = 3000; // ms //ToDo evtl. wieder kürzen!!

    public static boolean urlExists(String url) {
        // liefert liefert true, wenn es die URL gibt
        int retCode;
        if (!url.toLowerCase().startsWith("http")) {
            return false;
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestProperty("User-Agent", MSConfig.getUserAgent());
            conn.setReadTimeout(TIMEOUT);
            conn.setConnectTimeout(TIMEOUT);
            if ((retCode = conn.getResponseCode()) < 400) {
                return true;
            } else if (retCode == 403) {
                // aber sie gibt es :)
                return true;
            }
            conn.disconnect();
        } catch (Exception ignored) {
        }
        return false;
    }

    String addsUrl(String pfad1, String pfad2) {
        String ret = "";
        if (pfad1 != null && pfad2 != null) {
            if (!pfad1.equals("") && !pfad2.equals("")) {
                if (pfad1.charAt(pfad1.length() - 1) == '/') {
                    ret = pfad1.substring(0, pfad1.length() - 1);
                } else {
                    ret = pfad1;
                }
                if (pfad2.charAt(0) == '/') {
                    ret += pfad2;
                } else {
                    ret += '/' + pfad2;
                }
            }
        }
        if (ret.equals("")) {
            MSLog.fehlerMeldung(469872800, pfad1 + " " + pfad2);
        }
        return ret;
    }

    static void listeSort(LinkedList<String[]> liste, int stelle) {
        //Stringliste alphabetisch sortieren
        GermanStringSorter sorter = GermanStringSorter.getInstance();
        if (liste != null) {
            String str1;
            String str2;
            for (int i = 1; i < liste.size(); ++i) {
                for (int k = i; k > 0; --k) {
                    str1 = liste.get(k - 1)[stelle];
                    str2 = liste.get(k)[stelle];
                    // if (str1.compareToIgnoreCase(str2) > 0) {
                    if (sorter.compare(str1, str2) > 0) {
                        liste.add(k - 1, liste.remove(k));
                    } else {
                        break;
                    }
                }
            }
        }
    }

    static long extractDuration(String dauer) {
        long dauerInSeconds = 0;
        if (dauer.isEmpty()) {
            return 0;
        }
        try {
            if (dauer.contains("min")) {
                dauer = dauer.replace("min", "").trim();
                dauerInSeconds = Long.parseLong(dauer) * 60;
            } else {
                String[] parts = dauer.split(":");
                long power = 1;
                for (int i = parts.length - 1; i >= 0; i--) {
                    dauerInSeconds += Long.parseLong(parts[i]) * power;
                    power *= 60;
                }
            }
        } catch (Exception ex) {
            return 0;
        }
        return dauerInSeconds;
    }

    static long extractDurationSec(String dauer) {
        long dauerInSeconds = 0;
        if (dauer.isEmpty()) {
            return 0;
        }
        try {
            dauerInSeconds = Long.parseLong(dauer);
        } catch (Exception ex) {
            return 0;
        }
        return dauerInSeconds;
    }
}
