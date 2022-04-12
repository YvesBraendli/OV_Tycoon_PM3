package GUI.model;

import java.util.List;

public class ZoneSquare {
    private Square square;
    private ZoneColor color;
    private String name;
    private List<HorizontalStripe> border;

    public ZoneSquare(Square square, ZoneColor color, String name) {
        this.square = square;
        this.color = color;
        this.name = name;

    }

    public void setSquare(Square square) {
        this.square = square;
    }

    public void setColor(ZoneColor color) {
        this.color = color;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBorder(List<HorizontalStripe> border) {
        this.border = border;
    }

    public ZoneSquare() {}

    public String getName() {
        return name;
    }

    public Square getSquare() {
        return square;
    }

    public ZoneColor getColor() {
        return color;
    }

    public List<HorizontalStripe> getBorder() {
        return border;
    }

    @Override
    public String toString() {
        return "ZoneSquare{" +
                "square=" + square +
                ", color=" + color +
                ", name='" + name + '\'' +
                '}';
    }
}
