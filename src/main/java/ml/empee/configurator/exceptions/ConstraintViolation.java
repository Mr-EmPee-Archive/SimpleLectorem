package ml.empee.configurator.exceptions;

import ml.empee.configurator.ConfigField;

public class ConstraintViolation extends RuntimeException {

  public ConstraintViolation(String message, ConfigField configField) {
    super(
        String.format(
            "%s%n\tField: %s",
            message, configField.getConfigPath()
        )
    );
  }

}
