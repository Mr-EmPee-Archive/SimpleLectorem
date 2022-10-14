package ml.empee.configurator;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;
import ml.empee.configurator.exceptions.ConfigurationProcessorException;
import ml.empee.configurator.helper.YamlParser;

public abstract class ConfigurationUpdate {

  @Getter
  private final int version;
  private final String path;

  private File file;
  private final YamlParser yamlParser;

  public static ConfigurationUpdate create(JavaPlugin plugin, ConfigurationUpdate update) {
    update.file = new File(plugin.getDataFolder(), update.path);
    return update;
  }

  protected ConfigurationUpdate(int version, String path) {
    this.version = version;
    this.path = path;
    this.yamlParser = new YamlParser();
  }

  public final void update() {
    if(!file.exists()) {
      return;
    }

    try {
      YamlConfiguration config = new YamlConfiguration();
      if(config.getInt("config-version", 0) != version) {
        return;
      }

      config.load(file);
      yamlParser.loadComments(file);
      runUpdate(config);
      config.set("config-version", version + 1);

      yamlParser.write(file, config);
    } catch (IOException | InvalidConfigurationException e) {
      throw new ConfigurationProcessorException("Error while updating the config file " + file.getPath(), e);
    }

  }

  protected final void setComments(String path, List<String> comments) {
    yamlParser.setComments(path, comments);
  }

  protected final void setInlineComments(String path, List<String> comments) {
    yamlParser.setInlineComments(path, comments);
  }

  protected abstract void runUpdate(YamlConfiguration config) throws IOException;

  protected final void moveFile(YamlConfiguration config, String relativePath) throws IOException {
    config.save(file);

    File movedFile = new File(file.getParentFile(), relativePath);
    if(!file.renameTo(movedFile)) {
      throw new ConfigurationProcessorException(
          "Unable to complete the update process for the config file" + file.getName() + ". \n" +
          "Cause: Unable to move the file from " + file.getPath() + " to " + movedFile.getPath()
      );
    }
    file = movedFile;

  }

}
