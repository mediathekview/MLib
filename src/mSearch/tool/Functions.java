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
package mSearch.tool;

import com.jidesoft.utils.SystemInfo;
import java.io.File;
import java.security.CodeSource;
import java.util.ResourceBundle;
import mSearch.Main;
import mSearch.daten.DatenFilm;
import org.apache.commons.lang3.StringEscapeUtils;

public class Functions {

    public enum OperatingSystemType {

        UNKNOWN(""), WIN32("Windows"), WIN64("Windows"), LINUX("Linux"), MAC("Mac");
        private final String name;

        OperatingSystemType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Detect and return the currently used operating system.
     *
     * @return The enum for supported Operating Systems.
     */
    public static OperatingSystemType getOs() {
        OperatingSystemType os = OperatingSystemType.UNKNOWN;

        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            if (System.getenv("ProgramFiles(x86)") != null) {
                // win 64Bit
                os = OperatingSystemType.WIN64;
            } else if (System.getenv("ProgramFiles") != null) {
                // win 32Bit
                os = OperatingSystemType.WIN32;
            }

        } else if (SystemInfo.isLinux()) {
            os = OperatingSystemType.LINUX;
        } else if (System.getProperty("os.name").toLowerCase().contains("freebsd")) {
            os = OperatingSystemType.LINUX;

        } else if (SystemInfo.isMacOSX()) {
            os = OperatingSystemType.MAC;
        }
        return os;
    }

    public static String getOsString() {
        return getOs().toString();
    }

    public static String getPathJar() {
        // liefert den Pfad der Programmdatei mit File.separator am Schluss
        String pFilePath = "version.properties";
        File propFile = new File(pFilePath);
        if (!propFile.exists()) {
            try {
                CodeSource cS = Main.class.getProtectionDomain().getCodeSource();
                File jarFile = new File(cS.getLocation().toURI().getPath());
                String jarDir = jarFile.getParentFile().getPath();
                propFile = new File(jarDir + File.separator + pFilePath);
            } catch (Exception ignored) {
            }
        }else{
            DebugMsg.print("getPath");
        }
        String s = propFile.getAbsolutePath().replace(pFilePath, "");
        if (!s.endsWith(File.separator)) {
            s = s + File.separator;
        }
        return s;
    }

    public static String getProgVersionString() {
        return " [Rel: " + getBuildNr() + "]";
    }

    public static String[] getJavaVersion() {
        String[] ret = new String[4];
        int i = 0;
        ret[i++] = "Vendor: " + System.getProperty("java.vendor");
        ret[i++] = "VMname: " + System.getProperty("java.vm.name");
        ret[i++] = "Version: " + System.getProperty("java.version");
        ret[i++] = "Runtimeversion: " + System.getProperty("java.runtime.version");
        return ret;
    }

    public static String getCompileDate() {
        final ResourceBundle rb;
        String propToken = "DATE";
        String msg = "";
        try {
            ResourceBundle.clearCache();
            rb = ResourceBundle.getBundle("version");
            msg = rb.getString(propToken);
        } catch (Exception e) {
            Log.fehlerMeldung(807293847, e);
        }
        return msg;
    }

    public static String getBuildNr() {
        final ResourceBundle rb;
        String propToken = "BUILD";
        String msg = "";
        try {
            ResourceBundle.clearCache();
            rb = ResourceBundle.getBundle("version");
            msg = rb.getString(propToken);
        } catch (Exception e) {
            Log.fehlerMeldung(134679898, e);
        }
        return msg;
    }

