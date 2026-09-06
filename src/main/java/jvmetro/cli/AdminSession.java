package jvmetro.cli;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import jvmetro.model.Admin;
import jvmetro.model.Route;
import jvmetro.model.Station;
import jvmetro.model.Ticket;
import jvmetro.model.Train;
import jvmetro.model.User;
import jvmetro.page.Page;
import jvmetro.page.PageResult;
import jvmetro.repository.FileManager;
import jvmetro.service.DuplicateEntryException;
import jvmetro.service.EntryNotFoundException;
import jvmetro.service.RouteService;
import jvmetro.service.StationService;
import jvmetro.service.TicketService;
import jvmetro.service.TrainService;

public class AdminSession {

  private final MetroApp app;
  private final Admin admin;
  private final StationService stationService;
  private final TrainService trainService;
  private final RouteService routeService;
  private final TicketService ticketService;

  public AdminSession(MetroApp app, Admin admin) {
    if (app == null || admin == null)
      throw new NullPointerException("Illegal Creation of an Admon Session");

    this.app = app;
    this.admin = admin;
    this.stationService = new StationService(app.getFileManager());
    this.trainService = new TrainService(app.getFileManager());
    this.routeService = new RouteService(app.getFileManager());
    this.ticketService = new TicketService(app.getFileManager(), app.getUserService());
  }

  public void saveState() throws IOException {
    FileManager fileManager = app.getFileManager();
    stationService.saveStations(fileManager);
    trainService.saveTrains(fileManager);
    routeService.saveRoutes(fileManager);
    ticketService.saveTickets(fileManager);
  }

  private Admin getAdmin() {
    return admin;
  }

  private StationService getStationService() {
    return stationService;
  }

  private TrainService getTrainService() {
    return trainService;
  }

  private TicketService getTicketService() {
    return ticketService;
  }

  private RouteService getRouteService() {
    return routeService;
  }

  private void addStation(Scanner scanner) {
    String name = Common.promptInput(scanner, "Station Name: ");
    String location = Common.promptInput(scanner, "Location: ");

    try {
      stationService.addStation(new Station(name, location));
      System.out.println("Station added Successfully");
    } catch (DuplicateEntryException ex) {
      System.out.println(ex.getMessage());
    }
  }

