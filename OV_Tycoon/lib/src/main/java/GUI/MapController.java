package GUI;

import GUI.model.Pixel;
import GUI.model.Square;
import GUI.model.ZoneColor;
import GUI.model.ZoneSquare;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MapController {
    List<ZoneSquare> zoneSquares = new ArrayList<>();
    @FXML
    private Label zoneLabel;
    @FXML
    private Image img;
    @FXML
    private Canvas mapCanvas;
    @FXML
    private StackPane stackPane;
    private PixelWriter pw;
    private PixelReader pr;
    private Map<Color, List<Pixel>> overlaidPixelsMap;

    @FXML
    public void initialize() {
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        mapCanvas.setStyle("-fx-background-color: transparent;");
        pw = gc.getPixelWriter();
        initZoneSquares();
        addMapClickHandler();
    }

    private void initZoneSquares() {
        // startX, offsetX, startY, offsetY, color, name
        // TODO fix color group
        Pattern zoneSquareData = Pattern.compile("sX=([0-9]+), oX=([0-9]+), sY=([0-9]+), oY=([0-9]+), color:(.+), name=([^;]+);");
        final int dataGroupsCount = 6;
        try {
            String dir = System.getProperty("user.dir");
            if (!dir.contains("lib")) {
                dir += "\\lib";
            }
            String zonesTxtPath = dir + "\\src\\main\\resources\\zones.txt";
            File zones = new File(zonesTxtPath);
            BufferedReader br = new BufferedReader(new FileReader(zones));
            String line;
            while ((line = br.readLine()) != null) {
                Matcher matcher = zoneSquareData.matcher(line);
                if (matcher.find() && matcher.groupCount() == dataGroupsCount) {
                    ZoneSquare zoneSquare = new ZoneSquare();
                    Square square = new Square();
                    square.setStartX(Integer.parseInt(matcher.group(1)));
                    square.setEndX(Integer.parseInt(matcher.group(2)));
                    square.setStartY(Integer.parseInt(matcher.group(3)));
                    square.setEndY(Integer.parseInt(matcher.group(4)));
                    zoneSquare.setSquare(square);
                    zoneSquare.setColor(getZoneColorByName(matcher.group(5)));
                    zoneSquare.setName(matcher.group(6));
                    zoneSquares.add(zoneSquare);
                }
            }
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }
        /*zoneSquares.add(new ZoneSquare(new Square(446, 534, 343, 343 + 83), ZoneColor.GREEN, "Zone 170")); // 170
        zoneSquares.add(new ZoneSquare(new Square(314, 314 + 134, 307, 307 + 142), ZoneColor.RED, "Zone 122")); //122
        zoneSquares.add(new ZoneSquare(new Square(347, 347 + 147, 224, 224 + 129), ZoneColor.WHITE, "Zone 120")); // 120
        zoneSquares.add(new ZoneSquare(new Square(472, 472 + 120, 239, 239 + 124), ZoneColor.RED, "Zone 164")); // 164
        */
    }

    private void addMapClickHandler() {
        pr = img.getPixelReader();
        mapCanvas.setOnMouseClicked(e -> {
            this.onMapClick(e);
        });
    }

    private ZoneColor getZoneColor(String color) {
        for (ZoneColor mapColor : ZoneColor.values()) {
            if (mapColor.getColor().equals(color)) {
                return mapColor;
            }
        }
        return null;
    }

    private ZoneColor getZoneColorByName(String name) {
        for (ZoneColor zoneColor: ZoneColor.values()) {
            if (name != null && name.equals(zoneColor.toString())) {
                return zoneColor;
            }
        }
        return null;
    }


    private void onMapClick(MouseEvent mouseEvent) {
        /*if (stackPane.getChildren().size() >= 3) {
            stackPane.getChildren().remove(2);
        }*/
        final int overlayEffectShift = 5;
        long startTime = System.currentTimeMillis();
        int x = (int) mouseEvent.getX();
        int y = (int) mouseEvent.getY();
        this.removeOverlayPixels();
        Color c = pr.getColor(x, y);
        ZoneColor currColor = getZoneColor(c.toString());
        List<ZoneSquare> zones = zoneSquares.stream()
                .filter((zSq) -> zSq.getColor() == currColor)
                .collect(Collectors.toList());
        Color overlayColor = new Color(0.0d, 0.0d, 0.0d, 0.25d);
        double opacity = 1 - (1 - overlayColor.getOpacity()) * (1 - c.getOpacity());
        double r = overlayColor.getRed() * overlayColor.getOpacity() / opacity + c.getRed() * c.getOpacity() * (1 - overlayColor.getOpacity()) / opacity;
        double g = overlayColor.getGreen() * overlayColor.getOpacity() / opacity + c.getGreen() * c.getOpacity() * (1 - overlayColor.getOpacity()) / opacity;
        double b = overlayColor.getBlue() * overlayColor.getOpacity() / opacity + c.getBlue() * c.getOpacity() * (1 - overlayColor.getOpacity()) / opacity;
        Color mix = new Color(r, g, b, opacity);
        for (ZoneSquare sqr : zones) {
            if (currColor == sqr.getColor() && x >= sqr.getSquare().getStartX() && x <= sqr.getSquare().getEndX()
                    && y >= sqr.getSquare().getStartY() && y <= sqr.getSquare().getEndY()) {
                List<Pixel> overlaidPixels = new ArrayList<>();
                for (int i = sqr.getSquare().getStartX(); i < sqr.getSquare().getEndX(); i++) {
                    for (int j = sqr.getSquare().getStartY(); j < sqr.getSquare().getEndY(); j++) {
                        if (getZoneColor(pr.getColor(i, j).toString()) == currColor) {
                            overlaidPixels.add(new Pixel(i, j));
                            pw.setColor(i, j, overlayColor);
                            for (int k = 1; k < overlayEffectShift; k++) {
                                pw.setColor(i - k, j - k, mix);
                                overlaidPixels.add(new Pixel(i - k, j - k));
                            }
                            pw.setColor(i - overlayEffectShift, j - overlayEffectShift, c);
                            overlaidPixels.add(new Pixel(i - overlayEffectShift, j - overlayEffectShift));

                        }
                    }
                }
                int centerX = sqr.getSquare().getStartX() + sqr.getSquare().getEndX() / 2;
                int centerY = sqr.getSquare().getStartY() + sqr.getSquare().getEndY() / 2;
                String zoneInfo = String.format("%s, center: (%d, %d)", sqr.getName(), centerX, centerY);
                zoneLabel.setText(zoneInfo);
                /*Label l = new Label();
                l.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-background-color: lightblue");
                l.setText(zoneInfo);
                l.toFront();
                l.setTranslateX(centerX);
                l.setTranslateY(centerY);
                stackPane.getChildren().add(l);*/
                overlaidPixelsMap = new HashMap<>();
                overlaidPixelsMap.put(c, overlaidPixels);
                break;

            }
        }
        System.out.println(String.format("Click handling took %d ms", System.currentTimeMillis() - startTime));
    }

    private void removeOverlayPixels() {
        if (overlaidPixelsMap != null) {
            zoneLabel.setText("Zone");

            for (Color c : overlaidPixelsMap.keySet()) {
                Color c1 = new Color(1, 1, 1, 0);
                overlaidPixelsMap.get(c).forEach((pixel) -> {
                    pw.setColor(pixel.getX(), pixel.getY(), c1);
                });
            }
        }
    }
}

