package jvmetro.cli;

import java.util.Objects;
import java.util.Map;
import java.util.AbstractMap;
import java.util.ArrayList;

import jvmetro.page.Page;
import jvmetro.page.PageResult;

import jvmetro.service.UserService;
import jvmetro.service.InvalidLoginException;
import jvmetro.service.DuplicateEntryException;
import jvmetro.model.User;
import jvmetro.model.Admin;
import jvmetro.model.UserRole;
import jvmetro.model.Passenger;

// Handles all authetication-related functions
// Get the result through "getUser"
public class AuthPage implements Page {
  MetroApp.AppUtils utils;
  User user;
  UserService userService;

  ArrayList<Map.Entry<String, Runnable>> menu;

  public AuthPage(MetroApp.AppUtils utils, UserService userService) {
    this.utils = Objects.requireNonNull(utils);
    this.userService = Objects.requireNonNull(userService);

    menu = new ArrayList<>();
    menu.add(new AbstractMap.SimpleEntry<>("Login", this::login));
    menu.add(new AbstractMap.SimpleEntry<>("Register", this::register));
  }

  // If authentication was not complete, it returns null
  public User getUser() {
    return user;
  }

  @Override
  public PageResult show() {
    System.out.println("Welcome to the Metro App");

    boolean isStay = utils.askMenu(menu);
    if (isStay && user == null)
      return new PageResult.Stay();

    return new PageResult.Back();
  }

  private void login() {
    String email = utils.askInput("Enter Email: ").toLowerCase();
    String password = utils.askInput("Enter Password: ");

    try {
      user = userService.login(email, password);
    } catch (InvalidLoginException ex) {
      System.err.println(ex.getMessage());
    }
  }

  private void register() {
    String name = utils.askInput("Enter Name: ");
    String email = utils.askInput("Enter Email: ").toLowerCase();
    String password = utils.askInput("Enter Password: ");

    try {
      UserRole role = UserRole.valueOf(utils.askInput("Enter Account Type (Admin, Passenger): ").toUpperCase());
      if (role == UserRole.ADMIN)
        user = new Admin(name, email, password);
      else
        user = new Passenger(name, email, password, 0.0);

      userService.addUser(user);

    } catch (IllegalArgumentException | DuplicateEntryException ex) {
      System.err.println(ex.getMessage());
    }
  }
}
