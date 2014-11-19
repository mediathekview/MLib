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
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import javax.swing.event.EventListenerList;
import msearch.daten.ListeFilme;
import msearch.tool.MSConfig;
import msearch.filmeSuchen.sender.Mediathek3Sat;
import msearch.filmeSuchen.sender.MediathekArd;
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
import msearch.filmeSuchen.sender.MediathekZdfTivi;
import msearch.tool.DatumZeit;
import msearch.tool.GermanStringSorter;
import msearch.tool.MSLog;
import msearch.tool.MSFileSize;

public class MSFilmeSuchen {

    public ListeFilme listeFilmeNeu; // neu angelegte Liste und da kommen die neu gesuchten Filme rein
    public ListeFilme listeFilmeAlt; // ist die "alte" Liste, wird beim Aufruf übergeben und enthält am Ende das Ergebnis
    // private
    private final LinkedList<MediathekReader> mediathekListe = new LinkedList<>();
    private final EventListenerList listeners = new EventListenerList();
    private final MSListeRunSender listeSenderLaufen = new MSListeRunSender();
    private Date startZeit = new Date();
    private Date stopZeit = new Date();
    private final ArrayList<String> runde1 = new ArrayList<>();
    private final ArrayList<String> runde2 = new ArrayList<>();
    private final ArrayList<String> runde3 = new ArrayList<>();
    private final String[] titel1 = {"Sender laden ", "[min]", "Seiten", "Filme", "Fehler", "FVersuche", "FZeit[s]", "AnzÜberProxy"};
    private final String[] titel2 = {"Sender laden ", "Geladen[MB]", "Nix", "Deflaet", "Gzip", "noBuff[s]", "D-Rate[kByte/s]"};
    private final String[] titel3 = {"Dateigroesse ", "getGroesse:", "mit_403", "OkMitProxy", "Threads", "Wait"};
    private final static String TRENNER = " | ";
    private boolean allStarted = false;
    private int anzFilme = 0; // Anzahl gesuchter Filme
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    /**
     * ###########################################################################################################
     * Ablauf:
     * die gefundenen Filme kommen in die "listeFilme"
     * -> bei einem vollen Suchlauf: passiert nichts weiter
     * -> bei einem Update: "listeFilme" mit alter Filmliste auffüllen, URLs die es schon gibt werden verworfen
     * "listeFilme" ist dann die neue komplette Liste mit Filmen
     * ##########################################################################################################
     */
    public MSFilmeSuchen() {
        //Reader laden Spaltenweises Laden
        mediathekListe.add(new MediathekArd(this, 0));
        mediathekListe.add(new MediathekZdf(this, 0));
        mediathekListe.add(new MediathekZdfTivi(this, 0));
        mediathekListe.add(new MediathekArte_de(this, 0));
        mediathekListe.add(new MediathekArte_fr(this, 0));
        mediathekListe.add(new Mediathek3Sat(this, 1));
        mediathekListe.add(new MediathekSwr(this, 0));
        mediathekListe.add(new MediathekNdr(this, 1));
        mediathekListe.add(new MediathekKika(this, 0));
        // Spalte 2
        mediathekListe.add(new MediathekMdr(this, 0));
        mediathekListe.add(new MediathekWdr(this, 1));
        mediathekListe.add(new MediathekHr(this, 0));
        mediathekListe.add(new MediathekRbb(this, 1));
        mediathekListe.add(new MediathekBr(this, 0));
        mediathekListe.add(new MediathekSrf(this, 1));
        mediathekListe.add(new MediathekSrfPod(this, 0));
        mediathekListe.add(new MediathekOrf(this, 1));
    }

