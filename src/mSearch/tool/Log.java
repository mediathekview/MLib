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

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import org.apache.commons.lang3.StringUtils;

public class Log {

    private final static String FEHLER = "Fehler(" + MSConst.PROGRAMMNAME + "): ";

    // private
    private static class Error {

        String cl = "";
        int nr = 0;
        int count = 0;
        boolean ex = false;

        public Error(int nr, String cl, boolean ex) {
            this.nr = nr;
            this.cl = cl;
            this.ex = ex;
            this.count = 1;
        }
    }
    private static final LinkedList<Error> fehlerListe = new LinkedList<>();
    private static boolean progress = false;
    private static final Date startZeit = new Date(System.currentTimeMillis());
    private static File logFile = null;
    private static final ArrayList<String> logList = new ArrayList<>();

    public static synchronized void setLogfile(String logFileString) {
        logFile = new File(logFileString);
        File dir = new File(logFile.getParent());
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                logFile = null;
                Log.fehlerMeldung(632012165, "Kann den Pfad nicht anlegen: " + dir.toString());
            }
        }

    }

    public static synchronized void versionsMeldungen(String progName) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        systemMeldung("");
        systemMeldung("");
        systemMeldung("");
        systemMeldung("");
        systemMeldung("");
        systemMeldung(".___  ___.  _______  _______   __       ___   .___________. __    __   _______  __  ___");
        systemMeldung("|   \\/   | |   ____||       \\ |  |     /   \\  |           ||  |  |  | |   ____||  |/  /");
        systemMeldung("|  \\  /  | |  |__   |  .--.  ||  |    /  ^  \\ `---|  |----`|  |__|  | |  |__   |  '  /");
        systemMeldung("|  |\\/|  | |   __|  |  |  |  ||  |   /  /_\\  \\    |  |     |   __   | |   __|  |    <");
        systemMeldung("|  |  |  | |  |____ |  '--'  ||  |  /  _____  \\   |  |     |  |  |  | |  |____ |  .  \\");
        systemMeldung("|__|  |__| |_______||_______/ |__| /__/     \\__\\  |__|     |__|  |__| |_______||__|\\__\\");
        systemMeldung("");
        systemMeldung("");
        systemMeldung("");
        systemMeldung("##################################################################################");
        systemMeldung("Programmstart: " + sdf.format(startZeit));
        systemMeldung("##################################################################################");
        systemMeldung("");
        long totalMem = Runtime.getRuntime().totalMemory();
        systemMeldung("totalMemory: " + totalMem / (1000L * 1000L) + " MB");
        long maxMem = Runtime.getRuntime().maxMemory();
        systemMeldung("maxMemory: " + maxMem / (1000L * 1000L) + " MB");
        long freeMem = Runtime.getRuntime().freeMemory();
        systemMeldung("freeMemory: " + freeMem / (1000L * 1000L) + " MB");
        systemMeldung("");
        systemMeldung("##################################################################################");
        systemMeldung("");
        //Version
        systemMeldung(progName + Functions.getProgVersionString());
        systemMeldung("Compiled: " + Functions.getCompileDate());
        systemMeldung("");
        systemMeldung("##################################################################################");
        systemMeldung("");
        systemMeldung("Java");
        String[] java = Functions.getJavaVersion();
        for (String ja : java) {
            Log.systemMeldung(ja);
        }
        systemMeldung("");
    }

    public static synchronized void startMeldungen() {
        startZeit.setTime(System.currentTimeMillis());
        versionsMeldungen(MSConst.PROGRAMMNAME);
        systemMeldung("##################################################################################");
        systemMeldung("");
        systemMeldung("Programmpfad: " + Functions.getPathJar());
        systemMeldung("Filmliste: " + MSConfig.getPathFilmlist_json_akt(true /*aktDate*/));
        systemMeldung("Useragent: " + MSConfig.getUserAgent());
        systemMeldung("");
        systemMeldung("##################################################################################");
        systemMeldung("");
        if (MSConfig.loadLongMax()) {
            systemMeldung("Laden:  alles");
        } else {
            systemMeldung("Laden:  nur update");
        }
        if (MSConfig.updateFilmliste) {
            systemMeldung("Filmliste:  nur updaten");
        } else {
            systemMeldung("Filmliste:  neu erstellen");
        }
        systemMeldung("ImportURL 1:  " + MSConfig.importUrl_1__anhaengen);
        systemMeldung("ImportURL 2:  " + MSConfig.importUrl_2__anhaengen);
        systemMeldung("ImportOLD:  " + MSConfig.importOld);
        if (MSConfig.nurSenderLaden != null) {
            systemMeldung("Nur Sender laden:  " + StringUtils.join(MSConfig.nurSenderLaden, ','));
        }
        systemMeldung("");
        systemMeldung("##################################################################################");
    }

    public static synchronized void endeMeldung() {
        systemMeldung("");
        systemMeldung("");
        systemMeldung("");
        systemMeldung("");

        fehlerMeldungen().forEach(Log::systemMeldung);

        // Laufzeit ausgeben
        Date stopZeit = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        int minuten;
        try {
            minuten = Math.round((stopZeit.getTime() - startZeit.getTime()) / (1000 * 60));
        } catch (Exception ex) {
            minuten = -1;
        }
        systemMeldung("");
        systemMeldung("");
        systemMeldung("##################################################################################");
        systemMeldung("   --> Beginn: " + sdf.format(startZeit));
        systemMeldung("   --> Fertig: " + sdf.format(stopZeit));
        systemMeldung("   --> Dauer[Min]: " + (minuten == 0 ? "<1" : minuten));
        systemMeldung("##################################################################################");
        systemMeldung("");
        systemMeldung("   und Tschuess");
        systemMeldung("");
        systemMeldung("");
        systemMeldung("##################################################################################");
    }

    public static synchronized ArrayList<String> fehlerMeldungen() {
        int max = 0;
        ArrayList<String> retList = new ArrayList<>();
        retList.add("");
        retList.add("##################################################################################");
        if (fehlerListe.size() == 0) {
            retList.add(" Keine Fehler :)");
        } else {
            // Fehler ausgeben
            int i_1;
            int i_2;
            for (Error e : fehlerListe) {
                if (e.cl.length() > max) {
                    max = e.cl.length();
                }
            }
            max++;
            for (Error e : fehlerListe) {
                while (e.cl.length() < max) {
                    e.cl = e.cl + " ";
                }
            }
            for (int i = 1; i < fehlerListe.size(); ++i) {
                for (int k = i; k > 0; --k) {
                    i_1 = fehlerListe.get(k - 1).nr;
                    i_2 = fehlerListe.get(k).nr;
                    // if (str1.compareToIgnoreCase(str2) > 0) {
                    if (i_1 < i_2) {
                        fehlerListe.add(k - 1, fehlerListe.remove(k));
                    } else {
                        break;
                    }
                }
            }
            for (Error e : fehlerListe) {
                String strEx;
                if (e.ex) {
                    strEx = "Ex! ";
                } else {
                    strEx = "    ";
                }
                retList.add(strEx + e.cl + " Fehlernummer: " + e.nr + " Anzahl: " + e.count);
            }
        }
        retList.add("##################################################################################");
        return retList;
    }

    // Fehlermeldung mit Exceptions
    public static synchronized void fehlerMeldung(int fehlerNummer, Exception ex) {
        fehlermeldung_(fehlerNummer, ex, new String[]{});
    }

    public static synchronized void fehlerMeldung(int fehlerNummer, Exception ex, String text) {
        fehlermeldung_(fehlerNummer, ex, new String[]{text});
    }

    public static synchronized void fehlerMeldung(int fehlerNummer, Exception ex, String text[]) {
        fehlermeldung_(fehlerNummer, ex, text);
    }

    // Fehlermeldungen
    public static synchronized void fehlerMeldung(int fehlerNummer, String text) {
        fehlermeldung_(fehlerNummer, null, new String[]{text});
    }

    public static synchronized void fehlerMeldung(int fehlerNummer, String[] text) {
        fehlermeldung_(fehlerNummer, null, text);
    }

    public static synchronized void systemMeldung(String[] text) {
        systemmeldung_(text);
    }

    public static synchronized void systemMeldung(String text) {
        systemmeldung_(new String[]{text});
    }

    public static synchronized void progress(String texte) {
        progress = true;
        if (!texte.isEmpty()) {
            System.out.print(texte + "\r");
        }
    }

    private static void resetProgress() {
        // Leerzeile um die Progresszeile zu löschen
        if (progress) {
            System.out.print("                                                                                                             \r");
            progress = false;
        }
    }

    private static void addFehlerNummer(int nr, String classs, boolean exception) {
        for (Error e : fehlerListe) {
            if (e.nr == nr) {
                ++e.count;
                return;
            }
        }
        // dann gibts die Nummer noch nicht
        fehlerListe.add(new Error(nr, classs, exception));
    }

    private static void fehlermeldung_(int fehlerNummer, Exception ex, String[] texte) {
        final Throwable t = new Throwable();
        final StackTraceElement methodCaller = t.getStackTrace()[2];
        final String klasse = methodCaller.getClassName() + "." + methodCaller.getMethodName();
        String kl;
        try {
            kl = klasse;
            while (kl.contains(".")) {
                if (Character.isUpperCase(kl.charAt(0))) {
                    break;
                } else {
                    kl = kl.substring(kl.indexOf(".") + 1);
                }
            }
        } catch (Exception ignored) {
            kl = klasse;
        }
        addFehlerNummer(fehlerNummer, kl, ex != null);
        if (ex != null || MSConfig.debug) {
            // Exceptions immer ausgeben
            resetProgress();
            String x, z;
            if (ex != null) {
                x = "!";
            } else {
                x = "=";
            }
            z = "*";
            logList.add(x + x + x + x + x + x + x + x + x + x
                    + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x + x);

            try {
                // Stacktrace
                try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
                    if (ex != null) {
                        ex.printStackTrace(pw);
                    }
                    pw.flush();
                    sw.flush();
                    logList.add(sw.toString());
                }
            } catch (Exception ignored) {
            }

            logList.add(z + " Fehlernr: " + fehlerNummer);
            if (ex != null) {
                logList.add(z + " Exception: " + ex.getMessage());
            }
            logList.add(z + " " + FEHLER + kl);
            for (String aTexte : texte) {
                logList.add(z + "           " + aTexte);
            }
            logList.add("");
            printLog();
        }
    }

    private static void systemmeldung_(String[] texte) {
        resetProgress();
        final String z = ". ";
        if (texte.length <= 1) {
            logList.add(z + " " + texte[0]);
        } else {
            String zeile = "---------------------------------------";
            String txt;
            logList.add(z + zeile);
            for (String aTexte : texte) {
                txt = "| " + aTexte;
                logList.add(z + txt);
            }
            logList.add(z + zeile);
        }
        printLog();
    }

    private static void printLog() {
        logList.forEach(System.out::println);

        if (logFile != null) {
            try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(logFile, true), MSConst.KODIERUNG_UTF)) {
                for (String s : logList) {
                    out.write(s);
                    out.write("\n");
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
        logList.clear();
    }
}
