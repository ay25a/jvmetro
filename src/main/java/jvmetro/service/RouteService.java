package jvmetro.service;

import jvmetro.model.Route;
import jvmetro.model.Station;
import jvmetro.model.DeserializationException;
import jvmetro.repository.FileManager;
import jvmetro.repository.FileProcessingException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class RouteService {
  private final ArrayList<Route> routes;

  public RouteService(FileManager fileManager) {
    routes = new ArrayList<>();
    ArrayList<HashMap<String, String>> loaded = new ArrayList<>();

    try {
      loaded = fileManager.readData("routes");
    } catch (IOException | FileProcessingException ex) {
      System.err.println("Failed to load routes from a file: " + ex.getMessage());
      return;
    }

    for (HashMap<String, String> parsed : loaded) {
      try {
        routes.add(Route.from(parsed));

      } catch (DeserializationException ex) {
        System.err.println("A route cannot be loaded: " + ex.getMessage());
      }
    }
  }

  public void saveRoutes(FileManager fm) throws IOException {
    ArrayList<HashMap<String, String>> parsed = new ArrayList<>();
    routes.forEach(route -> parsed.add(route.serialize()));

    fm.writeData("routes", parsed);
  }

  public void addRoute(Station src, Station dest, double distance) throws DuplicateEntryException {
    int routeID = (src.name() + dest.name()).hashCode();

    for (Route r : routes) {
      if (r.id() == routeID && r.srcStationName().equals(src.name()))
        throw new DuplicateEntryException("Route already Exist!");
    }

    routes.add(new Route(routeID, src.name(), dest.name(), distance));
  }

  private double findDistance(String first, String from, String to) {
    for (Route route : routes) {
      if (route.srcStationName().equals(from) && route.destStationName().equals(to)) {
        return route.distanceKM();
      }
    }

    for (Route route : routes) {
      if (first != null && first.equals(route.srcStationName()))
        break;

      if (route.srcStationName().equals(from)) {
        if (first == null)
          first = route.srcStationName();
        double remainingDistance = findDistance(first, route.destStationName(), to);

        if (remainingDistance >= 0)
          return route.distanceKM() + remainingDistance;
      }
    }

    return -1;
  }

  public Route findRoute(Station src, Station dest) throws EntryNotFoundException {
    double distance = findDistance(null, src.name(), dest.name());

    if (distance == -1)
      throw new EntryNotFoundException("Route doesn't Exist!");

    return new Route(1, src.name(), dest.name(), distance);
  }

  public Route[] getRoutes() {
    return routes.toArray(Route[]::new);
  }
}