    public static void unescape(DatenFilm film) {
        film.arr[DatenFilm.FILM_THEMA_NR] = StringEscapeUtils.unescapeXml(film.arr[DatenFilm.FILM_THEMA_NR].trim());
        film.arr[DatenFilm.FILM_THEMA_NR] = StringEscapeUtils.unescapeHtml4(film.arr[DatenFilm.FILM_THEMA_NR].trim());

        // Beschreibung
        film.arr[DatenFilm.FILM_BESCHREIBUNG_NR] = StringEscapeUtils.unescapeXml(film.arr[DatenFilm.FILM_BESCHREIBUNG_NR].trim());
        film.arr[DatenFilm.FILM_BESCHREIBUNG_NR] = StringEscapeUtils.unescapeHtml4(film.arr[DatenFilm.FILM_BESCHREIBUNG_NR].trim());
        film.arr[DatenFilm.FILM_BESCHREIBUNG_NR] = removeHtml(film.arr[DatenFilm.FILM_BESCHREIBUNG_NR]);

        // Titel
        film.arr[DatenFilm.FILM_TITEL_NR] = StringEscapeUtils.unescapeXml(film.arr[DatenFilm.FILM_TITEL_NR].trim());
        film.arr[DatenFilm.FILM_TITEL_NR] = StringEscapeUtils.unescapeHtml4(film.arr[DatenFilm.FILM_TITEL_NR].trim());
    }

//    public static String utf8(String ret) {
//        ret = ret.replace("\\u0026", "&");
//        ret = ret.replace("\\u003C", "<");
//        ret = ret.replace("\\u003c", "<");
//        ret = ret.replace("\\u003E", ">");
//        ret = ret.replace("\\u003e", ">");
//        ret = ret.replace("\\u00E4", "ä");
//        ret = ret.replace("\\u00e4", "ä");
//        ret = ret.replace("\\u00C4", "Ä");
//        ret = ret.replace("\\u00c4", "Ä");
//        ret = ret.replace("\\u00F6", "ö");
//        ret = ret.replace("\\u00f6", "ö");
//        ret = ret.replace("\\u00D6", "Ö");
//        ret = ret.replace("\\u00d6", "Ö");
//        ret = ret.replace("\\u00FC", "ü");
//        ret = ret.replace("\\u00fc", "ü");
//        ret = ret.replace("\\u00DC", "Ü");
//        ret = ret.replace("\\u00dc", "Ü");
//        ret = ret.replace("\\u00DF", "ß");
//        ret = ret.replace("\\u00df", "ß");
//        ret = ret.replace("\\u20AC", "€");
//        ret = ret.replace("\\u20ac", "€");
//        ret = ret.replace("\\u0024", "$");
//        ret = ret.replace("\\u00A3", "£");
//        ret = ret.replace("\\u00a3", "£");
//        ret = ret.replace("\\u00F3", "\u00f3");
//        ret = ret.replace("\\u00f3", "\u00f3");
//        return ret;
//    }

    public static String addsPfad(String pfad1, String pfad2) {
        String ret = "";
        if (pfad1 != null && pfad2 != null) {
            if (pfad1.isEmpty()) {
                ret = pfad2;
            } else if (pfad2.isEmpty()) {
                ret = pfad1;
            } else if (!pfad1.equals("") && !pfad2.equals("")) {
                if (pfad1.endsWith(File.separator)) {
                    ret = pfad1.substring(0, pfad1.length() - 1);
                } else {
                    ret = pfad1;
                }
                if (pfad2.charAt(0) == File.separatorChar) {
                    ret += pfad2;
                } else {
                    ret += File.separator + pfad2;
                }
            }
        }
        if (ret.equals("")) {
            Log.fehlerMeldung(283946015, pfad1 + " - " + pfad2);
        }
        return ret;
    }

    public static String addUrl(String u1, String u2) {
        if (u1.endsWith("/")) {
            return u1 + u2;
        } else {
            return u1 + "/" + u2;
        }
    }

    public static boolean istUrl(String dateiUrl) {
        //return dateiUrl.startsWith("http") ? true : false || dateiUrl.startsWith("www") ? true : false;
        return dateiUrl.startsWith("http") || dateiUrl.startsWith("www");
    }

    public static String getDateiName(String pfad) {
        //Dateinamen einer URL extrahieren
        String ret = "";
        if (pfad != null) {
            if (!pfad.equals("")) {
                ret = pfad.substring(pfad.lastIndexOf("/") + 1);
            }
        }
        if (ret.contains("?")) {
            ret = ret.substring(0, ret.indexOf("?"));
        }
        if (ret.contains("&")) {
            ret = ret.substring(0, ret.indexOf("&"));
        }
        if (ret.equals("")) {
            Log.fehlerMeldung(395019631, pfad);
        }
        return ret;
    }

    public static String removeHtml(String in) {
        return in.replaceAll("\\<.*?>", "");
    }
}
