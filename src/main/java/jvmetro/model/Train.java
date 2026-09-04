package jvmetro.model;

import java.util.HashMap;
import java.util.Map;

public class Train {
  private String trainName;
  private int capacity;

  public Train(String name, int capacity) throws IllegalArgumentException {
    if (name.isBlank() || capacity < 0)
      throw new IllegalArgumentException("Train cannot be created; Invalid Values");

    this.trainName = name;
    this.capacity = capacity;
  }

  public static Train from(HashMap<String, String> map) throws DeserializationException {
    try {
      return new Train(map.get("name"), Integer.parseInt(map.get("capacity")));
    } catch (NullPointerException ex) {
      throw new DeserializationException("Train cannot be loaded; required fields are missing!");
    } catch (IllegalArgumentException ex) {
      throw new DeserializationException("Train cannot be loaded; incorrect field values!");
    }
  }

  public HashMap<String, String> serialize() {
    return new HashMap<String, String>(Map.of("name", trainName, "capacity", String.valueOf(capacity)));
  }

  public String getName() {
    return this.trainName;
  }

  public int getCapacity() {
    return this.capacity;
  }
}
