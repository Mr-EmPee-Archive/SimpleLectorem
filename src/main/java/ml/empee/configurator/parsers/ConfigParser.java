package ml.empee.configurator.parsers;

import ml.empee.configurator.exceptions.MisconfigurationException;
import org.bukkit.configuration.MemorySection;

public interface ConfigParser<K> {

  K parse(MemorySection section);

  default <T> T requireNonNull(T value) {
    if (value == null) {
      throw MisconfigurationException.builder()
          .message("The value is null")
          .build();
    }

    return value;
  }

}
