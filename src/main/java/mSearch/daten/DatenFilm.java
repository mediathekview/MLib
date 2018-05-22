/*
 *   MediathekView
 *   Copyright (C) 2008 W. Xaver
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
package mSearch.daten;

import mSearch.Const;
import mSearch.tool.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import sun.misc.Cleaner;

import java.sql.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class DatenFilm implements AutoCloseable, Comparable<DatenFilm> {
    public static final String AUFLOESUNG_NORMAL = "normal";
    public static final String AUFLOESUNG_HD = "hd";
    public static final String AUFLOESUNG_KLEIN = "klein";
    public static final String GEO_DE = "DE"; // nur in .. zu sehen
    public static final String GEO_AT = "AT";
    public static final String GEO_CH = "CH";
    public static final String GEO_EU = "EU";
    public static final String GEO_WELT = "WELT";
    //
    public static final int FILM_NR = 0; // wird vor dem Speichern gelöscht!
    public static final int FILM_SENDER = 1;
    public static final int FILM_THEMA = 2;
    public static final int FILM_TITEL = 3;
    public static final int FILM_ABSPIELEN = 4;
    public static final int FILM_AUFZEICHNEN = 5;
    public static final int FILM_DATUM = 6;
    public static final int FILM_ZEIT = 7;
    public static final int FILM_DAUER = 8;
    public static final int FILM_GROESSE = 9;
    public static final int FILM_HD = 10;
    public static final int FILM_UT = 11;
    public static final int FILM_GEO = 12;// Geoblocking
    public static final int FILM_URL = 13;
    public static final int FILM_ABO_NAME = 14;// wird vor dem Speichern gelöscht!
    public static final int FILM_URL_SUBTITLE = 15;
    public static final int FILM_URL_KLEIN = 16;
    public static final int FILM_URL_HD = 17;
    public static final int FILM_URL_HISTORY = 18;
    public static final int FILM_DATUM_LONG = 19;// Datum als Long ABER Sekunden!!
    public static final int FILM_WEBSEITE = 20; //URL der Website des Films beim Sender
    public static final int FILM_BESCHREIBUNG = 21;
    public static final int FILM_REF = 22;// Referenz auf this
    public static final int MAX_ELEM = 23;

    //Indices without storage context !!!
    public static final int FILM_NEU = 23;

    //TODO get rid out of DatenFilm
    public static final String[] COLUMN_NAMES = new String[MAX_ELEM];
    /**
     * The database instance for all descriptions.
     */
    private final static AtomicInteger FILM_COUNTER = new AtomicInteger(0);
    private static final GermanStringSorter sorter = GermanStringSorter.getInstance();
    private static final Logger logger = LogManager.getLogger(DatenFilm.class);
    public static boolean[] spaltenAnzeigen = new boolean[MAX_ELEM];

    static {
        COLUMN_NAMES[FILM_NR] = "Nr";
        COLUMN_NAMES[FILM_SENDER] = "Sender";
        COLUMN_NAMES[FILM_THEMA] = "Thema";
        COLUMN_NAMES[FILM_TITEL] = "Titel";
        COLUMN_NAMES[FILM_ABSPIELEN] = "";
        COLUMN_NAMES[FILM_AUFZEICHNEN] = "";
        COLUMN_NAMES[FILM_DATUM] = "Datum";
        COLUMN_NAMES[FILM_ZEIT] = "Zeit";
        COLUMN_NAMES[FILM_DAUER] = "Dauer";
        COLUMN_NAMES[FILM_GROESSE] = "Größe [MB]";
        COLUMN_NAMES[FILM_HD] = "HD";
        COLUMN_NAMES[FILM_UT] = "UT";
        COLUMN_NAMES[FILM_BESCHREIBUNG] = "Beschreibung";
        COLUMN_NAMES[FILM_GEO] = "Geo";
        COLUMN_NAMES[FILM_URL] = "URL";
        COLUMN_NAMES[FILM_WEBSEITE] = "Website";
        COLUMN_NAMES[FILM_ABO_NAME] = "Abo";
        COLUMN_NAMES[FILM_URL_SUBTITLE] = "URL Untertitel";
        COLUMN_NAMES[FILM_URL_KLEIN] = "URL Klein";
        COLUMN_NAMES[FILM_URL_HD] = "URL HD";
        COLUMN_NAMES[FILM_URL_HISTORY] = "URL History";
        COLUMN_NAMES[FILM_REF] = "Ref";
        COLUMN_NAMES[FILM_DATUM_LONG] = "DatumL";
    }

    static {
        try {
            Database.initializeDatabase();
        } catch (SQLException ignored) {
        }
    }

    public final String[] arr = new String[MAX_ELEM];
    /**
     * film date stored IN SECONDS!!!
     */
    public DatumFilm datumFilm = new DatumFilm(0);
    /**
     * film length in seconds.
     */
    public long dauerL = 0;
    public Object abo = null;
    public MSLong dateigroesseL; // Dateigröße in MByte
    /**
     * Die Filmnr
     */
    public int nr;
    /**
     * Internal film number, used for storage in cache map
     */
    private int filmNr;
    private boolean neuerFilm = false;
    private Cleaner cleaner;
    private CompletableFuture<Void> descriptionFuture;
    private CompletableFuture<Void> websiteFuture;

    public DatenFilm() {
        setupArr();

        dateigroesseL = new MSLong(0); // Dateigröße in MByte
        filmNr = FILM_COUNTER.getAndIncrement();

        DatenFilmCleanupTask task = new DatenFilmCleanupTask(filmNr);
        cleaner = Cleaner.create(this, task);
    }

    private void setupArr() {
        for (int i = 0; i < MAX_ELEM; i++)
            arr[i] = "";
    }

    public String getTitle() {
        return arr[FILM_TITEL];
    }

    public String getThema() {
        return arr[FILM_THEMA];
    }

    /**
     * Return the film size.
     *
     * @return size as a string
     */
    public String getSize() {
        return arr[FILM_GROESSE];
    }

    public String getSender() {
        return arr[FILM_SENDER];
    }

    @Override
    public void close() {
        cleaner.clean();
    }

    /**
     * Get the film description from database.
     *
     * @return the film description.
     */
    public String getDescription() {
        String sqlStr;

        try (Connection connection = PooledDatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT desc FROM description WHERE id = ?")) {
            statement.setInt(1, filmNr);

            if (descriptionFuture != null) {
                if (!descriptionFuture.isDone()) {
                    descriptionFuture.get();
                }
                //set to null when finished as we don´t need it anymore...
                descriptionFuture = null;
            }

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    sqlStr = rs.getString(1);
                } else
                    sqlStr = "";
            }
        } catch (SQLException | InterruptedException | ExecutionException e) {
            logger.error(e);
            sqlStr = "";
        }

        return sqlStr;
    }

    /**
     * Store description in database.
     *
     * @param desc String to be stored.
     */
    public void setDescription(final String desc) {
        if (desc != null && !desc.isEmpty()) {
            descriptionFuture = CompletableFuture.runAsync(() -> {
                try (Connection connection = PooledDatabaseConnection.getInstance().getConnection();
                     PreparedStatement statement = connection.prepareStatement("INSERT INTO description VALUES (?,?)")) {
                    String cleanedDesc = cleanDescription(desc, arr[FILM_THEMA], arr[FILM_TITEL]);
                    cleanedDesc = StringUtils.replace(cleanedDesc, "\n", "<br />");

                    statement.setInt(1, filmNr);
                    statement.setString(2, cleanedDesc);
                    statement.executeUpdate();
                } catch (SQLException ex) {
                    logger.error(ex);
                }
            });
        }
    }

    public String getWebsiteLink() {
        String res;

        try (Connection connection = PooledDatabaseConnection.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT link FROM website_links WHERE id = ?")) {
            statement.setInt(1, filmNr);

            if (websiteFuture != null) {
                if (!websiteFuture.isDone()) {
                    //make sure async op has finished before...
                    websiteFuture.get();
                }

                //we don´t need it anymore...
                websiteFuture = null;
            }

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    res = rs.getString(1);
                } else
                    res = "";
            }
        } catch (SQLException | InterruptedException | ExecutionException ex) {
            logger.error(ex);
            res = "";
        }

        return res;
    }

    public void setWebsiteLink(String link) {
        if (link != null && !link.isEmpty()) {
            websiteFuture = CompletableFuture.runAsync(() -> {
                try (Connection connection = PooledDatabaseConnection.getInstance().getConnection();
                     PreparedStatement statement = connection.prepareStatement("INSERT INTO website_links VALUES (?,?)")) {
                    statement.setInt(1, filmNr);
                    statement.setString(2, link);
                    statement.executeUpdate();
                } catch (SQLException ex) {
                    logger.error(ex);
                }
            });
        }
    }

    private String cleanDescription(String s, String thema, String titel) {
        // die Beschreibung auf x Zeichen beschränken

        s = Functions.removeHtml(s); // damit die Beschreibung nicht unnötig kurz wird wenn es erst später gemacht wird

        if (s.startsWith(titel)) {
            s = s.substring(titel.length()).trim();
        }
        if (s.startsWith(thema)) {
            s = s.substring(thema.length()).trim();
        }
        if (s.startsWith("|")) {
            s = s.substring(1).trim();
        }
        if (s.startsWith("Video-Clip")) {
            s = s.substring("Video-Clip".length()).trim();
        }
        if (s.startsWith(titel)) {
            s = s.substring(titel.length()).trim();
        }
        if (s.startsWith(":") || s.startsWith(",") || s.startsWith("\n")) {
            s = s.substring(1).trim();
        }

        if (s.contains("\\\"")) { // wegen " in json-Files
            s = StringUtils.replace(s, "\\\"", "\"");
        }

        return s;
    }

    public boolean isNew() {
        return neuerFilm;
    }

    public void setNew(final boolean newFilm) {
        neuerFilm = newFilm;
    }

    public String getUrlSubtitle() {
        return arr[FILM_URL_SUBTITLE];
    }

    public boolean hasSubtitle() {
        //Film hat Untertitel
        return !arr[DatenFilm.FILM_URL_SUBTITLE].isEmpty();
    }

    public String getUrlFuerAufloesung(String aufloesung) {
        final String ret;
        switch (aufloesung) {
            case AUFLOESUNG_KLEIN:
                ret = getUrlNormalOrRequested(DatenFilm.FILM_URL_KLEIN);
                break;

            case AUFLOESUNG_HD:
                ret = getUrlNormalOrRequested(DatenFilm.FILM_URL_HD);
                break;

            default://AUFLOESUNG_NORMAL
                ret = arr[DatenFilm.FILM_URL];
                break;
        }

        return ret;
    }

    public String getDateigroesse(String url) {
        if (url.equals(arr[DatenFilm.FILM_URL])) {
            return arr[DatenFilm.FILM_GROESSE];
        } else {
            return FileSize.laengeString(url);
        }
    }

    public String getUrlHistory() {
        if (arr[DatenFilm.FILM_URL_HISTORY].isEmpty()) {
            return arr[DatenFilm.FILM_URL];
        } else {
            return arr[DatenFilm.FILM_URL_HISTORY];
        }
    }

    public String getIndex() {
        // liefert einen eindeutigen Index für die Filmliste
        // URL beim KiKa und ORF ändern sich laufend!
        return (arr[FILM_SENDER] + arr[FILM_THEMA]).toLowerCase() + getUrl();
    }

    public String getUrl() {
        // liefert die URL zum VERGLEICHEN!!
        String url = "";
        if (arr[DatenFilm.FILM_SENDER].equals(Const.ORF)) {
            final String uurl = arr[DatenFilm.FILM_URL];
            try {
                final String online = "/online/";
                url = uurl.substring(uurl.indexOf(online) + online.length());
                if (!url.contains("/")) {
                    Log.errorLog(915230478, "Url: " + uurl);
                    return "";
                }
                url = url.substring(url.indexOf('/') + 1);
                if (!url.contains("/")) {
                    Log.errorLog(915230478, "Url: " + uurl);
                    return "";
                }
                url = url.substring(url.indexOf('/') + 1);
                if (url.isEmpty()) {
                    Log.errorLog(915230478, "Url: " + uurl);
                    return "";
                }
            } catch (Exception ex) {
                Log.errorLog(915230478, ex, "Url: " + uurl);
            }
            return Const.ORF + "----" + url;
        } else {
            return arr[DatenFilm.FILM_URL];
        }

    }

    public boolean isHD() {
        //Film gibts in HD
        return !arr[DatenFilm.FILM_URL_HD].isEmpty();
    }

    public DatenFilm getCopy() {
        DatenFilm ret = new DatenFilm();
        System.arraycopy(this.arr, 0, ret.arr, 0, arr.length);
        ret.datumFilm = this.datumFilm;
        ret.nr = this.nr;
        ret.dateigroesseL = this.dateigroesseL;
        ret.dauerL = this.dauerL;
        ret.abo = this.abo;
        return ret;
    }

    @Override
    public int compareTo(@NotNull DatenFilm other) {
        int ret;
        if ((ret = sorter.compare(arr[FILM_SENDER], other.arr[FILM_SENDER])) == 0) {
            return sorter.compare(arr[FILM_THEMA], other.arr[FILM_THEMA]);
        }
        return ret;
    }

    private void setFilmdauer() {
        //TODO OPTIMIZE ME!
        try {
            dauerL = 0;
            if (!this.arr[DatenFilm.FILM_DAUER].isEmpty()) {
                String[] parts = this.arr[DatenFilm.FILM_DAUER].split(":");
                long power = 1;
                for (int i = parts.length - 1; i >= 0; i--) {
                    dauerL += Long.parseLong(parts[i]) * power;
                    power *= 60;
                }
            }
        } catch (Exception ex) {
            dauerL = 0;
            logger.error("Dauer: {}", this.arr[DatenFilm.FILM_DAUER], ex);
        }
    }

    private void setDatum() {
        if (!arr[DatenFilm.FILM_DATUM].isEmpty()) {
            // nur dann gibts ein Datum
            try {
                final long l = Long.parseLong(arr[DatenFilm.FILM_DATUM_LONG]);
                datumFilm = new DatumFilm(l * 1000); // sind SEKUNDEN!!
            } catch (Exception ex) {
                logger.error("Datum: {}, Zeit: {}", arr[DatenFilm.FILM_DATUM], arr[DatenFilm.FILM_ZEIT], ex);
                datumFilm = new DatumFilm(0);
                arr[DatenFilm.FILM_DATUM] = "";
                arr[DatenFilm.FILM_ZEIT] = "";
            }
        }
    }

    public void init() {
        dateigroesseL = new MSLong(this);

        setFilmdauer();

        setDatum();
    }

    private String getUrlNormalOrRequested(int indexUrl) {
        // liefert die kleine normale URL
        if (!arr[indexUrl].isEmpty()) {
            try {
                // Prüfen, ob Pipe auch in URL enthalten ist. Beim ZDF ist das nicht der Fall.
                final int indexPipe = arr[indexUrl].indexOf('|');
                if (indexPipe < 0) {
                    return arr[indexUrl];
                }

                final int i = Integer.parseInt(arr[indexUrl].substring(0, indexPipe));
                return arr[DatenFilm.FILM_URL].substring(0, i) + arr[indexUrl].substring(arr[indexUrl].indexOf('|') + 1);
            } catch (Exception e) {
                Log.errorLog(915236703, e, arr[indexUrl]);
            }
        }
        return arr[DatenFilm.FILM_URL];
    }

    public static class Database {
        private Database() {
        }

        public static void closeDatabase() {
            try (Connection connection = PooledDatabaseConnection.getInstance().getConnection();
                 Statement statement = connection.createStatement()) {
                statement.executeUpdate("DROP TABLE IF EXISTS description");
                statement.executeUpdate("DROP TABLE IF EXISTS website_links");
                statement.executeUpdate("SHUTDOWN COMPACT");
            } catch (SQLException ignored) {
            }
            PooledDatabaseConnection.getInstance().close();
        }

        private static void initializeDatabase() throws SQLException {
            try (Connection connection = PooledDatabaseConnection.getInstance().getConnection();
                 Statement statement = connection.createStatement()) {
                statement.executeUpdate("SET DATABASE TRANSACTION CONTROL MVCC");
                //only increase cache size if there is enough memory
                if (!MemoryUtils.isLowMemoryEnvironment()) {
                    statement.executeUpdate("SET FILES CACHE ROWS 100000");
                    statement.executeUpdate("SET FILES CACHE SIZE 50000");
                }

                statement.executeUpdate("DROP TABLE IF EXISTS description");
                statement.executeUpdate("CREATE CACHED TABLE IF NOT EXISTS description (id INTEGER NOT NULL PRIMARY KEY, desc VARCHAR(1024))");
                statement.executeUpdate("DROP TABLE IF EXISTS website_links");
                statement.executeUpdate("CREATE CACHED TABLE IF NOT EXISTS website_links (id INTEGER NOT NULL PRIMARY KEY, link VARCHAR(1024))");
            }
        }
    }

}
