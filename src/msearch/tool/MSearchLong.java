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

import com.fasterxml.jackson.annotation.JsonIgnore;
import msearch.daten.DatenFilm;

public class MSearchLong implements Comparable<MSearchLong> {

    public Long l = 0L;
    public String s = "";

    public MSearchLong() {
    }

    @JsonIgnore
    public MSearchLong(long ll) {
        l = new Long(ll);
        s = l.toString();
    }

    @JsonIgnore
    public MSearchLong(DatenFilm film) {
        if (film.arr[DatenFilm.FILM_GROESSE_NR].equals("<1")) {
            film.arr[DatenFilm.FILM_GROESSE_NR] = "1";
        }
        try {
            if (!film.arr[DatenFilm.FILM_GROESSE_NR].isEmpty()) {
                l = new Long(Long.valueOf(film.arr[DatenFilm.FILM_GROESSE_NR]));
                s = film.arr[DatenFilm.FILM_GROESSE_NR];
            }
        } catch (Exception ex) {
            MSearchLog.fehlerMeldung(649891025, MSearchLog.FEHLER_ART_MREADER, MSearchLong.class.getName(), ex, "String: " + film.arr[DatenFilm.FILM_GROESSE_NR]);
            l = 0L;
            s = "";
        }
    }

    @Override
    @JsonIgnore
    public String toString() {
        return s;
    }

    @Override
    @JsonIgnore
    public int compareTo(MSearchLong ll) {
        return (l.compareTo(ll.l));
    }
}
