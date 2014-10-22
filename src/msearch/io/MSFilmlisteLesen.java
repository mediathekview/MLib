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
package msearch.io;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.zip.ZipInputStream;
import javax.swing.event.EventListenerList;
import msearch.daten.DatenFilm;
import msearch.daten.ListeFilme;
import msearch.daten.MSConfig;
import msearch.filmeSuchen.MSListenerFilmeLaden;
import msearch.filmeSuchen.MSListenerFilmeLadenEvent;
import msearch.tool.DatumZeit;
import msearch.tool.MSConst;
import msearch.tool.MSLog;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.tukaani.xz.XZInputStream;

public class MSFilmlisteLesen {

    private final EventListenerList listeners = new EventListenerList();
    private int max = 0;
    private int progress = 0;
    private static final int TIMEOUT = 10_000; //10 Sekunden
    private static final int PROGRESS_MAX = 100;

    public void addAdListener(MSListenerFilmeLaden listener) {
        listeners.add(MSListenerFilmeLaden.class, listener);
    }

    private InputStream getInputStreamForLocation(String source) throws Exception {
        InputStream in;
        long size = 0;
        final URI uri;
        if (source.startsWith("http")) {
            uri = new URI(source);
            //remote address for internet download
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
            conn.setRequestProperty("User-Agent", MSConfig.getUserAgent());
            if (conn.getResponseCode() < 400) {
                size = conn.getContentLengthLong();
            }
            in = new SizeInputStream(conn.getInputStream(), size, uri.toASCIIString());
        } else {
            //local file
            notifyProgress(source, "Download", PROGRESS_MAX);
            in = new FileInputStream(source);
        }

        return in;
    }

    private InputStream selectDecompressor(String source, InputStream in) throws Exception {
        if (source.endsWith(MSConst.FORMAT_XZ)) {
            in = new XZInputStream(in);
        } else if (source.endsWith(MSConst.FORMAT_BZ2)) {
            in = new BZip2CompressorInputStream(in);
        } else if (source.endsWith(MSConst.FORMAT_ZIP)) {
            ZipInputStream zipInputStream = new ZipInputStream(in);
            zipInputStream.getNextEntry();
            in = zipInputStream;
        }
        return in;
    }

    public void readFilmListe(String source, final ListeFilme listeFilme) {
        JsonToken jsonToken;
        String sender = "", thema = "";
        listeFilme.clear();
        this.notifyStart(source, PROGRESS_MAX); // für die Progressanzeige
        try {
            InputStream in = selectDecompressor(source, getInputStreamForLocation(source));
            JsonParser jp = new JsonFactory().createParser(in);
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
            while ((jsonToken = jp.nextToken()) != null) {
                if (jsonToken == JsonToken.END_OBJECT) {
                    break;
                }
                if (jp.isExpectedStartArrayToken()) {
                    DatenFilm datenFilm = new DatenFilm();
                    for (int i = 0; i < DatenFilm.COLUMN_NAMES_JSON.length; ++i) {
                        datenFilm.arr[DatenFilm.COLUMN_NAMES_JSON[i]] = jp.nextTextValue();
                        /// für die Entwicklungszeit
                        if (datenFilm.arr[DatenFilm.COLUMN_NAMES_JSON[i]] == null) {
                            datenFilm.arr[DatenFilm.COLUMN_NAMES_JSON[i]] = "";
                        }
                    }
                    if (datenFilm.arr[DatenFilm.FILM_SENDER_NR].isEmpty()) {
                        datenFilm.arr[DatenFilm.FILM_SENDER_NR] = sender;
                    } else {
                        sender = datenFilm.arr[DatenFilm.FILM_SENDER_NR];
                    }
                    if (datenFilm.arr[DatenFilm.FILM_THEMA_NR].isEmpty()) {
                        datenFilm.arr[DatenFilm.FILM_THEMA_NR] = thema;
                    } else {
                        thema = datenFilm.arr[DatenFilm.FILM_THEMA_NR];
                    }
                    listeFilme.importFilmliste(datenFilm);
                }
            }
        } catch (Exception ex) {
            MSLog.fehlerMeldung(945123641, MSLog.FEHLER_ART_PROG, "MSearchIoXmlFilmlisteLesen.readFilmListe: " + source, ex);
            listeFilme.clear();
        }
        notifyFertig(source, listeFilme);
    }

    private void notifyStart(String url, int mmax) {
        max = mmax;
        progress = 0;
        for (MSListenerFilmeLaden l : listeners.getListeners(MSListenerFilmeLaden.class)) {
            l.start(new MSListenerFilmeLadenEvent(url, "", max, 0, 0, false));
        }
    }

    private void notifyProgress(String url, String text, int p) {
        progress = p;
        if (progress > max) {
            progress = max;
        }
        for (MSListenerFilmeLaden l : listeners.getListeners(MSListenerFilmeLaden.class)) {
            l.progress(new MSListenerFilmeLadenEvent(url, text, max, progress, 0, false));
        }
    }

    private void notifyFertig(String url, ListeFilme liste) {
        MSLog.systemMeldung("Liste Filme gelesen: " + DatumZeit.getHeute_dd_MM_yyyy() + " " + DatumZeit.getJetzt_HH_MM_SS());
        MSLog.systemMeldung("Anzahl Filme: " + liste.size());
        for (MSListenerFilmeLaden l : listeners.getListeners(MSListenerFilmeLaden.class)) {
            l.fertig(new MSListenerFilmeLadenEvent(url, "", max, progress, 0, false));
        }
    }

    @SuppressWarnings("NullableProblems")
    private class SizeInputStream extends InputStream {

        // The number of bytes that can be read from the InputStream
        private final long size;
        // The number of bytes that have been read from the InputStream
        private long bytesRead = 0;

        private InputStream in = null;
        private long progress, oldProgress;
        private final String from;

        public SizeInputStream(InputStream in, long size, String from) {
            this.in = in;
            this.size = size;
            this.from = from;
        }

        @Override
        public void close() throws IOException {
            in.close();
        }

        @Override
        public int available() throws IOException {
            return in.available();
        }

        @Override
        public int read() throws IOException {
            int b = in.read();
            if (b != -1) {
                bytesRead++;
                progress();
            }
            return b;
        }

        @Override
        public int read(byte[] b) throws IOException {
            int read = in.read(b);
            bytesRead += read;
            progress();
            return read;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int read = in.read(b, off, len);
            bytesRead += read;
            progress();
            return read;
        }

        private void progress() {
            if (size > 0) {
                // macht nur dann Sinn
                progress = bytesRead * 100 / size;
                if (progress != oldProgress) {
                    oldProgress = progress;
                    notifyProgress(from, "Download", (int) progress);
                }
            }
        }
    }

}
