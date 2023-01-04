package ml.empee.configurator;

import java.util.HashMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ml.empee.configurator.parsers.ConfigParser;
import ml.empee.configurator.parsers.LocationParser;
import org.bukkit.Location;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigManager {

  private static HashMap<Class<?>, ConfigParser<?>> parsers = new HashMap<>();

  static {
    parsers.put(Location.class, new LocationParser());
  }

  public static <T> void registerParser(Class<T> clazz, ConfigParser<T> parser) {
    parsers.put(clazz, parser);
  }

  public static  <T> ConfigParser<T> getParser(Class<T> clazz) {
    return (ConfigParser<T>) parsers.get(clazz);
  }

}
