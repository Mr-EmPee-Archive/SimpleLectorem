package ml.empee.configurator;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.SneakyThrows;
import ml.empee.configurator.annotations.Path;
import ml.empee.configurator.exceptions.MisconfigurationException;
import ml.empee.configurator.parsers.ConfigParser;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class Configuration {

  private final JavaPlugin plugin;
  private final ConfigManager configManager = ConfigManager.getInstance();

  private final File file;
  private final String path;
  @Getter
  private final int version;
  private final Configuration previousVersion;
  private YamlConfiguration config;

  protected Configuration(JavaPlugin plugin, String path, int version) {
    this(plugin, path, version, null);
  }

  protected Configuration(JavaPlugin plugin, String path, int version, Configuration previousVersion) {
    this.file = new File(plugin.getDataFolder(), path);
    this.plugin = plugin;
    this.path = path;

    if(!file.exists()) {
      plugin.saveResource(path, false);
    }

    this.version = version;
    this.previousVersion = previousVersion;
    this.config = YamlConfiguration.loadConfiguration(file);

    if (config.getInt("config-version", 1) == version) {
      inject();
    } else {
      update();
    }
  }

  @SneakyThrows
  protected void replaceWithResourceConfig() {
    Files.delete(file.toPath());
    plugin.saveResource(path, false);

    YamlConfiguration newConfig = YamlConfiguration.loadConfiguration(file);
    config.getKeys(true).forEach(key -> {
      newConfig.set(key, config.get(key));
    });

    newConfig.save(file);
  }

  @SneakyThrows
  private void update() {
    if (previousVersion == null) {
      throw new MisconfigurationException(path, null, "No previous version found");
    }

    previousVersion.update(previousVersion.config);
    config = previousVersion.config;
    config.set("config-version", version);

    config.save(file);
    inject();
  }

  protected void update(YamlConfiguration config) {}

  @SneakyThrows
  private void inject() {
    for (Field field : getConfigFields()) {
      String path = field.getAnnotation(Path.class).value();
      Method setter = getFieldSetter(field);
      Objects.requireNonNull(setter, "No setter found for field " + field.getName());

      Object value = config.get(path);
      if (value != null && !isAssignable(field, value)) {
        if (castToEnum(field, value).isPresent()) {
          value = castToEnum(field, value).get();
        } else if (castToCustom(field, value).isPresent()) {
          value = castToCustom(field, value).get();
        } else {
          throw MisconfigurationException.builder()
              .filePath(path)
              .configPath(path)
              .message("Expected " + field.getType().getSimpleName() + " but got " + value.getClass().getSimpleName())
              .build();
        }
      }

      FieldValidator.validate(path, field, value);

      try {
        setter.setAccessible(true);
        setter.invoke(this, value);
      } catch (InvocationTargetException e) {
        throw e.getCause();
      }
    }
  }

  private Optional<?> castToCustom(Field field, Object value) {
    if (!(value instanceof MemorySection)) {
      return Optional.empty();
    }

    Method setter = getFieldSetter(field);
    ConfigParser<?> parser = configManager.getParser(setter.getParameterTypes()[0]);
    if (parser != null) {
      return Optional.ofNullable(parser.parse((MemorySection) value));
    }

    return Optional.empty();
  }

  private Optional<?> castToEnum(Field field, Object value) {
    Method setter = getFieldSetter(field);
    if (setter.getParameterTypes()[0].isEnum()) {
      return Stream.of(setter.getParameterTypes()[0].getEnumConstants())
          .filter(constant -> constant.toString().equalsIgnoreCase(value.toString()))
          .findFirst();
    }

    return Optional.empty();
  }

  private boolean isAssignable(Field field, Object value) {
    Method setter = getFieldSetter(field);
    if (setter != null) {
      Class<?> type = setter.getParameterTypes()[0];
      return type.isAssignableFrom(value.getClass());
    }

    return field.getType().isAssignableFrom(value.getClass());
  }

  private List<Field> getConfigFields() {
    return Arrays.stream(getClass().getDeclaredFields())
        .filter(f -> f.getAnnotation(Path.class) != null)
        .collect(Collectors.toList());
  }

  private Method getFieldSetter(Field field) {
    return Arrays.stream(getClass().getDeclaredMethods())
        .filter(m -> m.getParameterCount() == 1)
        .filter(m -> m.getReturnType() == void.class)
        .filter(m -> m.getName().equalsIgnoreCase("set" + field.getName()))
        .findFirst().orElse(null);
  }
}
