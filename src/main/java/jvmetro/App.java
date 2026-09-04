package jvmetro;

import jvmetro.repository.FileManager;
import jvmetro.repository.TextFileManager;
import jvmetro.service.UserService;
import java.io.IOException;

import jvmetro.page.PageController;
import jvmetro.cli.UserPages;

public class App {
  private AppContext context;

  App() throws IOException {
    FileManager fm = new TextFileManager();
    this.context = new AppContext(fm, new UserService(fm));
  }

  private void run() {
    PageController controller = new PageController(context, UserPages.introduction);
    controller.run();
  }

  private void saveAll() throws IOException {
    context.getUserService().saveUsers(context.getFileManager());
  }
 
  public static void main(String[] args) {
    try {
      App app = new App();
      app.run();
      app.saveAll();
    } catch (IOException ex) {
      System.err.println("Failed to Start the Application");
      System.err.println(ex.getMessage());
    }
  }
}
