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
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipInputStream;
import javax.swing.event.EventListenerList;
import msearch.daten.DatenFilm;
import msearch.daten.ListeFilme;
import msearch.daten.MSConfig;
import msearch.filmeSuchen.MSListenerFilmeLaden;
import msearch.filmeSuchen.MSListenerFilmeLadenEvent;
import msearch.tool.DatumZeit;
import msearch.tool.MSConst;
import msearch.tool.MSGuiFunktionen;
import msearch.tool.MSLog;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.tukaani.xz.XZInputStream;

public class MSFilmlisteLesen {

    private final EventListenerList listeners = new EventListenerList();
    private int max = 0;
    private int progress = 0;
    private static final int TIMEOUT = 10000; //10 Sekunden

    public void addAdListener(MSListenerFilmeLaden listener) {
        listeners.add(MSListenerFilmeLaden.class, listener);
    }

    public boolean filmlisteLesenJson(String vonDateiUrl, String nachDatei, ListeFilme listeFilme) {
        // die Filmliste "vonDateiUrl" (Url oder lokal) wird in die List "listeFilme" eingelesen und 
        // wenn angegeben: in "nachDatei" gespeichert
        boolean ret = false;
        File tmpFile = null;
        boolean istUrl = MSGuiFunktionen.istUrl(vonDateiUrl);
        try {
            if (istUrl && vonDateiUrl.endsWith(MSConst.FORMAT_XZ)
                    || istUrl && vonDateiUrl.endsWith(MSConst.FORMAT_BZ2)
                    || istUrl && vonDateiUrl.endsWith(MSConst.FORMAT_ZIP)) {
                // da wird eine temp-Datei benutzt
                this.notifyStart(300);
                this.notifyProgress(vonDateiUrl);
            } else {
                this.notifyStart(200);
                this.notifyProgress(vonDateiUrl);
            }
            if (istUrl) {
                // dann erst Download in temp-Datei
                if (vonDateiUrl.endsWith(MSConst.FORMAT_XZ)) {
                    tmpFile = File.createTempFile("mediathek", MSConst.FORMAT_XZ);
                } else if (vonDateiUrl.endsWith(MSConst.FORMAT_BZ2)) {
                    tmpFile = File.createTempFile("mediathek", MSConst.FORMAT_BZ2);
                } else if (vonDateiUrl.endsWith(MSConst.FORMAT_ZIP)) {
                    tmpFile = File.createTempFile("mediathek", MSConst.FORMAT_ZIP);
                } else {
                    tmpFile = File.createTempFile("mediathek", null);
                }
                if (filmlisteDownload(vonDateiUrl, tmpFile)) {
                    // dann einlesen
                    if (nachDatei.isEmpty()) {
                        if (filmlisteJsonEinlesen(tmpFile, listeFilme)) {
                            ret = true;
                        }
                    } else {
                        File ziel = new File(nachDatei);
                        if (filmlisteEntpackenKopieren(tmpFile, ziel)) {
                            if (filmlisteJsonEinlesen(ziel, listeFilme)) {
                                ret = true;
                            }
                        }
                    }
                }
            } else {
                // lokale Datei
                if (nachDatei.isEmpty()) {
                    if (filmlisteJsonEinlesen(new File(vonDateiUrl), listeFilme)) {
                        ret = true;
                    }
                } else {
                    File ziel = new File(nachDatei);
                    if (filmlisteEntpackenKopieren(new File(vonDateiUrl), ziel)) {
                        if (filmlisteJsonEinlesen(ziel, listeFilme)) {
                            ret = true;
                        }
                    }
                }
            }
            // ##########################################################
            // und jetzt die Liste einlesen
            notifyFertig(listeFilme);
        } catch (Exception ex) {
            MSLog.fehlerMeldung(468956200, MSLog.FEHLER_ART_PROG, "MSearchIoXmlFilmlisteLesen.filmlisteLesen", ex, "von: " + vonDateiUrl);
        }
        if (tmpFile != null) {
            try {
                tmpFile.delete();
            } catch (Exception ignore) {
            }
        }
        return ret;
    }

