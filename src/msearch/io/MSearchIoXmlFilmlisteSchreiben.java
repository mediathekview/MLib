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

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import java.io.BufferedWriter;
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
import msearch.tool.MSearchConst;
import msearch.tool.MSearchLog;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.json.simple.JSONArray;

public class MSearchIoXmlFilmlisteSchreiben {

    private XMLOutputFactory outFactory;
    private XMLStreamWriter writer;
    private OutputStreamWriter out = null;
    private BufferedWriter bw = null;
    ZipOutputStream zipOutputStream = null;
    BZip2CompressorOutputStream bZip2CompressorOutputStream = null;

    public MSearchIoXmlFilmlisteSchreiben() {
    }

    public void filmeSchreiben(String datei, ListeFilme listeFilme) {
        try {
            MSearchLog.systemMeldung("Filme Schreiben");
            xmlSchreibenStart(datei);
            xmlSchreibenFilmliste(listeFilme);
            xmlSchreibenEnde(datei);
        } catch (Exception ex) {
            MSearchLog.fehlerMeldung(846930145, MSearchLog.FEHLER_ART_PROG, "IoXmlSchreiben.FilmeSchreiben", ex, "nach: " + datei);
        }
    }

    public void filmeSchreibenCvs(String datei, ListeFilme listeFilme) {
        try {
            MSearchLog.systemMeldung("Filme Schreiben");
            cvsSchreibenStart(datei);
            cvsSchreibenFilmliste(listeFilme);
            cvsSchreibenEnde(datei);
        } catch (Exception ex) {
            MSearchLog.fehlerMeldung(846930145, MSearchLog.FEHLER_ART_PROG, "IoXmlSchreiben.FilmeSchreiben", ex, "nach: " + datei);
        }
    }

    public void filmeSchreibenJson(String datei, ListeFilme listeFilme) {
        try {
            MSearchLog.systemMeldung("Filme Schreiben");
            File file = new File(datei);
            File dir = new File(file.getParent());
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    MSearchLog.fehlerMeldung(936254789, MSearchLog.FEHLER_ART_PROG, "MSearchIoXmlFilmlisteSchreiben.xmlSchreibenStart", "Kann den Pfad nicht anlegen: " + dir.toString());
                }
            }
            MSearchLog.systemMeldung("Start Schreiben nach: " + datei);
            if (datei.endsWith(MSearchConst.FORMAT_BZ2)) {
                bZip2CompressorOutputStream = new BZip2CompressorOutputStream(new FileOutputStream(file), 9 /*Blocksize: 1 - 9*/);
                out = new OutputStreamWriter(bZip2CompressorOutputStream, MSearchConst.KODIERUNG_UTF);
            } else if (datei.endsWith(MSearchConst.FORMAT_ZIP)) {
                zipOutputStream = new ZipOutputStream(new FileOutputStream(file));
                ZipEntry entry = new ZipEntry(MSearchConst.XML_DATEI_FILME);
                zipOutputStream.putNextEntry(entry);
                out = new OutputStreamWriter(zipOutputStream, MSearchConst.KODIERUNG_UTF);
            } else {
                out = new OutputStreamWriter(new FileOutputStream(file), MSearchConst.KODIERUNG_UTF);
            }
//            bw = new BufferedWriter(out);
            //////////////////////////
            //Filmliste Metadaten schreiben
            listeFilme.metaDaten[ListeFilme.FILMLISTE_VERSION_NR] = MSearchConst.VERSION_FILMLISTE;
            //Filme schreiben
            ListIterator<DatenFilm> iterator;
            DatenFilm datenFilm;
            String sender = "", thema = "";
            DatenFilm datenFilmSchreiben = new DatenFilm();
            iterator = listeFilme.listIterator();
            try {
                bw.write(DatenFilm.FILME);
                bw.write("\n");
//            bw.write("#");
            } catch (Exception ex) {
            }
            while (iterator.hasNext()) {

                JSONArray list = new JSONArray();
                datenFilm = iterator.next();
                for (int i = 0; i < datenFilm.arr.length; ++i) {
                    list.add(datenFilm.arr[i]);
                }
                ////////////////////////////////////////
                out.write(list.toJSONString());
                out.write("\n");//neue Zeile
            }
            ////////////////////////////////////////
            if (datei.endsWith(MSearchConst.FORMAT_BZ2)) {
                out.close();
                bZip2CompressorOutputStream.close();
            } else if (datei.endsWith(MSearchConst.FORMAT_ZIP)) {
                zipOutputStream.closeEntry();
                out.close();
                zipOutputStream.close();
            } else {
                out.close();
            }

