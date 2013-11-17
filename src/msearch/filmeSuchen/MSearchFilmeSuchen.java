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
package msearch.filmeSuchen;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.event.EventListenerList;
import msearch.daten.ListeFilme;
import msearch.daten.MSearchConfig;
import msearch.filmeSuchen.sender.Mediathek3Sat;
import msearch.filmeSuchen.sender.MediathekArd;
import msearch.filmeSuchen.sender.MediathekArdPodcast;
import msearch.filmeSuchen.sender.MediathekArte_de;
import msearch.filmeSuchen.sender.MediathekArte_fr;
import msearch.filmeSuchen.sender.MediathekBr;
import msearch.filmeSuchen.sender.MediathekHr;
import msearch.filmeSuchen.sender.MediathekKika;
import msearch.filmeSuchen.sender.MediathekMdr;
import msearch.filmeSuchen.sender.MediathekNdr;
import msearch.filmeSuchen.sender.MediathekOrf;
import msearch.filmeSuchen.sender.MediathekRbb;
import msearch.filmeSuchen.sender.MediathekReader;
import msearch.filmeSuchen.sender.MediathekSrf;
import msearch.filmeSuchen.sender.MediathekSrfPod;
import msearch.filmeSuchen.sender.MediathekSwr;
import msearch.filmeSuchen.sender.MediathekWdr;
import msearch.filmeSuchen.sender.MediathekZdf;
import msearch.io.MSearchGetUrl;
import msearch.tool.DatumZeit;
import msearch.tool.GuiFunktionen;
import msearch.tool.MSearchLog;
import msearch.tool.MSearchUrlDateiGroesse;

public class MSearchFilmeSuchen {

    public ListeFilme listeFilmeNeu; // neu angelegte Liste und da kommen die neu gesuchten Filme rein
    public ListeFilme listeFilmeAlt; // ist die "alte" Liste, wird beim Aufruf übergeben und enthält am Ende das Ergebnis
    // private
    private LinkedList<MediathekReader> mediathekListe = new LinkedList<>();
    private EventListenerList listeners = new EventListenerList();
    private MSearchListeRunSender listeSenderLaufen = new MSearchListeRunSender();
    private Date startZeit = null;
    private Date stopZeit = null;
    private ArrayList<String> runde1 = new ArrayList<>();
    private ArrayList<String> runde2 = new ArrayList<>();
    private ArrayList<String> runde3 = new ArrayList<>();
    private String[] titel1 = {"Sender       ", "[min]", "Seiten", "Filme", "Fehler", "FVersuche", "FZeit[s]", "Anz-Proxy"};
    private String[] titel3 = {"Sender       ", "Geladen[MB]", "Nix", "Deflaet", "Gzip", "AnzGroesse", "Anz-403", "Anz-Proxy"};
    private final String TRENNER = " | ";
    private final String TTRENNER = " || ";
    private boolean allStarted = false;

    /**
     * ###########################################################################################################
     * Ablauf:
     * die gefundenen Filme kommen in die "listeFilme"
     * -> bei einem vollen Suchlauf: passiert nichts weiter
     * -> bei einem Update: "listeFilme" mit alter Filmliste auffüllen, URLs die es schon gibt werden verworfen
     * "listeFilme" ist dann die neue komplette Liste mit Filmen
     * ##########################################################################################################
     */
    public MSearchFilmeSuchen() {
        //Reader laden Spaltenweises Laden
        mediathekListe.add(new MediathekArd(this, 0));
        mediathekListe.add(new MediathekArdPodcast(this, 1));
        mediathekListe.add(new MediathekZdf(this, 0));
        mediathekListe.add(new MediathekArte_de(this, 1));
        mediathekListe.add(new MediathekArte_fr(this, 1));
        mediathekListe.add(new Mediathek3Sat(this, 0));
        mediathekListe.add(new MediathekSwr(this, 1));
        mediathekListe.add(new MediathekNdr(this, 1));
        mediathekListe.add(new MediathekKika(this, 0));
        // Spalte 2
        mediathekListe.add(new MediathekMdr(this, 0));
        mediathekListe.add(new MediathekWdr(this, 0));
        mediathekListe.add(new MediathekHr(this, 0));
        mediathekListe.add(new MediathekRbb(this, 1));
        mediathekListe.add(new MediathekBr(this, 0));
        mediathekListe.add(new MediathekSrf(this, 1));
        mediathekListe.add(new MediathekSrfPod(this, 0));
        mediathekListe.add(new MediathekOrf(this, 0));
    }

//    public void infoMeldung() {
//        MSearchLog.startMeldungen(this.getClass().getName());
//    }
    public void addAdListener(MSearchListenerFilmeLaden listener) {
        listeners.add(MSearchListenerFilmeLaden.class, listener);
    }

