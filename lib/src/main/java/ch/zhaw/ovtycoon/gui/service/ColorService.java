package ch.zhaw.ovtycoon.gui.service;

import ch.zhaw.ovtycoon.Config;
import ch.zhaw.ovtycoon.gui.model.ZoneColor;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorService {
    private static final Pattern JAVAFX_COLOR_AS_HEX_STRING_PATTERN = Pattern.compile("^0x([0-9a-f]{2})([0-9a-f]{2})([0-9a-f]{2})([0-9a-f]{2})$");
    private static final int HEXADECIMAL_RADIX = 16;
    private static final int JAVAFX_COLOR_HEX_STRING_PARTS_AMOUNT = 4;
    private static final double HIGHEST_BYTE_NUMBER_AS_DOUBLE = 255.0d;
    private PixelReader pixelReader;

    public ColorService(PixelReader pixelReader) {
        this.pixelReader = pixelReader;
    }

    public ColorService() {}

    public boolean isZoneColor(int x, int y, ZoneColor zoneColor) {
        if (pixelReader == null) throw new UnsupportedOperationException("Operation \"isZoneColor\" not supported: pixelReader is null");
        return zoneColor == getZoneColor(pixelReader.getColor(x, y).toString());
    }

    public Config.PlayerColor getPlayerColor(String color) {
        for (Config.PlayerColor playerColor : Config.PlayerColor.values()) {
            if (playerColor.getHexValue().equals(color)) {
                return playerColor;
            }
        }
        return null;
    }

    public ZoneColor getZoneColorByName(String name) {
        for (ZoneColor zoneColor: ZoneColor.values()) {
            if (name != null && name.equals(zoneColor.toString())) {
                return zoneColor;
            }
        }
        return null;
    }

    public Color getColor(String hex) {
        Matcher matcher = JAVAFX_COLOR_AS_HEX_STRING_PATTERN.matcher(hex);
        double[] colorParams = new double[JAVAFX_COLOR_HEX_STRING_PARTS_AMOUNT];
        if (!matcher.find() || matcher.groupCount() != JAVAFX_COLOR_HEX_STRING_PARTS_AMOUNT) return null;
        for (int i = 0; i < matcher.groupCount(); i++) {
            colorParams[i] = Integer.parseInt(matcher.group(i + 1), HEXADECIMAL_RADIX) / HIGHEST_BYTE_NUMBER_AS_DOUBLE;
        }
        return new Color(colorParams[0], colorParams[1], colorParams[2], colorParams[3]);
    }

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
