package ml.empee.configurator;

import ml.empee.configurator.exceptions.MalformedConfigurationException;

public abstract class ConfigSection extends Config {

  protected ConfigSection(String sectionPath, Config parent, boolean required) {
    super(parent);

    configurationSection = parent.configurationSection.getConfigurationSection(sectionPath);
    if (required && configurationSection == null) {
      throw MalformedConfigurationException.builder()
          .path(getAbsolutePath(sectionPath))
          .message("Unable to find the configuration section")
          .build();
    }

  }

}
