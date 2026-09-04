package jvmetro.service;

import java.util.HashMap;
import java.util.ArrayList;
import java.io.IOException;

import jvmetro.model.User;
import jvmetro.model.DeserializationException;
import jvmetro.repository.FileManager;
import jvmetro.repository.FileProcessingException;

public class UserService {
  private HashMap<String, User> users;

  public UserService(FileManager fm) {
    users = new HashMap<>();

    try {
      ArrayList<HashMap<String, String>> parsed = fm.readData("users");

      for (HashMap<String, String> map : parsed) {
        try {
          User user = User.from(map);
          users.put(user.getEmail(), user);
        } catch (DeserializationException ex) {
          System.err.println("Skipping Loading a User: " + ex.getMessage());
        }
      }
    } catch (IOException | FileProcessingException ex) {
      System.err.println("Users will be loaded empty; " + ex.getMessage());
    }
  }

  public void saveUsers(FileManager fm) throws IOException {
    ArrayList<HashMap<String, String>> parsed = new ArrayList<>();

    users.forEach((e, user) -> parsed.add(user.serialize()));

    fm.writeData("users", parsed);
  }

  public void addUser(User user) throws DuplicateEntryException {
    if (users.containsKey(user.getEmail()))
      throw new DuplicateEntryException("User cannot be added; Duplicate name found for " + user.getName());

    users.put(user.getEmail(), user);
  }

  public User[] getUsers() {
    return users.values().toArray(User[]::new);
  }

  public User getUser(String email) throws EntryNotFoundException {
    User user = users.get(email);

    if (user == null)
      throw new EntryNotFoundException("No user found with the provided email");

    return users.get(email);
  }

  public User login(String email, String password) throws InvalidLoginException {
    User user = users.get(email);

    if (user == null || !user.login(email, password))
      throw new InvalidLoginException("Incorrect email or password!");

    return user;
  }
}
