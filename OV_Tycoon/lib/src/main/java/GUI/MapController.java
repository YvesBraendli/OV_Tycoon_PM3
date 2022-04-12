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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
    private Canvas mapCanvasOverlay;
    @FXML
    private StackPane overlayStackPane;
    private PixelWriter pw;
    private PixelWriter mapPw;
    private PixelReader pr;
    private Map<String, List<Pixel>> overlaidZones;
    private final int overlayEffectShift = 5;
    private final Color overlayColor = new Color(0.0d, 0.0d, 0.0d, 0.25d);
    private final Color neighbourOverlayColor = new Color(0.57d, 0.97d, 0.64d, 1.0d);
    private boolean showPlayerColors = false;


    @FXML
    public void initialize() {
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        GraphicsContext overlayGc = mapCanvasOverlay.getGraphicsContext2D();
        pw = overlayGc.getPixelWriter();
        mapPw = gc.getPixelWriter();
        initZoneSquares();
        addMapClickHandler();
        ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<List<HorizontalStripe>>> stripes = new ArrayList<>();
        long start = System.currentTimeMillis();
        this.zoneSquares.forEach(zsq -> stripes.add(es.submit(() -> getXBorderStripes(zsq))));
        stripes.forEach((s) -> {
            try {
                s.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
        System.out.println(String.format("Finished stripe init in %d ms", System.currentTimeMillis() - start));
    }

    private void initZoneSquares() {
        // startX, offsetX, startY, offsetY, color, name
        Pattern zoneSquareData = Pattern.compile("sX=([0-9]+), oX=([0-9]+), sY=([0-9]+), oY=([0-9]+), color:([^,]+), name=([^;]+);");
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
    }

    @FXML
    private void startGame() {
        for (int i = 0; i < zoneSquares.size(); i++) {
            Color clr = i < 5 ? Color.BLUE :  Color.RED;
            this.drawZone(zoneSquares.get(i), clr, mapPw);
        }
        showPlayerColors = true;
    }

    // TODO should depend if move = attack, reinforcement or move trains
    private void onMapClick(MouseEvent mouseEvent) {
        overlayStackPane.setStyle("-fx-background-color: transparent;");
        long startTime = System.currentTimeMillis();
        int x = (int) mouseEvent.getX();
        int y = (int) mouseEvent.getY();
        this.removeOverlayPixels();
        overlaidZones = new HashMap<>();
        boolean markNeighbours = true;
        ZoneSquare sqr = getClickedZone(x, y);
        if (sqr.getBorder() != null) {
            List<Pixel> overlaidPixels = new ArrayList<>();
            this.setZoneActive(sqr, overlaidPixels, overlayColor, true);
            int centerX = sqr.getSquare().getStartX() + sqr.getSquare().getEndX() / 2;
            int centerY = sqr.getSquare().getStartY() + sqr.getSquare().getEndY() / 2;
            String zoneInfo = String.format("%s, center: (%d, %d)", sqr.getName(), centerX, centerY);
            zoneLabel.setText(zoneInfo);
            overlaidZones.put(sqr.getName(), overlaidPixels);
            // TODO ev just mark borders -> currently takes to much time (ca. 0.3s)
            if (markNeighbours && "Zone 121".equals(sqr.getName())) {
                List<ZoneSquare> neighbours = zoneSquares.stream().filter((zsq) ->
                        // neighbour zones
                        (zsq.getName().equals("Zone 122") || zsq.getName().equals("Zone 110") || zsq.getName().equals("Zone 130"))
                        && zsq.getColor() != sqr.getColor() // if attack -> zone that can be attacked can't have same color as own zone
                ).collect(Collectors.toList());
                neighbours.forEach((n) -> {
                    List<Pixel> overlayPixels = new ArrayList<>();
                    setZoneActive(n, overlayPixels, neighbourOverlayColor, false);
                    overlaidZones.put(n.getName(), overlayPixels);
                });
            }
            // TODO ev add css blur
            overlayStackPane.setStyle("-fx-background-color: black; -fx-opacity: 0.5;");
        }
        System.out.println(String.format("Click handling took %d ms", System.currentTimeMillis() - startTime));
    }

    private ZoneSquare getClickedZone(int x, int y) {
        long start = System.currentTimeMillis();
        List<ZoneSquare> containsY = this.zoneSquares.stream()
                .filter((z) ->
                        z.getBorder().stream().map((str) -> str.getY()).collect(Collectors.toList()).contains(y)
                ).collect(Collectors.toList());
        ZoneSquare res = containsY.stream()
                .filter(z -> z.getBorder().stream()
                        .anyMatch(st -> st.getY() == y && st.getStartX() <= x && st.getEndX() >= x))
                .findFirst().orElse(new ZoneSquare());
        System.out.println(String.format("Finding sqr %s took %d ms", res.getName(), System.currentTimeMillis() - start));
        return res;
    }

    private List<Pixel> getBorder(ZoneSquare zsq) {
        List<Pixel> borderPixels = new ArrayList<>();
        ZoneColor zoneColor = zsq.getColor();
        for (HorizontalStripe str : zsq.getBorder()) {
            int y = str.getY();
            for (int j = str.getStartX(); j <= str.getEndX(); j++) {
                if (isBorder(j, y, zoneColor)) {
                    borderPixels.add(new Pixel(j, y));
                }
            }
        }
        return borderPixels;
    }

    private List<HorizontalStripe> getXBorderStripes(ZoneSquare zsq) {
        Square sqr = zsq.getSquare();
        ZoneColor zoneColor = zsq.getColor();
        List<HorizontalStripe> stripes = new ArrayList<>();
        for (int i = sqr.getStartY(); i < sqr.getEndY(); i++) {
            HorizontalStripe currStripe = new HorizontalStripe();
            for (int j = sqr.getStartX(); j < sqr.getEndX(); j++) {
                if (isZoneColor(j, i, zoneColor) && isXBorder(j, i, zoneColor)) {
                    if (currStripe.getStartX() == 0){
                        currStripe.setStartX(j);
                        currStripe.setY(i);
                    }
                    else if (currStripe.getEndX() == 0) {
                        currStripe.setEndX(j);
                        stripes.add(currStripe);
                    }
                    else {
                        currStripe = new HorizontalStripe();
                        currStripe.setStartX(j);
                        currStripe.setY(i);
                    }
                }
            }
        }
        zsq.setBorder(stripes);
        return stripes;
    }

    private boolean isXBorder(int x, int y, ZoneColor zoneColor) {
        return !isZoneColor(x - 1, y, zoneColor) || !isZoneColor(x + 1, y, zoneColor);
    }


    private boolean isBorder(int x, int y, ZoneColor zoneColor) {
        for (int i = -1; i <= 1 ; i++) {
            for (int j = -1; j <= 1 ; j++) {
                if (i == 0 && j == 0) {
                    continue;
                }
                if (!isZoneColor(x + i, y + j, zoneColor)){
                    return true;
                }
            }

        }
        return false;
    }

    private boolean isZoneColor(int x, int y, ZoneColor zoneColor) {
        return zoneColor == getZoneColor(pr.getColor(x, y).toString());
    }

    private void addMapClickHandler() {
        pr = img.getPixelReader();
        mapCanvasOverlay.setOnMouseClicked(e -> {
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

    private void removeOverlayPixels() {
        if (overlaidZones != null) {
            zoneLabel.setText("Zone");
            for (String c : overlaidZones.keySet()) {
                Color c1 = new Color(1, 1, 1, 0);
                overlaidZones.get(c).forEach((pixel) -> {
                    pw.setColor(pixel.getX(), pixel.getY(), c1);
                });
            }
        }
    }

    private void setZoneActive(ZoneSquare sqr, List<Pixel> overlaidPixels, Color overlayColor, boolean shift) {
        Color currColor = null;
        Color mix = null;
        Color overlay = overlayColor;
        for (HorizontalStripe str : sqr.getBorder()) {
            int y = str.getY();
            for (int x = str.getStartX(); x <= str.getEndX(); x++) {
                // TODO refactor
                if (currColor == null) {
                    if (showPlayerColors) {
                        currColor = sqr.getColor() == ZoneColor.PLAYER_BLUE ? Color.BLUE : Color.RED;
                    }
                    else {
                        currColor = pr.getColor(x, y);
                    }
                }
                if (currColor != null && mix == null) mix = mixColors(overlay, currColor);
                overlaidPixels.add(new Pixel(x, y));
                pw.setColor(x, y, overlay);
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
        Pattern clrPattern = Pattern.compile("0x([0-9a-f]{2})([0-9a-f]{2})([0-9a-f]{2})([0-9a-f]{2})");
        Matcher matcher = clrPattern.matcher(hex);
        double[] clrParams = new double[4];
        if (!matcher.find() || matcher.groupCount() != 4) return Color.BLACK;
        for (int i = 0; i < matcher.groupCount(); i++) {
            clrParams[i] = Integer.parseInt(matcher.group(i + 1), 16) / 255.0d;
        }
        return new Color(clrParams[0], clrParams[1], clrParams[2], clrParams[3]);
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

