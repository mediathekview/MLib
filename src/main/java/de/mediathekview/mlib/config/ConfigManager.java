package de.mediathekview.mlib.config;

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
            .fromFileOnClasspath(getConfigFileName())
            .optionalSource()
            .fromFileOnPath("./" + getConfigFileName())
            .build(getConfigClass());
  }

  public T getConfig() {
    if (config == null) {
      readConfig();
    }
    return config;
  }
}
