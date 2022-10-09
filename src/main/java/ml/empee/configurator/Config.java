package ml.empee.configurator;

import org.bukkit.configuration.ConfigurationSection;

import lombok.Getter;
import lombok.NonNull;

public class Config {

  @Getter
  protected final Config parent;
  protected ConfigurationSection configurationSection;

  protected Config(Config parent) {
    this.parent = parent;
  }

  public String getAbsolutePath() {
    Config config = this;
    StringBuilder path = new StringBuilder();
    while (config.parent != null) {
      path.insert(0, config.getPath() + ".");
      config = config.parent;
    }

    return config.getPath() + " / " + path;
  }

  public String getPath() {
    return configurationSection.getCurrentPath();
  }

  public String getAbsolutePath(@NonNull String path) {
    return getAbsolutePath() + (getParent() == null ? "" : ".") + path;
  }

}
