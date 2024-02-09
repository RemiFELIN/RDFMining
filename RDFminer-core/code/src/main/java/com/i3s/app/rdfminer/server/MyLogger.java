package com.i3s.app.rdfminer.server;

import java.time.Instant;

public class MyLogger {

    public static void info(String method, String message) {
        System.out.println(Instant.now() + " - " + method + "/ - " + message);
    }

}
