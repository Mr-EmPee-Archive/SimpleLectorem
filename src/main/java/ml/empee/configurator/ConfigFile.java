package ml.empee.configurator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;
import ml.empee.configurator.exceptions.MalformedConfigurationException;

public class ConfigFile extends Config {

  private final JavaPlugin plugin;
  @Getter
  private final List<ConfigurationUpdate> updates = new ArrayList<>();

  private final String path;
  private final File configurationFile;
  @Getter
  private int version;

  public ConfigFile(JavaPlugin plugin, String path) {
    super(null);

    this.plugin = plugin;
    this.path = path;

    configurationFile = new File(plugin.getDataFolder(), path);
    if (configurationFile.exists()) {
      refresh();
    }
  }

  public final void refresh() {
    if(!configurationFile.exists()) {
      plugin.saveResource(path, false);
    }

    YamlConfiguration config = new YamlConfiguration();
    try {
      config.load(configurationFile);
    } catch (IOException | InvalidConfigurationException e) {
      throw MalformedConfigurationException.builder()
          .message("I/O error occurred while parsing the config file")
          .path(getAbsolutePath()).cause(e).build();
    }

    this.version = config.getInt("config-version", 0);
    this.configurationSection = config;
  }

  protected final void registerUpdate(ConfigurationUpdate update) {
    updates.add(ConfigurationUpdate.create(plugin, update));
  }
  @Override
  public final String getPath() {
    return configurationFile.getName();
  }

}
