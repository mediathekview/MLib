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

import msearch.filmeSuchen.MSearchFilmeSuchen;
import msearch.filmeSuchen.MSearchListenerFilmeLaden;
import msearch.filmeSuchen.MSearchListenerFilmeLadenEvent;
import msearch.io.MSearchIoXmlFilmlisteLesen;
import msearch.io.MSearchIoXmlFilmlisteSchreiben;
import msearch.daten.MSearchConfig;
import msearch.daten.ListeFilme;
import msearch.tool.Funktionen;
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
                if (ar[i].equalsIgnoreCase(Main.STARTP_DATEI_FILMLISTE)) {
                    if (ar.length > i) {
                        MSearchConfig.dateiFilmliste = (ar[i + 1]);
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
        if (MSearchConfig.dateiFilmliste.isEmpty()) {
            MSearchLog.systemMeldung("Keine URI der Filmliste angegeben");
            System.exit(-1);
        }
        // Infos schreiben
        MSearchLog.startMeldungen(this.getClass().getName());
        MSearchLog.systemMeldung("");
        MSearchLog.systemMeldung("");
        mSearchFilmeSuchen.addAdListener(new MSearchListenerFilmeLaden() {
            @Override
            public void fertig(MSearchListenerFilmeLadenEvent event) {
                undTschuess(true);
            }
        });
        // laden was es schon gibt
        //Daten.ioXmlFilmlisteLesen.filmlisteLesen(Daten.getBasisVerzeichnis() + Konstanten.XML_DATEI_FILME, false /* istUrl */, Daten.listeFilme);
        new MSearchIoXmlFilmlisteLesen().filmlisteLesen(MSearchConfig.dateiFilmliste, listeFilme);
        // das eigentliche Suchen der Filme bei den Sendern starten
        if (MSearchConfig.nurSenderLaden == null) {
            mSearchFilmeSuchen.filmeBeimSenderLaden(listeFilme);
        } else {
            mSearchFilmeSuchen.updateSender(MSearchConfig.nurSenderLaden, listeFilme);
        }
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
        MSearchLog.systemMeldung("Programmpfad:   " + Funktionen.getPathJar());
        MSearchLog.systemMeldung("Sender löschen: " + senderLoeschen);
        MSearchLog.systemMeldung("Filmliste:      " + dateiFilmliste);
        MSearchLog.systemMeldung("###########################################################");
        MSearchLog.systemMeldung("");
        MSearchLog.systemMeldung("");
        new MSearchIoXmlFilmlisteLesen().filmlisteLesen(dateiFilmliste, listeFilme);
        // dann nur einen Sender löschen und dann wieder beenden
        int anz1 = listeFilme.size();
        MSearchLog.systemMeldung("Anzahl Filme vorher: " + anz1);
        listeFilme.delSender(senderLoeschen);
        int anz2 = listeFilme.size();
        MSearchLog.systemMeldung("Anzehl Filme nachher: " + anz2);
        MSearchLog.systemMeldung(" --> gelöscht: " + (anz1 - anz2));
        new MSearchIoXmlFilmlisteSchreiben().filmeSchreiben(dateiFilmliste, listeFilme);
        System.exit(0);
    }

    @Override
    public synchronized void run() {
        // für den MServer
        serverLaufen = true;
        if (MSearchConfig.dateiFilmliste.isEmpty()) {
            MSearchLog.systemMeldung("Keine URI der Filmliste angegeben");
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
        new MSearchIoXmlFilmlisteLesen().filmlisteLesen(MSearchConfig.dateiFilmliste, listeFilme);
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
        if (!MSearchConfig.importUrl__anhaengen.equals("")) {
            // wenn eine ImportUrl angegeben, dann die Filme die noch nicht drin sind anfügen
            MSearchLog.systemMeldung("Filmliste importieren (anhängen) von: " + MSearchConfig.importUrl__anhaengen);
            ListeFilme tmpListe = new ListeFilme();
            new MSearchIoXmlFilmlisteLesen().filmlisteLesen(MSearchConfig.importUrl__anhaengen, tmpListe);
            listeFilme.updateListe(tmpListe, false /* nur URL vergleichen */);
            tmpListe.clear();
            System.gc();
            listeFilme.sort();
        }
        if (!MSearchConfig.importUrl__ersetzen.equals("")) {
            // wenn eine ImportUrl angegeben, dann noch eine Liste importieren, Filme die es schon gibt
            // werden ersetzt
            MSearchLog.systemMeldung("Filmliste importieren (ersetzen) von: " + MSearchConfig.importUrl__ersetzen);
            ListeFilme tmpListe = new ListeFilme();
            new MSearchIoXmlFilmlisteLesen().filmlisteLesen(MSearchConfig.importUrl__ersetzen, tmpListe);
            tmpListe.updateListe(listeFilme, false /* nur URL vergleichen */);
            tmpListe.metaDaten = listeFilme.metaDaten;
            listeFilme.clear();
            System.gc();
            tmpListe.sort(); // jetzt sollte alles passen
            listeFilme = tmpListe;
        }
        new MSearchIoXmlFilmlisteSchreiben().filmeSchreiben(MSearchConfig.dateiFilmliste, listeFilme);
        if (!MSearchConfig.exportFilmliste.equals("")) {
            //datei schreiben
            new MSearchIoXmlFilmlisteSchreiben().filmeSchreiben(MSearchConfig.exportFilmliste, listeFilme);
        }
        MSearchLog.printEndeMeldung();
        // nur dann das Programm beenden
        if (exit) {
            System.exit(listeFilme.isEmpty() ? 1 : 0);
        }
    }
}
