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
package msearch.filmeLaden;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import msearch.daten.MSConfig;
import msearch.tool.MSConst;
import msearch.tool.MSFunktionen;
import msearch.tool.MSLog;

public class MSFilmlistenSuchen {

    // damit werden die DownloadURLs zum Laden einer Filmliste gesucht
    // Liste mit den URLs zum Download der Filmliste
    public ListeFilmlistenUrls listeFilmlistenUrls_akt = new ListeFilmlistenUrls();
    public ListeFilmlistenUrls listeFilmlistenUrls_diff = new ListeFilmlistenUrls();
    private static boolean firstSearchAkt = true;
    private static boolean firstSearchDiff = true;

    public String suchenAkt(ArrayList<String> bereitsVersucht) {
        // passende URL zum Laden der Filmliste suchen
        String retUrl;
        if (firstSearchAkt || listeFilmlistenUrls_akt.isEmpty()) {
            // nach dem Programmstart wird die Liste einmal aktualisiert
            firstSearchAkt = false;
            // da sich die Listen nicht ändern nur eimal pro Start laden
            updateURLsFilmlisten(true, false);
        }
        retUrl = (listeFilmlistenUrls_akt.getRand(bereitsVersucht)); //eine Zufällige Adresse wählen
        if (bereitsVersucht != null) {
            bereitsVersucht.add(retUrl);
        }
        return retUrl;
    }

    public String suchenDiff(ArrayList<String> bereitsVersucht) {
        // passende URL zum Laden der Filmliste suchen
        String retUrl;
        if (firstSearchDiff || listeFilmlistenUrls_diff.isEmpty()) {
            // nach dem Programmstart wird die Liste einmal aktualisiert
            firstSearchDiff = false;
            // da sich die Listen nicht ändern nur eimal pro Start laden
            updateURLsFilmlisten(false, true);
        }
        retUrl = (listeFilmlistenUrls_diff.getRand(bereitsVersucht)); //eine Zufällige Adresse wählen
        if (bereitsVersucht != null) {
            bereitsVersucht.add(retUrl);
        }
        return retUrl;
    }

