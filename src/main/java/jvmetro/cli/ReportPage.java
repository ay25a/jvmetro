package jvmetro.cli;

import java.util.Objects;
import java.util.Map;
import java.util.AbstractMap;
import java.util.ArrayList;

import jvmetro.page.Page;
import jvmetro.page.PageResult;

import jvmetro.service.TicketService;
import jvmetro.service.StationService;
import jvmetro.service.RouteService;
import jvmetro.service.UserService;
import jvmetro.model.Ticket;
import jvmetro.model.User;
import jvmetro.model.Station;
import jvmetro.model.Route;

// Handles system report generation
public class ReportPage implements Page {
  MetroApp.AppUtils utils;
  TicketService ticketService;
  UserService userService;
  StationService stationService;
  RouteService routeService;

  ArrayList<Map.Entry<String, Runnable>> menu;

  public ReportPage(MetroApp.AppUtils utils, TicketService ticketService, UserService userService,
      StationService stationService, RouteService routeService) {
    this.utils = Objects.requireNonNull(utils);
    this.ticketService = Objects.requireNonNull(ticketService);
    this.userService = Objects.requireNonNull(userService);
    this.stationService = Objects.requireNonNull(stationService);
    this.routeService = Objects.requireNonNull(routeService);

    menu = new ArrayList<>();
    menu.add(new AbstractMap.SimpleEntry<>("Generate Report", this::generateReport));
  }

  @Override
  public PageResult show() {
    boolean isStay = utils.askMenu(menu);
    if (isStay)
      return new PageResult.Stay();

    return new PageResult.Back();
  }

  private void generateReport() {
    Ticket[] tickets = ticketService.getTickets();
    User[] users = userService.getUsers();
    Station[] stations = stationService.getStations();
    Route[] routes = routeService.getRoutes();

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
}
