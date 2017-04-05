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
package de.mediathekview.mlib.filmlisten;

import java.io.*;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipInputStream;

import javax.swing.event.EventListenerList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.Film;
import org.apache.commons.lang3.time.FastDateFormat;
import org.tukaani.xz.XZInputStream;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.ListeFilme;
import de.mediathekview.mlib.filmesuchen.ListenerFilmeLaden;
import de.mediathekview.mlib.filmesuchen.ListenerFilmeLadenEvent;
import de.mediathekview.mlib.tool.InputStreamProgressMonitor;
import de.mediathekview.mlib.tool.Log;
import de.mediathekview.mlib.tool.MVHttpClient;
import de.mediathekview.mlib.tool.ProgressMonitorInputStream;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class FilmlisteLesen {
    private static final int PROGRESS_MAX = 100;
    private static WorkMode workMode = WorkMode.NORMAL; // die Klasse wird an verschiedenen Stellen benutzt, klappt sonst nicht immer, zB. FilmListe zu alt und neu laden
    private final EventListenerList listeners = new EventListenerList();
    private int max = 0;
    private int progress = 0;
    private long milliseconds = 0;

    /**
     * Set the specific work mode for reading film list.
     * In FASTAUTO mode, no film descriptions will be read into memory.
     *
     * @param mode The mode in which to operate when reading film list.
     */
    public static void setWorkMode(WorkMode mode) {
        workMode = mode;
    }

    public void addAdListener(ListenerFilmeLaden listener) {
        listeners.add(ListenerFilmeLaden.class, listener);
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

    public ListeFilme readData(InputStream aInputStream) throws IOException {
        Type filmListType = new TypeToken<List<Film>>(){}.getType();
        Gson gson = new GsonBuilder().registerTypeAdapter(filmListType, new FakeJsonDeserializer()).create();
        try(InputStreamReader inputStreamReader = new InputStreamReader(aInputStream))
        {
            ListeFilme listeFilme = new ListeFilme();
            listeFilme.addAll(gson.fromJson(inputStreamReader, filmListType));
            return listeFilme;
        }
    }

    public static void main(String... args)
    {
        new FilmlisteLesen().processFromFile("/home/nicklas/Entwicklung/git/MLib/src/test/resources/TestFilmlist.json",new ListeFilme());
    }

    /**
     * Read a locally available filmlist.
     *
     * @param aSource     file path as string
     * @param listeFilme the list to read to
     */
    private void processFromFile(String aSource, ListeFilme listeFilme) {
        notifyProgress(aSource, PROGRESS_MAX);
        try (InputStream in = selectDecompressor(aSource, Files.newInputStream(Paths.get(aSource)))){
            listeFilme = readData(in);
        } catch (FileNotFoundException ex) {
            Log.errorLog(894512369, "FilmListe existiert nicht: " + aSource);
            listeFilme.clear();
        } catch (Exception ex) {
            Log.errorLog(945123641, ex, "FilmListe: " + aSource);
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

            if (!source.startsWith("http")) {
                processFromFile(source, listeFilme);
            } else {
                processFromWeb(new URL(source), listeFilme);
            }

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
     * Download a process a filmliste from the web.
     *
     * @param source     source url as string
     * @param listeFilme the list to read to
     */
    private void processFromWeb(URL source, ListeFilme listeFilme) {
        Request.Builder builder = new Request.Builder().url(source);
        builder.addHeader("User-Agent", Config.getUserAgent());

        //our progress monitor callback
        InputStreamProgressMonitor monitor = new InputStreamProgressMonitor() {
            private int oldProgress = 0;

            @Override
            public void progress(long bytesRead, long size) {
                final int iProgress = (int) (bytesRead * 100 / size);
                if (iProgress != oldProgress) {
                    oldProgress = iProgress;
                    notifyProgress(source.toString(), iProgress);
                }
            }
        };

        try (Response response = MVHttpClient.getInstance().getHttpClient().newCall(builder.build()).execute();
             ResponseBody body = response.body()) {
            if (response.isSuccessful()) {
                try (InputStream input = new ProgressMonitorInputStream(body.byteStream(), body.contentLength(), monitor)) {
                    try (InputStream is = selectDecompressor(source.toString(), input))
                    {
                        listeFilme = readData(is);
                    }
                }
            }
        } catch (Exception ex) {
            Log.errorLog(945123641, ex, "FilmListe: " + source);
            listeFilme.clear();
        }
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
            l.progress(new ListenerFilmeLadenEvent(url, "Download", max, progress, 0, false));
        }
    }

    private void notifyFertig(String url, ListeFilme liste) {
        Log.sysLog("Liste Filme gelesen am: " + FastDateFormat.getInstance("dd.MM.yyyy, HH:mm").format(new Date()));
        Log.sysLog("  erstellt am: " + liste.genDate());
        Log.sysLog("  Anzahl Filme: " + liste.size());
        for (ListenerFilmeLaden l : listeners.getListeners(ListenerFilmeLaden.class)) {
            l.fertig(new ListenerFilmeLadenEvent(url, "", max, progress, 0, false));
        }
    }

    public enum WorkMode {

        NORMAL, FASTAUTO
    }
}
