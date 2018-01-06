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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import mSearch.Config;
import mSearch.Const;
import mSearch.daten.DatenFilm;
import mSearch.daten.ListeFilme;
import mSearch.filmeSuchen.ListenerFilmeLaden;
import mSearch.filmeSuchen.ListenerFilmeLadenEvent;
import mSearch.tool.InputStreamProgressMonitor;
import mSearch.tool.Log;
import mSearch.tool.MVHttpClient;
import mSearch.tool.ProgressMonitorInputStream;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.time.FastDateFormat;
import org.tukaani.xz.XZInputStream;

import javax.swing.event.EventListenerList;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class FilmListReader implements AutoCloseable {
    private static final int PROGRESS_MAX = 100;
    /**
     * Memory limit for the xz decompressor. No limit by default.
     */
    protected int DECOMPRESSOR_MEMORY_LIMIT = -1;
    private final EventListenerList listeners = new EventListenerList();
    private final ListenerFilmeLadenEvent progressEvent = new ListenerFilmeLadenEvent("", "Download", 0, 0, 0, false);
    private int max = 0;
    private int progress = 0;
    private long milliseconds = 0;
    private String sender = "";
    private String thema = "";

    public void addAdListener(ListenerFilmeLaden listener) {
        listeners.add(ListenerFilmeLaden.class, listener);
    }

    /**
     * Remove all registered listeners when we do not need them anymore.
     */
    private void removeRegisteredListeners() {
        ListenerFilmeLaden list[] = listeners.getListeners(ListenerFilmeLaden.class);
        for (ListenerFilmeLaden lst : list) {
            listeners.remove(ListenerFilmeLaden.class, lst);
        }
    }

    private InputStream selectDecompressor(String source, InputStream in) throws Exception {
        final InputStream is;

        switch (source.substring(source.lastIndexOf('.'))) {
            case Const.FORMAT_XZ:
                is = new XZInputStream(in, DECOMPRESSOR_MEMORY_LIMIT, false);
                break;

            case ".json":
                is = in;
                break;

            default:
                throw new UnsupportedOperationException("Unbekanntes Dateiformat entdeckt.");
        }

        return is;
    }

    private void parseNeu(JsonParser jp, DatenFilm datenFilm) throws IOException {
        final String value = jp.nextTextValue();
        datenFilm.setNew(Boolean.parseBoolean(value));

        datenFilm.arr[DatenFilm.FILM_NEU] = null;
    }

    protected void parseWebsiteLink(JsonParser jp, DatenFilm datenFilm) throws IOException {
        final String value = jp.nextTextValue();
        if (value != null && !value.isEmpty()) {
            datenFilm.setWebsiteLink(value);
        }

        datenFilm.arr[DatenFilm.FILM_WEBSEITE] = null;
    }

    private void parseDescription(JsonParser jp, DatenFilm datenFilm) throws IOException {
        final String value = jp.nextTextValue();
        if (value != null && !value.isEmpty())
            datenFilm.setDescription(value);

        datenFilm.arr[DatenFilm.FILM_BESCHREIBUNG] = null;
    }

    protected void parseGeo(JsonParser jp, DatenFilm datenFilm) throws IOException {
        datenFilm.arr[DatenFilm.FILM_GEO] = checkedString(jp);
    }

    private void parseSender(JsonParser jp, DatenFilm datenFilm) throws IOException {
        String value = checkedString(jp);
        if (value.isEmpty())
            datenFilm.arr[DatenFilm.FILM_SENDER] = sender;
        else {
            datenFilm.arr[DatenFilm.FILM_SENDER] = value;
            //store for future reads
            sender = datenFilm.arr[DatenFilm.FILM_SENDER];
        }
    }

    private void parseThema(JsonParser jp, DatenFilm datenFilm) throws IOException {
        String value = checkedString(jp);
        if (value.isEmpty())
            datenFilm.arr[DatenFilm.FILM_THEMA] = thema;
        else {
            datenFilm.arr[DatenFilm.FILM_THEMA] = value;
            thema = datenFilm.arr[DatenFilm.FILM_THEMA];
        }
    }

    private String checkedString(JsonParser jp) throws IOException {
        String value = jp.nextTextValue();
        //only check for null and replace for the default rows...
        if (value == null)
            value = "";

        return value;
    }

    private void parseMetaData(JsonParser jp, ListeFilme listeFilme) throws IOException {
        JsonToken jsonToken;
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
    }

    private void skipFieldDescriptions(JsonParser jp) throws IOException {
        JsonToken jsonToken;
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
    }

    private void parseDefault(JsonParser jp, DatenFilm datenFilm, final int TAG) throws IOException {
        datenFilm.arr[TAG] = checkedString(jp);
    }

    private void readData(JsonParser jp, ListeFilme listeFilme) throws IOException {
        JsonToken jsonToken;

        if (jp.nextToken() != JsonToken.START_OBJECT) {
            throw new IllegalStateException("Expected data to start with an Object");
        }

        parseMetaData(jp, listeFilme);

        skipFieldDescriptions(jp);

        while (!Config.getStop() && (jsonToken = jp.nextToken()) != null) {
            if (jsonToken == JsonToken.END_OBJECT) {
                break;
            }
            if (jp.isExpectedStartArrayToken()) {
                DatenFilm datenFilm = new DatenFilm();

                parseSender(jp, datenFilm);
                parseThema(jp, datenFilm);
                parseDefault(jp, datenFilm, DatenFilm.FILM_TITEL);
                parseDefault(jp, datenFilm, DatenFilm.FILM_DATUM);
                parseDefault(jp, datenFilm, DatenFilm.FILM_ZEIT);
                parseDefault(jp, datenFilm, DatenFilm.FILM_DAUER);
                parseDefault(jp, datenFilm, DatenFilm.FILM_GROESSE);
                parseDescription(jp, datenFilm);
                parseDefault(jp, datenFilm, DatenFilm.FILM_URL);
                parseWebsiteLink(jp, datenFilm);
                parseDefault(jp, datenFilm, DatenFilm.FILM_URL_SUBTITLE);
                parseDefault(jp, datenFilm, DatenFilm.FILM_URL_RTMP);
                parseDefault(jp, datenFilm, DatenFilm.FILM_URL_KLEIN);
                parseDefault(jp, datenFilm, DatenFilm.FILM_URL_RTMP_KLEIN);
                parseDefault(jp, datenFilm, DatenFilm.FILM_URL_HD);
                parseDefault(jp, datenFilm, DatenFilm.FILM_URL_RTMP_HD);
                parseDefault(jp, datenFilm, DatenFilm.FILM_DATUM_LONG);
                parseDefault(jp, datenFilm, DatenFilm.FILM_URL_HISTORY);
                parseGeo(jp, datenFilm);
                parseNeu(jp, datenFilm);

                listeFilme.importFilmliste(datenFilm);

                if (milliseconds > 0) {
                    // muss "rückwärts" laufen, da das Datum sonst 2x gebaut werden muss
                    // wenns drin bleibt, kann mans noch ändern
                    if (!checkDate(datenFilm)) {
                        listeFilme.remove(datenFilm);
                    }
                }
            }
        }

        //finally commit all data to database cache...
        DatenFilm.Database.commitAllChanges();
    }

    /**
     * Read a locally available filmlist.
     *
     * @param source     file path as string
     * @param listeFilme the list to read to
     */
    private void processFromFile(String source, ListeFilme listeFilme) {
        notifyProgress(source, PROGRESS_MAX);
        try (FileInputStream fis = new FileInputStream(source);
             InputStream in = selectDecompressor(source, fis);
             JsonParser jp = new JsonFactory().createParser(in)) {
            readData(jp, listeFilme);
        } catch (FileNotFoundException ex) {
            Log.errorLog(894512369, "FilmListe existiert nicht: " + source);
            listeFilme.clear();
        } catch (Exception ex) {
            Log.errorLog(945123641, ex, "FilmListe: " + source);
            listeFilme.clear();
        }
    }

    private void checkDays(long days) {
        if (days > 0) {
            milliseconds = System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(days, TimeUnit.DAYS);
        } else {
            milliseconds = 0;
        }
    }

    public void readFilmListe(String source, final ListeFilme listeFilme, int days) {
        try {
            Log.sysLog("Liste Filme lesen von: " + source);
            listeFilme.clear();
            this.notifyStart(source, PROGRESS_MAX); // für die Progressanzeige

            checkDays(days);

            if (source.startsWith("http")) {
                final URL sourceUrl = new URL(source);
                processFromWeb(sourceUrl, listeFilme);
            } else
                processFromFile(source, listeFilme);

            if (Config.getStop()) {
                Log.sysLog("--> Abbruch");
                listeFilme.clear();
            }
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }

        notifyFertig(source, listeFilme);
    }

    /**
     * Download and process a filmliste from the web.
     *
     * @param source     source url as string
     * @param listeFilme the list to read to
     */
    private void processFromWeb(URL source, ListeFilme listeFilme) {
        Request.Builder builder = new Request.Builder().url(source);
        builder.addHeader("User-Agent", Config.getUserAgent());

        //our progress monitor callback
        InputStreamProgressMonitor monitor = new InputStreamProgressMonitor() {
            private final String sourceString = source.toString();
            private int oldProgress = 0;

            @Override
            public void progress(final long bytesRead, final long size) {
                final int iProgress = (int) (bytesRead * 100 / size);
                if (iProgress != oldProgress) {
                    oldProgress = iProgress;
                    notifyProgress(sourceString, iProgress);
                }
            }
        };

        try (Response response = MVHttpClient.getInstance().getHttpClient().newCall(builder.build()).execute()) {
            if (response.isSuccessful()) {
                try (ResponseBody body = response.body();
                     InputStream input = new ProgressMonitorInputStream(body.byteStream(), body.contentLength(), monitor);
                     InputStream is = new XZInputStream(input);
                     JsonParser jp = new JsonFactory().createParser(is)) {
                    readData(jp, listeFilme);
                }
            }
        } catch (Exception ex) {
            Log.errorLog(945123641, ex, "FilmListe: " + source);
            listeFilme.clear();
        }
    }

    private boolean checkDate(DatenFilm film) {
        // true wenn der Film angezeigt werden kann!
        try {
            if (film.datumFilm.getTime() != 0) {
                if (film.datumFilm.getTime() < milliseconds) {
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

    private void notifyProgress(String url, int iProgress) {
        progress = iProgress;
        if (progress > max) {
            progress = max;
        }
        for (ListenerFilmeLaden l : listeners.getListeners(ListenerFilmeLaden.class)) {
            progressEvent.senderUrl = url;
            progressEvent.progress = progress;
            progressEvent.max = max;
            l.progress(progressEvent);
        }
    }

    private void notifyFertig(String url, ListeFilme liste) {
        Log.sysLog("Liste Filme gelesen am: " + FastDateFormat.getInstance("dd.MM.yyyy, HH:mm").format(new Date()));
        Log.sysLog("  erstellt am: " + liste.genDate());
        Log.sysLog("  Anzahl Filme: " + liste.size());
        for (ListenerFilmeLaden l : listeners.getListeners(ListenerFilmeLaden.class)) {
            progressEvent.senderUrl = url;
            progressEvent.text = "";
            progressEvent.max = max;
            progressEvent.progress = progress;
            l.fertig(progressEvent);
        }
    }

    @Override
    public void close() {
        removeRegisteredListeners();
    }
}
