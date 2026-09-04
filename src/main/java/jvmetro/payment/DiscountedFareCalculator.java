package jvmetro.payment;

import jvmetro.model.Route;
import jvmetro.model.TicketType;

public class DiscountedFareCalculator implements FareCalculator {
  private static final double DISCOUNT_RATE = 0.20;

  private final StandardFareCalculator standard = new StandardFareCalculator();

  @Override
  public double calculateFare(Route route, TicketType type) {
    return standard.calculateFare(route, type) * (1 - DISCOUNT_RATE);
  }
}
