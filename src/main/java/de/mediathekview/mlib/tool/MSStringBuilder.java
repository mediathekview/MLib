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

import java.util.ArrayList;

@Deprecated
public class MSStringBuilder {

  private final StringBuilder cont;

  public MSStringBuilder() {
    cont = new StringBuilder();
  }

  public MSStringBuilder(final int capacity) {
    cont = new StringBuilder(capacity);
  }

  public synchronized void append(final char[] str) {
    cont.append(str);
  }

  public synchronized void append(final char[] str, final int offset, final int len) {
    cont.append(str, offset, len);
  }

  // =====================================================
  // =====================================================
  // =====================================================
  public String extract(final String musterStart, final String musterEnde) {
    return extract(musterStart, "", musterEnde, 0, 0, "");
  }

  public String extract(final String musterStart, final String musterEnde, final int abPos) {
    return extract(musterStart, "", musterEnde, abPos, 0, "");
  }

  public String extract(final String musterStart, final String musterEnde, final int abPos,
      final int bisPos) {
    return extract(musterStart, "", musterEnde, abPos, bisPos, "");
  }

  public String extract(final String musterStart1, final String musterStart2,
      final String musterEnde) {
    return extract(musterStart1, musterStart2, musterEnde, 0, 0, "");
  }

  public String extract(final String musterStart1, final String musterStart2,
      final String musterEnde, final int abPos, final int bisPos, final String addUrl) {
    int pos1, pos2;
    if ((pos1 = cont.indexOf(musterStart1, abPos)) == -1) {
      return "";
    }
    pos1 += musterStart1.length();
    if (!musterStart2.isEmpty() && (pos1 = cont.indexOf(musterStart2, pos1)) == -1) {
      return "";
    }
    pos1 += musterStart2.length();
    if ((pos2 = cont.indexOf(musterEnde, pos1)) == -1) {
      return "";
    }
    if (bisPos > 0 && pos2 > bisPos) {
      return "";
    }
    final String ret = cont.substring(pos1, pos2);
    if (!ret.isEmpty()) {
      // damit nicht nur! addUrl zurückkommt
      return addUrl + ret;
    }
    return "";
  }

  public String extract(final String musterStart1, final String musterStart2,
      final String musterEnde, final String addUrl) {
    return extract(musterStart1, musterStart2, musterEnde, 0, 0, addUrl);
  }

  public void extractList(final int ab, final int bis, final String musterStart1,
      final String musterStart2, final String musterEnde, final String addUrl,
      final ArrayList<String> result) {
    int pos1, pos2, stopPos, count = 0;
    String str;
    pos1 = ab;
    stopPos = bis;
    if (pos1 == -1) {
      return;
    }

    while ((pos1 = cont.indexOf(musterStart1, pos1)) != -1) {
      ++count;
      if (count > 10_000) {
        DbgMsg.print("Achtung");
        break;
      }
      pos1 += musterStart1.length();

      if (!musterStart2.isEmpty()) {
        if ((pos2 = cont.indexOf(musterStart2, pos1)) == -1) {
          continue;
        }
        pos1 = pos2 + musterStart2.length();
      }

      if ((pos2 = cont.indexOf(musterEnde, pos1)) == -1) {
        continue;
      }
      if (stopPos > 0 && pos2 > stopPos) {
        continue;
      }

      if ((str = cont.substring(pos1, pos2)).isEmpty()) {
        continue;
      }

      str = addUrl + str;
      if (!result.contains(str)) {
        result.add(str);
        if (result.size() > 1000) {
          DbgMsg.print("Achtung");
        }
      }

    }
  }

  public void extractList(final String musterStart, final String musterEnde,
      final ArrayList<String> result) {
    extractList("", "", musterStart, "", musterEnde, "", result);
  }

  public void extractList(final String musterStart1, final String musterStart2,
      final String musterEnde, final ArrayList<String> result) {
    extractList("", "", musterStart1, musterStart2, musterEnde, "", result);
  }

  public void extractList(final String abMuster, final String bisMuster, final String musterStart,
      final String musterEnde, final String addUrl, final ArrayList<String> result) {
    extractList(abMuster, bisMuster, musterStart, "", musterEnde, addUrl, result);
  }

  public void extractList(final String abMuster, final String bisMuster, final String musterStart1,
      final String musterStart2, final String musterEnde, final String addUrl,
      final ArrayList<String> result) {
    int pos1, pos2, stopPos, count = 0;
    String str;
    pos1 = abMuster.isEmpty() ? 0 : cont.indexOf(abMuster);
    if (pos1 == -1) {
      return;
    }

    stopPos = bisMuster.isEmpty() ? -1 : cont.indexOf(bisMuster, pos1);

    while ((pos1 = cont.indexOf(musterStart1, pos1)) != -1) {
      ++count;
      if (count > 10_000) {
        DbgMsg.print("Achtung");
        break;
      }
      pos1 += musterStart1.length();

      if (!musterStart2.isEmpty()) {
        if ((pos2 = cont.indexOf(musterStart2, pos1)) == -1) {
          continue;
        }
        pos1 = pos2 + musterStart2.length();
      }

      if ((pos2 = cont.indexOf(musterEnde, pos1)) == -1) {
        continue;
      }
      if (stopPos > 0 && pos2 > stopPos) {
        continue;
      }

      if ((str = cont.substring(pos1, pos2)).isEmpty()) {
        continue;
      }

      str = addUrl + str;
      addStr(str, result);
    }
  }

  public synchronized int indexOf(final String str) {
    return cont.indexOf(str);
  }

  public synchronized int indexOf(final String str, final int fromIndex) {
    return cont.indexOf(str, fromIndex);
  }

  public int lastIndexOf(final String of) {
    return cont.lastIndexOf(of);
  }

  public int length() {
    return cont.length();
  }

  public synchronized void setLength(final int newLength) {
    cont.setLength(newLength);
  }

  // =====================================================
  // StringBuilder Kram
  public String substring(final int start) {
    return cont.substring(start);
  }

  public String substring(final int start, final int end) {
    return cont.substring(start, end);
  }

  private void addStr(final String str, final ArrayList<String> result) {
    if (!result.contains(str)) {
      result.add(str);
      if (result.size() > 1000) {
        DbgMsg.print("Achtung");
      }
    }
  }

}
