package ml.empee.configurator.exceptions;

import lombok.Builder;

public class MisconfigurationException extends RuntimeException {

  private final String configPath;
  private final String message;
  private final String filePath;

  @Builder
  public MisconfigurationException(String configPath, String message, String filePath) {
    super("Misconfiguration in " + filePath + " at " + configPath + ": " + message);

    this.configPath = configPath;
    this.message = message;
    this.filePath = filePath;
  }
}
