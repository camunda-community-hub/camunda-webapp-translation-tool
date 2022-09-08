package org.camunda.webapptranslation.tool.app;

import java.util.HashMap;
import java.util.Map;

public class AppTimeTracker {

    /**
     * Manage multiple tracker.
     */
    private static final Map<String, AppTimeTracker> allTimeTracker = new HashMap<>();
    private final String name;
    private long timeStart;
    private long executionTime;
    private long sumOfTimeMs = 0;
    private long nbOccurences = 0;

    private AppTimeTracker(String name) {
        this.name = name;
    }

    /**
     * Get a specific time tracker by its name. If this not already exist, create it
     *
     * @param name name of tracker
     * @return the tracker
     */
    public static AppTimeTracker getTimeTracker(String name) {
        AppTimeTracker timeTracker = allTimeTracker.get(name);
        if (timeTracker == null) {
            timeTracker = new AppTimeTracker(name);
            allTimeTracker.put(name, timeTracker);
        }
        return timeTracker;
    }

    /**
     * Get all TimeTrackers
     *
     * @return all tracker
     */
    public static Map<String, AppTimeTracker> getAllTimeTracker() {
        return allTimeTracker;
    }

    /**
     * Call a start to start to monitor the time. Call stop() at the end of the operation
     */
    public void start() {
        timeStart = System.currentTimeMillis();
        executionTime = 0;
    }

    /**
     * Stop finish monitoring the current execution
     */
    public void stop() {
        executionTime = System.currentTimeMillis() - timeStart;
        sumOfTimeMs += executionTime;
        nbOccurences++;
    }

    public long getLastExecutionTime() {
        return executionTime;
    }

    public String getInformations() {
        return "  *****> " + getName() + ": "
                + getSumOfTimeMs() + " ms for "
                + getNbOccurences() + " ope."
                + (getNbOccurences() > 0 ? ((int) getSumOfTimeMs() / getNbOccurences()) + " ms/ope" : "");
    }

    public String getName() {
        return name;
    }

    public long getTimeStart() {
        return timeStart;
    }

    public long getSumOfTimeMs() {
        return sumOfTimeMs;
    }

    public long getNbOccurences() {
        return nbOccurences;
    }
}
