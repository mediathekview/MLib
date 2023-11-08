package de.mediathekview.mlib.config;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.yacl4j.core.ConfigurationBuilder;

/** A manager to load configurations. */
public abstract class ConfigManager<T extends ConfigDTO> {
  private T config;

  protected abstract String getConfigFileName();

  protected abstract Class<T> getConfigClass();

  protected ConfigManager() {
    config = null;
  }

  public void readConfig() {
    config =
        ConfigurationBuilder.newBuilder()
            .source()
            .fromFileOnPath(getResourcePath(getConfigFileName()))
            .build(getConfigClass());
  }

  public T getConfig() {
    if (config == null) {
      readConfig();
      initializeConfigAfterRead(config);
    }
    return config;
  }

  protected void initializeConfigAfterRead(final T config) {
    // Do something after the configuration is read
  }
  
  /*
   * check if the given file exists in FS or
   * try to read a resource from filesystem
   */
  public String getResourcePath(String resourceName) {
    try {
      if (new java.io.File(resourceName).exists()) {
        return resourceName;
      } else {       
        ClassLoader classLoader = getClass().getClassLoader();
        URL resourceUrl = classLoader.getResource(resourceName);
        if (resourceUrl != null) {
          Path resourcePath = Paths.get(resourceUrl.toURI());
          return resourcePath.toString();
        }
      }
    } catch(Exception e) {}
    return null;
  }
}
