package ml.empee.configurator.parsers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.MemorySection;

public class LocationParser implements ConfigParser<Location> {
  @Override
  public Location parse(MemorySection section) {
    Location location = new Location(null, 0, 0, 0);
    location.setWorld(Bukkit.getWorld(section.getString("world")));
    location.setX(requireNonNull(section.getDouble("x")));
    location.setY(requireNonNull(section.getDouble("y")));
    location.setZ(requireNonNull(section.getDouble("z")));
    location.setYaw((float) section.getDouble("yaw", 0));
    location.setYaw((float) section.getDouble("pitch", 0));
    return location;
  }
}
