/*
 *   MediathekView
 *   Copyright (C) 2013 W. Xaver
 *   W.Xaver[at]googlemail.com
 *   http://zdfmediathk.sourceforge.net/
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package msearch.tool;

import msearch.daten.DatenFilm;

public class MSLong implements Comparable<MSLong> {

    public Long l = 0L;
    public String s = "";

    public MSLong() {
    }

    public MSLong(long ll) {
        l = ll;
        s = l.toString();
    }

    public MSLong(DatenFilm film) {
        if (film.arr[DatenFilm.FILM_GROESSE_NR].equals("<1")) {
            film.arr[DatenFilm.FILM_GROESSE_NR] = "1";
        }
        try {
            if (!film.arr[DatenFilm.FILM_GROESSE_NR].isEmpty()) {
                l = Long.valueOf(film.arr[DatenFilm.FILM_GROESSE_NR]);
                s = film.arr[DatenFilm.FILM_GROESSE_NR];
            }
        } catch (Exception ex) {
            MSLog.fehlerMeldung(649891025, MSLog.FEHLER_ART_MREADER, MSLong.class.getName(), ex, "String: " + film.arr[DatenFilm.FILM_GROESSE_NR]);
            l = 0L;
            s = "";
        }
    }

    @Override
    public String toString() {
        return s;
    }

    @Override
    public int compareTo(MSLong ll) {
        return (l.compareTo(ll.l));
    }
}
