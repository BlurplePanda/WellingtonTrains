// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2023T2, Assignment 2
 * Name:
 * Username:
 * ID:
 */

import ecs100.*;

import java.util.*;
import java.util.Map.Entry;
import java.io.*;
import java.nio.file.*;

/**
 * WellingtonTrains
 * A program to answer queries about Wellington train lines and timetables for
 * the train services on those train lines.
 * <p>
 * See the assignment page for a description of the program and what you have to do.
 */

public class WellingtonTrains {
    //Fields to store the collections of Stations and Lines
    private Map<String, Station> stations = new HashMap<>();
    private Map<String, TrainLine> trainLines = new HashMap<>();

    // Fields for the suggested GUI.
    private String stationName;        // station to get info about, or to start journey from
    private String lineName;           // train line to get info about.
    private String destinationName;
    private int startTime = 0;         // time for enquiring about

    private static boolean loadedData = false;  // used to ensure that the program is called from main.

    /**
     * main method:  load the data and set up the user interface
     */
    public static void main(String[] args) {
        WellingtonTrains wel = new WellingtonTrains();
        wel.loadData();   // load all the data
        wel.setupGUI();   // set up the interface
    }

    /**
     * Load data files
     */
    public void loadData() {
        loadStationData();
        UI.println("Loaded Stations");
        loadTrainLineData();
        UI.println("Loaded Train Lines");
        // The following is only needed for the Completion and Challenge
        loadTrainServicesData();
        UI.println("Loaded Train Services");
        loadedData = true;
    }

    /**
     * User interface has buttons for the queries and text fields to enter stations and train line
     * You will need to implement the methods here.
     */
    public void setupGUI() {
        UI.addButton("All Stations", this::listAllStations);
        UI.addButton("Stations by name", this::listStationsByName);
        UI.addButton("All Lines", this::listAllTrainLines);
        UI.addTextField("Station", (String name) -> {
            this.stationName = name;
        });
        UI.addTextField("Train Line", (String name) -> {
            this.lineName = name;
        });
        UI.addTextField("Destination", (String name) -> {
            this.destinationName = name;
        });
        UI.addTextField("Time (24hr)", (String time) ->
        {
            try {
                this.startTime = Integer.parseInt(time);
            } catch (Exception e) {
                UI.println("Enter four digits");
            }
        });
        UI.addButton("Lines of Station", () -> {
            listLinesOfStation(this.stationName);
        });
        UI.addButton("Stations on Line", () -> {
            listStationsOnLine(this.lineName);
        });
        UI.addButton("Stations connected?", () -> {
            printConnected(this.stationName, this.destinationName);
        });
        UI.addButton("Next Services", () -> {
            findNextServices(this.stationName, this.startTime);
        });
        UI.addButton("Find Trip", () -> {
            findTrip(this.stationName, this.destinationName, this.startTime);
        });

        UI.addButton("Quit", UI::quit);
        UI.setMouseListener(this::doMouse);

        UI.setWindowSize(900, 400);
        UI.setDivider(0.2);
        // this is just to remind you to start the program using main!
        if (!loadedData) {
            UI.setFontSize(36);
            UI.drawString("Start the program from main", 2, 36);
            UI.drawString("in order to load the data", 2, 80);
            UI.sleep(2000);
            UI.quit();
        } else {
            UI.drawImage("data/geographic-map.png", 0, 0);
            UI.drawString("Click to list closest stations", 2, 12);
        }
    }

    public void doMouse(String action, double x, double y) {
        if (action.equals("released")) {
            /*# YOUR CODE HERE */

        }
    }

    // Methods for loading data and answering queries

    public void loadStationData() {
        try {
            Scanner scan = new Scanner(Path.of("data/stations.data"));
            while (scan.hasNext()) {
                String name = scan.next();
                int zone = scan.nextInt();
                double x = scan.nextDouble();
                double y = scan.nextDouble();
                scan.nextLine();
                Station station = new Station(name, zone, x, y);
                stations.put(name, station);
            }
        } catch (IOException e) {
            UI.println("Station file reading failed :(");
        }
    }

    public void loadTrainLineData() {
        try {
            Scanner scan = new Scanner(Path.of("data/train-lines.data"));
            while (scan.hasNext()) {
                String name = scan.next();
                scan.nextLine();
                TrainLine line = new TrainLine(name);
                trainLines.put(name, line);
                Scanner sc = new Scanner(Path.of("data/" + name + "-stations.data"));
                while (sc.hasNext()) {
                    String stnName = sc.next();
                    sc.nextLine();
                    Station station = stations.get(stnName);
                    line.addStation(station);
                    station.addTrainLine(line);
                }
            }
        } catch (IOException e) {
            UI.println("Train line file reading failed :(");
        }
    }

