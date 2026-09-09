package jvmetro;

import java.io.IOException;
import jvmetro.test.*;

public class TestMain {
  // Main but for running the tests
  public static void main(String[] args) {
    FileManagerTest fmTest = new FileManagerTest();

    try {
      fmTest.run();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }
}
