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

public class MSListenerFilmeLadenEvent {

    public String sender = "";
    public String text = "";
    public int max = 0;
    public int progress = 0;
    public boolean fehler = false;

    public MSListenerFilmeLadenEvent(String ssender, String ttext, int mmax, int pprogress, boolean ffehler) {
        sender = ssender;
        text = ttext;
        max = mmax;
        progress = pprogress;
        fehler = ffehler;
    }
}
