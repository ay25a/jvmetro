package jvmetro.model;

import java.util.HashMap;
import java.util.Map;

// Using a Record since Route is immutable
public record Route(int id, String srcStationName, String destStationName, double distanceKM) {
  public Route {
    if (id == 0 || srcStationName.isBlank() || destStationName.isBlank() || distanceKM <= 0.0)
      throw new IllegalArgumentException("Route is not valid");
  }

  public static Route from(Map<String, String> map) throws DeserializationException {
    try {
      int id = Integer.parseInt(map.get("id"));
      String src = map.get("src");
      String dest = map.get("dest");
      double distance = Double.parseDouble(map.get("distance"));
      return new Route(id, src, dest, distance);

    } catch (NullPointerException | IllegalArgumentException e) {
      throw new DeserializationException("Route is Corrupted!");
    }
  }

  public HashMap<String, String> serialize() {
    HashMap<String, String> parsed = new HashMap<>();
    parsed.putAll(Map.of("id", String.valueOf(id), "src", srcStationName));
    parsed.putAll(Map.of("dest", destStationName, "distance", String.valueOf(distanceKM)));

    return parsed;
  }
}
