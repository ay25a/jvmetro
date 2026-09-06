package jvmetro.model;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Ticket {

  private final int ticketID;
  private final String passengerEmail;
  private final String srcStation;
  private final String destStation;
  private final LocalDate issueDate;
  private final TicketType type;
  private final double fare;
  private TicketStatus status;

  public Ticket(
    int ticketID,
    String passengerEmail,
    String sourceStation,
    String destinationStation,
    TicketType type,
    TicketStatus status,
    double fare,
    LocalDate issueDate
  ) throws IllegalArgumentException {
    if (
      passengerEmail.isBlank() ||
      sourceStation.isBlank() ||
      destinationStation.isBlank() ||
      type == null ||
      status == null ||
      issueDate == null ||
      fare < 0
    ) {
      throw new IllegalArgumentException("Invalid Ticket Values!");
    }

    this.ticketID = ticketID;
    this.passengerEmail = passengerEmail;
    this.srcStation = sourceStation;
    this.destStation = destinationStation;
    this.type = type;
    this.status = status;
    this.fare = fare;
    this.issueDate = issueDate;
  }

  public static Ticket from(HashMap<String, String> map) throws DeserializationException {
    try {
      return new Ticket(
        Integer.parseInt(map.get("id")),
        map.get("passenger"),
        map.get("source_station"),
        map.get("destination_station"),
        TicketType.valueOf(map.get("type")),
        TicketStatus.valueOf(map.get("status")),
        Double.parseDouble(map.get("fare")),
        LocalDate.parse(map.get("issue_date"))
      );
    } catch (NullPointerException | IllegalArgumentException ex) {
      throw new DeserializationException("Ticket is Corrupted!");
    }
  }

  public HashMap<String, String> serialize() {
    HashMap<String, String> parsed = new HashMap<>();
    parsed.putAll(Map.of("id", String.valueOf(ticketID), "passenger", passengerEmail));
    parsed.putAll(Map.of("source_station", srcStation, "destination_station", destStation));
    parsed.putAll(Map.of("type", type.name(), "status", status.name()));
    parsed.putAll(Map.of("fare", String.valueOf(fare), "issue_date", issueDate.toString()));

    return parsed;
  }

  public void cancelTicket() {
    this.status = TicketStatus.CANCELLED;
  }

  public void useTicket() {
    this.status = TicketStatus.USED;
  }

  public int getID() {
    return ticketID;
  }

  public String getPassenger() {
    return passengerEmail;
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

  public TicketType getType() {
    return type;
  }

  public TicketStatus getStatus() {
    return status;
  }
}
