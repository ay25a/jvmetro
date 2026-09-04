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

public class AdminPages {


  public static final Page mainMenu = new MenuPage("Admin", "Log out", List.of());; 
}
