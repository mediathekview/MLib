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

public class FilmListWriter {

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

    public void writeFilmList(String datei, ListeFilme listeFilme) {
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

                    writeSender(jg, datenFilm);
                    writeThema(jg, datenFilm);
                    writeTitel(jg, datenFilm);
                    jg.writeString(datenFilm.arr[DatenFilm.FILM_DATUM]);
                    writeZeit(jg, datenFilm);
                    jg.writeString(datenFilm.arr[DatenFilm.FILM_DAUER]);
                    jg.writeString(datenFilm.arr[DatenFilm.FILM_GROESSE]);
                    jg.writeString(datenFilm.getDescription());
                    jg.writeString(datenFilm.arr[DatenFilm.FILM_URL]);
                    jg.writeString(datenFilm.getWebsiteLink());
                    jg.writeString(datenFilm.arr[DatenFilm.FILM_URL_SUBTITLE]);
                    skipEntry(jg); //DatenFilm.FILM_URL_RTMP
                    jg.writeString(datenFilm.arr[DatenFilm.FILM_URL_KLEIN]);
                    skipEntry(jg); //DatenFilm.URL_RTMP_KLEIN
                    jg.writeString(datenFilm.arr[DatenFilm.FILM_URL_HD]);
                    skipEntry(jg); //DatenFilm.FILM_URL_RTMP_HD
                    jg.writeString(datenFilm.arr[DatenFilm.FILM_DATUM_LONG]);
                    jg.writeString(datenFilm.arr[DatenFilm.FILM_URL_HISTORY]);
                    jg.writeString(datenFilm.arr[DatenFilm.FILM_GEO]);
                    jg.writeString(Boolean.toString(datenFilm.isNew()));

                    jg.writeEndArray();
                }
                jg.writeEndObject();
                Log.sysLog("   --> geschrieben!");
            }
        } catch (Exception ex) {
            Log.errorLog(846930145, ex, "nach: " + datei);
        }
    }

    private void skipEntry(JsonGenerator jg) throws IOException {
        jg.writeString("");
    }

    private void writeTitel(JsonGenerator jg, DatenFilm datenFilm) throws IOException {
        jg.writeString(datenFilm.arr[DatenFilm.FILM_TITEL]);
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
        String strZeit = datenFilm.arr[DatenFilm.FILM_ZEIT];
        final int len = strZeit.length();

        if (strZeit.isEmpty() || len < 8)
            jg.writeString("");
        else {
            strZeit = strZeit.substring(0, len - 3);
            jg.writeString(strZeit);
        }
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
