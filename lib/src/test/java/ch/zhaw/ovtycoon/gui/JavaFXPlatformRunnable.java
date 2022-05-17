package ch.zhaw.ovtycoon.gui;

import javafx.application.Platform;

public class JavaFXPlatformRunnable {
    private static JavaFXPlatformRunnable javaFXPlatformRunnable = null;

    private JavaFXPlatformRunnable() {
        Platform.startup(() -> {});
    }

    public static void run() {
        if (javaFXPlatformRunnable == null) {
            javaFXPlatformRunnable = new JavaFXPlatformRunnable();
        }
    }
}
