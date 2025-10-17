package com.example.Drone_Project;


import java.util.ArrayList;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class JobOverviewService {
    private final ConcurrentHashMap<String, SingleItemJob> jobs = new ConcurrentHashMap<>();
    public ArrayList<Long> allNormalJobTimes = new ArrayList<>();
    public ArrayList<Long> allPriorityJobTimes = new ArrayList<>();
    public static int totalBatteryUsage = 0;
    public static Stack<SingleItemJob> jobStack = new Stack<>();

    private final ConcurrentHashMap<String, DoubleItemJob> doubleJobs = new ConcurrentHashMap<>();
    public ArrayList<Long> allDoubleNormalJobTimes = new ArrayList<>();
    public ArrayList<Long> allDoublePriorityJobTimes = new ArrayList<>();
    public static int totalDoubleBatteryUsage = 0;
    public static Stack<DoubleItemJob> doubleJobStack = new Stack<>();


    public SingleItemJob getJobById(String id) {
        return jobs.get(id);
    }
    public ConcurrentHashMap<String, SingleItemJob> getAllJobs() {
        return jobs;
    }

    public void addJob(SingleItemJob job) {
        if (job == null || job.getId() == null) return;
        jobs.put(job.getId(), job);
    }

    public void removeJob(String id) {
        jobs.remove(id);
    }

    public boolean hasJob(String id) {
        return jobs.containsKey(id);
    }

    public void addNormalJobTime(long time) {
        allNormalJobTimes.add(time);
    }
    public void addPriorityJobTime(long time) {
        allPriorityJobTimes.add(time);
    }
    private long average(ArrayList<Long> list) {
        if (list == null || list.isEmpty()) return 0L;
        long sum = 0L;
        for (long v : list) sum += v;
        return sum / list.size();
    }
    public long getAverageNormalJobTime() { return average(allNormalJobTimes); }
    public long getAveragePriorityJobTime() { return average(allPriorityJobTimes); }

    public void addToTotalBatteryUsage(int usage) {
        totalBatteryUsage += usage;
    }

    public int getTotalBatteryUsage() {
        return totalBatteryUsage;
    }

    public Stack<SingleItemJob> completedJobs() {
        return jobStack;
    }

    public void addToJobStack(SingleItemJob job) {
        jobStack.push(job);
    }




    //services for double item jobs

    public DoubleItemJob getDoubleJobById(String id) {
        return doubleJobs.get(id);
    }
    public ConcurrentHashMap<String, DoubleItemJob> getAllDoubleJobs() {
        return doubleJobs;
    }

    public void addDoubleJob(DoubleItemJob job) {
        if (job == null || job.getId() == null) return;
        doubleJobs.put(job.getId(), job);
    }

    public void removeDoubleJob(String id) {
        doubleJobs.remove(id);
    }

    public boolean hasDoubleJob(String id) {
        return doubleJobs.containsKey(id);
    }

    public void addDoubleNormalJobTime(long time) {
        allDoubleNormalJobTimes.add(time);
    }
    public void addDoublePriorityJobTime(long time) {
        allDoublePriorityJobTimes.add(time);
    }
    public long getAverageDoubleNormalJobTime() { return average(allDoubleNormalJobTimes); }
    public long getAverageDoublePriorityJobTime() { return average(allDoublePriorityJobTimes); }

    public void addToDoubleTotalBatteryUsage(int usage) {
        totalDoubleBatteryUsage += usage;
    }

    public int getTotalDoubleBatteryUsage() {
        return totalDoubleBatteryUsage;
    }

    public Stack<DoubleItemJob> completedDoubleJobs() {
        return doubleJobStack;
    }

    public void addToDoubleJobStack(DoubleItemJob job) {
        doubleJobStack.push(job);
    }


    
}
