package ml.empee.configurator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;
import ml.empee.configurator.exceptions.ConfigurationProcessorException;

public abstract class ConfigurationUpdate {

  @Getter
  private final int version;
  private final String path;
  private File configFile;

  public static ConfigurationUpdate create(JavaPlugin plugin, ConfigurationUpdate update) {
    update.configFile = new File(plugin.getDataFolder(), update.path);
    return update;
  }

  protected ConfigurationUpdate(int version, String path) {
    this.version = version;
    this.path = path;
  }

  public final void update() {
    if(!configFile.exists()) {
      return;
    }

    try {
      YamlConfiguration config = new YamlConfiguration();
      if(config.getInt("config-version", 0) != version) {
        return;
      }

      config.load(configFile);
      runUpdate(config);
      config.set("config-version", version + 1);
      config.save(configFile);
    } catch (IOException | InvalidConfigurationException e) {
      throw new ConfigurationProcessorException("Error while updating the config file " + configFile.getPath(), e);
    }

  }

  protected abstract void runUpdate(YamlConfiguration config) throws IOException;

  protected final void moveFile(YamlConfiguration config, String relativePath) throws IOException {
    config.save(configFile);

    File file = new File(configFile.getParentFile(), relativePath);
    if(!configFile.renameTo(file)) {
      throw new ConfigurationProcessorException(
          "Unable to complete the update process for the config file" + configFile.getName() + ". \n" +
          "Cause: Unable to move the file from " + configFile.getPath() + " to " + file.getPath()
      );
    }
    configFile = file;

  }

}