    public void updateURLsFilmlisten(boolean akt, boolean diff) {
        ListeFilmlistenUrls tmp = new ListeFilmlistenUrls();
        if (akt) {
            getDownloadUrlsFilmlisten(MSConst.ADRESSE_FILMLISTEN_SERVER_AKT, tmp, MSConfig.getUserAgent(), DatenFilmlisteUrl.SERVER_ART_AKT);
            if (tmp.isEmpty()) {
                getDownloadUrlsFilmlisten(MSConst.ADRESSE_FILMLISTEN_SERVER_AKT_RES, tmp, MSConfig.getUserAgent(), DatenFilmlisteUrl.SERVER_ART_AKT);
            }
            if (!tmp.isEmpty()) {
                listeFilmlistenUrls_akt = tmp;
            } else if (listeFilmlistenUrls_akt.isEmpty()) {
                listeFilmlistenUrls_akt.add(new DatenFilmlisteUrl("http://wp11128329.server-he.de/filme/Filmliste-akt.xz", DatenFilmlisteUrl.SERVER_ART_AKT));
                listeFilmlistenUrls_akt.add(new DatenFilmlisteUrl("http://wp11234018.server-he.de/filme/Filmliste-akt.xz", DatenFilmlisteUrl.SERVER_ART_AKT));
                listeFilmlistenUrls_akt.add(new DatenFilmlisteUrl("http://mv.mynews.de/filme/Filmliste-akt.xz", DatenFilmlisteUrl.SERVER_ART_AKT));
                listeFilmlistenUrls_akt.add(new DatenFilmlisteUrl("http://mv.hostingkunde.de/filme/Filmliste-akt.xz", DatenFilmlisteUrl.SERVER_ART_AKT));
                listeFilmlistenUrls_akt.add(new DatenFilmlisteUrl("http://mv-1.df-kunde.de/filme/Filmliste-akt.xz", DatenFilmlisteUrl.SERVER_ART_AKT));
                listeFilmlistenUrls_akt.add(new DatenFilmlisteUrl("http://mv-2.df-kunde.de/filme/Filmliste-akt.xz", DatenFilmlisteUrl.SERVER_ART_AKT));
                listeFilmlistenUrls_akt.add(new DatenFilmlisteUrl("http://mv-3.df-kunde.de/filme/Filmliste-akt.xz", DatenFilmlisteUrl.SERVER_ART_AKT));

                listeFilmlistenUrls_akt.add(new DatenFilmlisteUrl("http://hosting1735.af906.netcup.net/filme/Filmliste-akt.xz", DatenFilmlisteUrl.SERVER_ART_AKT));
                listeFilmlistenUrls_akt.add(new DatenFilmlisteUrl("http://hosting1766.af915.netcup.net/filme/Filmliste-akt.xz", DatenFilmlisteUrl.SERVER_ART_AKT));
                listeFilmlistenUrls_akt.add(new DatenFilmlisteUrl("http://hosting1767.af915.netcup.net/filme/Filmliste-akt.xz", DatenFilmlisteUrl.SERVER_ART_AKT));
                listeFilmlistenUrls_akt.add(new DatenFilmlisteUrl("http://mediathek.alfahosting.org/filme/Filmliste-akt.xz", DatenFilmlisteUrl.SERVER_ART_AKT));
                listeFilmlistenUrls_akt.add(new DatenFilmlisteUrl("http://mediathekview.alfahosting.org/filme/Filmliste-akt.xz", DatenFilmlisteUrl.SERVER_ART_AKT));
            }
            listeFilmlistenUrls_akt.sort();
        }
        if (diff) {
            getDownloadUrlsFilmlisten(MSConst.ADRESSE_FILMLISTEN_SERVER_DIFF, tmp, MSConfig.getUserAgent(), DatenFilmlisteUrl.SERVER_ART_DIFF);
            if (tmp.isEmpty()) {
                getDownloadUrlsFilmlisten(MSConst.ADRESSE_FILMLISTEN_SERVER_DIFF_RES, tmp, MSConfig.getUserAgent(), DatenFilmlisteUrl.SERVER_ART_DIFF);
            }
            if (!tmp.isEmpty()) {
                listeFilmlistenUrls_diff = tmp;
            } else if (listeFilmlistenUrls_diff.isEmpty()) {
                listeFilmlistenUrls_diff.add(new DatenFilmlisteUrl("http://wp11128329.server-he.de/filme/Filmliste-diff.xz", DatenFilmlisteUrl.SERVER_ART_DIFF));
                listeFilmlistenUrls_diff.add(new DatenFilmlisteUrl("http://wp11234018.server-he.de/filme/Filmliste-diff.xz", DatenFilmlisteUrl.SERVER_ART_DIFF));
                listeFilmlistenUrls_diff.add(new DatenFilmlisteUrl("http://mv.mynews.de/filme/Filmliste-diff.xz", DatenFilmlisteUrl.SERVER_ART_DIFF));
                listeFilmlistenUrls_diff.add(new DatenFilmlisteUrl("http://mv.hostingkunde.de/filme/Filmliste-diff.xz", DatenFilmlisteUrl.SERVER_ART_DIFF));
                listeFilmlistenUrls_diff.add(new DatenFilmlisteUrl("http://mv-1.df-kunde.de/filme/Filmliste-diff.xz", DatenFilmlisteUrl.SERVER_ART_DIFF));
                listeFilmlistenUrls_diff.add(new DatenFilmlisteUrl("http://mv-2.df-kunde.de/filme/Filmliste-diff.xz", DatenFilmlisteUrl.SERVER_ART_DIFF));
                listeFilmlistenUrls_diff.add(new DatenFilmlisteUrl("http://mv-3.df-kunde.de/filme/Filmliste-diff.xz", DatenFilmlisteUrl.SERVER_ART_DIFF));

                listeFilmlistenUrls_diff.add(new DatenFilmlisteUrl("http://hosting1735.af906.netcup.net/filme/Filmliste-diff.xz", DatenFilmlisteUrl.SERVER_ART_DIFF));
                listeFilmlistenUrls_diff.add(new DatenFilmlisteUrl("http://hosting1766.af915.netcup.net/filme/Filmliste-diff.xz", DatenFilmlisteUrl.SERVER_ART_DIFF));
                listeFilmlistenUrls_diff.add(new DatenFilmlisteUrl("http://hosting1767.af915.netcup.net/filme/Filmliste-diff.xz", DatenFilmlisteUrl.SERVER_ART_DIFF));
                listeFilmlistenUrls_diff.add(new DatenFilmlisteUrl("http://mediathek.alfahosting.org/filme/Filmliste-diff.xz", DatenFilmlisteUrl.SERVER_ART_DIFF));
                listeFilmlistenUrls_diff.add(new DatenFilmlisteUrl("http://mediathekview.alfahosting.org/filme/Filmliste-diff.xz", DatenFilmlisteUrl.SERVER_ART_DIFF));
            }
            listeFilmlistenUrls_diff.sort();
        }
        if (tmp.isEmpty()) {
            MSLog.systemMeldung(new String[]{"Es ist ein Fehler aufgetreten!",
                "Es konnten keine Updateserver zum aktualisieren der Filme",
                "gefunden werden."});
        }
    }

