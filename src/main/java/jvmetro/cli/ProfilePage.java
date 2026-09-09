package jvmetro.cli;

import java.util.Objects;
import java.util.Map;
import java.util.AbstractMap;
import java.util.ArrayList;

import jvmetro.page.Page;
import jvmetro.page.PageResult;

import jvmetro.payment.PaymentService;
import jvmetro.payment.CashPayment;
import jvmetro.payment.CardPayment;

import jvmetro.service.UserService;
import jvmetro.model.User;
import jvmetro.model.UserRole;
import jvmetro.model.Passenger;

// Handles all profile-related functions
public class ProfilePage implements Page {
  MetroApp.AppUtils utils;
  User user;
  UserService userService;
  PaymentService paymentService;

  ArrayList<Map.Entry<String, Runnable>> menu;

  public ProfilePage(MetroApp.AppUtils utils, User user, UserService userService) {
    this.utils = Objects.requireNonNull(utils);
    this.user = Objects.requireNonNull(user);
    this.userService = Objects.requireNonNull(userService);

    menu = new ArrayList<>();
    menu.add(new AbstractMap.SimpleEntry<>("Edit Name", this::changeName));
  }

  public ProfilePage(MetroApp.AppUtils utils, Passenger user, UserService userService, PaymentService paymentService) {
    this(utils, user, userService);
    this.paymentService = Objects.requireNonNull(paymentService);

    menu.add(new AbstractMap.SimpleEntry<>("Charge Balance", this::addBalance));
  }

  @Override
  public PageResult show() {
    System.out.println("===== Profile =====");
    System.out.println("Name: " + user.getName());
    System.out.println("Email: " + user.getEmail());
    if (user.getRole() == UserRole.PASSENGER)
      System.out.println("Balance: " + ((Passenger) user).getBalance());

    boolean isStay = utils.askMenu(menu);
    if (isStay)
      return new PageResult.Stay();

    return new PageResult.Back();
  }

  private void changeName() {
    String name = utils.askInput("Enter New Name: ");

    if (!user.setName(name))
      System.err.println("Please enter a valid name!");
  }

  private void addBalance() {
    String method = utils.askInput("Enter Method (Cash, Card): ");

    try {
      int amount = Integer.parseInt(utils.askInput("Enter Amount: "));
      boolean success = false;

      if (method.equalsIgnoreCase("card")) {
        String card = utils.askInput("Enter Card Number: ");
        success = paymentService.processPayment(new CardPayment(card), amount);
      } else if (method.equalsIgnoreCase("cash"))
        success = paymentService.processPayment(new CashPayment(), amount);
      else
        throw new IllegalArgumentException();

      if (success)
        ((Passenger) user).addBalance(amount);
      else
        System.out.println("Unknown Error Occured");

    } catch (IllegalArgumentException ex) {
      System.err.println("Please Enter a valid input!");
    }
  }
}
