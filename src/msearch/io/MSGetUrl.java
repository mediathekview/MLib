/*
 *  MediathekView
 *  Copyright (C) 2008 W. Xaver
 *  W.Xaver[at]googlemail.com
 *  http://zdfmediathk.sourceforge.net/
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package msearch.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import msearch.daten.MSConfig;
import msearch.tool.MSConst;
import msearch.tool.MSLog;
import msearch.tool.MSStringBuilder;

public class MSGetUrl {

    public static final int LISTE_SEITEN_ZAEHLER = 1;
    public static final int LISTE_SEITEN_ZAEHLER_FEHlER = 2;
    public static final int LISTE_SEITEN_ZAEHLER_FEHLERVERSUCHE = 3;
    public static final int LISTE_SEITEN_ZAEHLER_WARTEZEIT_FEHLVERSUCHE = 4;
    public static final int LISTE_SUMME_KBYTE = 5;
    public static final int LISTE_SEITEN_PROXY = 6;
    public static final int LISTE_SEITEN_NO_BUFFER = 7;
    private static final long UrlWartenBasis = 500;//ms, Basiswert zu dem dann der Faktor multipliziert wird
//    private int faktorWarten = 1;
    private int timeout = 10000;
    private long wartenBasis = UrlWartenBasis;
    private static final LinkedList<Seitenzaehler> listeSeitenZaehler = new LinkedList<>();
    private static final LinkedList<Seitenzaehler> listeSeitenZaehlerFehler = new LinkedList<>();
    private static final LinkedList<Seitenzaehler> listeSeitenZaehlerFehlerVersuche = new LinkedList<>();
    private static final LinkedList<Seitenzaehler> listeSeitenZaehlerWartezeitFehlerVersuche = new LinkedList<>(); // Wartezeit für Wiederholungen [s]
    private static final LinkedList<Seitenzaehler> listeSummeByte = new LinkedList<>(); // Summe Daten in Byte für jeden Sender
    private static final LinkedList<Seitenzaehler> listeSeitenProxy = new LinkedList<>(); // Anzahl Seiten über Proxy geladen
    private static final LinkedList<Seitenzaehler> listeSeitenNoBuffer = new LinkedList<>(); // Anzahl Seiten bei BufferOverRun
    private static final int LADE_ART_UNBEKANNT = 0;
    private static final int LADE_ART_NIX = 1;
    private static final int LADE_ART_DEFLATE = 2;
    private static final int LADE_ART_GZIP = 3;
    final static Lock lock = new ReentrantLock();
    private static long summeByte = 0;

    private class Seitenzaehler {

        String senderName = "";
        long seitenAnzahl = 0;
        long ladeArtNix = 0;
        long ladeArtDeflate = 0;
        long ladeArtGzip = 0;

        public Seitenzaehler(String ssenderName, long sseitenAnzahl) {
            senderName = ssenderName;
            seitenAnzahl = sseitenAnzahl;
        }

        public void addLadeArt(int ladeArt, long inc) {
            switch (ladeArt) {
                case LADE_ART_NIX:
                    ladeArtNix += inc;
                    break;
                case LADE_ART_DEFLATE:
                    ladeArtDeflate += inc;
                    break;
                case LADE_ART_GZIP:
                    ladeArtGzip += inc;
                    break;
                default:
            }
        }
    }

    public MSGetUrl(long wwartenBasis) {
        wartenBasis = wwartenBasis;
    }

    //===================================
    // public
    //===================================
    public MSStringBuilder getUri_Utf(String sender, String addr, MSStringBuilder seite, String meldung) {
        return getUri(sender, addr, MSConst.KODIERUNG_UTF, 1 /* versuche */, seite, meldung);
    }

    public MSStringBuilder getUri_Iso(String sender, String addr, MSStringBuilder seite, String meldung) {
        return getUri(sender, addr, MSConst.KODIERUNG_ISO15, 1 /* versuche */, seite, meldung);
    }

    public synchronized MSStringBuilder getUri(String sender, String addr, String kodierung, int maxVersuche, MSStringBuilder seite, String meldung) {
        final int PAUSE = 1000;
        int aktTimeout = timeout;
        int aktVer = 0;
        int wartezeit;
        boolean letzterVersuch;
        do {
            ++aktVer;
            try {
                if (aktVer > 1) {
                    // und noch eine Pause vor dem nächsten Versuch
                    this.wait(PAUSE);
                }
                letzterVersuch = (aktVer >= maxVersuche);
                seite = getUri(sender, addr, seite, kodierung, aktTimeout, meldung, maxVersuche, letzterVersuch);
                if (seite.length() > 0) {
                    // und nix wie weiter 
                    if (MSConfig.debug && aktVer > 1) {
                        String text = sender + " [" + aktVer + "/" + maxVersuche + "] ~~~> " + addr;
                        MSLog.systemMeldung(text);
                    }
                    // nur dann zählen
                    incSeitenZaehler(LISTE_SEITEN_ZAEHLER, sender, 1, LADE_ART_UNBEKANNT);
                    return seite;
                } else {
                    // hat nicht geklappt
                    if (aktVer > 1) {
                        wartezeit = (aktTimeout + PAUSE);
                    } else {
                        wartezeit = (aktTimeout);
                    }
                    incSeitenZaehler(LISTE_SEITEN_ZAEHLER_WARTEZEIT_FEHLVERSUCHE, sender, wartezeit / 1000, LADE_ART_UNBEKANNT);
                    incSeitenZaehler(LISTE_SEITEN_ZAEHLER_FEHLERVERSUCHE, sender, 1, LADE_ART_UNBEKANNT);
                    if (letzterVersuch) {
                        // dann wars leider nichts
                        incSeitenZaehler(LISTE_SEITEN_ZAEHLER_FEHlER, sender, 1, LADE_ART_UNBEKANNT);
                    }
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(698963200, MSLog.FEHLER_ART_GETURL, MSGetUrl.class.getName() + ".getUri", ex, sender);
            }
        } while (!MSConfig.getStop() && aktVer < maxVersuche);
        return seite;
    }

    public void setTimeout(int ttimeout) {
        timeout = ttimeout;
    }

    public int getTimeout() {
        return timeout;
    }

    public static synchronized String getSummeMegaByte() {
        // liefert MB zurück
        return summeByte == 0 ? "0" : ((summeByte / 1024 / 1024) == 0 ? "<1" : String.valueOf(summeByte / 1024 / 1024));
    }

    public static synchronized long getSeitenZaehler(int art, String sender) {
        long ret = 0;
        LinkedList<Seitenzaehler> liste = getListe(art);
        Iterator<Seitenzaehler> it = liste.iterator();
        Seitenzaehler sz;
        while (it.hasNext()) {
            sz = it.next();
            if (sz.senderName.equals(sender)) {
                ret = sz.seitenAnzahl;
            }
        }
        if (art == LISTE_SUMME_KBYTE) {
            // Byte in kByte
            ret = ret / 1024;
        }
        return ret;
    }

    public static synchronized long getSeitenZaehler(int art) {
        long ret = 0;
        LinkedList<Seitenzaehler> liste = getListe(art);
        for (Seitenzaehler entry : liste) {
            ret += entry.seitenAnzahl;
        }
        if (art == LISTE_SUMME_KBYTE) {
            // Byte in kByte
            ret = ret / 1024;
        }
        return ret;
    }

    public static synchronized String[] getZaehlerLadeArt(String sender) {
        String[] ret = {"", "", ""};
        LinkedList<Seitenzaehler> liste = getListe(LISTE_SUMME_KBYTE);
        for (Seitenzaehler sz : liste) {
            if (sz.senderName.equals(sender)) {
                ret[0] = (sz.ladeArtNix == 0 ? "0" : ((sz.ladeArtNix / 1024 / 1024) == 0 ? "<1" : String.valueOf(sz.ladeArtNix / 1024 / 1024)));
                ret[1] = (sz.ladeArtDeflate == 0 ? "0" : ((sz.ladeArtDeflate / 1024 / 1024) == 0 ? "<1" : String.valueOf(sz.ladeArtDeflate / 1024 / 1024)));
                ret[2] = (sz.ladeArtGzip == 0 ? "0" : ((sz.ladeArtGzip / 1024 / 1024) == 0 ? "<1" : String.valueOf(sz.ladeArtGzip / 1024 / 1024)));
            }
        }
        return ret;
    }

    public static synchronized void resetZaehler() {
        listeSeitenZaehler.clear();
        listeSeitenZaehlerFehler.clear();
        listeSeitenZaehlerFehlerVersuche.clear();
        listeSeitenZaehlerWartezeitFehlerVersuche.clear();
        listeSummeByte.clear();
        listeSeitenProxy.clear();
        listeSeitenNoBuffer.clear();
        summeByte = 0;
    }

    //===================================
    // private
    //===================================
    private static synchronized LinkedList<Seitenzaehler> getListe(int art) {
        switch (art) {
            case LISTE_SEITEN_ZAEHLER:
                return listeSeitenZaehler;
            case LISTE_SEITEN_ZAEHLER_FEHLERVERSUCHE:
                return listeSeitenZaehlerFehlerVersuche;
            case LISTE_SEITEN_ZAEHLER_FEHlER:
                return listeSeitenZaehlerFehler;
            case LISTE_SEITEN_ZAEHLER_WARTEZEIT_FEHLVERSUCHE:
                return listeSeitenZaehlerWartezeitFehlerVersuche;
            case LISTE_SUMME_KBYTE:
                return listeSummeByte;
            case LISTE_SEITEN_PROXY:
                return listeSeitenProxy;
            case LISTE_SEITEN_NO_BUFFER:
                return listeSeitenNoBuffer;
            default:
                return null;
        }
    }

    private void incSeitenZaehler(int art, String sender, long inc, int ladeArt) {
        lock.lock();
        try {
            boolean gefunden = false;
            LinkedList<Seitenzaehler> liste = getListe(art);
            Iterator<Seitenzaehler> it = liste.iterator();
            Seitenzaehler sz;
            while (it.hasNext()) {
                sz = it.next();
                if (sz.senderName.equals(sender)) {
                    sz.seitenAnzahl += inc;
                    sz.addLadeArt(ladeArt, inc);
                    gefunden = true;
                    break;
                }
            }
            if (!gefunden) {
                Seitenzaehler s = new Seitenzaehler(sender, inc);
                s.addLadeArt(ladeArt, inc);
                liste.add(s);
            }
        } catch (Exception ignored) {
        } finally {
            lock.unlock();
        }
    }

    private synchronized MSStringBuilder getUri(String sender, String addr, MSStringBuilder seite, String kodierung, int timeout, String meldung, int versuch, boolean lVersuch) {
        seite.setLength(0);
        HttpURLConnection conn = null;
        InputStream in = null;
        InputStreamReader inReader = null;
        int retCode;
        int ladeArt = LADE_ART_UNBEKANNT;
        MVInputStream mvIn = null;
        String encoding;
        // immer etwas bremsen
        try {
            long w = wartenBasis;// * faktorWarten;
            this.wait(w);
        } catch (Exception ex) {
            MSLog.fehlerMeldung(976120379, MSLog.FEHLER_ART_GETURL, MSGetUrl.class.getName() + ".getUri", ex, sender);
        }
        try {
            // conn = url.openConnection(Proxy.NO_PROXY);
            conn = (HttpURLConnection) new URL(addr).openConnection();
            conn.setRequestProperty("User-Agent", MSConfig.getUserAgent());
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
            if (timeout > 0) {
                conn.setReadTimeout(timeout);
                conn.setConnectTimeout(timeout);
            }
            // the encoding returned by the server
            encoding = conn.getContentEncoding();
            if ((retCode = conn.getResponseCode()) < 400) {
                mvIn = new MVInputStream(conn);
            } else {
                MSLog.fehlerMeldung(302160789, MSLog.FEHLER_ART_GETURL, MSGetUrl.class.getName() + ".getUri",
                        new String[]{"HTTP-Fehlercode: " + retCode, "Sender: " + sender, "URL: " + addr,});
                if (retCode == 403 || retCode == 408) {
                    if (!MSConfig.proxyUrl.isEmpty() && MSConfig.proxyPort > 0) {
                        // nur dann verwenden
                        // ein anderer Versuch
                        // wenn möglich, einen Proxy einrichten
                        //SocketAddress saddr = new InetSocketAddress("localhost", 9050);
                        SocketAddress saddr = new InetSocketAddress(MSConfig.proxyUrl, MSConfig.proxyPort);
                        Proxy proxy = new Proxy(Proxy.Type.SOCKS, saddr);

                        conn = (HttpURLConnection) new URL(addr).openConnection(proxy);
                        // dafür gibts den:
                        // Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0
                        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0");
                        //conn.setRequestProperty("User-Agent", MSearchConfig.getUserAgent_dynamic());
                        conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
                        if (timeout > 0) {
                            conn.setReadTimeout(timeout);
                            conn.setConnectTimeout(timeout);
                        }
                        encoding = conn.getContentEncoding();
                        mvIn = new MVInputStream(conn);
                        incSeitenZaehler(LISTE_SEITEN_PROXY, sender, 1, ladeArt);
                    }
                }
            }
            if (mvIn == null) {
                return seite;
            }
            if (mvIn.getInputStream() == null) {
                return seite;
            }
            if (encoding == null) {
                ladeArt = LADE_ART_NIX;
                in = mvIn;
            } else if (encoding.equalsIgnoreCase("gzip")) {
                ladeArt = LADE_ART_GZIP;
                in = new GZIPInputStream(mvIn);
            } else if (encoding.equalsIgnoreCase("deflate")) {
                ladeArt = LADE_ART_DEFLATE;
                in = new InflaterInputStream(mvIn, new Inflater(true));
            } else {
                ladeArt = LADE_ART_NIX;
                in = mvIn;
            }
            inReader = new InputStreamReader(in, kodierung);
//            char[] zeichen = new char[1];
//            while (!MSearchConfig.getStop() && inReader.read(zeichen) != -1) {
//                // hier wird andlich geladen
//                seite.append(zeichen);
//            }
            char[] buffer = new char[1024];
            int n;
            while (!MSConfig.getStop() && (n = inReader.read(buffer)) != -1) {
                // hier wird andlich geladen
                seite.append(buffer, 0, n);
            }
            incSeitenZaehler(LISTE_SUMME_KBYTE, sender, mvIn.summe, ladeArt);
        } catch (IOException ex) {
            if (conn != null) {
                try {
                    InputStream i = conn.getErrorStream();
                    if (i != null) {
                        i.close();
                    }
                    if (inReader != null) {
                        inReader.close();
                    }
                } catch (Exception e) {
                    MSLog.fehlerMeldung(645105987, MSLog.FEHLER_ART_GETURL, MSGetUrl.class.getName() + ".getUri", e, "");
                }
            }
            if (lVersuch) {
                String[] text;
                if (meldung.equals("")) {
                    text = new String[]{"Sender: " + sender + " - timout: " + timeout + " Versuche: " + versuch,
                        "URL: " + addr};
                } else {
                    text = new String[]{"Sender: " + sender + " - timout: " + timeout + " Versuche: " + versuch,
                        "URL: " + addr,
                        meldung};
                }
                switch (ex.getMessage()) {
                    case "Read timed out":
                        MSLog.fehlerMeldung(502739817, MSLog.FEHLER_ART_GETURL, MSGetUrl.class.getName() + ".getUri: TimeOut", text);
                        break;
                    case "No buffer space available":
                        MSLog.fehlerMeldung(915263697, MSLog.FEHLER_ART_GETURL, MSGetUrl.class.getName() + ".getUri: No buffer space available", text);
                        try {
                            // Pause zum Abbauen von Verbindungen
                            final int WARTEN = 2;
                            this.wait(WARTEN * 1000);
                            incSeitenZaehler(LISTE_SEITEN_NO_BUFFER, sender, WARTEN, LADE_ART_UNBEKANNT);
                        } catch (Exception ignored) {
                        }
                        break;
                    default:
                        MSLog.fehlerMeldung(379861049, MSLog.FEHLER_ART_GETURL, MSGetUrl.class.getName() + ".getUri", ex, text);
                        break;
                }
            }
        } catch (Exception ex) {
            MSLog.fehlerMeldung(973969801, MSLog.FEHLER_ART_GETURL, MSGetUrl.class.getName() + ".getUri", ex, "");
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception ex) {
                MSLog.fehlerMeldung(696321478, MSLog.FEHLER_ART_GETURL, MSGetUrl.class.getName() + ".getUri", ex, "");
            }
        }
        return seite;
    }

    private class MVInputStream extends InputStream {

        InputStream in = null;
        long summe = 0;
        int nr = 0;

        public MVInputStream(HttpURLConnection con) {
            try {
                if (con != null) {
                    in = con.getInputStream();
                }
            } catch (Exception ignored) {
            }
        }

        public InputStream getInputStream() {
            return in;
        }

        /*public long getSumme() {
         return summe;
         }*/
        @Override
        public int read() throws IOException {
            nr = in.read();
            if (nr != -  1) {
                ++summe;
                ++summeByte;
            }
            return nr;
        }

        @Override
        public int read(byte[] b) throws IOException {
            nr = in.read(b);
            if (nr != -  1) {
                summe += nr;
                summeByte += nr;
            }
            return nr;
        }

        @Override
        public int read(byte b[], int off, int len) throws IOException {
            nr = in.read(b, off, len);
            if (nr != -1) {
                summe += nr;
                summeByte += nr;
            }
            return nr;
        }
    }
}
