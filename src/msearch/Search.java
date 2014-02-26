/*
 *  MediathekView
 *  Copyright (C) 2013 W. Xaver
 *  W.Xaver[at]googlemail.com
 *  http://zdfmediathk.sourceforge.net/
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package msearch;

import msearch.daten.ListeFilme;
import msearch.daten.MSearchConfig;
import msearch.filmeSuchen.MSearchFilmeSuchen;
import msearch.filmeSuchen.MSearchListenerFilmeLaden;
import msearch.filmeSuchen.MSearchListenerFilmeLadenEvent;
import msearch.io.MSearchFilmlisteLesen;
import msearch.io.MSearchFilmlisteSchreiben;
import msearch.tool.MSearchFunktionen;
import msearch.tool.MSearchLog;
import static msearch.tool.MSearchLog.versionsMeldungen;

public class Search implements Runnable {

    private ListeFilme listeFilme = new ListeFilme();
    private MSearchFilmeSuchen mSearchFilmeSuchen;
    private boolean serverLaufen = false;

    public Search(String[] ar) {
        mSearchFilmeSuchen = new MSearchFilmeSuchen();
        if (ar != null) {
            for (int i = 0; i < ar.length; ++i) {
                if (ar[i].equals(Main.STARTP_ALLES)) {
                    MSearchConfig.senderAllesLaden = true;
                }
                if (ar[i].equals(Main.STARTP_UPDATE)) {
                    MSearchConfig.updateFilmliste = true;
                }
                if (ar[i].equalsIgnoreCase(Main.STARTP_DIR_FILMS)) {
                    if (ar.length > i) {
                        MSearchConfig.dirFilme = (ar[i + 1]);
                    }
                }
                if (ar[i].equalsIgnoreCase(Main.STARTP_USER_AGENT)) {
                    if (ar.length > i) {
                        MSearchConfig.setUserAgent(ar[i + 1]);
                    }
                }
                if (ar[i].equalsIgnoreCase(Main.STARTP_SENDER)) {
                    if (ar.length > i) {
                        MSearchConfig.nurSenderLaden = new String[]{ar[i + 1]};
                    }
                }
                if (ar[i].equalsIgnoreCase(Main.STARTP_DEBUG)) {
                    MSearchConfig.debug = true;
                }
            }
        }
    }

    public void starten() {
////        if (MSearchConfig.dirFilme.isEmpty()) {
////            MSearchLog.systemMeldung("Keine URI der Filmliste angegeben");
////            System.exit(-1);
////        }
////        // Infos schreiben
////        MSearchLog.startMeldungen(this.getClass().getName());
////        MSearchLog.systemMeldung("");
////        MSearchLog.systemMeldung("");
////        mSearchFilmeSuchen.addAdListener(new MSearchListenerFilmeLaden() {
////            @Override
////            public void fertig(MSearchListenerFilmeLadenEvent event) {
////                undTschuess(true);
////            }
////        });
////        // laden was es schon gibt
////        //public boolean filmlisteLesenJson(String vonDateiUrl, String nachDatei, ListeFilme listeFilme) {
////        new MSearchFilmlisteLesen().filmlisteLesenJson(MSearchConfig.dirFilme, "", listeFilme);
////        // das eigentliche Suchen der Filme bei den Sendern starten
////        if (MSearchConfig.nurSenderLaden == null) {
////            mSearchFilmeSuchen.filmeBeimSenderLaden(listeFilme);
////        } else {
////            mSearchFilmeSuchen.updateSender(MSearchConfig.nurSenderLaden, listeFilme);
////        }
    }

    public void addAdListener(MSearchListenerFilmeLaden listener) {
        mSearchFilmeSuchen.addAdListener(listener);
    }

    public ListeFilme getListeFilme() {
        return listeFilme;
    }

    public static void senderLoeschenUndExit(String senderLoeschen, String dateiFilmliste) {
        // Infos schreiben
        ListeFilme listeFilme = new ListeFilme();
        versionsMeldungen("Search.senderLoeschenUndExit()");
        MSearchLog.systemMeldung("###########################################################");
        MSearchLog.systemMeldung("Programmpfad:   " + MSearchFunktionen.getPathJar());
        MSearchLog.systemMeldung("Sender löschen: " + senderLoeschen);
        MSearchLog.systemMeldung("Filmliste:      " + dateiFilmliste);
        MSearchLog.systemMeldung("###########################################################");
        MSearchLog.systemMeldung("");
        MSearchLog.systemMeldung("");
        new MSearchFilmlisteLesen().filmlisteLesenXml(dateiFilmliste, listeFilme);
        // dann nur einen Sender löschen und dann wieder beenden
        int anz1 = listeFilme.size();
        MSearchLog.systemMeldung("Anzahl Filme vorher: " + anz1);
        listeFilme.delSender(senderLoeschen);
        int anz2 = listeFilme.size();
        MSearchLog.systemMeldung("Anzehl Filme nachher: " + anz2);
        MSearchLog.systemMeldung(" --> gelöscht: " + (anz1 - anz2));
        new MSearchFilmlisteSchreiben().filmlisteSchreibenXml(dateiFilmliste, listeFilme);
        System.exit(0);
    }

    @Override
    public synchronized void run() {
        // für den MServer
        serverLaufen = true;
        if (MSearchConfig.dirFilme.isEmpty()) {
            MSearchLog.systemMeldung("Kein Pfad der Filmlisten angegeben");
            System.exit(-1);
        }
        // Infos schreiben
        MSearchLog.startMeldungen(this.getClass().getName());
        MSearchLog.systemMeldung("");
        MSearchLog.systemMeldung("");
        mSearchFilmeSuchen.addAdListener(new MSearchListenerFilmeLaden() {
            @Override
            public void fertig(MSearchListenerFilmeLadenEvent event) {
                serverLaufen = false;
            }
        });
        // laden was es schon gibt
        //Daten.ioXmlFilmlisteLesen.filmlisteLesen(Daten.getBasisVerzeichnis() + Konstanten.XML_DATEI_FILME, false /* istUrl */, Daten.listeFilme);
        new MSearchFilmlisteLesen().filmlisteLesenJson(MSearchConfig.getPathFilmlist(false), "", listeFilme);
        // das eigentliche Suchen der Filme bei den Sendern starten
        if (MSearchConfig.nurSenderLaden == null) {
            mSearchFilmeSuchen.filmeBeimSenderLaden(listeFilme);
        } else {
            mSearchFilmeSuchen.updateSender(MSearchConfig.nurSenderLaden, listeFilme);
        }
        try {
            while (serverLaufen) {
                this.wait(5000);
            }
        } catch (Exception ex) {
            MSearchLog.fehlerMeldung(496378742, MSearchLog.FEHLER_ART_FILME_SUCHEN, Search.class.getName(), "run()");
        }
        undTschuess(false /* exit */);
    }

    private void undTschuess(boolean exit) {
        listeFilme = mSearchFilmeSuchen.getErgebnis();
        ListeFilme tmpListe = new ListeFilme();
        if (!MSearchConfig.importUrl__anhaengen.isEmpty()) {
            // wenn eine ImportUrl angegeben, dann die Filme die noch nicht drin sind anfügen
            MSearchLog.systemMeldung("Filmliste importieren (anhängen) von: " + MSearchConfig.importUrl__anhaengen);
            tmpListe.clear();
            new MSearchFilmlisteLesen().filmlisteLesenJson(MSearchConfig.importUrl__anhaengen, "", tmpListe);
            listeFilme.updateListe(tmpListe, false /* nur URL vergleichen */);
            tmpListe.clear();
            System.gc();
            listeFilme.sort();
        }
        if (!MSearchConfig.importUrl__ersetzen.isEmpty()) {
            // wenn eine ImportUrl angegeben, dann noch eine Liste importieren, Filme die es schon gibt
            // werden ersetzt
            MSearchLog.systemMeldung("Filmliste importieren (ersetzen) von: " + MSearchConfig.importUrl__ersetzen);
            tmpListe.clear();
            new MSearchFilmlisteLesen().filmlisteLesenJson(MSearchConfig.importUrl__ersetzen, "", tmpListe);
            tmpListe.updateListe(listeFilme, false /* nur URL vergleichen */);
            tmpListe.metaDaten = listeFilme.metaDaten;
            listeFilme.clear();
            System.gc();
            tmpListe.sort(); // jetzt sollte alles passen
            listeFilme = tmpListe;
        }
        //================================================
        // Filmliste schreiben, normal, xz und bz2 komprimiert
        new MSearchFilmlisteSchreiben().filmlisteSchreibenJson(MSearchConfig.getPathFilmlist(false), listeFilme);
        new MSearchFilmlisteSchreiben().filmlisteSchreibenJson(MSearchConfig.getPathFilmlist(true), listeFilme);
        new MSearchFilmlisteSchreiben().filmlisteSchreibenJson(MSearchConfig.getPathFilmlist_json_xz(), listeFilme);
        new MSearchFilmlisteSchreiben().filmlisteSchreibenXml(MSearchConfig.getPathFilmlist_xml_bz2(), listeFilme);
        //================================================
        // Org-Diff
        if (MSearchConfig.orgFilmlisteErstellen) {
            // org-Liste anlegen, typ. erste Liste am Tag
            MSearchLog.systemMeldung("Org-Lilste erzeugen: " + MSearchConfig.getPathFilmlist_org());
            new MSearchFilmlisteSchreiben().filmlisteSchreibenJson(MSearchConfig.getPathFilmlist_org(), listeFilme);
            new MSearchFilmlisteSchreiben().filmlisteSchreibenJson(MSearchConfig.getPathFilmlist_org_xz(), listeFilme);
        }
        if (MSearchConfig.diffFilmlisteErstellen) {
            // noch das diff erzeugen
            MSearchLog.systemMeldung("Diff erzeugen, von: " + MSearchConfig.getPathFilmlist_org() + " nach: " + MSearchConfig.getPathFilmlist_diff());
            tmpListe.clear();
            ListeFilme diff;
            if (!new MSearchFilmlisteLesen().filmlisteLesenJson(MSearchConfig.getPathFilmlist_org(), "", tmpListe) || tmpListe.isEmpty()) {
                // dann ist die komplette Liste das diff
                diff = listeFilme;
            } else if (tmpListe.filmlisteIstAelter(24 * 60 * 60)) {
                // älter als ein Tag, dann stimmt was nicht!
                diff = listeFilme;
            } else {
                // nur dann macht die Arbeit sinn
                diff = listeFilme.neueFilme(tmpListe);
            }
            new MSearchFilmlisteSchreiben().filmlisteSchreibenJson(MSearchConfig.getPathFilmlist_diff(), diff);
            new MSearchFilmlisteSchreiben().filmlisteSchreibenJson(MSearchConfig.getPathFilmlist_diff_xz(), diff);
        }
        //================================================
        // fertig
        MSearchLog.printEndeMeldung();
        // nur dann das Programm beenden
        if (exit) {
            System.exit(listeFilme.isEmpty() ? 1 : 0);
        }
    }
}
