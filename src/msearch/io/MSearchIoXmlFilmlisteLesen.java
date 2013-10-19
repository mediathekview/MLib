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
import com.esotericsoftware.kryo.io.Input;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipInputStream;
import javax.swing.event.EventListenerList;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import msearch.filmeSuchen.MSearchListenerFilmeLaden;
import msearch.filmeSuchen.MSearchListenerFilmeLadenEvent;
import msearch.daten.MSearchConfig;
import msearch.daten.DatenFilm;
import msearch.daten.ListeFilme;
import msearch.tool.DatumZeit;
import msearch.tool.GuiFunktionen;
import msearch.tool.MSearchConst;
import msearch.tool.MSearchLog;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class MSearchIoXmlFilmlisteLesen {

    private EventListenerList listeners = new EventListenerList();
    private int max = 0;
    private int progress = 0;
    private int event;
    private int ii, i;
    private ListeFilme listeFilme_;

    public void addAdListener(MSearchListenerFilmeLaden listener) {
        listeners.add(MSearchListenerFilmeLaden.class, listener);
    }

    public boolean filmlisteLesen(String vonDateiUrl, ListeFilme listeFilme) {
        // die Filmliste "vonDateiUrl" (Url oder lokal) wird in die List "listeFilme" eingelesen
        boolean ret = true;
        boolean istUrl = GuiFunktionen.istUrl(vonDateiUrl);
        if (!istUrl) {
            if (!new File(vonDateiUrl).exists()) {
                MSearchLog.fehlerMeldung(936254789, MSearchLog.FEHLER_ART_PROG, "MSearchIoXmlFilmlisteLesen.filmlisteLesen", "Datei existiert nicht: " + vonDateiUrl);
                return false;
            }
        }
        if (istUrl && vonDateiUrl.endsWith(MSearchConst.FORMAT_BZ2) || istUrl && vonDateiUrl.endsWith(MSearchConst.FORMAT_ZIP)) {
            // da wird eine temp-Datei benutzt
            this.notifyStart(300);
            this.notifyProgress(vonDateiUrl);
        } else {
            this.notifyStart(200);
            this.notifyProgress(vonDateiUrl);
        }
        XMLInputFactory inFactory = XMLInputFactory.newInstance();
        inFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
        XMLStreamReader parser;
        InputStreamReader inReader = null;
        BZip2CompressorInputStream bZip2CompressorInputStream;
        int timeout = 10000; //10 Sekunden
        URLConnection conn;
        File tmpFile = null;
        try {
            if (!istUrl) {
                if (!new File(vonDateiUrl).exists()) {
                    return false;
                }
            }
            if (!istUrl) {
                if (vonDateiUrl.endsWith(MSearchConst.FORMAT_BZ2)) {
                    bZip2CompressorInputStream = new BZip2CompressorInputStream(new FileInputStream(vonDateiUrl));
                    inReader = new InputStreamReader(bZip2CompressorInputStream, MSearchConst.KODIERUNG_UTF);
                } else if (vonDateiUrl.endsWith(MSearchConst.FORMAT_ZIP)) {
                    ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(vonDateiUrl));
                    zipInputStream.getNextEntry();
                    inReader = new InputStreamReader(zipInputStream, MSearchConst.KODIERUNG_UTF);
                } else {
                    inReader = new InputStreamReader(new FileInputStream(vonDateiUrl), MSearchConst.KODIERUNG_UTF);
                }
            } else {
                conn = new URL(vonDateiUrl).openConnection();
                conn.setConnectTimeout(timeout);
                conn.setReadTimeout(timeout);
                conn.setRequestProperty("User-Agent", MSearchConfig.getUserAgent());
                tmpFile = File.createTempFile("mediathek", null);
                //tmpFile.deleteOnExit();
                BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
                FileOutputStream fOut = new FileOutputStream(tmpFile);
                byte[] buffer = new byte[1024];
//                long dateiGroesse = MSearchUrlDateiGroesse.laenge(vonDateiUrl);
                int n = 0;
                int count = 0;
                int countMax;
                if (vonDateiUrl.endsWith(MSearchConst.FORMAT_BZ2) || istUrl && vonDateiUrl.endsWith(MSearchConst.FORMAT_ZIP)) {
                    countMax = 44;
                } else {
                    countMax = 250;
                }
                this.notifyProgress(vonDateiUrl);

                while (!MSearchConfig.getStop() && (n = in.read(buffer)) != -1) {
                    fOut.write(buffer, 0, n);
                    ++count;
                    if (count > countMax) {
                        this.notifyProgress(vonDateiUrl);
                        count = 0;
                    }
                }
                try {
                    fOut.close();
                    in.close();
                } catch (Exception e) {
                }
                if (vonDateiUrl.endsWith(MSearchConst.FORMAT_BZ2)) {
                    inReader = new InputStreamReader(new BZip2CompressorInputStream(new FileInputStream(tmpFile)), MSearchConst.KODIERUNG_UTF);
                } else if (vonDateiUrl.endsWith(MSearchConst.FORMAT_ZIP)) {
                    ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(tmpFile));
                    zipInputStream.getNextEntry();
                    inReader = new InputStreamReader(zipInputStream, MSearchConst.KODIERUNG_UTF);
                } else {
                    inReader = new InputStreamReader(new FileInputStream(tmpFile), MSearchConst.KODIERUNG_UTF);
                }
            }
            parser = inFactory.createXMLStreamReader(inReader);
            ret = filmlisteXmlLesen(parser, vonDateiUrl /*Text im progressbar*/, listeFilme);
            notifyFertig(listeFilme);
        } catch (Exception ex) {
            ret = false;
            MSearchLog.fehlerMeldung(468956200, MSearchLog.FEHLER_ART_PROG, "MSearchIoXmlFilmlisteLesen.filmlisteLesen", ex, "von: " + vonDateiUrl);
        } finally {
            try {
                if (inReader != null) {
                    inReader.close();
                }
                if (tmpFile != null) {
                    tmpFile.delete();
                }
            } catch (Exception ex) {
                MSearchLog.fehlerMeldung(468983014, MSearchLog.FEHLER_ART_PROG, "MSearchIoXmlFilmlisteLesen.filmlisteLesen", ex);
            }
        }
        return ret;
    }

    public boolean filmlisteLesenSax2(String vonDateiUrl, ListeFilme listeFilme) {
        // die Filmliste "vonDateiUrl" (Url oder lokal) wird in die List "listeFilme" eingelesen
        boolean ret = true;
        boolean istUrl = GuiFunktionen.istUrl(vonDateiUrl);
        listeFilme_ = listeFilme;
        if (!istUrl) {
            if (!new File(vonDateiUrl).exists()) {
                MSearchLog.fehlerMeldung(936254789, MSearchLog.FEHLER_ART_PROG, "MSearchIoXmlFilmlisteLesen.filmlisteLesen", "Datei existiert nicht: " + vonDateiUrl);
                return false;
            }
        }
        if (istUrl && vonDateiUrl.endsWith(MSearchConst.FORMAT_BZ2) || istUrl && vonDateiUrl.endsWith(MSearchConst.FORMAT_ZIP)) {
            // da wird eine temp-Datei benutzt
            this.notifyStart(300);
            this.notifyProgress(vonDateiUrl);
        } else {
            this.notifyStart(200);
            this.notifyProgress(vonDateiUrl);
        }
        XMLInputFactory inFactory = XMLInputFactory.newInstance();
        inFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
        XMLStreamReader xMLStreamReader;
        XMLReader xMLReader;
        InputStreamReader inReader = null;
        BZip2CompressorInputStream bZip2CompressorInputStream;
        int timeout = 10000; //10 Sekunden
        URLConnection conn;
        File tmpFile = null;
        try {
            if (!istUrl) {
                if (!new File(vonDateiUrl).exists()) {
                    return false;
                }
            }
            if (!istUrl) {
                if (vonDateiUrl.endsWith(MSearchConst.FORMAT_BZ2)) {
                    bZip2CompressorInputStream = new BZip2CompressorInputStream(new FileInputStream(vonDateiUrl));
                    inReader = new InputStreamReader(bZip2CompressorInputStream, MSearchConst.KODIERUNG_UTF);
                } else if (vonDateiUrl.endsWith(MSearchConst.FORMAT_ZIP)) {
                    ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(vonDateiUrl));
                    zipInputStream.getNextEntry();
                    inReader = new InputStreamReader(zipInputStream, MSearchConst.KODIERUNG_UTF);
                } else {
                    inReader = new InputStreamReader(new FileInputStream(vonDateiUrl), MSearchConst.KODIERUNG_UTF);
                }
            } else {
                conn = new URL(vonDateiUrl).openConnection();
                conn.setConnectTimeout(timeout);
                conn.setReadTimeout(timeout);
                conn.setRequestProperty("User-Agent", MSearchConfig.getUserAgent());
                tmpFile = File.createTempFile("mediathek", null);
                //tmpFile.deleteOnExit();
                BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
                FileOutputStream fOut = new FileOutputStream(tmpFile);
                byte[] buffer = new byte[1024];
//                long dateiGroesse = MSearchUrlDateiGroesse.laenge(vonDateiUrl);
                int n = 0;
                int count = 0;
                int countMax;
                if (vonDateiUrl.endsWith(MSearchConst.FORMAT_BZ2) || istUrl && vonDateiUrl.endsWith(MSearchConst.FORMAT_ZIP)) {
                    countMax = 44;
                } else {
                    countMax = 250;
                }
                this.notifyProgress(vonDateiUrl);

                while (!MSearchConfig.getStop() && (n = in.read(buffer)) != -1) {
                    fOut.write(buffer, 0, n);
                    ++count;
                    if (count > countMax) {
                        this.notifyProgress(vonDateiUrl);
                        count = 0;
                    }
                }
                try {
                    fOut.close();
                    in.close();
                } catch (Exception e) {
                }
                if (vonDateiUrl.endsWith(MSearchConst.FORMAT_BZ2)) {
                    inReader = new InputStreamReader(new BZip2CompressorInputStream(new FileInputStream(tmpFile)), MSearchConst.KODIERUNG_UTF);
                } else if (vonDateiUrl.endsWith(MSearchConst.FORMAT_ZIP)) {
                    ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(tmpFile));
                    zipInputStream.getNextEntry();
                    inReader = new InputStreamReader(zipInputStream, MSearchConst.KODIERUNG_UTF);
                } else {
                    inReader = new InputStreamReader(new FileInputStream(tmpFile), MSearchConst.KODIERUNG_UTF);
                }
            }
            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            MySAXApp handler = new MySAXApp();
            xmlReader.setContentHandler(handler);
            xmlReader.parse(new InputSource(inReader));
            DatenFilm datenFilm;
            notifyFertig(listeFilme);
        } catch (Exception ex) {
            ret = false;
            MSearchLog.fehlerMeldung(468956200, MSearchLog.FEHLER_ART_PROG, "MSearchIoXmlFilmlisteLesen.filmlisteLesen", ex, "von: " + vonDateiUrl);
        } finally {
            try {
                if (inReader != null) {
                    inReader.close();
                }
                if (tmpFile != null) {
                    tmpFile.delete();
                }
            } catch (Exception ex) {
                MSearchLog.fehlerMeldung(468983014, MSearchLog.FEHLER_ART_PROG, "MSearchIoXmlFilmlisteLesen.filmlisteLesen", ex);
            }
        }
        return ret;
    }

    class MySAXApp extends DefaultHandler {

        DatenFilm datenFilm = null;
        int feld = -1;
        StringBuilder chars = new StringBuilder();

        @Override
        public void startDocument() {
            System.out.println("Start document");
        }

        @Override
        public void endDocument() {
            System.out.println("End document");
        }

        @Override
        public void startElement(String uri, String name,
                String qName, Attributes atts) {

            if (name.equals(DatenFilm.FILME_)) {
                datenFilm = new DatenFilm();
            } else if (datenFilm != null) {
                feld = -1;
                for (int i = 0; i < DatenFilm.MAX_ELEM; ++i) {
                    if (DatenFilm.COLUMN_NAMES_[i].equals(name)) {
                        feld = i;
                        break;
                    }
                }
            }
        }

        @Override
        public void endElement(String uri, String name, String qName) {
            if (name.equals(DatenFilm.FILME_)) {
                if (datenFilm != null) {
                    listeFilme_.add(datenFilm);
                }
                datenFilm = null;
            }
        }

        @Override
        public void characters(char ch[], int start, int length) {
            if (feld > -1) {
                chars.append(ch, start, length);
                datenFilm.arr[feld] = chars.toString();
                chars.setLength(0);
                feld=-1;
            }

        }
    }

    public boolean filmlisteLesenCvs(String vonDateiUrl, ListeFilme listeFilme) {
        // die Filmliste "vonDateiUrl" (Url oder lokal) wird in die List "listeFilme" eingelesen
        boolean ret = true;
        boolean istUrl = GuiFunktionen.istUrl(vonDateiUrl);
        if (!istUrl) {
            if (!new File(vonDateiUrl).exists()) {
                MSearchLog.fehlerMeldung(936254789, MSearchLog.FEHLER_ART_PROG, "MSearchIoXmlFilmlisteLesen.filmlisteLesen", "Datei existiert nicht: " + vonDateiUrl);
                return false;
            }
        }
        if (istUrl && vonDateiUrl.endsWith(MSearchConst.FORMAT_BZ2) || istUrl && vonDateiUrl.endsWith(MSearchConst.FORMAT_ZIP)) {
            // da wird eine temp-Datei benutzt
            this.notifyStart(300);
            this.notifyProgress(vonDateiUrl);
        } else {
            this.notifyStart(200);
            this.notifyProgress(vonDateiUrl);
        }
        InputStreamReader inReader = null;
        BZip2CompressorInputStream bZip2CompressorInputStream;
        LineNumberReader lineNumberReader = null;
        int timeout = 10000; //10 Sekunden
        URLConnection conn;
        File tmpFile = null;
        try {
            if (!istUrl) {
                if (!new File(vonDateiUrl).exists()) {
                    return false;
                }
            }
            if (!istUrl) {
                if (vonDateiUrl.endsWith(MSearchConst.FORMAT_BZ2)) {
                    bZip2CompressorInputStream = new BZip2CompressorInputStream(new FileInputStream(vonDateiUrl));
                    inReader = new InputStreamReader(bZip2CompressorInputStream, MSearchConst.KODIERUNG_UTF);
                } else if (vonDateiUrl.endsWith(MSearchConst.FORMAT_ZIP)) {
                    ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(vonDateiUrl));
                    zipInputStream.getNextEntry();
                    inReader = new InputStreamReader(zipInputStream, MSearchConst.KODIERUNG_UTF);
                } else {
                    inReader = new InputStreamReader(new FileInputStream(vonDateiUrl), MSearchConst.KODIERUNG_UTF);
                }
            } else {
                conn = new URL(vonDateiUrl).openConnection();
                conn.setConnectTimeout(timeout);
                conn.setReadTimeout(timeout);
                conn.setRequestProperty("User-Agent", MSearchConfig.getUserAgent());
                tmpFile = File.createTempFile("mediathek", null);
                //tmpFile.deleteOnExit();
                BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
                FileOutputStream fOut = new FileOutputStream(tmpFile);
                byte[] buffer = new byte[1024];
//                long dateiGroesse = MSearchUrlDateiGroesse.laenge(vonDateiUrl);
                int n = 0;
                int count = 0;
                int countMax;
                if (vonDateiUrl.endsWith(MSearchConst.FORMAT_BZ2) || istUrl && vonDateiUrl.endsWith(MSearchConst.FORMAT_ZIP)) {
                    countMax = 44;
                } else {
                    countMax = 250;
                }
                this.notifyProgress(vonDateiUrl);

                while (!MSearchConfig.getStop() && (n = in.read(buffer)) != -1) {
                    fOut.write(buffer, 0, n);
                    ++count;
                    if (count > countMax) {
                        this.notifyProgress(vonDateiUrl);
                        count = 0;
                    }
                }
                try {
                    fOut.close();
                    in.close();
                } catch (Exception e) {
                }
                if (vonDateiUrl.endsWith(MSearchConst.FORMAT_BZ2)) {
                    inReader = new InputStreamReader(new BZip2CompressorInputStream(new FileInputStream(tmpFile)), MSearchConst.KODIERUNG_UTF);
                } else if (vonDateiUrl.endsWith(MSearchConst.FORMAT_ZIP)) {
                    ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(tmpFile));
                    zipInputStream.getNextEntry();
                    inReader = new InputStreamReader(zipInputStream, MSearchConst.KODIERUNG_UTF);
                } else {
                    inReader = new InputStreamReader(new FileInputStream(tmpFile), MSearchConst.KODIERUNG_UTF);
                }
            }
            ret = filmlisteCvsLesen(inReader, vonDateiUrl /*Text im progressbar*/, listeFilme);
            notifyFertig(listeFilme);
        } catch (Exception ex) {
            ret = false;
            MSearchLog.fehlerMeldung(468956200, MSearchLog.FEHLER_ART_PROG, "MSearchIoXmlFilmlisteLesen.filmlisteLesen", ex, "von: " + vonDateiUrl);
        } finally {
            try {
                if (inReader != null) {
                    inReader.close();
                }
                if (tmpFile != null) {
                    tmpFile.delete();
                }
            } catch (Exception ex) {
                MSearchLog.fehlerMeldung(468983014, MSearchLog.FEHLER_ART_PROG, "MSearchIoXmlFilmlisteLesen.filmlisteLesen", ex);
            }
        }
        return ret;
    }

    public boolean filmlisteLesenJson(String vonDateiUrl, ListeFilme listeFilme) {
        // die Filmliste "vonDateiUrl" (Url oder lokal) wird in die List "listeFilme" eingelesen
        boolean ret = true;
        boolean istUrl = GuiFunktionen.istUrl(vonDateiUrl);
        if (!istUrl) {
            if (!new File(vonDateiUrl).exists()) {
                MSearchLog.fehlerMeldung(936254789, MSearchLog.FEHLER_ART_PROG, "MSearchIoXmlFilmlisteLesen.filmlisteLesen", "Datei existiert nicht: " + vonDateiUrl);
                return false;
            }
        }
        if (istUrl && vonDateiUrl.endsWith(MSearchConst.FORMAT_BZ2) || istUrl && vonDateiUrl.endsWith(MSearchConst.FORMAT_ZIP)) {
            // da wird eine temp-Datei benutzt
            this.notifyStart(300);
            this.notifyProgress(vonDateiUrl);
        } else {
            this.notifyStart(200);
            this.notifyProgress(vonDateiUrl);
        }
        XMLInputFactory inFactory = XMLInputFactory.newInstance();
        inFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
        XMLStreamReader parser;
        InputStreamReader inputStreamReader = null;
        BZip2CompressorInputStream bZip2CompressorInputStream;
        int timeout = 10000; //10 Sekunden
        URLConnection conn;
        File tmpFile = null;
        try {
            if (!istUrl) {
                if (!new File(vonDateiUrl).exists()) {
                    return false;
                }
            }
            if (!istUrl) {
                if (vonDateiUrl.endsWith(MSearchConst.FORMAT_BZ2)) {
                    bZip2CompressorInputStream = new BZip2CompressorInputStream(new FileInputStream(vonDateiUrl));
                    inputStreamReader = new InputStreamReader(bZip2CompressorInputStream, MSearchConst.KODIERUNG_UTF);
                } else if (vonDateiUrl.endsWith(MSearchConst.FORMAT_ZIP)) {
                    ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(vonDateiUrl));
                    zipInputStream.getNextEntry();
                    inputStreamReader = new InputStreamReader(zipInputStream, MSearchConst.KODIERUNG_UTF);
                } else {
                    inputStreamReader = new InputStreamReader(new FileInputStream(vonDateiUrl), MSearchConst.KODIERUNG_UTF);
                }
            } else {
                conn = new URL(vonDateiUrl).openConnection();
                conn.setConnectTimeout(timeout);
                conn.setReadTimeout(timeout);
                conn.setRequestProperty("User-Agent", MSearchConfig.getUserAgent());
                tmpFile = File.createTempFile("mediathek", null);
                //tmpFile.deleteOnExit();
                BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
                FileOutputStream fOut = new FileOutputStream(tmpFile);
                byte[] buffer = new byte[1024];
//                long dateiGroesse = MSearchUrlDateiGroesse.laenge(vonDateiUrl);
                int n = 0;
                int count = 0;
                int countMax;
                if (vonDateiUrl.endsWith(MSearchConst.FORMAT_BZ2) || istUrl && vonDateiUrl.endsWith(MSearchConst.FORMAT_ZIP)) {
                    countMax = 44;
                } else {
                    countMax = 250;
                }
                this.notifyProgress(vonDateiUrl);

                while (!MSearchConfig.getStop() && (n = in.read(buffer)) != -1) {
                    fOut.write(buffer, 0, n);
                    ++count;
                    if (count > countMax) {
                        this.notifyProgress(vonDateiUrl);
                        count = 0;
                    }
                }
                try {
                    fOut.close();
                    in.close();
                } catch (Exception e) {
                }
                if (vonDateiUrl.endsWith(MSearchConst.FORMAT_BZ2)) {
                    inputStreamReader = new InputStreamReader(new BZip2CompressorInputStream(new FileInputStream(tmpFile)), MSearchConst.KODIERUNG_UTF);
                } else if (vonDateiUrl.endsWith(MSearchConst.FORMAT_ZIP)) {
                    ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(tmpFile));
                    zipInputStream.getNextEntry();
                    inputStreamReader = new InputStreamReader(zipInputStream, MSearchConst.KODIERUNG_UTF);
                } else {
                    inputStreamReader = new InputStreamReader(new FileInputStream(tmpFile), MSearchConst.KODIERUNG_UTF);
                }
            }

            JsonFactory jsonF = new JsonFactory();
            //JsonParser jp = jsonF.createParser(new File(vonDateiUrl));
            JsonParser jp = jsonF.createParser(inputStreamReader);
            if (jp.nextToken() != JsonToken.START_OBJECT) {
                throw new IOException("Expected data to start with an Object");
            }
            // Iterate over object fields:
            while (jp.nextToken() != JsonToken.END_OBJECT) {
                if (jp.isExpectedStartArrayToken()) {
                    for (int k = 0; k < ListeFilme.MAX_ELEM; ++k) {
                        listeFilme.metaDaten[k] = jp.nextTextValue();
                    }
                    break;
                }
            }
            while (jp.nextToken() != JsonToken.END_OBJECT) {
                if (jp.isExpectedStartArrayToken()) {
                    // sind nur die Feldbeschreibungen, brauch mer nicht
                    jp.nextToken();
                    break;
                }
            }
            while (jp.nextToken() != JsonToken.END_OBJECT) {
                if (jp.isExpectedStartArrayToken()) {
                    DatenFilm datenFilm = new DatenFilm();
                    for (int i = 0; i < DatenFilm.MAX_ELEM; ++i) {
                        datenFilm.arr[i] = jp.nextTextValue();
                    }
                    listeFilme.addWithNr(datenFilm);
                }
            }
            jp.close(); // important to close both parser and u            


            notifyFertig(listeFilme);
        } catch (Exception ex) {
            ret = false;
            MSearchLog.fehlerMeldung(468956200, MSearchLog.FEHLER_ART_PROG, "MSearchIoXmlFilmlisteLesen.filmlisteLesen", ex, "von: " + vonDateiUrl);
        } finally {
            try {
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (tmpFile != null) {
                    tmpFile.delete();
                }
            } catch (Exception ex) {
                MSearchLog.fehlerMeldung(468983014, MSearchLog.FEHLER_ART_PROG, "MSearchIoXmlFilmlisteLesen.filmlisteLesen", ex);
            }
        }
        return ret;
    }

    public ListeFilme filmlisteLesenKryo(String vonDateiUrl) {
        // die Filmliste "vonDateiUrl" (Url oder lokal) wird in die List "listeFilme" eingelesen
        boolean ret = true;
        ListeFilme listeFilme = null;
        if (!new File(vonDateiUrl).exists()) {
            MSearchLog.fehlerMeldung(936254789, MSearchLog.FEHLER_ART_PROG, "MSearchIoXmlFilmlisteLesen.filmlisteLesen", "Datei existiert nicht: " + vonDateiUrl);
            return null;
        }
        this.notifyStart(200);
        this.notifyProgress(vonDateiUrl);
        InputStreamReader inReader = null;
        BZip2CompressorInputStream bZip2CompressorInputStream;
        int timeout = 10000; //10 Sekunden
        URLConnection conn;
        File tmpFile = null;
        try {
            if (!new File(vonDateiUrl).exists()) {
                return null;
            }
            Kryo kryo = new Kryo();
            Input input = new Input(new FileInputStream(vonDateiUrl));
            listeFilme = kryo.readObject(input, ListeFilme.class);
            input.close();

//            ListIterator<DatenFilm> iterator;
//            iterator = listeFilme.listIterator();
//            while (iterator.hasNext()){
//                iterator.next().init();
//            }

            notifyFertig(listeFilme);
        } catch (Exception ex) {
            ret = false;
            MSearchLog.fehlerMeldung(468956200, MSearchLog.FEHLER_ART_PROG, "MSearchIoXmlFilmlisteLesen.filmlisteLesen", ex, "von: " + vonDateiUrl);
        }
        return listeFilme;
    }

    public String filmlisteUmbenennen(String dateiFilmliste, ListeFilme listeFilme) {
        String dest = "";
        try {
            if (listeFilme.isEmpty()) {
                MSearchLog.fehlerMeldung(312126987, MSearchLog.FEHLER_ART_PROG, "MSearchIoXmlFilmlisteLesen.filmlisteUmbenennen", "Die Filmliste ist leer.");
                return "";
            }
            dest = dateiFilmliste + listeFilme.genDateRev();
            if (dateiFilmliste.equals(dest)) {
                return "";
            }
            File fileDest = new File(dest);
            if (fileDest.exists()) {
                MSearchLog.systemMeldung(new String[]{"Filmliste umbenennen: ", "Es gibt schon eine Liste mit dem Datum."});
                return "";
            }
            File fileSrc = new File(dateiFilmliste);
            fileSrc.renameTo(fileDest);
            fileSrc = null;
            fileDest = null;
        } catch (Exception ex) {
            MSearchLog.fehlerMeldung(978451206, MSearchLog.FEHLER_ART_PROG, "MSearchIoXmlFilmlisteLesen.filmlisteUmbenennen", ex);
        }
        return dest;
    }

    // ##############################
    // private
    // ##############################
    private boolean filmlisteCvsLesen(InputStreamReader inReader, String text, ListeFilme listeFilme) throws XMLStreamException {
        boolean ret = true;
        int count = 0;
        DatenFilm datenFilm;
        String sender = "", thema = "";
        String filmTag = DatenFilm.FILME_;
        String zeile = "";
        try {
//            //Filmeliste
//            if (event_ == XMLStreamConstants.START_ELEMENT) {
//                if (parser.getLocalName().equals(ListeFilme.FILMLISTE)) {
//                    get(parser, ListeFilme.FILMLISTE, ListeFilme.COLUMN_NAMES, listeFilme.metaDaten, ListeFilme.MAX_ELEM);
//                }
//            }
            boolean start = false;
            LineNumberReader lineNumberReader = new LineNumberReader(inReader);
            while ((zeile = lineNumberReader.readLine()) != null) {
                if (!start) {
                    if (zeile.equals(DatenFilm.FILME)) {
                        start = true;
                    }
                } else {
                    datenFilm = new DatenFilm();
                    splitToFilm(zeile, datenFilm);
                    if (datenFilm.arr[DatenFilm.FILM_GROESSE_NR].contains("|")) {
                        System.out.print("");
                    }
                    if (datenFilm.arr[DatenFilm.FILM_SENDER_NR].equals("")) {
                        datenFilm.arr[DatenFilm.FILM_SENDER_NR] = sender;
                    } else {
                        sender = datenFilm.arr[DatenFilm.FILM_SENDER_NR];
                    }
                    if (datenFilm.arr[DatenFilm.FILM_THEMA_NR].equals("")) {
                        datenFilm.arr[DatenFilm.FILM_THEMA_NR] = thema;
                    } else {
                        thema = datenFilm.arr[DatenFilm.FILM_THEMA_NR];
                    }
                    ++count;
                    if (count > 790) {
                        count = 0;
                        this.notifyProgress(text);
                    }
                    listeFilme.addWithNr(datenFilm);
                }
            }
        } catch (Exception ex) {
        }
        return ret;
    }

    private boolean filmlisteCvsLesen_(InputStreamReader inReader, String text, ListeFilme listeFilme) throws XMLStreamException {
        boolean ret = true;
        int count = 0;
        DatenFilm datenFilm;
        String sender = "", thema = "";
        String filmTag = DatenFilm.FILME_;
        String zeile = "";
        try {
//            //Filmeliste
//            if (event_ == XMLStreamConstants.START_ELEMENT) {
//                if (parser.getLocalName().equals(ListeFilme.FILMLISTE)) {
//                    get(parser, ListeFilme.FILMLISTE, ListeFilme.COLUMN_NAMES, listeFilme.metaDaten, ListeFilme.MAX_ELEM);
//                }
//            }
            boolean start = false;
            char[] zeichen = new char[1];
            char c;
            datenFilm = new DatenFilm();
            int i = 0;
            while (inReader.read(zeichen) != -1) {
                if (zeichen[0] == '#') {
                    break;
                }
            }
            while (inReader.read(zeichen) != -1) {
                c = zeichen[0];
                if (c == ';') {
                    ++i;
                } else if (c == '\n') {
                    if (datenFilm.arr[DatenFilm.FILM_SENDER_NR].equals("")) {
                        datenFilm.arr[DatenFilm.FILM_SENDER_NR] = sender;
                    } else {
                        sender = datenFilm.arr[DatenFilm.FILM_SENDER_NR];
                    }
                    if (datenFilm.arr[DatenFilm.FILM_THEMA_NR].equals("")) {
                        datenFilm.arr[DatenFilm.FILM_THEMA_NR] = thema;
                    } else {
                        thema = datenFilm.arr[DatenFilm.FILM_THEMA_NR];
                    }
                    ++count;
                    if (count > 790) {
                        count = 0;
                        this.notifyProgress(text);
                    }
                    listeFilme.addWithNr(datenFilm);
                    datenFilm = new DatenFilm();
                    i = 0;
                } else {
                    datenFilm.arr[i] += c;
                }
            }
        } catch (Exception ex) {
        }
        return ret;
    }

    private void splitToFilm(String zeile, DatenFilm film) {
        int i = 0;
//        for (int s = 0; s < zeile.length(); ++s) {
//            if (zeile.charAt(s) == ';') {
//                ++i;
//            } else {
//                film.arr[i] += zeile.charAt(s);
//            }
//        }
        while (zeile.contains(";")) {
            film.arr[i++] = zeile.substring(0, zeile.indexOf(";"));
            zeile = zeile.substring(zeile.indexOf(";") + 1);
        }
    }

    private boolean filmlisteXmlLesen(XMLStreamReader parser, String text, ListeFilme listeFilme) throws XMLStreamException {
        boolean ret = true;
        int count = 0;
        DatenFilm datenFilm;
        String sender = "", thema = "";
        int event_;
        String filmTag = DatenFilm.FILME_;
        String[] namen = DatenFilm.COLUMN_NAMES_;
        while (!MSearchConfig.getStop() && parser.hasNext()) {
            event_ = parser.next();
            //Filme
            if (event_ == XMLStreamConstants.START_ELEMENT) {
                if (parser.getLocalName().equals(filmTag)) {
                    datenFilm = new DatenFilm();
                    get(parser, filmTag, namen, datenFilm.arr, DatenFilm.MAX_ELEM);
                    if (datenFilm.arr[DatenFilm.FILM_SENDER_NR].equals("")) {
                        datenFilm.arr[DatenFilm.FILM_SENDER_NR] = sender;
                    } else {
                        sender = datenFilm.arr[DatenFilm.FILM_SENDER_NR];
                    }
                    if (datenFilm.arr[DatenFilm.FILM_THEMA_NR].equals("")) {
                        datenFilm.arr[DatenFilm.FILM_THEMA_NR] = thema;
                    } else {
                        thema = datenFilm.arr[DatenFilm.FILM_THEMA_NR];
                    }
                    ++count;
                    if (count > 790) {
                        count = 0;
                        this.notifyProgress(text);
                    }
                    listeFilme.addWithNr(datenFilm);
                    continue;
                }
            }
            //Filmeliste
            if (event_ == XMLStreamConstants.START_ELEMENT) {
                if (parser.getLocalName().equals(ListeFilme.FILMLISTE)) {
                    get(parser, ListeFilme.FILMLISTE, ListeFilme.COLUMN_NAMES, listeFilme.metaDaten, ListeFilme.MAX_ELEM);
                }
            }
        }
        return ret;
    }

    private void get(XMLStreamReader parser, String xmlElem, String[] xmlNames, String[] strRet, int maxElem) throws XMLStreamException {
        ii = 0;
        outer:
        while (parser.hasNext()) {
            event = parser.next();
            if (event == XMLStreamConstants.END_ELEMENT) {
                if (parser.getLocalName().equals(xmlElem)) {
                    break;
                }
            }
            if (event == XMLStreamConstants.START_ELEMENT) {
                for (i = ii; i < maxElem; ++i) {
                    // String s = parser.getLocalName();
                    if (parser.getLocalName().equals(xmlNames[i])) {
                        strRet[i] = parser.getElementText();
                        ii = ++i;
                        continue outer;
                    }
                }
                for (i = 0; i < maxElem; ++i) {
                    // String s = parser.getLocalName();
                    if (parser.getLocalName().equals(xmlNames[i])) {
                        strRet[i] = parser.getElementText();
                        continue outer;
                    }
                }
            }
        }
    }

    private void notifyStart(int mmax) {
        max = mmax;
        progress = 0;
        for (MSearchListenerFilmeLaden l : listeners.getListeners(MSearchListenerFilmeLaden.class)) {
            l.start(new MSearchListenerFilmeLadenEvent("", "", max, 0));
        }
    }

    private void notifyProgress(String text) {
        if (progress < max) {
            progress += 1;
        }
        for (MSearchListenerFilmeLaden l : listeners.getListeners(MSearchListenerFilmeLaden.class)) {
            l.progress(new MSearchListenerFilmeLadenEvent("", text, max, progress));
        }
    }

    private void notifyFertig(ListeFilme liste) {
        MSearchLog.systemMeldung("Liste Filme gelesen: " + DatumZeit.getHeute_dd_MM_yyyy() + " " + DatumZeit.getJetzt_HH_MM_SS());
        MSearchLog.systemMeldung("Anzahl Filme: " + liste.size());
        for (MSearchListenerFilmeLaden l : listeners.getListeners(MSearchListenerFilmeLaden.class)) {
            l.fertig(new MSearchListenerFilmeLadenEvent("", "", max, progress));
        }
    }
}
