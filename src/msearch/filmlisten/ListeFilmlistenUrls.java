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
package msearch.filmlisten;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;

public class ListeFilmlistenUrls extends LinkedList<DatenFilmlisteUrl> {
    // ist die Liste mit den URLs zum Download einer Filmliste

    public boolean addWithCheck(DatenFilmlisteUrl filmliste) {
        for (DatenFilmlisteUrl datenUrlFilmliste : this) {
            if (datenUrlFilmliste.arr[DatenFilmlisteUrl.FILM_UPDATE_SERVER_URL_NR].equals(filmliste.arr[DatenFilmlisteUrl.FILM_UPDATE_SERVER_URL_NR])) {
                return false;
            }
        }
        return add(filmliste);
    }

    public void sort() {
        int nr = 0;
        Collections.sort(this);
        for (DatenFilmlisteUrl datenUrlFilmliste : this) {
            String str = String.valueOf(nr++);
            while (str.length() < 3) {
                str = "0" + str;
            }
            datenUrlFilmliste.arr[DatenFilmlisteUrl.FILM_UPDATE_SERVER_NR_NR] = str;
        }
    }

    public String[][] getTableObjectData() {
        DatenFilmlisteUrl filmUpdate;
        String[][] object;
        ListIterator<DatenFilmlisteUrl> iterator = this.listIterator();
        object = new String[this.size()][DatenFilmlisteUrl.FILM_UPDATE_SERVER_MAX_ELEM];
        int i = 0;
        while (iterator.hasNext()) {
            filmUpdate = iterator.next();
            System.arraycopy(filmUpdate.arr, 0, object[i], 0, DatenFilmlisteUrl.FILM_UPDATE_SERVER_MAX_ELEM);
            object[i][DatenFilmlisteUrl.FILM_UPDATE_SERVER_DATUM_NR] = filmUpdate.getDateStr(); // lokale Zeit anzeigen
            object[i][DatenFilmlisteUrl.FILM_UPDATE_SERVER_ZEIT_NR] = filmUpdate.getTimeStr(); // lokale Zeit anzeigen
            ++i;
        }
        return object;
    }

    public DatenFilmlisteUrl getDatenUrlFilmliste(String url) {
        DatenFilmlisteUrl update;
        for (DatenFilmlisteUrl datenUrlFilmliste : this) {
            update = datenUrlFilmliste;
            if (update.arr[DatenFilmlisteUrl.FILM_UPDATE_SERVER_URL_NR].equals(url)) {
                return update;
            }
        }
        return null;
    }

    public String getRand(ArrayList<String> bereitsGebraucht) {
        // gibt nur noch akt.xml und diff.xml und da sind alle Listen
        // aktuell, Prio: momentan sind alle Listen gleich gewichtet
        if (this.isEmpty()) {
            return "";
        }

        LinkedList<DatenFilmlisteUrl> listePrio = new LinkedList<>();
        //nach prio gewichten
        for (DatenFilmlisteUrl datenFilmlisteUrl : this) {
            if (bereitsGebraucht != null) {
                if (bereitsGebraucht.contains(datenFilmlisteUrl.arr[DatenFilmlisteUrl.FILM_UPDATE_SERVER_URL_NR])) {
                    // wurde schon versucht
                    continue;
                }
                if (datenFilmlisteUrl.arr[DatenFilmlisteUrl.FILM_UPDATE_SERVER_PRIO_NR].equals(DatenFilmlisteUrl.FILM_UPDATE_SERVER_PRIO_1)) {
                    listePrio.add(datenFilmlisteUrl);
                    listePrio.add(datenFilmlisteUrl);
                } else {
                    listePrio.add(datenFilmlisteUrl);
                    listePrio.add(datenFilmlisteUrl);
                    listePrio.add(datenFilmlisteUrl);
                }
            }
        }

        DatenFilmlisteUrl datenFilmlisteUrl;
        if (listePrio.size() > 0) {
            int nr = new Random().nextInt(listePrio.size());
            datenFilmlisteUrl = listePrio.get(nr);
        } else {
            // dann wird irgendeine Versucht
            int nr = new Random().nextInt(this.size());
            datenFilmlisteUrl = this.get(nr);
        }
        return datenFilmlisteUrl.arr[DatenFilmlisteUrl.FILM_UPDATE_SERVER_URL_NR];
    }

//    public String getRand(ArrayList<String> bereitsGebraucht, int errcount) {
//        final int MAXMINUTEN = 50;
//        int minCount = 3;
//        if (errcount > 0) {
//            minCount = 3 + 2 * errcount;
//        }
//        String ret = "";
//        if (!this.isEmpty()) {
//            DatenFilmlisteUrl datenUrlFilmliste;
//            LinkedList<DatenFilmlisteUrl> listeZeit = new LinkedList<>();
//            LinkedList<DatenFilmlisteUrl> listePrio = new LinkedList<>();
//            //aktuellsten auswählen
//            Iterator<DatenFilmlisteUrl> it = this.iterator();
//            Date today = new Date(System.currentTimeMillis());
//            Date d;
//            int minuten = 200;
//            int count = 0;
//            while (it.hasNext()) {
//                datenUrlFilmliste = it.next();
//                if (bereitsGebraucht != null) {
//                    if (bereitsGebraucht.contains(datenUrlFilmliste.arr[DatenFilmlisteUrl.FILM_UPDATE_SERVER_URL_NR])) {
//                        // wurde schon versucht
//                        continue;
//                    }
//                }
//                try {
//                    d = datenUrlFilmliste.getDate();
//                    // debug
//                    //SimpleDateFormat sdf_datum_zeit = new SimpleDateFormat("dd.MM.yyyy  HH:mm:ss");
//                    //String s = sdf_datum_zeit.format(d);
//                    long m = today.getTime() - d.getTime();
//                    if (m < 0) {
//                        m = 0;
//                    }
//                    minuten = Math.round(m / (1000 * 60));
//                } catch (Exception ignored) {
//                }
//                if (minuten < MAXMINUTEN) {
//                    listeZeit.add(datenUrlFilmliste);
//                    ++count;
//                } else if (count < minCount) {
//                    listeZeit.add(datenUrlFilmliste);
//                    ++count;
//                }
//            }
//            //nach prio gewichten
//            it = listeZeit.iterator();
//            while (it.hasNext()) {
//                datenUrlFilmliste = it.next();
//                if (datenUrlFilmliste.arr[DatenFilmlisteUrl.FILM_UPDATE_SERVER_PRIO_NR].equals(DatenFilmlisteUrl.FILM_UPDATE_SERVER_PRIO_1)) {
//                    listePrio.add(datenUrlFilmliste);
//                } else {
//                    listePrio.add(datenUrlFilmliste);
//                    listePrio.add(datenUrlFilmliste);
//                }
//            }
//            if (listePrio.size() > 0) {
//                int nr = new Random().nextInt(listePrio.size());
//                datenUrlFilmliste = listePrio.get(nr);
//            } else {
//                // dann wird irgendeine Versucht
//                int nr = new Random().nextInt(this.size());
//                datenUrlFilmliste = this.get(nr);
//            }
//            ret = datenUrlFilmliste.arr[DatenFilmlisteUrl.FILM_UPDATE_SERVER_URL_NR];
//        }
//        return ret;
//    }
}
