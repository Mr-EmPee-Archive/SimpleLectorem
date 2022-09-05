package ml.empee.configurator;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

class TestConfig {
    
    @Test
    void testConfigParsing() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        when(plugin.getDataFolder()).thenReturn( new File("src/test/resources") );

        MyConfiguration config = new MyConfiguration(plugin);
        assert config.wordAsDouble == 11.2;
        assert config.bool == false;
        assert config.decimal == 10.4;
        assert config.integer == -2;
        assert config.stringList.get(1).equals("World");
        assert config.firstSection.string.equals("Hello from section1!");
        assert config.secondSection.getString("string").equals("Hello from section2!");
    }

    @Test
    void testRequiredField() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        when(plugin.getDataFolder()).thenReturn( new File("src/test/resources") );

        assertThrows(MalformedConfigurationException.class, () -> {
            new ConfigFile(plugin, "SimpleConfig.yml", false) {
                @Setter @Path(value = "nonExistingPath", required = true)
                private Double decimal;
            };
        });

        assert new ConfigFile(plugin, "SimpleConfig.yml", false) {
            @Setter @Path(value = "nonExistingPath")
            private Double decimal;
        }.decimal == null;
    }

    @Test
    void testNumberConstraintValidation() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        when(plugin.getDataFolder()).thenReturn( new File("src/test/resources") );

        assertThrows(ConstraintViolation.class, () -> {
            new ConfigFile(plugin, "SimpleConfig.yml", false) {
                @Setter @Path("double") @Constraint(min = "10.5")
                private Double decimal;
            };
        });

        assertThrows(ConstraintViolation.class, () -> {
            new ConfigFile(plugin, "SimpleConfig.yml", false) {
                @Setter @Path("double") @Constraint(max = "10.2")
                private Double decimal;
            };
        });

        new ConfigFile(plugin, "SimpleConfig.yml", false) {
            @Setter @Path("double") @Constraint(min = "10.2", max="10.5")
            private Double decimal;
        };
    }

    @Test
    void testStringConstraintValidation() {
        JavaPlugin plugin = mock(JavaPlugin.class);
        when(plugin.getDataFolder()).thenReturn( new File("src/test/resources") );

        assertThrows(ConstraintViolation.class, () -> {
            new ConfigFile(plugin, "SimpleConfig.yml", false) {
                @Setter @Path("section.string") @Constraint(length = 10)
                private String string;
            };
        });

        new ConfigFile(plugin, "SimpleConfig.yml", false) {
            @Setter @Path("section.string") @Constraint(length = 30, notBlank = true)
            private String string;
        };

        assertThrows(ConstraintViolation.class, () -> {
            new ConfigFile(plugin, "SimpleConfig.yml", false) {
                @Setter @Path("section.string") @Constraint(pattern = "[0-9]")
                private String string;
            };
        });

        new ConfigFile(plugin, "SimpleConfig.yml", false) {
            @Setter @Path("section.string") @Constraint(pattern = ".*")
            private String string;
        };

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
            super(plugin, "SimpleConfig.yml", false);
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
