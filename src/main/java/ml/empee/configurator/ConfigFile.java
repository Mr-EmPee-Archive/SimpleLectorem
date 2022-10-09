package ml.empee.configurator;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;
import ml.empee.configurator.exceptions.MalformedConfigurationException;

public class ConfigFile extends Config {

  @Getter
  private final File configurationFile;

  public ConfigFile(JavaPlugin plugin, String path, boolean replace) {
    super(null);

    configurationFile = new File(plugin.getDataFolder(), path);
    if (!configurationFile.exists() || replace) {
      plugin.saveResource(path, true);
    }

    YamlConfiguration config = new YamlConfiguration();
    try {
      config.load(configurationFile);
    } catch (IOException | InvalidConfigurationException e) {
      throw MalformedConfigurationException.builder()
          .message("I/O error occurred while parsing the config file")
          .path(getAbsolutePath()).cause(e).build();
    }

    this.configurationSection = config;
  }

  @Override
  public String getPath() {
    return configurationFile.getName();
  }

}
