package jvmetro.model;

import java.util.HashMap;
import java.util.Map;

public record Route(int id, String srcStationName, String destStationName, double distanceKM) {
  public Route {
    if (id == 0 || srcStationName.isBlank() || destStationName.isBlank() || distanceKM <= 0.0)
      throw new IllegalArgumentException("Route is not valid");
  }

  public static Route from(Map<String, String> map) throws DeserializationException {
    try {
      return new Route(Integer.parseInt(map.get("id")), map.get("src"), map.get("dest"),
          Double.parseDouble(map.get("distance")));
    } catch (NullPointerException | IllegalArgumentException e) {
      throw new DeserializationException("Route is Corrupted!");
    }
  }

  public HashMap<String, String> serialize() {
    return new HashMap<String, String>(Map.of(
          "id", String.valueOf(id), 
          "src", srcStationName, 
          "dest", destStationName, 
          "distance", String.valueOf(distanceKM)));
  }
}
