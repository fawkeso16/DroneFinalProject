package com.example.Drone_Project;


public enum DroneStatus {
    AVAILABLE("Available"),
    BUSY("Busy"),
    RECHARGING("Recharging"),
    PICKUP("Pickup"),
    QUEUEING("Queueing");


    private final String code;

    DroneStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}