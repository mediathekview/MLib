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
package msearch.filmeSuchen;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

public class MSListeRunSender extends LinkedList<MSRunSender> {

    private final static String TRENNER = " | ";
    private static final String SENDER = " Sender ";

    public boolean listeFertig() {
        // liefert true wenn alle Sender fertig sind
        for (MSRunSender run : this) {
            if (!run.fertig) {
                return false;
            }
        }
        return true;
    }

    public MSRunSender getSender(String sender) {
        for (MSRunSender run : this) {
            if (run.sender.equals(sender)) {
                return run;
            }
        }
        return null;
    }

    public MSRunSender senderFertig(String sender) {
        for (MSRunSender run : this) {
            if (run.sender.equals(sender)) {
                run.fertig = true;
                run.endZeit = new Date();
                return run;
            }
        }
        return null;
    }

    public String getSenderRun() {
        String ret = "";
        for (MSRunSender run : this) {
            if (!run.fertig) {
                ret += run.sender + " ";
            }
        }
        return ret;
    }

    public int getAnzSenderRun() {
        int ret = 0;
        for (MSRunSender run : this) {
            if (!run.fertig) {
                ++ret;
            }
        }
        return ret;
    }

    public int getMax() {
        int ret = 0;
        for (MSRunSender run : this) {
            ret += run.max;
        }
        return ret;
    }

    public int getProgress() {
        int prog = 0;
        int max = 0;
        for (MSRunSender run : this) {
            prog += run.progress;
            max += run.max;
        }
        if (prog >= max && max >= 1) {
            prog = max - 1;
        }
        return prog;
    }

    public void inc(String sender, MSRunSender.Count what) {
        inc(sender, what.ordinal(), 1);
    }

    public void inc(String sender, MSRunSender.Count what, long i) {
        inc(sender, what.ordinal(), i);
    }

    public void inc(String sender, int what, long inc) {
        getCounter(sender).counter[what] += inc;
    }

    public long get(String sender, MSRunSender.Count what) {
        return getCounter(sender).counter[what.ordinal()];
    }

    public long get(String sender, int i) {
        return getCounter(sender).counter[i];
    }

    public long get(MSRunSender.Count what) {
        long ret = 0;
        for (MSRunSender run : this) {
            ret += run.counter[what.ordinal()];
        }
        return ret;
    }

    public ArrayList<String> getTextCount(ArrayList<String> ret) {
        getTextCount_(ret, new MSRunSender.Count[]{MSRunSender.Count.ANZAHL, MSRunSender.Count.FILME, MSRunSender.Count.FEHLER,
            MSRunSender.Count.FEHLVERSUCHE, MSRunSender.Count.WARTEZEIT_FEHLVERSUCHE,
            MSRunSender.Count.PROXY, MSRunSender.Count.NO_BUFFER});
        ret.add("");
        ret.add("");

        getTextCount_(ret, new MSRunSender.Count[]{MSRunSender.Count.SUM_DATA_BYTE, MSRunSender.Count.SUM_TRAFFIC_BYTE,
            MSRunSender.Count.SUM_TRAFFIC_LOADART_NIX, MSRunSender.Count.SUM_TRAFFIC_LOADART_DEFLATE, MSRunSender.Count.SUM_TRAFFIC_LOADART_GZIP,
            MSRunSender.Count.GET_SIZE_SUM, MSRunSender.Count.GET_SIZE_SUM403, MSRunSender.Count.GET_SIZE_PROXY});

        ret.add("");
        ret.add("");
        return ret;
    }