    /**
     * es werden alle Filme gesucht
     */
    public synchronized void filmeBeimSenderLaden(ListeFilme listeFilme) {
        allStarted = false;
        initStart(listeFilme);
        // die mReader nach Prio starten
        mrStarten(0);
        if (!MSearchConfig.getStop()) {
            mrWarten();
            mrStarten(1);
            allStarted = true;
        }
    }

    /**
     * es wird nur ein Sender aktualisiert
     *
     * @param nameSenderFilmliste
     */
    public void updateSender(String nameSenderFilmliste, ListeFilme listeFilme) {
        updateSender(new String[]{nameSenderFilmliste}, listeFilme);
    }

    /**
     * es wird nur einige Sender aktualisiert
     *
     * @param nameSender
     */
    public void updateSender(String[] nameSender, ListeFilme listeFilme) {
        // nur für den Mauskontext "Sender aktualisieren"
        allStarted = false;
        boolean starten = false;
        MSearchConfig.senderAllesLaden = false;
        MSearchConfig.updateFilmliste = true;
        initStart(listeFilme);
        Iterator<MediathekReader> it = mediathekListe.iterator();
        while (it.hasNext()) {
            MediathekReader reader = it.next();
            for (String s : nameSender) {
                if (reader.checkNameSenderFilmliste(s)) {
                    starten = true;
                    new Thread(reader).start();
                }
            }
        }
        allStarted = true;
        if (!starten) {
            // dann fertig
            meldenFertig("");
        }
    }

    public String[] getNamenSender() {
        // liefert eine Array mit allen Sendernamen
        LinkedList<String> liste = new LinkedList<>();
        Iterator<MediathekReader> it = mediathekListe.iterator();
        while (it.hasNext()) {
            liste.add(it.next().getNameSender());
        }
        GuiFunktionen.listeSort(liste);
        return liste.toArray(new String[]{});
    }

    public synchronized void melden(String sender, int max, int progress, String text) {
        MSearchRunSender runSender = listeSenderLaufen.getSender(sender);
        if (runSender != null) {
            runSender.max = max;
            runSender.progress = progress;
        } else {
            // Sender startet
            MSearchLog.systemMeldung("Starten[" + ((MSearchConfig.senderAllesLaden) ? "alles" : "update") + "] " + sender + ": " + DatumZeit.getJetzt_HH_MM_SS());
            listeSenderLaufen.add(new MSearchRunSender(sender, max, progress));
            //wird beim Start des Senders aufgerufen, 1x
            if (listeSenderLaufen.size() <= 1 /* erster Aufruf */) {
                notifyStart(new MSearchListenerFilmeLadenEvent(sender, text, listeSenderLaufen.getMax(), listeSenderLaufen.getProgress()));
            }
        }
        notifyProgress(new MSearchListenerFilmeLadenEvent(sender, text, listeSenderLaufen.getMax(), listeSenderLaufen.getProgress()));
        progressBar();
    }

