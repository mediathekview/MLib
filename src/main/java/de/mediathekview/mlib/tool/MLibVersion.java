/*
 * MediathekView
 * Copyright (C) 2017 A. Finkhäuser
 * alex@elaon.de
 * http://mediathekview.de
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.mediathekview.mlib.tool;


import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.mediathekview.dto.Version;

public class MLibVersion {
	
	private static MLibVersion instanz=null;
	private static final Logger LOG = LogManager.getLogger(Version.class);
	
	private static final Version MLIB_VERSION = new Version(3,0,2, true); // Fallbackwert (Major, Minor, Patch, Snapshot)
	
	private Version version;
	
	private MLibVersion() { // Singleton
		version = readVersion();
	}
	
	public static MLibVersion getInstance() {
		if(instanz == null) instanz = new MLibVersion();
		return instanz;
	}
	
	/**
	 * Ermittelt die MLib-Version anhand der von Maven erzeugten pom.properties in der jar oder versucht es mit der Java API. 
	 * Schlägt beides fehl fällt er zurück auf den festen Wert.
	 * @return Version Die MLib-Version als Version Objekt.
	 */
	private synchronized Version readVersion() {
	    Version version = null;
	    // try to load from maven properties first
	    try {
	        Properties p = new Properties();
	        InputStream is = getClass().getResourceAsStream("/META-INF/maven/de.mediathekview/MLib/pom.properties");
	        if (is != null) {
	            p.load(is);
	            version = new Version(p.getProperty("version", ""));
	        } else {
	        	LOG.debug("MLib-Version konnte nicht aus der pom.properties geladen werden. Fallback zur Java API.");
	        }
	    } catch (Exception e) {
	    	LOG.debug("MLib-Version konnte nicht aus der pom.properties geladen werden. Fallback zur Java API.", e);
	    }

	    // fallback to using Java API
	    if (version == null) {
	        Package aPackage = getClass().getPackage();
	        if (aPackage != null) {
	            version = new Version(aPackage.getImplementationVersion());
	            if (version.toNumber() == 0) {
	                version = new Version(aPackage.getSpecificationVersion());
	            }
	        }
	    }
	 // we could not compute the version so use a blank
	    if (version == null) version = MLIB_VERSION;
	    return version;
	}
	
	public Version getVersion() {
		return version;
	}
	
	/**
	 * Gibt die MLib-Version als formatierten String zurück.
	 * Format:  [Vers.: major.minor.patch-snapshot]
	 * @return String Versionsstring
	 */
	public String getVersionStringFormated() {
		return 	" [Vers.: " + version.toString() + ']';
	}

}
