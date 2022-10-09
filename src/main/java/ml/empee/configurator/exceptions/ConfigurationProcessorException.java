package ml.empee.configurator.exceptions;

public class ConfigurationProcessorException extends RuntimeException {

  public ConfigurationProcessorException(String message, Throwable cause) {
    super(message, cause);
  }

  public ConfigurationProcessorException(String message) {
    super(message);
  }

}