    public static String[] getNamenSender() {
        // liefert eine Array mit allen Sendernamen
        LinkedList<String> liste = new LinkedList<>();
        liste.add(MediathekArd.SENDERNAME);
        liste.add(MediathekZdf.SENDERNAME);
        liste.add(MediathekZdfTivi.SENDERNAME);
        liste.add(MediathekArte_de.SENDERNAME);
        liste.add(MediathekArte_fr.SENDERNAME);
        liste.add(Mediathek3Sat.SENDERNAME);
        liste.add(MediathekSwr.SENDERNAME);
        liste.add(MediathekNdr.SENDERNAME);
        liste.add(MediathekKika.SENDERNAME);
        // Spalte 2
        liste.add(MediathekMdr.SENDERNAME);
        liste.add(MediathekWdr.SENDERNAME);
        liste.add(MediathekHr.SENDERNAME);
        liste.add(MediathekRbb.SENDERNAME);
        liste.add(MediathekBr.SENDERNAME);
        liste.add(MediathekSrf.SENDERNAME);
        liste.add(MediathekSrfPod.SENDERNAME);
        liste.add(MediathekOrf.SENDERNAME);

        GermanStringSorter sorter = GermanStringSorter.getInstance();
        Collections.sort(liste, sorter);
        return liste.toArray(new String[liste.size()]);
    }

    public void addAdListener(MSListenerFilmeLaden listener) {
        listeners.add(MSListenerFilmeLaden.class, listener);
    }

    /**
     * es werden alle Filme gesucht
     *
     * @param listeFilme
     */
    public synchronized void filmeBeimSenderLaden(ListeFilme listeFilme) {
        initStart(listeFilme);
        // die mReader nach Prio starten
        mrStarten(0);
        if (!MSConfig.getStop()) {
            mrWarten();
            mrStarten(1);
            allStarted = true;
        }
    }

