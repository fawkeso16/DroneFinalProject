package com.example.Drone_Project;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

@Service
public class DroneMovementService {
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(40);
    private static final double BATTERY_PER_NODE = 0.1;
    private static final long MOVEMENT_DELAY_MS = 75;

    public CompletableFuture<Boolean> moveToPickUpStation(Drone drone, PickupStation pickUpStation, List<Node> pathToPickUp) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
    
        if (pathToPickUp == null || pathToPickUp.isEmpty()) {
            System.err.println("No path found for drone: " + drone.getDroneid());
            drone.setAvailable(DroneStatus.AVAILABLE.getCode());
            future.complete(false);
            return future;
        }
    
        final Iterator<Node> iterator = pathToPickUp.iterator();
    
        ScheduledFuture<?> scheduled = scheduler.scheduleAtFixedRate(() -> {
            if (!iterator.hasNext()) {
                pickUpStation.requestPickup(drone);
                future.complete(true);
                throw new RuntimeException("STOP"); 
            }
    
            Node next = iterator.next();
            drone.moveTo(next.x, next.y);
            drone.setBattery(drone.getBattery() - BATTERY_PER_NODE);
    
            if (drone.getBattery() <= 0) {
                drone.setAvailable(DroneStatus.AVAILABLE.getCode());
                future.complete(false);
                throw new RuntimeException("STOP");
            }
    
        }, 0, MOVEMENT_DELAY_MS, TimeUnit.MILLISECONDS);
    
        future.whenComplete((result, error) -> scheduled.cancel(false));
    
        return future;
    }

    public CompletableFuture<Boolean> moveToDestination(Drone drone, List<Node> pathToDestination) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        if (pathToDestination == null || pathToDestination.isEmpty()) {
            future.complete(false);
            return future;
        }
    
        final Iterator<Node> iterator = pathToDestination.iterator();
    
        ScheduledFuture<?> scheduled = scheduler.scheduleAtFixedRate(() -> {
            if (!iterator.hasNext()) {
                future.complete(true);
                throw new RuntimeException("STOP");
            }
    
            Node next = iterator.next();
            drone.moveTo(next.x, next.y);
            drone.setBattery(drone.getBattery() - BATTERY_PER_NODE);
    
            if (drone.getBattery() <= 0) {
                future.complete(false);
                throw new RuntimeException("STOP");
            }
    
        }, 0, MOVEMENT_DELAY_MS, TimeUnit.MILLISECONDS);
    
        future.whenComplete((res, ex) -> scheduled.cancel(false));
    
        return future;
    }

    public CompletableFuture<Boolean> executeSingleItemDelivery(
            Drone drone, 
            PickupStation pickUpStation, 
            List<Node> pathToPickUp, 
            List<Node> pathToDestination) {
        
        return moveToPickUpStation(drone, pickUpStation, pathToPickUp)
            .thenCompose(pickUpSuccess -> {
                if (!pickUpSuccess) {
                    System.err.println("Drone " + drone.getDroneid() + " failed to reach pick-up station.");
                    return CompletableFuture.completedFuture(false);
                }
                return moveToDestination(drone, pathToDestination);
            })
            .thenApply(destSuccess -> {
                if (!destSuccess) {
                    System.err.println("Drone " + drone.getDroneid() + " failed to reach destination.");
                }
                return destSuccess;
            });
    }

    public CompletableFuture<Boolean> executeDoubleItemDelivery(
            Drone drone,
            PickupStation pickUpStation,
            List<Node> pathToPickUp,
            List<Node> pathToFirstDestination,
            List<Node> pathToSecondDestination,
            Item firstItem,
            Item secondItem) {
        
        System.out.println("[DUAL] Paths => pickup:" + (pathToPickUp == null ? 0 : pathToPickUp.size()) + 
            " firstLeg:" + (pathToFirstDestination == null ? 0 : pathToFirstDestination.size()) + 
            " secondLeg:" + (pathToSecondDestination == null ? 0 : pathToSecondDestination.size()));

        return moveToPickUpStation(drone, pickUpStation, pathToPickUp)
            .thenCompose(pickUpSuccess -> {
                if (!pickUpSuccess) {
                    System.err.println("[DUAL] Drone " + drone.getDroneid() + " failed to reach pick-up station.");
                    return CompletableFuture.completedFuture(false);
                }
                System.out.println("[DUAL] Drone " + drone.getDroneid() + " collected both items at station.");
                return moveToDestination(drone, pathToFirstDestination);
            })
            .thenCompose(firstLegSuccess -> {
                if (!firstLegSuccess) {
                    System.err.println("[DUAL] Drone " + drone.getDroneid() + " failed en route to first drop.");
                    return CompletableFuture.completedFuture(false);
                }
                System.out.println("[DUAL] Drone " + drone.getDroneid() + " delivered FIRST item " + firstItem.getItemId());
                
                if (pathToSecondDestination == null || pathToSecondDestination.isEmpty()) {
                    System.out.println("[DUAL] No second leg path (same destination) treated as success.");
                    return CompletableFuture.completedFuture(true);
                }
                
                Node secStart = pathToSecondDestination.get(0);
                Node secEnd = pathToSecondDestination.get(pathToSecondDestination.size() - 1);
                System.out.println("[DUAL] Second leg start=" + secStart.x + "," + secStart.y + 
                    " end=" + secEnd.x + "," + secEnd.y + " length=" + pathToSecondDestination.size());
                
                CompletableFuture<Void> pause = new CompletableFuture<>();
                scheduler.schedule(() -> {
                    System.out.println("[DUAL] Pause after first drop complete. Beginning second leg...");
                    pause.complete(null);
                }, 1, TimeUnit.SECONDS);
                
                return pause.thenCompose(v -> moveToDestination(drone, pathToSecondDestination))
                    .thenApply(secondLegSuccess -> {
                        if (!secondLegSuccess) {
                            System.err.println("[DUAL] Drone " + drone.getDroneid() + " failed en route to second drop.");
                            return false;
                        }
                        System.out.println("[DUAL] Drone " + drone.getDroneid() + " delivered SECOND item " + secondItem.getItemId());
                        return true;
                    });
            });
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
