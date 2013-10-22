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
import javax.swing.JOptionPane;
import javax.swing.event.EventListenerList;
import msearch.daten.ListeFilme;
import msearch.filmeSuchen.MSearchListenerFilmeLaden;
import msearch.filmeSuchen.MSearchListenerFilmeLadenEvent;
import msearch.io.MSearchFilmlisteLesen;
import msearch.tool.MSearchListenerMediathekView;
import msearch.tool.MSearchLog;
import msearch.tool.MVMessageDialog;

public class MSearchImportFilmliste {

//    public ListeFilme listeFilme;
    public String[] filmlisteMetaDaten;
    private EventListenerList listeners = new EventListenerList();
    private MSearchFilmlisteLesen ioXmlFilmlisteLesen = null;
    public MSearchFilmlistenSuchen filmlistenSuchen = new MSearchFilmlistenSuchen();

    public MSearchImportFilmliste() {
        ioXmlFilmlisteLesen = new MSearchFilmlisteLesen();
        ioXmlFilmlisteLesen.addAdListener(new MSearchListenerFilmeLaden() {
            @Override
            public synchronized void start(MSearchListenerFilmeLadenEvent event) {
                for (MSearchListenerFilmeLaden l : listeners.getListeners(MSearchListenerFilmeLaden.class)) {
                    l.start(event);
                }
            }

            @Override
            public synchronized void progress(MSearchListenerFilmeLadenEvent event) {
                for (MSearchListenerFilmeLaden l : listeners.getListeners(MSearchListenerFilmeLaden.class)) {
                    l.progress(event);

                }
            }

            @Override
            public synchronized void fertig(MSearchListenerFilmeLadenEvent event) {
            }
        });
    }

    public void addAdListener(MSearchListenerFilmeLaden listener) {
        listeners.add(MSearchListenerFilmeLaden.class, listener);
    }

    // #######################################
    // Filme von Server/Datei importieren
    // #######################################
    public ListeDownloadUrlsFilmlisten getDownloadUrlsFilmlisten(boolean update) {
        if (update) {
            filmlistenSuchen.suchen(null);
        }
        return filmlistenSuchen.listeDownloadUrlsFilmlisten;
    }

    public ListeFilmlistenServer getListeFilmlistnServer() {
        return filmlistenSuchen.listeFilmlistenServer;
    }

    // #########################################################
    // Filme als Liste importieren
    // #########################################################
    public void importFilmliste(String dateiUrl, ListeFilme listeFilme) {
        MSearchListenerMediathekView.notify(MSearchListenerMediathekView.EREIGNIS_FILMLISTE_GEAENDERT, MSearchFilmeLaden.class.getSimpleName());
        if (dateiUrl.equals("")) {
            // Filme als Liste importieren, Url automatisch ermitteln
            filmeImportierenAuto(listeFilme);
        } else {
            // Filme als Liste importieren, feste URL/Datei
            filmeImportierenDatei(dateiUrl, listeFilme);
        }
    }

    public void filmeImportierenAuto(ListeFilme listeFilme) {
        new Thread(new FilmeImportierenAutoThread(listeFilme)).start();
    }

    private class FilmeImportierenAutoThread implements Runnable {

        private ListeFilme listeFilme;

        public FilmeImportierenAutoThread(ListeFilme llisteFilme) {
            listeFilme = llisteFilme;
        }

        @Override
        public void run() {
            //wenn auto-update-url dann erst mal die Updateserver aktualiseren
            boolean ret = false;
            ArrayList<String> versuchteUrls = new ArrayList<String>();
            String updateUrl = filmlistenSuchen.suchen(versuchteUrls);
            if (!updateUrl.equals("")) {
                for (int i = 0; i < 10; ++i) {
                    //10 mal mit einem anderen Server probieren
                    if (urlLaden(updateUrl, listeFilme)) {
                        // hat geklappt, nix wie weiter
                        ret = true; // keine Fehlermeldung
                        if (i < 4 && listeFilme.filmlisteIstAelter(5 * 60 * 60 /*sekunden*/)) {
                            MSearchLog.systemMeldung("Filmliste zu alt, neuer Versuch");
                        } else {
                            // 5 Versuche mit einer alten Liste sind genug
                            break;
                        }
                    }
                    updateUrl = filmlistenSuchen.listeDownloadUrlsFilmlisten.getRand(versuchteUrls, i); //nächste Adresse in der Liste wählen
                    versuchteUrls.add(updateUrl);
                }
            }
            if (!ret /* listeFilme ist schon wieder null -> "FilmeLaden" */) {
                MVMessageDialog.showMessageDialog(null, "Das Laden der Filmliste hat nicht geklappt!", "Fehler", JOptionPane.ERROR_MESSAGE);
                MSearchLog.fehlerMeldung(951235497, MSearchLog.FEHLER_ART_PROG, "Filme laden", "Es konnten keine Filme geladen werden!");
            }
            fertigMelden();
        }
    }

    // #######################################
    // Filme aus Datei laden
    // #######################################
    public void filmeImportierenDatei(String pfad, ListeFilme listeFilme) {
        new Thread(new FilmeImportierenDateiThread(pfad, listeFilme)).start();
    }

    private class FilmeImportierenDateiThread implements Runnable {

        private String pfad;
        private ListeFilme listeFilme;

        public FilmeImportierenDateiThread(String ppfad, ListeFilme llisteFilme) {
            pfad = ppfad;
            listeFilme = llisteFilme;
        }

        @Override
        public void run() {
            if (!urlLaden(pfad, listeFilme)) {
                MVMessageDialog.showMessageDialog(null, "Das Laden der Filmliste hat nicht geklappt!", "Fehler", JOptionPane.ERROR_MESSAGE);
            }
            fertigMelden();
        }
    }

    //===================================
    // private
    //===================================
    private boolean urlLaden(String dateiUrl, ListeFilme listeFilme) {
        boolean ret = false;
        try {
            if (!dateiUrl.equals("")) {
                MSearchLog.systemMeldung("Filmliste laden von: " + dateiUrl);
//                listeFilme = new ListeFilme();
                ret = ioXmlFilmlisteLesen.filmlisteLesenXml(dateiUrl, listeFilme);
            }
        } catch (Exception ex) {
            MSearchLog.fehlerMeldung(965412378, MSearchLog.FEHLER_ART_PROG, "ImportListe.urlLaden: ", ex);
        }
        return ret;
    }

    private synchronized void fertigMelden() {
        for (MSearchListenerFilmeLaden l : listeners.getListeners(MSearchListenerFilmeLaden.class)) {
            l.fertig(new MSearchListenerFilmeLadenEvent("", "", 0, 0));

        }
    }
}
