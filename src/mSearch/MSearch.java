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
package mSearch;

import mSearch.daten.ListeFilme;
import mSearch.filmeSuchen.MSFilmeSuchen;
import mSearch.filmeSuchen.MSListenerFilmeLaden;
import mSearch.filmeSuchen.MSListenerFilmeLadenEvent;
import mSearch.filmlisten.MSFilmlisteLesen;
import mSearch.filmlisten.WriteFilmlistJson;
import mSearch.tool.MSConfig;
import mSearch.tool.Log;

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
            Log.systemMeldung("Kein Pfad der Filmlisten angegeben");
            System.exit(-1);
        }
        // Infos schreiben
        Log.startMeldungen();
        Log.systemMeldung("");
        Log.systemMeldung("");
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
            Log.fehlerMeldung(496378742, "run()");
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

    private void importLive(ListeFilme tmpListe, String importUrl) {
        //================================================
        // noch anere Listen importieren
        Log.systemMeldung("Live-Streams importieren von: " + importUrl);
        tmpListe.clear();
        new MSFilmlisteLesen().readFilmListe(importUrl, tmpListe, 0 /*all days*/);
        Log.systemMeldung("--> von  Anz. Filme: " + listeFilme.size());
        listeFilme.addLive(tmpListe);
        Log.systemMeldung("--> nach Anz. Filme: " + listeFilme.size());
        tmpListe.clear();
        System.gc();
        listeFilme.sort();
    }

    private void importUrl(ListeFilme tmpListe, String importUrl) {
        //================================================
        // noch anere Listen importieren
        Log.systemMeldung("Filmliste importieren von: " + importUrl);
        tmpListe.clear();
        new MSFilmlisteLesen().readFilmListe(importUrl, tmpListe, 0 /*all days*/);
        Log.systemMeldung("--> von  Anz. Filme: " + listeFilme.size());
        listeFilme.updateListe(tmpListe, false /* nur URL vergleichen */, false /*ersetzen*/);
        Log.systemMeldung("--> nach Anz. Filme: " + listeFilme.size());
        tmpListe.clear();
        System.gc();
        listeFilme.sort();
    }

    private void importOld(ListeFilme tmpListe, String importUrl) {
        //================================================
        // noch anere Listen importieren
        Log.systemMeldung("Alte Filmliste importieren von: " + importUrl);
        tmpListe.clear();
        new MSFilmlisteLesen().readFilmListe(importUrl, tmpListe, 0 /*all days*/);
        Log.systemMeldung("--> von  Anz. Filme: " + listeFilme.size());
        int anz = listeFilme.updateListeOld(tmpListe);
        Log.systemMeldung("    gefunden: " + anz);
        Log.systemMeldung("--> nach Anz. Filme: " + listeFilme.size());
        tmpListe.clear();
        System.gc();
        listeFilme.sort();
    }

    private void undTschuess() {
        MSConfig.setStop(false); // zurücksetzen!! sonst klappt das Lesen der Importlisten nicht!!!!!
        listeFilme = msFilmeSuchen.listeFilmeNeu;
        ListeFilme tmpListe = new ListeFilme();

        //================================================
        // noch anere Listen importieren
        Log.systemMeldung("");
        if (!MSConfig.importLive.isEmpty()) {
            // wenn eine ImportUrl angegeben, dann die Filme die noch nicht drin sind anfügen
            Log.systemMeldung("");
            Log.systemMeldung("============================================================================");
            Log.systemMeldung("Live-Streams importieren");
            importLive(tmpListe, MSConfig.importLive);
            Log.systemMeldung("");
        }
        if (!MSConfig.importUrl_1__anhaengen.isEmpty()) {
            // wenn eine ImportUrl angegeben, dann die Filme die noch nicht drin sind anfügen
            Log.systemMeldung("");
            Log.systemMeldung("============================================================================");
            Log.systemMeldung("Filmliste Import 1");
            importUrl(tmpListe, MSConfig.importUrl_1__anhaengen);
            Log.systemMeldung("");
        }
        if (!MSConfig.importUrl_2__anhaengen.isEmpty()) {
            // wenn eine ImportUrl angegeben, dann die Filme die noch nicht drin sind anfügen
            Log.systemMeldung("");
            Log.systemMeldung("============================================================================");
            Log.systemMeldung("Filmliste Import 2");
            importUrl(tmpListe, MSConfig.importUrl_2__anhaengen);
            Log.systemMeldung("");
        }
        if (!MSConfig.importOld.isEmpty() && MSConfig.loadLongMax()) {
            // wenn angeben, dann Filme die noch "gehen" aus einer alten Liste anhängen
            Log.systemMeldung("");
            Log.systemMeldung("============================================================================");
            Log.systemMeldung("Filmliste OLD importieren");
            importOld(tmpListe, MSConfig.importOld);
            Log.systemMeldung("");
        }

        //================================================
        // Filmliste schreiben, normal, xz komprimiert
        Log.systemMeldung("");
        Log.systemMeldung("");
        Log.systemMeldung("============================================================================");
        Log.systemMeldung("============================================================================");
        Log.systemMeldung("Filmeliste fertig: " + listeFilme.size() + " Filme");
        Log.systemMeldung("============================================================================");
        Log.systemMeldung("");
        Log.systemMeldung("   --> und schreiben:");
        new WriteFilmlistJson().filmlisteSchreibenJson(MSConfig.getPathFilmlist_json_akt(false /*aktDate*/), listeFilme);
        new WriteFilmlistJson().filmlisteSchreibenJson(MSConfig.getPathFilmlist_json_akt(true /*aktDate*/), listeFilme);
        new WriteFilmlistJson().filmlisteSchreibenJson(MSConfig.getPathFilmlist_json_akt_xz(), listeFilme);

        //================================================
        // Org
        Log.systemMeldung("");
        if (MSConfig.orgFilmlisteErstellen) {
            // org-Liste anlegen, typ. erste Liste am Tag
            Log.systemMeldung("");
            Log.systemMeldung("============================================================================");
            Log.systemMeldung("Org-Lilste schreiben: " + MSConfig.getPathFilmlist_json_org());
            new WriteFilmlistJson().filmlisteSchreibenJson(MSConfig.getPathFilmlist_json_org(), listeFilme);
            new WriteFilmlistJson().filmlisteSchreibenJson(MSConfig.getPathFilmlist_json_org_xz(), listeFilme);
        }

        //====================================================
        // noch das diff erzeugen
        String org = MSConfig.orgFilmliste.isEmpty() ? MSConfig.getPathFilmlist_json_org() : MSConfig.orgFilmliste;
        Log.systemMeldung("");
        Log.systemMeldung("============================================================================");
        Log.systemMeldung("Diff erzeugen, von: " + org + " nach: " + MSConfig.getPathFilmlist_json_diff());
        tmpListe.clear();
        ListeFilme diff;
        new MSFilmlisteLesen().readFilmListe(org, tmpListe, 0 /*all days*/);
        if (tmpListe.isEmpty()) {
            // dann ist die komplette Liste das diff
            Log.systemMeldung("   --> Lesefehler der Orgliste: Diff bleibt leer!");
            diff = new ListeFilme();
        } else if (tmpListe.isOlderThan(24 * 60 * 60)) {
            // älter als ein Tag, dann stimmt was nicht!
            Log.systemMeldung("   --> Orgliste zu alt: Diff bleibt leer!");
            diff = new ListeFilme();
        } else {
            // nur dann macht die Arbeit sinn
            diff = listeFilme.neueFilme(tmpListe);
        }
        Log.systemMeldung("   --> und schreiben:");
        new WriteFilmlistJson().filmlisteSchreibenJson(MSConfig.getPathFilmlist_json_diff(), diff);
        new WriteFilmlistJson().filmlisteSchreibenJson(MSConfig.getPathFilmlist_json_diff_xz(), diff);
        Log.systemMeldung("   --> Anz. Filme Diff: " + diff.size());

        //================================================
        // fertig
        Log.endeMeldung();
    }

}
