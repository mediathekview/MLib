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
package msearch.filmeLaden;

import java.util.ArrayList;
import javax.swing.event.EventListenerList;
import msearch.daten.ListeFilme;
import msearch.daten.MSConfig;
import msearch.filmeSuchen.MSListenerFilmeLaden;
import msearch.filmeSuchen.MSListenerFilmeLadenEvent;
import msearch.io.MSFilmlisteLesen;
import msearch.tool.MSLog;

public class MSImportFilmliste {

    private EventListenerList listeners;
    private final MSFilmlisteLesen msFilmlisteLesen;
    public MSFilmlistenSuchen msFilmlistenSuchen;

    public MSImportFilmliste() {
        listeners = new EventListenerList();
        msFilmlisteLesen = new MSFilmlisteLesen();
        msFilmlistenSuchen = new MSFilmlistenSuchen();
        msFilmlisteLesen.addAdListener(new MSListenerFilmeLaden() {
            @Override
            public synchronized void start(MSListenerFilmeLadenEvent event) {
                for (MSListenerFilmeLaden l : listeners.getListeners(MSListenerFilmeLaden.class)) {
                    l.start(event);
                }
            }

            @Override
            public synchronized void progress(MSListenerFilmeLadenEvent event) {
                for (MSListenerFilmeLaden l : listeners.getListeners(MSListenerFilmeLaden.class)) {
                    l.progress(event);

                }
            }

            @Override
            public synchronized void fertig(MSListenerFilmeLadenEvent event) {
            }
        });
    }

    public void addAdListener(MSListenerFilmeLaden listener) {
        listeners.add(MSListenerFilmeLaden.class, listener);
    }

    // #######################################
    // Filme von Server/Datei importieren
    // #######################################
    public ListeDownloadUrlsFilmlisten getDownloadUrls_Filmlisten(boolean update, boolean listeDiffs) {
        // update: vorher ein update der Liste
        // diff: ist die Liste mit den Diffs
        if (listeDiffs) {
            if (update) {
                msFilmlistenSuchen.suchenDiff(null);
            }
            return msFilmlistenSuchen.listeDownloadUrlsFilmlisten_diff;
        } else {
            if (update) {
                msFilmlistenSuchen.suchen(null);
            }
            return msFilmlistenSuchen.listeDownloadUrlsFilmlisten;
        }
    }

    public ListeFilmlistenServer getListe_FilmlistenServer() {
        return msFilmlistenSuchen.listeFilmlistenServer;
    }

    // #########################################################
    // Filmeliste importieren, URL automatisch wählen
    // #########################################################
    public void filmeImportierenAuto(String dateiZiel, ListeFilme listeFilme, ListeFilme listeFilmeDiff) {
        MSConfig.setStop(false);
        new Thread(new FilmeImportierenAutoThread(dateiZiel, listeFilme, listeFilmeDiff)).start();
    }

    private class FilmeImportierenAutoThread implements Runnable {

        private final ListeFilme listeFilme;
        private final ListeFilme listeFilmeDiff;
        private final String ziel;
        private boolean diff = false;

        public FilmeImportierenAutoThread(String dateiZiel, ListeFilme llisteFilme, ListeFilme llisteFilmeDiff) {
            ziel = dateiZiel;
            listeFilme = llisteFilme;
            listeFilmeDiff = llisteFilmeDiff;
        }

        @Override
        public void run() {
            boolean ret;
            if (listeFilme.toOldForDiff()) {
                // dann eine komplette Liste laden
                listeFilme.clear();
                ret = suchen(listeFilme);
            } else {
                // nur ein Update laden
                diff = true;
                ret = suchen(listeFilmeDiff);
                if (listeFilmeDiff.isEmpty()) {
                    ret = false;
                }
                if (!ret) {
                    // wenn diff, dann nochmal mit einer kompletten Liste versuchen
                    diff = false;
                    listeFilme.clear();
                    listeFilmeDiff.clear();
                    ret = suchen(listeFilme);
                }
            }
            if (!ret /* listeFilme ist schon wieder null -> "FilmeLaden" */) {
                MSLog.fehlerMeldung(951235497, MSLog.FEHLER_ART_PROG, "Filme laden", "Es konnten keine Filme geladen werden!");
            }
            fertigMelden(ret);
        }

        private boolean suchen(ListeFilme liste) {
            //wenn auto-update-url dann erst mal die Updateserver aktualiseren
            boolean ret = false;
            ArrayList<String> versuchteUrls = new ArrayList<>();
            String updateUrl = diff ? msFilmlistenSuchen.suchenDiff(versuchteUrls) : msFilmlistenSuchen.suchen(versuchteUrls);

            if (!updateUrl.equals("")) {
                for (int i = 0; i < 5; ++i) {
                    //5 mal mit einem anderen Server probieren
                    if (urlLaden(updateUrl, diff ? "" : ziel, liste)) { // dann muss die komplette Liste erst später geschrieben werden
                        // hat geklappt, nix wie weiter
                        ret = true; // keine Fehlermeldung
                        if (i < 3 && liste.filmlisteIstAelter(5 * 60 * 60 /*sekunden*/)) {
                            MSLog.systemMeldung("Filmliste zu alt, neuer Versuch");
                        } else {
                            // 3 Versuche mit einer alten Liste sind genug
                            break;
                        }
                    } else {
                        // nur wenn nicht abgebrochen, weitermachen
                        if (MSConfig.getStop()) {
                            break;
                        }
                    }
                    updateUrl = msFilmlistenSuchen.listeDownloadUrlsFilmlisten_diff.getRand(versuchteUrls, i); //nächste Adresse in der Liste wählen
                    versuchteUrls.add(updateUrl);
                }
            }
            return ret;
        }
    }

    // #######################################
    // Filmeliste importieren, mit fester URL/Pfad
    // #######################################
    public void filmeImportierenDatei(String pfad, String dateiZiel, ListeFilme listeFilme) {
        MSConfig.setStop(false);
        new Thread(new FilmeImportierenDateiThread(pfad, dateiZiel, listeFilme)).start();
    }

    private class FilmeImportierenDateiThread implements Runnable {

        private final String pfad;
        private final String ziel;
        private final ListeFilme listeFilme;

        public FilmeImportierenDateiThread(String ppfad, String dateiZiel, ListeFilme llisteFilme) {
            pfad = ppfad;
            ziel = dateiZiel;
            listeFilme = llisteFilme;
        }

        @Override
        public void run() {
            fertigMelden(urlLaden(pfad, ziel, listeFilme));
        }
    }

    //===================================
    // private
    //===================================
    private boolean urlLaden(String dateiUrl, String dateiZiel, ListeFilme listeFilme) {
        boolean ret = false;
        try {
            if (!dateiUrl.equals("")) {
                MSLog.systemMeldung("Filmliste laden von: " + dateiUrl);
                ret = msFilmlisteLesen.filmlisteLesenJson(dateiUrl, dateiZiel, listeFilme);
            }
        } catch (Exception ex) {
            MSLog.fehlerMeldung(965412378, MSLog.FEHLER_ART_PROG, "ImportListe.urlLaden: ", ex);
        }
        return ret;
    }

    private synchronized void fertigMelden(boolean ok) {
        for (MSListenerFilmeLaden l : listeners.getListeners(MSListenerFilmeLaden.class)) {
            l.fertig(new MSListenerFilmeLadenEvent("", "", 0, 0, !ok));
        }
    }
}
