package de.mediathekview.mlib.tool;

import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.*;

public class MVHttpClient {
    private final static MVHttpClient ourInstance = new MVHttpClient();
    private final OkHttpClient httpClient;
    private final OkHttpClient copyClient;
    private final OkHttpClient sslUnsafeClient;

    private MVHttpClient() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(100, 1, TimeUnit.SECONDS))
                .build();
        httpClient.dispatcher().setMaxRequests(100);

        copyClient = httpClient.newBuilder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(2, TimeUnit.SECONDS).build();
                
        sslUnsafeClient = getUnsafeOkHttpClient();
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
    public OkHttpClient getSSLUnsafeClient() {
        return sslUnsafeClient;
    }
    
    /*
     * Thanks to: https://gist.github.com/mefarazath/c9b588044d6bffd26aac3c520660bf40
     */
    private OkHttpClient getUnsafeOkHttpClient() {
    try {
        // Create a trust manager that does not validate certificate chains
        final TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                                                   String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                                                   String authType) throws CertificateException {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };

        // Install the all-trusting trust manager
        final SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        // Create an ssl socket factory with our all-trusting manager
        final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        return new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(2, TimeUnit.SECONDS)
                .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                }).build();

    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}
}
