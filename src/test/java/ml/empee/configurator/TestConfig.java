package ml.empee.configurator;

import java.io.File;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

import lombok.AccessLevel;
import lombok.Setter;
import ml.empee.configurator.annotations.Constraint;
import ml.empee.configurator.annotations.Path;
import ml.empee.configurator.exceptions.ConstraintViolation;
import ml.empee.configurator.exceptions.MalformedConfigurationException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TestConfig {

  @Test
  void testConfigParsing() {
    JavaPlugin plugin = mock(JavaPlugin.class);
    when(plugin.getDataFolder()).thenReturn(new File("src/test/resources"));

    MyConfiguration config = ConfigurationProcessor.processConfiguration(new MyConfiguration(plugin));
    assert config.wordAsDouble == 11.2;
    assert !config.bool;
    assert config.decimal == 10.4;
    assert config.integer == -2;
    assert config.stringList.get(1).equals("World");
    assert config.firstSection.string.equals("Hello from section1!");
    assert config.secondSection.getString("string").equals("Hello from section2!");
  }

  @Test
  void testRequiredField() {
    JavaPlugin plugin = mock(JavaPlugin.class);
    when(plugin.getDataFolder()).thenReturn(new File("src/test/resources"));

    Config testNonExisting = new ConfigFile(plugin, "SimpleConfig.yml") {
      @Setter
      @Path(value = "nonExistingPath", required = true)
      private Double decimal;
    };

    assertThrows(MalformedConfigurationException.class, () -> ConfigurationProcessor.processConfiguration(testNonExisting));

    assert ConfigurationProcessor.processConfiguration(new ConfigFile(plugin, "SimpleConfig.yml") {
      @Setter
      @Path(value = "nonExistingPath")
      private Double decimal;
    }).decimal == null;
  }

  @Test
  void testNumberConstraintValidation() {
    JavaPlugin plugin = mock(JavaPlugin.class);
    when(plugin.getDataFolder()).thenReturn(new File("src/test/resources"));

    Config expectLowerThenMin = new ConfigFile(plugin, "SimpleConfig.yml") {
      @Setter
      @Path("double")
      @Constraint(min = "10.5")
      private Double decimal;
    };
    assertThrows(ConstraintViolation.class, () -> ConfigurationProcessor.processConfiguration(expectLowerThenMin));

    Config expextHigherThenMax = new ConfigFile(plugin, "SimpleConfig.yml") {
      @Setter
      @Path("double")
      @Constraint(max = "10.2")
      private Double decimal;
    };
    assertThrows(ConstraintViolation.class, () -> ConfigurationProcessor.processConfiguration(expextHigherThenMax));

    ConfigurationProcessor.processConfiguration(new ConfigFile(plugin, "SimpleConfig.yml") {
      @Setter
      @Path("double")
      @Constraint(min = "10.2", max = "10.5")
      private Double decimal;
    });
  }

  @Test
  void testStringConstraintValidation() {
    JavaPlugin plugin = mock(JavaPlugin.class);
    when(plugin.getDataFolder()).thenReturn(new File("src/test/resources"));

    Config expectGreaterThen = new ConfigFile(plugin, "SimpleConfig.yml") {
      @Setter
      @Path("section.string")
      @Constraint(length = 10)
      private String string;
    };
    assertThrows(ConstraintViolation.class, () -> ConfigurationProcessor.processConfiguration(expectGreaterThen));

    ConfigurationProcessor.processConfiguration(new ConfigFile(plugin, "SimpleConfig.yml") {
      @Setter
      @Path("section.string")
      @Constraint(length = 30, notBlank = true)
      private String string;
    });

    Config expectPatternViolation = new ConfigFile(plugin, "SimpleConfig.yml") {
      @Setter
      @Path("section.string")
      @Constraint(pattern = "[0-9]")
      private String string;
    };
    assertThrows(ConstraintViolation.class, () -> ConfigurationProcessor.processConfiguration(expectPatternViolation));

    ConfigurationProcessor.processConfiguration(new ConfigFile(plugin, "SimpleConfig.yml") {
      @Setter
      @Path("section.string")
      @Constraint(pattern = ".*")
      private String string;
    });

  }

  @Setter(AccessLevel.PRIVATE)
  private static class MyConfiguration extends ConfigFile {

    @Path("string")
    private Double wordAsDouble;

    private void setWordAsDouble(String word) {
      this.wordAsDouble = Double.parseDouble(word);
    }

    @Path("bool")
    private Boolean bool;

    @Path("double")
    private Double decimal;

    @Path("int")
    private Integer integer;

    @Path("stringList")
    private List<String> stringList;

    @Path("section")
    private FirstSection firstSection;

    @Path("section.section")
    private ConfigurationSection secondSection;

    public MyConfiguration(JavaPlugin plugin) {
      super(plugin, "SimpleConfig.yml");
    }

    @Setter(AccessLevel.PRIVATE)
    private static class FirstSection extends ConfigSection {
      @Path("string")
      private String string;

      public FirstSection(String sectionPath, Config parent, boolean required) {
        super(sectionPath, parent, required);
      }

    }

  }
}
