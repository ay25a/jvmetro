package jvmetro.service;

import java.util.HashMap;
import java.util.ArrayList;
import java.io.IOException;

import jvmetro.model.User;
import jvmetro.model.DeserializationException;
import jvmetro.repository.FileManager;
import jvmetro.repository.FileProcessingException;

public class UserService {
  private final HashMap<String, User> users;

  // Load all users using the FileManager
  public UserService(FileManager fileManager) {
    users = new HashMap<>();
    ArrayList<HashMap<String, String>> loaded = new ArrayList<>();

    try {
      loaded = fileManager.readData("users");
    } catch (IOException | FileProcessingException ex) {
      System.err.println("Failed to load users from a file: " + ex.getMessage());
      return;
    }

    for (HashMap<String, String> parsed : loaded) {
      try {
        User user = User.from(parsed);
        users.put(user.getEmail(), user);
      } catch (DeserializationException ex) {
        System.err.println("A user cannot be loaded: " + ex.getMessage());
      }
    }
  }

  // Save all users to a File
  public void saveUsers(FileManager fileManager) throws IOException {
    ArrayList<HashMap<String, String>> parsed = new ArrayList<>();
    users.values().forEach(user -> parsed.add(user.serialize()));

    fileManager.writeData("users", parsed);
  }

  public User getUser(String email) throws EntryNotFoundException {
    User user = users.get(email);

    if(user != null)
      return user;

    throw new EntryNotFoundException("User does not exist!");
  }

  // Add a new user if email doesn't exist already
  public void addUser(User user) throws DuplicateEntryException {
    if (users.putIfAbsent(user.getEmail(), user) != null)
      throw new DuplicateEntryException("User with the same Email already exists!");
  }

  public User[] getUsers() {
    return users.values().toArray(User[]::new);
  }

  // Test the email and password for a matching user if any
  public User login(String email, String password) throws InvalidLoginException {
    User user = users.get(email);

    if (user == null || !user.login(email, password))
      throw new InvalidLoginException("Incorrect email or password!");

    return user;
  }
}