    public void meldenFertig(String sender) {
        //wird ausgeführt wenn Sender beendet ist
        String zeile = "";
        MSearchLog.systemMeldung("-------------------------------------------------------------------------------------");
        MSearchLog.systemMeldung("Fertig " + sender + ": " + DatumZeit.getJetzt_HH_MM_SS() + ", Filme: " + listeFilmeNeu.countSender(sender));
        MSearchLog.systemMeldung("-------------------------------------------------------------------------------------");
        MSearchRunSender run = listeSenderLaufen.senderFertig(sender);
        if (run != null) {
            String groesse = (MSearchGetUrl.getSeitenZaehler(MSearchGetUrl.LISTE_SUMME_BYTE, run.sender) == 0) ? "<1" : Long.toString(MSearchGetUrl.getSeitenZaehler(MSearchGetUrl.LISTE_SUMME_BYTE, run.sender));
            String[] ladeart = MSearchGetUrl.getZaehlerLadeArt(run.sender);
            // =================================
            // Zeile1
            zeile = textLaenge(titel1[0].length(), run.sender) + TTRENNER;
            zeile += textLaenge(titel1[1].length(), run.getLaufzeitMinuten()) + TRENNER;
            zeile += textLaenge(titel1[2].length(), String.valueOf(MSearchGetUrl.getSeitenZaehler(MSearchGetUrl.LISTE_SEITEN_ZAEHLER, run.sender))) + TRENNER;
            zeile += textLaenge(titel1[3].length(), String.valueOf(listeFilmeNeu.countSender(run.sender))) + TRENNER;
            zeile += textLaenge(titel1[4].length(), String.valueOf(MSearchGetUrl.getSeitenZaehler(MSearchGetUrl.LISTE_SEITEN_ZAEHLER_FEHlER, run.sender))) + TRENNER;
            zeile += textLaenge(titel1[5].length(), String.valueOf(MSearchGetUrl.getSeitenZaehler(MSearchGetUrl.LISTE_SEITEN_ZAEHLER_FEHLERVERSUCHE, run.sender))) + TRENNER;
            zeile += textLaenge(titel1[6].length(), String.valueOf(MSearchGetUrl.getSeitenZaehler(MSearchGetUrl.LISTE_SEITEN_ZAEHLER_WARTEZEIT_FEHLVERSUCHE, run.sender))) + TRENNER;
            zeile += textLaenge(titel1[7].length(), String.valueOf(MSearchGetUrl.getSeitenZaehler(MSearchGetUrl.LISTE_SEITEN_PROXY, run.sender))) + TRENNER;
            runde1.add(zeile);
            // =================================
            // Zeile3
            zeile = textLaenge(titel3[0].length(), run.sender) + TTRENNER;
            zeile += textLaenge(titel3[1].length(), groesse) + TRENNER;
            zeile += textLaenge(titel3[2].length(), ladeart[0]) + TRENNER;
            zeile += textLaenge(titel3[3].length(), ladeart[1]) + TRENNER;
            zeile += textLaenge(titel3[4].length(), ladeart[2]) + TRENNER;
            zeile += textLaenge(titel3[5].length(), String.valueOf(MSearchUrlDateiGroesse.getZaehler(run.sender))) + TRENNER;
            zeile += textLaenge(titel3[6].length(), String.valueOf(MSearchUrlDateiGroesse.getZaehler403(run.sender))) + TRENNER;
            zeile += textLaenge(titel3[7].length(), String.valueOf(MSearchUrlDateiGroesse.getZaehlerProxy(run.sender))) + TRENNER;
            runde3.add(zeile);
        }
        if (!allStarted || !listeSenderLaufen.listeFertig()) {
            //nur ein Sender fertig oder noch nicht alle gestartet
            notifyProgress(new MSearchListenerFilmeLadenEvent(sender, "", listeSenderLaufen.getMax(), listeSenderLaufen.getProgress()));
        } else {
            // wird einmal aufgerufen, wenn alle Sender fertig sind
            MSearchLog.progressEnde();
            endeMeldung();
            notifyFertig(new MSearchListenerFilmeLadenEvent(sender, "", listeSenderLaufen.getMax(), listeSenderLaufen.getProgress()));
        }
    }

    public ListeFilme getErgebnis() {
        return listeFilmeNeu;
    }

    //===================================
    // private
    //===================================
    private synchronized void mrStarten(int prio) {
        MediathekReader mr;
        Iterator<MediathekReader> it = mediathekListe.iterator();
        // Prio 0 laden
        while (it.hasNext()) {
            mr = it.next();
            if (mr.getStartPrio() == prio) {
                new Thread(mr).start();
            }
        }
    }

