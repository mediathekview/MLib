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

import msearch.tool.MSearchConst;

public class MSearchConfig {

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
    public static String exportFilmlisteXml = ""; // Filmliste wird nach dem Suchen noch in die Datei exportiert (Format: XML), bz2
    public static String exportFilmlisteJson = ""; // Filmliste wird nach dem Suchen noch in die Datei exportiert (Format: Json), xz
    public static boolean orgFilmlisteErstellen = false; // dann wird eine neue Org-Liste angelegt, typ. die erste Liste am Tag
    public static String orgFilmliste = ""; // ist die Org-Filmliste, typ. die erste am Tag
    public static String exportOrgFilmliste = ""; // die Org-Filmliste wird nach dem Suchen noch in die Datei exportiert (Format: Json), xz
    public static String diffFilmliste = ""; // ist das diff, Filmliste gegen Org-Liste
    public static String exportDiffFilmliste = ""; // und die wird noch exportiert, xz
    //
    private static String userAgent = null;
    // flags
    public static boolean debug = false; // Debugmodus
    // Verzeichnis zum Speichern der Programmeinstellungen
    public static String dateiFilmliste = "";
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static boolean stop = false; // damit kannn das Laden gestoppt werden

    public static void setUserAgent(String ua) {
        // Useragent den der Benutzer vorgegeben hat
        userAgent = ua;
    }

    public static String getUserAgent() {
        if (userAgent == null) {
            return MSearchConst.USER_AGENT_DEFAULT;
        } else {
            return userAgent;
        }
    }

    public static String getUserAgent_dynamic() {
        int zufall = 1 + (int) (Math.random() * 10000); // 1 - 10000
        //String user = " user-" + zufall;
        if (userAgent == null) {
            return MSearchConst.USER_AGENT_DEFAULT + " user-" + zufall;
        } else {
            return userAgent + " user-" + zufall;
        }
    }

    /**
     * Damit kann das Suchen abgebrochen werden
     */
    public static synchronized void setStop() {
        MSearchConfig.stop = true;
    }

    /**
     * Damit kann "stop" gesetzt/rückgesetzt werden
     */
    public static synchronized void setStop(boolean set) {
        MSearchConfig.stop = set;
    }

    /**
     * Abfrage, ob ein Abbruch erfogte
     *
     * @return true/false
     */
    public static synchronized boolean getStop() {
        return MSearchConfig.stop;
    }
}
