package eu.spex.iorg;

/**
 * Entry point for the executable fat jar.
 * <p>
 * The JavaFX launcher refuses to start when the main class itself extends {@link javafx.application.Application}
 * and JavaFX is loaded from the classpath instead of the module path. Delegating from a plain class avoids that check.
 */
public class Launcher {

    public static void main(String[] args) {
        ImageOrganizr.main(args);
    }
}
