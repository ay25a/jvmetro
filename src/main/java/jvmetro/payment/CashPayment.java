package jvmetro.payment;

public class CashPayment implements Payment {
  @Override
  public boolean pay(double amount) {
    return amount > 0;
  }
}
