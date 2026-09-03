package eu.spex.iorg.service;

import java.io.PrintStream;
import java.text.MessageFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    public enum Level {
        DEBUG, INFO, WARN, ERROR
    }

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private static Level minLevel = Level.INFO;

    private Logger() {
    }

    public static void setMinLevel(Level level) {
        minLevel = level;
    }

    public static void debug(String message, Object... params) {
        log(Level.DEBUG, System.out, message, params);
    }

    public static void info(String message, Object... params) {
        log(Level.INFO, System.out, message, params);
    }

    public static void warn(String message, Object... params) {
        log(Level.WARN, System.out, message, params);
    }

    public static void error(String message, Object... params) {
        log(Level.ERROR, System.err, message, params);
    }

    public static void error(Throwable th, String message, Object... params) {
        if (!isEnabled(Level.ERROR)) {
            return;
        }
        log(Level.ERROR, System.err, message, params);
        th.printStackTrace(System.err);
    }

    private static boolean isEnabled(Level level) {
        return level.ordinal() >= minLevel.ordinal();
    }

    private static void log(Level level, PrintStream out, String message, Object... params) {
        if (!isEnabled(level)) {
            return;
        }
        String formatted = params.length == 0 ? message : MessageFormat.format(message, params);
        out.println(LocalTime.now().format(TIME_FORMAT) + " " + level + ": " + formatted);
    }
}
