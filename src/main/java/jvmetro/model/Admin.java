package jvmetro.model;

public final class Admin extends User {
  public Admin(String name, String email, String password) throws IllegalArgumentException {
    super(name, email, password, UserRole.ADMIN);
  }
}
