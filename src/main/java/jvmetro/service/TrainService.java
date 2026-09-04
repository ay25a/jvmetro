package jvmetro.service;

import java.util.HashMap;
import java.util.ArrayList;
import java.io.IOException;

import jvmetro.model.Train;
import jvmetro.model.DeserializationException;
import jvmetro.repository.FileManager;
import jvmetro.repository.FileProcessingException;

public class TrainService {
  private ArrayList<Train> trains;

  public TrainService(FileManager fm) {
    trains = new ArrayList<>();

    try {
      ArrayList<HashMap<String, String>> parsed = fm.readData("trains");

      for (HashMap<String, String> map : parsed) {
        try {
          trains.add(Train.from(map));
        } catch (DeserializationException ex) {
          System.err.println("Skipping Loading a Train: " + ex.getMessage());
        }
      }
    } catch (IOException | FileProcessingException ex) {
      System.err.println("Trains will be loaded empty; " + ex.getMessage());
    }
  }

  public void saveTrains(FileManager fm) throws IOException {
    ArrayList<HashMap<String, String>> parsed = new ArrayList<>();
    ;

    trains.forEach(train -> parsed.add(train.serialize()));

    fm.writeData("trains", parsed);
  }

  public void addTrain(Train train) {
    for (Train t : trains) {
      if (t.getName().equals(train.getName()))
        throw new DuplicateEntryException("Train cannot be added; Duplicate name found for " + train.getName());
    }

    trains.add(train);
  }

  public Train[] getTrains() {
    return trains.toArray(Train[]::new);
  }
}