    public void listAllStations() {
        UI.clearText();
        UI.println("All stations in region:\n----------");
        for (Station stn : stations.values()) {
            UI.println(stn.toString());
        }
    }

    public void listStationsByName() {
        ArrayList<String> stnNames = new ArrayList<>();
        for (Station stn : stations.values()) {
            stnNames.add(stn.getName());
        }
        UI.clearText();
        UI.println("All stations by name:\n----------");
        Collections.sort(stnNames);
        for (String stnName : stnNames) {
            UI.println(stations.get(stnName).toString());
        }
    }

    public void listAllTrainLines() {
        UI.clearText();
        UI.println("All train lines in region:\n----------");
        for (TrainLine line : trainLines.values()) {
            UI.println(line.toString());
        }
    }

    public void listLinesOfStation(String stnName) {
        UI.clearText();
        Station stn = stations.get(stnName);
        if (stn != null) {
            for (TrainLine line : stn.getTrainLines()) {
                UI.println(line.toString());
            }
        }
        else {
            UI.println("Please enter a valid station name.");
        }
    }

    public void listStationsOnLine(String lineName) {
        UI.clearText();
        TrainLine line = trainLines.get(lineName);
        if (line != null) {
            for (Station station : line.getStations()) {
                UI.println(station.toString());
            }
        } else {
            UI.println("Please enter a valid train line name.");
        }
    }

    public List<TrainLine> checkConnected(String stationName, String destinationName) {
        List<TrainLine> results = new ArrayList<>();
        Station station = stations.get(stationName);
        Station destination = stations.get(destinationName);
        for (TrainLine line : trainLines.values()) {
            if (line.getStations().contains(station) && line.getStations().contains(destination)
                    && line.getStations().indexOf(station) < line.getStations().indexOf(destination)) {
                results.add(line);
            }
        }
        return results;
    }

    public void printConnected(String stationName, String destinationName) {
        UI.clearText();
        if (stationName != null && destinationName != null) {
            List<TrainLine> lines = checkConnected(stationName, destinationName);
            if (lines.isEmpty()) {
                UI.printf("No train lines found from %s to %s.", stationName, destinationName);
            } else {
                for (TrainLine line : lines) {
                    UI.println(line.toString());
                }
            }
        }
        else {
            UI.println("Please enter valid station/destination names.");
        }
    }

    //completion

    public void loadTrainServicesData() {
        try {
            Scanner lineNameScan = new Scanner(Path.of("data/train-lines.data"));
            while (lineNameScan.hasNext()) {
                String lineName = lineNameScan.next();
                TrainLine line = trainLines.get(lineName);
                List<String> timesScan = Files.readAllLines(Path.of("data/" + lineName + "-services.data"));
                for (String times : timesScan) {
                    TrainService service = new TrainService(line);
                    line.addTrainService(service);
                    Scanner timeScan = new Scanner(times);
                    while (timeScan.hasNext()) {
                        int time = timeScan.nextInt();
                        service.addTime(time);
                    }
                }
            }
        } catch (IOException e) {
            UI.println("Train service file reading failed :(");
        }
    }

    public void findNextServices(String stationName, int startTime) {
        UI.clearText();
        Station station = stations.get(stationName);
        if (station != null) {
            for (TrainLine line : station.getTrainLines()) {
                int departTime = stnNextTime(station, line, startTime);
                if (departTime != -1) {
                    UI.println(line.getName() + " at " + departTime);
                }
            }
        }
        else {
            UI.println("Please enter a valid station name.");
        }
    }

    public void findTrip(String stationName, String destinationName, int startTime) {
        UI.clearText();
        Station station = stations.get(stationName);
        Station destination = stations.get(destinationName);
        if (station != null && destination != null) {
            int zones = Math.abs(station.getZone() - destination.getZone()) + 1;
            for (TrainLine line : checkConnected(stationName, destinationName)) {
                int depart = stnNextTime(station, line, startTime);
                int arrive = stnNextTime(destination, line, startTime);
                if (depart != -1 && arrive != -1) {
                    UI.println(line.getName() + " line, departing at " + stationName + " at " + depart
                            + " and arriving at " + destinationName + " at " + arrive
                            + ", passing through " + zones + " fare zones.");
                }
            }
        }
        else {
            UI.println("Please enter a valid station/destination name.");
        }
    }

    private int stnNextTime(Station station, TrainLine line, int startTime) {
        int stnNum = line.getStations().indexOf(station);
        for (TrainService service : line.getTrainServices()) {
            int stnTime = service.getTimes().get(stnNum);
            if (stnTime > startTime) {
                return stnTime;
            }
        }
        return -1;
    }
}