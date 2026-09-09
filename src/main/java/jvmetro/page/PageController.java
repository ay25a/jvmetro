package jvmetro.page;

import java.util.ArrayDeque;
import java.util.Objects;

public class PageController {
  private ArrayDeque<Page> stack;
  public PageController(){
    stack = new ArrayDeque<>();
  }

  public void run(Page from) {
    Objects.requireNonNull(from, "Starting Page cannot be null!");
    stack.push(from);

    while (!stack.isEmpty()) {
      Page page = stack.peek();

      System.out.println();
      PageResult res = page.show();

      switch (res) {
        case PageResult.Next next:
          stack.push(next.page());
          break;
        case PageResult.Back back:
          stack.pop();
          break;
        case PageResult.Stay s:
          continue;
        case PageResult.Exit e:
          stack.clear();
          break;
      }
    }
  }
}