    /**
     * es wird nur einige Sender aktualisiert
     *
     * @param nameSender
     * @param listeFilme
     */
    public void updateSender(String[] nameSender, ListeFilme listeFilme) {
        // nur für den Mauskontext "Sender aktualisieren"
        boolean starten = false;
        initStart(listeFilme);
        for (MediathekReader reader : mediathekListe) {
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

    private String getThreads(String sender) {
        // liefert die Anzahl Threads des Senders
        try {
            for (MediathekReader aMediathekListe : mediathekListe) {
                if (aMediathekListe.getNameSender().equals(sender)) {
                    return String.valueOf(aMediathekListe.getThreads());
                }
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    private String getWaitTime(String sender) {
        // liefert die Wartezeit beim Seitenladen des Senders
        try {
            for (MediathekReader aMediathekListe : mediathekListe) {
                if (aMediathekListe.getNameSender().equals(sender)) {
                    return String.valueOf(aMediathekListe.getWaitTime());
                }
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    public synchronized void melden(String sender, int max, int progress, String text) {
        MSRunSender runSender = listeSenderLaufen.getSender(sender);
        if (runSender != null) {
            runSender.max = max;
            runSender.progress = progress;
        } else {
            // Sender startet
            listeSenderLaufen.add(new MSRunSender(sender, max, progress));
            //wird beim Start des Senders aufgerufen, 1x
            if (listeSenderLaufen.size() <= 1 /* erster Aufruf */) {
                notifyStart(new MSListenerFilmeLadenEvent(sender, text, listeSenderLaufen.getMax(), listeSenderLaufen.getProgress(), listeFilmeNeu.size(), false));
            }
        }
        notifyProgress(new MSListenerFilmeLadenEvent(sender, text, listeSenderLaufen.getMax(), listeSenderLaufen.getProgress(), listeFilmeNeu.size(), false));
        progressBar();
    }

    public synchronized void meldenFertig(String sender) {
        //wird ausgeführt wenn Sender beendet ist
        String zeile;
        MSRunSender run = listeSenderLaufen.senderFertig(sender);
        if (allStarted && listeSenderLaufen.listeFertig()) {
            MSLog.progress(""); // zum löschen der Progressbar
        }
        zeile = "" + "\n";
        zeile += "-------------------------------------------------------------------------------------" + "\n";
        zeile += "Fertig " + sender + ": " + DatumZeit.getJetzt_HH_MM_SS() + " Uhr, Filme: " + listeFilmeNeu.countSender(sender) + "\n";
        int sekunden = getDauerSekunden();
        zeile += "     -> Dauer[Min]: " + (sekunden / 60 == 0 ? "<1" : sekunden / 60) + "\n";
        zeile += "     ->       Rest: " + listeSenderLaufen.getSenderRun() + "\n";
        zeile += "-------------------------------------------------------------------------------------" + "\n";
        MSLog.systemMeldung(zeile);

        if (run != null) {
            int dauerS = run.getLaufzeitSekunden();
            long groesseKB = MSGetUrl.getSeitenZaehler(MSGetUrl.LISTE_SUMME_KBYTE, run.sender);

            String rate = "<1";
            if (groesseKB > 0 && dauerS > 0) {
                rate = String.valueOf(groesseKB / dauerS);
            }
            String groesse = (groesseKB / 1000 == 0) ? "<1" : Long.toString(groesseKB / 1000);
            //String groesse = (MSGetUrl.getSeitenZaehler(MSGetUrl.LISTE_SUMME_KBYTE, run.sender) == 0) ? "<1" : Long.toString(MSGetUrl.getSeitenZaehler(MSGetUrl.LISTE_SUMME_KBYTE, run.sender));
            String[] ladeart = MSGetUrl.getZaehlerLadeArt(run.sender);
            // =================================
            // Zeile1
            zeile = textLaenge(titel1[0].length(), run.sender) + TRENNER;
            zeile += textLaenge(titel1[1].length(), run.getLaufzeitMinuten()) + TRENNER;
            zeile += textLaenge(titel1[2].length(), String.valueOf(MSGetUrl.getSeitenZaehler(MSGetUrl.LISTE_SEITEN_ZAEHLER, run.sender))) + TRENNER;
            zeile += textLaenge(titel1[3].length(), String.valueOf(listeFilmeNeu.countSender(run.sender))) + TRENNER;
            zeile += textLaenge(titel1[4].length(), String.valueOf(MSGetUrl.getSeitenZaehler(MSGetUrl.LISTE_SEITEN_ZAEHLER_FEHlER, run.sender))) + TRENNER;
            zeile += textLaenge(titel1[5].length(), String.valueOf(MSGetUrl.getSeitenZaehler(MSGetUrl.LISTE_SEITEN_ZAEHLER_FEHLERVERSUCHE, run.sender))) + TRENNER;
            zeile += textLaenge(titel1[6].length(), String.valueOf(MSGetUrl.getSeitenZaehler(MSGetUrl.LISTE_SEITEN_ZAEHLER_WARTEZEIT_FEHLVERSUCHE, run.sender))) + TRENNER;
            zeile += textLaenge(titel1[7].length(), String.valueOf(MSGetUrl.getSeitenZaehler(MSGetUrl.LISTE_SEITEN_PROXY, run.sender))) + TRENNER;
            runde1.add(zeile);
            // =================================
            // Zeile2
            zeile = textLaenge(titel2[0].length(), run.sender) + TRENNER;
            zeile += textLaenge(titel2[1].length(), groesse) + TRENNER;
            zeile += textLaenge(titel2[2].length(), ladeart[0]) + TRENNER;
            zeile += textLaenge(titel2[3].length(), ladeart[1]) + TRENNER;
            zeile += textLaenge(titel2[4].length(), ladeart[2]) + TRENNER;
            zeile += textLaenge(titel2[5].length(), String.valueOf(MSGetUrl.getSeitenZaehler(MSGetUrl.LISTE_SEITEN_NO_BUFFER, run.sender))) + TRENNER;
            zeile += textLaenge(titel2[6].length(), rate) + TRENNER;
            runde2.add(zeile);
            // =================================
            // Zeile3
            zeile = textLaenge(titel3[0].length(), run.sender) + TRENNER;
            zeile += textLaenge(titel3[1].length(), String.valueOf(MSFileSize.getZaehler(run.sender))) + TRENNER;
            zeile += textLaenge(titel3[2].length(), String.valueOf(MSFileSize.getZaehler403(run.sender))) + TRENNER;
            zeile += textLaenge(titel3[3].length(), String.valueOf(MSFileSize.getZaehlerProxy(run.sender))) + TRENNER;
            zeile += textLaenge(titel3[4].length(), getThreads(run.sender)) + TRENNER;
            zeile += textLaenge(titel3[5].length(), getWaitTime(run.sender)) + TRENNER;
            runde3.add(zeile
            );
        }
        if (!allStarted || !listeSenderLaufen.listeFertig()) {
            //nur ein Sender fertig oder noch nicht alle gestartet
            notifyProgress(new MSListenerFilmeLadenEvent(sender, "", listeSenderLaufen.getMax(), listeSenderLaufen.getProgress(), listeFilmeNeu.size(), false));
        } else {
            // wird einmal aufgerufen, wenn alle Sender fertig sind
            anzFilme = listeFilmeNeu.size();
            if (MSConfig.updateFilmliste) {
                // alte Filme eintragen wenn angefordert oder nur ein update gesucht wurde
                //////toDo
                listeFilmeNeu.updateListe(listeFilmeAlt, true /* über den Index vergleichen */, false /*ersetzen*/);
            }
            listeFilmeNeu.sort();
            // FilmlisteMetaDaten
            listeFilmeNeu.writeMetaData();
            stopZeit = new Date(System.currentTimeMillis());
            ArrayList<String> ret = endeMeldung();
            for (String s : ret) {
                MSLog.systemMeldung(s);
            }
            notifyFertig(new MSListenerFilmeLadenEvent(sender, "", listeSenderLaufen.getMax(), listeSenderLaufen.getProgress(), listeFilmeNeu.size(), false));
        }
    }

    public ListeFilme getErgebnis() {
        return listeFilmeNeu;
    }

    //===================================
    // private
    //===================================
    private synchronized void mrStarten(int prio) {
        // Prio 0 laden
        for (MediathekReader mr : mediathekListe) {
            if (mr.getStartPrio() == prio) {
                new Thread(mr).start();
            }
        }
    }

    private synchronized void mrWarten() {
        // 4 Minuten warten, alle 10 Sekunden auf STOP prüfen
        try {
            for (int i = 0; i < 4 * 60; ++i) {
                if (MSConfig.getStop()) {
                    break;
                }
                this.wait(1000); // warten, Sender nach der Gesamtlaufzeit starten
            }
        } catch (Exception ex) {
            MSLog.fehlerMeldung(978754213, MSLog.FEHLER_ART_PROG, "FilmeSuchenSender.mrWarten", ex);
        }
    }

    public ArrayList<String> endeMeldung() {
        // wird einmal aufgerufen, wenn alle Sender fertig sind
        ArrayList<String> retArray = new ArrayList<>();
        String zeile;
        if (MSConfig.getStop()) {
            // Abbruch melden
            retArray.add("                                                                                     ");
            retArray.add("                                                                                     ");
            retArray.add("*************************************************************************************");
            retArray.add("*************************************************************************************");
            retArray.add("     ----- Abbruch -----                                                             ");
            retArray.add("*************************************************************************************");
            retArray.add("*************************************************************************************");
            retArray.add("                                                                                     ");
            retArray.add("                                                                                     ");
        }
        // Sender ===============================================
        // ======================================================
        retArray.add("");
        retArray.add("");
        retArray.add("=================================================================================");
        retArray.add("==  Sender  =====================================================================");
        retArray.add("");
        // Zeile 1 =============================================
        zeile = "";
        for (String s : titel1) {
            zeile += s + TRENNER;
        }
        retArray.add(zeile);
        retArray.add("---------------------------------------------------------------------------------------");
        for (String s : runde1) {
            retArray.add(s);
        }
        retArray.add("");
        retArray.add("");
        // Zeile 2 =============================================
        zeile = "";
        for (String s : titel2) {
            zeile += s + TRENNER;
        }
        retArray.add(zeile);
        retArray.add("----------------------------------------------------------------------------------");
        for (String s : runde2) {
            retArray.add(s);
        }
        retArray.add("");
        retArray.add("");
        // Zeile 3 =============================================
        zeile = "";
        for (String s : titel3) {
            zeile += s + TRENNER;
        }
        retArray.add(zeile);
        retArray.add("---------------------------------------------------------------------");
        for (String s : runde3) {
            retArray.add(s);
        }
        // Gesamt ===============================================
        // ======================================================
        int sekunden = getDauerSekunden();
        retArray.add("");
        retArray.add("=================================================================================");
        retArray.add("=================================================================================");
        retArray.add("");
        retArray.add("        Filme geladen: " + anzFilme);
        retArray.add("       Seiten geladen: " + MSGetUrl.getSeitenZaehler(MSGetUrl.LISTE_SEITEN_ZAEHLER));
        String groesse = (MSGetUrl.getSeitenZaehler(MSGetUrl.LISTE_SUMME_KBYTE) / 1000 == 0) ? "<1" : Long.toString(MSGetUrl.getSeitenZaehler(MSGetUrl.LISTE_SUMME_KBYTE) / 1000);
        retArray.add("   Summe geladen[MiB]: " + groesse);
        retArray.add("        Traffic [MiB]: " + MSGetUrl.getSummeMegaByte());
        // Durchschnittswerte ausgeben
        long kb = (MSGetUrl.getSeitenZaehler(MSGetUrl.LISTE_SUMME_KBYTE)) / (sekunden == 0 ? 1 : sekunden);
        retArray.add("     ->   Rate[KiB/s]: " + (kb == 0 ? "<1" : kb));
        retArray.add("     ->    Dauer[Min]: " + (sekunden / 60 == 0 ? "<1" : sekunden / 60));
        retArray.add("            ->  Start: " + sdf.format(startZeit));
        retArray.add("            ->   Ende: " + sdf.format(stopZeit));
        retArray.add("");
        retArray.add("=================================================================================");
        retArray.add("=================================================================================");
        return retArray;
    }

    private int getDauerSekunden() {
        int sekunden;
        try {
            sekunden = Math.round((stopZeit.getTime() - startZeit.getTime()) / (1000));
        } catch (Exception ex) {
            sekunden = 1;
        }
        if (sekunden <= 0) {
            sekunden = 1;
        }
        return sekunden;
    }

    private void initStart(ListeFilme listeFilme) {
        allStarted = false;
        anzFilme = 0;
        listeFilmeAlt = listeFilme;
        MSConfig.setStop(false);
        startZeit = new Date(System.currentTimeMillis());
        listeFilmeNeu = new ListeFilme();
        listeFilmeNeu.liveStreamEintragen();
        runde1.clear();
        runde2.clear();
        runde3.clear();
        MSGetUrl.resetZaehler();
        MSFileSize.resetZaehler(getNamenSender());
        MSLog.systemMeldung("");
        MSLog.systemMeldung("=======================================");
        MSLog.systemMeldung("Start Filme laden:");
        if (MSConfig.senderAllesLaden) {
            MSLog.systemMeldung("Filme laden: alle laden");
        } else {
            MSLog.systemMeldung("Filme laden: nur update laden");
        }
        if (MSConfig.updateFilmliste) {
            MSLog.systemMeldung("Filmliste: aktualisieren");
        } else {
            MSLog.systemMeldung("Filmliste: neue erstellen");
        }
        MSLog.systemMeldung("=======================================");
        MSLog.systemMeldung("");
    }

    private void progressBar() {
        int max = listeSenderLaufen.getMax();
        int progress = listeSenderLaufen.getProgress();
        int proz = 0;
        String text;
        int sekunden = 0;
        try {
            sekunden = Math.round((new Date(System.currentTimeMillis()).getTime() - startZeit.getTime()) / (1000));
        } catch (Exception ignored) {
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
            text += " ]  " + MSGetUrl.getSeitenZaehler(MSGetUrl.LISTE_SEITEN_ZAEHLER) + " Seiten / "
                    + proz + "% von " + max + " Themen / Filme: " + listeFilmeNeu.size()
                    + " / Dauer[Min]: " + (sekunden / 60 == 0 ? "<1" : sekunden / 60)
                    + " / R-Sender: " + listeSenderLaufen.getAnzSenderRun();
            MSLog.progress(text);
        }
    }

    private String textLaenge(int max, String text) {
        if (text.length() > max) {
            text = text.substring(0, max - 1);
        }
        while (text.length() < max) {
            text = text + " ";
        }
        return text;
    }

    private void notifyStart(MSListenerFilmeLadenEvent event) {
        for (Object l : listeners.getListenerList()) {
            if (l instanceof MSListenerFilmeLaden) {
                ((MSListenerFilmeLaden) l).start(event);
            }
        }
    }

    private void notifyProgress(MSListenerFilmeLadenEvent event) {
        for (Object l : listeners.getListenerList()) {
            if (l instanceof MSListenerFilmeLaden) {
                ((MSListenerFilmeLaden) l).progress(event);
            }
        }
    }

    private void notifyFertig(MSListenerFilmeLadenEvent event) {
        for (Object l : listeners.getListenerList()) {
            if (l instanceof MSListenerFilmeLaden) {
                ((MSListenerFilmeLaden) l).fertig(event);
            }
        }
    }
}
