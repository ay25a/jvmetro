package jvmetro.service;

import java.util.HashMap;
import java.util.ArrayList;
import java.io.IOException;
import java.util.UUID;

import jvmetro.model.Ticket;
import jvmetro.model.TicketType;
import jvmetro.model.TicketStatus;
import jvmetro.model.DeserializationException;
import jvmetro.model.Route;
import jvmetro.model.Passenger;
import jvmetro.model.User;
import jvmetro.repository.FileManager;
import jvmetro.repository.FileProcessingException;
import jvmetro.payment.FareCalculator;
import jvmetro.payment.StandardFareCalculator;
import jvmetro.payment.DiscountedFareCalculator;

public class TicketService {
  private static final int FREQUENT_RIDER_THRESHOLD = 10;

  private ArrayList<Ticket> tickets;
  private FareCalculator fareCalculator;

  public TicketService(FileManager fm, User currentUser) {
    tickets = new ArrayList<>();

    try {
      ArrayList<HashMap<String, String>> parsed = fm.readData("tickets");

      for (HashMap<String, String> map : parsed) {
        try {
          tickets.add(Ticket.from(map));
        } catch (DeserializationException ex) {
          System.err.println("Skipping Loading a Ticket: " + ex.getMessage());
        }
      }
    } catch (IOException | FileProcessingException ex) {
      System.err.println("Tickets will be loaded empty; " + ex.getMessage());
    }

    long existingTicketCount = tickets.stream()
        .filter(t -> t.getPassenger().equals(currentUser.getName()))
        .count();

    this.fareCalculator = (existingTicketCount > FREQUENT_RIDER_THRESHOLD)
        ? new DiscountedFareCalculator()
        : new StandardFareCalculator();
  }

  public void saveTickets(FileManager fm) throws IOException {
    ArrayList<HashMap<String, String>> parsed = new ArrayList<>();

    tickets.forEach(ticket -> parsed.add(ticket.serialize()));

    fm.writeData("tickets", parsed);
  }

  public double calculateFare(Route route, TicketType type) {
    return fareCalculator.calculateFare(route, type);
  }

  public void addTicket(Passenger passenger, Route route, TicketType ticketType) {
    double fare = calculateFare(route, ticketType);

    Ticket ticket = new Ticket(
        UUID.randomUUID().toString(),
        passenger.getName(),
        route.getID(),
        ticketType,
        TicketStatus.ACTIVE,
        fare);

    tickets.add(ticket);
  }

  public Ticket[] getTickets(Passenger passenger) {
    return tickets.stream()
        .filter(t -> t.getPassenger().equals(passenger.getName()))
        .toArray(Ticket[]::new);
  }

  public Ticket[] getTickets() {
    return tickets.toArray(Ticket[]::new);
  }
}
