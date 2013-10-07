/*
 * MediathekView
 * Copyright (C) 2013 W. Xaver
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
package msearch;

import msearch.tool.MSearchLog;

public class Main {

    public static final String STARTP_ALLES = "-alles";
    public static final String STARTP_UPDATE = "-update";
    public static final String STARTP_USER_AGENT = "-agent";
    public static final String STARTP_DATEI_FILMLISTE = "-filmliste";
    public static final String STARTP_SENDER = "-sender";
    public static final String STARTP_DEBUG = "-d";

    /*
     * Aufruf:
     * java -jar Mediathek [Pfad zur Konfigdatei, sonst homeverzeichnis] [Schalter]
     *
     * Programmschalter:
     * -alles       die Filmliste wird komplett gesucht, sons nur die aktuellsten Filme
     * -update      die Filmliste wird aktualisiert, sonst vorher geleert
     * -agent       der verwendete UserAgent bei http-Aufrufen
     * -filmliste   Datei mit der Filmliste (wird gelesen und das Ergebnis dort gespeichert
     * -sender      es wird nur der eine Sender aktualisiert
     * -D Debugmode
     *
     * */
    public Main() {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(final String args[]) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (args != null) {
                    for (String s : args) {
                        if (s.equalsIgnoreCase("-v")) {
                            MSearchLog.versionsMeldungen(this.getClass().getName());
                            System.exit(0);
                        }
                    }
                }
                new Search(args).starten();
            }
        });
    }
}
