package jvmetro.service;

public class InvalidLoginException extends RuntimeException {
  public InvalidLoginException(String message) {
    super(message);
  }
}
