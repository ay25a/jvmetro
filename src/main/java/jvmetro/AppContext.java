package jvmetro;

import jvmetro.page.PageContext;

import java.util.Objects;
import jvmetro.repository.FileManager;
import jvmetro.model.User;
import jvmetro.service.UserService;

public class AppContext extends PageContext {
  private final FileManager fileManager;
  private final UserService userService;
  private User currentUser;

  public AppContext(FileManager fileManager, UserService userService){
    super(System.out, new java.util.Scanner(System.in));
    this.fileManager = Objects.requireNonNull(fileManager, "FileManager cannot be null");
    this.userService = Objects.requireNonNull(userService, "UserService cannot be null");
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
}
