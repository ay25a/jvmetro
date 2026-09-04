package jvmetro.model;

import java.util.HashMap;
import java.util.Map;

public class Ticket {
  private String ticketID;
  private String passengerName;
  private String routeID;
  private TicketType ticketType;
  private TicketStatus ticketStatus;
  private double fare;

  public Ticket(String id, String passengerName, String routeID, TicketType ticketType, TicketStatus ticketStatus,
      double fare) throws IllegalArgumentException {
    if (id.isBlank() || passengerName.isBlank() || routeID.isBlank() || ticketType == null || ticketStatus == null
        || fare < 0)
      throw new IllegalArgumentException("Ticket cannot be created; Invalid Values");

    this.ticketID = id;
    this.passengerName = passengerName;
    this.routeID = routeID;
    this.ticketType = ticketType;
    this.ticketStatus = ticketStatus;
    this.fare = fare;
  }

  public static Ticket from(HashMap<String, String> map) throws DeserializationException {
    try {
      return new Ticket(
          map.get("ticketID"),
          map.get("passengerName"),
          map.get("routeID"),
          TicketType.valueOf(map.get("ticketType")),
          TicketStatus.valueOf(map.get("ticketStatus")),
          Double.parseDouble(map.get("fare")));
    } catch (NullPointerException ex) {
      throw new DeserializationException("Ticket cannot be loaded; required fields are missing!");
    } catch (IllegalArgumentException ex) {
      throw new DeserializationException("Ticket cannot be loaded; incorrect field values!");
    }
  }

  public HashMap<String, String> serialize() {
    return new HashMap<String, String>(Map.of(
        "ticketID", ticketID,
        "passengerName", passengerName,
        "routeID", routeID,
        "ticketType", ticketType.name(),
        "ticketStatus", ticketStatus.name(),
        "fare", String.valueOf(fare)));
  }

  public void cancelTicket() {
    this.ticketStatus = TicketStatus.CANCELLED;
  }

  public String getID() {
    return this.ticketID;
  }

  public String getPassenger() {
    return this.passengerName;
  }

  public String getRouteID() {
    return this.routeID;
  }

  public double getFare() {
    return this.fare;
  }

  public TicketType getTicketType() {
    return this.ticketType;
  }

  public TicketStatus getTicketStatus() {
    return this.ticketStatus;
  }
}
