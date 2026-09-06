package jvmetro.service;

import jvmetro.model.Station;
import jvmetro.model.DeserializationException;
import jvmetro.repository.FileManager;
import jvmetro.repository.FileProcessingException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class StationService {
  private final ArrayList<Station> stations;

  public StationService(FileManager fileManager) {
    stations = new ArrayList<>();
    ArrayList<HashMap<String, String>> loaded = new ArrayList<>();

    try {
      loaded = fileManager.readData("stations");
    } catch (IOException | FileProcessingException ex) {
      System.err.println("Failed to load stations from a file: " + ex.getMessage());
      return;
    }

    for (HashMap<String, String> parsed : loaded) {
      try {
        stations.add(Station.from(parsed));

      } catch (DeserializationException ex) {
        System.err.println("A station cannot be loaded: " + ex.getMessage());
      }
    }
  }

  public void saveStations(FileManager fm) throws IOException {
    ArrayList<HashMap<String, String>> parsed = new ArrayList<>();
    stations.forEach(station -> parsed.add(station.serialize()));

    fm.writeData("stations", parsed);
  }

  public Station getStation(String stationName) throws EntryNotFoundException {
    for (Station st : stations) {
      if (st.name().equals(stationName))
        return st;
    }

    throw new EntryNotFoundException("Station does not Exist!");
  }

  public void addStation(Station newStation) throws DuplicateEntryException {
    try {
      getStation(newStation.name());
      throw new DuplicateEntryException("Station already Exists!");
    } catch (EntryNotFoundException ex) {
      stations.add(newStation);
    }
  };

  public Station[] getStations() {
    return stations.toArray(Station[]::new);
  }
}
