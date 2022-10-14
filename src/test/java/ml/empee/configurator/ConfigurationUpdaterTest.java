package ml.empee.configurator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lombok.AccessLevel;
import lombok.Setter;
import ml.empee.configurator.annotations.Path;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConfigurationUpdaterTest {

  @BeforeEach
  public void setup() throws IOException {
    new File("src/test/resources/CopiedConfig.yml").delete();
    Files.copy(Paths.get("src/test/resources/SimpleConfig.yml"), Paths.get("src/test/resources/CopiedConfig.yml"));
  }

  @Test
  void shouldUpdateConfig() {
    JavaPlugin plugin = mock(JavaPlugin.class);
    when(plugin.getDataFolder()).thenReturn(new File("src/test/resources"));

    TestConfig config = ConfigurationManager.loadConfiguration(new TestConfig(plugin));
    assert config.bool;
    new File("src/test/resources/MovedConfig.yml").delete();
  }

  static class TestUpdate extends ConfigurationUpdate {

    protected TestUpdate() {
      super(0, "CopiedConfig.yml");
    }

    @Override
    protected void runUpdate(YamlConfiguration config) throws IOException {
      config.set("bool", config.getDouble("double") > 0);
      moveFile(config, "MovedConfig.yml");
      setComments("bool", Arrays.asList("This is a comment"));
    }

  }

  @Setter(AccessLevel.PRIVATE)
  static class TestConfig extends ConfigFile {

    @Path("bool")
    private Boolean bool;

    public TestConfig(JavaPlugin plugin) {
      super(plugin, "MovedConfig.yml");

      registerUpdate(new TestUpdate());
    }
  }

}
