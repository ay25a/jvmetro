package jvmetro.cli;

import jvmetro.page.Page;
import jvmetro.page.PageResult;
import jvmetro.payment.CardPayment;
import jvmetro.payment.Payment;
import jvmetro.payment.PaymentService;
import jvmetro.service.EntryNotFoundException;
import jvmetro.page.MenuPage;
import jvmetro.AppContext;
import jvmetro.model.InsufficientBalanceException;
import jvmetro.model.Passenger;
import jvmetro.model.Route;
import jvmetro.model.Station;
import jvmetro.model.Ticket;
import jvmetro.model.TicketType;
import jvmetro.page.MenuItem;

import java.util.List;

public class PassengerPages {
  private static final Page useTicket = (scanner) -> {
    AppContext app = AppContext.getContext();

    int ticketID = Integer.parseInt(CommonPages.prompt(scanner, "Ticket ID: "));

    try {
      Ticket ticket = app.getTicketService().findTicket(ticketID);
      ticket.useTicket();

      System.out.println("Ticket used successfully.");

    } catch (EntryNotFoundException ex) {
      System.out.println(ex.getMessage());
    } catch (IllegalArgumentException ex) {
      System.out.println(ex.getMessage());
    }

    return new PageResult.Back();
  };

  private static final Page cancelTicket = (scanner) -> {
    AppContext app = AppContext.getContext();

    int ticketID = Integer.parseInt(CommonPages.prompt(scanner, "Ticket ID: "));

    try {
      Ticket ticket = app.getTicketService().findTicket(ticketID);
      ticket.cancelTicket();

      System.out.println("Ticket cancelled successfully.");

    } catch (EntryNotFoundException ex) {
      System.out.println(ex.getMessage());
    } catch (IllegalArgumentException ex) {
      System.out.println(ex.getMessage());
    }

    return new PageResult.Back();
  };

  public static final Page manageTickets = new MenuPage(
      List.of(
          new MenuItem("Use a Ticket", useTicket),
          new MenuItem("Cancel a Ticket", cancelTicket)),
      "Tickets",
      "Back");
  public static final Page buyTicket = (scanner) -> {
    AppContext app = AppContext.getContext();

    String source = CommonPages.prompt(scanner, "Source Station Name: ");
    String destination = CommonPages.prompt(scanner, "Destination Station Name: ");
    String ticketTypeInput = CommonPages.prompt(scanner, "Ticket Type (single, daily, monthly): ").toUpperCase();

    try {
      Station sourceStation = app.getStationService().getStation(source);

      Station destinationStation = app.getStationService().getStation(destination);

      TicketType ticketType = TicketType.valueOf(ticketTypeInput);

      Route route = app.getRouteService().findRoute(sourceStation, destinationStation);

      double fare = app.getTicketService().getFareCalculator().calculateFare(route, ticketType);

      System.out.printf("Fare: %.2f\n", fare);

      String confirmation = CommonPages.prompt(scanner, "Confirm purchase? (yes/no): ").toLowerCase();

      if (!confirmation.equals("yes")) {
        System.out.println("Ticket purchase cancelled.");
        return new PageResult.Back();
      }

      Passenger passenger = (Passenger) app.getUser();

      Ticket ticket = app.getTicketService().addTicket(passenger, route, ticketType);

      System.out.println("Ticket purchased successfully.");
      System.out.printf("Ticket ID: %d\n", ticket.getID());
      System.out.printf("Fare: %.2f\n", ticket.getFare());

    } catch (EntryNotFoundException | IllegalArgumentException | InsufficientBalanceException ex) {
      System.out.println(ex.getMessage());
    }

    return new PageResult.Back();
  };

  private static final Page cashTopUp = (scanner) -> {
    AppContext app = AppContext.getContext();

    double amount = Double.parseDouble(
        CommonPages.prompt(scanner, "Top-Up Amount: "));

    String confirmation = CommonPages.prompt(
        scanner,
        "Confirm cash top-up of %.2f? (yes/no): ".formatted(amount)).toLowerCase();

    if (!confirmation.equals("yes")) {
      System.out.println("Top-up cancelled.");
      return new PageResult.Back();
    }

    Passenger passenger = (Passenger) app.getUser();
    passenger.topupBalance(amount);

    System.out.println("Cash top-up successful.");

    return new PageResult.Back();
  };

  private static final Page cardTopUp = (scanner) -> {
    AppContext app = AppContext.getContext();

    double amount = Double.parseDouble(
        CommonPages.prompt(scanner, "Top-Up Amount: "));

    String cardNumber = CommonPages.prompt(
        scanner,
        "Card Number: ");

    Payment payment = new CardPayment(cardNumber);

    String confirmation = CommonPages.prompt(
        scanner,
        "Confirm card top-up of %.2f? (yes/no): ".formatted(amount)).toLowerCase();

    if (!confirmation.equals("yes")) {
      System.out.println("Top-up cancelled.");
      return new PageResult.Back();
    }

    Passenger passenger = (Passenger) app.getUser();
    passenger.topupBalance(amount);

    System.out.println("Card top-up successful.");

    return new PageResult.Back();
  };

  private static final Page topUpMenu = new MenuPage(
      List.of(
          new MenuItem("Cash Top-Up", cashTopUp),
          new MenuItem("Card Top-Up", cardTopUp)),
      "Top Up",
      "Back");

  public static final Page topUp = (scanner) -> {
    AppContext app = AppContext.getContext();
    Passenger passenger = (Passenger) app.getUser();

    System.out.printf(
        "Current Balance: %.2f\n",
        passenger.getBalance());

    return new PageResult.Replace(topUpMenu);
  };

  public static final Page mainMenu = new MenuPage(
      List.of(
          new MenuItem("Profile", CommonPages.profile),
          new MenuItem("Find Route", CommonPages.findRoute),
          new MenuItem("View Tickets", manageTickets),
          new MenuItem("Buy a Ticket", buyTicket),
          new MenuItem("Topup", topUp)),
      "Actions", "Logout");
}
