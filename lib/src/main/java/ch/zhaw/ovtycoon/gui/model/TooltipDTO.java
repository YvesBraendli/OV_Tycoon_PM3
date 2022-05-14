package ch.zhaw.ovtycoon.gui.model;

public class TooltipDTO {
    private final int translateX;
    private final int translateY;
    private final String tooltipText;

    public TooltipDTO(int translateX, int translateY, String tooltipText) {
        this.translateX = translateX;
        this.translateY = translateY;
        this.tooltipText = tooltipText;
    }

    public int getTranslateX() {
        return translateX;
    }

    public int getTranslateY() {
        return translateY;
    }

    public String getTooltipText() {
        return tooltipText;
    }
}