    public void getTextSum(ArrayList<String> retArray) {
        //wird ausgeführt wenn Sender beendet ist
        final String[] titel1 = {" Sender ", " [min] ", " [kB/s] ", "s/Seite", "Threads", "Wait"};
        String zeile = "";
        String[] names = new String[titel1.length];
        for (int i = 0; i < titel1.length; ++i) {
            names[i] = titel1[i];
            zeile += textLaenge(names[i].length(), names[i]) + TRENNER;
        }
        retArray.add(zeile);
        retArray.add("-------------------------------------------------------");

        for (MSRunSender run : this) {
            int dauerSender = run.getLaufzeitSekunden();
            long groesseByte = this.get(run.sender, MSRunSender.Count.SUM_TRAFFIC_BYTE);
            long anzahlSeiten = this.get(run.sender, MSRunSender.Count.ANZAHL);

            String rate = "";
            if (groesseByte > 0 && dauerSender > 0) {
                double doub = (1.0 * groesseByte / dauerSender / 1000); // kB/s
                rate = doub < 1 ? "<1" : String.format("%.1f", (doub));
            }

            String dauerProSeite = "";
            if (anzahlSeiten > 0) {
                dauerProSeite = String.format("%.2f", (1.0 * dauerSender / anzahlSeiten));
            }

            // =================================
            // Zeile1
            zeile = textLaenge(titel1[0].length(), run.sender) + TRENNER;
            zeile += textLaenge(titel1[1].length(), run.getLaufzeitMinuten()) + TRENNER;
            zeile += textLaenge(titel1[2].length(), rate) + TRENNER;
            zeile += textLaenge(titel1[3].length(), dauerProSeite) + TRENNER;
            zeile += textLaenge(titel1[4].length(), run.maxThreads + "") + TRENNER;
            zeile += textLaenge(titel1[5].length(), run.waitOnLoad + "") + TRENNER;
            retArray.add(zeile);
        }
        retArray.add("");
        retArray.add("");
    }

    private MSRunSender getCounter(String sender) {
        for (MSRunSender run : this) {
            if (run.sender.equals(sender)) {
                return run;
            }
        }
        MSRunSender ret = new MSRunSender(sender, 0, 0);
        add(ret);
        return ret;
    }

    private ArrayList<String> getTextCount_(ArrayList<String> ret, MSRunSender.Count[] spalten) {
        String zeile;
        String[] names = MSRunSender.Count.getNames();

        // Titelzeile
        zeile = SENDER + TRENNER;
        for (int i = 0; i < names.length; ++i) {
            // alle Spalten checken, ob gebraucht
            for (MSRunSender.Count sp : spalten) {
                if (i == sp.ordinal()) {
                    zeile += textLaenge(names[i].length(), names[i]) + TRENNER;
                }
            }
        }

        ret.add(zeile);
        ret.add("-------------------------------------------------------");

        for (MSRunSender run : this) {
            zeile = textLaenge(SENDER.length(), run.sender) + TRENNER;
            for (int i = 0; i < names.length; ++i) {
                // alle Spalten chekcken, ob gebraucht
                for (MSRunSender.Count sp : spalten) {
                    if (i == sp.ordinal()) {
                        if (i == MSRunSender.Count.SUM_DATA_BYTE.ordinal() || i == MSRunSender.Count.SUM_TRAFFIC_BYTE.ordinal()
                                || i == MSRunSender.Count.SUM_TRAFFIC_LOADART_DEFLATE.ordinal()
                                || i == MSRunSender.Count.SUM_TRAFFIC_LOADART_GZIP.ordinal()
                                || i == MSRunSender.Count.SUM_TRAFFIC_LOADART_NIX.ordinal()) {
                            zeile += textLaenge(names[i].length(), String.valueOf(MSRunSender.getStringZaehler(get(run.sender, i)))) + TRENNER;
                        } else if (i == MSRunSender.Count.WARTEZEIT_FEHLVERSUCHE.ordinal()) {
                            long l = get(run.sender, i); // dann sinds ms
                            zeile += textLaenge(names[i].length(), String.valueOf(l == 0 ? "0" : (l < 1000 ? "<1" : l / 1000))) + TRENNER;
                        } else {
                            zeile += textLaenge(names[i].length(), String.valueOf(get(run.sender, i))) + TRENNER;
                        }
                    }
                }
            }
            ret.add(zeile);
        }
        ret.add("");
        return ret;
    }

    private String textLaenge(int max, String text) {
        if (text.length() > max) {
            text = text.substring(0, max - 1);
        }
        while (text.length() < max) {
            text = text + " ";
        }
        return text;
    }

}
