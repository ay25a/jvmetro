package jvmetro.tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Map;
import java.io.IOException;

import jvmetro.repository.*;

public class FileManagerTest {
  final ArrayList<HashMap<String, String>> RECORDS;
  final TestLogger logger = new TestLogger("FileManagerTest");

  public FileManagerTest() {
    HashMap<String, String> map1 = new HashMap<>(Map.of(
        "user1", "Alice Smith",
        "user2", "Bob_123",
        "user3", "Charlie-7",
        "user4", "X92"));

    HashMap<String, String> map2 = new HashMap<>(Map.of(
        "item1", "Apple",
        "item2", "B42",
        "item3", "Orange Juice",
        "item4", "Mango-7",
        "item5", "Grape"));

    HashMap<String, String> map3 = new HashMap<>(Map.of(
        "code1", "A123",
        "code2", "B-456",
        "code3", "XYZ",
        "code4", "C789",
        "code5", "Test 9",
        "code6", "LMN_2"));

    HashMap<String, String> map4 = new HashMap<>(Map.of(
        "key1", "Red",
        "key2", "Blue 42",
        "key3", "Green",
        "key4", "Y-7",
        "key5", "Yellow_9",
        "key6", "Black"));

    HashMap<String, String> map5 = new HashMap<>(Map.of(
        "prod1", "Laptop",
        "prod2", "Phone 12",
        "prod3", "Tablet",
        "prod4", "Monitor-5",
        "prod5", "Mouse",
        "prod6", "Kbd_88",
        "prod7", "Printer"));

    HashMap<String, String> map6 = new HashMap<>(Map.of(
        "id1", "A1",
        "id2", "B-2",
        "id3", "C3",
        "id4", "User 42",
        "id5", "D_5",
        "id6", "EF99",
        "id7", "G7",
        "id8", "H-123"));

    HashMap<String, String> map7 = new HashMap<>(Map.of(
        "name1", "John",
        "name2", "Sarah 7",
        "name3", "Mike",
        "name4", "Emma_22",
        "name5", "David"));

    HashMap<String, String> map8 = new HashMap<>(Map.of(
        "type1", "Admin",
        "type2", "User 01",
        "type3", "Guest",
        "type4", "Mod-7",
        "type5", "Staff",
        "type6", "Dev_123",
        "type7", "QA"));

    RECORDS = new ArrayList<>(Arrays.asList(map1, map2, map3, map4, map5, map6, map7, map8));
  }

  private void testManager(FileManager fm) {
    try {
      fm.writeData("test_data", RECORDS);

      ArrayList<HashMap<String, String>> received = fm.readData("test_data");

      if (RECORDS.equals(received))
        logger.logMessage(fm.toString() + " Succeded!");
      else
        logger.logError(fm.toString() + " read mismatched input!");
    } catch (IOException ex) {
      logger.logError(fm.toString() + " IOException: " + ex.getMessage());
      ex.printStackTrace();
    } catch (FileProcessingException ex) {
      logger.logError(fm.toString() + " FileProcessingException: " + ex.getMessage());
      ex.printStackTrace();
    }
  }

  public void run() {
    try {
      TextFileManager tfm = new TextFileManager();

      testManager(tfm);

      logger.logMessage("Finished!");
    } catch (IOException ex) {
      logger.logError("Failed to run the test! IOException: " + ex.getMessage());
      ex.printStackTrace();
    }
  }
}
