package jvmetro.payment;

import java.util.Map;

import jvmetro.model.Route;
import jvmetro.model.TicketType;

public class StandardFareCalculator implements FareCalculator {
  private static final double RATE_PER_KM = 0.50;

  private static final Map<TicketType, Double> TYPE_MULTIPLIER = Map.of(
    TicketType.SINGLE, 1.0,
    TicketType.DAILY, 3.0,
    TicketType.MONTHLY, 20.0
  );

  @Override
  public double calculateFare(Route route, TicketType type) {
    return route.distanceKM() * RATE_PER_KM * TYPE_MULTIPLIER.get(type);
  }
}
