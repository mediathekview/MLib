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

    private String sender = "";
    private String thema = "";

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

    private void writeFormatHeader(JsonGenerator jg, ListeFilme listeFilme) throws IOException {
// Infos zur Filmliste
        jg.writeArrayFieldStart(ListeFilme.FILMLISTE);
        for (int i = 0; i < ListeFilme.MAX_ELEM; ++i) {
            jg.writeString(listeFilme.metaDaten[i]);
        }
        jg.writeEndArray();
    }

    public void filmlisteSchreibenJson(String datei, ListeFilme listeFilme) {
        try {
            Log.sysLog("Filme schreiben (" + listeFilme.size() + " Filme) :");

            Log.sysLog("   --> Start Schreiben nach: " + datei);

            sender = "";
            thema = "";

            //Check if Cache directory exists on OSX
            if (SystemInfo.isMacOSX()) {
                checkOsxCacheDirectory();
            }
            try (FileOutputStream fos = new FileOutputStream(datei);
                 JsonGenerator jg = getJsonGenerator(fos)) {

                jg.writeStartObject();

                writeFormatHeader(jg, listeFilme);
                writeFormatDescription(jg);

                //Filme schreiben
                for (DatenFilm datenFilm : listeFilme) {
                    jg.writeArrayFieldStart(DatenFilm.TAG_JSON_LIST);
                    for (int i = 0; i < DatenFilm.JSON_NAMES.length; ++i) {
                        final int m = DatenFilm.JSON_NAMES[i];
                        switch (m) {
                            case DatenFilm.FILM_URL_RTMP:
                            case DatenFilm.FILM_URL_RTMP_KLEIN:
                            case DatenFilm.FILM_URL_RTMP_HD:
                                jg.writeString("");
                                break;

                            case DatenFilm.FILM_NEU:
                                jg.writeString(Boolean.toString(datenFilm.isNew()));
                                break;

                            case DatenFilm.FILM_SENDER:
                                writeSender(jg, datenFilm);
                                break;

                            case DatenFilm.FILM_THEMA:
                                writeThema(jg, datenFilm);
                                break;

                            case DatenFilm.FILM_BESCHREIBUNG:
                                jg.writeString(datenFilm.getDescription());
                                break;

                            case DatenFilm.FILM_WEBSEITE:
                                jg.writeString(datenFilm.getWebsiteLink());
                                break;

                            case DatenFilm.FILM_ZEIT:
                                writeZeit(jg, datenFilm);
                                break;

                            default:
                                jg.writeString(datenFilm.arr[m]);
                                break;
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

    private void writeSender(JsonGenerator jg, DatenFilm datenFilm) throws IOException {
        if (datenFilm.arr[DatenFilm.FILM_SENDER].equals(sender)) {
            jg.writeString("");
        } else {
            sender = datenFilm.arr[DatenFilm.FILM_SENDER];
            jg.writeString(datenFilm.arr[DatenFilm.FILM_SENDER]);
        }
    }

    private void writeThema(JsonGenerator jg, DatenFilm datenFilm) throws IOException {
        if (datenFilm.arr[DatenFilm.FILM_THEMA].equals(thema)) {
            jg.writeString("");
        } else {
            thema = datenFilm.arr[DatenFilm.FILM_THEMA];
            jg.writeString(datenFilm.arr[DatenFilm.FILM_THEMA]);
        }
    }

    private void writeZeit(JsonGenerator jg, DatenFilm datenFilm) throws IOException {
        String strZeit = datenFilm.arr[DatenFilm.FILM_ZEIT]
                .substring(0, datenFilm.arr[DatenFilm.FILM_ZEIT].length() - 3);
        jg.writeString(strZeit);
    }

    private void writeFormatDescription(JsonGenerator jg) throws IOException {
// Infos der Felder in der Filmliste
        jg.writeArrayFieldStart(ListeFilme.FILMLISTE);
        for (int i = 0; i < DatenFilm.JSON_NAMES.length; ++i) {
            jg.writeString(DatenFilm.COLUMN_NAMES[DatenFilm.JSON_NAMES[i]]);
        }
        jg.writeEndArray();
    }
}
