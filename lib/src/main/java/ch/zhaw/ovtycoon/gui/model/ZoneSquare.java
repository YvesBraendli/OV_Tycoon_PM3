package ch.zhaw.ovtycoon.gui.model;

import javafx.scene.text.Text;

import java.util.List;

// TODO make everything except color and txt final
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

    public void setTroopsAmountText(Text txt) {
        this.txt = txt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ZoneSquare that = (ZoneSquare) o;

        if (square != null ? !square.equals(that.square) : that.square != null) return false;
        if (color != that.color) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (border != null ? !border.equals(that.border) : that.border != null) return false;
        if (center != null ? !center.equals(that.center) : that.center != null) return false;
        return txt != null ? txt.equals(that.txt) : that.txt == null;
    }

    // TODO document changes to default hash code method
    @Override
    public int hashCode() {
        int result = square != null ? square.hashCode() : 0;
        // result = 31 * result + (color != null ? color.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (border != null ? border.hashCode() : 0);
        result = 31 * result + (center != null ? center.hashCode() : 0);
        // result = 31 * result + (txt != null ? txt.hashCode() : 0);
        return result;
    }
}
