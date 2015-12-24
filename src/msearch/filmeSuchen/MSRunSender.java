/*
 * MediathekView
 * Copyright (C) 2011 W. Xaver
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
package msearch.filmeSuchen;

import java.util.Date;
import msearch.tool.MSLog;

public class MSRunSender {

    public String sender = "";
    public int max = 0; // max laden
    public int progress = 0;
    public Date startZeit = new Date();
    public Date endZeit = new Date(); // zur Sicherheit

    public boolean fertig = false;
    long[] counter = new long[Count.values().length];

    public static enum Count {

        ANZAHL("Anzahl"), FILME("Filme"), FEHLER("Fehler"), FEHLVERSUCHE("FVersuche"), WARTEZEIT_FEHLVERSUCHE("Zeit-FV" /*ms*/),
        PROXY("Proxy" /*Seite über Proxy laden*/), NO_BUFFER("NoBuffer"),
        RUN_MAX_THREAD("Threads" /*Sender mit Anz. Threads laden*/), WAIT_ON_LOAD("wait"/*Wartezeit bei jedem Laden einer Seite*/),
        SUM_DATA_BYTE("sum-data" /*Datenmenge, entpackt*/), SUM_TRAFFIC_BYTE("sum-traffic" /*Datenmenge die übertragen wird*/),
        SUM_TRAFFIC_LOADART_NIX("sum-tr-nix"), SUM_TRAFFIC_LOADART_DEFLATE("sum-tr-deflate"), SUM_TRAFFIC_LOADART_GZIP("sum-tr-gzip"),
        SIZE_SUM("size-sum"), SIZE_SUM403("size-403"), SIZE_PROXY("size-proxy");

        final String name;

        private Count(String name) {
            this.name = name;
        }

        public static String[] getNames() {
            String[] ret = new String[values().length];

            for (int i = 0; i < values().length; ++i) {
                ret[i] = values()[i].name;
            }
            return ret;
        }
    }

    public MSRunSender(String sender, int max, int progress) {
        this.sender = sender;
        this.max = max;
        this.progress = progress;
    }

    public String getLaufzeitMinuten() {
        String ret = "";
        int sekunden;
        try {
            if (startZeit != null) {
                sekunden = Math.round((endZeit.getTime() - startZeit.getTime()) / 1000);
                String min = String.valueOf(sekunden / 60);
                String sek = String.valueOf(sekunden % 60);
                if (sek.length() == 1) {
                    sek = "0" + sek;
                }
                ret = min + ":" + sek;
            }
        } catch (Exception ex) {
            MSLog.fehlerMeldung(976431583, ex, sender);
        }
        return ret;
    }

    public int getLaufzeitSekunden() {
        int sekunden = 0;
        try {
            if (startZeit != null && endZeit != null) {
                sekunden = Math.round((endZeit.getTime() - startZeit.getTime()) / 1000);
            }
        } catch (Exception ex) {
            MSLog.fehlerMeldung(976431583, ex, sender);
        }
        return sekunden;
    }

    public static synchronized String getStringZaehler(long z) {
        String ret = z == 0 ? "0" : ((z / 1000 / 1000) == 0 ? "<1" : String.valueOf(z / 1000 / 1000));
        return ret;
    }
}
