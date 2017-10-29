/*
 * WebAccessHelper.java
 * 
 * Projekt    : MLib
 * erstellt am: 04.10.2017
 * Autor      : Sascha
 * 
 */
package de.mediathekview.mlib.communication;

import java.net.URL;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import de.mediathekview.mlib.datasources.WebTargetDS;

public class WebAccessHelper {
    
    private WebAccessHelper() {
        
    }
    
    public static String getJsonResultFromPostAccess(URL serverUrl, String request) throws IllegalArgumentException {
        if(null == serverUrl)
            throw new IllegalArgumentException("Es wurde keine gültige ServerURL angegeben");
        
        WebTarget target = WebTargetDS.getInstance(serverUrl.toString());
        
        return target.request(MediaType.APPLICATION_JSON_TYPE)
                              .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE), String.class);

    }

}
