package jvmetro.model;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Ticket {
  private final int ticketID;
  private final String passengerName;
  private final String srcStation;
  private final String destStation;
  private final LocalDate issueDate;
  private final TicketType ticketType;
  private TicketStatus ticketStatus;
  private final double fare;

  public Ticket(
      int ticketID,
      String passengerName,
      String srcStation,
      String destStation,
      TicketType ticketType,
      TicketStatus ticketStatus,
      double fare,
      LocalDate issueDate) throws IllegalArgumentException {

    if (passengerName.isBlank()
        || srcStation.isBlank() || destStation.isBlank()
        || ticketType == null
        || ticketStatus == null
        || issueDate == null
        || fare < 0) {

      throw new IllegalArgumentException("Invalid Ticket Values!");
    }

    this.ticketID = ticketID;
    this.passengerName = passengerName;
    this.srcStation = srcStation;
    this.destStation = destStation;
    this.ticketType = ticketType;
    this.ticketStatus = ticketStatus;
    this.fare = fare;
    this.issueDate = issueDate;
  }

  public static Ticket from(HashMap<String, String> map) throws DeserializationException {
    try {
      return new Ticket(
          Integer.parseInt(map.get("ticketID")),
          map.get("passengerName"),
          map.get("srcStation"),
          map.get("destStation"),
          TicketType.valueOf(map.get("ticketType")),
          TicketStatus.valueOf(map.get("ticketStatus")),
          Double.parseDouble(map.get("fare")),
          LocalDate.parse(map.get("issueDate")));

    } catch (NullPointerException | IllegalArgumentException ex) {
      throw new DeserializationException("Ticket is Corrupted!");
    }
  }

  public HashMap<String, String> serialize() {
    return new HashMap<>(Map.of(
        "ticketID", String.valueOf(ticketID),
        "passengerName", passengerName,
        "srcStation", srcStation,
        "destStation", destStation,
        "ticketType", ticketType.name(),
        "ticketStatus", ticketStatus.name(),
        "fare", String.valueOf(fare),
        "issueDate", issueDate.toString()));
  }

  public void cancelTicket() {
    this.ticketStatus = TicketStatus.CANCELLED;
  }

  public void useTicket() {
    this.ticketStatus = TicketStatus.USED;
  }

  public int getID() {
    return ticketID;
  }

  public String getPassenger() {
    return passengerName;
  }

  public String getSrcStation() {
    return srcStation;
  }

  public String getDestStation() {
    return destStation;
  }

  public LocalDate getIssueDate() {
    return issueDate;
  }

  public double getFare() {
    return fare;
  }

  public TicketType getTicketType() {
    return ticketType;
  }

  public TicketStatus getTicketStatus() {
    return ticketStatus;
  }
}
