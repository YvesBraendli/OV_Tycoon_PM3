package ch.zhaw.ovtycoon.gui.model;

/**
 * Colors of the zones on the zvv zones image.
 */
public enum ZoneColor {
    RED("0xf18d6dff"), BLUE("0xa2cbeeff"), GREEN("0x8bc688ff"), YELLOW("0xf6d78cff"), WHITE("0xd6ffffff");
    private final String color;
    private ZoneColor(String c) {
        this.color = c;
    }

    public String getColorAsHexString() {
        return color;
    }
}
