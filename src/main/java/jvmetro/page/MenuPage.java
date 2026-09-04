package jvmetro.page;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MenuPage implements Page {
  private final ArrayList<MenuItem> items;
  private final String title;
  private final String menuBack;

  public MenuPage(String title, String menuBackMessage, List<MenuItem> items) {
    if (items == null || items.isEmpty())
      throw new IllegalArgumentException("MenuPage has to contain at least one item!");

    this.items = new ArrayList<>(items);
    this.title = Objects.requireNonNullElse(title, "Menu");
    this.menuBack = Objects.requireNonNullElse(menuBackMessage, "Back");
  }

  public PageResult show(java.util.Scanner scanner) {
    System.out.printf("===== %s =====\n", title);

    for (int i = 0; i < items.size(); ++i)
      System.out.printf("(%d) %s\n", i + 1, items.get(i).name());
    System.out.printf("(%d) %s\n", items.size() + 1, menuBack);

    try {
      System.out.print("> ");
      int choice = Integer.parseInt(scanner.nextLine());

      if (choice == items.size() + 1)
        return new PageResult.Back();
      else
        return new PageResult.Next(items.get(choice - 1).page());
    } catch (NumberFormatException | IndexOutOfBoundsException ex) {
      System.out.println("Please Enter a valid Number!");
    }

    return new PageResult.Stay();
  }
}
