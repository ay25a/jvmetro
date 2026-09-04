package jvmetro.model;

import java.util.HashMap;

public final class Passenger extends User {
  private double balance;

  public Passenger(String name, String email, String password, double balance) throws IllegalArgumentException {
    super(name, email, password, UserRole.PASSENGER);

    if (balance < 0.0)
      throw new IllegalArgumentException("Passenger cannot have negative balance!");

    this.balance = balance;
  }

  @Override
  public HashMap<String, String> serialize() {
    HashMap<String, String> map = super.serialize();

    map.put("balance", String.valueOf(balance));
    return map;
  }

  public void deductBalance(double amount) throws InsufficientBalanceException, IllegalArgumentException {
    if (amount < 0.0)
      throw new IllegalArgumentException("Cannot deduct a negative amount!");

    if (amount > balance)
      throw new InsufficientBalanceException("Insufficient Balance!");

    this.balance -= amount;
  }

  public void topupBalance(double amount) throws IllegalArgumentException {
    if (amount < 0.0)
      throw new IllegalArgumentException("Cannot top up a negative amount!");

    this.balance += amount;
  }

  public double getBalance() {
    return balance;
  }
}
