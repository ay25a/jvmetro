package jvmetro;

import jvmetro.page.PageContext;

import java.util.Objects;
import jvmetro.repository.FileManager;
import jvmetro.model.User;
import jvmetro.service.UserService;
import jvmetro.service.StationService;
import jvmetro.service.TrainService;
import jvmetro.service.RouteService;

public class AppContext extends PageContext {
  private final FileManager fileManager;
  private final UserService userService;
  private StationService stationService;
  private TrainService trainService;
  private RouteService routeService;
  private User currentUser;

  public AppContext(FileManager fileManager, UserService userService){
    super(System.out, new java.util.Scanner(System.in));
    this.fileManager = Objects.requireNonNull(fileManager, "FileManager cannot be null");
    this.userService = Objects.requireNonNull(userService, "UserService cannot be null");
  }

  public void loadServices(){
    this.stationService = new StationService(fileManager);
    this.trainService = new TrainService(fileManager);
    this.routeService = new RouteService(fileManager);
  }

  public FileManager getFileManager(){
    return fileManager;
  }

  public UserService getUserService(){
    return userService;
  }

  public void setUser(User user){
    this.currentUser = user;
  }

  public User getUser(){
    return this.currentUser;
  }

  public StationService getStationService(){
    return this.stationService;
  }
  
  public TrainService getTrainService(){
    return this.trainService;
  }

  public RouteService getRouteService(){
    return this.routeService;
  }
}
