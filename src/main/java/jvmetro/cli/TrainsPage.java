package jvmetro.cli;

import java.util.Objects;
import java.util.Map;
import java.util.AbstractMap;
import java.util.ArrayList;

import jvmetro.page.Page;
import jvmetro.page.PageResult;

import jvmetro.service.TrainService;
import jvmetro.service.DuplicateEntryException;
import jvmetro.model.Train;

// Handles all train-related functions
public class TrainsPage implements Page {
  MetroApp.AppUtils utils;
  TrainService trainService;

  ArrayList<Map.Entry<String, Runnable>> menu;

  public TrainsPage(MetroApp.AppUtils utils, TrainService trainService) {
    this.utils = Objects.requireNonNull(utils);
    this.trainService = Objects.requireNonNull(trainService);

    menu = new ArrayList<>();
    menu.add(new AbstractMap.SimpleEntry<>("Add Train", this::addTrain));
  }

  @Override
  public PageResult show() {
    printTrains();

    boolean isStay = utils.askMenu(menu);
    if (isStay)
      return new PageResult.Stay();

    return new PageResult.Back();
  }

  private void addTrain() {
    String name = utils.askInput("Train Name: ");
    String capacity = utils.askInput("Capacity: ");

    try {
      Train train = new Train(name, Integer.parseInt(capacity));
      trainService.addTrain(train);
      System.out.println("Train Added Successfully");
    } catch (NumberFormatException ex) {
      System.err.println("Invalid Train Capacity");
    } catch (DuplicateEntryException | IllegalArgumentException ex) {
      System.err.println(ex.getMessage());
    }
  }

  private void printTrains() {
    Train[] trains = trainService.getTrains();

    String border = "*".repeat(40);
    System.out.println(border);
    System.out.printf("* %-20s * %-13s *\n", "Train Name", "Capacity");
    System.out.println(border);

    if (trains.length == 0)
      System.out.printf("%-10s No Trains Found\n", " ");

    for (Train tr : trains)
      System.out.printf("* %-20s * %-13s *\n", tr.name(), String.valueOf(tr.capacity()));
    System.out.println(border);
  }
}
