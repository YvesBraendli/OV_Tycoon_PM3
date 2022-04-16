package GUI;

import GUI.model.HorizontalStripe;
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
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MapController {
    @FXML
    private Label zoneLabel;
    @FXML
    private Image mapImage;
    @FXML
    private Canvas mapCanvas;
    @FXML
    private Canvas mapCanvasOverlay;
    @FXML
    private StackPane overlayStackPane;
    private PixelWriter pw;
    private PixelWriter mapPw;
    private PixelReader pr;
    private Map<String, List<Pixel>> overlaidZones;
    private final List<ZoneSquare> zoneSquares = new ArrayList<>();
    private final Color transparentColor = Color.TRANSPARENT;
    private final Color overlayColor = new Color(0.0d, 0.0d, 0.0d, 0.25d);
    private final Color neighbourOverlayColor = new Color(1, 1, 1, 0.25d);
    private final int overlayEffectShift = 5;


    @FXML
    public void initialize() {
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        GraphicsContext overlayGc = mapCanvasOverlay.getGraphicsContext2D();
        pw = overlayGc.getPixelWriter();
        mapPw = gc.getPixelWriter();
        initZoneSquares();
        addMapClickHandler();
        long start = System.currentTimeMillis();
        this.zoneSquares.forEach(zsq -> getXBorderStripes(zsq)); // TODO why faster without multithreading?
        System.out.println(String.format("Finished stripe init in %d ms", System.currentTimeMillis() - start));
        this.addPlayerColorsToZones();
    }

    private void initZoneSquares() {
        // startX, offsetX, startY, offsetY, color, name
        Pattern zoneSquareData = Pattern.compile("sX=([0-9]+), oX=([0-9]+), sY=([0-9]+), oY=([0-9]+), color=([^,]+), name=([^;]+);");
        final int dataGroupsCount = 6;
        try {
            String dir = System.getProperty("user.dir");
            if (!dir.contains("lib")) {
                dir += "\\lib";
            }
            String zonesTxtPath = dir + "\\src\\main\\resources\\zones.txt";
            File zones = new File(zonesTxtPath);
            try (BufferedReader br = new BufferedReader(new FileReader(zones))) {
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
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void addPlayerColorsToZones() {
        Random random = new Random();
        for (int i = 0; i < zoneSquares.size(); i++) {
            int randomInt = random.nextInt(3);
            Color clr = null;
            switch (randomInt) {
                case 0: clr = Color.GREENYELLOW; break;
                case 1: clr = Color.BLUE; break;
                default: clr = Color.RED; break;
            }
            this.drawZone(zoneSquares.get(i), clr, mapPw);
        }
    }

    // TODO should depend if move = attack, reinforcement or move trains
    private void onMapClick(MouseEvent mouseEvent) {
        overlayStackPane.setStyle("-fx-background-color: transparent;");
        long startTime = System.currentTimeMillis();
        int x = (int) mouseEvent.getX();
        int y = (int) mouseEvent.getY();
        this.removeAllOverlaidPixels();
        overlaidZones = new HashMap<>();
        boolean markNeighbours = true;
        ZoneSquare sqr = getClickedZone(x, y);
        if (sqr.getBorder() != null) {
            List<Pixel> overlaidPixels = new ArrayList<>();
            if (markNeighbours && "Zone 163".equals(sqr.getName())) {
                List<ZoneSquare> neighbours = zoneSquares.stream().filter((zsq) ->
                        // neighbour zones
                        (zsq.getName().equals("Zone 164") || zsq.getName().equals("Zone 120")
                                || zsq.getName().equals("Zone 160"))
                        && zsq.getColor() != sqr.getColor() // if attack -> zone that can be attacked can't have same color as own zone
                ).collect(Collectors.toList());
                neighbours.forEach((n) -> {
                    Color nOverLay = mixColors(neighbourOverlayColor, getColor(n.getColor().getColorAsHexString()));
                    List<Pixel> overlayPixels = new ArrayList<>();
                    setZoneActive(n, overlayPixels, nOverLay, false);
                    overlaidZones.put(n.getName(), overlayPixels);
                });
            }
            this.setZoneActive(sqr, overlaidPixels, overlayColor, true);
            String zoneInfo = sqr.getName();
            zoneLabel.setText(zoneInfo);
            overlaidZones.put(sqr.getName(), overlaidPixels);
            overlayStackPane.setStyle("-fx-background-color: black; -fx-opacity: 0.5;");
        }
        System.out.println(String.format("Click handling took %d ms", System.currentTimeMillis() - startTime));
    }

    private ZoneSquare getClickedZone(int x, int y) {
        List<ZoneSquare> containsY = this.zoneSquares.stream()
                .filter((z) ->
                        z.getBorder().stream().map((str) -> str.getY()).collect(Collectors.toList()).contains(y)
                ).collect(Collectors.toList());
        ZoneSquare clickedZone = containsY.stream()
                .filter(z -> z.getBorder().stream()
                        .anyMatch(st -> st.getY() == y && st.getStartX() <= x && st.getEndX() >= x))
                .findFirst().orElse(new ZoneSquare());
        return clickedZone;
    }

    // TODO current impl doesnt work if !mapClr(i - 1) mapClr(i) !mapClr(i+1)
    private List<HorizontalStripe> getXBorderStripes(ZoneSquare zsq) {
        Square sqr = zsq.getSquare();
        ZoneColor zoneColor = zsq.getColor();
        List<HorizontalStripe> stripes = new ArrayList<>();
        for (int i = sqr.getStartY(); i <= sqr.getEndY(); i++) { // TODO check if index out of bounds exception possible here
            HorizontalStripe currStripe = new HorizontalStripe();

            boolean enteredZone = false;

            boolean prev = false;

            for (int j = sqr.getStartX(); j <= sqr.getEndX(); j++) {
                prev = enteredZone;
                enteredZone = isZoneColor(j, i, zoneColor);
                if (enteredZone && !prev) {
                    currStripe.setStartX(j);
                    currStripe.setY(i);
                }
                else if (!enteredZone && prev) {
                    currStripe.setEndX(j - 1); // prev was last pixel in zone
                    stripes.add(currStripe);
                    currStripe = new HorizontalStripe(); // if stripe intersected
                }
            }
        }
        zsq.setBorder(stripes);
        return stripes;
    }

    private boolean isZoneColor(int x, int y, ZoneColor zoneColor) {
        return zoneColor == getZoneColor(pr.getColor(x, y).toString());
    }

    private void addMapClickHandler() {
        pr = mapImage.getPixelReader();
        mapCanvasOverlay.setOnMouseClicked(mouseEvent -> this.onMapClick(mouseEvent));
    }

    private ZoneColor getZoneColor(String color) {
        for (ZoneColor mapColor : ZoneColor.values()) {
            if (mapColor.getColorAsHexString().equals(color)) {
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

    private void removeAllOverlaidPixels() {
        if (overlaidZones == null) return;
        zoneLabel.setText("Zone");
        overlaidZones.values().forEach(zone -> zone.forEach(pixel -> pw.setColor(pixel.getX(), pixel.getY(), transparentColor)));
    }

    private void setZoneActive(ZoneSquare sqr, List<Pixel> overlaidPixels, Color overlayColor, boolean shift) {
        Color currColor = getColor(sqr.getColor().getColorAsHexString());
        if (currColor == null) return;
        Color mix = mixColors(overlayColor, currColor);
        for (HorizontalStripe stripe : sqr.getBorder()) {
            int y = stripe.getY();
            for (int x = stripe.getStartX(); x <= stripe.getEndX(); x++) {
                overlaidPixels.add(new Pixel(x, y));
                pw.setColor(x, y, overlayColor);
                if (shift) {
                    for (int k = 1; k < overlayEffectShift; k++) {
                        pw.setColor(x - k, y - k, mix);
                        overlaidPixels.add(new Pixel(x - k, y - k));
                    }
                    pw.setColor(x - overlayEffectShift, y - overlayEffectShift, currColor);
                    overlaidPixels.add(new Pixel(x - overlayEffectShift, y - overlayEffectShift));
                }

            }
        }
    }

    private Color getColor(String hex) {
        Pattern hexColor = Pattern.compile("^0x([0-9a-f]{2})([0-9a-f]{2})([0-9a-f]{2})([0-9a-f]{2})$");
        Matcher matcher = hexColor.matcher(hex);
        double[] colorParams = new double[4];
        if (!matcher.find() || matcher.groupCount() != 4) return null;
        for (int i = 0; i < matcher.groupCount(); i++) {
            colorParams[i] = Integer.parseInt(matcher.group(i + 1), 16) / 255.0d;
        }
        return new Color(colorParams[0], colorParams[1], colorParams[2], colorParams[3]);
    }

    private void drawZone(ZoneSquare sqr, Color c, PixelWriter pw) {
        for (HorizontalStripe str : sqr.getBorder()) {
            int y = str.getY();
            for (int x = str.getStartX(); x <= str.getEndX(); x++) {
                pw.setColor(x, y, c);
            }
        }
        sqr.setColor(getZoneColor(c.toString()));
    }

    private Color mixColors(Color foregroundColor, Color backgroundColor) {
        double opacity = 1 - (1 - foregroundColor.getOpacity()) * (1 - backgroundColor.getOpacity());
        double r = foregroundColor.getRed() * foregroundColor.getOpacity() / opacity + backgroundColor.getRed() * backgroundColor.getOpacity() * (1 - foregroundColor.getOpacity()) / opacity;
        double g = foregroundColor.getGreen() * foregroundColor.getOpacity() / opacity + backgroundColor.getGreen() * backgroundColor.getOpacity() * (1 - foregroundColor.getOpacity()) / opacity;
        double b = foregroundColor.getBlue() * foregroundColor.getOpacity() / opacity + backgroundColor.getBlue() * backgroundColor.getOpacity() * (1 - foregroundColor.getOpacity()) / opacity;
        return new Color(r, g, b, opacity);
    }
}

