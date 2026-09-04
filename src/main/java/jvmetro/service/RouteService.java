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

      for(HashMap<String, String> map : parsed) {
        try {
          routes.add(Route.from(map));
        } catch (DeserializationException e) {
          System.err.println("Skipping loading a route; " + e.getMessage());
        }
      }
    } catch (IOException | FileProcessingException e) {
      System.err.println("Routes will be loaded empty; " + e.getMessage());
    }
  }

  public void saveRoutes(FileManager fm) throws IOException {
    ArrayList<HashMap<String, String>> parsed = new ArrayList<>();

    routes.forEach(route -> parsed.add(route.serialize()));

    fm.writeData("routes", parsed);
  }

  public void addRoute(Station src, Station dest, double distance) throws DuplicateEntryException {
    String routeID = String.valueOf((src.getName() + dest.getName()).hashCode());
    
    for (Route r : routes) {
      if (r.getID().equals(routeID)) {
        throw new DuplicateEntryException("Routes cannot be added; Route already exist!");
      }

      routes.add(new Route(routeID, src.getName(), dest.getName(), distance));
    }
  }

  public Route findRoute(Station src, Station dest) throws EntryNotFoundException{
    String searchID = String.valueOf((src.getName() + dest.getName()).hashCode());

    for (Route r : routes) {
      if (r.getID().equals(searchID)) {
        return r;
      }
    }

    throw new EntryNotFoundException("No route from '" + src.getName() + "' to '" + dest.getName() + "'");
  }

  public Route[] getRoutes() {
    return routes.toArray(Route[]::new);
  }
}
