package jvmetro.page;

import java.util.ArrayDeque;
import java.util.Scanner;
import java.util.Objects;

public class PageController {
  ArrayDeque<Page> stack;
  Scanner scanner;

  public PageController(Page startingPage) {
    Objects.requireNonNull(startingPage, "Starting Page cannot be null!");

    stack = new ArrayDeque<>();
    stack.push(startingPage);
    this.scanner = new Scanner(System.in);
  }

  public void run() {
    while (!stack.isEmpty()) {
      Page page = stack.peek();

      System.out.println();
      PageResult res = page.show(scanner);

      switch (res) {
        case PageResult.Next next:
          stack.push(next.page());
          break;
        case PageResult.Back back:
          stack.pop();
          break;
        case PageResult.Replace replace:
          stack.pop();
          stack.push(replace.page());
          break;
        case PageResult.Stay s:
          continue;
        case PageResult.Exit e:
          stack.clear();
          break;
      }
    }

    this.scanner.close();
  }
}
