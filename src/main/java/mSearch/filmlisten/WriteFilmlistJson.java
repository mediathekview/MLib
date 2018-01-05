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
import com.jidesoft.utils.SystemInfo;
import mSearch.daten.DatenFilm;
import mSearch.daten.ListeFilme;
import mSearch.tool.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WriteFilmlistJson {

    protected JsonGenerator getJsonGenerator(OutputStream os) throws IOException {
        JsonFactory jsonF = new JsonFactory();
        return jsonF.createGenerator(os, JsonEncoding.UTF8);
    }

    private void checkOsxCacheDirectory() {
        final Path filePath = Paths.get(System.getProperty("user.home") + File.separator + "Library/Caches/MediathekView");
        if (Files.notExists(filePath)) {
            try {
                Files.createDirectories(filePath);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public void filmlisteSchreibenJson(String datei, ListeFilme listeFilme) {
        try {
            Log.sysLog("Filme schreiben (" + listeFilme.size() + " Filme) :");

            Log.sysLog("   --> Start Schreiben nach: " + datei);
            String sender = "", thema = "";

            //Check if Cache directory exists on OSX
            if (SystemInfo.isMacOSX()) {
                checkOsxCacheDirectory();
            }
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
                        } else if (m == DatenFilm.FILM_BESCHREIBUNG) {
                            final String desc = datenFilm.getDescription();
                            jg.writeString(desc);
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
    }

    /*private void compressFile(String inputName, String outputName) {
        try (InputStream input = new FileInputStream(inputName);
             FileOutputStream fos = new FileOutputStream(outputName);
             final OutputStream output = new XZOutputStream(fos, new LZMA2Options());
             final ReadableByteChannel inputChannel = Channels.newChannel(input);
             final WritableByteChannel outputChannel = Channels.newChannel(output)) {

            Functions.fastChannelCopy(inputChannel, outputChannel);
        } catch (IOException ex) {
            Log.errorLog(987654321, ex);
        }
    }*/
}
