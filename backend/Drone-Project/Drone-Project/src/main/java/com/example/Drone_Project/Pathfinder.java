package com.example.Drone_Project;

// Pathfinder.java
// basic a* to find shortedt path from start to end, we define 3 variables, - CameFrom, StartToHere and Heuristic
// camefrom is a map of node to nodes use to store the paths
//startToHere is a map of node to double used to store the cost of the path from start to the current node
// heuristic is a map of node to double used to store the estimated cost from the current node to the end node


// 

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;



public class Pathfinder {
    public List<Node> findPath(Grid map, Node start, Node end) {
    java.util.Map<Node, Node> cameFrom = new HashMap<>();
    java.util.Map<Node, Double> startToHere = new HashMap<>();
    java.util.Map<Node, Double> heuristic = new HashMap<>();
    PriorityQueue<Node> openSet = new PriorityQueue<>(
        Comparator.comparingDouble(n -> heuristic.getOrDefault(n, Double.MAX_VALUE))
    );
    startToHere.put(start, 0.0);
    heuristic.put(start, start.distanceTo(end));
    openSet.add(start);

    while (!openSet.isEmpty()) {

        Node current = openSet.poll();
        if (current.equals(end)) {
            return reconstructPath(cameFrom, current);
        }
        for (Node neighbor : current.getNeighbors()) {
           double startToNeighbour = startToHere.getOrDefault(current, Double.MAX_VALUE) + current.distanceTo(neighbor);
           if (startToNeighbour < startToHere.getOrDefault(neighbor, Double.MAX_VALUE)) {
                cameFrom.put(neighbor, current); 
                startToHere.put(neighbor, startToNeighbour); 
                heuristic.put(neighbor, startToNeighbour + neighbor.distanceTo(end));       
                openSet.remove(neighbor);
                openSet.add(neighbor);
            }
        }
    }

    return Collections.emptyList(); 
}


private List<Node> reconstructPath(java.util.Map<Node, Node> cameFrom, Node current) {
    List<Node> path = new LinkedList<>();
    while (current != null) {
        path.add(0, current);
        current = cameFrom.get(current);
    }

    for (int i = 1; i < path.size(); i++) {
        int dx = Math.abs(path.get(i).x - path.get(i - 1).x);
        int dy = Math.abs(path.get(i).y - path.get(i - 1).y);

        if (dx > 1 || dy > 1 || (dx == 0 && dy == 0)) {
            throw new RuntimeException("Invalid move from " + path.get(i - 1) + " to " + path.get(i));
        }
    }

    return path;
}

}

