package ch.zhaw.ovtycoon.gui.service;

import ch.zhaw.ovtycoon.gui.model.HorizontalStripe;
import ch.zhaw.ovtycoon.gui.model.Pixel;
import ch.zhaw.ovtycoon.gui.model.ZoneColor;
import ch.zhaw.ovtycoon.gui.model.ZoneSquare;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides methods to load all {@link ZoneSquare}'s from the zones.txt configuration file
 * and the passed image. Calculates the zone square's borders based on the passed scale.
 */
public class MapLoaderService {
    // startX, offsetX, startY, offsetY, color, name
    private static final Pattern ZONE_SQUARE_DATA_PATTERN = Pattern.compile("sX=([0-9]+), oX=([0-9]+), sY=([0-9]+), oY=([0-9]+), color=([^,]+), name=([^,]+), center=\\(([0-9]+,[0-9]+)\\);");
    private static final int DATA_GROUP_COUNT = 7;
    private static final String ZONES_TXT_PATH_POSTFIX = "/src/main/resources/zones.txt";
    private final PixelReader imagePixelReader;
    private final ColorService colorService;
    private double scale = 1.0d;

    public MapLoaderService(Image image, double scale) {
        this.imagePixelReader = image.getPixelReader();
        this.colorService = new ColorService(imagePixelReader);
        this.scale = scale;
    }

    /**
     * Reads data from the zones.txt file, calculates the exact borders of the zone squares
     * and returns a list of zone squares. Takes 1 - 2 seconds to finish.
     * @return Initialized list with zone squares.
     */
    public List<ZoneSquare> initZoneSquaresFromConfig() {
        List<ZoneSquare> zoneSquares = new ArrayList<>();
        try {
            String dir = System.getProperty("user.dir");
            if (!dir.contains("lib")) {
                dir += "/lib";
            }
            String zonesTxtPath = dir + ZONES_TXT_PATH_POSTFIX;
            File zones = new File(zonesTxtPath);
            try (BufferedReader br = new BufferedReader(new FileReader(zones))) {
                String line;
                while ((line = br.readLine()) != null) {
                    Matcher matcher = ZONE_SQUARE_DATA_PATTERN.matcher(line);
                    if (matcher.find() && matcher.groupCount() == DATA_GROUP_COUNT) {
                        ZoneConfigDTO dto = new ZoneConfigDTO();
                        dto.setStartX(Integer.parseInt(matcher.group(1)));
                        dto.setOffsetX(Integer.parseInt(matcher.group(2)));
                        dto.setStartY(Integer.parseInt(matcher.group(3)));
                        dto.setOffsetY(Integer.parseInt(matcher.group(4)));
                        dto.setColor(colorService.getZoneColorByName(matcher.group(5)));
                        String name = matcher.group(6);
                        String center = matcher.group(7);
                        String[] centerCoordinates = center.split(",");
                        Pixel zoneCenter = getCenter(Integer.parseInt(centerCoordinates[0]), Integer.parseInt(centerCoordinates[1]));
                        ZoneSquare zoneSquare = new ZoneSquare(name, getXBorderStripes(dto), zoneCenter);
                        zoneSquares.add(zoneSquare);
                    }
                }
            }
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return zoneSquares;
    }

    private List<HorizontalStripe> getXBorderStripes(ZoneConfigDTO zoneConfigDTO) {
        ZoneColor zoneColor = zoneConfigDTO.getColor();
        List<HorizontalStripe> stripes = new ArrayList<>();
        for (int i = zoneConfigDTO.getStartY(); i <= zoneConfigDTO.getEndY(); i++) {
            HorizontalStripe currStripe = new HorizontalStripe();

            boolean enteredZone = false;

            boolean prev = false;
            for (int j = zoneConfigDTO.getStartX(); j <= zoneConfigDTO.getEndX(); j++) {
                prev = enteredZone;
                enteredZone = colorService.isZoneColor(j, i, zoneColor);
                if (enteredZone && !prev) {
                    currStripe.setStartX((int) (j * scale));
                    currStripe.setY((int) (i * scale));
                } else if (!enteredZone && prev) {
                    currStripe.setEndX((int) ((j - 1) * scale)); // prev was last pixel in zone
                    stripes.add(currStripe);
                    currStripe = new HorizontalStripe(); // if stripe intersected
                }
            }
        }
        return stripes;
    }

    private Pixel getCenter(int x, int y) {
        return scale == 1.0d ? new Pixel(x, y) : new Pixel((int) (x * scale), (int) (y * scale));
    }

    private class ZoneConfigDTO {
        private int startX;
        private int offsetX;
        private int startY;
        private int offsetY;
        private ZoneColor color;

        public int getStartX() {
            return startX;
        }

        public void setStartX(int startX) {
            this.startX = startX;
        }

        public int getStartY() {
            return startY;
        }

        public void setStartY(int startY) {
            this.startY = startY;
        }

        public ZoneColor getColor() {
            return color;
        }

        public void setColor(ZoneColor color) {
            this.color = color;
        }

        public int getEndX() {
            return startX + offsetX;
        }

        public void setOffsetX(int offsetX) {
            this.offsetX = offsetX;
        }

        public int getEndY() {
            return startY + offsetY;
        }

        public void setOffsetY(int offsetY) {
            this.offsetY = offsetY;
        }
    }
}
