package jvmetro.payment;

public class PaymentService {
  public boolean processPayment(Payment payment, double amount) {
    if (payment == null || amount < 0)
      return false;

    return payment.pay(amount);
  }
}
