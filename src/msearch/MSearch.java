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
import msearch.filmeSuchen.MSFilmeSuchen;
import msearch.filmeSuchen.MSListenerFilmeLaden;
import msearch.filmeSuchen.MSListenerFilmeLadenEvent;
import msearch.filmlisten.MSFilmlisteLesen;
import msearch.filmlisten.WriteFilmlistJson;
import msearch.tool.MSConfig;
import msearch.tool.MSLog;

public class MSearch implements Runnable {

    private ListeFilme listeFilme = new ListeFilme();
    private final MSFilmeSuchen msFilmeSuchen;
    private boolean serverLaufen = false;

    public MSearch() {
        msFilmeSuchen = new MSFilmeSuchen();
    }

    @Override
    public synchronized void run() {
        // für den MServer
        serverLaufen = true;
        MSConfig.setStop(false);//damits vom letzten mal stoppen nicht mehr gesetzt ist, falls es einen harten Abbruch gab
        if (MSConfig.dirFilme.isEmpty()) {
            MSLog.systemMeldung("Kein Pfad der Filmlisten angegeben");
            System.exit(-1);
        }
        // Infos schreiben
        MSLog.startMeldungen();
        MSLog.systemMeldung("");
        MSLog.systemMeldung("");
        msFilmeSuchen.addAdListener(new MSListenerFilmeLaden() {
            @Override
            public void fertig(MSListenerFilmeLadenEvent event) {
                serverLaufen = false;
            }
        });
        // laden was es schon gibt
        new MSFilmlisteLesen().readFilmListe(MSConfig.getPathFilmlist_json_akt(false /*aktDate*/), listeFilme, 0 /*all days*/);
        // das eigentliche Suchen der Filme bei den Sendern starten
        if (MSConfig.nurSenderLaden == null) {
            msFilmeSuchen.filmeBeimSenderLaden(listeFilme);
        } else {
            msFilmeSuchen.updateSender(MSConfig.nurSenderLaden, listeFilme);
        }
        try {
            while (serverLaufen) {
                this.wait(5000);
            }
        } catch (Exception ex) {
            MSLog.fehlerMeldung(496378742, "run()");
        }
        undTschuess();
    }

    public void stop() {
        if (serverLaufen) {
            // nur dann wird noch gesucht
            MSConfig.setStop();
        }
    }

    public ListeFilme getListeFilme() {
        return listeFilme;
    }

    private void importUrl(ListeFilme tmpListe, String importUrl) {
        //================================================
        // noch anere Listen importieren
        MSLog.systemMeldung("Filmliste importieren von: " + importUrl);
        tmpListe.clear();
        new MSFilmlisteLesen().readFilmListe(importUrl, tmpListe, 0 /*all days*/);
        MSLog.systemMeldung("--> von  Anz. Filme: " + listeFilme.size());
        listeFilme.updateListe(tmpListe, false /* nur URL vergleichen */, false /*ersetzen*/);
        MSLog.systemMeldung("--> nach Anz. Filme: " + listeFilme.size());
        tmpListe.clear();
        System.gc();
        listeFilme.sort();
    }

    private void importOld(ListeFilme tmpListe, String importUrl) {
        //================================================
        // noch anere Listen importieren
        MSLog.systemMeldung("Alte Filmliste importieren von: " + importUrl);
        tmpListe.clear();
        new MSFilmlisteLesen().readFilmListe(importUrl, tmpListe, 0 /*all days*/);
        MSLog.systemMeldung("--> von  Anz. Filme: " + listeFilme.size());
        int anz = listeFilme.updateListeOld(tmpListe);
        MSLog.systemMeldung("    gefunden: " + anz);
        MSLog.systemMeldung("--> nach Anz. Filme: " + listeFilme.size());
        tmpListe.clear();
        System.gc();
        listeFilme.sort();
    }

