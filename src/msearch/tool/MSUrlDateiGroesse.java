package msearch.tool;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import msearch.daten.MSConfig;

public class MSUrlDateiGroesse {

    final static int TIMEOUT = 3000; // ms //ToDo evtl. wieder kürzen!!
    //private static int anz = 0;
    private static String[] sender;
    private static int[] anz;
    private static int[] anz403;
    private static int[] anzProxy;

    public static void resetZaehler(String[] ssender) {
        sender = ssender;
        anz = new int[sender.length];
        anz403 = new int[sender.length];
        anzProxy = new int[sender.length];
        for (int i = 0; i < ssender.length; ++i) {
            anz[i] = 0;
            anz403[i] = 0;
            anzProxy[i] = 0;
        }
    }

    public static int getZaehler(String ssender) {
        for (int i = 0; i < sender.length; ++i) {
            if (sender[i].equalsIgnoreCase(ssender)) {
                return anz[i];
            }
        }
        return 0;
    }

    public static int getZaehler403(String ssender) {
        for (int i = 0; i < sender.length; ++i) {
            if (sender[i].equalsIgnoreCase(ssender)) {
                return anz403[i];
            }
        }
        return 0;
    }

    public static int getZaehlerProxy(String ssender) {
        for (int i = 0; i < sender.length; ++i) {
            if (sender[i].equalsIgnoreCase(ssender)) {
                return anzProxy[i];
            }
        }
        return 0;
    }

    public static boolean urlExists(String url) {
        // liefert liefert true, wenn es die URL gibt
        int retCode;
        if (!url.toLowerCase().startsWith("http")) {
            return false;
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestProperty("User-Agent", MSConfig.getUserAgent());
            conn.setReadTimeout(2 * TIMEOUT);
            conn.setConnectTimeout(2 * TIMEOUT);
            if ((retCode = conn.getResponseCode()) < 400) {
                return true;
            } else if (retCode == 403) {
                // aber sie gibt es :)
                return true;
            }
            conn.disconnect();
        } catch (Exception ignored) {
        }
        return false;
    }

    public static String laengeString(String url) {
        // liefert die Dateigröße einer URL in MB!!
        // Anzeige der Größe in MB und deshalb: Faktor 1000
        return laengeString(url, "");
    }

    public static String laengeString(String url, String ssender) {
        // liefert die Dateigröße einer URL in MB!!
        // Anzeige der Größe in MB und deshalb: Faktor 1000
        String groesseStr = "";
        long l = laenge(url, ssender);
        if (l > 1000 * 1000) {
            // größer als 1MB sonst kann ich mirs sparen
            groesseStr = String.valueOf(l / (1000 * 1000));
        } else if (l > 0) {
            groesseStr = "1";
        }
        return groesseStr;
    }

    private static long laenge(String url, String ssender) {
        // liefert die Dateigröße einer URL in BYTE!
        // oder -1
        long ret = -1;
        int retCode;
        if (!url.toLowerCase().startsWith("http")) {
            return ret;
        }
        countArray(anz, ssender);
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestProperty("User-Agent", MSConfig.getUserAgent());
            conn.setReadTimeout(TIMEOUT);
            conn.setConnectTimeout(TIMEOUT);
            retCode = conn.getResponseCode();
            if (retCode < 400) {
                ret = conn.getContentLengthLong(); //gibts erst seit jdk 7
            }
            conn.disconnect();

            // dann über eine Proxy
            if (retCode == 403) {
                countArray(anz403, ssender);
                if (!MSConfig.proxyUrl.isEmpty() && MSConfig.proxyPort > 0) {
                    // nur dann verwenden, wenn ein Proxy angegeben
                    try {
                        SocketAddress saddr = new InetSocketAddress(MSConfig.proxyUrl, MSConfig.proxyPort);
                        Proxy proxy = new Proxy(Proxy.Type.SOCKS, saddr);
                        conn = (HttpURLConnection) new URL(url).openConnection(proxy);
                        conn.setRequestProperty("User-Agent", MSConfig.getUserAgent());
                        conn.setReadTimeout(TIMEOUT);
                        conn.setConnectTimeout(TIMEOUT);
                        ret = conn.getContentLengthLong(); //gibts erst seit jdk 7
                        conn.disconnect();
                        if (ret > 0) {
                            countArray(anzProxy, ssender);
                        }
                    } catch (Exception ex) {
                        ret = -1;
                        MSLog.fehlerMeldung(963215478, MSLog.FEHLER_ART_PROG, "MVUrlDateiGroesse.laenge", ex);
                    }
                }
            }
        } catch (Exception ex) {
            ret = -1;
            if (ex.getMessage().equals("Read timed out")) {
                MSLog.fehlerMeldung(825141452, MSLog.FEHLER_ART_PROG, "StarterClass.StartenDownload.laenge", "Read timed out: " + ssender);
            } else {
                MSLog.fehlerMeldung(643298301, MSLog.FEHLER_ART_PROG, "StarterClass.StartenDownload.laenge", ex);
            }
        }
        if (ret < 300 * 1024) {
            // alles unter 300k sind Playlisten, ...
            // dann wars nix
            ret = -1;
        }
        return ret;
    }

    private static void countArray(int[] arr, String ssender) {
        if (!ssender.isEmpty()) {
            for (int i = 0; i < sender.length; ++i) {
                if (sender[i].equalsIgnoreCase(ssender)) {
                    ++arr[i];
                    break;
                }
            }
        }
    }
}