    public String filmlisteUmbenennen(String dateiFilmliste, ListeFilme listeFilme) {
        String dest = "";
        try {
            if (listeFilme.isEmpty()) {
                MSLog.fehlerMeldung(312126987, MSLog.FEHLER_ART_PROG, "MSearchIoXmlFilmlisteLesen.filmlisteUmbenennen", "Die Filmliste ist leer.");
                return "";
            }
            dest = dateiFilmliste + listeFilme.genDateRev();
            if (dateiFilmliste.equals(dest)) {
                return "";
            }
            File fileDest = new File(dest);
            if (fileDest.exists()) {
                MSLog.systemMeldung(new String[]{"Filmliste umbenennen: ", "Es gibt schon eine Liste mit dem Datum."});
                return "";
            }
            File fileSrc = new File(dateiFilmliste);
            fileSrc.renameTo(fileDest);
        } catch (Exception ex) {
            MSLog.fehlerMeldung(978451206, MSLog.FEHLER_ART_PROG, "MSearchIoXmlFilmlisteLesen.filmlisteUmbenennen", ex);
        }
        return dest;
    }

    // ##############################
    // private
    // ##############################
    private boolean filmlisteJsonEinlesen(File vonDatei, ListeFilme listeFilme) {
        boolean ret = false;
        BZip2CompressorInputStream bZip2CompressorInputStream;
        JsonFactory jsonF = new JsonFactory();
        JsonParser jp = null;
        JsonToken jsonToken;
        String sender = "", thema = "";
        try {
            // ##########################################################
            // und jetzt die Liste einlesen, URL kann es jetzt schon nicht mehr sein!
            if (!vonDatei.exists()) {
                MSLog.fehlerMeldung(702030698, MSLog.FEHLER_ART_PROG, "MSearchIoXmlFilmlisteLesen.filmlisteLesen", "Datei existiert nicht: " + vonDatei.getName());
                return false;
            }
            if (vonDatei.getName().endsWith(MSConst.FORMAT_XZ)) {
                XZInputStream xZInputStream = new XZInputStream(new FileInputStream(vonDatei));
                jp = jsonF.createParser(new InputStreamReader(xZInputStream, MSConst.KODIERUNG_UTF));
            } else if (vonDatei.getName().endsWith(MSConst.FORMAT_BZ2)) {
                bZip2CompressorInputStream = new BZip2CompressorInputStream(new FileInputStream(vonDatei));
                jp = jsonF.createParser(new InputStreamReader(bZip2CompressorInputStream, MSConst.KODIERUNG_UTF));
            } else if (vonDatei.getName().endsWith(MSConst.FORMAT_ZIP)) {
                ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(vonDatei));
                zipInputStream.getNextEntry();
                jp = jsonF.createParser(new InputStreamReader(zipInputStream, MSConst.KODIERUNG_UTF));
            } else {
                jp = jsonF.createParser(vonDatei); // geht so am schnellsten
            }
            if (jp.nextToken() != JsonToken.START_OBJECT) {
                throw new IOException("Expected data to start with an Object");
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
                    listeFilme.importFilmliste(datenFilm);
                }
            }
            jp.close();
            ret = true;
        } catch (Exception ex) {
            MSLog.fehlerMeldung(468956200, MSLog.FEHLER_ART_PROG, "MSearchIoXmlFilmlisteLesen.filmlisteLesen", ex, "von: " + vonDatei.getName());
        }
        return ret;
    }

    private boolean filmlisteEntpackenKopieren(File vonDatei, File nachDatei) {
        boolean ret = false;
        String vonDateiName = vonDatei.getName();
        BufferedInputStream in;
        if (vonDateiName.equals(nachDatei.getName())) {
            return true;
        }
        try {
            if (vonDatei.getName().endsWith(MSConst.FORMAT_XZ)) {
                in = new BufferedInputStream(new XZInputStream(new FileInputStream(vonDatei)));
            } else if (vonDateiName.endsWith(MSConst.FORMAT_BZ2)) {
                in = new BufferedInputStream(new BZip2CompressorInputStream(new FileInputStream(vonDatei)));
            } else if (vonDateiName.endsWith(MSConst.FORMAT_ZIP)) {
                ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(vonDatei));
                zipInputStream.getNextEntry();
                in = new BufferedInputStream(zipInputStream);
            } else {
                Files.copy(Paths.get(vonDatei.getPath()), Paths.get(nachDatei.getPath()), StandardCopyOption.REPLACE_EXISTING);
                return true;
                //in = new BufferedInputStream(new FileInputStream(vonDatei));
            }
            FileOutputStream fOut;
            byte[] buffer = new byte[1024];
            int n = 0;
            int count = 0;
            int countMax;
            if (vonDateiName.endsWith(MSConst.FORMAT_XZ)
                    || vonDateiName.endsWith(MSConst.FORMAT_BZ2)
                    || vonDateiName.endsWith(MSConst.FORMAT_ZIP)) {
                countMax = 44;
            } else {
                countMax = 250;
            }
            fOut = new FileOutputStream(nachDatei);
            this.notifyProgress(vonDateiName);
            while (!MSConfig.getStop() && (n = in.read(buffer)) != -1) {
                fOut.write(buffer, 0, n);
                ++count;
                if (count > countMax) {
                    this.notifyProgress(vonDateiName);
                    count = 0;
                }
            }
            ret = !MSConfig.getStop();
            try {
                fOut.close();
                in.close();
            } catch (Exception e) {
            }
        } catch (Exception ex) {
            MSLog.fehlerMeldung(915236765, MSLog.FEHLER_ART_PROG, "MSearchIoXmlFilmlisteLesen.filmlisteEntpackenKopieren", ex);
        }
        return ret;
    }

    private boolean filmlisteDownload(String uurl, File nachDatei) {
        boolean ret = false;
        try {
            URLConnection conn = new URL(uurl).openConnection();
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
            conn.setRequestProperty("User-Agent", MSConfig.getUserAgent());
            BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
            FileOutputStream fOut;
            byte[] buffer = new byte[1024];
            int n = 0;
            int count = 0;
            int countMax;
            if (uurl.endsWith(MSConst.FORMAT_XZ)
                    || uurl.endsWith(MSConst.FORMAT_BZ2)
                    || uurl.endsWith(MSConst.FORMAT_ZIP)) {
                countMax = 44;
            } else {
                countMax = 250;
            }
            fOut = new FileOutputStream(nachDatei);
            this.notifyProgress(uurl);
            while (!MSConfig.getStop() && (n = in.read(buffer)) != -1) {
                fOut.write(buffer, 0, n);
                ++count;
                if (count > countMax) {
                    this.notifyProgress(uurl);
                    count = 0;
                }
            }
            ret = !MSConfig.getStop();
            try {
                fOut.close();
                in.close();
            } catch (Exception e) {
            }
        } catch (Exception ex) {
            MSLog.fehlerMeldung(952163678, MSLog.FEHLER_ART_PROG, "MSearchIoXmlFilmlisteLesen.filmlisteDownload", ex);
        }
        return ret;
    }

    private void notifyStart(int mmax) {
        max = mmax;
        progress = 0;
        for (MSListenerFilmeLaden l : listeners.getListeners(MSListenerFilmeLaden.class)) {
            l.start(new MSListenerFilmeLadenEvent("", "", max, 0, false));
        }
    }

    private void notifyProgress(String text) {
        if (progress < max) {
            progress += 1;
        }
        for (MSListenerFilmeLaden l : listeners.getListeners(MSListenerFilmeLaden.class)) {
            l.progress(new MSListenerFilmeLadenEvent("", text, max, progress, false));
        }
    }

    private void notifyFertig(ListeFilme liste) {
        MSLog.systemMeldung("Liste Filme gelesen: " + DatumZeit.getHeute_dd_MM_yyyy() + " " + DatumZeit.getJetzt_HH_MM_SS());
        MSLog.systemMeldung("Anzahl Filme: " + liste.size());
        for (MSListenerFilmeLaden l : listeners.getListeners(MSListenerFilmeLaden.class)) {
            l.fertig(new MSListenerFilmeLadenEvent("", "", max, progress, false));
        }
    }
}