  private void addRoute(Scanner scanner) {
    String src = Common.promptInput(scanner, "Source Station: ");
    String dest = Common.promptInput(scanner, "Destination Station: ");
    String distance = Common.promptInput(scanner, "Distance (in KM): ");

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

  private void findRoute(Scanner scanner) {
    String source = Common.promptInput(scanner, "Source Station Name: ");
    String dest = Common.promptInput(scanner, "Destination Station Name: ");

    try {
      Station s1 = stationService.getStation(source);
      Station s2 = stationService.getStation(dest);

      Route route = routeService.findRoute(s1, s2);
      System.out.println("Route had been Found: ");
      System.out.printf(
        "Source: %s\nDestination: %s\nTotal Distance (in KM): %f\n",
        source,
        dest,
        route.distanceKM()
      );
    } catch (EntryNotFoundException ex) {
      System.out.println(ex.getMessage());
    }
  }

  private void generateReport() {
    Ticket[] tickets = getTicketService().getTickets();
    User[] users = app.getUserService().getUsers();
    Station[] stations = getStationService().getStations();
    Route[] routes = getRouteService().getRoutes();

    int totalTickets = tickets.length;
    int activeTickets = 0;
    int usedTickets = 0;
    int cancelledTickets = 0;

    double totalFare = 0.0;
    double cancelledFare = 0.0;

    for (Ticket ticket : tickets) {
      totalFare += ticket.getFare();

      switch (ticket.getStatus()) {
        case ACTIVE:
          activeTickets++;
          break;
        case USED:
          usedTickets++;
          break;
        case CANCELLED:
          cancelledTickets++;
          cancelledFare += ticket.getFare();
          break;
      }
    }

    int passengers = 0;
    int staff = 0;
    int admins = 0;

    for (User user : users) {
      switch (user.getRole()) {
        case PASSENGER:
          passengers++;
          break;
        case ADMIN:
          admins++;
          break;
      }
    }

    System.out.println("============== SYSTEM REPORT ==============");

    System.out.println("\n--- Users ---");
    System.out.printf("Total Users: %d\n", users.length);
    System.out.printf("Passengers: %d\n", passengers);
    System.out.printf("Staff: %d\n", staff);
    System.out.printf("Admins: %d\n", admins);

    System.out.println("\n--- Network ---");
    System.out.printf("Total Stations: %d\n", stations.length);
    System.out.printf("Total Routes: %d\n", routes.length);

    System.out.println("\n--- Tickets ---");
    System.out.printf("Total Tickets: %d\n", totalTickets);
    System.out.printf("Active Tickets: %d\n", activeTickets);
    System.out.printf("Used Tickets: %d\n", usedTickets);
    System.out.printf("Cancelled Tickets: %d\n", cancelledTickets);

    System.out.println("\n--- Revenue ---");
    System.out.printf("Total Fare Paid: %.2f\n", totalFare);
    System.out.printf("Cancelled Ticket Fare: %.2f\n", cancelledFare);
    System.out.printf("Net Fare: %.2f\n", totalFare - cancelledFare);

    System.out.println("============================================");
  }

  private final Page StationManagement = scanner -> {
    Common.printStations(getStationService().getStations());
    int choice = Common.promptMenu(
      scanner,
      List.of("Add Station", "Add Route", "Find Route"),
      null
    );
    if (choice == Common.MENU_BACK) return new PageResult.Back();

    switch (choice) {
      case 0:
        addStation(scanner);
        break;
      case 1:
        addRoute(scanner);
        break;
      case 2:
        findRoute(scanner);
        break;
    }

    return new PageResult.Stay();
  };

  private final Page TrainManagement = scanner -> {
    Common.printTrains(getTrainService().getTrains());
    int choice = Common.promptMenu(scanner, List.of("Add Train"), null);
    if (choice == 0) {
      String name = Common.promptInput(scanner, "Train Name: ");
      String capacity = Common.promptInput(scanner, "Capacity: ");

      try {
        Train train = new Train(name, Integer.parseInt(capacity));
        getTrainService().addTrain(train);
        System.out.println("Train Added Successfully");
      } catch (NumberFormatException ex) {
        System.out.println("Invalid Train Capacity");
      } catch (DuplicateEntryException | IllegalArgumentException ex) {
        System.out.println(ex.getMessage());
      }
      return new PageResult.Stay();
    }

    return new PageResult.Back();
  };

  private final Page ProfilePage = scanner -> {
    System.out.println("===== Admin Profile =====");
    System.out.println("Name: " + getAdmin().getName());
    System.out.println("Email: " + getAdmin().getEmail());

    int choice = Common.promptMenu(scanner, List.of("Edit Name"), null);
    if (choice == 0) {
      String name = Common.promptInput(scanner, "New Name: ");

      try {
        getAdmin().setName(name);
      } catch (IllegalArgumentException ex) {
        System.err.println(ex.getMessage());
      }

      return new PageResult.Stay();
    }

    return new PageResult.Back();
  };

  public final Page MainMenu = scanner -> {
    System.out.println("[+] Welcome " + this.getAdmin().getName());
    int choice = Common.promptMenu(
      scanner,
      List.of(
        "Profile Page",
        "Station Management",
        "Train Management",
        "View System Tickets",
        "Generate Report"
      ),
      "Log out"
    );

    switch (choice) {
      case 0:
        return new PageResult.Next(ProfilePage);
      case 1:
        return new PageResult.Next(StationManagement);
      case 2:
        return new PageResult.Next(TrainManagement);
      case 3: {
        Common.printTickets(getTicketService().getTickets());
        return new PageResult.Stay();
      }
      case 4: {
        generateReport();
        return new PageResult.Stay();
      }
    }

    return new PageResult.Back();
  };
}
