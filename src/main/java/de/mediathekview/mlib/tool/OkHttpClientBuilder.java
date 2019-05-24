package de.mediathekview.mlib.tool;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

public class OkHttpClientBuilder {
  private static final Logger LOG = LogManager.getLogger(OkHttpClientBuilder.class);
  private final OkHttpClient.Builder httpClientBuilder;
  private int maxRequests;

  public OkHttpClientBuilder() {
    httpClientBuilder = new OkHttpClient.Builder();
  }

  public OkHttpClientBuilder withConnectTimeout(final long timeoutInSeconds) {
    httpClientBuilder.connectTimeout(timeoutInSeconds, TimeUnit.SECONDS);
    return this;
  }

  public OkHttpClientBuilder withWriteTimeout(final long timeoutInSeconds) {
    httpClientBuilder.writeTimeout(timeoutInSeconds, TimeUnit.SECONDS);
    return this;
  }

  public OkHttpClientBuilder withReadTimeout(final long timeoutInSeconds) {
    httpClientBuilder.readTimeout(timeoutInSeconds, TimeUnit.SECONDS);
    return this;
  }

  public OkHttpClientBuilder withConnectionPool(
      final int maxIdleConnections, final int keepAliveDurationInSeconds) {
    httpClientBuilder.connectionPool(
        new ConnectionPool(maxIdleConnections, keepAliveDurationInSeconds, TimeUnit.SECONDS));
    return this;
  }

  public OkHttpClientBuilder withMaxRequests(final int aMaxRequests) {
    maxRequests = aMaxRequests;
    return this;
  }

  public OkHttpClientBuilder withUnsafe() {
    // Create a trust manager that does not validate certificate chains
    final TrustManager[] trustAllCerts =
        new TrustManager[] {
          new X509TrustManager() {
            @Override
            public void checkClientTrusted(final X509Certificate[] chain, final String authType) {}

            @Override
            public void checkServerTrusted(final X509Certificate[] chain, final String authType) {}

            @Override
            public X509Certificate[] getAcceptedIssuers() {
              return new X509Certificate[0];
            }
          }
        };

    try {
      // Install the all-trusting trust manager
      final SSLContext sslContext = SSLContext.getInstance("SSL");
      sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
      // Create an ssl socket factory with our all-trusting manager
      final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

      httpClientBuilder
          .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
          .hostnameVerifier((hostname, session) -> true)
          .build();
    } catch (final NoSuchAlgorithmException noSuchAlgorithmException) {
      LOG.error("Can't get the SSL context for the unsafe http client.");
    } catch (final KeyManagementException keyManagementException) {
      LOG.error("Something went wrong on building the unsafe http client.");
    }
    return this;
  }

  public OkHttpClient build() {
    final OkHttpClient client = httpClientBuilder.build();
    client.dispatcher().setMaxRequests(100);
    return client;
  }
}
