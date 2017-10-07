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
package de.mediathekview.mlib.filmesuchen;

import java.util.EventListener;

@Deprecated
public class ListenerFilmeLaden implements EventListener {

  ListenerFilmeLadenEvent event;

  public void fertig(final ListenerFilmeLadenEvent e) {}

  public void fertigOnlyOne(final ListenerFilmeLadenEvent e) {
    // dient dem Melden des ersten Mal Laden der Filmliste beim ProgStart
  }

  public void progress(final ListenerFilmeLadenEvent e) {}

  public void start(final ListenerFilmeLadenEvent e) {}
}
