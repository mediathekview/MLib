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

public class Config {

    public static int bandbreite = 0; // maxBandbreite in Byte
    private static String userAgent = null;
    public static boolean debug = false; // Debugmodus
    private static boolean stop = false; // damit kannn das Laden gestoppt werden

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
