package jvmetro;

import java.io.IOException;

import jvmetro.repository.JsonFileManager;
import jvmetro.repository.FileManager;
import jvmetro.cli.MetroApp;

public class Main {

  // Main entry point of our program
  public static void main(String[] args) {
    FileManager fileManager;

    try {
      // The type of FileManager can be changed here
      fileManager = new JsonFileManager();
    } catch (IOException ex) {
      System.err.println("Application Cannot Start! Error: " + ex.getMessage());
      ex.printStackTrace();
      return;
    }

    MetroApp app = new MetroApp(fileManager);

    // Save the application data even any time the process ends
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      System.out.println("Shutdown triggered! Saving Application State...");

      app.saveState();
    }));

    app.run();
  }
}
