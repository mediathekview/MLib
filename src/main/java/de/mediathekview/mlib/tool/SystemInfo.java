package de.mediathekview.mlib.tool;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SystemInfo {
	
	private static SystemInfo instanz = null;
	
	private static final Logger LOG = LogManager.getLogger(Version.class);
	
	private OperatingSystemType os;
	private JavaVersion javaversion;
	private Path jarpath;
	
	private SystemInfo() {
		os = readOs();
		javaversion = readJavaVersion();
		jarpath = readJarPath();
	}
	
	public static SystemInfo getInstance() {
		if (instanz == null) instanz = new SystemInfo();
		return instanz;
	}
	
	
	public enum OperatingSystemType {

        UNKNOWN(""), WIN32("Windows"), WIN64("Windows"), LINUX("Linux"), MAC("Mac");
        private final String name;

        OperatingSystemType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Detect and return the currently used operating system.
     *
     * @return The enum for supported Operating Systems.
     */
    private OperatingSystemType readOs() {
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            if (System.getenv("ProgramFiles(x86)") != null) {	// win 64Bit
                return OperatingSystemType.WIN64;
            }
            if (System.getenv("ProgramFiles") != null) {	    // win 32Bit
                return OperatingSystemType.WIN32;
            }
        }
        if (com.jidesoft.utils.SystemInfo.isLinux() || System.getProperty("os.name").toLowerCase().contains("freebsd")) {
            return OperatingSystemType.LINUX;
        }
        if (com.jidesoft.utils.SystemInfo.isMacOSX()) {
            return OperatingSystemType.MAC;
        }
        return OperatingSystemType.UNKNOWN;
    }
    
    /**
     * Ermittelt den Jar Pfad
     * @return Path Den Jar Pfad als Path
     */
    private Path readJarPath() {
    	try {
    		String ermittelterPfad = SystemInfo.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    		if(ermittelterPfad.contains("/lib")) { //Mlib liegt im lib Ordner
    			return Paths.get(ermittelterPfad).getParent().getParent();
    		}
			return Paths.get(ermittelterPfad).getParent();
		} catch (Exception e) {
			LOG.warn("Konnte Jar Pfad nicht ermitteln.", e);
		}
    	return null;
    }

    private JavaVersion readJavaVersion() {
    	return new JavaVersion(
    			System.getProperty("java.vendor"),
    			System.getProperty("java.vm.name"),
    			System.getProperty("java.version"),
    			System.getProperty("java.runtime.version"));
    }
    
    public OperatingSystemType getOs() {
		return os;
	}
    
    public JavaVersion getJavaVersion() {
		return javaversion;
	}

	public Path getJarpath() {
		return jarpath;
	}

}
