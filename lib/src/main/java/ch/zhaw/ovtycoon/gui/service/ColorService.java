package ch.zhaw.ovtycoon.gui.service;

import ch.zhaw.ovtycoon.Config;
import ch.zhaw.ovtycoon.gui.model.ZoneColor;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides methods for {@link Color}, {@link ZoneColor}
 * and {@link ch.zhaw.ovtycoon.Config.PlayerColor} related operations.
 */
public class ColorService {
    private static final Pattern JAVAFX_COLOR_AS_HEX_STRING_PATTERN = Pattern.compile("^0x([0-9a-f]{2})([0-9a-f]{2})([0-9a-f]{2})([0-9a-f]{2})$");
    private static final int HEXADECIMAL_RADIX = 16;
    private static final int JAVAFX_COLOR_HEX_STRING_PARTS_AMOUNT = 4;
    private static final double HIGHEST_BYTE_NUMBER_AS_DOUBLE = 255.0d;
    private PixelReader pixelReader;

    /**
     * Creates an instance of the color service with the passed pixel reader.
     * @param pixelReader Pixel reader to be used
     */
    public ColorService(PixelReader pixelReader) {
        this.pixelReader = pixelReader;
    }

    /**
     * Creates an instance of the color service without setting the {@link #pixelReader} property.
     * Method isZoneColor will be unavailable.
     */
    public ColorService() {}

    /**
     * Checks if color of the pixel at the passed x and y coordinate is equal to the passed zone color.
     * @param x x coordinate of the pixel to be checked
     * @param y y coordinate of the pixel to be checked
     * @param zoneColor zone color for comparison
     * @throws UnsupportedOperationException if the {@link #pixelReader} is null.
     * @return Whether the color at the passed coordinates is equal to the passed zone color or not.
     */
    public boolean isZoneColor(int x, int y, ZoneColor zoneColor) {
        if (pixelReader == null) throw new UnsupportedOperationException("Operation \"isZoneColor\" not supported: pixelReader is null");
        return zoneColor == getZoneColor(pixelReader.getColor(x, y).toString());
    }

    /**
     * Gets the {@link ch.zhaw.ovtycoon.Config.PlayerColor} with the hexadecimal value equal to the passed string.
     * @param color String with a hexadecimal color value.
     * @return Player color with the hexadecimal value equal to the passed string, null
     * null if no matching player color could be found
     */
    public Config.PlayerColor getPlayerColor(String color) {
        for (Config.PlayerColor playerColor : Config.PlayerColor.values()) {
            if (playerColor.getHexValue().equals(color)) {
                return playerColor;
            }
        }
        return null;
    }

    /**
     * Gets the {@link ZoneColor} with the name equal to the passed name.
     * @param name Name of the zone color
     * @return Matching zone color. Null if no match could be found.
     */
    public ZoneColor getZoneColorByName(String name) {
        for (ZoneColor zoneColor: ZoneColor.values()) {
            if (name != null && name.equals(zoneColor.toString())) {
                return zoneColor;
            }
        }
        return null;
    }

    /**
     * Converts the passed string to the according {@link Color}
     * @param hex String containing hexadecimal value
     * @return Converted color
     */
    public Color getColor(String hex) {
        Matcher matcher = JAVAFX_COLOR_AS_HEX_STRING_PATTERN.matcher(hex);
        double[] colorParams = new double[JAVAFX_COLOR_HEX_STRING_PARTS_AMOUNT];
        if (!matcher.find() || matcher.groupCount() != JAVAFX_COLOR_HEX_STRING_PARTS_AMOUNT) return null;
        for (int i = 0; i < matcher.groupCount(); i++) {
            colorParams[i] = Integer.parseInt(matcher.group(i + 1), HEXADECIMAL_RADIX) / HIGHEST_BYTE_NUMBER_AS_DOUBLE;
        }
        return new Color(colorParams[0], colorParams[1], colorParams[2], colorParams[3]);
    }

    /**
     * Converts two colors to a mixed color.
     * @param foregroundColor Foreground color
     * @param backgroundColor Background color
     * @return Mixed color
     */
    public Color mixColors(Color foregroundColor, Color backgroundColor) {
        double opacity = 1 - (1 - foregroundColor.getOpacity()) * (1 - backgroundColor.getOpacity());
        double r = foregroundColor.getRed() * foregroundColor.getOpacity() / opacity + backgroundColor.getRed() * backgroundColor.getOpacity() * (1 - foregroundColor.getOpacity()) / opacity;
        double g = foregroundColor.getGreen() * foregroundColor.getOpacity() / opacity + backgroundColor.getGreen() * backgroundColor.getOpacity() * (1 - foregroundColor.getOpacity()) / opacity;
        double b = foregroundColor.getBlue() * foregroundColor.getOpacity() / opacity + backgroundColor.getBlue() * backgroundColor.getOpacity() * (1 - foregroundColor.getOpacity()) / opacity;
        return new Color(r, g, b, opacity);
    }

    private ZoneColor getZoneColor(String color) {
        for (ZoneColor mapColor : ZoneColor.values()) {
            if (mapColor.getColorAsHexString().equals(color)) {
                return mapColor;
            }
        }
        return null;
    }
}
