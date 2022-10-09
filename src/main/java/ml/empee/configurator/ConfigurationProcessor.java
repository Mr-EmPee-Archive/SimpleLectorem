package ml.empee.configurator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import lombok.NonNull;
import lombok.SneakyThrows;
import ml.empee.configurator.annotations.Constraint;
import ml.empee.configurator.annotations.Path;
import ml.empee.configurator.exceptions.ConfigurationProcessorException;
import ml.empee.configurator.exceptions.ConstraintViolation;
import ml.empee.configurator.exceptions.MalformedConfigurationException;

public final class ConfigurationProcessor {

  private ConfigurationProcessor() {
  }

  private static final Field configurationSection;

  static {
    try {
      configurationSection = Config.class.getDeclaredField("configurationSection");
      configurationSection.setAccessible(true);
    } catch (NoSuchFieldException e) {
      throw new ConfigurationProcessorException("Error while baking AnnotationProcessor reflection", e);
    }
  }

  public static <T extends Config> T loadConfiguration(T config) {
    Class<? extends Config> instanceClazz = config.getClass();

    List<ConfigField> configFields = getConfigFields(instanceClazz);
    configFields.forEach(f -> injectConfigValue(config, f));
    return config;
  }

  @SneakyThrows
  private static void injectConfigValue(Config config, ConfigField configField) {
    ConfigurationSection section = (ConfigurationSection) configurationSection.get(config);

    Object value = section.get(configField.getConfigPath());
    if (value != null) {
      Class<?> parameterClass = configField.getParameterClass();
      if (parameterClass.isAssignableFrom(value.getClass())) {
        validateConstraints(value, configField);
      } else {
        if (ConfigSection.class.isAssignableFrom(parameterClass)) {
          Constructor<?> constructor = parameterClass.getConstructor(String.class, Config.class, boolean.class);
          constructor.setAccessible(true);

          value = loadConfiguration(
              (Config) constructor.newInstance(configField.getConfigPath(), config, configField.isRequired())
          );
        } else {
          throw MalformedConfigurationException.builder()
              .message("The config value type doesn't match the parameter class type")
              .path(config.getAbsolutePath(configField.getConfigPath()))
              .build();
        }
      }

      try {
        configField.setField(config, value);
      } catch (InvocationTargetException e) {
        throw MalformedConfigurationException.builder()
            .message("Error while parsing the config value")
            .cause(e.getCause())
            .path(config.getAbsolutePath(configField.getConfigPath()))
            .build();
      }
    } else if (configField.isRequired()) {
      throw MalformedConfigurationException.builder()
          .message("The config value is required")
          .path(config.getAbsolutePath(configField.getConfigPath()))
          .build();
    }
  }

  private static void validateConstraints(Object value, ConfigField configField) {
    Constraint constraint = configField.getConstraints();
    if (constraint == null) {
      return;
    }

    if (value instanceof Number) {
      validateNumericConstraints(new BigDecimal(value.toString()), constraint, configField);
    } else if (value instanceof String) {
      validateStringConstraints((String) value, constraint, configField);
    }
  }

  private static void validateNumericConstraints(@NonNull BigDecimal value, @NonNull Constraint constraint,
      ConfigField configField) {

    if (!constraint.min().isEmpty() && value.compareTo(new BigDecimal(constraint.min())) < 0) {
      throw new ConstraintViolation("The field's value must be greater then " + constraint.min(), configField);
    }

    if (!constraint.max().isEmpty() && value.compareTo(new BigDecimal(constraint.max())) > 0) {
      throw new ConstraintViolation("The field's value must be lower then " + constraint.max(), configField);
    }
  }

  private static void validateStringConstraints(@NonNull String value, @NonNull Constraint constraint,
      ConfigField configField) {

    if (constraint.length() != Integer.MAX_VALUE && value.length() > constraint.length()) {
      throw new ConstraintViolation(
          "The field's value size must be lower then " + constraint.length() + " chars",
          configField
      );
    }

    if (constraint.notBlank() && value.trim().isEmpty()) {
      throw new ConstraintViolation("The field's value can't be blank", configField);
    }

    if (!constraint.pattern().isEmpty() && !value.matches(constraint.pattern())) {
      throw new ConstraintViolation(
          "The field's value must match the pattern '" + constraint.pattern() + "'",
          configField
      );
    }
  }

  /**
   * Retrieve all the @Path annotated fields
   *
   * @see Path
   */
  private static List<ConfigField> getConfigFields(Class<? extends Config> clazz) {
    List<ConfigField> configPairs = new ArrayList<>();
    for (Field field : clazz.getDeclaredFields()) {
      Path path = field.getAnnotation(Path.class);
      if (path == null) {
        continue;
      }

      configPairs.add(
          ConfigField.builder()
              .configClass(clazz)
              .field(field)
              .configPath(path.value())
              .required(path.required())
              .build()
      );
    }

    return configPairs;
  }

}
