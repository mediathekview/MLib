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
package msearch.daten;

import msearch.tool.DatumZeit;
import msearch.tool.MSConst;
import msearch.tool.MSGuiFunktionen;

public class MSConfig {

    //alle Programmeinstellungen
    public static String proxyUrl = "";
    public static int proxyPort = -1;
    public static int warten = 1;
    public static int bandbreite = 0; // maxBandbreite in Byte
    public static String importUrl__anhaengen = "";
    public static String importUrl__ersetzen = "";
    public static boolean senderAllesLaden = false;
    public static boolean updateFilmliste = false; // die bestehende Filmliste wird aktualisiert und bleibt erhalten
    public static String[] nurSenderLaden = null; // es wird nur dieser Sender geladen => "senderAllesLaden"=false, "updateFillmliste"=true
//    public static String exportFilmlisteXml = ""; // Filmliste wird nach dem Suchen noch in die Datei exportiert (Format: XML), bz2
//    public static String exportFilmlisteJson = ""; // Filmliste wird nach dem Suchen noch in die Datei exportiert (Format: Json), xz
    public static boolean orgFilmlisteErstellen = false; // dann wird eine neue Org-Liste angelegt, typ. die erste Liste am Tag
    public static boolean diffFilmlisteErstellen = false; // dann wird ein diff erstellt
    //
    private static String userAgent = null;
    // flags
    public static boolean debug = false; // Debugmodus
    // Verzeichnis zum Speichern der Programmeinstellungen
    public static String dirFilme = ""; // Pfad mit den Filmlisten
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static boolean stop = false; // damit kannn das Laden gestoppt werden

    public static void setUserAgent(String ua) {
        // Useragent den der Benutzer vorgegeben hat
        userAgent = ua;
    }

    public static String getUserAgent() {
        if (userAgent == null) {
            return MSConst.USER_AGENT_DEFAULT;
        } else {
            return userAgent;
        }
    }

    public static String getUserAgent_dynamic() {
        int zufall = 1 + (int) (Math.random() * 10000); // 1 - 10000
        //String user = " user-" + zufall;
        if (userAgent == null) {
            return MSConst.USER_AGENT_DEFAULT + " user-" + zufall;
        } else {
            return userAgent + " user-" + zufall;
        }
    }

    // Namen der Filmlisten
    public static final String nameAktFilmlist = "filme.json"; // ist die aktuelle Filmliste
    public static final String nameAktFilmlist_xz = "filme.xz"; // ist die aktuelle Filmliste, xz komprimiert
    public static final String nameAktFilmlist_bz2 = "filme-xml.bz2"; // ist die aktuelle Filmliste (xml Format), bz2 komprimiert, für die Programmversion <4

    public static final String nameOrgFilmlist = "filme-org.json"; // ist die "ORG" Filmliste, typ. die erste am Tag
    public static final String nameOrgFilmlist_xz = "filme-org.xz"; // ist die "ORG" Filmliste, typ. die erste am Tag, xz komprimiert

    public static final String nameDiffFilmlist = "filme-diff.json"; // ist ein diff der aktuellen zur ORG Filmliste
    public static final String nameDiffFilmlist_xz = "filme-diff.xz"; // ist ein diff der aktuellen zur ORG Filmliste, xz komprimiert

    public static String getPathFilmlist(boolean aktDate) {
        if (aktDate) {
            return MSGuiFunktionen.addsPfad(dirFilme, DatumZeit.getJetzt_yyyy_MM_dd__HH_mm_ss() + "__" + nameAktFilmlist);
        } else {
            return MSGuiFunktionen.addsPfad(dirFilme, nameAktFilmlist);
        }
    }

    public static String getPathFilmlist_json_xz() {
        return MSGuiFunktionen.addsPfad(dirFilme, nameAktFilmlist_xz);
    }

    public static String getPathFilmlist_xml_bz2() {
        return MSGuiFunktionen.addsPfad(dirFilme, nameAktFilmlist_bz2);
    }

    public static String getPathFilmlist_org() {
        return MSGuiFunktionen.addsPfad(dirFilme, nameOrgFilmlist);
    }

    public static String getPathFilmlist_org_xz() {
        return MSGuiFunktionen.addsPfad(dirFilme, nameOrgFilmlist_xz);
    }

    public static String getPathFilmlist_diff() {
        return MSGuiFunktionen.addsPfad(dirFilme, nameDiffFilmlist);
    }

    public static String getPathFilmlist_diff_xz() {
        return MSGuiFunktionen.addsPfad(dirFilme, nameDiffFilmlist_xz);
    }

    /**
     * Damit kann das Suchen abgebrochen werden
     */
    public static synchronized void setStop() {
        MSConfig.stop = true;
    }

    /**
     * Damit kann "stop" gesetzt/rückgesetzt werden
     */
    public static synchronized void setStop(boolean set) {
        MSConfig.stop = set;
    }

    /**
     * Abfrage, ob ein Abbruch erfogte
     *
     * @return true/false
     */
    public static synchronized boolean getStop() {
        return MSConfig.stop;
    }
}
