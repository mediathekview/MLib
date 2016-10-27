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
package mSearch;

import java.text.SimpleDateFormat;
import java.util.Date;
import mSearch.tool.Functions;

public class Config {

    //alle Programmeinstellungen
    public static String proxyUrl = "";
    public static int proxyPort = -1;
    public static int warten = 1;
    public static int bandbreite = 0; // maxBandbreite in Byte
    public static String importUrl_1__anhaengen = "";
    public static String importUrl_2__anhaengen = "";
    public static String importOld = ""; // alte Liste importieren
    public static String importLive = ""; // live-streams

    public static final int LOAD_SHORT = 0;
    public static final int LOAD_LONG = 1;
    public static final int LOAD_MAX = 2;
    public static int senderLoadHow = LOAD_SHORT;

    public static boolean updateFilmliste = false; // die bestehende Filmliste wird aktualisiert und bleibt erhalten
    public static String[] nurSenderLaden = null; // es wird nur dieser Sender geladen => "senderAllesLaden"=false, "updateFillmliste"=true
    public static String orgFilmliste = ""; // OrgFilmliste, zum Erstellen des Diff, angelegt wird sie immer im Ordner der Filmlisten, wenn leer wird die eigene Org-Liste gesucht
    public static boolean orgFilmlisteErstellen = false; // dann wird eine neue Org-Liste angelegt, typ. die erste Liste am Tag
    //
    private static String userAgent = null;
    // flags
    public static boolean debug = false; // Debugmodus
    // Verzeichnis zum Speichern der Programmeinstellungen
    public static String dirFilme = ""; // Pfad mit den Filmlisten
    //public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static boolean stop = false; // damit kannn das Laden gestoppt werden

    // Namen der Filmlisten im: Konfig-Ordner/filmlisten/
    public static final String nameAktFilmlist = "filme.json"; // ist die aktuelle Filmliste
    public static final String nameAktFilmlist_xz = "filme.xz"; // ist die aktuelle Filmliste, xz komprimiert

    public static final String nameOrgFilmlist = "filme-org.json"; // ist die "ORG" Filmliste, typ. die erste am Tag
    public static final String nameOrgFilmlist_xz = "filme-org.xz"; // ist die "ORG" Filmliste, typ. die erste am Tag, xz komprimiert

    public static final String nameDiffFilmlist = "filme-diff.json"; // ist ein diff der aktuellen zur ORG Filmliste
    public static final String nameDiffFilmlist_xz = "filme-diff.xz"; // ist ein diff der aktuellen zur ORG Filmliste, xz komprimiert

    public static void setUserAgent(String ua) {
        // Useragent den der Benutzer vorgegeben hat
        userAgent = ua;
    }

    public static String getUserAgent() {
        if (userAgent == null) {
            return Const.USER_AGENT_DEFAULT;
        } else {
            return userAgent;
        }
    }

    public static boolean loadShort() {
        return senderLoadHow == LOAD_SHORT;
    }

    public static boolean loadLong() {
        return senderLoadHow == LOAD_LONG;
    }

    public static boolean loadLongMax() {
        return senderLoadHow >= LOAD_LONG;
    }

    public static boolean loadMax() {
        return senderLoadHow == LOAD_MAX;
    }

    /*public static String getUserAgent_dynamic() {
     int zufall = 1 + (int) (Math.random() * 10000); // 1 - 10000
     //String user = " user-" + zufall;
     if (userAgent == null) {
     return MSConst.USER_AGENT_DEFAULT + " user-" + zufall;
     } else {
     return userAgent + " user-" + zufall;
     }
     }*/
    public static String getPathFilmlist_json_akt(boolean aktDate) {
        if (aktDate) {
            return Functions.addsPfad(dirFilme, new SimpleDateFormat("yyyy.MM.dd__HH.mm.ss").format(new Date()) + "__" + nameAktFilmlist);
        } else {
            return Functions.addsPfad(dirFilme, nameAktFilmlist);
        }
    }

    public static String getPathFilmlist_json_akt_xz() {
        return Functions.addsPfad(dirFilme, nameAktFilmlist_xz);
    }

    public static String getPathFilmlist_json_org() {
        return Functions.addsPfad(dirFilme, nameOrgFilmlist);
    }

    public static String getPathFilmlist_json_org_xz() {
        return Functions.addsPfad(dirFilme, nameOrgFilmlist_xz);
    }

    public static String getPathFilmlist_json_diff() {
        return Functions.addsPfad(dirFilme, nameDiffFilmlist);
    }

    public static String getPathFilmlist_json_diff_xz() {
        return Functions.addsPfad(dirFilme, nameDiffFilmlist_xz);
    }

    /**
     * Damit kann das Suchen abgebrochen werden
     */
    public static synchronized void setStop() {
        Config.stop = true;
    }

    /**
     * Damit kann "stop" gesetzt/rückgesetzt werden
     *
     * @param set
     */
    public static synchronized void setStop(boolean set) {
        Config.stop = set;
    }

    /**
     * Abfrage, ob ein Abbruch erfogte
     *
     * @return true/false
     */
    public static synchronized boolean getStop() {
        return Config.stop;
    }
}
