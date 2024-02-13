package com.i3s.app.rdfminer.server;

import java.time.Instant;

public class MyLogger {

    public static void error(String message) {
        System.out.println(Instant.now() + " - ERROR - " + message);
    }

    public static void info(String message) {
        System.out.println(Instant.now() + " - INFO - " + message);
    }

    public static void warn(String message) {
        System.out.println(Instant.now() + " - WARN - " + message);
    }

}
