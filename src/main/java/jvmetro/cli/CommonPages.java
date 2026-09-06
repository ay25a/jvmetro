package jvmetro.cli;

import jvmetro.page.Page;
import jvmetro.page.PageResult;
import jvmetro.page.MenuPage;
import jvmetro.page.MenuItem;
import jvmetro.AppContext;
import jvmetro.service.InvalidLoginException;
import jvmetro.service.DuplicateEntryException;
import jvmetro.service.EntryNotFoundException;

import jvmetro.model.User;
import jvmetro.model.Admin;
import jvmetro.model.Passenger;
import jvmetro.model.UserRole;
import jvmetro.model.Station;
import jvmetro.model.Ticket;
import jvmetro.model.Route;

import java.util.List;
import java.util.Scanner;

public class CommonPages {
  protected static String prompt(Scanner scanner, String message) {
    System.out.print(message);
    return scanner.nextLine().trim();
  }

  private static Page login = (scanner) -> {
    AppContext app = AppContext.getContext();
    String email = prompt(scanner, "Email Address: ");
    String password = prompt(scanner, "Password: ");

    try {
      User user = app.getUserService().login(email, password);
      app.initialize(user);

      return new PageResult.Replace(user.getRole() == UserRole.ADMIN ? AdminPages.mainMenu : PassengerPages.mainMenu);
    } catch (InvalidLoginException ex) {
      System.err.println(ex.getMessage());
      return new PageResult.Back();
    }
  };

  private static final Page register = (scanner) -> {
    AppContext app = AppContext.getContext();
    String name = prompt(scanner, "Name: ");
    String email = prompt(scanner, "Email Address: ").toLowerCase();
    String password = prompt(scanner, "Password: ");
    String accountType = prompt(scanner, "AccountType (admin, passenger): ").toUpperCase();

    try {
      UserRole role = UserRole.valueOf(accountType);
      Page menu = null;
      User user = null;
      switch (role) {
        case UserRole.ADMIN:
          user = new Admin(name, email, password);
          menu = AdminPages.mainMenu;
          break;
        case UserRole.PASSENGER:
          user = new Passenger(name, email, password, 0.0);
          menu = PassengerPages.mainMenu;
          break;
      }

      app.getUserService().addUser(user);
      app.initialize(user);

      return new PageResult.Replace(menu);
    } catch (IllegalArgumentException | DuplicateEntryException ex) {
      System.err.println(ex.getMessage());
      return new PageResult.Back();
    }
  };

  public static final Page introduction = new MenuPage(
      List.of(new MenuItem("Sign Up", register), new MenuItem("Login", login)),
      "Welcome to the Metro System", "Exit");

  private static final Page editProfile = (scanner) -> {
    try {
      String name = prompt(scanner, "New Name: ");
      AppContext.getContext().getUser().setName(name);
      System.out.println("Name Changed Successfully");
    } catch (IllegalArgumentException ex) {
      System.out.println(ex.getMessage());
    }

    return new PageResult.Back();
  };

  public static final Page profile = (scanner) -> {
    User user = AppContext.getContext().getUser();
    System.out.printf("Name: %s\n", user.getName());
    System.out.printf("Email: %s\n", user.getEmail());

    Page menu = new MenuPage(List.of(new MenuItem("Edit Name", editProfile)), null, null);
    return new PageResult.Replace(menu);
  };

  public static final Page stations = (s) -> {
    Station[] stations = AppContext.getContext().getStationService().getStations();

    System.out.println("-------- Stations ----------");
    for (Station st : stations)
      System.out.printf("Station Name = %s\nLocation = %s\n================\n", st.name(), st.location());

    return new PageResult.Back();
  };

  public static final Page findRoute = (scanner) -> {
    stations.show(scanner);

    AppContext app = AppContext.getContext();
    String source = prompt(scanner, "Source Station Name: ");
    String dest = prompt(scanner, "Destination Station Name: ");

    try {
      Station s1 = app.getStationService().getStation(source);
      Station s2 = app.getStationService().getStation(dest);

      Route route = app.getRouteService().findRoute(s1, s2);
      System.out.println("Route had been Found: ");
      System.out.printf("Source: %s\nDestination: %s\nTotal Distance (in KM): %f\n", source, dest, route.distanceKM());
    } catch (EntryNotFoundException ex) {
      System.out.println(ex.getMessage());
    }

    return new PageResult.Back();
  };

  public static final Page tickets = (scanner) -> {
    AppContext app = AppContext.getContext();
    User user = app.getUser();

    Ticket[] tickets;

    if (user.getRole() == UserRole.ADMIN) {
      tickets = app.getTicketService().getTickets();
    } else {
      tickets = app.getTicketService().getPassengerTickets();
    }

    System.out.println("-------- Tickets ----------");

    for (Ticket ticket : tickets) {
      System.out.printf(
          "Ticket ID = %d\n" +
              "Passenger = %s\n" +
              "Ticket Type = %s\n" +
              "Fare = %.2f\n" +
              "Issue Date = %s\n" +
              "Status = %s\n" +
              "================\n",
          ticket.getID(),
          ticket.getPassenger(),
          ticket.getTicketType(),
          ticket.getFare(),
          ticket.getIssueDate(),
          ticket.getTicketStatus());
    }

    return new PageResult.Back();
  };
}
