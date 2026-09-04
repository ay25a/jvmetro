package jvmetro.model;

import java.util.HashMap;
import java.util.Map;

public class Station {
  private String stationName;
  private String location;

  public Station(String name, String location) throws DeserializationException {
    if (name.isBlank() || location.isBlank()) {
      throw new DeserializationException("Station cannot be created; Invalid Values");
    }

    this.stationName = name;
    this.location = location;
  }

  public static Station from(Map<String, String> map) throws DeserializationException {
    try {
      return new Station(map.get("name"), map.get("location"));
    } catch (NullPointerException e) {
      throw new DeserializationException("Station cannot be loaded; require field are missing!");
    } catch (IllegalArgumentException e) {
      throw new DeserializationException("Station cannot be loadded; incorrect field values!");
    }
  }

  public HashMap<String, String> serialized() {
    return new HashMap<String, String>(Map.of("name", stationName, "location", location));
  }

  public String getName() {
    return this.stationName;
  }

  public String getLocation() {
    return this.location;
  }
}
