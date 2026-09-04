package jvmetro.model;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class User {
  private static final String EMAIL_REGEX = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@" +
      "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
  private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

  private String name;
  private String email;
  private String password;
  private UserRole role;

  public User(String name, String email, String password, UserRole role) throws IllegalArgumentException {
    if (name.isBlank() || email.isBlank() || password.isBlank() || role == null)
      throw new IllegalArgumentException("User cannot be created; Invalid Values");

    if (!EMAIL_PATTERN.matcher(email).matches())
      throw new IllegalArgumentException("Email format is invalid!");

    if (password.length() < 3)
      throw new IllegalArgumentException("Password should be at least 3 characters!");

    this.name = name;
    this.email = email;
    this.password = password;
    this.role = role;
  }

  public static User from(HashMap<String, String> map) throws DeserializationException {
    try {
      if (UserRole.valueOf(map.get("role")) == UserRole.ADMIN)
        return new Admin(map.get("name"), map.get("email"), map.get("password"));
      else
        return new Passenger(map.get("name"), map.get("email"), map.get("password"),
            Double.valueOf(map.get("balance")));
    } catch (NullPointerException ex) {
      throw new DeserializationException("User cannot be loaded; required fields are missing!");
    } catch (IllegalArgumentException ex) {
      throw new DeserializationException("User cannot be loaded; incorrect field values!");
    }
  }

  public HashMap<String, String> serialize() {
    return new HashMap<String, String>(
        Map.of("name", name, "email", email, "password", password, "role", role.name()));
  }

  public boolean login(String email, String password) {
    return (this.email.equalsIgnoreCase(email) && this.password.equals(password));
  }

  public String getName() {
    return name;
  }

  public void setName(String name) throws IllegalArgumentException {
    if (name.isBlank())
      throw new IllegalArgumentException("Name cannot be empty!");

    this.name = name;
  }

  public String getEmail() {
    return email;
  }
}
