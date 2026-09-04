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
  public static boolean isTryAgain(AppContext ctx) {
    ctx.output.print("Try again? ");
    String choice = ctx.scanner.nextLine().toLowerCase().trim();

    return choice.equals("y") || choice.equals("yes");
  }

  private static final Page getUserMenu(User user) {
    if (user instanceof Admin)
      return AdminPages.mainMenu;

    return PassengerPages.mainMenu;
  }

  private static final Page login = ctx -> {
    AppContext context = (AppContext) ctx;

    try {
      ctx.output.print("Email Address: ");
      String email = ctx.scanner.nextLine().trim();

      ctx.output.print("Password: ");
      String password = ctx.scanner.nextLine().trim();

      User user = context.getUserService().login(email, password);

      context.setUser(user);
      context.loadServices();

      return new PageResult.Replace(getUserMenu(user));
    } catch (InvalidLoginException ex) {
      ctx.output.println(ex.getMessage());

      return isTryAgain(context) ? new PageResult.Stay() : new PageResult.Back();
    }
  };

  private static final Page register = ctx -> {
    AppContext context = (AppContext) ctx;

    try {
      ctx.output.print("Full Name: ");
      String name = context.scanner.nextLine();

      ctx.output.print("Email (must be a valid email): ");
      String email = context.scanner.nextLine();

      ctx.output.print("Password (must be at least 3 characters): ");
      String password = context.scanner.nextLine();

      ctx.output.print("Account Type (Admin, Passenger): ");
      String accountType = ctx.scanner.nextLine().toLowerCase().trim();

      User user = null;
      if (accountType.equals("admin"))
        user = new Admin(name, email, password);
      else if (accountType.equals("passenger"))
        user = new Passenger(name, email, password, 0.0);
      else
        throw new IllegalArgumentException("Unknown Account Type Entered");

      context.getUserService().addUser(user);
      context.setUser(user);
      context.loadServices();

      return new PageResult.Replace(getUserMenu(user));
    } catch (IllegalArgumentException | DuplicateEntryException ex) {
      ctx.output.println(ex.getMessage());

      return isTryAgain(context) ? new PageResult.Stay() : new PageResult.Back();
    }
  };

  public static final Page introduction = new MenuPage("Welcome to the Metro System", "Exit", List.of(
      new MenuItem("Register", register),
      new MenuItem("Login", login)));

  public static final Page showProfile = ctx -> {
    AppContext context = (AppContext) ctx;

    User user = context.getUser();

    ctx.output.printf("Name: %s\n", user.getName());
    ctx.output.printf("Email: %s\n", user.getEmail());

    return new PageResult.Back();
  };

  public static final Page editProfile = rctx -> {
    AppContext ctx = (AppContext)rctx;

    ctx.output.println("New Name: ");
    String name = ctx.scanner.nextLine();

    try {
      ctx.getUser().setName(name);
      ctx.output.println("Name Changed Successfully");
    } catch (IllegalArgumentException ex) {
      ctx.output.println(ex.getMessage());
    }

    return new PageResult.Back();
  };
}
