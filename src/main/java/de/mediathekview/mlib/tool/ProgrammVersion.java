package de.mediathekview.mlib.tool;

import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.mediathekview.dto.Version;

public class ProgrammVersion {

  private static ProgrammVersion instanz = null;
  private static final Logger LOG = LogManager.getLogger(Version.class);

  private ProgrammVersion() {

  }

  public static ProgrammVersion getInstance() {
    if (instanz == null)
      instanz = new ProgrammVersion();
    return instanz;
  }

  /**
   * Ermittelt die Programm-Version anhand der von Maven erzeugten pom.properties in der jar oder
   * versucht es mit der Java API. Schlägt beides fehl fällt er zurück auf eine leere Version.
   * 
   * @return Version Die Programm-Version als Version Objekt.
   */
  public synchronized Version getVersion(Class<?> klasse, String progname) {
    Version version = null;
    // try to load from maven properties first
    try {
      Properties p = new Properties();
      InputStream is = getClass().getResourceAsStream(
          "/META-INF/maven/" + klasse.getPackage().getName() + "/" + progname + "/pom.properties");
      if (is != null) {
        p.load(is);
        version = new Version(p.getProperty("version", ""));
      } else {
        LOG.debug(
            "MLib-Version konnte nicht aus der pom.properties geladen werden. Fallback zur Java API.");
      }
    } catch (Exception e) {
      LOG.debug(
          "MLib-Version konnte nicht aus der pom.properties geladen werden. Fallback zur Java API.",
          e);
    }

    // fallback to using Java API
    if (version == null) {
      Package aPackage = klasse.getPackage();
      if (aPackage != null) {
        version = new Version(aPackage.getImplementationVersion());
        if (version.toNumber() == 0) {
          version = new Version(aPackage.getSpecificationVersion());
        }
      }
    }
    // we could not compute the version so use a blank
    if (version == null)
      version = new Version();
    return version;
  }

  /**
   * 
   * @param klasse Die aufrufende Klasse um das Package rauszufinden
   * @param progname Der Programmname wie er in maven steht
   * @param fallback Die Fallbackversion, welche verwendet wird, wenn die Version nicht ausgelesen
   *        werden konnte.
   * @return Version die Programmversion als Version Objekt
   */
  public synchronized Version getVersion(Class<?> klasse, String progname, Version fallback) {
    Version progversion = getVersion(klasse, progname);
    if (progversion.toNumber() == 0)
      return fallback;
    return progversion;
  }

  /**
   * Gibt die MLib-Version als formatierten String zurück. Format: [Vers.:
   * major.minor.patch-snapshot]
   * 
   * @return String Versionsstring
   */
  public String getVersionStringFormated(Class<?> klasse, String progname) {
    return " [Vers.: " + getVersion(klasse, progname) + ']';
  }

  /**
   * Gibt die MLib-Version als formatierten String zurück. Format: [Vers.:
   * major.minor.patch-snapshot]
   * 
   * @return String Versionsstring
   */
  public String getVersionStringFormated(Class<?> klasse, String progname, Version fallback) {
    Version progversion = getVersion(klasse, progname);
    if (progversion.toNumber() == 0)
      return " [Vers.: " + fallback + ']';
    return " [Vers.: " + progversion + ']';
  }

}
