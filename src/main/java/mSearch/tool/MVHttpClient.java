package mSearch.tool;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import org.apache.commons.configuration2.Configuration;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

public class MVHttpClient {
    private final static MVHttpClient ourInstance = new MVHttpClient();
    private OkHttpClient httpClient;
    private OkHttpClient copyClient;
    private final Configuration config = ApplicationConfiguration.getConfiguration();

    private MVHttpClient() {
        String proxyHost = System.getProperty("http.proxyHost");
        String proxyPort = System.getProperty("http.proxyPort");

        try {
            if (proxyHost != null && proxyPort != null && !proxyHost.isEmpty() && !proxyPort.isEmpty()) {
                //we are configuring the proxy from environment variables...
                final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort)));
                setupProxyClients(proxy);
                SysMsg.sysMsg(String.format("MVHttpClient: Proxy configured from environment variables: (%s)", proxyHost));
            } else {
                //environment variables were not set, use application settings...
                try {
                    proxyHost = config.getString(ApplicationConfiguration.HTTP_PROXY_HOSTNAME);
                    proxyPort = config.getString(ApplicationConfiguration.HTTP_PROXY_PORT);
                    if (!proxyHost.isEmpty() && !proxyPort.isEmpty()) {
                        final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort)));
                        setupProxyClients(proxy);
                        SysMsg.sysMsg(String.format("MVHttpClient: Proxy configured from application config: (%s)", proxyHost));
                    } else {
                        //no proxy setup specified...
                        setupNonProxyClients();
                    }
                } catch (NoSuchElementException e) {
                    setupNonProxyClients();
                }
            }
        } catch (NumberFormatException ex) {
            setupNonProxyClients();
            Log.errorLog(123456789, ex, "PROXY config failed. Creating non proxy config");
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

    private Authenticator createAuthenticator(String prxUser, String prxPassword) {
        return (route, response) -> {
            if (response.request().header(HTTP_PROXY_AUTHORIZATION) != null) {
                return null; // Give up, we've already attempted to authenticate.
            }
            final String credential = Credentials.basic(prxUser, prxPassword);
            return response.request().newBuilder()
                    .header(HTTP_PROXY_AUTHORIZATION, credential)
                    .build();
        };
    }

    private Authenticator setupProxyAuthenticator() {
        String prxUser = System.getProperty("http.proxyUser");
        String prxPassword = System.getProperty("http.proxyPassword");
        Authenticator proxyAuthenticator = null;

        if (prxUser != null && prxPassword != null && !prxUser.isEmpty() && !prxPassword.isEmpty()) {
            //create proxy auth from environment vars
            proxyAuthenticator = createAuthenticator(prxUser, prxPassword);
            SysMsg.sysMsg(String.format("Proxy Authentication from environment vars: (%s)", prxUser));
        } else {
            //try to create proxy auth from settings
            try {
                prxUser = config.getString(ApplicationConfiguration.HTTP_PROXY_USERNAME);
                prxPassword = config.getString(ApplicationConfiguration.HTTP_PROXY_PASSWORD);
                if (!prxUser.isEmpty() && !prxPassword.isEmpty()) {
                    proxyAuthenticator = createAuthenticator(prxUser, prxPassword);
                    SysMsg.sysMsg(String.format("Proxy Authentication from application settings: (%s)", prxUser));
                }
            } catch (NoSuchElementException ignored) {
            }
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
