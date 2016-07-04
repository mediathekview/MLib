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
package mSearch.filmlisten;

import mSearch.tool.ProgressMonitorInputStream;
import mSearch.Config;
import mSearch.Const;
import mSearch.tool.Log;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import mSearch.daten.DatenFilm;
import mSearch.daten.ListeFilme;
import mSearch.filmeSuchen.ListenerFilmeLaden;
import mSearch.filmeSuchen.ListenerFilmeLadenEvent;
import org.tukaani.xz.XZInputStream;

import javax.swing.event.EventListenerList;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipInputStream;
import mSearch.tool.InputStreamProgressMonitor;

public class FilmlisteLesen {

    public enum WorkMode {

        NORMAL, FASTAUTO
    }
    private static WorkMode workMode = WorkMode.NORMAL; // die Klasse wird an verschiedenen Stellen benutzt, klappt sonst nicht immer, zB. FilmListe zu alt und neu laden
    private final EventListenerList listeners = new EventListenerList();
    private int max = 0;
    private int progress = 0;
    private static final int TIMEOUT = 10_000; //10 Sekunden
    private static final int PROGRESS_MAX = 100;
    private long seconds = 0;

    public void addAdListener(ListenerFilmeLaden listener) {
        listeners.add(ListenerFilmeLaden.class, listener);
    }

    /**
     * Set the specific work mode for reading film list.
     * In FASTAUTO mode, no film descriptions will be read into memory.
     *
     * @param mode The mode in which to operate when reading film list.
     */
    public static void setWorkMode(WorkMode mode) {
        workMode = mode;
    }

    private InputStream getInputStreamForLocation(String source) throws IOException,URISyntaxException {
        InputStream in;
        long size = 0;
        final URI uri;
        if (source.startsWith("http")) {
            uri = new URI(source);
            //remote address for internet download
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
            conn.setRequestProperty("User-Agent", Config.getUserAgent());
            if (conn.getResponseCode() < 400) {
                size = conn.getContentLengthLong();
            }
            in = new ProgressMonitorInputStream(conn.getInputStream(), size, new InputStreamProgressMonitor() {
                private int oldProgress = 0;

                @Override
                public void progress(long bytesRead, long size) {
                    final int iProgress = (int) (bytesRead * 100 / size);
                    if (iProgress != oldProgress) {
                        oldProgress = iProgress;
                        notifyProgress(uri.toASCIIString(), "Download", iProgress);
                    }
                }
            });
        } else {
            //local file
            notifyProgress(source, "Download", PROGRESS_MAX);
            in = new FileInputStream(source);
        }

        return in;
    }

    private InputStream selectDecompressor(String source, InputStream in) throws Exception {
        if (source.endsWith(Const.FORMAT_XZ)) {
            in = new XZInputStream(in);
        } else if (source.endsWith(Const.FORMAT_ZIP)) {
            ZipInputStream zipInputStream = new ZipInputStream(in);
            zipInputStream.getNextEntry();
            in = zipInputStream;
        }
        return in;
    }

