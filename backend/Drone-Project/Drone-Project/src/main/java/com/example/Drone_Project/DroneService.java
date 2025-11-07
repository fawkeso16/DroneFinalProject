// DroneService Class - this class manages drone operations, including job creation, pathfinding, and movement. It interacts with various components like jobs, map, battery stations, and pick-up stations to facilitate drone tasks.

package com.example.Drone_Project;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static java.util.concurrent.Executors.newFixedThreadPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class DroneService {
    private final ConcurrentHashMap<String, Drone> drones;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    public final ReentrantLock lock = new ReentrantLock();

    private final JobOverviewService jobs;
    private final Grid map;
    private final List<Location> targets;   
    private final List<BatteryStation> batteryStations;
    private final List<PickupStation> pickUpStations;
    private final ExecutorService pathfindingExecutor = newFixedThreadPool(40);
    private final ExecutorService moveExecutor = Executors.newFixedThreadPool(40);
    private final Random random = new Random();
    private final LogManager logManager;
    private final Pathfinder pathfinder = new Pathfinder();
    private final DroneMovementService movementService;


    @Autowired
    public DroneService(ConcurrentHashMap<String, Drone> TestDrones, JobOverviewService jobs, Grid map,
                        List<Location> TestTargets, List<BatteryStation> TestBatteryStations,
                        List<PickupStation> TestPickUpStations, LogManager logManager,
                        DroneMovementService movementService) {
        this.drones = TestDrones;
        this.jobs = jobs;
        this.map = map;
        this.targets = TestTargets;
        this.batteryStations = TestBatteryStations;
        this.logManager = logManager;
        this.pickUpStations = TestPickUpStations;
        this.movementService = movementService;
    }

    public List<Drone> getAllDrones() {
        return new ArrayList<>(drones.values());
    }

    // Get all paths for drones that are currently busy
    // Structure = map of drone IDs to their paths
    public java.util.Map<String, List<Node>> getCurrentPaths() {
        java.util.Map<String, List<Node>> paths = new HashMap<>();
    
        for (java.util.Map.Entry<String, Drone> entry : drones.entrySet()) {
            Drone drone = entry.getValue();
            if (drone.getAvailable().equals(DroneStatus.BUSY.getCode())) {
                String jobId = drone.getDroneid() + "-JOB";
                SingleItemJob job = jobs.getJobById(jobId);
                if (job != null && job.getFullPath() != null) {
                    paths.put(drone.getDroneid(), new ArrayList<>(job.getFullPath()));
                }
            }
        }
        return paths;
    }
    

    public void moveAllJobs() {
        jobs.getAllJobs();
    }

    //Take in drone and item, create a job for the drone to pick up the item from the pick-up station and deliver it to a random target.
    //if the drone is not available, it will not create a job for it. (send to battery station or ignore)
    public synchronized CompletableFuture<Boolean> createJob(Drone drone, Item item, Location destinationn) {
        CompletableFuture<Boolean> jobFuture = new CompletableFuture<>();
        
        if (!drone.getAvailable().equals(DroneStatus.AVAILABLE.getCode())) {
            jobFuture.complete(false);
            System.out.println("FAILURE: Drone " + drone.getDroneid() + " is not available for item " + item.getItemId());
            return jobFuture;
        }

        Item.CurrentItemsAvailable.removeItem(item);
        Location destination = destinationn;
        final PickupStation pickUpStation = item.getPickUpStation();
        final Node pickUpLocation = pickUpStation.getLocation();

        if (destination == null) {
            System.err.println("No destination set for drone: " + drone.getDroneid());
            jobFuture.complete(false);
            return jobFuture;
        }
        
        Node start = new Node((int) drone.getX(), (int) drone.getY());
        Node end = map.getNode(destination.getNode().x, destination.getNode().y);

        // Battery validation
        if (drone.calculateBatteryUsage(start.distanceTo(pickUpLocation) + pickUpLocation.distanceTo(end)) > drone.getBattery()) {
            drone.setAvailable(DroneStatus.RECHARGING.getCode());
            System.out.println("FAILURE: Drone " + drone.getDroneid() + " does not have enough battery for item " + item.getItemId());

            logManager.addLog(new String[]{
                getHourWithAmPm() + " ",
                "Job Failed To Assign",
                "Failed job - No Battery: " + drone.getDroneid()
            });
            
            sendToNearestBatteryStation(drone);
            Item.CurrentItemsAvailable.addItem(item);
            jobs.removeJob(drone.getDroneid() + "-JOB");
            jobFuture.complete(false);
            return jobFuture;
        }

        String jobid = drone.getDroneid() + "-JOB";
        drone.setAvailable(DroneStatus.BUSY.getCode());
        
        logManager.addLog(new String[]{
            getHourWithAmPm() + " ",
            "Job Assigned",
            "Created job - Assigned Drone: " + drone.getDroneid() + " to Item: " + item.getItemName()
        });

        pathfindingExecutor.submit(() -> {
            try {
                List<Node> pathToPickUp = pathfinder.findPath(map, map.getNode(start.x, start.y), map.getNode(pickUpLocation.x, pickUpLocation.y));
                List<Node> pathFromPickUpToDestination = pathfinder.findPath(map, map.getNode(pickUpLocation.x, pickUpLocation.y), end);

                if (pathToPickUp == null || pathToPickUp.isEmpty()) {
                    System.err.println("No path found for drone: " + drone.getDroneid());
                    drone.setAvailable(DroneStatus.AVAILABLE.getCode());
                    jobFuture.complete(false);
                    return;
                }

                SingleItemJob job = new SingleItemJob(jobid, item, drone, destination, pathToPickUp, pathFromPickUpToDestination);
                jobs.addJob(job);
                Item.CurrentItemsAvailable.removeItem(item);

                try {
                    eventPublisher.publishEvent(new PathsUpdatedEvent(getCurrentPaths()));
                } catch (Exception e) {
                    System.err.println("WebSocket broadcast failed: " + e.getMessage());
                }
            
                double usage = drone.calculateBatteryUsage(start.distanceTo(pickUpLocation) + pickUpLocation.distanceTo(end));
    
                // Execute movement using DroneMovementService
                moveExecutor.submit(() -> {
                    movementService.executeSingleItemDelivery(drone, pickUpStation, pathToPickUp, pathFromPickUpToDestination)
                        .thenAccept(result -> handleSingleJobCompletion(result, drone, jobid, item, job, usage, jobFuture))
                        .exceptionally(ex -> {
                            System.err.println("Exception during moveToTarget for drone " + drone.getDroneid() + ": " + ex.getMessage());
                            drone.setAvailable(DroneStatus.AVAILABLE.getCode());
                            jobs.removeJob(jobid);
                            jobFuture.complete(false);
                            return null;
                        });
                });

            } catch (Exception e) {
                System.err.println("Pathfinding error for drone " + drone.getDroneid() + ": " + e.getMessage());
                drone.setAvailable(DroneStatus.AVAILABLE.getCode());
                jobs.removeJob(jobid);
                jobFuture.completeExceptionally(e);
            }
        });

        return jobFuture;
    }

    private void handleSingleJobCompletion(boolean result, Drone drone, String jobid, 
                                          Item item, SingleItemJob job, double usage, 
                                          CompletableFuture<Boolean> jobFuture) {
        if (result) {
            drone.setAvailable(DroneStatus.AVAILABLE.getCode());

            logManager.addLog(new String[]{
                getHourWithAmPm(),
                "Job Completed ",
                "Item Delivered: " + item.getItemId() + " by Drone: " + drone.getDroneid()
            });

            jobs.removeJob(jobid);
            jobs.addToTotalBatteryUsage((int) usage);
            job.setTimeStarted(item.addedTo);
            job.setTimeCompleted(System.currentTimeMillis());
            job.setDuration();
            jobs.addToJobStack(job);

            if (item.priority) {
                jobs.allPriorityJobTimes.add(job.getJobDuration());
                System.out.println("Priority job for " + item.getItemId() + " took " + job.getJobDuration() + " milliseconds");
            } else {
                jobs.allNormalJobTimes.add(job.getJobDuration());
                System.out.println("Normal job for " + item.getItemId() + " took " + job.getJobDuration() + " milliseconds");
            }

            jobFuture.complete(true);
        } else {
            logManager.addLog(new String[]{
                getHourWithAmPm(),
                "Job Failed ",
                "Failed job: " + jobid
            });

            drone.setAvailable(DroneStatus.AVAILABLE.getCode());
            jobs.removeJob(jobid);
            System.out.println("FAILURE: Drone " + drone.getDroneid() + " failed to deliver item " + item.getItemId());
            jobFuture.complete(false);
        }
    }


    public synchronized CompletableFuture<Boolean> createJobDouble(Drone drone, Item item, Location destination, Item item2, Location destination2) {
        CompletableFuture<Boolean> jobFuture = new CompletableFuture<>();
        
        if (!drone.getAvailable().equals(DroneStatus.AVAILABLE.getCode())) {
            jobFuture.complete(false);
            System.out.println("FAILURE: Drone " + drone.getDroneid() + " is not available for item " + item.getItemId());
            return jobFuture;
        }

        Item.CurrentItemsAvailable.removeItem(item);
        Item.CurrentItemsAvailable.removeItem(item2);

        final PickupStation pickUpStation = item.getPickUpStation();
        final Node pickUpLocation = pickUpStation.getLocation();

        if (pickUpLocation == null || destination == null || destination2 == null) {
            System.err.println("No destination(s) set for drone: " + drone.getDroneid());
            jobFuture.complete(false);
            return jobFuture;
        }

        Node start = new Node((int) drone.getX(), (int) drone.getY());
        Node firstTarget = map.getNode(destination.getNode().x, destination.getNode().y);
        Node secondTarget = map.getNode(destination2.getNode().x, destination2.getNode().y);

        double distFirst = pickUpLocation.distanceTo(firstTarget);
        double distSecond = pickUpLocation.distanceTo(secondTarget);
        
        if (distSecond < distFirst) {
            Location tmpLoc = destination;
            destination = destination2;
            destination2 = tmpLoc;
            Node tmpNode = firstTarget;
            firstTarget = secondTarget;
            secondTarget = tmpNode;
            Item tmpItem = item;
            item = item2;
            item2 = tmpItem;
        }
        
        final Location fDestinationOne = destination;
        final Location fDestinationTwo = destination2;
        final Item fItem1 = item;
        final Item fItem2 = item2;
        final Node fFirstTarget = firstTarget;
        final Node fSecondTarget = secondTarget;

        double totalDistance = start.distanceTo(pickUpLocation) + 
                              pickUpLocation.distanceTo(firstTarget) + 
                              firstTarget.distanceTo(secondTarget);
        
        if (drone.calculateBatteryUsage(totalDistance) > drone.getBattery()) {
            drone.setAvailable(DroneStatus.RECHARGING.getCode());
            System.out.println("FAILURE: Drone " + drone.getDroneid() + " does not have enough battery for dual delivery");

            logManager.addLog(new String[]{
                getHourWithAmPm() + " ",
                "Job Failed To Assign",
                "Failed job - No Battery: " + drone.getDroneid()
            });
            
            sendToNearestBatteryStation(drone);
            Item.CurrentItemsAvailable.addItem(fItem1);
            Item.CurrentItemsAvailable.addItem(fItem2);
            jobs.removeJob(drone.getDroneid() + "-JOB");
            jobFuture.complete(false);
            return jobFuture;
        }

        // Job 
        String jobid = drone.getDroneid() + "-JOB";
        drone.setAvailable(DroneStatus.BUSY.getCode());
        
        logManager.addLog(new String[]{
            getHourWithAmPm() + " ",
            "Dual Job Assigned",
            "Assigned Drone: " + drone.getDroneid() + " Items: [First: " + fItem1.getItemName() + ", Second: " + fItem2.getItemName() + "]"
        });

        pathfindingExecutor.submit(() -> {
            try {
                List<Node> pathToPickUp = pathfinder.findPath(map, map.getNode(start.x, start.y), map.getNode(pickUpLocation.x, pickUpLocation.y));
                List<Node> pathToFirstTarget = pathfinder.findPath(map, map.getNode(pickUpLocation.x, pickUpLocation.y), fFirstTarget);
                List<Node> pathToSecondTarget = pathfinder.findPath(map, map.getNode(fFirstTarget.x, fFirstTarget.y), fSecondTarget);

                if (pathToPickUp == null || pathToPickUp.isEmpty()) {
                    System.err.println("No path found for drone: " + drone.getDroneid());
                    drone.setAvailable(DroneStatus.AVAILABLE.getCode());
                    jobFuture.complete(false);
                    return;
                }

                DoubleItemJob job = new DoubleItemJob(jobid, fItem1, drone, fDestinationOne, fItem2, fDestinationTwo, 
                                                      pathToPickUp, pathToFirstTarget, pathToSecondTarget);
                jobs.addDoubleJob(job);

                try {
                    eventPublisher.publishEvent(new PathsUpdatedEvent(getCurrentPaths()));
                } catch (Exception e) {
                    System.err.println("WebSocket broadcast failed: " + e.getMessage());
                }

                double usage = drone.calculateBatteryUsage(totalDistance);

                moveExecutor.submit(() -> {
                    movementService.executeDoubleItemDelivery(drone, pickUpStation, pathToPickUp, 
                                                             pathToFirstTarget, pathToSecondTarget, fItem1, fItem2)
                        .thenAccept(result -> handleDoubleJobCompletion(result, drone, jobid, fItem1, fItem2, job, usage, jobFuture))
                        .exceptionally(ex -> {
                            System.err.println("Exception during dual delivery for drone " + drone.getDroneid() + ": " + ex.getMessage());
                            drone.setAvailable(DroneStatus.AVAILABLE.getCode());
                            jobs.removeJob(jobid);
                            jobFuture.complete(false);
                            return null;
                        });
                });

            } catch (Exception e) {
                System.err.println("Dual pathfinding error for drone " + drone.getDroneid() + ": " + e.getMessage());
                drone.setAvailable(DroneStatus.AVAILABLE.getCode());
                jobs.removeJob(jobid);
                jobFuture.completeExceptionally(e);
            }
        });

        return jobFuture;
    }

    private void handleDoubleJobCompletion(boolean result, Drone drone, String jobid, 
                                          Item item1, Item item2, DoubleItemJob job, 
                                          double usage, CompletableFuture<Boolean> jobFuture) {
        if (result) {
            drone.setAvailable(DroneStatus.AVAILABLE.getCode());

            logManager.addLog(new String[]{
                getHourWithAmPm(),
                "Dual Job Completed ",
                "Items Delivered: " + item1.getItemId() + ", " + item2.getItemId() + " by Drone: " + drone.getDroneid()
            });

            jobs.removeJob(jobid);
            jobs.addToTotalBatteryUsage((int) usage);
            job.setTimeStarted(item1.addedTo);
            job.setTimeCompleted(System.currentTimeMillis());
            job.setDuration();
            jobs.addToJobStack(job);

            if (item1.priority) {
                jobs.allPriorityJobTimes.add(job.getJobDuration());
                System.out.println("Priority job for " + item1.getItemId() + " took " + job.getJobDuration() + " milliseconds");
            } else {
                jobs.allNormalJobTimes.add(job.getJobDuration());
                System.out.println("Normal job for " + item1.getItemId() + " took " + job.getJobDuration() + " milliseconds");
            }

            jobFuture.complete(true);
        } else {
            logManager.addLog(new String[]{
                getHourWithAmPm(),
                "Dual Job Failed ",
                "Failed dual job: " + jobid
            });

            drone.setAvailable(DroneStatus.AVAILABLE.getCode());
            jobs.removeJob(jobid);
            System.out.println("FAILURE: Drone " + drone.getDroneid() + " failed to deliver both items " + item1.getItemId() + ", " + item2.getItemId());
            jobFuture.complete(false);
        }
    }




    public void shutdown() {
        pathfindingExecutor.shutdown();
        try {
            if (!pathfindingExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                pathfindingExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            pathfindingExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    public static String getHourWithAmPm() {
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
        return formatter.format(new Date(System.currentTimeMillis()));
    }



    //find nearest 'availiable' battery station to the drone and send it there
   public void sendToNearestBatteryStation(Drone drone) {
    CompletableFuture.runAsync(() -> {
        boolean sent = false;
        while (!sent) {
            List<BatteryStation> sortedStations = new ArrayList<>(batteryStations);
            sortedStations.sort((a, b) -> {
                double distA = Math.pow(drone.getX() - a.getLocation().x, 2) +
                            Math.pow(drone.getY() - a.getLocation().y, 2);
                double distB = Math.pow(drone.getX() - b.getLocation().x, 2) +
                             Math.pow(drone.getY() - b.getLocation().y, 2);
                return Double.compare(distA, distB);
            });

            BatteryStation nearestStation = null;
            for (BatteryStation station : sortedStations) {
                if (1 == 1) {
                    nearestStation = station;
                    break;
                }
            }

            if (nearestStation != null) {
                final BatteryStation finalNearestStation = nearestStation;
                System.out.println("Drone " + drone.getDroneid() + " is moving to battery station: " + finalNearestStation.getName());
                boolean completed = moveToBS(drone, finalNearestStation.getX(), finalNearestStation.getY());
                if (completed) {
                    finalNearestStation.addDrone(drone);
                    sent = true;
                } else {
                    System.err.println("Drone " + drone.getDroneid() + " failed to reach battery station.");
                    
                }
            } else {
                System.err.println("No available battery stations for drone " + drone.getDroneid() + ", retrying...");
                try {
                    Thread.sleep(500); 
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    });
}


    //function to move drone to battery station, after replacing the battery.
    //simple pathfinding as before, here we simply move to target but do not broadcast thr path;
    public boolean moveToBS(Drone drone, double x, double y) {

        List<Node> path = pathfinder.findPath(map, map.getNode((int) drone.getX(), (int) drone.getY()), map.getNode((int) x, (int) y));

        for (Node next : path) {
            drone.moveTo(next.x, next.y);
            try {
                Thread.sleep(50); 
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }
        String[] logMessage = {
            getHourWithAmPm(),
            "Reacharging Drone",
            "Drone " + drone.getDroneid() + " moved to battery station at (" + x + " - " + y + ")"
        };
        logManager.addLog(logMessage);
        return true;
    }

    public Location randomTarget() {
        return targets.get(random.nextInt(targets.size()));
    }


    // public void allJobs() {
    //     System.out.println("creating jobs for all drones");
    //     for (ItemForDelivery item : ItemForDelivery.CurrentItemsAvailable.getItemsForDelivery()) {
    //         Drone drone = findClosestAvailableDrone(item.getPickUpStation().getLocation());
    //         if (drone == null) {
    //             System.out.println("No available drones found.");
    //             return;
    //         }
    //        createJob(drone, item);
    //     }
    // }

    public Drone findClosestAvailableDrone(Node location) {
        double minDistance = Double.MAX_VALUE;
        Drone closestDrone = null;
    
        List<Drone> availableDrones = drones.values().stream()
            .filter(drone -> drone.getAvailable().equals(DroneStatus.AVAILABLE.getCode()))
            .sorted(Comparator.comparing(Drone::getDroneid))
            .collect(Collectors.toList());
    
        for (Drone drone : availableDrones) {
            double distance = Math.pow(drone.getX() - location.x, 2) +
                              Math.pow(drone.getY() - location.y, 2);
            if (distance < minDistance) {
                minDistance = distance;
                closestDrone = drone;
            }
        }
    
        return closestDrone;
    }
    


}

