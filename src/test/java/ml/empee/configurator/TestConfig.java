package ml.empee.configurator;

import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import ml.empee.configurator.annotations.Path;
import org.bukkit.Location;

@Getter @Setter(AccessLevel.PRIVATE)
public class TestConfig extends Configuration {
  public TestConfig() {
    super("configTemplate.yml", 1);
  }

  public TestConfig(int version, Configuration nextVersion) {
    super("configTemplate.yml", version, nextVersion);
  }

  @Path("enum")
  private TestEnum testEnum;

  @Path("string")
  private Double wordAsDouble;

  private void setWordAsDouble(String word) {
    this.wordAsDouble = Double.parseDouble(word);
  }

  @Path("bool")
  private Boolean bool;

  @Path("double")
  private Double decimal;

  @Path("int")
  private Integer integer;

  @Path("stringList")
  private List<String> stringList;

  @Path("location")
  private Location location;

  @Path("enumList")
  private List<TestEnum> enumList;

  public enum TestEnum {
    TEST,
    TEST2
  }

}