    private synchronized void mrWarten() {
        // 3 Minuten warten, alle 10 Sekunden auf STOP prüfen
        try {
            for (int i = 0; i < 18; ++i) {
                if (MSearchConfig.getStop()) {
                    break;
                }
                this.wait(10 * 1000); // 0,5 Min. warten, Sender nach der Gesamtlaufzeit starten
            }
        } catch (Exception ex) {
            MSearchLog.fehlerMeldung(978754213, MSearchLog.FEHLER_ART_PROG, "FilmeSuchenSender.mrWarten", ex);
        }
    }

    private void endeMeldung() {
        // wird einmal aufgerufen, wenn alle Sender fertig sind
        String zeile = "";
        MSearchLog.progressEnde();
        // Sender ===============================================
        // ======================================================
        MSearchLog.systemMeldung("");
        MSearchLog.systemMeldung("");
        MSearchLog.systemMeldung("=================================================================================");
        MSearchLog.systemMeldung("==  Sender  =====================================================================");
        MSearchLog.systemMeldung("");
        // Zeile 1 =============================================
        zeile = titel1[0] + TTRENNER + titel1[1] + TRENNER + titel1[2] + TRENNER + titel1[3] + TRENNER + titel1[4] + TRENNER + titel1[5] + TRENNER + titel1[6] + TRENNER + titel1[7];
        MSearchLog.systemMeldung(zeile);
        MSearchLog.systemMeldung("---------------------------------------------------------------------------------");
        for (String s : runde1) {
            MSearchLog.systemMeldung(s);
        }
        MSearchLog.systemMeldung("");
        MSearchLog.systemMeldung("");
        // Zeile 3 =============================================
        zeile = titel3[0] + TTRENNER + titel3[1] + TRENNER + titel3[2] + TRENNER + titel3[3] + TRENNER + titel3[4] + TRENNER + titel3[5] + TRENNER + titel3[6] + TRENNER + titel3[7];
        MSearchLog.systemMeldung(zeile);
        MSearchLog.systemMeldung("---------------------------------------------------------------------------------");
        for (String s : runde3) {
            MSearchLog.systemMeldung(s);
        }
        // Gesamt ===============================================
        // ======================================================
        int anzFilme = listeFilmeNeu.size();
        if (MSearchConfig.updateFilmliste) {
            // alte Filme eintragen wenn angefordert oder nur ein update gesucht wurde
            listeFilmeNeu.updateListe(listeFilmeAlt, true /* über den Index vergleichen */);
        }
        listeFilmeNeu.sort();
        // FilmlisteMetaDaten
        listeFilmeNeu.metaDatenSchreiben();
//        listeFilmeAlt.anhaengen(listeFilmeNeu);
//        listeFilmeNeu = null;
        stopZeit = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        int sekunden;
        try {
            sekunden = Math.round((stopZeit.getTime() - startZeit.getTime()) / (1000));
        } catch (Exception ex) {
            sekunden = -1;
        }
        MSearchLog.systemMeldung("");
        MSearchLog.systemMeldung("=================================================================================");
        MSearchLog.systemMeldung("=================================================================================");
        MSearchLog.systemMeldung("");
        MSearchLog.systemMeldung("        Filme geladen: " + anzFilme);
        MSearchLog.systemMeldung("       Seiten geladen: " + MSearchGetUrl.getSeitenZaehler(MSearchGetUrl.LISTE_SEITEN_ZAEHLER));
        String groesse = (MSearchGetUrl.getSeitenZaehler(MSearchGetUrl.LISTE_SUMME_BYTE) == 0) ? "<1" : Long.toString(MSearchGetUrl.getSeitenZaehler(MSearchGetUrl.LISTE_SUMME_BYTE));
        MSearchLog.systemMeldung("   Summe geladen[MiB]: " + groesse);
        MSearchLog.systemMeldung("        Traffic [MiB]: " + MSearchGetUrl.getSummeMegaByte());
        if (sekunden <= 0) {
            sekunden = 1;
        }
        // Durchschnittswerte ausgeben
        long kb = (MSearchGetUrl.getSeitenZaehler(MSearchGetUrl.LISTE_SUMME_BYTE) * 1024) / sekunden;
        MSearchLog.systemMeldung("     ->   Rate[KiB/s]: " + (kb == 0 ? "<1" : kb));
        MSearchLog.systemMeldung("     ->    Dauer[Min]: " + (sekunden / 60 == 0 ? "<1" : sekunden / 60));
        MSearchLog.systemMeldung("            ->  Start: " + sdf.format(startZeit));
        MSearchLog.systemMeldung("            ->   Ende: " + sdf.format(stopZeit));
        MSearchLog.systemMeldung("");
        MSearchLog.systemMeldung("=================================================================================");
        MSearchLog.systemMeldung("=================================================================================");
    }

