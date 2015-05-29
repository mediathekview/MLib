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
package msearch.filmlisten;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ListIterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import msearch.daten.DatenFilm;
import msearch.daten.ListeFilme;
import msearch.tool.MSConst;
import msearch.tool.MSLog;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

public class WriteFilmlistJson {

    public void filmlisteSchreibenJson(String datei, ListeFilme listeFilme) {
        ZipOutputStream zipOutputStream = null;
        BZip2CompressorOutputStream bZipOutputStream = null;
        XZOutputStream xZOutputStream = null;
        JsonGenerator jg = null;
        try {
            MSLog.systemMeldung("Filme schreiben (" + listeFilme.size() + " Filme) :");
            File file = new File(datei);
            File dir = new File(file.getParent());
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    MSLog.fehlerMeldung(915236478, "Kann den Pfad nicht anlegen: " + dir.toString());
                }
            }
            MSLog.systemMeldung("   --> Start Schreiben nach: " + datei);
            String sender = "", thema = "";
            JsonFactory jsonF = new JsonFactory();
            if (datei.endsWith(MSConst.FORMAT_XZ)) {
                LZMA2Options options = new LZMA2Options();
                xZOutputStream = new XZOutputStream(new FileOutputStream(file), options);
                jg = jsonF.createGenerator(xZOutputStream);
            } else if (datei.endsWith(MSConst.FORMAT_BZ2)) {
                bZipOutputStream = new BZip2CompressorOutputStream(new FileOutputStream(file), 9 /*Blocksize: 1 - 9*/);
                jg = jsonF.createGenerator(bZipOutputStream, JsonEncoding.UTF8);
            } else if (datei.endsWith(MSConst.FORMAT_ZIP)) {
                zipOutputStream = new ZipOutputStream(new FileOutputStream(file));
                ZipEntry entry = new ZipEntry(MSConst.XML_DATEI_FILME);
                zipOutputStream.putNextEntry(entry);
                jg = jsonF.createGenerator(zipOutputStream, JsonEncoding.UTF8);
            } else {
                jg = jsonF.createGenerator(new File(datei), JsonEncoding.UTF8);
            }
            jg.useDefaultPrettyPrinter(); // enable indentation just to make debug/testing easier
            jg.writeStartObject();
            // Infos zur Filmliste
            jg.writeArrayFieldStart(ListeFilme.FILMLISTE);
            for (int i = 0; i < ListeFilme.MAX_ELEM; ++i) {
                jg.writeString(listeFilme.metaDaten[i]);
            }
            jg.writeEndArray();
            // Infos der Felder in der Filmliste
            jg.writeArrayFieldStart(ListeFilme.FILMLISTE);
            for (int i = 0; i < DatenFilm.COLUMN_NAMES_JSON.length; ++i) {
                jg.writeString(DatenFilm.COLUMN_NAMES[DatenFilm.COLUMN_NAMES_JSON[i]]);
            }
            jg.writeEndArray();
            //Filme schreiben
            ListIterator<DatenFilm> iterator;
            DatenFilm datenFilm;
            iterator = listeFilme.listIterator();
            while (iterator.hasNext()) {
                datenFilm = iterator.next();
                jg.writeArrayFieldStart(DatenFilm.FILME_);
                for (int i = 0; i < DatenFilm.COLUMN_NAMES_JSON.length; ++i) {
                    int m = DatenFilm.COLUMN_NAMES_JSON[i];
                    if (m == DatenFilm.FILM_SENDER_NR) {
                        if (datenFilm.arr[m].equals(sender)) {
                            jg.writeString("");
                        } else {
                            sender = datenFilm.arr[m];
                            jg.writeString(datenFilm.arr[m]);
                        }
                    } else if (m == DatenFilm.FILM_THEMA_NR) {
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
            MSLog.systemMeldung("   --> geschrieben!");
        } catch (Exception ex) {
            MSLog.fehlerMeldung(846930145, ex, "nach: " + datei);
        } finally {
            try {
                if (jg != null) {
                    jg.close();
                }
                // die werden nicht immer korrekt geschlossen !??!
                if (zipOutputStream != null) {
                    zipOutputStream.close();
                }
                if (bZipOutputStream != null) {
                    bZipOutputStream.close();
                }
                if (xZOutputStream != null) {
                    xZOutputStream.close();
                }
            } catch (Exception e) {
                MSLog.fehlerMeldung(732101201, e, "close stream: " + datei);
            }
        }
    }

}
