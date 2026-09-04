package jvmetro.payment;

public class CardPayment implements Payment {
  private String cardNumber;

  public CardPayment(String cardNumber) {
    this.cardNumber = cardNumber;
  }

  @Override
  public boolean pay(double amount) {
    return amount > 0 && cardNumber != null && !cardNumber.isBlank();
  }
}
