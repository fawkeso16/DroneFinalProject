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
        this.position = new Node(x, y);
        this.battery = battery;
        this.available = DroneStatus.AVAILABLE.getCode();
        this.destination = null; 
    }

    public void moveTo(int newX, int newY) {
        lock.lock();
        try {
            this.position.setX(newX);
            this.position.setY(newY);
        } finally {
            lock.unlock();
        }
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
        lock.lock();
        try {
            return position.getX();
        } finally {
            lock.unlock();
        }
    }
    public void setX(int x) {
        lock.lock();
        try {
            this.position.setX(x);
        } finally {
            lock.unlock();
        }
    }
    public int getY() {
        lock.lock();
        try {
            return position.getY();
        } finally {
            lock.unlock();
        }
    }
    public void setY(int y) {
        lock.lock();
        try {
            this.position.setY(y);
        } finally {
            lock.unlock();
        }
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
                ", x=" + position.getX() +
                ", y=" + position.getY() +
                ", battery=" + battery +
                ", available=" + available +
                '}';
    }


}
