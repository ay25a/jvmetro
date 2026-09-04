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
  private static final Page addStation = rctx -> {
    AppContext ctx = (AppContext)rctx;

    ctx.output.print("Enter Station Name: ");
    String name = ctx.scanner.nextLine();

    ctx.output.print("Enter Station Location: ");
    String location = ctx.scanner.nextLine();

    try {
      Station station = new Station(name, location);
      ctx.getStationService().addStation(station);
      ctx.output.println("Station added Successfully");
    }catch(DuplicateEntryException ex){
      ctx.output.println(ex.getMessage());
    }

    return new PageResult.Back();
  };

  private static final Page addTrain = rctx -> {
    AppContext ctx = (AppContext)rctx;

    ctx.output.print("Enter Train Name: ");
    String name = ctx.scanner.nextLine();

    ctx.output.print("Enter Train Capacity: ");
    String capacity = ctx.scanner.nextLine();

    try {
      Train train = new Train(name, Integer.parseInt(capacity));
      ctx.getTrainService().addTrain(train);
      ctx.output.println("Train Added Successfully");
    }catch(NumberFormatException ex){
      ctx.output.println("Invalid Train Capacity");
    }
    catch(DuplicateEntryException | IllegalArgumentException ex){
      ctx.output.println(ex.getMessage());
    }

    return new PageResult.Back();
  };

  public static final Page mainMenu = new MenuPage("Admin", "Log out", List.of(
      new MenuItem("Show Profile", CommonPages.showProfile),
      new MenuItem("Change User Name", CommonPages.editProfile),
      new MenuItem("Add a Station", addStation),
      new MenuItem("Add a Train", addTrain)));
}
