package com.example.Drone_Project;

import java.util.List;

public class PathsUpdatedEvent {
    private final java.util.Map<String, List<Node>> allPaths;

    public PathsUpdatedEvent(java.util.Map<String, List<Node>> allPaths) {
        this.allPaths = allPaths;
    }

    public java.util. Map<String, List<Node>> getAllPaths() {
        return allPaths;
    }
}