    private void initStart(ListeFilme listeFilme) {
        listeFilmeAlt = listeFilme;
        MSearchConfig.setStop(false);
        startZeit = new Date(System.currentTimeMillis());
        listeFilmeNeu = new ListeFilme();
        listeFilmeNeu.liveStreamEintragen();
        runde1.clear();
        runde2.clear();
        runde3.clear();
        MSearchGetUrl.resetZaehler();
        MSearchUrlDateiGroesse.resetZaehler(getNamenSender());
        MSearchLog.systemMeldung("");
        MSearchLog.systemMeldung("=======================================");
        MSearchLog.systemMeldung("Start Filme laden:");
        if (MSearchConfig.senderAllesLaden) {
            MSearchLog.systemMeldung("Filme laden: alle laden");
        } else {
            MSearchLog.systemMeldung("Filme laden: nur update laden");
        }
        if (MSearchConfig.updateFilmliste) {
            MSearchLog.systemMeldung("Filmliste: aktualisieren");
        } else {
            MSearchLog.systemMeldung("Filmliste: neue erstellen");
        }
        MSearchLog.systemMeldung("=======================================");
        MSearchLog.systemMeldung("");
    }

    private void progressBar() {
        int max = listeSenderLaufen.getMax();
        int progress = listeSenderLaufen.getProgress();
        int proz = 0;
        String text;
        int sekunden = 0;
        try {
            sekunden = Math.round((new Date(System.currentTimeMillis()).getTime() - startZeit.getTime()) / (1000));
        } catch (Exception ex) {
        }
        if (max != 0) {
            if (progress != 0) {
                proz = progress * 100 / max;
            }
            if (max > 0 && proz == 100) {
                proz = 99;
            }
            text = "  [ ";
            int a = proz / 10;
            for (int i = 0; i < a; ++i) {
                text += "#";
            }
            for (int i = 0; i < (10 - a); ++i) {
                text += "-";
            }
            text += " ]  " + MSearchGetUrl.getSeitenZaehler(MSearchGetUrl.LISTE_SEITEN_ZAEHLER) + " Seiten  /  "
                    + proz + "% von " + max + " Themen  /  Filme: " + listeFilmeNeu.size()
                    + "  /  Dauer[Min]: " + (sekunden / 60 == 0 ? "<1" : sekunden / 60);
            MSearchLog.progress(text);
        }
    }

    private String textLaenge(int max, String text) {
        if (text.length() > max) {
            //text = text.substring(0, MAX);
            text = text.substring(0, max - 1);
        }
        while (text.length() < max) {
            text = text + " ";
        }
        return text;
    }

    private void notifyStart(MSearchListenerFilmeLadenEvent event) {
        for (Object l : listeners.getListenerList()) {
            if (l instanceof MSearchListenerFilmeLaden) {
                ((MSearchListenerFilmeLaden) l).start(event);
            }
        }
//        for (MSearchListenerFilmeLaden l : listeners.getListeners(MSearchListenerFilmeLaden.class)) {
//            l.start(event);
//        }
    }

    private void notifyProgress(MSearchListenerFilmeLadenEvent event) {
        for (Object l : listeners.getListenerList()) {
            if (l instanceof MSearchListenerFilmeLaden) {
                ((MSearchListenerFilmeLaden) l).progress(event);
            }
        }

    }

    private void notifyFertig(MSearchListenerFilmeLadenEvent event) {
        for (Object l : listeners.getListenerList()) {
            if (l instanceof MSearchListenerFilmeLaden) {
                ((MSearchListenerFilmeLaden) l).fertig(event);
            }
        }

    }
}
