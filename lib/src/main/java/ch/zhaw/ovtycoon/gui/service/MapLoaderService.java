package ch.zhaw.ovtycoon.gui.service;

import ch.zhaw.ovtycoon.gui.model.HorizontalStripe;
import ch.zhaw.ovtycoon.gui.model.Pixel;
import ch.zhaw.ovtycoon.gui.model.Square;
import ch.zhaw.ovtycoon.gui.model.ZoneColor;
import ch.zhaw.ovtycoon.gui.model.ZoneSquare;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.text.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapLoaderService {
    // startX, offsetX, startY, offsetY, color, name
    private static final Pattern ZONE_SQUARE_DATA_PATTERN = Pattern.compile("sX=([0-9]+), oX=([0-9]+), sY=([0-9]+), oY=([0-9]+), color=([^,]+), name=([^;]+), center=\\(([0-9]+,[0-9]+)\\);");
    private static final int DATA_GROUP_COUNT = 7;
    private static final String ZONES_TXT_PATH_POSTFIX = "\\src\\main\\resources\\zones.txt";

    private final PixelReader imagePixelReader;
    private final ColorService colorService;

    public MapLoaderService(Image image) {
        this.imagePixelReader = image.getPixelReader();
        this.colorService = new ColorService(imagePixelReader);
    }

    public List<ZoneSquare> initZoneSquaresFromConfig() {
        long start = System.currentTimeMillis();
        List<ZoneSquare> zoneSquares = new ArrayList<>();
        try {
            String dir = System.getProperty("user.dir");
            if (!dir.contains("lib")) {
                dir += "\\lib";
            }
            String zonesTxtPath = dir + ZONES_TXT_PATH_POSTFIX;
            File zones = new File(zonesTxtPath);
            try (BufferedReader br = new BufferedReader(new FileReader(zones))) {
                String line;
                while ((line = br.readLine()) != null) {
                    Matcher matcher = ZONE_SQUARE_DATA_PATTERN.matcher(line);
                    if (matcher.find() && matcher.groupCount() == DATA_GROUP_COUNT) {
                        ZoneSquare zoneSquare = new ZoneSquare();
                        Square square = new Square();
                        square.setStartX(Integer.parseInt(matcher.group(1)));
                        square.setEndX(Integer.parseInt(matcher.group(2)));
                        square.setStartY(Integer.parseInt(matcher.group(3)));
                        square.setEndY(Integer.parseInt(matcher.group(4)));
                        zoneSquare.setSquare(square);
                        zoneSquare.setColor(colorService.getZoneColorByName(matcher.group(5)));
                        zoneSquare.setName(matcher.group(6));
                        String center = matcher.group(7);
                        String[] centerCoordinates = center.split(",");
                        zoneSquare.setCenter(new Pixel(Integer.parseInt(centerCoordinates[0]), Integer.parseInt(centerCoordinates[1])));
                        setTroopsCountText(zoneSquare); // TODO move to ZoneSquare class
                        zoneSquares.add(zoneSquare);
                    }
                }
            }
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }
        zoneSquares.forEach(zsq -> setXBorderStripes(zsq)); // TODO why faster without multithreading?
        System.out.println(String.format("Finished stripe init in %d ms", System.currentTimeMillis() - start));
        return zoneSquares;
    }

    // TODO current impl doesnt work if !mapClr(i - 1) mapClr(i) !mapClr(i+1)
    private void setXBorderStripes(ZoneSquare zsq) {
        Square sqr = zsq.getSquare();
        ZoneColor zoneColor = zsq.getColor();
        List<HorizontalStripe> stripes = new ArrayList<>();
        for (int i = sqr.getStartY(); i <= sqr.getEndY(); i++) { // TODO check if index out of bounds exception possible here
            HorizontalStripe currStripe = new HorizontalStripe();

            boolean enteredZone = false;

            boolean prev = false;

            for (int j = sqr.getStartX(); j <= sqr.getEndX(); j++) {
                prev = enteredZone;
                enteredZone = colorService.isZoneColor(j, i, zoneColor);
                if (enteredZone && !prev) {
                    currStripe.setStartX(j);
                    currStripe.setY(i);
                } else if (!enteredZone && prev) {
                    currStripe.setEndX(j - 1); // prev was last pixel in zone
                    stripes.add(currStripe);
                    currStripe = new HorizontalStripe(); // if stripe intersected
                }
            }
        }
        zsq.setBorder(stripes);
    }

    private Text setTroopsCountText(ZoneSquare zoneSquare) {
        Text txt = new Text();
        txt.setStyle("-fx-fill: lightgray;-fx-font-weight: bold;");
        txt.setText("0");
        txt.setTranslateX(zoneSquare.getCenter().getX());
        txt.setTranslateY(zoneSquare.getCenter().getY());
        zoneSquare.setTxt(txt);
        return txt;
    }
}
