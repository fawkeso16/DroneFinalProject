package com.example.Drone_Project;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    
    @Bean
    public Grid grid() {
        return new Grid(190, createSquareZone(80, 80, 30)); 
    }


    public Set<Node> createSquareZone(int startX, int startY, int size) {
        Set<Node> zone = new HashSet<>();
        for (int x = startX; x < startX + size; x++) {
            for (int y = startY; y < startY + size; y++) {
                zone.add(new Node(x, y));
            }
        }
        return zone;
    }

    public Set<Node> testNoFlyZone() {
        return createSquareZone(150, 150, 30);
    }

    @Bean
    public LogManager logManager() {
        return new LogManager(100);
    }

    @Bean
    public ConcurrentHashMap<String, Drone> drones() {
        ConcurrentHashMap<String, Drone> drones = new ConcurrentHashMap<>();
        drones.put("DRONE001", new Drone("DRONE001", randomCoord(), randomCoord(), 100));
        drones.put("DRONE002", new Drone("DRONE002", randomCoord(), randomCoord(), 100));
        drones.put("DRONE003", new Drone("DRONE003", randomCoord(), randomCoord(), 100));
        drones.put("DRONE004", new Drone("DRONE004", randomCoord(), randomCoord(), 100));
        drones.put("DRONE005", new Drone("DRONE005", randomCoord(), randomCoord(), 100));
        drones.put("DRONE006", new Drone("DRONE006", randomCoord(), randomCoord(), 100));
        drones.put("DRONE007", new Drone("DRONE007", randomCoord(), randomCoord(), 100));
        drones.put("DRONE008", new Drone("DRONE008", randomCoord(), randomCoord(), 100));
        drones.put("DRONE009", new Drone("DRONE009", randomCoord(), randomCoord(), 100));
        drones.put("DRONE010", new Drone("DRONE010", randomCoord(), randomCoord(), 100));
        drones.put("DRONE011", new Drone("DRONE011", randomCoord(), randomCoord(), 100));
        drones.put("DRONE012", new Drone("DRONE012", randomCoord(), randomCoord(), 100));
        drones.put("DRONE013", new Drone("DRONE013", randomCoord(), randomCoord(), 100));
        drones.put("DRONE014", new Drone("DRONE014", randomCoord(), randomCoord(), 100));
        return drones;
    }


    @Bean
    public ConcurrentHashMap<String, Drone> TestDrones() {
        int DRONE_COUNT = 30;
        ConcurrentHashMap<String, Drone> testDrones = new ConcurrentHashMap<>();
        for (int i = 1; i <= DRONE_COUNT; i++) {
            testDrones.put("DRONE" + String.format("%03d", i),
                    new Drone("DRONE" + String.format("%03d", i),
                            randomCoord(), randomCoord(), 100));
        }
        return testDrones;
    }
    


    private static int randomCoord() {
        return (int) (Math.random() * 188);
    }

    @Bean
    public List<Location> targets(Grid map) {
        List<Location> targets = new ArrayList<>();
        for (int i = 0; i < 80; i++) {
            Node m = map.getRandomNode();
            Node n = new Node(m.x, m.y);
            targets.add(new Location(n));
        }
        return targets;
    }

    @Bean
    public List<Location> TestTargets(Grid map) {
        List<Location> targets = new ArrayList<>();

        int[][] coords = {
            {5, 10}, {12, 34}, {45, 67}, {23, 89}, {78, 56},
            {90, 10}, {66, 32}, {14, 99}, {100, 3}, {7, 7},
            {55, 55}, {33, 77}, {60, 120}, {180, 10}, {170, 170},
            {160, 45}, {150, 150}, {145, 30}, {134, 67}, {122, 88},
            {119, 43}, {111, 60}, {108, 108}, {99, 99}, {88, 88},
            {77, 33}, {65, 65}, {50, 50}, {40, 40}, {30, 30},
            {20, 20}, {10, 10}, {0, 0}, {5, 145}, {187, 0},
            {100, 187}, {187, 187}, {80, 140}, {141, 80}, {10, 180},
            {33, 44}, {44, 33}, {25, 75}, {75, 25}, {123, 123},
            {150, 5}, {5, 150}, {60, 170}, {170, 60}, {99, 187}
        };

        for (int[] coord : coords) {
            Node n = new Node(coord[0], coord[1]);
            targets.add(new Location(n));
        }

        return targets;
    }

    @Bean
    public List<BatteryStation> batteryStations(Grid map) {
        List<BatteryStation> batteryStations = new ArrayList<>();
        for (int i = 0; i < 14; i++) {
            Node m = map.getRandomNode();
            Node n = new Node(m.x, m.y);
            batteryStations.add(new BatteryStation("BatteryStation-" + i, n));
            System.out.println("Created Battery Station: " + n);
        }
        return batteryStations;
    }

    @Bean
    public List<BatteryStation> TestBatteryStations(Grid map) {
        List<BatteryStation> batteryStations = new ArrayList<>();

        int[][] coords = {
            {10, 10}, {30, 40}, {60, 90}, {90, 60},
            {120, 130}, {160, 20}, {170, 175}
        };

        for (int i = 0; i < coords.length; i++) {
            Node n = new Node(coords[i][0], coords[i][1]);
            batteryStations.add(new BatteryStation("BatteryStation-" + i, n));
            System.out.println("Created Battery Station: " + n);
        }

        return batteryStations;
    }

    @Bean
    public List<PickupStation> pickUpStations(Grid map) {
        List<PickupStation> pickUpStations = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Node m = map.getRandomNode();
            Node n = new Node(m.x, m.y);
            pickUpStations.add(new PickupStation("PickUpStation-" + i, n));
            // System.out.println("Created Pick Up Station: " + n);
        }
        return pickUpStations;
    }

    @Bean
    public List<PickupStation> TestPickUpStations(Grid map) {
        List<PickupStation> pickUpStations = new ArrayList<>();

        int[][] coords = {
            {25, 40}, {66, 130}, {145, 145}, {140, 60}
        };

        for (int i = 0; i < coords.length; i++) {
            Node n = new Node(coords[i][0], coords[i][1]);
            pickUpStations.add(new PickupStation("PickUpStation-" + i, n));
            // System.out.println("Created Pick Up Station: " + n);
        }

        return pickUpStations;
    }

}