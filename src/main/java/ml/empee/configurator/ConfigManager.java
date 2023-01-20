package ml.empee.configurator;

import java.util.HashMap;
import ml.empee.configurator.parsers.ConfigParser;
import ml.empee.configurator.parsers.LocationParser;
import org.bukkit.Location;

public final class ConfigManager {

  private static ConfigManager instance;
  private HashMap<Class<?>, ConfigParser<?>> parsers = new HashMap<>();

  public static ConfigManager getInstance() {
    if(instance == null) {
      instance = new ConfigManager();
    }

    return instance;
  }

  public ConfigManager() {
    parsers.put(Location.class, new LocationParser());
  }

  public <T> void registerParser(Class<T> clazz, ConfigParser<T> parser) {
    parsers.put(clazz, parser);
  }

  public <T> ConfigParser<T> getParser(Class<T> clazz) {
    return (ConfigParser<T>) parsers.get(clazz);
  }

}
