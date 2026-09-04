package jvmetro.service;

public class DuplicateEntryException extends RuntimeException {
  public DuplicateEntryException(String message) {
    super(message);
  }
}
