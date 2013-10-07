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
import java.util.Iterator;
import java.util.LinkedList;
import msearch.filmeSuchen.MSearchListenerFilmeLaden;
import msearch.filmeSuchen.MSearchListenerFilmeLadenEvent;
import msearch.io.MSearchIoXmlFilmlisteLesen;
import msearch.io.MSearchIoXmlFilmlisteSchreiben;
import msearch.daten.MSearchConfig;
import msearch.daten.ListeFilme;
import msearch.tool.MSearchLog;

public class Search {

    private ListeFilme listeFilme = new ListeFilme();
    private MSearchFilmeSuchen filmeSuchenSender;

    public Search(String[] ar) {
        filmeSuchenSender = new MSearchFilmeSuchen();
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
        filmeSuchenSender.addAdListener(new MSearchListenerFilmeLaden() {
            @Override
            public void fertig(MSearchListenerFilmeLadenEvent event) {
                undTschuess(true /* exit */);
            }
        });
        // laden was es schon gibt
        //Daten.ioXmlFilmlisteLesen.filmlisteLesen(Daten.getBasisVerzeichnis() + Konstanten.XML_DATEI_FILME, false /* istUrl */, Daten.listeFilme);
        new MSearchIoXmlFilmlisteLesen().filmlisteLesen(MSearchConfig.dateiFilmliste, listeFilme);
        // das eigentliche Suchen der Filme bei den Sendern starten
        if (MSearchConfig.nurSenderLaden == null) {
            filmeSuchenSender.filmeBeimSenderLaden(listeFilme);
        } else {
            filmeSuchenSender.updateSender(MSearchConfig.nurSenderLaden, listeFilme);
        }
    }

    public void addAdListener(MSearchListenerFilmeLaden listener) {
        filmeSuchenSender.addAdListener(listener);
    }

    public ListeFilme getListeFilme() {
        return listeFilme;
    }

    private void undTschuess(boolean exit) {
        if (!MSearchConfig.importUrl__anhaengen.equals("")) {
            // wenn eine ImportUrl angegeben, dann die Filme die noch nicht drin sind anfügen
            MSearchLog.systemMeldung("Filmliste importieren von: " + MSearchConfig.importUrl__anhaengen);
            ListeFilme tmpListe = new ListeFilme();
            new MSearchIoXmlFilmlisteLesen().filmlisteLesen(MSearchConfig.importUrl__anhaengen, tmpListe);
            listeFilme.updateListe(tmpListe, false /* nur URL vergleichen */);
            tmpListe.clear();
        }
        if (!MSearchConfig.importUrl__ersetzen.equals("")) {
            // wenn eine ImportUrl angegeben, dann noch eine Liste importieren, Filme die es schon gibt
            // werden ersetzt
            MSearchLog.systemMeldung("Filmliste importieren von: " + MSearchConfig.importUrl__ersetzen);
            ListeFilme tmpListe = new ListeFilme();
            new MSearchIoXmlFilmlisteLesen().filmlisteLesen(MSearchConfig.importUrl__ersetzen, tmpListe);
            tmpListe.updateListe(listeFilme, false /* nur URL vergleichen */);
            tmpListe.metaDaten = listeFilme.metaDaten;
            tmpListe.sort(); // jetzt sollte alles passen
            listeFilme.clear();
            listeFilme = tmpListe;
        }
        new MSearchIoXmlFilmlisteSchreiben().filmeSchreiben(MSearchConfig.dateiFilmliste, listeFilme);
        if (!MSearchConfig.exportFilmliste.equals("")) {
            LinkedList<String> out = new LinkedList<>();
            String tmp;
            do {
                if (MSearchConfig.exportFilmliste.startsWith(",")) {
                    MSearchConfig.exportFilmliste = MSearchConfig.exportFilmliste.substring(1);
                }
                if (MSearchConfig.exportFilmliste.contains(",")) {
                    tmp = MSearchConfig.exportFilmliste.substring(0, MSearchConfig.exportFilmliste.indexOf(","));
                    MSearchConfig.exportFilmliste = MSearchConfig.exportFilmliste.substring(MSearchConfig.exportFilmliste.indexOf(","));
                    out.add(tmp);
                } else {
                    out.add(MSearchConfig.exportFilmliste);
                }
            } while (MSearchConfig.exportFilmliste.contains(","));
            Iterator<String> it = out.iterator();
            while (it.hasNext()) {
                //datei schreiben
                new MSearchIoXmlFilmlisteSchreiben().filmeSchreiben(it.next(), listeFilme);
            }
        }
        MSearchLog.printEndeMeldung();
        if (exit) {
            // nur dann das Programm beenden
            if (listeFilme.isEmpty()) {
                //Satz mit x, war wohl nix
                System.exit(1);
            } else {
                System.exit(0);
            }
        }
    }
}
