package jvmetro.model;

import java.util.HashMap;
import java.util.Map;

public record Train(String name, int capacity) {
  public Train {
    if (name.isBlank() || capacity < 0)
      throw new IllegalArgumentException("Train name cannot be empty, and capacity needs to be more than 0");
  }

  public static Train from(HashMap<String, String> map) throws DeserializationException {
    try {
      return new Train(map.get("name"), Integer.parseInt(map.get("capacity")));
    } catch (NullPointerException | IllegalArgumentException ex) {
      throw new DeserializationException("Train is Corrupted!");
    }
  }

  public HashMap<String, String> serialize() {
    return new HashMap<String, String>(Map.of("name", name, "capacity", String.valueOf(capacity)));
  }
}
