package jvmetro;

import java.util.Objects;
import jvmetro.repository.FileManager;
import jvmetro.model.Passenger;
import jvmetro.model.User;
import jvmetro.model.UserRole;
import jvmetro.service.UserService;
import jvmetro.service.StationService;
import jvmetro.service.TicketService;
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
  private TicketService ticketService;

  public void initialize(User current) {
    this.currentUser = current;
    this.stationService = new StationService(fileManager);
    this.trainService = new TrainService(fileManager);
    this.routeService = new RouteService(fileManager);

    if (this.currentUser.getRole() == UserRole.PASSENGER)
      this.ticketService = new TicketService(fileManager, (Passenger) currentUser);
    else
      this.ticketService = new TicketService(fileManager, null);

    this.ticketService.updateTickets(userService);
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

  public TicketService getTicketService() {
    return ticketService;
  }
}
