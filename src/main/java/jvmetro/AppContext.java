package jvmetro;

import java.util.Objects;
import jvmetro.repository.FileManager;
import jvmetro.model.User;
import jvmetro.service.UserService;
import jvmetro.service.StationService;
import jvmetro.service.TrainService;
import jvmetro.service.RouteService;

public class AppContext {
  private static AppContext context;
  private final FileManager fileManager;
  private final UserService userService;

  private AppContext(FileManager fileManager, UserService userService) {
    this.fileManager = Objects.requireNonNull(fileManager, "FileManager cannot be null");
    this.userService = Objects.requireNonNull(userService, "UserService cannot be null");
  }

  public static void createContext(FileManager manager) {
    AppContext.context = new AppContext(manager, new UserService(manager));
  }

  public static AppContext getContext() {
    return Objects.requireNonNull(AppContext.context, "AppContext is not Created yet!");
  }

  private User currentUser;
  private StationService stationService;
  private TrainService trainService;
  private RouteService routeService;

  public void initialize(User current) {
    this.currentUser = current;
    this.stationService = new StationService(fileManager);
    this.trainService = new TrainService(fileManager);
    this.routeService = new RouteService(fileManager);
  }

  public FileManager getFileManager() {
    return fileManager;
  }

  public UserService getUserService() {
    return userService;
  }

  public User getUser() {
    return this.currentUser;
  }

  public StationService getStationService() {
    return this.stationService;
  }

  public TrainService getTrainService() {
    return this.trainService;
  }

  public RouteService getRouteService() {
    return this.routeService;
  }
}
