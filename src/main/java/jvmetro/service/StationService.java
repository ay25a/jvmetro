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
          System.err.println("Station cannot be loaded:: " + e.getMessage());
        }
      }
    } catch (IOException | FileProcessingException e) {
      System.err.println("Stations will not be loaded:: " + e.getMessage());
    }
  }

  public void saveStations(FileManager fm) throws IOException {
    ArrayList<HashMap<String, String>> parsed = new ArrayList<>();
    stations.forEach(station -> parsed.add(station.serialize()));

    fm.writeData("stations", parsed);
  }

  public Station getStation(String stationName) throws EntryNotFoundException {
    Station station = null;

    for (Station st : stations) {
      if (st.name().equals(stationName))
        station = st;
    }

    if (station == null)
      throw new EntryNotFoundException("Station does not Exist!");

    return station;
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
