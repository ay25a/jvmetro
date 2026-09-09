package jvmetro.model;

import java.util.HashMap;
import java.util.Objects;
import java.util.Map;
import java.util.regex.Pattern;

public sealed class User permits Passenger, Admin {
  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\s@]+@[^\s@]+\\.[^\s@]+$");

  private String name;
  public String getName() {
    return name;
  }

  // try changing the name
  public boolean setName(String name) {
    if (name.isBlank())
      return false;

    this.name = name;
    return true;
  }

  private final String email;
  public String getEmail() {
    return email;
  }

  private final UserRole role;
  public UserRole getRole() {
    return role;
  }

  private final String password;

  public User(String name, String email, String password, UserRole role) throws IllegalArgumentException {
    if (name.isBlank())
      throw new IllegalArgumentException("Name cannot be empty!");

    // Check if email is valid using our compiled regex
    if (!EMAIL_PATTERN.matcher(email).matches())
      throw new IllegalArgumentException("Email is invalid!");

    if (password.length() < 3)
      throw new IllegalArgumentException("Password should be at least 3 characters!");

    this.name = name;
    this.email = email;
    this.password = password;
    this.role = Objects.requireNonNull(role, "UserRole cannot be null");
  }

  // Create a User from a map
  public static User from(HashMap<String, String> map) throws DeserializationException {
    try {
      String name = map.get("name");
      String email = map.get("email");
      String password = map.get("password");
      UserRole role = UserRole.valueOf(map.get("role"));

      if (role == UserRole.ADMIN)
        return new Admin(name, email, password);
      return new Passenger(name, email, password, Double.valueOf(map.get("balance")));

    } catch (NullPointerException | IllegalArgumentException ex) {
      throw new DeserializationException("Corrupted User Data");
    }
  }

  // Converts the User to a map to store it in a File
  public HashMap<String, String> serialize() {
    HashMap<String, String> parsed = new HashMap<>();
    parsed.putAll(Map.of("name", name, "email", email));
    parsed.putAll(Map.of("password", password, "role", role.name()));

    return parsed;
  }

  // Test if email and password matches
  public boolean login(String email, String password) {
    return this.email.equalsIgnoreCase(email) && this.password.equals(password);
  }
}
