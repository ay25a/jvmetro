package jvmetro.service;

import java.util.HashMap;
import java.util.ArrayList;
import java.io.IOException;

import jvmetro.model.Train;
import jvmetro.model.DeserializationException;
import jvmetro.repository.FileManager;
import jvmetro.repository.FileProcessingException;

public class TrainService {
  private final ArrayList<Train> trains;

  public TrainService(FileManager fileManager) {
    trains = new ArrayList<>();
    ArrayList<HashMap<String, String>> loaded = new ArrayList<>();

    try {
      loaded = fileManager.readData("trains");
    } catch (IOException | FileProcessingException ex) {
      System.err.println("Failed to load trains from a file: " + ex.getMessage());
      return;
    }

    for (HashMap<String, String> parsed : loaded) {
      try {
        trains.add(Train.from(parsed));

      } catch (DeserializationException ex) {
        System.err.println("A train cannot be loaded: " + ex.getMessage());
      }
    }
  }

  public void saveTrains(FileManager fm) throws IOException {
    ArrayList<HashMap<String, String>> parsed = new ArrayList<>();
    trains.forEach(train -> parsed.add(train.serialize()));

    fm.writeData("trains", parsed);
  }

  private Train getTrain(String trainName) throws EntryNotFoundException {
    for (Train tr : trains) {
      if (tr.name().equals(trainName))
        return tr;
    }

    throw new EntryNotFoundException("Train doesn't Exist!");
  }

  public void addTrain(Train train) throws DuplicateEntryException {
    try {
      getTrain(train.name());
      throw new DuplicateEntryException("Train already Exists!");
    } catch (EntryNotFoundException ex) {
      trains.add(train);
    }
  }

  public Train[] getTrains() {
    return trains.toArray(Train[]::new);
  }
}