    public void readFilmListe(String source, final ListeFilme listeFilme, int days) {
        Log.sysLog("Liste Filme lesen von: " + source);
        JsonToken jsonToken;
        String sender = "", thema = "";
        JsonParser jp = null;
        listeFilme.clear();
        this.notifyStart(source, PROGRESS_MAX); // für die Progressanzeige

        if (days > 0) {
            final long maxDays = 1000L * 60L * 60L * 24L * days;
            seconds = new Date().getTime() - maxDays;
        } else {
            seconds = 0;
        }

        try {
            InputStream in = selectDecompressor(source, getInputStreamForLocation(source));
            jp = new JsonFactory().createParser(in);
            if (jp.nextToken() != JsonToken.START_OBJECT) {
                throw new IllegalStateException("Expected data to start with an Object");
            }

            while ((jsonToken = jp.nextToken()) != null) {
                if (jsonToken == JsonToken.END_OBJECT) {
                    break;
                }
                if (jp.isExpectedStartArrayToken()) {
                    for (int k = 0; k < ListeFilme.MAX_ELEM; ++k) {
                        listeFilme.metaDaten[k] = jp.nextTextValue();
                    }
                    break;
                }
            }
            while ((jsonToken = jp.nextToken()) != null) {
                if (jsonToken == JsonToken.END_OBJECT) {
                    break;
                }
                if (jp.isExpectedStartArrayToken()) {
                    // sind nur die Feldbeschreibungen, brauch mer nicht
                    jp.nextToken();
                    break;
                }
            }
            while (!Config.getStop() && (jsonToken = jp.nextToken()) != null) {
                if (jsonToken == JsonToken.END_OBJECT) {
                    break;
                }
                if (jp.isExpectedStartArrayToken()) {
                    DatenFilm datenFilm = new DatenFilm();
                    for (int i = 0; i < DatenFilm.JSON_NAMES.length; ++i) {
                        //if we are in FASTAUTO mode, we don´t need film descriptions.
                        //this should speed up loading on low end devices...
                        if (workMode == WorkMode.FASTAUTO) {
                            if (DatenFilm.JSON_NAMES[i] == DatenFilm.FILM_BESCHREIBUNG
                                    || DatenFilm.JSON_NAMES[i] == DatenFilm.FILM_WEBSEITE
                                    || DatenFilm.JSON_NAMES[i] == DatenFilm.FILM_GEO) {
                                jp.nextToken();
                                continue;
                            }
                        }

                        if (DatenFilm.JSON_NAMES[i] == DatenFilm.FILM_NEU) {
                            final String value = jp.nextTextValue();
                            //This value is unused...
                            //datenFilm.arr[DatenFilm.FILM_NEU_NR] = value;
                            datenFilm.setNew(Boolean.parseBoolean(value));
                        }
                        else {
                            datenFilm.arr[DatenFilm.JSON_NAMES[i]] = jp.nextTextValue();
                        }

                        /// für die Entwicklungszeit
                        if (datenFilm.arr[DatenFilm.JSON_NAMES[i]] == null) {
                            datenFilm.arr[DatenFilm.JSON_NAMES[i]] = "";
                        }
                    }
                    if (datenFilm.arr[DatenFilm.FILM_SENDER].isEmpty()) {
                        datenFilm.arr[DatenFilm.FILM_SENDER] = sender;
                    } else {
                        sender = datenFilm.arr[DatenFilm.FILM_SENDER];
                    }
                    if (datenFilm.arr[DatenFilm.FILM_THEMA].isEmpty()) {
                        datenFilm.arr[DatenFilm.FILM_THEMA] = thema;
                    } else {
                        thema = datenFilm.arr[DatenFilm.FILM_THEMA];
                    }

                    listeFilme.importFilmliste(datenFilm);
                    if (seconds > 0) {
                        // muss "rückwärts" laufen, da das Datum sonst 2x gebaut werden muss
                        // wenns drin bleibt, kann mans noch ändern
                        if (!checkDate(datenFilm)) {
                            listeFilme.remove(datenFilm);
                        }
                    }
                }
            }
            jp.close();
        } catch (FileNotFoundException ex) {
            Log.errorLog(894512369, ex, "FilmListe: " + source);
            listeFilme.clear();
        } catch (Exception ex) {
            Log.errorLog(945123641, ex, "FilmListe: " + source);
            listeFilme.clear();
        } finally {
            try {
                if (jp != null) {
                    jp.close();
                }
            } catch (Exception ignored) {
            }
        }
        if (Config.getStop()) {
            Log.sysLog("--> Abbruch");
            listeFilme.clear();
        }
        notifyFertig(source, listeFilme);
    }

    private boolean checkDate(DatenFilm film) {
        // true wenn der Film angezeigt werden kann!
        try {
            if (film.datumFilm.getTime() != 0) {
                if (film.datumFilm.getTime() < seconds) {
                    return false;
                }
            }
        } catch (Exception ex) {
            Log.errorLog(495623014, ex);
        }
        return true;
    }

    private void notifyStart(String url, int mmax) {
        max = mmax;
        progress = 0;
        for (ListenerFilmeLaden l : listeners.getListeners(ListenerFilmeLaden.class)) {
            l.start(new ListenerFilmeLadenEvent(url, "", max, 0, 0, false));
        }
    }

    private void notifyProgress(String url, String text, int p) {
        progress = p;
        if (progress > max) {
            progress = max;
        }
        for (ListenerFilmeLaden l : listeners.getListeners(ListenerFilmeLaden.class)) {
            l.progress(new ListenerFilmeLadenEvent(url, text, max, progress, 0, false));
        }
    }

    private void notifyFertig(String url, ListeFilme liste) {
        Log.sysLog("Liste Filme gelesen am: " + new SimpleDateFormat("dd.MM.yyyy, HH:mm").format(new Date()));
        Log.sysLog("  erstellt am: " + liste.genDate());
        Log.sysLog("  Anzahl Filme: " + liste.size());
        for (ListenerFilmeLaden l : listeners.getListeners(ListenerFilmeLaden.class)) {
            l.fertig(new ListenerFilmeLadenEvent(url, "", max, progress, 0, false));
        }
    }
}
