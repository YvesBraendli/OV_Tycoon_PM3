package ch.zhaw.ovtycoon.gui.model.customnodes;

import ch.zhaw.ovtycoon.gui.model.NotificationType;
import javafx.scene.control.Label;

/**
 * Notification label. Has a predefined height {@link #NOTIFICATION_HEIGHT}.
 * Extends {@link Label}
 */
public class Notification extends Label {
    private static final double NOTIFICATION_HEIGHT = 100.0d;

    /**
     * Creates a notification instance.
     * @param type Type of the notification. The background color depends on the type.
     * @param text Text to be displayed
     * @param width Width of the notification label
     */
    public Notification(NotificationType type, String text, double width) {
        String backgroundColor;
        switch (type) {
            case WARNING: backgroundColor = "darkorange"; break;
            case ERROR: backgroundColor = "indianred"; break;
            default: backgroundColor = "midnightblue";
        }
        setPrefWidth(width);
        setPrefHeight(NOTIFICATION_HEIGHT);
        setMaxHeight(NOTIFICATION_HEIGHT);
        setText(text);
        setStyle("-fx-font-family: Arial; -fx-font-size: 24px; -fx-text-fill: white; -fx-padding: 20px 50px 20px 50px;" +
                String.format("-fx-background-color: %s;", backgroundColor));
    }
}
