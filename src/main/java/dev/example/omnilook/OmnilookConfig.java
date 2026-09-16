package dev.example.omnilook;

import java.io.*;
import java.util.Properties;

/**
 * Tiny properties-file config. No config-screen dependency is used here
 * (Mod Menu / Legacy Mod Menu support is out of scope for this minimal
 * build) - edit config/omnilook-lite.properties by hand, or wire up
 * Legacy Mod Menu yourself later if you want a GUI.
 */
public final class OmnilookConfig {
    private static final File FILE = new File("config", "omnilook-lite.properties");

    public static boolean cameraModeToggle = false; // false = hold, true = toggle
    public static boolean yawFollowModeToggle = false;
    public static double mouseSensitivityMultiplier = 1.0D;

    private OmnilookConfig() {}

    public static void load() {
        Properties props = new Properties();
        if (FILE.exists()) {
            try (FileReader reader = new FileReader(FILE)) {
                props.load(reader);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        cameraModeToggle = Boolean.parseBoolean(props.getProperty("cameraModeToggle", "false"));
        yawFollowModeToggle = Boolean.parseBoolean(props.getProperty("yawFollowModeToggle", "false"));
        try {
            mouseSensitivityMultiplier = Double.parseDouble(props.getProperty("mouseSensitivityMultiplier", "1.0"));
        } catch (NumberFormatException e) {
            mouseSensitivityMultiplier = 1.0D;
        }

        save();
    }

    public static void save() {
        Properties props = new Properties();
        props.setProperty("cameraModeToggle", String.valueOf(cameraModeToggle));
        props.setProperty("yawFollowModeToggle", String.valueOf(yawFollowModeToggle));
        props.setProperty("mouseSensitivityMultiplier", String.valueOf(mouseSensitivityMultiplier));

        File dir = FILE.getParentFile();
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }

        try (FileWriter writer = new FileWriter(FILE)) {
            props.store(writer, "Omnilook Lite config - true = press to toggle, false = hold to activate");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
