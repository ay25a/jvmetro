package jvmetro.cli;

import jvmetro.page.Page;
import jvmetro.page.PageResult;
import jvmetro.page.MenuPage;
import jvmetro.page.MenuItem;
import jvmetro.AppContext;
import jvmetro.service.InvalidLoginException;
import jvmetro.service.DuplicateEntryException;

import jvmetro.model.User;
import jvmetro.model.Admin;
import jvmetro.model.Passenger;

import java.util.List;

public class CommonPages {
  private static Page getUserMenu(User user) {
    if (user instanceof Admin)
      return AdminPages.mainMenu;

    return PassengerPages.mainMenu;
  }

  private static final Page login = scanner -> {
    try {
      System.out.print("Email Address: ");
      String email = scanner.nextLine().trim();

      System.out.print("Password: ");
      String password = scanner.nextLine().trim();

      AppContext app = AppContext.getContext();
      User user = app.getUserService().login(email, password);
      app.initialize(user);

      return new PageResult.Replace(getUserMenu(user));
    } catch (InvalidLoginException ex) {
      System.out.println(ex.getMessage());
    }

    return new PageResult.Back();
  };

  private static final Page register = scanner -> {
    try {
      System.out.print("Full Name: ");
      String name = scanner.nextLine();

      System.out.print("Email (must be a valid email): ");
      String email = scanner.nextLine();

      System.out.print("Password (must be at least 3 characters): ");
      String password = scanner.nextLine();

      System.out.print("Account Type (Admin, Passenger): ");
      String accountType = scanner.nextLine().toLowerCase().trim();

      User user = null;
      if (accountType.equals("admin"))
        user = new Admin(name, email, password);
      else if (accountType.equals("passenger"))
        user = new Passenger(name, email, password, 0.0);
      else
        throw new IllegalArgumentException("Unknown Account Type Entered");

      AppContext app = AppContext.getContext();
      app.getUserService().addUser(user);
      app.initialize(user);

      return new PageResult.Replace(getUserMenu(user));
    } catch (IllegalArgumentException | DuplicateEntryException ex) {
      System.out.println(ex.getMessage());
    }

    return new PageResult.Back();
  };

  public static final Page introduction = new MenuPage("Welcome to the Metro System", "Exit",
      List.of(new MenuItem("Register", register), new MenuItem("Login", login)));

  private static final Page editProfile = scanner -> {
    System.out.print("New Name: ");
    String name = scanner.nextLine();

    try {
      AppContext.getContext().getUser().setName(name);
      System.out.println("Name Changed Successfully");
    } catch (IllegalArgumentException ex) {
      System.out.println(ex.getMessage());
    }

    return new PageResult.Back();
  };

  public static final Page profile = scanner -> {
    User user = AppContext.getContext().getUser();
    System.out.printf("Name: %s\n", user.getName());
    System.out.printf("Email: %s\n", user.getEmail());

    Page menu = new MenuPage("Action", "Back", List.of(new MenuItem("Edit Name", editProfile)));
    return new PageResult.Replace(menu);
  };
}
