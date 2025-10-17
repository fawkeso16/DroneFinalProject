package com.example.Drone_Project;

import java.util.concurrent.locks.ReentrantLock;

public class Drone {

    public final ReentrantLock lock = new ReentrantLock();

    private String droneid;
    private Node position;
    private double battery;
    private String available;
    private Location destination;

    public Drone(String droneid, int x, int y, double battery) {
        this.droneid = droneid;
        this.position.x = x;
        this.position.y = y;
        this.battery = battery;
        this.available = DroneStatus.AVAILABLE.getCode();
        this.destination = null; 
    }

    public void moveTo(int newX, int newY) {
        this.position.x = newX;
        this.position.y = newY;
    }

    public void setDestination(Location destination) {
        this.destination = destination;
    }

    public double calculateBatteryUsage(double distance){
        int batteryUsage = (int)(distance * 10); 
        return batteryUsage / 100.0;
    }

    public Location getDestination() {
        return destination;
    }

    public String getDroneid() {
        return droneid;
    }
    public void setDroneid(String droneid) {
        this.droneid = droneid;
    }
    public int getX() {
        return position.x;
    }
    public void setX(int x) {
        this.position.x = x;
    }
    public int getY() {
        return position.y;
    }
    public void setY(int y) {
        this.position.y = y;
    }
    public double getBattery() {
        return battery;
    }
    public void setBattery(double battery) {
        this.battery = battery;
    }
    public String getAvailable() {
        return available;
    }
    public void setAvailable(String available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return "Drone{" +
                "droneid='" + droneid + '\'' +
                ", x=" + position.x +
                ", y=" + position.y +
                ", battery=" + battery +
                ", available=" + available +
                '}';
    }


}
