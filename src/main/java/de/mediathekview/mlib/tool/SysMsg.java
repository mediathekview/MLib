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

@Deprecated
public class SysMsg {

  public static boolean playerMeldungenAus = false;
  public static final int LOG_SYSTEM = 1;
  public static final int LOG_PLAYER = 2;

  private static final int MAX_LAENGE_1 = 50000;
  private static final int MAX_LAENGE_2 = 30000;
  private static int zeilenNrSystem = 0;
  private static int zeilenNrProgramm = 0;

  public static void clearText(final int art) {
  }

  public synchronized static String getText(final int logArt) {
    return null;
  }

  public static synchronized void playerMsg(final String text) {
  }

  public static synchronized void sysMsg(final String text) {
  }

  public static synchronized void sysMsg(final String[] text) {
  }

  private static String getNr(final int nr) {
    return null;
  }

  private static void notify(final int art, final String zeile) {
  }

  private static void playermeldung(final String[] texte) {
  }

  private static void systemmeldung(final String[] texte) {
  }

}
