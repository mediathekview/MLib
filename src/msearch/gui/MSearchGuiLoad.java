/*
 * MediathekView
 * Copyright (C) 2016 W. Xaver
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
package msearch.gui;

import javafx.application.Platform;
import javax.swing.event.EventListenerList;
import msearch.filmeSuchen.MSFilmeSuchen;
import msearch.filmeSuchen.MSListenerFilmeLaden;
import msearch.filmeSuchen.MSListenerFilmeLadenEvent;
import msearch.tool.MSConfig;

public class MSearchGuiLoad {

    private enum ListenerMelden {

        START, PROGRESS, FINISHED
    }
    public MSFilmeSuchen msFilmeSuchen;
    private final EventListenerList listeners = new EventListenerList();
    private boolean istAmLaufen = false;

    public MSearchGuiLoad() {
        msFilmeSuchen = new MSFilmeSuchen();
        msFilmeSuchen.addAdListener(new MSListenerFilmeLaden() {
            @Override
            public synchronized void start(MSListenerFilmeLadenEvent event) {
                for (MSListenerFilmeLaden l : listeners.getListeners(MSListenerFilmeLaden.class)) {
                    Platform.runLater(() -> l.start(event));
                }
            }

            @Override
            public synchronized void progress(MSListenerFilmeLadenEvent event) {
                for (MSListenerFilmeLaden l : listeners.getListeners(MSListenerFilmeLaden.class)) {
                    Platform.runLater(() -> l.progress(event));
                }
            }

            @Override
            public synchronized void fertig(MSListenerFilmeLadenEvent event) {
                // Ergebnisliste listeFilme eintragen -> Feierabend!
                Data.listeFilme = msFilmeSuchen.listeFilmeNeu;
                istAmLaufen = false;
                for (MSListenerFilmeLaden l : listeners.getListeners(MSListenerFilmeLaden.class)) {
                    Platform.runLater(() -> l.fertig(event));
                }
                System.gc();
            }
        });
    }

    // #######################################
    // Filme bei den Sendern laden
    // #######################################
    public void filmeBeimSenderSuchen(boolean filmlisteUpdate) {
        // Filme bei allen Sender suchen
        if (!istAmLaufen) {
            // nicht doppelt starten
            istAmLaufen = true;
            MSConfig.updateFilmliste = filmlisteUpdate;
            msFilmeSuchen.filmeBeimSenderLaden(Data.listeFilme);
        }
    }

    public void updateSender(String[] sender) {
        // Filme nur bei EINEM Sender suchen (nur update)
        if (!istAmLaufen) {
            // nicht doppelt starten
            istAmLaufen = true;
            msFilmeSuchen.updateSender(sender, Data.listeFilme);
        }
    }

    // #######################################
    // #######################################
    public static String[] getSenderNamen() {
        return MSFilmeSuchen.getNamenSender();
    }

//    private void undEnde(MSListenerFilmeLadenEvent event) {
//        istAmLaufen = false;
//        for (MSListenerFilmeLaden l : listeners.getListeners(MSListenerFilmeLaden.class)) {
//            Platform.runLater(() -> {
//                l.fertig(event);
//            });
//        }
//        System.gc();
//    }
    // ###########################
    // Listener
    // ###########################
    public void addAdListener(MSListenerFilmeLaden listener) {
        listeners.add(MSListenerFilmeLaden.class, listener);
    }

//    private void notifyStart(MSListenerFilmeLadenEvent event) {
//        for (MSListenerFilmeLaden l : listeners.getListeners(MSListenerFilmeLaden.class)) {
//            Platform.runLater(() -> {
//                l.start(event);
//            });
//        }
//    }
//
//    private void notifyProgress(MSListenerFilmeLadenEvent event) {
//        for (MSListenerFilmeLaden l : listeners.getListeners(MSListenerFilmeLaden.class)) {
//            Platform.runLater(() -> {
//                l.progress(event);
//            });
//        }
//    }
//
//    private void notifyFertig(MSListenerFilmeLadenEvent event) {
//        for (MSListenerFilmeLaden l : listeners.getListeners(MSListenerFilmeLaden.class)) {
//            Platform.runLater(() -> {
//                l.fertig(event);
//            });
//        }
//    }
}
