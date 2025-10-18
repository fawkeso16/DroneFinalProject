package com.example.Drone_Project;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;


//data struccture = timestamp, type, message
final class LogManager {

    private final List<String>  logs;
    private final int maxLogs;
    private final String filePath = "src/main/resources/files/log.csv";


    public LogManager(int maxLogs){
        this.logs = new ArrayList<>();
        this.maxLogs = maxLogs;


        this.loadLogs();

    }

    public List<String> loadLogs(){

        try{
            try (Scanner scanner = new Scanner(new java.io.File(filePath))) {
                while (scanner.hasNextLine()) {
                    logs.add(scanner.nextLine());
                }
            }
            return logs;
        } catch (Exception e) {
            System.err.println("Error loading logs: " + e.getMessage());
            return Collections.emptyList();
        }
        

    }

    public void addLog(String[] log) {
        if (logs.size() >= maxLogs) {
            logs.remove(0);
        }

    String csvLog = String.join(",", log);
        logs.add(csvLog);
        saveLogs();
    }


    public List<String> getLogs() {
    List<String> fileLogs = new ArrayList<>();
    try {
        File file = new File(this.filePath);
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                fileLogs.add(scanner.nextLine());
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }

    return fileLogs;
}



    private void saveLogs() {
        try {
            try (java.io.FileWriter writer = new java.io.FileWriter(filePath, false)) {
                for (String log : logs) {
                    writer.write(log + System.lineSeparator());
                }
            }
        } catch (Exception e) {
            System.err.println("Error saving logs: " + e.getMessage());
        }
    }

  
}

