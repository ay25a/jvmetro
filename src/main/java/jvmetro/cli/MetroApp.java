package jvmetro.cli;

import java.util.Objects;
import java.util.ArrayList;
import java.util.Map;
import java.util.AbstractMap;
import java.util.Scanner;
import java.io.IOException;

import jvmetro.repository.FileManager;
import jvmetro.service.*;
import jvmetro.payment.PaymentService;

import jvmetro.model.User;
import jvmetro.model.Passenger;
import jvmetro.model.Admin;
import jvmetro.model.UserRole;
import jvmetro.page.*;

public class MetroApp implements Page {
  private final AppUtils utils;
  private final FileManager fileManager;
  private final UserService userService;

  User user;
  private PageController pageController;
  private StationService stationService;
  private TrainService trainService;
  private TicketService ticketService;
  private RouteService routeService;
  private PaymentService paymentService;

  private ArrayList<Map.Entry<String, Page>> menu;
  private AuthPage authPage;
  private ProfilePage profilePage;
  private TicketsPage ticketsPage;
  private StationsPage stationsPage;
  private ReportPage reportPage;
  private TrainsPage trainsPage;

  // First initialization allows to run the authentication page
  public MetroApp(FileManager fileManager) {
    this.fileManager = Objects.requireNonNull(fileManager);
    this.userService = new UserService(fileManager);

    this.utils = new AppUtils(new Scanner(System.in));
    this.authPage = new AuthPage(utils, userService);
    pageController = new PageController();
  }

  // Final initialization of services, pages, and menu
  private void initialize() {
    routeService = new RouteService(fileManager);
    stationService = new StationService(fileManager);

    trainService = new TrainService(fileManager);
    trainsPage = new TrainsPage(utils, trainService);
    ticketService = new TicketService(fileManager, userService);


    if (user.getRole() == UserRole.PASSENGER) {
      paymentService = new PaymentService();
      stationsPage = new StationsPage(utils, stationService, routeService);
      profilePage = new ProfilePage(utils, (Passenger) user, userService, paymentService);
      ticketsPage = new TicketsPage(utils, (Passenger) user, ticketService, stationService, routeService);
    } else {
      profilePage = new ProfilePage(utils, user, userService);
      ticketsPage = new TicketsPage(utils, ticketService);
      reportPage = new ReportPage(utils, ticketService, userService, stationService, routeService);
      stationsPage = new StationsPage(utils, (Admin)user, stationService, routeService);
    }

    menu = new ArrayList<>();
    menu.add(new AbstractMap.SimpleEntry<String, Page>("Profile Page", profilePage));
    menu.add(new AbstractMap.SimpleEntry<String, Page>("Tickets Page", ticketsPage));
    menu.add(new AbstractMap.SimpleEntry<String, Page>("Stations Page", stationsPage));

    if (user.getRole() == UserRole.ADMIN) {
      menu.add(new AbstractMap.SimpleEntry<String, Page>("Report Page", reportPage));
      menu.add(new AbstractMap.SimpleEntry<String, Page>("Trains Page", trainsPage));
    }
  }

  public void saveState() {
    if (user == null)
      return;

    try {
      userService.saveUsers(fileManager);
      if (user.getRole() == UserRole.ADMIN) {
        routeService.saveRoutes(fileManager);
        stationService.saveStations(fileManager);
        trainService.saveTrains(fileManager);
      } else
        ticketService.saveTickets(fileManager);

    } catch (IOException ex) {
      System.err.println("Failed to save one or more service data!");
    }
  }

  public void run() {
    pageController.run(authPage);

    user = authPage.getUser();
    if (user == null)
      return;

    this.initialize();
    pageController.run(this);
    utils.scanner().close();
  }

  @Override
  public PageResult show() {
    Objects.requireNonNull(menu, "Illegal call, run using MetroApp::run instead");

    Page page = utils.askMenu(menu, "Log out");
    if (page != null)
      return new PageResult.Next(page);

    return new PageResult.Exit();
  }

  public static record AppUtils(Scanner scanner) {
    public String askInput(String msg) {
      System.out.print(msg);
      return scanner.nextLine().trim();
    }

    // Prints a menu until a valid item has been choosen
    // Returns true if user done an action or false if user wants to go back
    public boolean askMenu(ArrayList<Map.Entry<String, Runnable>> menu) {
      if (menu == null || menu.size() == 0)
        return false;

      System.out.println("======== Menu ========");
      for (int i = 0; i < menu.size(); ++i)
        System.out.printf("(%d) %s\n", i + 1, menu.get(i).getKey());
      System.out.printf("(%d) %s\n", menu.size() + 1, "Back");

      while (true) {
        try {
          int choice = Integer.parseInt(askInput("> ")) - 1;
          if (choice == menu.size())
            return false;

          menu.get(choice).getValue().run();
          return true;

        } catch (NumberFormatException | IndexOutOfBoundsException ex) {
          System.err.println("Invalid Menu Item");
        }
      }
    }

    public Page askMenu(ArrayList<Map.Entry<String, Page>> menu, String back) {
      if (menu == null || menu.size() == 0)
        return null;

      System.out.println("======== Menu ========");
      for (int i = 0; i < menu.size(); ++i)
        System.out.printf("(%d) %s\n", i + 1, menu.get(i).getKey());

      System.out.printf("(%d) %s\n", menu.size() + 1, back == null ? "Back" : back);
      while (true) {
        try {
          int choice = Integer.parseInt(askInput("> ")) - 1;
          if (choice == menu.size())
            return null;

          return menu.get(choice).getValue();

        } catch (NumberFormatException | IndexOutOfBoundsException ex) {
          System.err.println("Invalid Menu Item");
        }
      }
    }
  }
}
