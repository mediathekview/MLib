/*
 * MediathekView Copyright (C) 2008 W. Xaver W.Xaver[at]googlemail.com
 * http://zdfmediathk.sourceforge.net/
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */
package de.mediathekview.mlib.tool;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

@Deprecated
public class Duration {

  private static class Counter {

    String text;
    int count;
    long time;
    Date start;

    public Counter(final String nr, final int count) {
      text = nr;
      this.count = count;
      start = new Date();
    }
  }

  private static Date stopZeitStatic = new Date(System.currentTimeMillis());
  private static final DecimalFormat DF = new DecimalFormat("###,##0.00");
  private static int sum = 0;

  private static final ArrayList<Counter> COUNTER_LIST = new ArrayList<>();

  public static synchronized void counterStart(final String text) {
    Counter cc = null;
    for (final Counter c : COUNTER_LIST) {
      if (c.text.equals(text)) {
        cc = c;
        break;
      }
    }
    if (cc == null) {
      COUNTER_LIST.add(new Counter(text, 0));
    } else {
      cc.start = new Date();
    }
  }

  public static synchronized void counterStop(final String text) {
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
    } catch (final Exception ignored) {
      kl = klasse;
    }

    String extra = "";
    Counter cc = null;
    for (final Counter c : COUNTER_LIST) {
      if (c.text.equals(text)) {
        cc = c;
        break;
      }
    }
    if (cc != null) {
      cc.count++;
      try {
        final long time = Math.round(new Date().getTime() - cc.start.getTime());
        cc.time += time;
        extra = cc.text + " Anzahl: " + cc.count + "   Dauer: " + roundDuration(time);
      } catch (final Exception ex) {
      }
    }

    staticPing(kl, text, extra);
  }

  public static synchronized void printCounter() {
    int max = 0;
    for (final Counter c : COUNTER_LIST) {
      if (c.text.length() > max) {
        max = c.text.length();
      }
    }
    max++;
    for (final Counter c : COUNTER_LIST) {
      while (c.text.length() < max) {
        c.text = c.text + " ";
      }
    }

    System.out.println("");
    System.out.println("");
    System.out.println("#################################################################");
    for (final Counter c : COUNTER_LIST) {
      System.out
          .println(c.text + " Anzahl: " + c.count + "   Gesamtdauer: " + roundDuration(c.time));
    }
    System.out.println("#################################################################");
    System.out.println("");
  }

  public static String roundDuration(final long s) {
    String ret;
    if (s > 1_000.0) {
      ret = DF.format(s / 1_000.0) + " s";
    } else {
      ret = DF.format(s) + " ms";
    }

    return ret;
  }

  public synchronized static void staticPing(final String text) {
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
    } catch (final Exception ignored) {
      kl = klasse;
    }
    staticPing(kl, text, "");
  }

  private static void staticPing(final String klasse, final String text, final String extra) {
    final Date now = new Date(System.currentTimeMillis());
    long sekunden;
    try {
      sekunden = Math.round(now.getTime() - stopZeitStatic.getTime());
    } catch (final Exception ex) {
      sekunden = -1;
    }
    System.out.println("");
    System.out.println("========== ========== ========== ========== ==========");
    System.out.println("DURATION " + sum++ + ":  " + text + "  [" + roundDuration(sekunden) + "]");
    System.out.println("   Klasse:  " + klasse);
    if (!extra.isEmpty()) {
      System.out.println("   " + extra);
    }
    System.out.println("========== ========== ========== ========== ==========");
    System.out.println("");

    stopZeitStatic = now;
  }

}
