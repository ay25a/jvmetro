package jvmetro.cli;

import java.util.Objects;
import java.util.Map;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import jvmetro.page.Page;
import jvmetro.page.PageResult;

import jvmetro.service.TicketService;
import jvmetro.service.StationService;
import jvmetro.service.RouteService;
import jvmetro.service.EntryNotFoundException;
import jvmetro.model.Passenger;
import jvmetro.model.Station;
import jvmetro.model.Route;
import jvmetro.model.Ticket;
import jvmetro.model.TicketType;
import jvmetro.model.TicketStatus;
import jvmetro.model.InsufficientBalanceException;

// Handles all ticket-related functions
public class TicketsPage implements Page {
  MetroApp.AppUtils utils;
  TicketService ticketService;
  StationService stationService;
  RouteService routeService;
  Passenger passenger;
  Ticket[] tickets;

  ArrayList<Map.Entry<String, Runnable>> menu;

  public TicketsPage(MetroApp.AppUtils utils, TicketService ticketService) {
    this.utils = Objects.requireNonNull(utils);
    this.ticketService = Objects.requireNonNull(ticketService);

    menu = new ArrayList<>();
    menu.add(new AbstractMap.SimpleEntry<>("Refresh Tickets", this::refresh));
    menu.add(new AbstractMap.SimpleEntry<>("Only Active Tickets", this::filter));
    menu.add(new AbstractMap.SimpleEntry<>("Sort By Date Tickets", this::sort));
    refresh();
  }

  public TicketsPage(MetroApp.AppUtils utils, Passenger passenger, TicketService ticketService,
      StationService stationService, RouteService routeService) {
    this(utils, ticketService);
    this.passenger = Objects.requireNonNull(passenger);
    this.stationService = Objects.requireNonNull(stationService);
    this.routeService = Objects.requireNonNull(routeService);

    menu.add(new AbstractMap.SimpleEntry<>("Buy Ticket", this::buyTicket));
    menu.add(new AbstractMap.SimpleEntry<>("Use Ticket", this::useTicket));
    menu.add(new AbstractMap.SimpleEntry<>("Cancel Ticket", this::cancelTicket));
    refresh();
  }

  @Override
  public PageResult show() {
    printTickets();

    boolean isStay = utils.askMenu(menu);
    if (isStay)
      return new PageResult.Stay();

    return new PageResult.Back();
  }

  private void filter() {
    tickets = Arrays.stream(tickets).filter(ti -> ti.getStatus() == TicketStatus.ACTIVE).toArray(Ticket[]::new);
  }

  private void sort() {
    Arrays.sort(tickets, Comparator.comparing(Ticket::getIssueDate));
  }

  private void refresh() {
    tickets = passenger == null
        ? ticketService.getTickets()
        : ticketService.getPassengerTickets(passenger).toArray(new Ticket[0]);
  }

  private Route findRoute() {
    String source = utils.askInput("Source Station Name: ");
    String dest = utils.askInput("Destination Station Name: ");

    try {
      Station s1 = stationService.getStation(source);
      Station s2 = stationService.getStation(dest);

      Route route = routeService.findRoute(s1, s2);
      System.out.printf("Trip = %s <-> %s\nTotal Distance (in KM): %.2f\n",
          source, dest, route.distanceKM());
      return route;
    } catch (EntryNotFoundException ex) {
      System.out.println(ex.getMessage());
      return null;
    }
  }

  private void buyTicket() {
    Route route = findRoute();
    if (route == null)
      return;

    try {
      TicketType type = TicketType.valueOf(utils.askInput("Ticket Type (Single, Daily, Monthly): ").toUpperCase());

      double fare = ticketService.calculateFare(route, type);
      System.out.println("Fare: " + fare);

      String confirm = utils.askInput("Confirm Purchase? (Y/N): ");
      if (!confirm.equalsIgnoreCase("y"))
        return;

      ticketService.addTicket(passenger, route, type);
      System.out.println("Ticket bought successfully!");
    } catch (IllegalArgumentException ex) {
      System.err.println("Please enter a valid ticket type!");
    } catch (InsufficientBalanceException ex) {
      System.out.println(ex.getMessage());
    }

    refresh();
  }

  private void useTicket() {
    String ticketId = utils.askInput("Ticket ID: ");

    try {
      Ticket ticket = ticketService.getTicket(Integer.parseInt(ticketId), passenger);
      if (ticket.getStatus() != TicketStatus.ACTIVE)
        System.out.println("Inactive Tickets cannot be Used!");
      else if (ticket.getType() == TicketType.SINGLE)
        ticket.useTicket();
    } catch (EntryNotFoundException | NumberFormatException ex) {
      System.out.println("ID does not correspond to any ticket!");
    }
  }

  private void cancelTicket() {
    System.out.println("Warning: Ticket will not be refunded! Future auto renewals will be cancelled if applicable");
    String ticketId = utils.askInput("Ticket ID: ");

    try {
      Ticket ticket = ticketService.getTicket(Integer.parseInt(ticketId), passenger);
      if (ticket.getStatus() != TicketStatus.ACTIVE)
        System.out.println("Inactive Tickets cannot be cancelled!");
      else
        ticket.cancelTicket();
    } catch (EntryNotFoundException | NumberFormatException ex) {
      System.out.println("ID does not correspond to any ticket!");
    }
  }

  public void printTickets() {
    String border = "*".repeat(50);
    System.out.println(border);

    if (tickets.length == 0)
      System.out.printf("%-10s No Tickets Found\n", " ");

    for (Ticket ti : tickets) {
      System.out.printf("########### ID: %d ##########\n", ti.getID());
      System.out.printf("Ticket ID = %d\nPassenger Email = %s\n", ti.getID(), ti.getPassenger());
      System.out.printf("Ticket Type = %s\nStatus = %s\n", ti.getType().name(), ti.getStatus().name());
      System.out.printf("Fare = %s\nIssue Date = %s\n", ti.getFare(), ti.getIssueDate().toString());
      System.out.printf("Trip: %s <-> %s\n", ti.getSrcStation(), ti.getDestStation());
    }

    System.out.println(border);
  }
}