    private void undTschuess() {
        boolean stop = MSConfig.getStop();
        MSConfig.setStop(false); // zurücksetzen!! sonst klappt das Lesen der Importlisten nicht!!!!!
        listeFilme = msFilmeSuchen.listeFilmeNeu;
        ListeFilme tmpListe = new ListeFilme();

        //================================================
        // noch anere Listen importieren
        MSLog.systemMeldung("");
        if (!MSConfig.importUrl_1__anhaengen.isEmpty()) {
            // wenn eine ImportUrl angegeben, dann die Filme die noch nicht drin sind anfügen
            MSLog.systemMeldung("");
            MSLog.systemMeldung("============================================================================");
            MSLog.systemMeldung("Filmliste Import 1");
            importUrl(tmpListe, MSConfig.importUrl_1__anhaengen);
            MSLog.systemMeldung("");
        }
        if (!MSConfig.importUrl_2__anhaengen.isEmpty()) {
            // wenn eine ImportUrl angegeben, dann die Filme die noch nicht drin sind anfügen
            MSLog.systemMeldung("");
            MSLog.systemMeldung("============================================================================");
            MSLog.systemMeldung("Filmliste Import 2");
            importUrl(tmpListe, MSConfig.importUrl_2__anhaengen);
            MSLog.systemMeldung("");
        }
        if (!MSConfig.importOld.isEmpty() && MSConfig.loadLongMax()) {
            // wenn angeben, dann Filme die noch "gehen" aus einer alten Liste anhängen
            MSLog.systemMeldung("");
            MSLog.systemMeldung("============================================================================");
            MSLog.systemMeldung("Filmliste OLD importieren");
            importOld(tmpListe, MSConfig.importOld);
            MSLog.systemMeldung("");
        }

        //================================================
        // Filmliste schreiben, normal, xz komprimiert
        MSLog.systemMeldung("");
        MSLog.systemMeldung("");
        MSLog.systemMeldung("============================================================================");
        MSLog.systemMeldung("============================================================================");
        MSLog.systemMeldung("Filmeliste fertig: " + listeFilme.size() + " Filme");
        MSLog.systemMeldung("============================================================================");
        MSLog.systemMeldung("");
        MSLog.systemMeldung("   --> und schreiben:");
        new WriteFilmlistJson().filmlisteSchreibenJson(MSConfig.getPathFilmlist_json_akt(false /*aktDate*/), listeFilme);
        new WriteFilmlistJson().filmlisteSchreibenJson(MSConfig.getPathFilmlist_json_akt(true /*aktDate*/), listeFilme);
        new WriteFilmlistJson().filmlisteSchreibenJson(MSConfig.getPathFilmlist_json_akt_xz(), listeFilme);

        //================================================
        // Org
        MSLog.systemMeldung("");
        if (MSConfig.orgFilmlisteErstellen) {
            // org-Liste anlegen, typ. erste Liste am Tag
            MSLog.systemMeldung("");
            MSLog.systemMeldung("============================================================================");
            MSLog.systemMeldung("Org-Lilste schreiben: " + MSConfig.getPathFilmlist_json_org());
            new WriteFilmlistJson().filmlisteSchreibenJson(MSConfig.getPathFilmlist_json_org(), listeFilme);
            new WriteFilmlistJson().filmlisteSchreibenJson(MSConfig.getPathFilmlist_json_org_xz(), listeFilme);
        }

        //====================================================
        // noch das diff erzeugen
        String org = MSConfig.orgFilmliste.isEmpty() ? MSConfig.getPathFilmlist_json_org() : MSConfig.orgFilmliste;
        MSLog.systemMeldung("");
        MSLog.systemMeldung("============================================================================");
        MSLog.systemMeldung("Diff erzeugen, von: " + org + " nach: " + MSConfig.getPathFilmlist_json_diff());
        tmpListe.clear();
        ListeFilme diff;
        new MSFilmlisteLesen().readFilmListe(org, tmpListe, 0 /*all days*/);
        if (tmpListe.isEmpty()) {
            // dann ist die komplette Liste das diff
            MSLog.systemMeldung("   --> Lesefehler der Orgliste: Diff bleibt leer!");
            diff = new ListeFilme();
        } else if (tmpListe.isOlderThan(24 * 60 * 60)) {
            // älter als ein Tag, dann stimmt was nicht!
            MSLog.systemMeldung("   --> Orgliste zu alt: Diff bleibt leer!");
            diff = new ListeFilme();
        } else {
            // nur dann macht die Arbeit sinn
            diff = listeFilme.neueFilme(tmpListe);
        }
        MSLog.systemMeldung("   --> und schreiben:");
        new WriteFilmlistJson().filmlisteSchreibenJson(MSConfig.getPathFilmlist_json_diff(), diff);
        new WriteFilmlistJson().filmlisteSchreibenJson(MSConfig.getPathFilmlist_json_diff_xz(), diff);
        MSLog.systemMeldung("   --> Anz. Filme Diff: " + diff.size());

        //================================================
        // fertig
        MSLog.endeMeldung();
    }

}
