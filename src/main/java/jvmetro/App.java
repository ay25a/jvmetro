package jvmetro;

import jvmetro.repository.TextFileManager;
import java.io.IOException;

import jvmetro.page.PageController;
import jvmetro.cli.CommonPages;

public class App {
  App() throws IOException {
    AppContext.createContext(new TextFileManager());
  }

  private void run() {
    PageController controller = new PageController(CommonPages.introduction);
    controller.run();
  }

  private void saveAll() {
    AppContext scanner = AppContext.getContext();
    try {
      scanner.getUserService().saveUsers(scanner.getFileManager());
      scanner.getStationService().saveStations(scanner.getFileManager());
      scanner.getTrainService().saveTrains(scanner.getFileManager());
      scanner.getRouteService().saveRoutes(scanner.getFileManager());
    } catch (IOException e) {
      System.err.println(e.getMessage());
    }
  }

  public static void main(String[] args) {
    try {
      App app = new App();
      app.run();
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        System.out.println("Shutting down... saving state.");
        app.saveAll();
      }));
    } catch (IOException ex) {
      System.err.println("Failed to Start the Application");
      System.err.println(ex.getMessage());
    }
  }
}
