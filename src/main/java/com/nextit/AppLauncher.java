package com.nextit;

/**
 * Bootstrap launcher for fat JAR and native jpackage packaging.
 *
 * <p>Does not extend {@code javafx.application.Application}, allowing the JVM to start
 * from a standalone executable without requiring external module path arguments.</p>
 */
public final class AppLauncher {

    private AppLauncher() {}

    public static void main(String[] args) {
        Main.main(args);
    }
}

