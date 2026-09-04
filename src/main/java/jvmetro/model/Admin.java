package jvmetro.model;

public class Admin extends User {
  public Admin(String name, String email, String password) throws IllegalArgumentException {
    super(name, email, password, UserRole.ADMIN);
  }
}
