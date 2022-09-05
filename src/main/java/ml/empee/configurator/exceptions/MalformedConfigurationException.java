package ml.empee.configurator.exceptions;

import lombok.Builder;
import lombok.Getter;
import ml.empee.configurator.Config;

@Getter
public class MalformedConfigurationException extends RuntimeException {

    @Builder
    public MalformedConfigurationException(String message, Config config, String path, Throwable cause) {
        super(
            String.format(
            "%s%n\tPath: %s",
                    message, buildAbsolutePath(config, path)
            ), cause
        );
    }

    private static String buildAbsolutePath(Config config, String path) {
        return config.getAbsolutePath() + (config.getParent() == null ? "" : ".") + path;
    }

}
