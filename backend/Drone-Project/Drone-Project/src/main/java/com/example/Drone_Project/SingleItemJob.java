// Job Class - this class represents a job for a drone, including the drone assigned, the destination, the paths to pick up and destination, and the item being delivered. It provides methods to access and modify these properties.
package com.example.Drone_Project;

import java.util.List;

public class SingleItemJob {
    
    private String id;
    private Drone drone;
    private Location Destination;
    private List<Node> PathToPickUp;
    private List<Node> PathToDestination;
    public List<Node> fullPath;
    public Item item;   
    public long timeStarted;
    public long timeCompleted; 
    public long duration;


    public SingleItemJob(String id, Item item, Drone drone, Location destination, List<Node> pathToPickUp, List<Node> pathToDestination) {
            this.id = id;
            this.drone = drone;
            this.Destination = destination;
            this.PathToPickUp = pathToPickUp;
            this.item = item;
            this.PathToDestination = pathToDestination;
            this.fullPath = new java.util.ArrayList<>();
            this.fullPath.addAll(pathToPickUp);
            this.fullPath.addAll(pathToDestination);
            this.timeStarted = System.currentTimeMillis();
    }


    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public Item getItem() {
        return item;
    }
    public void setItem(Item item) {
        this.item = item;
    }

    public Drone getDrone() {
        return drone;
    }
    public void setDrone(Drone drone) {
        this.drone = drone;
    }
    public Location getDestination() {
        return Destination;
    }
    public void setDestination(Location destination) {
        this.Destination = destination;
    }
    public List<Node> getPathToPickUp() {
        return PathToPickUp;
    }
    public void setPathToPickUp(List<Node> pathToPickUp) {
        this.PathToPickUp = pathToPickUp;
    }
    public List<Node> getPathToDestination() {
        return PathToDestination;
    }
    public void setPathToDestination(List<Node> pathToDestination) {
        this.PathToDestination = pathToDestination;
    }
    public List<Node> getFullPath() {
        return fullPath;
    }
    public void setFullPath(List<Node> fullPath) {
        this.fullPath = fullPath;
    }

    public void appendToFullPath(List<Node> extra) {
        if (extra == null || extra.isEmpty()) return;
        this.fullPath.addAll(extra);
    }

    public long getTimeCompleted() {
        return timeCompleted;
    }
    public void setTimeCompleted(long timeCompleted) {
        this.timeCompleted = timeCompleted;
    }
    public long getTimeStarted() {
        return timeStarted;
    }
    public void setTimeStarted(long timeStarted) {
        this.timeStarted = timeStarted;
    }
    public long getJobDuration() {
        return timeCompleted - timeStarted;
    }
    public void setDuration() {
        this.duration = timeCompleted - timeStarted;
    }
    @Override
    public String toString() {
        return "Job{" +
                "id='" + id + '\'' +
                ", drone=" + drone +
                // ", Destination=" + Destination +
                ". Item=" + item + "/n\n" +
                // ", PathToPickUp=" + PathToPickUp +
                // ", PathToDestination=" + PathToDestination +
                ", TimeCompleted=" + timeCompleted +
                '}';
    }

    
}
