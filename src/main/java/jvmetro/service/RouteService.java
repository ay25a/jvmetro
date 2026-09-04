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
  private ArrayList<Route> routes;

  public RouteService(FileManager fm) {
    routes = new ArrayList<>();

    try {
      ArrayList<HashMap<String, String>> parsed = fm.readData("routes");

      for (HashMap<String, String> map : parsed) {
        try {
          routes.add(Route.from(map));
        } catch (DeserializationException e) {
          System.err.println("Route will be skipped: " + e.getMessage());
        }
      }
    } catch (IOException | FileProcessingException e) {
      System.err.println("Routes will not be loaded: " + e.getMessage());
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
      if(first != null && first.equals(route.srcStationName()))
        break;

      if (route.srcStationName().equals(from)) {
        if(first == null)
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

    if(distance == -1)
      throw new EntryNotFoundException("Route doesn't Exist!");

    return new Route(1, src.name(), dest.name(), distance);
  }

  public Route[] getRoutes() {
    return routes.toArray(Route[]::new);
  }
}
