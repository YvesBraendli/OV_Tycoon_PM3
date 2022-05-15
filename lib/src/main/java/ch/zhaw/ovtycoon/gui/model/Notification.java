package ch.zhaw.ovtycoon.gui.model;

import javafx.scene.control.Label;

public class Notification extends Label {
    private static final double NOTIFICATION_HEIGHT = 100.0d;

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
