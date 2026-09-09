package jvmetro.cli;

import java.util.Objects;
import java.util.Map;
import java.util.AbstractMap;
import java.util.ArrayList;

import jvmetro.page.Page;
import jvmetro.page.PageResult;

import jvmetro.service.StationService;
import jvmetro.service.RouteService;
import jvmetro.service.DuplicateEntryException;
import jvmetro.service.EntryNotFoundException;
import jvmetro.model.Station;
import jvmetro.model.Route;
import jvmetro.model.Admin;

// Handles all station-related functions
public class StationsPage implements Page {
  MetroApp.AppUtils utils;
  StationService stationService;
  RouteService routeService;

  ArrayList<Map.Entry<String, Runnable>> menu;

  public StationsPage(MetroApp.AppUtils utils, StationService stationService, RouteService routeService) {
    this.utils = Objects.requireNonNull(utils);
    this.stationService = Objects.requireNonNull(stationService);
    this.routeService = Objects.requireNonNull(routeService);

    menu = new ArrayList<>();
    menu.add(new AbstractMap.SimpleEntry<>("Find Route", this::findRoute));
  }

  public StationsPage(MetroApp.AppUtils utils, Admin admin, StationService stationService, RouteService routeService) {
    this(utils, stationService, routeService);
    Objects.requireNonNull(admin);

    menu.add(new AbstractMap.SimpleEntry<>("Add Station", this::addStation));
    menu.add(new AbstractMap.SimpleEntry<>("Add Route", this::addRoute));
  }

  @Override
  public PageResult show() {
    printStations();

    boolean isStay = utils.askMenu(menu);
    if (isStay)
      return new PageResult.Stay();

    return new PageResult.Back();
  }

  private void addStation() {
    String name = utils.askInput("Station Name: ");
    String location = utils.askInput("Location: ");

    try {
      stationService.addStation(new Station(name, location));
      System.out.println("Station added Successfully");
    } catch (DuplicateEntryException ex) {
      System.err.println(ex.getMessage());
    }
  }

  private void findRoute() {
    String source = utils.askInput("Source Station Name: ");
    String dest = utils.askInput("Destination Station Name: ");

    try {
      Station s1 = stationService.getStation(source);
      Station s2 = stationService.getStation(dest);

      Route route = routeService.findRoute(s1, s2);
      System.out.println("######################");
      System.out.println("Route had been Found: ");
      System.out.printf("Trip = %s <-> %s\nTotal Distance (in KM): %.2f\n",
          source, dest, route.distanceKM());
      System.out.println("######################");
    } catch (EntryNotFoundException ex) {
      System.out.println(ex.getMessage());
    }
  }

  private void addRoute() {
    String src = utils.askInput("Source Station: ");
    String dest = utils.askInput("Destination Station: ");
    String distance = utils.askInput("Distance (in KM): ");

    try {
      Station station1 = stationService.getStation(src);
      Station station2 = stationService.getStation(dest);

      routeService.addRoute(station1, station2, Double.parseDouble(distance));
      System.out.println("Route Added Successfully");
    } catch (NumberFormatException ex) {
      System.err.println("Please Enter a valid distance");
    } catch (EntryNotFoundException | DuplicateEntryException ex) {
      System.err.println(ex.getMessage());
    }
  }

  private void printStations() {
    Station[] stations = stationService.getStations();

    String border = "*".repeat(40);
    System.out.println(border);
    System.out.printf("* %-17s * %-16s *\n", "Station Name", "Location");
    System.out.println(border);

    if (stations.length == 0)
      System.out.printf("%-10s No Stations Found\n", " ");

    for (Station st : stations)
      System.out.printf("* %-17s * %-16s *\n", st.name(), st.location());

    System.out.println(border);
  }
}