    public static void getDownloadUrlsFilmlisten(String dateiUrl, ListeFilmlistenUrls listeFilmlistenUrls, String userAgent, String art) {
        //String[] ret = new String[]{""/* version */, ""/* release */, ""/* updateUrl */};
        try {
            int event;
            XMLInputFactory inFactory = XMLInputFactory.newInstance();
            inFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
            XMLStreamReader parser;
            InputStreamReader inReader;
            if (MSFunktionen.istUrl(dateiUrl)) {
                // eine URL verarbeiten
                int timeout = 20000; //ms
                URLConnection conn;
                conn = new URL(dateiUrl).openConnection();
                conn.setRequestProperty("User-Agent", userAgent);
                conn.setReadTimeout(timeout);
                conn.setConnectTimeout(timeout);
                inReader = new InputStreamReader(conn.getInputStream(), MSConst.KODIERUNG_UTF);
            } else {
                // eine Datei verarbeiten
                File f = new File(dateiUrl);
                if (!f.exists()) {
                    return;
                }
                inReader = new InputStreamReader(new FileInputStream(f), MSConst.KODIERUNG_UTF);
            }
            parser = inFactory.createXMLStreamReader(inReader);
            while (parser.hasNext()) {
                event = parser.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    String parsername = parser.getLocalName();
                    if (parsername.equals("Server")) {
                        //wieder ein neuer Server, toll
                        getServer(parser, listeFilmlistenUrls, art);
                    }
                }
            }
        } catch (Exception ex) {
            MSLog.fehlerMeldung(821069874, MSLog.FEHLER_ART_PROG, MSFilmlistenSuchen.class.getName(), ex, "Die URL-Filmlisten konnte nicht geladen werden: " + dateiUrl);
        }
    }

    private static void getServer(XMLStreamReader parser, ListeFilmlistenUrls listeFilmlistenUrls, String art) {
        String zeit = "";
        String datum = "";
        String serverUrl = "";
        String prio = "";
        int event;
        try {
            while (parser.hasNext()) {
                event = parser.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    //parsername = parser.getLocalName();
                    switch (parser.getLocalName()) {
                        case "URL":
                            serverUrl = parser.getElementText();
                            break;
                        case "Prio":
                            prio = parser.getElementText();
                            break;
                        case "Datum":
                            datum = parser.getElementText();
                            break;
                        case "Zeit":
                            zeit = parser.getElementText();
                            break;
                    }
                }
                if (event == XMLStreamConstants.END_ELEMENT) {
                    //parsername = parser.getLocalName();
                    if (parser.getLocalName().equals("Server")) {
                        if (!serverUrl.equals("")) {
                            //public DatenFilmUpdate(String url, String prio, String zeit, String datum, String anzahl) {
                            if (prio.equals("")) {
                                prio = DatenFilmlisteUrl.FILM_UPDATE_SERVER_PRIO_1;
                            }
                            listeFilmlistenUrls.addWithCheck(new DatenFilmlisteUrl(serverUrl, prio, art));
                        }
                        break;
                    }
                }
            }
        } catch (XMLStreamException ex) {
        }

    }

    public static File ListeFilmlistenSchreiben(ListeFilmlistenUrls listeFilmlistenUrls) {
        File tmpFile = null;
        XMLOutputFactory outFactory;
        XMLStreamWriter writer;
        OutputStreamWriter out;
        final String TAG_LISTE = "Mediathek";
        final String TAG_SERVER = "Server";
        final String TAG_SERVER_URL = "URL";
        final String TAG_SERVER_DATUM = "Datum";
        final String TAG_SERVER_ZEIT = "Zeit";
        try {
            tmpFile = File.createTempFile("mediathek", null);
            tmpFile.deleteOnExit();
            outFactory = XMLOutputFactory.newInstance();
            out = new OutputStreamWriter(new FileOutputStream(tmpFile), MSConst.KODIERUNG_UTF);
            writer = outFactory.createXMLStreamWriter(out);
            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeCharacters("\n");//neue Zeile
            writer.writeStartElement(TAG_LISTE);
            writer.writeCharacters("\n");//neue Zeile
            Iterator<DatenFilmlisteUrl> it = listeFilmlistenUrls.iterator();
            while (it.hasNext()) {
                DatenFilmlisteUrl d = it.next();
                writer.writeStartElement(TAG_SERVER);
                writer.writeCharacters("\n");
                // Tags schreiben: URL
                writer.writeCharacters("\t");// Tab
                writer.writeStartElement(TAG_SERVER_URL);
                writer.writeCharacters(d.arr[DatenFilmlisteUrl.FILM_UPDATE_SERVER_URL_NR]);
                writer.writeEndElement();
                writer.writeCharacters("\n");
                // fertig
                // Tags schreiben: Datum
                writer.writeCharacters("\t");// Tab
                writer.writeStartElement(TAG_SERVER_DATUM);
                writer.writeCharacters(d.arr[DatenFilmlisteUrl.FILM_UPDATE_SERVER_DATUM_NR]);
                writer.writeEndElement();
                writer.writeCharacters("\n");
                // fertig
                // Tags schreiben: Zeit
                writer.writeCharacters("\t");// Tab
                writer.writeStartElement(TAG_SERVER_ZEIT);
                writer.writeCharacters(d.arr[DatenFilmlisteUrl.FILM_UPDATE_SERVER_ZEIT_NR]);
                writer.writeEndElement();
                writer.writeCharacters("\n");
                // fertig
                writer.writeEndElement();
                writer.writeCharacters("\n");
            }
            // Schließen
            writer.writeEndElement();
            writer.writeEndDocument();
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            MSLog.fehlerMeldung(634978521, MSLog.FEHLER_ART_PROG, MSFilmlistenSuchen.class.getName(), ex, "Die URL-Filmlisten konnten nicht geschrieben werden");
        }
        return tmpFile;
    }
}
