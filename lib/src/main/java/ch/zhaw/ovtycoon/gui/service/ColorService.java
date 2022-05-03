package ch.zhaw.ovtycoon.gui.service;

import ch.zhaw.ovtycoon.gui.model.ZoneColor;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorService {
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("^0x([0-9a-f]{2})([0-9a-f]{2})([0-9a-f]{2})([0-9a-f]{2})$");
    private PixelReader pixelReader;

    public ColorService(PixelReader pixelReader) {
        this.pixelReader = pixelReader;
    }

    public ColorService() {}

    // TODO throw exception if called & pixel reader is null
    public boolean isZoneColor(int x, int y, ZoneColor zoneColor) {
        if (pixelReader == null) throw new UnsupportedOperationException();
        return zoneColor == getZoneColor(pixelReader.getColor(x, y).toString());
    }

    public ZoneColor getZoneColor(String color) {
        for (ZoneColor mapColor : ZoneColor.values()) {
            if (mapColor.getColorAsHexString().equals(color)) {
                return mapColor;
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
        Matcher matcher = HEX_COLOR_PATTERN.matcher(hex);
        double[] colorParams = new double[4];
        if (!matcher.find() || matcher.groupCount() != 4) return null;
        for (int i = 0; i < matcher.groupCount(); i++) {
            colorParams[i] = Integer.parseInt(matcher.group(i + 1), 16) / 255.0d;
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
}
