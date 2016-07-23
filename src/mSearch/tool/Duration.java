/*    
 *    MediathekView
 *    Copyright (C) 2008   W. Xaver
 *    W.Xaver[at]googlemail.com
 *    http://zdfmediathk.sourceforge.net/
 *    
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package mSearch.tool;

import java.text.DecimalFormat;
import java.util.Date;
import mSearch.Config;

public class Duration {

    private static Date stopZeitStatic = new Date(System.currentTimeMillis());
    private static final DecimalFormat DF = new DecimalFormat("###,##0.00");
    private static int sum = 0;
    private Date startZeit = new Date(System.currentTimeMillis());
    private Date stopZeit = new Date(System.currentTimeMillis());
    private int sekunden;
    private int count = 0;
    private String TEXT = "";

    public Duration(String t) {
        TEXT = t;
        start("");
    }

    public synchronized static void staticDbgPing(String text) {
        if (Config.debug) {
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
            staticPing(kl, text, null);
        }
    }

    public synchronized static void staticPing(String text) {
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
        staticPing(kl, text, null);
    }

    public synchronized static void staticPing(String text, Date start) {
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
        staticPing(kl, text, start);
    }

    private static void staticPing(String klasse, String text, Date start) {
        Date now = new Date(System.currentTimeMillis());
        long sekunden, sFromStart = -1;
        try {
            sekunden = Math.round(now.getTime() - stopZeitStatic.getTime());
            if (start != null) {
                sFromStart = Math.round(now.getTime() - start.getTime());
            }

        } catch (Exception ex) {
            sekunden = -1;
        }

        System.out.println("");
        System.out.println("========== ========== ========== ========== ==========");
        System.out.println("DURATION " + sum++ + ":  " + text + "  [" + roundDuration(sekunden)
                + (sFromStart > -1 ? " Σ " + roundDuration(sFromStart) : "") + "]");
        System.out.println("   Klasse:  " + klasse);
        System.out.println("========== ========== ========== ========== ==========");
        System.out.println("");

        stopZeitStatic = new Date(System.currentTimeMillis());
    }

    public static String roundDuration(long s) {
        String ret;
        if (s > 1_000.0) {
            ret = DF.format(s / 1_000.0) + " s";
        } else {
            ret = DF.format(s) + " ms";
        }

        return ret;
    }

    public void ping(String text) {
        stop(TEXT + " #  " + text + " " + count++);
        startZeit = new Date(System.currentTimeMillis());
    }

    public final void start(String text) {
        startZeit = new Date(System.currentTimeMillis());
        if (!text.isEmpty()) {
            System.out.println("");
            System.out.println("======================================");
            System.out.println(" Start: " + text);
            System.out.println("======================================");
            System.out.println("");
        }
    }

    public void stop(String text) {
        stopZeit = new Date(System.currentTimeMillis());
        try {
            sekunden = Math.round(stopZeit.getTime() - startZeit.getTime());
        } catch (Exception ex) {
            sekunden = -1;
        }
        System.out.println("");
        System.out.println("======================================");
        System.out.println(" " + text + " [ms]: " + sekunden);
        System.out.println("======================================");
        System.out.println("");
    }
}
