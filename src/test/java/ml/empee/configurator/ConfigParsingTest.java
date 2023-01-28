package ml.empee.configurator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import ml.empee.configurator.annotations.Max;
import ml.empee.configurator.annotations.Min;
import ml.empee.configurator.annotations.Path;
import ml.empee.configurator.annotations.RegEx;
import ml.empee.configurator.annotations.Required;
import ml.empee.configurator.exceptions.MisconfigurationException;
import ml.empee.configurator.updates.UpTestConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ConfigParsingTest {
  private static final File resource = new File("src/test/resources");

  private static final JavaPlugin plugin = Mockito.mock(JavaPlugin.class);

  @BeforeAll
  static void setupClass() {
    Mockito.when(plugin.getDataFolder()).thenReturn(resource);
    Mockito.mockStatic(Bukkit.class).when(() -> Bukkit.getWorld(anyString())).thenReturn(null);
  }

  @BeforeEach
  void setup() throws IOException {
    Files.copy(
        resource.toPath().resolve("configTemplate.yml"),
        resource.toPath().resolve("config.yml"), StandardCopyOption.REPLACE_EXISTING
    );
  }

  @Test
  void shouldParseConfig() {
    TestConfig config = new TestConfig(plugin);

    assertEquals(11.2, config.getWordAsDouble());
    assertEquals(TestConfig.TestEnum.TEST, config.getTestEnum());
    assertEquals(false, config.getBool());
    assertEquals(10.4, config.getDecimal());
    assertEquals(-2, config.getInteger());
    assertEquals(Arrays.asList("Hello", "World"), config.getStringList());
    assertEquals(new Location(null, 1, 2, 3), config.getLocation());
  }

  @Test
  void shouldValidateConfigs() {
    new Configuration(plugin, "config.yml", 1) {
      @Path("double") @Min(10)
      private Double decimal;

      public void setDecimal(Double decimal) {
        this.decimal = decimal;
      }
    };
    assertThrows(MisconfigurationException.class, () -> new Configuration(plugin, "config.yml", 1) {
      @Path("double") @Min(15)
      private Double decimal;

      public void setDecimal(Double decimal) {
        this.decimal = decimal;
      }
    });

    new Configuration(plugin, "config.yml", 1) {
      @Path("double") @Max(15)
      private Double decimal;

      public void setDecimal(Double decimal) {
        this.decimal = decimal;
      }
    };
    assertThrows(MisconfigurationException.class, () -> new Configuration(plugin, "config.yml", 1) {
      @Path("double") @Max(10.2)
      private Double decimal;

      public void setDecimal(Double decimal) {
        this.decimal = decimal;
      }
    });

    new Configuration(plugin, "config.yml", 1) {
      @Path("stringList") @RegEx("[A-Z][a-z]*")
      private List<String> stringList;

      public void setStringList(List<String> stringList) {
        this.stringList = stringList;
      }
    };

    assertThrows(MisconfigurationException.class, () -> new Configuration(plugin, "config.yml", 1) {
      @Path("stringList") @RegEx("[A-Z]*")
      private List<String> stringList;

      public void setStringList(List<String> stringList) {
        this.stringList = stringList;
      }
    });

    new Configuration(plugin, "config.yml", 1) {
      @Path("stringList") @Required
      private List<String> stringList;

      public void setStringList(List<String> stringList) {
        this.stringList = stringList;
      }
    };

    assertThrows(MisconfigurationException.class, () -> new Configuration(plugin, "config.yml", 1) {
      @Path("stringListNonExisting") @Required
      private List<String> stringList;

      public void setStringList(List<String> stringList) {
        this.stringList = stringList;
      }
    });
  }

  @Test
  void shouldUpdateConfig() {
    UpTestConfig config = new UpTestConfig(plugin);
    assertEquals(false, config.getBool());
  }

}
