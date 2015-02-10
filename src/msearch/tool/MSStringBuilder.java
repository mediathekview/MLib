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

import java.util.ArrayList;

public class MSStringBuilder {

    private StringBuilder cont;
    private int pos1 = 0, pos2 = 0, pos3 = 0;

    public MSStringBuilder() {
        cont = new StringBuilder();
    }

    public MSStringBuilder(int capacity) {
        cont = new StringBuilder(capacity);
    }

    public String substring(int start) {
        return cont.substring(start);
    }

    public int lastIndexOf(String of) {
        return cont.lastIndexOf(of);
    }

    public int length() {
        return cont.length();
    }

    public String substring(int start, int end) {
        return cont.substring(start, end);
    }

    public synchronized void append(char[] str) {
        cont.append(str);
    }

    public synchronized void append(char[] str, int offset, int len) {
        cont.append(str, offset, len);
    }

    public synchronized void setLength(int newLength) {
        cont.setLength(newLength);
    }

    public synchronized int indexOf(String str, int fromIndex) {
        return cont.indexOf(str, fromIndex);
    }

    public synchronized int indexOf(String str) {
        return cont.indexOf(str);
    }

    public String extract(String musterStart, String musterEnde) {
        return extract(musterStart, musterEnde, 0);
    }

    public String extract(String musterStart, String musterEnde, int abPos) {
        return extract(musterStart, musterEnde, abPos, "");
    }

    public String extract(String musterStart, String musterEnde, int abPos, String addUrl) {
        if ((pos1 = cont.indexOf(musterStart, abPos)) != -1) {
            pos1 += musterStart.length();
            if ((pos2 = cont.indexOf(musterEnde, pos1)) != -1) {
                String ret = cont.substring(pos1, pos2);
                if (!ret.isEmpty()) {
                    return addUrl + ret;
                } else {
                    return "";
                }
            }
        }
        return "";
    }

    public void extractList(String abMuster, String musterStart, String musterEnde, ArrayList<String> result) {
        try {
            pos1 = 0;
            while ((pos1 = cont.indexOf(abMuster, pos1)) != -1) {
                pos1 += abMuster.length();
                if ((pos2 = cont.indexOf(musterStart, pos1)) != -1) {
                    pos2 += musterStart.length();
                    if ((pos3 = cont.indexOf(musterEnde, pos2)) != -1) {
                        result.add(cont.substring(pos2, pos3));
                        if (result.size() > 1000) {
                            System.out.println("Achtung");
                        }
                    }
                }
            }
        } catch (Exception ex) {
            MSLog.fehlerMeldung(462310871, MSLog.FEHLER_ART_MREADER, "extractList", ex);
        }
    }

    public void extractList(String musterStart, String musterEnde, int abPos, String addUrl, ArrayList<String> result) {
        pos1 = abPos;
        while ((pos1 = cont.indexOf(musterStart, pos1)) != -1) {
            pos1 += musterStart.length();
            if ((pos2 = cont.indexOf(musterEnde, pos1)) != -1) {
                String s = addUrl + cont.substring(pos1, pos2);
                if (!result.contains(s)) {
                    result.add(s);
                }
            }
        }
    }

    public void extractList(String abMuster, String bisMuster, String musterStart, String musterEnde, String addUrl, ArrayList<String> result) {
        if ((pos1 = cont.indexOf(abMuster)) != -1) {
            int stopPos = cont.indexOf(bisMuster); // bei "" -> 0
            while ((pos1 = cont.indexOf(musterStart, pos1)) != -1) {
                pos1 += musterStart.length();
                if ((pos2 = cont.indexOf(musterEnde, pos1)) != -1) {
                    if (stopPos <= 0 || pos2 < stopPos) {
                        result.add(addUrl + cont.substring(pos1, pos2));
                    }
                }
            }
        }
    }

    public void extractList(String abMuster, String bisMuster, String musterStart1, String musterStart2, String musterEnde, String addUrl, ArrayList<String> result) {
        if ((pos1 = cont.indexOf(abMuster)) != -1) {
            int stopPos = cont.indexOf(bisMuster);
            while ((pos1 = cont.indexOf(musterStart1, pos1)) != -1) {
                pos1 += musterStart1.length();
                pos1 = cont.indexOf(musterStart2, pos1);
                pos1 += musterStart2.length();
                if ((pos2 = cont.indexOf(musterEnde, pos1)) != -1) {
                    if (stopPos < 0 || pos2 < stopPos) {
                        result.add(addUrl + cont.substring(pos1, pos2));
                    }
                }
            }
        }
    }

    public String extractLast(String musterStart, String musterEnde) {
        if ((pos1 = cont.lastIndexOf(musterStart)) != -1) {
            pos1 += musterStart.length();
            if ((pos2 = cont.indexOf(musterEnde, pos1)) != -1) {
                return cont.substring(pos1, pos2);
            }
        }
        return "";
    }

    public String extractLast(String musterStart1, String musterStart2, String musterEnde) {
        if ((pos1 = cont.lastIndexOf(musterStart1)) != -1) {
            pos1 += musterStart1.length();
            if ((pos1 = cont.indexOf(musterStart2, pos1)) != -1) {
                pos1 += musterStart2.length();
                if ((pos2 = cont.indexOf(musterEnde, pos1)) != -1) {
                    return cont.substring(pos1, pos2);
                }
            }
        }
        return "";
    }

    public String extract(String musterStart, String musterEnde, int abPos, int stopPos) {
        if ((pos1 = cont.indexOf(musterStart, abPos)) != -1) {
            pos1 += musterStart.length();
            if ((pos2 = cont.indexOf(musterEnde, pos1)) != -1) {
                if (stopPos <= 0 || pos2 < stopPos) {
                    return cont.substring(pos1, pos2);
                }
            }
        }
        return "";
    }

    public String extract(String musterStart1, String musterStart2, String musterEnde) {
        return extract(musterStart1, musterStart2, musterEnde, 0, -1);
    }

    public String extract(String musterStart1, String musterStart2, String musterEnde, int abPos) {
        return extract(musterStart1, musterStart2, musterEnde, abPos, -1);
    }

    public String extract(String musterStart1, String musterStart2, String musterEnde, int abPos, int stopPos) {
        if ((pos1 = cont.indexOf(musterStart1, abPos)) != -1) {
            pos1 += musterStart1.length();
            if ((pos1 = cont.indexOf(musterStart2, pos1)) != -1) {
                pos1 += musterStart2.length();
                if ((pos2 = cont.indexOf(musterEnde, pos1)) != -1) {
                    if (stopPos < 0 || pos2 < stopPos) {
                        return cont.substring(pos1, pos2);
                    }
                }
            }
        }
        return "";
    }
}
