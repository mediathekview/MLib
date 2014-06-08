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

import java.util.Iterator;
import java.util.LinkedList;

public class MSListeRunSender extends LinkedList<MSRunSender> {

    public boolean listeFertig() {
        // liefert true wenn alle Sender fertig sind
        // und löscht dann auch die Liste
        Iterator<MSRunSender> it = iterator();
        while (it.hasNext()) {
            if (!it.next().fertig) {
                return false;
            }
        }
        this.clear();
        return true;
    }

    public MSRunSender getSender(String sender) {
        Iterator<MSRunSender> it = iterator();
        while (it.hasNext()) {
            MSRunSender runSender = it.next();
            if (runSender.sender.equals(sender)) {
                return runSender;
            }
        }
        return null;
    }

    public MSRunSender senderFertig(String sender) {
        MSRunSender run = null;
        Iterator<MSRunSender> it = iterator();
        while (it.hasNext()) {
            run = it.next();
            if (run.sender.equals(sender)) {
                run.fertig = true;
                return run;
            }
        }
        return null;
    }

    public String getSenderRun() {
        MSRunSender run;
        String ret = "";
        Iterator<MSRunSender> it = iterator();
        while (it.hasNext()) {
            run = it.next();
            if (!run.fertig) {
                ret += run.sender + " ";
            }
        }
        return ret;
    }

    public int getAnzSenderRun() {
        int ret = 0;
        MSRunSender run;
        Iterator<MSRunSender> it = iterator();
        while (it.hasNext()) {
            run = it.next();
            if (!run.fertig) {
                ++ret;
            }
        }
        return ret;
    }

    public int getMax() {
        int ret = 0;
        Iterator<MSRunSender> it = iterator();
        while (it.hasNext()) {
            ret += it.next().max;
        }
        return ret;
    }

    public int getProgress() {
        int prog = 0;
        int max = 0;
        MSRunSender run = null;
        Iterator<MSRunSender> it = iterator();
        while (it.hasNext()) {
            run = it.next();
            prog += run.progress;
            max += run.max;
        }
        if (prog >= max && max >= 1) {
            prog = max - 1;
        }
        return prog;
    }
}
