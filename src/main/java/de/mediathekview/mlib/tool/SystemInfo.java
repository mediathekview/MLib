package de.mediathekview.mlib.tool;

import java.io.File;
import java.security.CodeSource;

import de.mediathekview.mlib.Const;

public class SystemInfo {
	
	
	
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
    public static OperatingSystemType getOs() {
        OperatingSystemType os = OperatingSystemType.UNKNOWN;

        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            if (System.getenv("ProgramFiles(x86)") != null) {	// win 64Bit
                os = OperatingSystemType.WIN64;
            } else if (System.getenv("ProgramFiles") != null) {	// win 32Bit
                os = OperatingSystemType.WIN32;
            }
        } else if (com.jidesoft.utils.SystemInfo.isLinux() || System.getProperty("os.name").toLowerCase().contains("freebsd")) {
            os = OperatingSystemType.LINUX;
        } else if (com.jidesoft.utils.SystemInfo.isMacOSX()) {
            os = OperatingSystemType.MAC;
        }
        return os;
    }

    public static String getOsString() {
        return getOs().toString();
    }

    public static String getPathJar()
    {
        // liefert den Pfad der Programmdatei mit File.separator am Schluss
        String pFilePath = "version.properties";
        File propFile = new File(pFilePath);
        if (!propFile.exists())
        {
            try
            {
                CodeSource cS = Const.class.getProtectionDomain().getCodeSource();
                File jarFile = new File(cS.getLocation().toURI().getPath());
                String jarDir = jarFile.getParentFile().getPath();
                propFile = new File(jarDir + File.separator + pFilePath);
            } catch (Exception ignored)
            {
            }
        } else
        {
            DbgMsg.print("getPath");
        }
        String s = propFile.getAbsolutePath().replace(pFilePath, "");
        if (!s.endsWith(File.separator))
        {
            s = s + File.separator;
        }
        if (s.endsWith("/lib/"))
        {
            // dann sind wir in der msearch-lib
            s = s.replace("/lib/", "");
        }
        return s;
    }


    public static String[] getJavaVersion() {
        String[] ret = new String[4];
        int i = 0;
        ret[i++] = "Vendor: " + System.getProperty("java.vendor");
        ret[i++] = "VMname: " + System.getProperty("java.vm.name");
        ret[i++] = "Version: " + System.getProperty("java.version");
        ret[i++] = "Runtimeversion: " + System.getProperty("java.runtime.version");
        return ret;
    }

}
