package jvmetro.cli;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import jvmetro.model.Station;
import jvmetro.model.Ticket;
import jvmetro.model.Train;

public class Common {
  public static String promptInput(Scanner scanner, String message) {
    System.out.print(message);
    return scanner.nextLine().trim();
  }

  public static final int MENU_BACK = -1;

  public static int promptMenu(Scanner scanner, List<String> menu, String backMessage) {
    if (scanner == null || menu == null)
      throw new NullPointerException("Scanner/Menu cannot be null");
    if (menu.size() == 0)
      return MENU_BACK;

    backMessage = Objects.requireNonNullElse(backMessage, "Back");
    System.out.println("======== Action ========");
    for (int i = 0; i < menu.size(); ++i)
      System.out.printf("(%d) %s\n", i + 1, menu.get(i));

    System.out.printf("(%d) %s\n", menu.size() + 1, backMessage);
    while (true) {
      try {
        int choice = Integer.parseInt(promptInput(scanner, "> "));
        if (choice == menu.size() + 1)
          return MENU_BACK;

        menu.get(choice - 1);
        return choice - 1;
      } catch (NumberFormatException | IndexOutOfBoundsException ex) {
        System.out.println("Please Enter a Valid Menu Item Number!");
      }
    }
  }

  public static void printTrains(Train[] trains) {
    String border = "*".repeat(40);
    System.out.println(border);
    System.out.printf("* %-20s * %-13s *\n", "Train Name", "Capacity");
    System.out.println(border);

    if (trains.length == 0)
      System.out.printf("%-10s No Trains Found\n", " ");

    for (Train tr : trains)
      System.out.printf("* %-20s * %-13s *\n", tr.name(), String.valueOf(tr.capacity()));
    System.out.println(border);
  }

  public static void printStations(Station[] stations) {
    String border = "*".repeat(40);
    System.out.println(border);
    System.out.printf("* %-17s * %-16s *\n", "Station Name", "Location");
    System.out.println(border);

    if (stations.length == 0)
      System.out.printf("%-10s No Stations Found\n", " ");

    for (Station st : stations)
      System.out.printf("* %-17s * %-16s *\n", st.name(), st.location());

    System.out.println(border);
  }

  public static void printTickets(Ticket[] tickets) {
    String border = "*".repeat(50);
    System.out.println(border);

    if (tickets.length == 0)
      System.out.printf("%-10s No Tickets Found\n", " ");

    for (Ticket ti : tickets) {
      System.out.printf("########### ID: %d ##########\n", ti.getID());
      System.out.printf("Ticket ID = %d\nPassenger Email = %s\n", ti.getID(), ti.getPassenger());
      System.out.printf("Ticket Type = %s\nStatus = %s\n", ti.getType().name(), ti.getStatus().name());
      System.out.printf("Fare = %s\nIssue Date = %s\n", ti.getFare(), ti.getIssueDate().toString());
      System.out.printf("Trip: %s -> %s", ti.getSrcStation(), ti.getDestStation());
    }

    System.out.println(border);
  }
}
