package ch.zhaw.ovtycoon.gui.model;

import ch.zhaw.ovtycoon.Config;
import javafx.scene.text.Text;

import java.util.List;

public class ZoneSquare {
    private final String name;
    private final List<HorizontalStripe> border;
    private final Pixel center;
    private final Text troopsAmountText = new Text(); // TODO ev use in equals & hashCode as well
    private Config.PlayerColor color;

    public ZoneSquare(String name, List<HorizontalStripe> border, Pixel center) {
        this.name = name;
        this.border = border;
        this.center = center;
        initializeTroopsAmountText();
    }

    public void setColor(Config.PlayerColor color) {
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public Config.PlayerColor getColor() {
        return color;
    }

    public List<HorizontalStripe> getBorder() {
        return border;
    }

    public Pixel getCenter() {
        return center;
    }

    public Text getTroopsAmountText() {
        return troopsAmountText;
    }

    public void updateTroopsAmount(String amountNew) {
        this.troopsAmountText.setText(amountNew);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ZoneSquare that = (ZoneSquare) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (border != null ? !border.equals(that.border) : that.border != null) return false;
        return center != null ? center.equals(that.center) : that.center == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (border != null ? border.hashCode() : 0);
        result = 31 * result + (center != null ? center.hashCode() : 0);
        return result;
    }

    private void initializeTroopsAmountText() {
        troopsAmountText.setStyle("-fx-fill: lightgray;-fx-font-weight: bold;");
        troopsAmountText.setTranslateX(center.getX());
        troopsAmountText.setTranslateY(center.getY());
    }
}
