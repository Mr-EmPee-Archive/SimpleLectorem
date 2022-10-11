package ml.empee.configurator.components;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import ml.empee.configurator.Config;
import ml.empee.configurator.ConfigSection;
import ml.empee.configurator.annotations.Path;

@Setter(AccessLevel.PRIVATE) @Getter
public class LocationSection extends ConfigSection {

  @Getter(AccessLevel.NONE)
  private Location location;

  @Path("world")
  private World world;
  @Path("x")
  private Double x;
  @Path("y")
  private Double y;
  @Path("z")
  private Double z;

  public LocationSection(String sectionPath, Config parent, boolean required) {
    super(sectionPath, parent, required);
  }

  public Location get() {
    if(location == null) {
      location = new Location(world, x, y, z);
    }

    return location;
  }

  private void setWorld(String worldName) {
    world = Bukkit.getWorld(worldName);
  }

}
