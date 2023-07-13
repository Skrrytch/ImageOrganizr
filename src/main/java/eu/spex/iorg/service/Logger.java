package eu.spex.iorg.service;

import java.text.MessageFormat;

public class Logger {

    public static void info(String message, Object... params) {
        System.out.println("INFO: " + MessageFormat.format(message, params));
    }

    public static void warn(String message, Object... params) {
        System.out.println("WARN: " + MessageFormat.format(message, params));
    }

    public static void error(String message, Object... params) {
        System.out.println("ERROR: " + MessageFormat.format(message, params));
    }

    public static void error(Throwable th, String message, Object... params) {
        System.out.println("ERROR: " + MessageFormat.format(message, params));
        th.printStackTrace();
    }
}
