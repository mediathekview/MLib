/*
 * WebTargetDS.java
 *
 * Projekt    : MServer
 * erstellt am: 05.10.2017
 * Autor      : Sascha
 *
 * (c) 2017 by Sascha Wiegandt
 */
package de.mediathekview.mlib.datasources;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class WebTargetDS {

  private static final Map<String, WebTarget> connectionPool = new ConcurrentHashMap<>();

  private WebTargetDS() {}

  public static WebTarget getInstance(final String url, final long timeoutInSeconds) {
    if (connectionPool.containsKey(url)) {
      return connectionPool.get(url);
    } else {
      final Client client =
          ClientBuilder.newBuilder().readTimeout(timeoutInSeconds, TimeUnit.SECONDS).build();
      connectionPool.put(url, client.target(url));

      return connectionPool.get(url);
    }
  }
}
