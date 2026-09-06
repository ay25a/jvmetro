package jvmetro.cli;

import java.util.Objects;
import java.util.List;
import java.util.Scanner;
import java.io.IOException;

import jvmetro.repository.FileManager;
import jvmetro.service.UserService;
import jvmetro.service.InvalidLoginException;
import jvmetro.service.DuplicateEntryException;

import jvmetro.model.User;
import jvmetro.model.UserRole;
import jvmetro.model.Admin;
import jvmetro.model.Passenger;

import jvmetro.page.PageController;
import jvmetro.page.Page;
import jvmetro.page.PageResult;

public class MetroApp {
  private final FileManager fileManager;
  private final UserService userService;
  private final PageController pageController;
  User currentUser = null;
  private PassengerSession sessionPassenger = null;
  private AdminSession sessionAdmin = null;

  public MetroApp(FileManager fileManager) {
    this.fileManager = Objects.requireNonNull(fileManager, "FileManager cannot be null!");

    this.userService = new UserService(fileManager);
    this.pageController = new PageController(MainMenuPage);
  }

  public void saveState() {
    while (true) {
      try {
        userService.saveUsers(fileManager);

        if (sessionAdmin != null)
          sessionAdmin.saveState();

        if (sessionPassenger != null)
          sessionPassenger.saveState();

        break;
      } catch (IOException ex) {
        System.err.println("Failed to save one or more service data!");
        Scanner scanner = new Scanner(System.in);
        String again = Common.promptInput(scanner, "Try Again? (yes/no) ").toLowerCase();

        if (!again.equals("y") && !again.equals("yes")) {
          System.out.println("Application new Data will not be saved...");
          return;
        }
      }
    }
  }

  public void run() {
    pageController.run();
  }

  public Page login(String email, String password) {
    this.currentUser = this.userService.login(email, password);

    if (currentUser.getRole() == UserRole.ADMIN) {
      sessionAdmin = new AdminSession(this, (Admin) currentUser);
      return sessionAdmin.MainMenu;
    }

    sessionPassenger = new PassengerSession(this, (Passenger) currentUser);
    return sessionPassenger.MainMenu;
  }

  public UserService getUserService() {
    return userService;
  }

  public FileManager getFileManager() {
    return fileManager;
  }

  private Page LoginPage = (scanner) -> {
    System.out.println("===== Login =====");
    String email = Common.promptInput(scanner, "Enter Email: ");
    String password = Common.promptInput(scanner, "Enter Password: ");

    try {
      Page menu = this.login(email, password);
      return new PageResult.Replace(menu);

    } catch (InvalidLoginException ex) {
      System.err.println(ex.getMessage());
    }

    return new PageResult.Back();
  };

  private final Page RegisterPage = (scanner) -> {
    System.out.println("===== Create Account =====");
    int role = Common.promptMenu(scanner, List.of("Admin", "Passenger"), "cancel");
    if (role == Common.MENU_BACK)
      return new PageResult.Back();

    String name = Common.promptInput(scanner, "Enter Name: ");
    String email = Common.promptInput(scanner, "Enter Email: ").toLowerCase();
    String password = Common.promptInput(scanner, "Enter Password: ");

    try {
      if (role == 0)
        getUserService().addUser(new Admin(name, email, password));
      else
        getUserService().addUser(new Passenger(name, email, password, 0.0));

      Page menu = this.login(email, password);
      return new PageResult.Replace(menu);

    } catch (IllegalArgumentException | DuplicateEntryException ex) {
      System.err.println(ex.getMessage());
    } catch (InvalidLoginException ex) {
      System.err.println("An error happend while creating the accont! report this issue to the staff!");
    }

    return new PageResult.Back();
  };

  private final Page MainMenuPage = (scanner) -> {
    int choice = Common.promptMenu(scanner, List.of("Login", "Register"), "Exit");

    switch (choice) {
      case 0:
        return new PageResult.Next(LoginPage);
      case 1:
        return new PageResult.Next(RegisterPage);
    }

    return new PageResult.Exit();
  };
}
