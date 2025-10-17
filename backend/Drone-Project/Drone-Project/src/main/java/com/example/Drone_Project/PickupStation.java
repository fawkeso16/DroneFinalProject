//PickUpStation Class - we use pickupstations to distribute items to drones, we have a semaphore to limit the number of drones that can pick up items at the same time.



package com.example.Drone_Project;
import java.util.concurrent.Semaphore;

public class PickupStation {
    public String id;
    public String name;
    public Node location;
    private final Semaphore slots = new Semaphore(3);
    private final int maxSlots = 3;

    public PickupStation(String name, Node location) {
        this.id = name + "ID";
        this.name = name;
        this.location = location;
    }

    public void requestPickup(Drone drone) {
        System.out.println("Drone " + drone.getDroneid() + " requesting pickup at " + name);
        try {

            if(!slots.tryAcquire()) {
                drone.setAvailable(DroneStatus.QUEUEING.getCode());
            }      
            else{
                drone.setAvailable(DroneStatus.PICKUP.getCode());
            }
            Thread.sleep(2000); 
            
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Pickup interrupted for drone: " + drone.getDroneid());
        } finally {
            drone.setAvailable(DroneStatus.BUSY.getCode());
            slots.release();
        }
    }

    public double getX() {
        return (int)this.location.x;
    }

    public double getY() {
        return (int)this.location.y;
    }

    public Node getLocation() {
        return this.location;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }   

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAvailableSlots() {
        return slots.availablePermits();
    }

    public int getUsedSlots() {
        return maxSlots - slots.availablePermits();
    }

    public void setLocation(Node location) {
        this.location = location;
    }

    public boolean isAvailable() {
        return slots.availablePermits() > 0;
    }

    public boolean isFull() {
        return slots.availablePermits() <= 0;
    }

    public boolean isEmpty() {
        return slots.availablePermits() >= maxSlots;
    }

    @Override
    public String toString() {
        return "PickUpStation{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", location=" + location +
                ", availableSlots=" + slots.availablePermits() +
                '}';
    }
}