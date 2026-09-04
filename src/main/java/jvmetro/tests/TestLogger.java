package jvmetro.tests;

public class TestLogger {
  final String testName;

  TestLogger(String testName){
    this.testName = testName;
  }

  public void logError(String message){
    System.err.printf("[TEST: '%s'] Failed: %s\n", testName, message);
  }

  public void logMessage(String message){
    System.out.printf("[TEST: '%s'] %s\n", testName, message); 
  }

}

