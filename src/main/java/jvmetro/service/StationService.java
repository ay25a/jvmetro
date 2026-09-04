package jvmetro.service;

import jvmetro.model.Station;
import jvmetro.model.DeserializationException;
import jvmetro.repository.FileManager;
import jvmetro.repository.FileProcessingException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class StationService {

  private ArrayList<Station> stations;

  public StationService(FileManager fm) {
    stations = new ArrayList<>();

    try {
      ArrayList<HashMap<String, String>> parsed = fm.readData("stations");

      for (HashMap<String, String> map : parsed) {
        try {
          stations.add(Station.from(map));
        } catch (DeserializationException e) {
          System.err.println("Skipping loading a station: " + e.getMessage());
        }
      }
    } catch (IOException | FileProcessingException e) {
      System.err.println("Station will be loaded empty: " + e.getMessage());
    }
  }

  public void saveStations(FileManager fm) throws IOException {
    ArrayList<HashMap<String, String>> parsed = new ArrayList<>();

    stations.forEach(station -> parsed.add(station.serialized()));

    fm.writeData("stations", parsed);
  };

  public void addStation(Station station) throws DuplicateEntryException {
    for (Station s : stations) {
      if (s.getName().equals(station.getName())) {
        throw new DuplicateEntryException("Station cannot be added; Duplicate name found for " + station.getName());
      }
    }

    stations.add(station);
  };

  public Station[] getStations() {
    return stations.toArray(Station[]::new);
  }
}
