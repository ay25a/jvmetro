package jvmetro.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import jvmetro.model.DeserializationException;
import jvmetro.model.InsufficientBalanceException;
import jvmetro.model.Passenger;
import jvmetro.model.Route;
import jvmetro.model.Ticket;
import jvmetro.model.TicketStatus;
import jvmetro.model.TicketType;
import jvmetro.payment.DiscountedFareCalculator;
import jvmetro.payment.FareCalculator;
import jvmetro.payment.StandardFareCalculator;
import jvmetro.repository.FileManager;
import jvmetro.repository.FileProcessingException;

public class TicketService {

  private final ArrayList<Ticket> tickets;
  private int nextTicketID;

  public TicketService(FileManager fileManager, UserService userService) {
    this.tickets = new ArrayList<>();
    this.nextTicketID = 1;

    ArrayList<HashMap<String, String>> loaded = new ArrayList<>();

    try {
      loaded = fileManager.readData("tickets");
    } catch (IOException | FileProcessingException ex) {
      System.err.println(
          "Failed to load trains from a file: " + ex.getMessage());
      return;
    }

    for (HashMap<String, String> parsed : loaded) {
      try {
        Ticket ticket = updateTicket(userService, Ticket.from(parsed));
        if (ticket != null)
          tickets.add(ticket);

        int id = ticket.getID();
        if (id >= nextTicketID)
          nextTicketID = id + 1;
      } catch (DeserializationException ex) {
        System.err.println("A train cannot be loaded: " + ex.getMessage());
      }
    }
  }

  public Ticket updateTicket(UserService userService, Ticket ticket) {
    LocalDate today = LocalDate.now();
    if (ticket.getStatus() != TicketStatus.ACTIVE || ticket.getType() == TicketType.SINGLE)
      return ticket;

    if (ticket.getIssueDate().equals(today))
      return ticket;

    if (ticket.getType() == TicketType.MONTHLY && today.isBefore(ticket.getIssueDate().plusMonths(1)))
      return ticket;

    try {
      Passenger passenger = (Passenger) userService.getUser(ticket.getPassenger());
      passenger.deductBalance(ticket.getFare());

      Ticket renewedTicket = new Ticket(
          nextTicketID++,
          ticket.getPassenger(),
          ticket.getSrcStation(),
          ticket.getDestStation(),
          ticket.getType(),
          ticket.getStatus(),
          ticket.getFare(),
          today);

      return renewedTicket;
    } catch (EntryNotFoundException | ClassCastException ex) {
      return null;
    } catch (InsufficientBalanceException ex) {
      ticket.cancelTicket();
      return ticket;
    }
  }

  public void saveTickets(FileManager fm) throws IOException {
    ArrayList<HashMap<String, String>> parsed = new ArrayList<>();

    for (Ticket ticket : tickets) {
      parsed.add(ticket.serialize());
    }

    fm.writeData("tickets", parsed);
  }

  public double calculateFare(Route route, TicketType ticketType) {
    FareCalculator fareCalculator = route.distanceKM() < 100 ? new StandardFareCalculator()
        : new DiscountedFareCalculator();
    return fareCalculator.calculateFare(route, ticketType);
  }

  public Ticket addTicket(Passenger passenger, Route route, TicketType ticketType) throws InsufficientBalanceException {
    double fare = calculateFare(route, ticketType);
    passenger.deductBalance(fare);

    Ticket ticket = new Ticket(
        nextTicketID++,
        passenger.getEmail(),
        route.srcStationName(),
        route.destStationName(),
        ticketType,
        TicketStatus.ACTIVE,
        fare,
        LocalDate.now());

    tickets.add(ticket);
    return ticket;
  }

  public ArrayList<Ticket> getPassengerTickets(Passenger passenger) {
    ArrayList<Ticket> tickets = new ArrayList<>();

    for (Ticket ticket : this.tickets) {
      if (ticket.getPassenger().equals(passenger.getEmail()))
        tickets.add(ticket);
    }

    return tickets;
  }

  public Ticket findTicket(int ticketID) throws EntryNotFoundException {
    for (Ticket ticket : tickets) {
      if (ticket.getID() == ticketID)
        return ticket;
    }

    throw new EntryNotFoundException("Ticket not found: " + ticketID);
  }

  public Ticket[] getTickets() {
    return tickets.toArray(new Ticket[0]);
  }

  public Ticket getTicket(int id, Passenger passenger) throws EntryNotFoundException {
    for (Ticket ti : tickets) {
      if (ti.getID() == id && ti.getPassenger().equals(passenger.getEmail()))
        return ti;
    }

    throw new EntryNotFoundException("Ticket ID was not Found!");
  }
}
