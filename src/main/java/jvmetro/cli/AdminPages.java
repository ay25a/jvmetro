package jvmetro.cli;

import jvmetro.page.Page;
import jvmetro.page.PageResult;
import jvmetro.page.MenuPage;
import jvmetro.page.MenuItem;
import jvmetro.AppContext;
import jvmetro.service.EntryNotFoundException;
import jvmetro.service.DuplicateEntryException;
import jvmetro.model.Route;
import jvmetro.model.Station;
import jvmetro.model.Ticket;
import jvmetro.model.Train;
import jvmetro.model.User;

import java.util.List;

public class AdminPages {
  private final static Page addStation = (scanner) -> {
    String name = CommonPages.prompt(scanner, "Station Name: ");
    String location = CommonPages.prompt(scanner, "Location: ");

    try {
      Station station = new Station(name, location);
      AppContext.getContext().getStationService().addStation(station);
      System.out.println("Station added Successfully");
    } catch (DuplicateEntryException ex) {
      System.out.println(ex.getMessage());
    }

    return new PageResult.Back();
  };

  private static final Page stationManagement = (scanner) -> {
    CommonPages.stations.show(scanner);
    Page menu = new MenuPage(List.of(new MenuItem("Add Station", addStation)), null, null);
    return new PageResult.Replace(menu);
  };

  private static final Page addTrain = (scanner) -> {
    String name = CommonPages.prompt(scanner, "Train Name: ");
    String capacity = CommonPages.prompt(scanner, "Capacity: ");

    try {
      Train train = new Train(name, Integer.parseInt(capacity));
      AppContext.getContext().getTrainService().addTrain(train);
      System.out.println("Train Added Successfully");
    } catch (NumberFormatException ex) {
      System.out.println("Invalid Train Capacity");
    } catch (DuplicateEntryException | IllegalArgumentException ex) {
      System.out.println(ex.getMessage());
    }

    return new PageResult.Back();
  };

  private static final Page trainManagement = (scanner) -> {
    Train[] trains = AppContext.getContext().getTrainService().getTrains();
    System.out.println("-------- Trains ----------");
    for (Train tr : trains)
      System.out.printf("Train Name = %s\nCapacity = %d\n================\n", tr.name(), tr.capacity());

    Page menu = new MenuPage(List.of(new MenuItem("Add Train", addTrain)), null, null);
    return new PageResult.Replace(menu);
  };

  private static final Page addRoute = (scanner) -> {
    CommonPages.stations.show(scanner);

    AppContext app = AppContext.getContext();
    String src = CommonPages.prompt(scanner, "Source Station: ");
    String dest = CommonPages.prompt(scanner, "Destination Station: ");
    String distance = CommonPages.prompt(scanner, "Distance (in KM): ");

    try {
      Station station1 = app.getStationService().getStation(src);
      Station station2 = app.getStationService().getStation(dest);

      app.getRouteService().addRoute(station1, station2, Double.parseDouble(distance));
      System.out.println("Route Added Successfully");
    } catch (NumberFormatException ex) {
      System.err.println("Please Enter a valid distance");
    } catch (EntryNotFoundException | DuplicateEntryException ex) {
      System.err.println(ex.getMessage());
    }

    return new PageResult.Back();
  };

  public static final Page report = (scanner) -> {
    AppContext app = AppContext.getContext();

    Ticket[] tickets = app.getTicketService().getTickets();
    User[] users = app.getUserService().getUsers();
    Station[] stations = app.getStationService().getStations();
    Route[] routes = app.getRouteService().getRoutes();

    int totalTickets = tickets.length;
    int activeTickets = 0;
    int usedTickets = 0;
    int cancelledTickets = 0;

    double totalFare = 0.0;
    double cancelledFare = 0.0;

    for (Ticket ticket : tickets) {
      totalFare += ticket.getFare();

      switch (ticket.getTicketStatus()) {
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
    System.out.printf(
        "Net Fare: %.2f\n",
        totalFare - cancelledFare);

    System.out.println("============================================");

    return new PageResult.Back();
  };

  public static final Page mainMenu = new MenuPage(
      List.of(
          new MenuItem("Profile", CommonPages.profile),
          new MenuItem("Station Management", stationManagement),
          new MenuItem("Train Management", trainManagement),
          new MenuItem("Find Route", CommonPages.findRoute),
          new MenuItem("Add Route", addRoute),
          new MenuItem("View all Tickets", CommonPages.tickets),
          new MenuItem("Generate Report", report)),
      "Actions", "Logout");
}
