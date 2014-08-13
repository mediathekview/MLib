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
package msearch.gui;

import java.util.HashSet;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;
import msearch.daten.DatenFilm;
import msearch.daten.ListeFilme;
import msearch.daten.MSConfig;
import msearch.filmeSuchen.MSFilmeSuchen;
import msearch.filmeSuchen.MSListenerFilmeLaden;
import msearch.filmeSuchen.MSListenerFilmeLadenEvent;

public class FilmeLaden {

    private final HashSet<String> hashSet = new HashSet<>();
    private final ListeFilme diffListe = new ListeFilme();
    private ListeFilme listeFilme = null;
    boolean debug = true;

    private enum ListenerMelden {

        START, PROGRESS, FINISHED
    }
    // private
    private MSFilmeSuchen mSearchFilmeSuchen;
    private final EventListenerList listeners = new EventListenerList();
    private boolean istAmLaufen = false;

    public FilmeLaden(ListeFilme listeFilme_) {
        this.listeFilme = listeFilme_;
        mSearchFilmeSuchen = new MSFilmeSuchen();
        mSearchFilmeSuchen.addAdListener(new MSListenerFilmeLaden() {
            @Override
            public synchronized void start(MSListenerFilmeLadenEvent event) {
                notifyStart(event);
            }

            @Override
            public synchronized void progress(MSListenerFilmeLadenEvent event) {
                notifyProgress(event);
            }

            @Override
            public synchronized void fertig(MSListenerFilmeLadenEvent event) {
                // Ergebnisliste listeFilme eintragen -> Feierabend!
                listeFilme = mSearchFilmeSuchen.listeFilmeNeu;
                undEnde(event);
            }
        });
    }

    // #######################################
    // Filme bei den Sendern laden
    // #######################################
    public void filmeBeimSenderSuchen(boolean senderAllesLaden, boolean filmlisteUpdate) {
        // Filme bei allen Sender suchen
        if (!istAmLaufen) {
            // nicht doppelt starten
            istAmLaufen = true;
            hashSet.clear();
            listeFilme.neueFilme = false;

            fillHash(listeFilme);
            MSConfig.senderAllesLaden = senderAllesLaden;
            MSConfig.updateFilmliste = filmlisteUpdate;
            MSConfig.debug = debug;
            mSearchFilmeSuchen.filmeBeimSenderLaden(listeFilme);
        }
    }

    public void updateSender(String[] sender) {
        // Filme nur bei EINEM Sender suchen (nur update)
        if (!istAmLaufen) {
            // nicht doppelt starten
            istAmLaufen = true;
            hashSet.clear();
            listeFilme.neueFilme = false;

            fillHash(listeFilme);
            MSConfig.debug = debug;
            mSearchFilmeSuchen.updateSender(sender, listeFilme);
        }
    }

    // #######################################
    // #######################################
    public synchronized void setStop(boolean set) {
        MSConfig.setStop(set);
    }

    public String[] getSenderNamen() {
        return mSearchFilmeSuchen.getNamenSender();
    }

    private void undEnde(MSListenerFilmeLadenEvent event) {
        // Abos eintragen in der gesamten Liste vor Blacklist da das nur beim Ändern der Filmliste oder
        // beim Ändern von Abos gemacht wird

        // wenn nur ein Update
        if (!diffListe.isEmpty()) {
            listeFilme.updateListe(diffListe, true/* Vergleich über Index, sonst nur URL */, true /*ersetzen*/);
            listeFilme.metaDaten = diffListe.metaDaten;
            listeFilme.sort(); // jetzt sollte alles passen
            diffListe.clear();
        }

        searchHash(listeFilme);
        listeFilme.themenLaden();

        istAmLaufen = false;
        if (event.fehler) {
        } else {
        }
        notifyFertig(event);
        System.gc();
    }

    private void fillHash(ListeFilme listeFilme) {
        for (DatenFilm film : listeFilme) {
            hashSet.add(film.getUrlHistory());
        }
    }

    private void searchHash(ListeFilme listeFilme) {
        listeFilme.neueFilme = false;
        for (DatenFilm film : listeFilme) {
            if (!hashSet.contains(film.getUrlHistory())) {
                film.neuerFilm = true;
                listeFilme.neueFilme = true;
            } else {
                film.neuerFilm = false;
            }
        }
        hashSet.clear();
    }

    // ###########################
    // Listener
    // ###########################
    public void addAdListener(MSListenerFilmeLaden listener) {
        listeners.add(MSListenerFilmeLaden.class, listener);
    }

    private void notifyStart(MSListenerFilmeLadenEvent event) {
        for (MSListenerFilmeLaden l : listeners.getListeners(MSListenerFilmeLaden.class)) {
            run_(new Start(l, event, ListenerMelden.START));
        }
    }

    private void notifyProgress(MSListenerFilmeLadenEvent event) {
        for (MSListenerFilmeLaden l : listeners.getListeners(MSListenerFilmeLaden.class)) {
            run_(new Start(l, event, ListenerMelden.PROGRESS));
        }
    }

    private void notifyFertig(MSListenerFilmeLadenEvent event) {
        for (MSListenerFilmeLaden l : listeners.getListeners(MSListenerFilmeLaden.class)) {
            run_(new Start(l, event, ListenerMelden.FINISHED));
        }
    }

    private class Start implements Runnable {

        private final MSListenerFilmeLaden listenerFilmeLaden;
        private final MSListenerFilmeLadenEvent event;
        private final ListenerMelden listenerMelden;

        public Start(MSListenerFilmeLaden llistenerFilmeLaden, MSListenerFilmeLadenEvent eevent, ListenerMelden lliListenerMelden) {
            listenerFilmeLaden = llistenerFilmeLaden;
            event = eevent;
            listenerMelden = lliListenerMelden;
        }

        @Override
        public synchronized void run() {
            switch (listenerMelden) {
                case START:
                    listenerFilmeLaden.start(event);
                    break;
                case PROGRESS:
                    listenerFilmeLaden.progress(event);
                    break;
                case FINISHED:
                    listenerFilmeLaden.fertig(event);
                    break;
            }
        }
    }

    private void run_(Runnable r) {
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                // entweder hier
                r.run();
            } else {
                SwingUtilities.invokeLater(r);
            }
        } catch (Exception ex) {

        }
    }
}
