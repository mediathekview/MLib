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

public class MLibVersion {
	
	private static MLibVersion instanz;
	
	private MLibVersion() {
		// Singleton
	}
	
	public static MLibVersion getInstance() {
		if(instanz == null) {
			instanz = new MLibVersion();
		}
		return instanz;
	}
	
	public synchronized Version getVersion() {
	    Version version = null;

	    // try to load from maven properties first
	    try {
	        Properties p = new Properties();
	        InputStream is = getClass().getResourceAsStream("/META-INF/maven/de.mediathekviewer/MLib/pom.properties");
	        if (is != null) {
	            p.load(is);
	            version = new Version(p.getProperty("version", ""));
	        }
	    } catch (Exception e) {
	        // ignore
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

	    if (version == null) {
	        // we could not compute the version so use a blank
	        version = new Version();
	    }

	    return version;
	} 

}
