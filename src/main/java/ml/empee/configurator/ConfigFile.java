package ml.empee.configurator;

import ml.empee.configurator.annotations.AnnotationProcessor;
import ml.empee.configurator.exceptions.MalformedConfigurationException;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;

import java.io.File;
import java.io.IOException;

public class ConfigFile extends Config {

    @Getter
    private final File configurationFile;

    public ConfigFile(JavaPlugin plugin, String path, boolean replace) {
        super(null);

        configurationFile = new File(plugin.getDataFolder(), path);
        if(!configurationFile.exists() || replace) {
            plugin.saveResource(path, true);
        }

        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(configurationFile);
        } catch (IOException | InvalidConfigurationException e) {
            throw MalformedConfigurationException.builder()
                    .message("I/O error occurred while parsing the config file")
                    .config(this).cause(e).build();
        }

        this.configurationSection = config;
        AnnotationProcessor.injectConfigValues(this);
    }

    @Override
    public String getPath() {
        return configurationFile.getName();
    }

}
