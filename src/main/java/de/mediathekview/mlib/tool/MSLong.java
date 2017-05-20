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
package de.mediathekview.mlib.tool;


import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Qualities;

public class MSLong implements Comparable<MSLong>
{

    public Long l = 0L;
    public String s = "";

    public MSLong()
    {
    }

    public MSLong(long ll)
    {
        l = ll;
        s = l.toString();
    }

    public MSLong(Film film)
    {
        l = film.getFileSize(Qualities.NORMAL);
        s = String.valueOf(film.getFileSize(Qualities.NORMAL));
    }

    @Override
    public String toString()
    {
        return s;
    }

    @Override
    public int compareTo(MSLong ll)
    {
        return (l.compareTo(ll.l));
    }
}
