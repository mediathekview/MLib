package mSearch.tool;

import okhttp3.Authenticator;
import okhttp3.Credentials;
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
                SysMsg.sysMsg(String.format("MVHttpClient: Proxy configured: (%s)", proxyHost));

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

    private static final String HTTP_PROXY_AUTHORIZATION = "Proxy-Authorization";

    private Authenticator setupProxyAuthenticator() {
        final String prxUser = System.getProperty("http.proxyUser");
        final String prxPassword = System.getProperty("http.proxyPassword");
        Authenticator proxyAuthenticator = null;

        if (prxUser != null && prxPassword != null) {
            proxyAuthenticator = (route, response) -> {
                if (response.request().header(HTTP_PROXY_AUTHORIZATION) != null) {
                    return null; // Give up, we've already attempted to authenticate.
                }
                final String credential = Credentials.basic(prxUser, prxPassword);
                return response.request().newBuilder()
                        .header(HTTP_PROXY_AUTHORIZATION, credential)
                        .build();
            };
            SysMsg.sysMsg(String.format("Proxy Authentication: (%s)", prxUser));
        }

        return proxyAuthenticator;
    }

    /**
     * Set the proxy parameters on the shared HTTP clients.
     *
     * @param proxy The proxy settings to be used.
     */
    private void setupProxyClients(Proxy proxy) {
        final Authenticator proxyAuthenticator = setupProxyAuthenticator();

        OkHttpClient.Builder tmpBuilder;
        tmpBuilder = getDefaultClientBuilder()
                .proxy(proxy);

        if (proxyAuthenticator != null)
            tmpBuilder.proxyAuthenticator(proxyAuthenticator);
        httpClient = tmpBuilder.build();

        tmpBuilder = getDefaultClientBuilder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(2, TimeUnit.SECONDS)
                .proxy(proxy);

        if (proxyAuthenticator != null)
            tmpBuilder.proxyAuthenticator(proxyAuthenticator);
        copyClient = tmpBuilder.build();
    }

    /**
     * Setup HTTP client without proxy settings
     */
    private void setupNonProxyClients() {
        httpClient = getDefaultClientBuilder()
                .build();

        copyClient = getDefaultClientBuilder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(2, TimeUnit.SECONDS)
                .build();

        SysMsg.sysMsg("MVHttpClient: Proxy not configured");
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
