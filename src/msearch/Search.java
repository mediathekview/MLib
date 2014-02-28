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
import msearch.daten.MSConfig;
import msearch.filmeSuchen.MSFilmeSuchen;
import msearch.filmeSuchen.MSListenerFilmeLaden;
import msearch.filmeSuchen.MSListenerFilmeLadenEvent;
import msearch.io.MSFilmlisteLesen;
import msearch.io.MSFilmlisteSchreiben;
import msearch.tool.MSFunktionen;
import msearch.tool.MSLog;
import static msearch.tool.MSLog.versionsMeldungen;

public class Search implements Runnable {

    private ListeFilme listeFilme = new ListeFilme();
    private MSFilmeSuchen mSearchFilmeSuchen;
    private boolean serverLaufen = false;

    public Search(String[] ar) {
        mSearchFilmeSuchen = new MSFilmeSuchen();
        if (ar != null) {
            for (int i = 0; i < ar.length; ++i) {
                if (ar[i].equals(Main.STARTP_ALLES)) {
                    MSConfig.senderAllesLaden = true;
                }
                if (ar[i].equals(Main.STARTP_UPDATE)) {
                    MSConfig.updateFilmliste = true;
                }
                if (ar[i].equalsIgnoreCase(Main.STARTP_DIR_FILMS)) {
                    if (ar.length > i) {
                        MSConfig.dirFilme = (ar[i + 1]);
                    }
                }
                if (ar[i].equalsIgnoreCase(Main.STARTP_USER_AGENT)) {
                    if (ar.length > i) {
                        MSConfig.setUserAgent(ar[i + 1]);
                    }
                }
                if (ar[i].equalsIgnoreCase(Main.STARTP_SENDER)) {
                    if (ar.length > i) {
                        MSConfig.nurSenderLaden = new String[]{ar[i + 1]};
                    }
                }
                if (ar[i].equalsIgnoreCase(Main.STARTP_DEBUG)) {
                    MSConfig.debug = true;
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

    public void addAdListener(MSListenerFilmeLaden listener) {
        mSearchFilmeSuchen.addAdListener(listener);
    }

    public ListeFilme getListeFilme() {
        return listeFilme;
    }

    public static void senderLoeschenUndExit(String senderLoeschen, String dateiFilmliste) {
        // Infos schreiben
////        ListeFilme listeFilme = new ListeFilme();
////        versionsMeldungen("Search.senderLoeschenUndExit()");
////        MSLog.systemMeldung("###########################################################");
////        MSLog.systemMeldung("Programmpfad:   " + MSFunktionen.getPathJar());
////        MSLog.systemMeldung("Sender löschen: " + senderLoeschen);
////        MSLog.systemMeldung("Filmliste:      " + dateiFilmliste);
////        MSLog.systemMeldung("###########################################################");
////        MSLog.systemMeldung("");
////        MSLog.systemMeldung("");
////        new MSFilmlisteLesen().filmlisteLesenXml(dateiFilmliste, listeFilme);
////        // dann nur einen Sender löschen und dann wieder beenden
////        int anz1 = listeFilme.size();
////        MSLog.systemMeldung("Anzahl Filme vorher: " + anz1);
////        listeFilme.delSender(senderLoeschen);
////        int anz2 = listeFilme.size();
////        MSLog.systemMeldung("Anzehl Filme nachher: " + anz2);
////        MSLog.systemMeldung(" --> gelöscht: " + (anz1 - anz2));
////        new MSFilmlisteSchreiben().filmlisteSchreibenXml(dateiFilmliste, listeFilme);
        System.exit(0);
    }

    @Override
    public synchronized void run() {
        // für den MServer
        serverLaufen = true;
        if (MSConfig.dirFilme.isEmpty()) {
            MSLog.systemMeldung("Kein Pfad der Filmlisten angegeben");
            System.exit(-1);
        }
        // Infos schreiben
        MSLog.startMeldungen(this.getClass().getName());
        MSLog.systemMeldung("");
        MSLog.systemMeldung("");
        mSearchFilmeSuchen.addAdListener(new MSListenerFilmeLaden() {
            @Override
            public void fertig(MSListenerFilmeLadenEvent event) {
                serverLaufen = false;
            }
        });
        // laden was es schon gibt
        new MSFilmlisteLesen().filmlisteLesenJson(MSConfig.getPathFilmlist(false /*aktDate*/), "", listeFilme);
        // das eigentliche Suchen der Filme bei den Sendern starten
        if (MSConfig.nurSenderLaden == null) {
            mSearchFilmeSuchen.filmeBeimSenderLaden(listeFilme);
        } else {
            mSearchFilmeSuchen.updateSender(MSConfig.nurSenderLaden, listeFilme);
        }
        try {
            while (serverLaufen) {
                this.wait(5000);
            }
        } catch (Exception ex) {
            MSLog.fehlerMeldung(496378742, MSLog.FEHLER_ART_FILME_SUCHEN, Search.class.getName(), "run()");
        }
        undTschuess(false /* exit */);
    }

    private void undTschuess(boolean exit) {
        listeFilme = mSearchFilmeSuchen.getErgebnis();
        ListeFilme tmpListe = new ListeFilme();

        //================================================
        // noch anere Listen importieren
        MSLog.systemMeldung("");
        if (!MSConfig.importUrl__anhaengen.isEmpty()) {
            // wenn eine ImportUrl angegeben, dann die Filme die noch nicht drin sind anfügen
            MSLog.systemMeldung("Filmliste importieren (anhängen) von: " + MSConfig.importUrl__anhaengen);
            tmpListe.clear();
            new MSFilmlisteLesen().filmlisteLesenJson(MSConfig.importUrl__anhaengen, "", tmpListe);
            listeFilme.updateListe(tmpListe, false /* nur URL vergleichen */);
            tmpListe.clear();
            System.gc();
            listeFilme.sort();
        }
        if (!MSConfig.importUrl__ersetzen.isEmpty()) {
            // wenn eine ImportUrl angegeben, dann noch eine Liste importieren, Filme die es schon gibt
            // werden ersetzt
            MSLog.systemMeldung("Filmliste importieren (ersetzen) von: " + MSConfig.importUrl__ersetzen);
            tmpListe.clear();
            new MSFilmlisteLesen().filmlisteLesenJson(MSConfig.importUrl__ersetzen, "", tmpListe);
            tmpListe.updateListe(listeFilme, false /* nur URL vergleichen */);
            tmpListe.metaDaten = listeFilme.metaDaten;
            listeFilme.clear();
            System.gc();
            tmpListe.sort(); // jetzt sollte alles passen
            listeFilme = tmpListe;
        }

        //================================================
        // Filmliste schreiben, normal, xz und bz2 komprimiert
        MSLog.systemMeldung("");
        new MSFilmlisteSchreiben().filmlisteSchreibenJson(MSConfig.getPathFilmlist(false /*aktDate*/), listeFilme);
        new MSFilmlisteSchreiben().filmlisteSchreibenJson(MSConfig.getPathFilmlist(true /*aktDate*/), listeFilme);
        new MSFilmlisteSchreiben().filmlisteSchreibenJson(MSConfig.getPathFilmlist_json_xz(), listeFilme);
        new MSFilmlisteSchreiben().filmlisteSchreibenXml(MSConfig.getPathFilmlist_xml_bz2(), listeFilme);

        //================================================
        // Org-Diff
        MSLog.systemMeldung("");
        if (MSConfig.orgFilmlisteErstellen) {
            // org-Liste anlegen, typ. erste Liste am Tag
            MSLog.systemMeldung("Org-Lilste");
            MSLog.systemMeldung("  --> ersellen: " + MSConfig.getPathFilmlist_org());
            new MSFilmlisteSchreiben().filmlisteSchreibenJson(MSConfig.getPathFilmlist_org(), listeFilme);
            new MSFilmlisteSchreiben().filmlisteSchreibenJson(MSConfig.getPathFilmlist_org_xz(), listeFilme);
        }
        if (MSConfig.diffFilmlisteErstellen) {
            // noch das diff erzeugen
            MSLog.systemMeldung("Diff erzeugen, von: " + MSConfig.getPathFilmlist_org() + " nach: " + MSConfig.getPathFilmlist_diff());
            tmpListe.clear();
            ListeFilme diff;
            if (!new MSFilmlisteLesen().filmlisteLesenJson(MSConfig.getPathFilmlist_org(), "", tmpListe) || tmpListe.isEmpty()) {
                // dann ist die komplette Liste das diff
                diff = listeFilme;
            } else if (tmpListe.filmlisteIstAelter(24 * 60 * 60)) {
                // älter als ein Tag, dann stimmt was nicht!
                diff = listeFilme;
            } else {
                // nur dann macht die Arbeit sinn
                diff = listeFilme.neueFilme(tmpListe);
            }
            new MSFilmlisteSchreiben().filmlisteSchreibenJson(MSConfig.getPathFilmlist_diff(), diff);
            new MSFilmlisteSchreiben().filmlisteSchreibenJson(MSConfig.getPathFilmlist_diff_xz(), diff);
        }

        //================================================
        // fertig
        MSLog.printEndeMeldung();
        // nur dann das Programm beenden
        if (exit) {
            System.exit(listeFilme.isEmpty() ? 1 : 0);
        }
    }
}
