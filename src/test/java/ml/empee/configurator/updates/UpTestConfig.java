package ml.empee.configurator.updates;

import lombok.Getter;
import lombok.Setter;
import ml.empee.configurator.Configuration;
import ml.empee.configurator.annotations.Path;

@Setter @Getter
public class UpTestConfig extends Configuration {

  @Path("bool")
  private Boolean bool;

  public UpTestConfig() {
    super("config.yml", 3, new UpTestConfig_V2());
  }
}
