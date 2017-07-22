package de.mediathekview.mlib.config;

import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.provider.ConfigurationProviderBuilder;
import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.context.filesprovider.ConfigFilesProvider;
import org.cfg4j.source.files.FilesConfigurationSource;

import java.nio.file.Path;

/**
 * A manager to load configurations.
 */
public abstract class ConfigManager<T extends ConfigDTO>
{
    private T config;

    protected abstract Path getConfigFilePath();
    protected abstract String getConfigName();
    protected abstract Class<T> getConfigClass();

    ConfigManager()
    {
        ConfigurationSource configurationSource = new FilesConfigurationSource(() -> getConfigFilePath());
        final ConfigurationProvider configurationProvider = new ConfigurationProviderBuilder()
                .withConfigurationSource(configurationSource)
                .build();
        config = configurationProvider.bind(getConfigName(),getConfigClass());
    }

    public T getConfig()
    {
        return config;
    }
}
