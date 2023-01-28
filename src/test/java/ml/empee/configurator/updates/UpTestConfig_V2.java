package ml.empee.configurator.updates;

import lombok.Getter;
import lombok.Setter;
import ml.empee.configurator.Configuration;
import ml.empee.configurator.annotations.Path;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

@Setter @Getter
public class UpTestConfig_V2 extends Configuration {

  @Path("newBool")
  private Boolean bool;

  public UpTestConfig_V2(JavaPlugin plugin) {
    super(plugin, "config.yml", 2, new UpTestConfig_V1(plugin));
  }

  @Override
  protected void update(YamlConfiguration config) {
    config.set("newBool", null);
    config.set("bool", false);
  }
}
