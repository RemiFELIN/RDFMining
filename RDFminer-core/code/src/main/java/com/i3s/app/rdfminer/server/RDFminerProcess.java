package com.i3s.app.rdfminer.server;

import com.i3s.app.rdfminer.Global;

import java.util.HashMap;
import java.util.Map;

/**
 * RDFminer-server activity management
 * This is a singleton
 * @author Rémi Felin
 */
public class RDFminerProcess {

    private static RDFminerProcess instance;

    private Map<String, Thread> runningProcesses;

    private RDFminerProcess() {
        this.runningProcesses = new HashMap<>();
    }

    public boolean setProcess(String userID, Thread thread) {
        if (this.hasConflict(userID) || this.runningProcesses.keySet().size() >= Global.MAX_SIMULTANEOUS_EXEC) {
            return false;
        }
        this.runningProcesses.put(userID, thread);
        return true;
    }

    public Thread getThread(String userID) {
        return this.runningProcesses.get(userID);
    }

    public void startThread(String userID) {
        Thread toExec = this.getThread(userID);
        MyLogger.info("Starting task (id: " + toExec.getId() + ") ...");
        toExec.start();
        try {
            toExec.join();
        } catch (InterruptedException e) {
            MyLogger.error("error during process starting: (id: " + toExec.getId() + ") ...");
            MyLogger.error(e.getMessage());
        } finally {
            MyLogger.info("clean processes ...");
            this.runningProcesses.remove(userID);
        }
    }

    public boolean killProcess(String userID) {
        Thread tokill = this.runningProcesses.get(userID);
        if (tokill != null) {
            tokill.interrupt();
            try {
                // Wait for the thread to complete
                tokill.join();
            } catch (InterruptedException e) {
                MyLogger.error("error during process shutdown: (id: " + tokill.getId() + ") ...");
                MyLogger.error(e.getMessage());
                return false;
            }
            // clean runningProcesses ...
            this.runningProcesses.remove(userID);
            return true;
        } else {
            MyLogger.warn("this user (id: " + userID + ") does not have a project running ...");
            return true;
        }
    }

    private boolean hasConflict(String userID) {
        if (this.runningProcesses.containsKey(userID)) {
            MyLogger.warn("this user (id: " + userID + ") already has a project running");
            return true;
        }
        return false;
    }

    public static synchronized RDFminerProcess getInstance() {
        if (instance == null) {
            instance = new RDFminerProcess();
        }
        return instance;
    }

}
