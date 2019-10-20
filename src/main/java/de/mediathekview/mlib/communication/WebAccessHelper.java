/*
 * WebAccessHelper.java
 *
 * Projekt    : MLib
 * erstellt am: 04.10.2017
 * Autor      : Sascha
 *
 */
package de.mediathekview.mlib.communication;

import de.mediathekview.mlib.datasources.WebTargetDS;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.net.URL;

public class WebAccessHelper {

  private WebAccessHelper() {}

  public static String getJsonResultFromPostAccess(
      final URL serverUrl, final String request, final long timeoutInSeconds) {
    if (null == serverUrl) {
      throw new IllegalArgumentException("Es wurde keine gültige ServerURL angegeben");
    }

    final WebTarget target = WebTargetDS.getInstance(serverUrl.toString(), timeoutInSeconds);

    return target
        .request(MediaType.APPLICATION_JSON_TYPE)
        .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE), String.class);
  }
}
