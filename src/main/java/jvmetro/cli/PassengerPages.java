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

public class PassengerPages {
  public static final Page mainMenu = new MenuPage("Passenger", "Log out", List.of(
      new MenuItem("Show Profile", CommonPages.showProfile),
      new MenuItem("Change User Name", CommonPages.editProfile)));
}
