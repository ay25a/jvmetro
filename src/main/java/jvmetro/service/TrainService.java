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
          System.err.println("Train cannot be loaded: " + ex.getMessage());
        }
      }
    } catch (IOException | FileProcessingException ex) {
      System.err.println("Trains will not be loaded: " + ex.getMessage());
    }
  }

  public void saveTrains(FileManager fm) throws IOException {
    ArrayList<HashMap<String, String>> parsed = new ArrayList<>();
    trains.forEach(train -> parsed.add(train.serialize()));

    fm.writeData("trains", parsed);
  }

  public Train getTrain(String trainName) throws EntryNotFoundException {
    Train train = null;
    for(Train tr: trains){
      if(tr.name().equals(trainName))
        train = tr;
    }

    if(train == null)
      throw new EntryNotFoundException("Train doesn't Exist!");

    return train;
  }

  public void addTrain(Train train) throws DuplicateEntryException {
    try{
      getTrain(train.name());
      throw new DuplicateEntryException("Train already Exists!");
    }catch(EntryNotFoundException ex){
      trains.add(train);
    }
  }

  public Train[] getTrains() {
    return trains.toArray(Train[]::new);
  }
}
