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

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import etm.core.configuration.EtmManager;
import etm.core.monitor.EtmPoint;
import mSearch.Const;
import mSearch.daten.DatenFilm;
import mSearch.daten.ListeFilme;
import mSearch.tool.Log;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WriteFilmlistJson {

    private final JsonFactory jsonF = new JsonFactory();

    public void fastChannelCopy(final ReadableByteChannel src, final WritableByteChannel dest) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
        while (src.read(buffer) != -1) {
            buffer.flip();
            dest.write(buffer);
            buffer.compact();
        }

        buffer.flip();

        while (buffer.hasRemaining()) {
            dest.write(buffer);
        }
    }

    private JsonGenerator getJsonGenerator(OutputStream os) throws IOException {
        JsonGenerator jg = jsonF.createGenerator(os, JsonEncoding.UTF8);
        //jg.useDefaultPrettyPrinter(); // enable indentation just to make debug/testing easier

        return jg;
    }

    /**
     * Write film data and compress with LZMA2.
     *
     * @param datei      file path
     * @param listeFilme film data
     */
    public void filmlisteSchreibenJsonCompressed(String datei, ListeFilme listeFilme) {
        filmlisteSchreibenJson(datei, listeFilme);

        //remove .xz from fimlist file
        try {
            Log.sysLog("Komprimiere Datei: " + datei);
            if (datei.endsWith(Const.FORMAT_XZ)) {
                Path xz = testNativeXz();
                xz = null; //DEBUG
                if (xz != null) {
                    final String datei_ohne_xz = datei.substring(0, datei.length() - ".xz".length());
                    //native compression here
                    Process p = new ProcessBuilder(xz.toString(), "-9", datei_ohne_xz).start();
                    p.waitFor();
                } else
                    compressFile(datei);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.sysLog("Komprimieren fehlgeschlagen");
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public void filmlisteSchreibenJson(String datei, ListeFilme listeFilme) {
        EtmPoint performancePoint = EtmManager.getEtmMonitor().createPoint("WriteFilmlistJson.filmlisteSchreibenJson");

        try {
            Log.sysLog("Filme schreiben (" + listeFilme.size() + " Filme) :");

            Log.sysLog("   --> Start Schreiben nach: " + datei);
            String sender = "", thema = "";

            try (FileOutputStream fos = new FileOutputStream(datei);
                 JsonGenerator jg = getJsonGenerator(fos)) {

                jg.writeStartObject();
                // Infos zur Filmliste
                jg.writeArrayFieldStart(ListeFilme.FILMLISTE);
                for (int i = 0; i < ListeFilme.MAX_ELEM; ++i) {
                    jg.writeString(listeFilme.metaDaten[i]);
                }
                jg.writeEndArray();
                // Infos der Felder in der Filmliste
                jg.writeArrayFieldStart(ListeFilme.FILMLISTE);
                for (int i = 0; i < DatenFilm.JSON_NAMES.length; ++i) {
                    jg.writeString(DatenFilm.COLUMN_NAMES[DatenFilm.JSON_NAMES[i]]);
                }
                jg.writeEndArray();
                //Filme schreiben
                for (DatenFilm datenFilm : listeFilme) {
                    datenFilm.arr[DatenFilm.FILM_NEU] = Boolean.toString(datenFilm.isNew()); // damit wirs beim nächsten Programmstart noch wissen

                    jg.writeArrayFieldStart(DatenFilm.TAG_JSON_LIST);
                    for (int i = 0; i < DatenFilm.JSON_NAMES.length; ++i) {
                        int m = DatenFilm.JSON_NAMES[i];
                        if (m == DatenFilm.FILM_SENDER) {
                            if (datenFilm.arr[m].equals(sender)) {
                                jg.writeString("");
                            } else {
                                sender = datenFilm.arr[m];
                                jg.writeString(datenFilm.arr[m]);
                            }
                        } else if (m == DatenFilm.FILM_THEMA) {
                            if (datenFilm.arr[m].equals(thema)) {
                                jg.writeString("");
                            } else {
                                thema = datenFilm.arr[m];
                                jg.writeString(datenFilm.arr[m]);
                            }
                        } else {
                            jg.writeString(datenFilm.arr[m]);
                        }
                    }
                    jg.writeEndArray();
                }
                jg.writeEndObject();
                Log.sysLog("   --> geschrieben!");
            }
        } catch (Exception ex) {
            Log.errorLog(846930145, ex, "nach: " + datei);
        }
        performancePoint.collect();
    }

    private Path testNativeXz() {
        final String[] paths = {"/usr/bin/xz", "/opt/local/bin/xz", "/usr/local/bin/xz"};

        Path xz = null;

        for (String path : paths) {
            System.out.println("Path: " + path);
            xz = Paths.get(path);
            if (Files.isExecutable(xz)) {
                System.out.println("FOUND...");
                break;
            }
        }

        return xz;
    }

    private void compressFile(String datei) throws IOException {
        EtmPoint compress = EtmManager.getEtmMonitor().createPoint("WriteFilmlistJson.compressFile");

        Path tempFile = Files.createTempFile("prefix", "suffix");
        try (InputStream input = new FileInputStream(datei);
             FileOutputStream fos = new FileOutputStream(tempFile.toString());
             final OutputStream output = new XZOutputStream(fos, new LZMA2Options());
             final ReadableByteChannel inputChannel = Channels.newChannel(input);
             final WritableByteChannel outputChannel = Channels.newChannel(output)) {

            fastChannelCopy(inputChannel, outputChannel);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        compress.collect();
    }
}
