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
package msearch.filmlisten;

import java.util.ArrayList;
import javax.swing.event.EventListenerList;
import msearch.daten.ListeFilme;
import msearch.filmeSuchen.MSListenerFilmeLaden;
import msearch.filmeSuchen.MSListenerFilmeLadenEvent;
import msearch.tool.MSConfig;
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

    // #########################################################
    // Filmeliste importieren, URL automatisch wählen
    // #########################################################
    public void filmeImportierenAuto(ListeFilme listeFilme, ListeFilme listeFilmeDiff, int days) {
        MSConfig.setStop(false);
        new Thread(new FilmeImportierenAutoThread(listeFilme, listeFilmeDiff, days)).start();
    }

    private class FilmeImportierenAutoThread implements Runnable {

        private final ListeFilme listeFilme;
        private final ListeFilme listeFilmeDiff;
        private final int STATE_AKT = 2;
        private final int STATE_DIFF = 3;
        private int state;
        private int days;

        public FilmeImportierenAutoThread(ListeFilme listeFilme, ListeFilme listeFilmeDiff, int days) {
            this.listeFilme = listeFilme;
            this.listeFilmeDiff = listeFilmeDiff;
            this.days = days;
        }

        @Override
        public void run() {
            boolean ret;
            if (listeFilme.isTooOldForDiff()) {
                // dann eine komplette Liste laden
                state = STATE_AKT;
                listeFilme.clear();
                ret = suchenAktListe(listeFilme);
            } else {
                // nur ein Update laden
                state = STATE_DIFF;
                ret = suchenAktListe(listeFilmeDiff);
                if (listeFilmeDiff.isEmpty()) {
                    // wenn diff, dann nochmal mit einer kompletten Liste versuchen
                    state = STATE_AKT;
                    listeFilme.clear();
                    listeFilmeDiff.clear();
                    ret = suchenAktListe(listeFilme);
                }
            }
            if (!ret /* listeFilme ist schon wieder null -> "FilmeLaden" */) {
                MSLog.fehlerMeldung(951235497, MSLog.FEHLER_ART_PROG, "Filme laden", "Es konnten keine Filme geladen werden!");
            }
            fertigMelden(ret);
        }

        private boolean suchenAktListe(ListeFilme liste) {
            boolean ret = false;
            ArrayList<String> versuchteUrls = new ArrayList<>();
            String updateUrl = "";

            switch (state) {
                case STATE_AKT:
                    updateUrl = msFilmlistenSuchen.suchenAkt(versuchteUrls);
                    break;
                case STATE_DIFF:
                    updateUrl = msFilmlistenSuchen.suchenDiff(versuchteUrls);
                    break;
            }

            if (updateUrl.isEmpty()) {
                return false;
            }

            // 5 mal mit einem anderen Server probieren, wenns nicht klappt
            for (int i = 0; i < 5; ++i) {
                switch (state) {
                    case STATE_AKT:
                        ret = urlLaden(updateUrl, liste, days);
                        break;
                    case STATE_DIFF:
                        ret = urlLaden(updateUrl, liste, days); // dann muss die komplette Liste erst später geschrieben werden
                        break;
                }

                if (ret && i < 1 && liste.isOlderThan(5 * 60 * 60 /*sekunden*/)) {
                    // Laden hat geklappt ABER: Liste zu alt, dann gibts einen 2. Versuch
                    MSLog.systemMeldung("Filmliste zu alt, neuer Versuch");
                    ret = false;
                }

                if (ret) {
                    // hat geklappt, nix wie weiter
                    return true;
                }

                switch (state) {
                    case STATE_AKT:
                        updateUrl = msFilmlistenSuchen.listeFilmlistenUrls_akt.getRand(versuchteUrls); //nächste Adresse in der Liste wählen
                        break;
                    case STATE_DIFF:
                        updateUrl = msFilmlistenSuchen.listeFilmlistenUrls_diff.getRand(versuchteUrls); //nächste Adresse in der Liste wählen
                        break;
                }
                versuchteUrls.add(updateUrl);
                // nur wenn nicht abgebrochen, weitermachen
                if (MSConfig.getStop()) {
                    break;
                }

            }
            return ret;
        }
    }

    // #######################################
    // Filmeliste importieren, mit fester URL/Pfad
    // #######################################
    public void filmeImportierenDatei(String pfad, ListeFilme listeFilme, int days) {
        MSConfig.setStop(false);
        new Thread(new FilmeImportierenDateiThread(pfad, listeFilme, days)).start();

    }

    private class FilmeImportierenDateiThread implements Runnable {

        private final String pfad;
        private final ListeFilme listeFilme;
        private final int days;

        public FilmeImportierenDateiThread(String pfad, ListeFilme listeFilme, int days) {
            this.pfad = pfad;
            this.listeFilme = listeFilme;
            this.days = days;
        }

        @Override
        public void run() {
            fertigMelden(urlLaden(pfad, listeFilme, days));
        }
    }

    // #######################################
    // #######################################
    public void addAdListener(MSListenerFilmeLaden listener) {
        listeners.add(MSListenerFilmeLaden.class, listener);
    }

    public void updateDownloadUrlsFilmlisten(boolean akt) {
        msFilmlistenSuchen.updateURLsFilmlisten(akt);
    }

    private boolean urlLaden(String dateiUrl, ListeFilme listeFilme, int days) {
        boolean ret = false;
        try {
            if (!dateiUrl.equals("")) {
                MSLog.systemMeldung("Filmliste laden von: " + dateiUrl);
                msFilmlisteLesen.readFilmListe(dateiUrl, listeFilme, days);
                if (!listeFilme.isEmpty()) {
                    ret = true;
                }
            }
        } catch (Exception ex) {
            MSLog.fehlerMeldung(965412378, MSLog.FEHLER_ART_PROG, "ImportListe.urlLaden: ", ex);
        }
        return ret;

    }

    private synchronized void fertigMelden(boolean ok) {
        for (MSListenerFilmeLaden l : listeners.getListeners(MSListenerFilmeLaden.class)) {
            l.fertig(new MSListenerFilmeLadenEvent("", "", 0, 0, 0, !ok));
        }
    }
}
