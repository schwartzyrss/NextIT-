package com.nextit.util;

import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.prefs.Preferences;

/**
 * Manages native desktop window geometry, stage icons, browser-behavior suppression,
 * and session state persistence across application runs.
 */
public final class WindowBoundsManager {

    private static final Logger log = LoggerFactory.getLogger(WindowBoundsManager.class);

    private static final String PREF_X = "window_x";
    private static final String PREF_Y = "window_y";
    private static final String PREF_WIDTH = "window_width";
    private static final String PREF_HEIGHT = "window_height";
    private static final String PREF_MAXIMIZED = "window_maximized";

    public static final double MIN_WIDTH = 1280.0;
    public static final double MIN_HEIGHT = 800.0;
    public static final String APP_TITLE = "NextIT: Track Your Progress, Build Your Future";

    private WindowBoundsManager() {}

    /**
     * Configures the primary stage with application branding, icons, min bounds,
     * and restores previously saved window coordinates and maximization state.
     */
    public static void configureStage(Stage stage) {
        if (stage == null) return;

        stage.setTitle(APP_TITLE);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.setResizable(true);

        loadStageIcons(stage);
        restoreBounds(stage);

        // Save bounds whenever user resizes or moves the window
        stage.widthProperty().addListener((obs, oldV, newV) -> saveBounds(stage));
        stage.heightProperty().addListener((obs, oldV, newV) -> saveBounds(stage));
        stage.xProperty().addListener((obs, oldV, newV) -> saveBounds(stage));
        stage.yProperty().addListener((obs, oldV, newV) -> saveBounds(stage));
        stage.maximizedProperty().addListener((obs, oldV, newV) -> saveBounds(stage));
    }

    /**
     * Loads multi-resolution NextIT icons (16, 32, 48, 64, 128, 256) into the stage
     * so Windows can pick the ideal DPI icon for Title Bar, Taskbar, and Alt+Tab switcher.
     */
    public static void loadStageIcons(Stage stage) {
        int[] sizes = {16, 32, 48, 64, 128, 256};
        for (int size : sizes) {
            String path = "/com/nextit/icons/nextit-" + size + ".png";
            try (InputStream is = WindowBoundsManager.class.getResourceAsStream(path)) {
                if (is != null) {
                    stage.getIcons().add(new Image(is));
                }
            } catch (Exception e) {
                log.debug("Could not load icon {}: {}", path, e.getMessage());
            }
        }
    }

    /**
     * Restores window position and size from user preferences.
     * Validates that the window intersects with at least one active display;
     * otherwise, centers the window at standard minimum desktop resolution (1280x800).
     */
    public static void restoreBounds(Stage stage) {
        try {
            Preferences prefs = Preferences.userNodeForPackage(WindowBoundsManager.class);
            double width = prefs.getDouble(PREF_WIDTH, MIN_WIDTH);
            double height = prefs.getDouble(PREF_HEIGHT, MIN_HEIGHT);
            double x = prefs.getDouble(PREF_X, -1);
            double y = prefs.getDouble(PREF_Y, -1);
            boolean maximized = prefs.getBoolean(PREF_MAXIMIZED, false);

            width = Math.max(width, MIN_WIDTH);
            height = Math.max(height, MIN_HEIGHT);

            boolean isWithinVisibleScreen = false;
            if (x >= 0 && y >= 0) {
                ObservableList<Screen> screens = Screen.getScreens();
                for (Screen s : screens) {
                    Rectangle2D bounds = s.getVisualBounds();
                    if (bounds.intersects(x, y, Math.min(width, 400), Math.min(height, 300))) {
                        isWithinVisibleScreen = true;
                        break;
                    }
                }
            }

            stage.setWidth(width);
            stage.setHeight(height);

            if (isWithinVisibleScreen) {
                stage.setX(x);
                stage.setY(y);
            } else {
                stage.centerOnScreen();
            }

            if (maximized) {
                stage.setMaximized(true);
            }
        } catch (Exception e) {
            log.warn("Failed to restore window bounds: {}", e.getMessage());
            stage.setWidth(MIN_WIDTH);
            stage.setHeight(MIN_HEIGHT);
            stage.centerOnScreen();
        }
    }

    /**
     * Saves current window position, dimension, and maximized state into user preferences.
     */
    public static void saveBounds(Stage stage) {
        if (stage == null || !stage.isShowing()) return;
        try {
            Preferences prefs = Preferences.userNodeForPackage(WindowBoundsManager.class);
            boolean isMaximized = stage.isMaximized();
            prefs.putBoolean(PREF_MAXIMIZED, isMaximized);

            if (!isMaximized && !stage.isIconified()) {
                double w = stage.getWidth();
                double h = stage.getHeight();
                double x = stage.getX();
                double y = stage.getY();

                if (w >= MIN_WIDTH && h >= MIN_HEIGHT && x >= 0 && y >= 0) {
                    prefs.putDouble(PREF_WIDTH, w);
                    prefs.putDouble(PREF_HEIGHT, h);
                    prefs.putDouble(PREF_X, x);
                    prefs.putDouble(PREF_Y, y);
                }
            }
            prefs.flush();
        } catch (Exception e) {
            log.debug("Could not persist window bounds: {}", e.getMessage());
        }
    }

    /**
     * Attaches desktop-specific keyboard filters to a Scene:
     * - Suppresses accidental browser reload keys (F5, Ctrl+R)
     * - Suppresses browser history navigation keys (Alt+Left, Alt+Right)
     * - Preserves standard clipboard shortcuts (Ctrl+C, Ctrl+V, Ctrl+X, Ctrl+A) in inputs.
     */
    public static void installDesktopEventFilters(Scene scene) {
        if (scene == null) return;

        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            // Block F5 accidental reload
            if (event.getCode() == KeyCode.F5) {
                event.consume();
                return;
            }

            // Block Ctrl+R / Cmd+R page reload
            if (event.isShortcutDown() && event.getCode() == KeyCode.R) {
                event.consume();
                return;
            }

            // Block Alt+Left and Alt+Right browser page history navigation
            if (event.isAltDown() && (event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.RIGHT)) {
                event.consume();
            }
        });
    }
}
