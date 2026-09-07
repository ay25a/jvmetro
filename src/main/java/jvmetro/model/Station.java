package jvmetro.model;

import java.util.HashMap;
import java.util.Map;

// Using a Record since Station is immutable
public record Station(String name, String location) {
  public Station {
    if (name.isBlank() || location.isBlank())
      throw new IllegalArgumentException("Station Name or Location cannot be empty!");
  }

  public static Station from(HashMap<String, String> map) throws DeserializationException {
    try {
      return new Station(map.get("name"), map.get("location"));
    } catch (NullPointerException | IllegalArgumentException e) {
      throw new DeserializationException("Corrupted Station Record!");
    }
  }

  public HashMap<String, String> serialize() {
    return new HashMap<String, String>(Map.of("name", name, "location", location));
  }
}
