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

    private final StringBuilder cont;
    private int pos1 = 0;
    private int pos2 = 0;

    public MSStringBuilder() {
        cont = new StringBuilder();
    }

    public MSStringBuilder(int capacity) {
        cont = new StringBuilder(capacity);
    }

    //=====================================================
    //StringBuilder Kram
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

    //=====================================================
    //=====================================================
    //=====================================================
    public String extract(String musterStart, String musterEnde) {
        return extract(musterStart, "", musterEnde, 0, 0, "");
    }

    public String extract(String musterStart, String musterEnde, int abPos) {
        return extract(musterStart, "", musterEnde, abPos, 0, "");
    }

    public String extract(String musterStart, String musterEnde, int abPos, int bisPos) {
        return extract(musterStart, "", musterEnde, abPos, bisPos, "");
    }

    public String extract(String musterStart1, String musterStart2, String musterEnde) {
        return extract(musterStart1, musterStart2, musterEnde, 0, 0, "");
    }

    public String extract(String musterStart1, String musterStart2, String musterEnde, String addUrl) {
        return extract(musterStart1, musterStart2, musterEnde, 0, 0, addUrl);
    }

//    public String extract(String musterStart, String musterEnde, int abPos, String addUrl) {
//        return extract(musterStart, "", musterEnde, abPos, 0, addUrl);
//    }
//    public String extract(String musterStart1, String musterStart2, String musterEnde, int abPos) {
//        return extract(musterStart1, musterStart2, musterEnde, abPos, 0, "");
//    }
//    public String extract(String musterStart1, String musterStart2, String musterStart3, String musterEnde, String addUrl) {
//        int p = indexOf(musterStart1);
//        if (p <= 0) {
//            p = 0;
//        }
//        return extract(musterStart2, musterStart3, musterEnde, p, 0, addUrl);
//    }
//    public String extract(String musterStart1, String musterStart2, String musterEnde, int abPos, String bisMuster, String addUrl) {
//        int bisPos = cont.indexOf(bisMuster, abPos);
//        return extract(musterStart1, musterStart2, musterEnde, abPos, bisPos, addUrl);
//    }
    public String extract(String musterStart1, String musterStart2, String musterEnde, int abPos, int bisPos, String addUrl) {
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
        String ret = cont.substring(pos1, pos2);
        if (!ret.isEmpty()) {
            // damit nicht nur! addUrl zurückkommt
            return addUrl + ret;
        }
        return "";
    }

    public void extractList(String musterStart, String musterEnde, ArrayList<String> result) {
        extractList("", "", musterStart, "", musterEnde, "", result);
//        try {
//            pos1 = 0;
//            while ((pos1 = cont.indexOf(musterStart, pos1)) != -1) {
//                pos1 += musterStart.length();
//                if ((pos2 = cont.indexOf(musterEnde, pos1)) != -1) {
//                    result.add(cont.substring(pos1, pos2));
//                    if (result.size() > 1000) {
//                        System.out.println("Achtung");
//                    }
//                }
//            }
//        } catch (Exception ex) {
//            MSLog.fehlerMeldung(741203025, ex);
//        }
    }

    public void extractList(String musterStart1, String musterStart2, String musterEnde, ArrayList<String> result) {
        extractList("", "", musterStart1, musterStart2, musterEnde, "", result);
//        try {
//            pos1 = 0;
//            while ((pos1 = cont.indexOf(musterStart1, pos1)) != -1) {
//                pos1 += musterStart1.length();
//                if ((pos2 = cont.indexOf(musterStart2, pos1)) != -1) {
//                    pos2 += musterStart2.length();
//                    int pos3;
//                    if ((pos3 = cont.indexOf(musterEnde, pos2)) != -1) {
//                        result.add(cont.substring(pos2, pos3));
//                        if (result.size() > 1000) {
//                            System.out.println("Achtung");
//                        }
//                    }
//                }
//            }
//        } catch (Exception ex) {
//            MSLog.fehlerMeldung(462310871, ex);
//        }
    }
    //    public void extractList(String musterStart, String musterEnde, int abPos, String addUrl, ArrayList<String> result) {
    //        pos1 = abPos;
    //        while ((pos1 = cont.indexOf(musterStart, pos1)) != -1) {
    //            pos1 += musterStart.length();
    //            if ((pos2 = cont.indexOf(musterEnde, pos1)) != -1) {
    //                String s = addUrl + cont.substring(pos1, pos2);
    //                if (!result.contains(s)) {
    //                    result.add(s);
    //                }
    //            }
    //        }
    //    }

    public void extractList(String abMuster, String bisMuster, String musterStart, String musterEnde, String addUrl, ArrayList<String> result) {
        extractList(abMuster, bisMuster, musterStart, "", musterEnde, addUrl, result);
//        if ((pos1 = cont.indexOf(abMuster)) != -1) {
//            int stopPos = cont.indexOf(bisMuster); // bei "" -> 0
//            while ((pos1 = cont.indexOf(musterStart, pos1)) != -1) {
//                pos1 += musterStart.length();
//                if ((pos2 = cont.indexOf(musterEnde, pos1)) != -1) {
//                    if (stopPos <= 0 || pos2 < stopPos) {
//                        String s = cont.substring(pos1, pos2);
//                        if (!s.isEmpty()) {
//                            s = addUrl + s;
//                            if (!result.contains(s)) {
//                                result.add(s);
//                            }
//                        }
//                    }
//                }
//            }
//        }
    }

    public void extractList(String abMuster, String bisMuster, String musterStart1, String musterStart2, String musterEnde, String addUrl, ArrayList<String> result) {
        int pos1 = 0, pos2 = 0;
        if (!abMuster.isEmpty() && (pos1 = cont.indexOf(abMuster)) == -1) {
            return;
        }
        int stopPos = bisMuster.isEmpty() ? -1 : cont.indexOf(bisMuster, pos1);

        while ((pos1 = cont.indexOf(musterStart1, pos1)) != -1) {
            pos1 += musterStart1.length();

            if (!musterStart2.isEmpty()) {
                if ((pos1 = cont.indexOf(musterStart2, pos1)) == -1) {
                    continue;
                }
                pos1 += musterStart2.length();
            }

            if ((pos2 = cont.indexOf(musterEnde, pos1)) == -1) {
                continue;
            }
            if (stopPos > 0 && pos2 > stopPos) {
                continue;
            }

            String s = cont.substring(pos1, pos2);
            if (s.isEmpty()) {
                continue;
            }
            s = addUrl + cont.substring(pos1, pos2);
            if (!result.contains(s)) {
                result.add(s);
            }
        }
    }

}
