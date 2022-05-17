package ch.zhaw.ovtycoon.gui.model;

import ch.zhaw.ovtycoon.Config;

import java.util.List;

/**
 * Representation of a zone on the game map.
 */
public class ZoneSquare {
    private final String name;
    private final List<HorizontalStripe> border;
    private final Pixel center;
    private Config.PlayerColor color;

    /**
     * Creates a zone square instance.
     * @param name Name of the zone
     * @param border List of {@link HorizontalStripe} representing the border of the zone
     * @param center Predefined center of the zone. Is being used for determining the position
     *               of the troop amount text node.
     */
    public ZoneSquare(String name, List<HorizontalStripe> border, Pixel center) {
        this.name = name;
        this.border = border;
        this.center = center;
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

    /**
     * Determines if this zone square is equal to the passed object.
     * Only comparing final properties ({@link #name}, {@link #border}, {@link #center}).
     * @param o Object to compare with this object
     * @return Whether the zone squares are equal or not
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ZoneSquare that = (ZoneSquare) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (border != null ? !border.equals(that.border) : that.border != null) return false;
        return center != null ? center.equals(that.center) : that.center == null;
    }

    /**
     * Calculates the hash code value of this zone square.
     * Only final properties ({@link #name}, {@link #border}, {@link #center}) get used for the calculation.
     * @return calculated hash code
     */
    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (border != null ? border.hashCode() : 0);
        result = 31 * result + (center != null ? center.hashCode() : 0);
        return result;
    }
}
