package ml.empee.configurator.exceptions;

import lombok.Builder;
import lombok.Getter;

@Getter
public class MalformedConfigurationException extends RuntimeException {

  @Builder
  public MalformedConfigurationException(String message, String path, Throwable cause) {
    super(String.format("%s%n\tPath: %s", message, path), cause);
  }

}
