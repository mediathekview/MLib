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

import mSearch.tool.Functions;

public class Const {

    public static final String VERSION = "13";
    public static final String VERSION_FILMLISTE = "3";
    public static final String PROGRAMMNAME = "MSearch";
    public static final String USER_AGENT_DEFAULT = Const.PROGRAMMNAME + Functions.getProgVersionString();
    // MediathekView URLs
    public static final String ADRESSE_FILMLISTEN_SERVER_XML = "http://zdfmediathk.sourceforge.net/update.xml";
    public static final String ADRESSE_FILMLISTEN_SERVER_JSON = "http://zdfmediathk.sourceforge.net/update-json.xml";
    public static final String ADRESSE_FILMLISTEN_SERVER_DIFF = "http://zdfmediathk.sourceforge.net/diff.xml";
    public static final String ADRESSE_FILMLISTEN_SERVER_AKT = "http://zdfmediathk.sourceforge.net/akt.xml";
    public static final String ADRESSE_FILMLISTEN_SERVER_DIFF_RES = "http://92.51.131.172/diff.xml";
    public static final String ADRESSE_FILMLISTEN_SERVER_AKT_RES = "http://92.51.131.172/akt.xml";

//    public static final String DATEINAME_LISTE_FILMLISTEN = "filmlisten.xml";
    public static final String ADRESSE_PROGRAMM_VERSION = "http://zdfmediathk.sourceforge.net/version.xml";
    public static final String ADRESSE_DOWNLAD = "http://sourceforge.net/projects/zdfmediathk/";
    public static final String ADRESSE_ANLEITUNG = "http://sourceforge.net/p/zdfmediathk/wiki/Home/";
    public static final String ADRESSE_VORLAGE_PROGRAMMGRUPPEN = "http://zdfmediathk.sourceforge.net/programmgruppen/programmgruppen.xml";
    public static final String ADRESSE_WEBSITE = "http://zdfmediathk.sourceforge.net/";
    public static final String ADRESSE_FORUM = "http://sourceforge.net/apps/phpbb/zdfmediathk/";
    // Dateien/Verzeichnisse
    public static final String XML_DATEI = "mediathek.xml";
    public static final String XML_DATEI_FILME = "filme.xml";
    // 
    public static final int MIN_DATEI_GROESSE_FILM = 256 * 1000; //minimale Größe (256 kB) eines Films um nicht als Fehler zu gelten
    public static final String KODIERUNG_UTF = "UTF-8";
    public static final String KODIERUNG_ISO15 = "ISO-8859-15";
    public static final String XML_START = "Mediathek";
    public static final int MAX_SENDER_FILME_LADEN = 2;//es können maximal soviele Filme eines Senders/Servers gleichzeitig geladen werden
    public static final int STRING_BUFFER_START_BUFFER = 8 * 1024 * 8; // 8 KiB
    public static final String FORMAT_ZIP = ".zip";
    public static final String FORMAT_XZ = ".xz";
    public static final String FORMAT_JSON = ".json";
    public static final String RTMP_PRTOKOLL = "rtmp";
    public static final String RTMP_FLVSTREAMER = "-r ";
    public static final int ALTER_FILMLISTE_SEKUNDEN_FUER_AUTOUPDATE = 3 * 60 * 60; // beim Start des Programms wir die Liste geladen wenn sie älter ist als ..
    public static final String TIME_MAX_AGE_FOR_DIFF = "09"; // Uhrzeit ab der die Diffliste alle Änderungen abdeckt, die Filmliste darf also nicht vor xx erstellt worden sein
    public static final int MAX_BESCHREIBUNG = 400; // max länge der Beschreibung in Zeichen -> mehr gibts aber jetzt nicht mehr!
}
