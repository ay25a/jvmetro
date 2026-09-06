package jvmetro.cli;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import jvmetro.model.Passenger;
import jvmetro.model.Route;
import jvmetro.model.Station;
import jvmetro.model.Ticket;
import jvmetro.model.TicketType;
import jvmetro.model.InsufficientBalanceException;
import java.util.ArrayList;
import jvmetro.page.Page;
import jvmetro.page.PageResult;
import jvmetro.payment.CardPayment;
import jvmetro.payment.CashPayment;
import jvmetro.payment.PaymentService;
import jvmetro.repository.FileManager;
import jvmetro.service.EntryNotFoundException;
import jvmetro.service.RouteService;
import jvmetro.service.StationService;
import jvmetro.service.TicketService;
import jvmetro.service.TrainService;

public class PassengerSession {
  private final MetroApp app;
  private final Passenger passenger;
  private final PaymentService paymentService;

  private final StationService stationService;
  private final TrainService trainService;
  private final RouteService routeService;
  private final TicketService ticketService;

  public PassengerSession(MetroApp app, Passenger current) {
    if (app == null || current == null)
      throw new NullPointerException("Illegal Creation of Passenger Session!");

    this.app = app;
    this.passenger = current;
    this.paymentService = new PaymentService();
    this.stationService = new StationService(app.getFileManager());
    this.trainService = new TrainService(app.getFileManager());
    this.routeService = new RouteService(app.getFileManager());
    this.ticketService = new TicketService(app.getFileManager(), app.getUserService());
  }

  public void saveState() throws IOException {
    FileManager fileManager = app.getFileManager();
    ticketService.saveTickets(fileManager);
  }

  private Passenger getPassenger() {
    return passenger;
  }

  private Route findRoute(Scanner scanner) {
    String source = Common.promptInput(scanner, "Source Station Name: ");
    String dest = Common.promptInput(scanner, "Destination Station Name: ");

    try {
      Station s1 = stationService.getStation(source);
      Station s2 = stationService.getStation(dest);

      Route route = routeService.findRoute(s1, s2);
      System.out.println("######################");
      System.out.println("Route had been Found: ");
      System.out.printf("Trip = %s <-> %s\nTotal Distance (in KM): %.2f\n",
          source, dest, route.distanceKM());
      System.out.println("######################");
      return route;
    } catch (EntryNotFoundException ex) {
      System.out.println(ex.getMessage());
      return null;
    }
  }

  private final Page StationsPage = scanner -> {
    Common.printStations(getStationService().getStations());
    int choice = Common.promptMenu(scanner, List.of("Find Route"), null);
    if (choice == 0) {
      findRoute(scanner);
      return new PageResult.Stay();
    }

    return new PageResult.Back();
  };

  private final Page TicketsPage = (scanner) -> {
    TicketService ticketService = getTicketService();
    ArrayList<Ticket> tickets = ticketService.getPassengerTickets(getPassenger());

    Common.printTickets(tickets.toArray(new Ticket[0]));
    int choice = Common.promptMenu(scanner, List.of("Buy Ticket", "Use Ticket", "Cancel Ticket"), null);

    switch (choice) {
      case 0: {
        Route route = findRoute(scanner);
        if (route == null)
          return new PageResult.Stay();

        int typeChoice = Common.promptMenu(scanner, List.of("Single", "Daily", "Monthly"), "cancel");

        if (typeChoice == Common.MENU_BACK)
          return new PageResult.Stay();

        TicketType tiType = null;
        if (typeChoice == 0)
          tiType = TicketType.SINGLE;
        else if (typeChoice == 1)
          tiType = TicketType.DAILY;
        else
          tiType = TicketType.MONTHLY;

        try {
          double fare = ticketService.calculateFare(route, tiType);
          System.out.println("Fare: " + fare);

          int confirm = Common.promptMenu(scanner, List.of("Confirm"), "cancel");
          if (confirm == Common.MENU_BACK)
            return new PageResult.Stay();

          ticketService.addTicket(getPassenger(), route, tiType);
          System.out.println("Ticket bought successfully!");
        } catch (InsufficientBalanceException ex) {
          System.out.println(ex.getMessage());
        }

        return new PageResult.Stay();
      }

      case 1: {
        String ticketId = Common.promptInput(scanner, "Ticket ID: ");
        try {
          Ticket ticket = getTicketService().getTicket(Integer.parseInt(ticketId), getPassenger());
          ticket.useTicket();
        } catch (EntryNotFoundException | NumberFormatException ex) {
          System.out.println("ID does not correspond to any ticket!");
        }
        return new PageResult.Stay();
      }

      case 2: {
        String ticketId = Common.promptInput(scanner, "Ticket ID: ");
        try {
          Ticket ticket = getTicketService().getTicket(Integer.parseInt(ticketId), getPassenger());
          ticket.cancelTicket();
        } catch (EntryNotFoundException | NumberFormatException ex) {
          System.out.println("ID does not correspond to any ticket!");
        }
        return new PageResult.Stay();
      }

      default:
        return new PageResult.Back();
    }
  };

  public MetroApp getApp() {
    return app;
  }

  public PaymentService getPaymentService() {
    return paymentService;
  }

  public StationService getStationService() {
    return stationService;
  }

  public TrainService getTrainService() {
    return trainService;
  }

  public RouteService getRouteService() {
    return routeService;
  }

  public TicketService getTicketService() {
    return ticketService;
  }

  private void topup(Scanner scanner) {
    String ramount = Common.promptInput(scanner, "Enter Amount: ");
    int method = Common.promptMenu(scanner, List.of("Card", "Cash"), "cancel");
    if (method == Common.MENU_BACK)
      return;

    try {
      boolean success = false;
      int amount = Integer.parseInt(ramount);

      if (method == 1)
        success = paymentService.processPayment(
            new CashPayment(),
            amount);
      else if (method == 0) {
        String card = Common.promptInput(scanner, "Enter Card Number: ");
        success = paymentService.processPayment(new CardPayment(card), amount);
      }

      if (success) {
        passenger.addBalance(amount);
        System.out.println("Payment done successfully!");
      } else {
        System.out.println("Unknown Error Occured");
      }
    } catch (IllegalArgumentException ex) {
      System.err.println("Invalid Amount Entered!");
    }
  }

  private final Page ProfilePage = scanner -> {
    System.out.println("===== Passenger Profile =====");
    System.out.println("Name: " + getPassenger().getName());
    System.out.println("Email: " + getPassenger().getEmail());
    System.out.println("Balance: " + getPassenger().getBalance());

    int choice = Common.promptMenu(scanner, List.of("Edit Name", "Topup Balance"), null);
    switch (choice) {
      case 0: {
        try {
          String name = Common.promptInput(scanner, "New Name: ");
          getPassenger().setName(name);
        } catch (IllegalArgumentException ex) {
          System.err.println(ex.getMessage());
        }
        break;
      }
      case 1: {
        topup(scanner);
        break;
      }
      case Common.MENU_BACK:
        return new PageResult.Back();
    }

    return new PageResult.Stay();
  };

  public final Page MainMenu = scanner -> {
    System.out.println("[+] Welcome " + this.getPassenger().getName());
    int choice = Common.promptMenu(scanner,
        List.of("Profile Page", "Stations Page", "My Tickets"), "Log out");

    switch (choice) {
      case 0:
        return new PageResult.Next(ProfilePage);
      case 1:
        return new PageResult.Next(StationsPage);
      case 2:
        return new PageResult.Next(TicketsPage);
    }

    return new PageResult.Back();
  };
}
