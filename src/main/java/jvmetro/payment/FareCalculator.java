package jvmetro.payment;

import jvmetro.model.Route;
import jvmetro.model.TicketType;

public interface FareCalculator {
  double calculateFare(Route route, TicketType type);
}
