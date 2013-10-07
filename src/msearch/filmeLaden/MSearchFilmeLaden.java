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

import javax.swing.event.EventListenerList;
import msearch.daten.ListeFilme;
import msearch.daten.MSearchConfig;
import msearch.filmeSuchen.MSearchListenerFilmeLaden;
import msearch.filmeSuchen.MSearchListenerFilmeLadenEvent;
import msearch.tool.MSearchListenerMediathekView;

public class MSearchFilmeLaden {

    public static final int ALTER_FILMLISTE_SEKUNDEN_FUER_AUTOUPDATE = 3 * 60 * 60; // beim Start des Programms wir die Liste geladen wenn sie älter ist als ..
    // private
    private boolean stop = false;
    // private ListeFilme listeFilmeAlt = null; // ist nur eine Referenz auf die bestehende Liste und die bleibt unverändert!!!
    // private ListeFilme listeFilmeNeu = null; //ist eine NEUE ungefilterte Liste, wird beim Laden NEU erstellt
    private MSearchImportFilmliste filmeImportieren;
    private EventListenerList listeners = new EventListenerList();

    public MSearchFilmeLaden() {
        filmeImportieren = new MSearchImportFilmliste();
        filmeImportieren.addAdListener(new MSearchListenerFilmeLaden() {
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
                // Ergebnisliste listeFilme eintragen -> Feierabend!
                for (MSearchListenerFilmeLaden l : listeners.getListeners(MSearchListenerFilmeLaden.class)) {
                    l.fertig(event);
                }
            }
        });
    }

    public void addAdListener(MSearchListenerFilmeLaden listener) {
        listeners.add(MSearchListenerFilmeLaden.class, listener);
    }

    public ListeDownloadUrlsFilmlisten getDownloadUrlsFilmlisten(boolean update) {
        if (update) {
            filmeImportieren.filmlistenSuchen.suchen(null);
        }
        return filmeImportieren.filmlistenSuchen.listeDownloadUrlsFilmlisten;
    }

    public ListeFilmlistenServer getListeFilmlistnServer() {
        return filmeImportieren.filmlistenSuchen.listeFilmlistenServer;
    }

    // #########################################################
    // Filme als Liste importieren
    // #########################################################
    public void importFilmliste(String dateiUrl, ListeFilme listeFilme) {
        stop = false;
        MSearchListenerMediathekView.notify(MSearchListenerMediathekView.EREIGNIS_FILMLISTE_GEAENDERT, MSearchFilmeLaden.class.getSimpleName());
        if (dateiUrl.equals("")) {
            // Filme als Liste importieren, Url automatisch ermitteln
            filmeImportieren.filmeImportierenAuto(listeFilme);
        } else {
            // Filme als Liste importieren, feste URL/Datei
            filmeImportieren.filmeImportierenDatei(dateiUrl, listeFilme);
        }
    }
}
