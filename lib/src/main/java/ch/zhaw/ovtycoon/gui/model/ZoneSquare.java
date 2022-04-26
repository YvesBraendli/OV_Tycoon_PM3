package ch.zhaw.ovtycoon.gui.model;

import javafx.scene.text.Text;

import java.util.List;

public class ZoneSquare {
    private Square square;
    private ZoneColor color;
    private String name;
    private List<HorizontalStripe> border;
    private Pixel center;
    private Text txt;

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

    public Pixel getCenter() {
        return center;
    }

    public void setCenter(Pixel center) {
        this.center = center;
    }

    public Text getTxt() {
        return txt;
    }

    public void setTxt(Text txt) {
        this.txt = txt;
    }
}
