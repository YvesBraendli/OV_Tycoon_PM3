package ch.zhaw.ovtycoon.gui.model;

public enum ZoneColor {
    RED("0xf18d6dff"), BLUE("0xa2cbeeff"), GREEN("0x8bc688ff"), YELLOW("0xf6d78cff"), WHITE("0xd6ffffff"), PLAYER_RED("0xff0000ff"),
    PLAYER_BLUE("0x0000ffff"), PLAYER_GREEN("0xadff2fff");
    private final String color;
    private ZoneColor(String c) {
        this.color = c;
    }

    public String getColorAsHexString() {
        return color;
    }
}
