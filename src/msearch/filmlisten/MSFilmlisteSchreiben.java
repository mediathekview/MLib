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
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ListIterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import msearch.daten.DatenFilm;
import msearch.daten.ListeFilme;
import msearch.filmeSuchen.sender.MediathekPhoenix;
import msearch.filmeSuchen.sender.MediathekSr;
import msearch.tool.MSConst;
import msearch.tool.MSLog;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZOutputStream;

public class MSFilmlisteSchreiben {

    private XMLStreamWriter writer;
    private OutputStreamWriter out = null;
    private ZipOutputStream zipOutputStream = null;
    private BZip2CompressorOutputStream bZip2CompressorOutputStream = null;

    public void filmlisteSchreibenJson(String datei, ListeFilme listeFilme) {
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
                XZOutputStream out = new XZOutputStream(new FileOutputStream(file), options);
                jg = jsonF.createGenerator(out);
            } else if (datei.endsWith(MSConst.FORMAT_BZ2)) {
                bZip2CompressorOutputStream = new BZip2CompressorOutputStream(new FileOutputStream(file), 9 /*Blocksize: 1 - 9*/);
                jg = jsonF.createGenerator(bZip2CompressorOutputStream, JsonEncoding.UTF8);
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
            } catch (Exception e) {
            }
        }
    }

    public void filmlisteSchreibenXml(String datei, ListeFilme listeFilme) {
        try {
            MSLog.systemMeldung("Filme Schreiben");
            xmlSchreibenStart(datei);
            xmlSchreibenFilmliste(listeFilme);
            xmlSchreibenEnde(datei);
        } catch (Exception ex) {
            MSLog.fehlerMeldung(846930145, ex, "nach: " + datei);
        }
    }

    private void xmlSchreibenStart(String datei) throws IOException, XMLStreamException {
        File file = new File(datei);
        File dir = new File(file.getParent());
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                MSLog.fehlerMeldung(947623049, "Kann den Pfad nicht anlegen: " + dir.toString());
            }
        }
        MSLog.systemMeldung("   --> Start Schreiben nach: " + datei);
        XMLOutputFactory outFactory = XMLOutputFactory.newInstance();
        if (datei.endsWith(MSConst.FORMAT_BZ2)) {
            bZip2CompressorOutputStream = new BZip2CompressorOutputStream(new FileOutputStream(file), 9 /*Blocksize: 1 - 9*/);
            out = new OutputStreamWriter(bZip2CompressorOutputStream, MSConst.KODIERUNG_UTF);
        } else if (datei.endsWith(MSConst.FORMAT_ZIP)) {
            zipOutputStream = new ZipOutputStream(new FileOutputStream(file));
            ZipEntry entry = new ZipEntry(MSConst.XML_DATEI_FILME);
            zipOutputStream.putNextEntry(entry);
            out = new OutputStreamWriter(zipOutputStream, MSConst.KODIERUNG_UTF);
        } else {
            out = new OutputStreamWriter(new FileOutputStream(file), MSConst.KODIERUNG_UTF);
        }
        writer = outFactory.createXMLStreamWriter(out);
        writer.writeStartDocument("UTF-8", "1.0");
        writer.writeCharacters("\n");//neue Zeile
        writer.writeStartElement(MSConst.XML_START);
        writer.writeCharacters("\n");//neue Zeile
    }

    private void xmlSchreibenFilmliste(ListeFilme listeFilme) throws XMLStreamException {
        //Filmliste Metadaten schreiben
        listeFilme.metaDaten[ListeFilme.FILMLISTE_VERSION_NR] = MSConst.VERSION_FILMLISTE;
        xmlSchreibenDaten(ListeFilme.FILMLISTE, ListeFilme.COLUMN_NAMES, listeFilme.metaDaten);
        // Feldinfo schreiben
        int xmlMax = DatenFilm.COLUMN_NAMES.length;
        try {
            writer.writeStartElement(DatenFilm.FELD_INFO);
            writer.writeCharacters("\n");//neue Zeile
            for (int i = 0; i < xmlMax; ++i) {
                writer.writeStartElement(DatenFilm.COLUMN_NAMES_XML[i]);
                writer.writeCharacters(DatenFilm.COLUMN_NAMES[i]);
                writer.writeEndElement();
                writer.writeCharacters("\n");//neue Zeile
            }
            writer.writeEndElement();
            writer.writeCharacters("\n");//neue Zeile
        } catch (Exception ex) {
            MSLog.fehlerMeldung(638214005, ex);
        }
        // Filme schreiben
        ListIterator<DatenFilm> iterator;
        DatenFilm datenFilm;
        String sender = "", thema = "";
        DatenFilm datenFilmSchreiben = new DatenFilm();
        iterator = listeFilme.listIterator();
        while (iterator.hasNext()) {
            datenFilm = iterator.next();
            if (datenFilm.arr[DatenFilm.FILM_SENDER_NR].equals(MediathekPhoenix.SENDERNAME)
                    || datenFilm.arr[DatenFilm.FILM_SENDER_NR].equals(MediathekSr.SENDERNAME)) {
                // neue Sender werden nicht richtig angezeigt im Filter
                continue;
            }
            System.arraycopy(datenFilm.arr, 0, datenFilmSchreiben.arr, 0, datenFilm.arr.length);
            if (sender.equals(datenFilm.arr[DatenFilm.FILM_SENDER_NR])) {
                datenFilmSchreiben.arr[DatenFilm.FILM_SENDER_NR] = "";
            } else {
                sender = datenFilm.arr[DatenFilm.FILM_SENDER_NR];
            }
            if (thema.equals(datenFilm.arr[DatenFilm.FILM_THEMA_NR])) {
                datenFilmSchreiben.arr[DatenFilm.FILM_THEMA_NR] = "";
            } else {
                thema = datenFilm.arr[DatenFilm.FILM_THEMA_NR];
            }
            datenFilmSchreiben.clean();
            xmlSchreibenDaten(DatenFilm.FILME_, DatenFilm.COLUMN_NAMES_XML, datenFilmSchreiben.arr);
        }
    }

    private void xmlSchreibenDaten(String xmlName, String[] xmlSpalten, String[] datenArray) throws XMLStreamException {
        int xmlMax = datenArray.length;
        writer.writeStartElement(xmlName);
        for (int i = 0; i < xmlMax; ++i) {
            if (!datenArray[i].equals("")) {
                writer.writeStartElement(xmlSpalten[i]);
                writer.writeCharacters(datenArray[i]);
                writer.writeEndElement();
            }
        }
        writer.writeEndElement();
        writer.writeCharacters("\n");//neue Zeile
    }

    private void xmlSchreibenEnde(String datei) throws Exception {
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.flush();
        if (datei.endsWith(MSConst.FORMAT_BZ2)) {
            writer.close();
            out.close();
            bZip2CompressorOutputStream.close();
        } else if (datei.endsWith(MSConst.FORMAT_ZIP)) {
            zipOutputStream.closeEntry();
            writer.close();
            out.close();
            zipOutputStream.close();
        } else {
            writer.close();
            out.close();
        }
        MSLog.systemMeldung("   --> geschrieben!");
    }
}
