package mSearch.tool;

import okhttp3.OkHttpClient;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

public class MVHttpClient {
    private final static MVHttpClient ourInstance = new MVHttpClient();
    private OkHttpClient httpClient;
    private OkHttpClient copyClient;

    private MVHttpClient() {
        final String proxyHost = System.getProperty("http.proxyHost");
        final String proxyPort = System.getProperty("http.proxyPort");

        if (proxyHost != null && proxyPort != null) {
            //setup for proxy
            try {
                final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort)));
                setupProxyClients(proxy);
            } catch (NumberFormatException ex) {
                Log.errorLog(123456789, ex, "PROXY config failed. Creating non proxy config");
                //in case of error with proxy configuration log error and create no proxy config
                setupNonProxyClients();
            }
        } else {
            //TODO setup proxy from settings file

            //no proxy setup
            setupNonProxyClients();
        }

    }

    /**
     * Build the client builder with default settings.
     *
     * @return A Builder with default settings.
     */
    private OkHttpClient.Builder getDefaultClientBuilder() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS);
    }

    /**
     * Set the proxy parameters on the shared HTTP clients.
     *
     * @param proxy The proxy settings to be used.
     */
    private void setupProxyClients(Proxy proxy) {
        httpClient = getDefaultClientBuilder()
                .proxy(proxy)
                .build();

        copyClient = httpClient.newBuilder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(2, TimeUnit.SECONDS)
                .proxy(proxy)
                .build();

    }

    /**
     * Setup HTTP client without proxy settings
     */
    private void setupNonProxyClients() {
        httpClient = getDefaultClientBuilder()
                .build();

        copyClient = httpClient.newBuilder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(2, TimeUnit.SECONDS)
                .build();
    }

    public static MVHttpClient getInstance() {
        return ourInstance;
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    public OkHttpClient getReducedTimeOutClient() {
        return copyClient;
    }
}
