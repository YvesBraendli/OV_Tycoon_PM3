package ch.zhaw.ovtycoon.gui.model.dto;

public class ZoneTroopAmountInitDTO extends ZoneTroopAmountDTO {
    private final int translateX;
    private final int translateY;

    public ZoneTroopAmountInitDTO(String zoneName, int troopAmount, int translateX, int translateY) {
        super(zoneName, troopAmount);
        this.translateX = translateX;
        this.translateY = translateY;
    }

    public int getTranslateX() {
        return translateX;
    }

    public int getTranslateY() {
        return translateY;
    }
}
