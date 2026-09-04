package jvmetro.cli;

import jvmetro.page.Page;
import jvmetro.page.PageResult;
import jvmetro.page.MenuPage;
import jvmetro.page.MenuItem;
import jvmetro.AppContext;
import jvmetro.service.DuplicateEntryException;

import jvmetro.model.Station;
import jvmetro.model.Train;
import java.util.List;

public class AdminPages {
  private static final Page addStation = scanner -> {
    System.out.print("Enter Station Name: ");
    String name = scanner.nextLine();

    System.out.print("Enter Station Location: ");
    String location = scanner.nextLine();

    try {
      Station station = new Station(name, location);
      AppContext.getContext().getStationService().addStation(station);
      System.out.println("Station added Successfully");
    } catch (DuplicateEntryException ex) {
      System.out.println(ex.getMessage());
    }

    return new PageResult.Back();
  };

  private static final Page addTrain = scanner -> {
    System.out.print("Enter Train Name: ");
    String name = scanner.nextLine();

    System.out.print("Enter Train Capacity: ");
    String capacity = scanner.nextLine();

    try {
      Train train = new Train(name, Integer.parseInt(capacity));
      AppContext.getContext().getTrainService().addTrain(train);
      System.out.println("Train Added Successfully");
    } catch (NumberFormatException ex) {
      System.out.println("Invalid Train Capacity");
    } catch (DuplicateEntryException | IllegalArgumentException ex) {
      System.out.println(ex.getMessage());
    }

    return new PageResult.Back();
  };

  public static final Page mainMenu = new MenuPage("Admin", "Log out", List.of(
      new MenuItem("Profile Page", CommonPages.profile),
      new MenuItem("Add a Station", addStation),
      new MenuItem("Add a Train", addTrain)));
}