            MSearchLog.systemMeldung("geschrieben!");
        } catch (Exception ex) {
            MSearchLog.fehlerMeldung(846930145, MSearchLog.FEHLER_ART_PROG, "IoXmlSchreiben.FilmeSchreiben", ex, "nach: " + datei);
        }
    }

    public void filmeSchreibenKryo(String datei, ListeFilme listeFilme) {
        try {
            FileOutputStream o;
            MSearchLog.systemMeldung("Filme Schreiben");
            File file = new File(datei);
            File dir = new File(file.getParent());
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    MSearchLog.fehlerMeldung(936254789, MSearchLog.FEHLER_ART_PROG, "MSearchIoXmlFilmlisteSchreiben.xmlSchreibenStart", "Kann den Pfad nicht anlegen: " + dir.toString());
                }
            }
            MSearchLog.systemMeldung("Start Schreiben nach: " + datei);
            listeFilme.metaDaten[ListeFilme.FILMLISTE_VERSION_NR] = MSearchConst.VERSION_FILMLISTE;
            try {
                Kryo kryo = new Kryo();
                o = new FileOutputStream(file);
                Output op = new Output(o);
                kryo.writeObject(op, listeFilme);
                op.close();
            } catch (Exception ex) {
            }
            MSearchLog.systemMeldung("geschrieben!");
        } catch (Exception ex) {
            MSearchLog.fehlerMeldung(846930145, MSearchLog.FEHLER_ART_PROG, "IoXmlSchreiben.FilmeSchreiben", ex, "nach: " + datei);
        }
    }

    // ##############################
    // private
    // ##############################
    private void cvsSchreibenStart(String datei) throws IOException, XMLStreamException {
        File file = new File(datei);
        File dir = new File(file.getParent());
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                MSearchLog.fehlerMeldung(936254789, MSearchLog.FEHLER_ART_PROG, "MSearchIoXmlFilmlisteSchreiben.xmlSchreibenStart", "Kann den Pfad nicht anlegen: " + dir.toString());
            }
        }
        MSearchLog.systemMeldung("Start Schreiben nach: " + datei);
        if (datei.endsWith(MSearchConst.FORMAT_BZ2)) {
            bZip2CompressorOutputStream = new BZip2CompressorOutputStream(new FileOutputStream(file), 9 /*Blocksize: 1 - 9*/);
            out = new OutputStreamWriter(bZip2CompressorOutputStream, MSearchConst.KODIERUNG_UTF);
        } else if (datei.endsWith(MSearchConst.FORMAT_ZIP)) {
            zipOutputStream = new ZipOutputStream(new FileOutputStream(file));
            ZipEntry entry = new ZipEntry(MSearchConst.XML_DATEI_FILME);
            zipOutputStream.putNextEntry(entry);
            out = new OutputStreamWriter(zipOutputStream, MSearchConst.KODIERUNG_UTF);
        } else {
            out = new OutputStreamWriter(new FileOutputStream(file), MSearchConst.KODIERUNG_UTF);
        }
        bw = new BufferedWriter(out);
    }

    private void cvsSchreibenFilmliste(ListeFilme listeFilme) throws XMLStreamException {
        //Filmliste Metadaten schreiben
        listeFilme.metaDaten[ListeFilme.FILMLISTE_VERSION_NR] = MSearchConst.VERSION_FILMLISTE;
        cvsSchreibenDaten(ListeFilme.FILMLISTE, ListeFilme.COLUMN_NAMES, listeFilme.metaDaten);
        cvsSchreibenFeldInfo();
        //Filme schreiben
        ListIterator<DatenFilm> iterator;
        DatenFilm datenFilm;
        String sender = "", thema = "";
        DatenFilm datenFilmSchreiben = new DatenFilm();
        iterator = listeFilme.listIterator();
        try {
            bw.write(DatenFilm.FILME);
            bw.write("\n");
//            bw.write("#");
        } catch (Exception ex) {
        }
        while (iterator.hasNext()) {
            datenFilm = iterator.next();
            for (int i = 0; i < datenFilm.arr.length; ++i) {
                datenFilmSchreiben.arr[i] = datenFilm.arr[i];
            }
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
            cvsSchreibenDaten(DatenFilm.FILME_, DatenFilm.COLUMN_NAMES_, datenFilmSchreiben.getClean().arr);
        }
    }

    private void cvsSchreibenFeldInfo() {
        int xmlMax = DatenFilm.COLUMN_NAMES.length;
        try {
            bw.write(DatenFilm.FELD_INFO);
            bw.write("\n");//neue Zeile
            for (int i = 0; i < xmlMax; ++i) {
                bw.write(DatenFilm.COLUMN_NAMES_[i]);
                bw.write(DatenFilm.COLUMN_NAMES[i]);
                bw.write("\n");//neue Zeile
            }
            bw.write("\n");//neue Zeile
        } catch (Exception ex) {
            MSearchLog.fehlerMeldung(638214005, MSearchLog.FEHLER_ART_PROG, "IoXmlSchreiben.xmlSchreibenFeldInfo", ex);
        }
    }

    private void cvsSchreibenDaten(String xmlName, String[] xmlSpalten, String[] datenArray) throws XMLStreamException {
        int xmlMax = datenArray.length;
        try {
            for (int i = 0; i < xmlMax; ++i) {
                bw.write(datenArray[i]);
                bw.write(";");
            }
            bw.write("\n");//neue Zeile
        } catch (Exception ex) {
            MSearchLog.fehlerMeldung(638214005, MSearchLog.FEHLER_ART_PROG, "IoXmlSchreiben.xmlSchreibenFeldInfo", ex);
        }
    }

    private void cvsSchreibenEnde(String datei) throws Exception {
        bw.flush();
        if (datei.endsWith(MSearchConst.FORMAT_BZ2)) {
            bw.close();
            out.close();
            bZip2CompressorOutputStream.close();
        } else if (datei.endsWith(MSearchConst.FORMAT_ZIP)) {
            zipOutputStream.closeEntry();
            bw.close();
            out.close();
            zipOutputStream.close();
        } else {
            bw.close();
            out.close();
        }
        MSearchLog.systemMeldung("geschrieben!");
    }

    private void xmlSchreibenStart(String datei) throws IOException, XMLStreamException {
        File file = new File(datei);
        File dir = new File(file.getParent());
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                MSearchLog.fehlerMeldung(936254789, MSearchLog.FEHLER_ART_PROG, "MSearchIoXmlFilmlisteSchreiben.xmlSchreibenStart", "Kann den Pfad nicht anlegen: " + dir.toString());
            }
        }
        MSearchLog.systemMeldung("Start Schreiben nach: " + datei);
        outFactory = XMLOutputFactory.newInstance();
        if (datei.endsWith(MSearchConst.FORMAT_BZ2)) {
            bZip2CompressorOutputStream = new BZip2CompressorOutputStream(new FileOutputStream(file), 9 /*Blocksize: 1 - 9*/);
            out = new OutputStreamWriter(bZip2CompressorOutputStream, MSearchConst.KODIERUNG_UTF);
        } else if (datei.endsWith(MSearchConst.FORMAT_ZIP)) {
            zipOutputStream = new ZipOutputStream(new FileOutputStream(file));
            ZipEntry entry = new ZipEntry(MSearchConst.XML_DATEI_FILME);
            zipOutputStream.putNextEntry(entry);
            out = new OutputStreamWriter(zipOutputStream, MSearchConst.KODIERUNG_UTF);
        } else {
            out = new OutputStreamWriter(new FileOutputStream(file), MSearchConst.KODIERUNG_UTF);
        }
        writer = outFactory.createXMLStreamWriter(out);
        writer.writeStartDocument("UTF-8", "1.0");
        writer.writeCharacters("\n");//neue Zeile
        writer.writeStartElement(MSearchConst.XML_START);
        writer.writeCharacters("\n");//neue Zeile
    }

    private void xmlSchreibenFilmliste(ListeFilme listeFilme) throws XMLStreamException {
        //Filmliste Metadaten schreiben
        listeFilme.metaDaten[ListeFilme.FILMLISTE_VERSION_NR] = MSearchConst.VERSION_FILMLISTE;
        xmlSchreibenDaten(ListeFilme.FILMLISTE, ListeFilme.COLUMN_NAMES, listeFilme.metaDaten);
        xmlSchreibenFeldInfo();
        //Filme schreiben
        ListIterator<DatenFilm> iterator;
        DatenFilm datenFilm;
        String sender = "", thema = "";
        DatenFilm datenFilmSchreiben = new DatenFilm();
        iterator = listeFilme.listIterator();
        while (iterator.hasNext()) {
            datenFilm = iterator.next();
            for (int i = 0; i < datenFilm.arr.length; ++i) {
                datenFilmSchreiben.arr[i] = datenFilm.arr[i];
            }
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
            xmlSchreibenDaten(DatenFilm.FILME_, DatenFilm.COLUMN_NAMES_, datenFilmSchreiben.getClean().arr);
        }
    }

    private void xmlSchreibenFeldInfo() {
        int xmlMax = DatenFilm.COLUMN_NAMES.length;
        try {
            writer.writeStartElement(DatenFilm.FELD_INFO);
            writer.writeCharacters("\n");//neue Zeile
            for (int i = 0; i < xmlMax; ++i) {
                writer.writeStartElement(DatenFilm.COLUMN_NAMES_[i]);
                writer.writeCharacters(DatenFilm.COLUMN_NAMES[i]);
                writer.writeEndElement();
                writer.writeCharacters("\n");//neue Zeile
            }
            writer.writeEndElement();
            writer.writeCharacters("\n");//neue Zeile
        } catch (Exception ex) {
            MSearchLog.fehlerMeldung(638214005, MSearchLog.FEHLER_ART_PROG, "IoXmlSchreiben.xmlSchreibenFeldInfo", ex);
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
        if (datei.endsWith(MSearchConst.FORMAT_BZ2)) {
            writer.close();
            out.close();
            bZip2CompressorOutputStream.close();
        } else if (datei.endsWith(MSearchConst.FORMAT_ZIP)) {
            zipOutputStream.closeEntry();
            writer.close();
            out.close();
            zipOutputStream.close();
        } else {
            writer.close();
            out.close();
        }
        MSearchLog.systemMeldung("geschrieben!");
    }
}
