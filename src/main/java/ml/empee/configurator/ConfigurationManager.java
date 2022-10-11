package ml.empee.configurator;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ml.empee.configurator.exceptions.ConfigurationProcessorException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigurationManager {

  public static <T extends ConfigFile> T loadConfiguration(T config) {
    updateConfiguration(config);
    return ConfigurationProcessor.processConfiguration(config);
  }

  private static void updateConfiguration(ConfigFile config) {
    List<ConfigurationUpdate> updates = config.getUpdates();
    updates.sort(Comparator.comparingInt(ConfigurationUpdate::getVersion));
    for(ConfigurationUpdate update : updates) {
      if(update.getVersion() >= config.getVersion()) {
        update.update();
      }
    }

    config.refresh();
  }

}
