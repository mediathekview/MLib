package de.mediathekview.mlib.tool;

import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

public class OkHttpClientBuilder {
  private final OkHttpClient.Builder httpClientBuilder;

  OkHttpClientBuilder() {
    httpClientBuilder = new OkHttpClient.Builder();
  }

  OkHttpClientBuilder withConnectTimeout(final long timeoutInSeconds) {
    httpClientBuilder.connectTimeout(timeoutInSeconds, TimeUnit.SECONDS);
    return this;
  }

  OkHttpClientBuilder withReadTimeout(final long timeoutInSeconds) {
    httpClientBuilder.readTimeout(timeoutInSeconds, TimeUnit.SECONDS);
    return this;
  }

  public OkHttpClient build() {
    final OkHttpClient client = httpClientBuilder.build();
    client.dispatcher().setMaxRequests(100);
    return client;
  }
}
