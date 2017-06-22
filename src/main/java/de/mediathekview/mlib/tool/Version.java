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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Version {

    private int major;
    private int minor;
    private int patch;
    private boolean istSnapshot;
    
    private static final String STR_SNAPSHOT = "-SNAPSHOT";
    private static final Logger LOG = LogManager.getLogger(Version.class);

	public Version(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.istSnapshot = false;
    }
    
    public Version(int major, int minor, int patch, boolean istSnapshot) {
        this(major, minor, patch);
        this.istSnapshot = istSnapshot;
    }

    public Version(String versionsstring) {
    	if (versionsstring == null) {
    		major = 0;
    		minor = 0;
    		patch = 0;
    		istSnapshot = false;
    		return;
    	}
        String[] versions = versionsstring.replaceAll(STR_SNAPSHOT, "").split("\\.");
        if (versions.length == 3) {
            try {
                major = Integer.parseInt(versions[0]);
                minor = Integer.parseInt(versions[1]);
                patch = Integer.parseInt(versions[2]);
                istSnapshot = versionsstring.indexOf(STR_SNAPSHOT) > -1 ? true : false;
            } catch (NumberFormatException ex) {
                LOG.warn("Fehler beim Parsen der Version '" + versionsstring + "'.", ex);
                major = 0;
                minor = 0;
                patch = 0;
                istSnapshot = false;
            }
        }
    }

    public Version() {
        this(0,0,0, false);
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public int getPatch() {
        return patch;
    }

    public void setPatch(int patch) {
        this.patch = patch;
    }
    
    public boolean isIstSnapshot() {
		return istSnapshot;
	}

	public void setIstSnapshot(boolean istSnapshot) {
		this.istSnapshot = istSnapshot;
	}

    /**
     * Gibt die Version als gewichtete Zahl zurück.
     *
     * @return gewichtete Zahl als Integer
     */
    public int toNumber() {
    	if(istSnapshot) {
    		return major * 1000 + minor * 100 + patch * 10 + 1;
    	} else {
    		return major * 1000 + minor * 100 + patch * 10;
    	}
    }

    /**
     * Gibt die Version als String zurück
     *
     * @return String mit der Version
     */
    @Override
    public String toString() {
    	if(istSnapshot) {
    		return String.format("%d.%d.%d", major, minor, patch)+STR_SNAPSHOT;
    	} else {
    		return String.format("%d.%d.%d", major, minor, patch);
    	}
    }

    /**
     * Nimmt ein Objekt vom Typ Version an und vergleicht ihn mit sich selbst
     *
     * @param a Versionsobjekt welches zum vergleich rangezogen werden soll
     * @return 1 Version a ist größer, 0 Versionen sind gleich oder -1 Version a ist kleiner
     */
    public int compare(Version versionzwei) {
        if (this.toNumber() < versionzwei.toNumber()) {
            return 1;
        } else if (this.toNumber() == versionzwei.toNumber()) {
            return 0;
        } else {
            return -1;
        }
    }

}
