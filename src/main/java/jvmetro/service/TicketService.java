package jvmetro.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

import jvmetro.model.Ticket;
import jvmetro.model.TicketType;
import jvmetro.model.TicketStatus;
import jvmetro.model.DeserializationException;
import jvmetro.model.Route;
import jvmetro.model.Passenger;
import jvmetro.repository.FileManager;
import jvmetro.repository.FileProcessingException;
import jvmetro.payment.FareCalculator;
import jvmetro.payment.StandardFareCalculator;
import jvmetro.payment.DiscountedFareCalculator;
import jvmetro.model.InsufficientBalanceException;

public class TicketService {
  private final ArrayList<Ticket> tickets;
  private FareCalculator fareCalculator;
  private ArrayList<Ticket> passengerTickets;
  private int nextTicketID;

  public TicketService(FileManager fm, Passenger passenger) {
    this.tickets = new ArrayList<>();
    this.passengerTickets = new ArrayList<>();
    this.nextTicketID = 1;

    try {
      ArrayList<HashMap<String, String>> parsed = fm.readData("tickets");

      for (HashMap<String, String> map : parsed) {
        try {
          Ticket ticket = Ticket.from(map);
          tickets.add(ticket);

          int id = ticket.getID();
          if (id >= nextTicketID) {
            nextTicketID = id + 1;
          }
        } catch (DeserializationException ex) {
          System.err.println("Skipping loading a ticket: " + ex.getMessage());
        }
      }

      if (passenger != null)
        loadPassenger(passenger);

    } catch (IOException | FileProcessingException ex) {
      System.err.println("Tickets cannot be loaded: " + ex.getMessage());
    }
  }

  public void loadPassenger(Passenger passenger) {
    passengerTickets = new ArrayList<>();

    for (Ticket ticket : tickets) {
      if (ticket.getPassenger().equals(passenger.getEmail())) {
        passengerTickets.add(ticket);
      }
    }

    if (passengerTickets.size() >= 10) {
      fareCalculator = new DiscountedFareCalculator();
    } else {
      fareCalculator = new StandardFareCalculator();
    }
  }

  public void updateTickets(UserService service) {
    ArrayList<Ticket> toRemove = new ArrayList<>();
    LocalDate today = LocalDate.now();

    for (Ticket ticket : tickets) {
      if (ticket.getTicketStatus() != TicketStatus.ACTIVE
          || ticket.getTicketType() == TicketType.SINGLE)
        continue;

      if (ticket.getIssueDate().equals(today))
        continue;

      if (ticket.getTicketType() == TicketType.MONTHLY
          && today.isBefore(ticket.getIssueDate().plusMonths(1)))
        continue;

      try {
        Passenger passenger = (Passenger) service.getUser(ticket.getPassenger());
        passenger.deductBalance(ticket.getFare());

        Ticket renewedTicket = new Ticket(
            nextTicketID++,
            ticket.getPassenger(),
            ticket.getSrcStation(),
            ticket.getDestStation(),
            ticket.getTicketType(),
            ticket.getTicketStatus(),
            ticket.getFare(),
            today);

        tickets.add(renewedTicket);

      } catch (EntryNotFoundException | ClassCastException ex) {
        toRemove.add(ticket);
      } catch (InsufficientBalanceException ex) {
        ticket.cancelTicket();
      }
    }

    tickets.removeAll(toRemove);
  }

  public void saveTickets(FileManager fm) throws IOException {
    ArrayList<HashMap<String, String>> parsed = new ArrayList<>();

    for (Ticket ticket : tickets) {
      parsed.add(ticket.serialize());
    }

    fm.writeData("tickets", parsed);
  }

  public Ticket addTicket(Passenger passenger, Route route, TicketType ticketType) throws InsufficientBalanceException {
    loadPassenger(passenger);

    double fare = fareCalculator.calculateFare(route, ticketType);

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
    passengerTickets.add(ticket);

    return ticket;
  }

  public Ticket[] getPassengerTickets() {
    return passengerTickets.toArray(new Ticket[0]);
  }

  public Ticket findTicket(int ticketID) throws EntryNotFoundException {
    for (Ticket ticket : tickets) {
      if (ticket.getID() == ticketID) {
        return ticket;
      }
    }

    throw new EntryNotFoundException("Ticket not found: " + ticketID);
  }

  public Ticket[] getTickets() {
    return tickets.toArray(new Ticket[0]);
  }

  public FareCalculator getFareCalculator() {
    return this.fareCalculator;
  }
}