package com.example.Drone_Project;

public class Location {
    public String id;
    public String name;
    public Node node;

    public Location(Node location) {
        this.id = "temp";
        this.name = "temp";
        this.node = location;
    }


   public double getX() {
        return (int)this.node.x;
    }

    public double getY() {
        return (int)this.node.y;
    }

    public Node getNode() {
        return this.node;
    }

    @Override
    public String toString() {
        return "Location{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", node=" + node +
                '}';
    }
}