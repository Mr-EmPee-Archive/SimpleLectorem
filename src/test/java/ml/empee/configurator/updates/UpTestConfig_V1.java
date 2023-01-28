package ml.empee.configurator.updates;

import lombok.Setter;
import ml.empee.configurator.Configuration;
import ml.empee.configurator.annotations.Path;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

@Setter
public class UpTestConfig_V1 extends Configuration {

  @Path("bool")
  private Boolean bool;

  public UpTestConfig_V1(JavaPlugin plugin) {
    super(plugin, "config.yml", 1);
  }

  @Override
  protected void update(YamlConfiguration config) {
    config.set("bool", null);
    config.set("newBool", true);
  }

}
