package de.mediathekview.mlib.tool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class VersionReader {
  private static final Logger LOG = LogManager.getLogger(VersionReader.class);

  public Version readVersion() {
    final String versionToken = "VERSION";
    ResourceBundle.clearCache();
    try {
      final ResourceBundle resourceBundle = ResourceBundle.getBundle("version");
      return new Version(
          resourceBundle.containsKey(versionToken) ? resourceBundle.getString(versionToken) : "");
    } catch (final MissingResourceException missingResourceException) {
      LOG.error("Can't load the actual program version.");
      return new Version("");
    }
  }
}