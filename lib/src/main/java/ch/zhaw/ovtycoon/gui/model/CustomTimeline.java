package ch.zhaw.ovtycoon.gui.model;

import javafx.animation.Timeline;

public class CustomTimeline {
    private final Timeline timeline;
    private final boolean isBlocking;

    public CustomTimeline(Timeline timeline, boolean isBlocking) {
        this.timeline = timeline;
        this.isBlocking = isBlocking;
    }

    public Timeline getTimeline() {
        return timeline;
    }

    public boolean isBlocking() {
        return isBlocking;
    }
}
