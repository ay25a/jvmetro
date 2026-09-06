package jvmetro;

import java.io.IOException;

import jvmetro.repository.TextFileManager;
import jvmetro.cli.MetroApp;

public class Main {
  public static void main(String[] args) {
    TextFileManager fileManager;

    try {
      fileManager = new TextFileManager();
    } catch (IOException ex) {
      System.err.println("Application Cannot Start! Error: " + ex.getMessage());
      ex.printStackTrace();
      return;
    }

    MetroApp app = new MetroApp(fileManager);
    app.run();
    app.saveState();
  }
}
