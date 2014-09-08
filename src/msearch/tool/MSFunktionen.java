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
package msearch.tool;

import java.io.File;
import java.security.CodeSource;
import java.util.ResourceBundle;
import msearch.Main;
import msearch.daten.DatenFilm;
import org.apache.commons.lang3.StringEscapeUtils;

public class MSFunktionen {

    public static final int OS_UNKNOWN = 0;
    public static final int OS_WIN_32BIT = 1;
    public static final int OS_WIN_64BIT = 2;
    public static final int OS_LINUX = 3;
    public static final int OS_MAC = 4;
    public static final String OS_UNKNOWN_STRING = "";
    public static final String OS_WIN_32BIT_STRING = "Windows";
    public static final String OS_WIN_64BIT_STRING = "Windows";
    public static final String OS_LINUX_STRING = "Linux";
    public static final String OS_MAC_STRING = "Mac";

    public static int getOs() {
        int os = OS_UNKNOWN;
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            if (System.getenv("ProgramFiles") != null) {
                // win 32Bit
                os = OS_WIN_32BIT;
            } else if (System.getenv("ProgramFiles(x86)") != null) {
                // win 64Bit
                os = OS_WIN_64BIT;
            }
        } else if (System.getProperty("os.name").toLowerCase().contains("linux")) {
            os = OS_LINUX;
        } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            os = OS_MAC;
        }
        return os;
    }

    public static String getOsString() {
        String os = OS_UNKNOWN_STRING;
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            if (System.getenv("ProgramFiles") != null) {
                // win 32Bit
                os = OS_WIN_32BIT_STRING;
            } else if (System.getenv("ProgramFiles(x86)") != null) {
                // win 64Bit
                os = OS_WIN_64BIT_STRING;
            }
        } else if (System.getProperty("os.name").toLowerCase().contains("linux")) {
            os = OS_LINUX_STRING;
        } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            os = OS_MAC_STRING;
        }
        return os;
    }

    public static String getPathJar() {
        // liefert den Pfad der Programmdatei mit File.separator am Schluss
        String pFilePath = "pFile";
        File propFile = new File(pFilePath);
        if (!propFile.exists()) {
            try {
                CodeSource cS = Main.class.getProtectionDomain().getCodeSource();
                File jarFile = new File(cS.getLocation().toURI().getPath());
                String jarDir = jarFile.getParentFile().getPath();
                propFile = new File(jarDir + File.separator + pFilePath);
            } catch (Exception ex) {
            }
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
            MSLog.fehlerMeldung(807293847, MSLog.FEHLER_ART_PROG, MSFunktionen.class.getName(), e);
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
            MSLog.fehlerMeldung(134679898, MSLog.FEHLER_ART_PROG, MSFunktionen.class.getName(), e);
        }
        return msg;
    }

    public static void unescape(DatenFilm film) {
        film.arr[DatenFilm.FILM_THEMA_NR] = StringEscapeUtils.unescapeXml(film.arr[DatenFilm.FILM_THEMA_NR].trim());
        film.arr[DatenFilm.FILM_THEMA_NR] = StringEscapeUtils.unescapeHtml4(film.arr[DatenFilm.FILM_THEMA_NR].trim());

        // Beschreibung
        film.arr[DatenFilm.FILM_BESCHREIBUNG_NR] = StringEscapeUtils.unescapeXml(film.arr[DatenFilm.FILM_BESCHREIBUNG_NR].trim());
        film.arr[DatenFilm.FILM_BESCHREIBUNG_NR] = StringEscapeUtils.unescapeHtml4(film.arr[DatenFilm.FILM_BESCHREIBUNG_NR].trim());

        // Titel
        film.arr[DatenFilm.FILM_TITEL_NR] = StringEscapeUtils.unescapeXml(film.arr[DatenFilm.FILM_TITEL_NR].trim());
        film.arr[DatenFilm.FILM_TITEL_NR] = StringEscapeUtils.unescapeHtml4(film.arr[DatenFilm.FILM_TITEL_NR].trim());
    }

    public static String utf8(String ret) {
        ret = ret.replace("\\u0026", "&");
        ret = ret.replace("\\u003C", "<");
        ret = ret.replace("\\u003c", "<");
        ret = ret.replace("\\u003E", ">");
        ret = ret.replace("\\u003e", ">");
        ret = ret.replace("\\u00E4", "ä");
        ret = ret.replace("\\u00e4", "ä");
        ret = ret.replace("\\u00C4", "Ä");
        ret = ret.replace("\\u00c4", "Ä");
        ret = ret.replace("\\u00F6", "ö");
        ret = ret.replace("\\u00f6", "ö");
        ret = ret.replace("\\u00D6", "Ö");
        ret = ret.replace("\\u00d6", "Ö");
        ret = ret.replace("\\u00FC", "ü");
        ret = ret.replace("\\u00fc", "ü");
        ret = ret.replace("\\u00DC", "Ü");
        ret = ret.replace("\\u00dc", "Ü");
        ret = ret.replace("\\u00DF", "ß");
        ret = ret.replace("\\u00df", "ß");
        ret = ret.replace("\\u20AC", "€");
        ret = ret.replace("\\u20ac", "€");
        ret = ret.replace("\\u0024", "$");
        ret = ret.replace("\\u00A3", "£");
        ret = ret.replace("\\u00a3", "£");
        ret = ret.replace("\\u00F3", "\u00f3");
        ret = ret.replace("\\u00f3", "\u00f3");
        return ret;
    }

    public static String cleanUnicode(String ret, String sonst) {
        String r = "";
        char c;
        for (int i = 0; i < ret.length(); ++i) {
            c = ret.charAt(i);
            char hex = ret.charAt(i);
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.BASIC_LATIN) {
                r += c;
            } else // Umlaute, 
            if (c == 'Ä' || c == 'Ö' || c == 'Ü'
                    || c == 'ä' || c == 'ö' || c == 'ü') {
                r += c;
            } else if (c == 'ß') {
                r += "ß";
            } else // Buchstaben
            if (c == 'Â' || c == 'À' || c == 'Å' || c == 'Á') {
                r += "A";
            } else if (c == 'å' || c == 'á' || c == 'à' || c == 'â') {
                r += "a";
            } else if (c == 'Č' || c == 'Č') {
                r += "C";
            } else if (c == 'ć' || c == 'č' || c == 'ç') {
                r += "c";
            } else if (c == 'Đ') {
                r += "D";
            } else if (c == 'É' || c == 'È') {
                r += "E";
            } else if (c == 'é' || c == 'è' || c == 'ê' || c == 'ě' || c == 'ë') {
                r += "e";
            } else if (c == 'í') {
                r += "i";
            } else if (c == 'ñ') {
                r += "n";
            } else if (c == 'ó' || c == 'ô' || c == 'ø') {
                r += "o";
            } else if (c == 'Š') {
                r += "S";
            } else if (c == 'ś' || c == 'š' || c == 'ş') {
                r += "s";
            } else if (c == 'ł' || c == 'Ł') {
                r += "t";
            } else if (c == 'û' || c == 'ù') {
                r += "u";
            } else if (c == 'ý') {
                r += "y";
            } else if (c == 'Ž' || c == 'Ź') {
                r += "Z";
            } else if (c == 'ž' || c == 'ź') {
                r += "z";
            } else if (c == 'æ') {
                r += "ae";
            } else // Rest
            if (c == '\n') {
            } else if (c == '–') {
                r += "-";
            } else if (c == '„') {
                r += "\"";
            } else if (c == '„' || c == '”' || c == '“' || c == '«' || c == '»') {
                r += "\"";
            } else if (c == '?') {
                r += "?";
            } else if (c == '°' || c == '™') {
                r += "";
            } else if (c == '…') {
                r += "...";
            } else if (c == '€') {
                r += "€";
            } else if (c == '´' || c == '’' || c == '‘' || c == '¿') {
                r += "'";
            } else if (c == '\u003F') {
                r += "?";
            } else if (c == '\u0096') {
                r += "-";
            } else if (c == '\u0085') {
            } else if (c == '\u0080') {
            } else if (c == '\u0084') {
            } else if (c == '\u0092') {
            } else if (c == '\u0093') {
            } else if (c == '\u0091') {
                r += "-";
            } else {
                r += sonst;
            }
        }
        return r;
    }

    public static String addsPfad(String pfad1, String pfad2) {
        String ret = "";
        if (pfad1 != null && pfad2 != null) {
            if (!pfad1.equals("") && !pfad2.equals("")) {
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
            MSLog.fehlerMeldung(283946015, MSLog.FEHLER_ART_PROG, "GuiFunktionen.addsPfad", pfad1 + " - " + pfad2);
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
            MSLog.fehlerMeldung(395019631, MSLog.FEHLER_ART_PROG, "GuiFunktionen.getDateiName", pfad);
        }
        return ret;
    }

//    public static String getDateiSuffix(String pfad) {
//        //Suffix einer URL extrahieren
//        String ret = "";
//        if (pfad != null) {
//            if (!pfad.equals("") && pfad.contains(".")) {
//                ret = pfad.substring(pfad.lastIndexOf(".") + 1);
//            }
//        }
//        if (ret.equals("")) {
//            MSLog.fehlerMeldung(969871236, MSLog.FEHLER_ART_PROG, "GuiFunktionen.getDateiSuffix", pfad);
//        }
//        if (ret.length() > 3) {
//            if (ret.length() > 5) {
//                // dann ist was faul
//                ret = "---";
//            }
//            MSLog.fehlerMeldung(821397046, MSLog.FEHLER_ART_PROG, "GuiFunktionen.getDateiSuffix", pfad);
//        }
//        return ret;
//    }
//
//    public static String getHomePath() {
//        //lifert den Pfad zum Homeverzeichnis
//        return System.getProperty("user.home");
//    }
//
//    public static String[] addLeerListe(String[] str) {
//        //ein Leerzeichen der Liste voranstellen
//        int len = str.length + 1;
//        String[] liste = new String[len];
//        liste[0] = "";
//        System.arraycopy(str, 0, liste, 1, len - 1);
//        return liste;
//    }
//
//    public static String textLaenge(int max, String text, boolean mitte, boolean addVorne) {
//        if (text.length() > max) {
//            if (mitte) {
//                text = text.substring(0, 25) + " .... " + text.substring(text.length() - (max - 31));
//            } else {
//                text = text.substring(0, max - 1);
//            }
//        }
//        while (text.length() < max) {
//            if (addVorne) {
//                text = " " + text;
//            } else {
//                text = text + " ";
//            }
//        }
//        return text;
//    }
}
