package jvmetro.model;

import java.util.HashMap;
import java.util.Map;

public class Route {
  private String routeID;
  private String srcStation;
  private String destStation;
  private double distanceKm;

  public Route(String id, String src, String dest, double distance) throws DeserializationException {
    if (id.isBlank() || src.isBlank() || dest.isBlank() || distance <= 0.0) {
      throw new DeserializationException("Route cannot be created; Invalid Values");
    }

    this.routeID = id;
    this.srcStation = src;
    this.destStation = dest;
    this.distanceKm = distance;
  }

  public static Route from(Map<String, String> map) throws DeserializationException {
    try {
      return new Route(map.get("id"), map.get("src"), map.get("dest"), Double.parseDouble(map.get("distance")));
    } catch (NullPointerException e) {
      throw new DeserializationException("Route cannot be loaded; required field are missing!");
    } catch (IllegalArgumentException e) {
      throw new DeserializationException("Route cannot be loaded; incorrect field values!");
    }
  }

  public HashMap<String, String> serialize() {
    return new HashMap<String, String>(
        Map.of("id", routeID, "src", srcStation, "dest", destStation, "distance", String.valueOf(distanceKm)));
  }

  public String getID() {
    return this.routeID;
  }

  public String getSourceStation() {
    return this.srcStation;
  }

  public String getDestinationStation() {
    return this.destStation;
  }

  public double getDistance() {
    return this.distanceKm;
  }
}
