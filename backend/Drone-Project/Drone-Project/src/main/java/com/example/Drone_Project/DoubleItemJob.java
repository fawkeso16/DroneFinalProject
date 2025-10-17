package com.example.Drone_Project;

import java.util.List;

public class DoubleItemJob extends SingleItemJob {
    private final Item secondItem;
    private final Location secondDestination;
    private final List<Node> pathFromFirstTargetToSecondTarget;
    

    public DoubleItemJob(String id, Item firstItem, Drone drone, Location firstDestination,
                     Item secondItem, Location secondDestination,
                     List<Node> pathToPickupFirst, List<Node> pathFromPickupToFirstTarget,
                     List<Node> pathFromFirstTargetToSecondTarget) {
        super(id, firstItem, drone, firstDestination, pathToPickupFirst, pathFromPickupToFirstTarget);
        this.secondItem = secondItem;
        this.secondDestination = secondDestination;
        this.pathFromFirstTargetToSecondTarget = pathFromFirstTargetToSecondTarget;
        appendToFullPath(pathFromFirstTargetToSecondTarget);

    }

    public Item getSecondItem() {
        return secondItem;
    }

    public Location getSecondDestination() {
        return secondDestination;
    }

    public List<Node> getPathFromFirstTargetToSecondTarget() {
        return pathFromFirstTargetToSecondTarget;
    }



}
