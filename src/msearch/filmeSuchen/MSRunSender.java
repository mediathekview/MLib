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
    public int max = 0;
    public int progress = 0;
    public Date startZeit = new Date();
    public boolean fertig = false;

    public MSRunSender(String ssender, int mmax, int pprogress) {
        sender = ssender;
        max = mmax;
        progress = pprogress;
    }

    public String getLaufzeitMinuten() {
        Date endZeit = new Date();
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
            MSLog.fehlerMeldung(976431583, MSLog.FEHLER_ART_PROG, "Suchen.RunSender.getLaufzeitMinuten", ex, sender);
        }
        return ret;
    }

    public int getLaufzeitSekunden() {
        Date endZeit = new Date();
        int sekunden = 0;
        try {
            if (startZeit != null) {
                sekunden = Math.round((endZeit.getTime() - startZeit.getTime()) / 1000);
            }
        } catch (Exception ex) {
            MSLog.fehlerMeldung(976431583, MSLog.FEHLER_ART_PROG, "Suchen.RunSender.getLaufzeitMinuten", ex, sender);
        }
        return sekunden;
    }
}
