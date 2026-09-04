package jvmetro.page;

import java.util.Scanner;
import java.io.PrintStream;
import java.util.Objects;

public class PageContext{
  final public PrintStream output;
  final public Scanner scanner; 

  public PageContext(PrintStream output, Scanner scanner){
    this.output = Objects.requireNonNull(output, "Page Context Output cannot be null");
    this.scanner = Objects.requireNonNull(scanner, "PageContext Scanner cannot be null");
  }
}
