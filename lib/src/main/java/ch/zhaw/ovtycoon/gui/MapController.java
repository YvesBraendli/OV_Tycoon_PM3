package ch.zhaw.ovtycoon.gui;

import ch.zhaw.ovtycoon.gui.model.Action;
import ch.zhaw.ovtycoon.gui.model.ActionButton;
import ch.zhaw.ovtycoon.gui.model.HorizontalStripe;
import ch.zhaw.ovtycoon.gui.model.Pixel;
import ch.zhaw.ovtycoon.gui.model.Square;
import ch.zhaw.ovtycoon.gui.model.ZoneColor;
import ch.zhaw.ovtycoon.gui.model.ZoneSquare;
import ch.zhaw.ovtycoon.gui.model.ZoneTooltip;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

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
    private Image mapImage;
    @FXML
    private Canvas mapCanvas;
    @FXML
    private Canvas mapCanvasOverlay;
    @FXML
    private StackPane overlayStackPane;
    @FXML
    private StackPane labelStackPane;
    @FXML
    private VBox mapVBox;
    private ActionButton actionBtn;
    private PixelWriter pw;
    private PixelWriter mapPw;
    private PixelReader pr;
    private Map<String, List<Pixel>> overlaidZones = new HashMap<>();
    private final List<ZoneSquare> zoneSquares = new ArrayList<>();
    private final Color transparentColor = Color.TRANSPARENT;
    private final Color overlayColor = new Color(0.0d, 0.0d, 0.0d, 0.25d);
    private final Color neighbourOverlayColor = new Color(1, 1, 1, 0.25d);
    private final int overlayEffectShift = 5;
    private boolean firstSelect = false;
    private ZoneSquare source;
    private ZoneSquare target;
    private ZoneSquare currHovered;
    private boolean hoverActive = true;


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
        this.addHoverHandler();
        actionBtn = new ActionButton();
        actionBtn.setAlignment(Pos.BOTTOM_CENTER);
        actionBtn.getStyleClass().add("action-btn");
        this.mapVBox.getChildren().add(actionBtn);
        actionBtn.setOnMouseClicked(event -> onActionButtonClick());
    }

    private void addHoverHandler() {
        labelStackPane.setOnMouseMoved((mouseEvent) -> {
            handleHover(mouseEvent);
        });
    }

    private void handleHover(MouseEvent mouseEvent) {
        if (!hoverActive) return;
        int x = (int) mouseEvent.getX();
        int y = (int) mouseEvent.getY();
        ZoneSquare hoveredZone = getZoneAtCoordinates(x, y);
        if (hoveredZone == null) {
            if (this.labelStackPane.getChildren().size() > this.zoneSquares.size()) {
                this.labelStackPane.getChildren().remove(this.labelStackPane.getChildren().size() - 1);
                currHovered = null;
            }
            return;
        } else if (hoveredZone.equals(currHovered)) {
            return;
        }
        if (currHovered != null && this.labelStackPane.getChildren().size() > this.zoneSquares.size()) labelStackPane.getChildren().remove(labelStackPane.getChildren().size() - 1);
        currHovered = hoveredZone;
        ZoneTooltip t = new ZoneTooltip(hoveredZone.getName());
        t.setTranslateX(hoveredZone.getCenter().getX());
        t.setTranslateY(hoveredZone.getCenter().getY() - 30.0);
        labelStackPane.getChildren().add(t);
    }

    private void onActionButtonClick() {
        if (actionBtn.getAction() != Action.ATTACK) return; // only attack implemented currently
        if (source == null || target == null) return;
        int number = new Random().nextInt(6) + 1;

        Label fightLabel = new Label();
        fightLabel.setText("Waiting for dice throw...");
        fightLabel.setPrefWidth(400.0d);
        fightLabel.setPrefHeight(100.0d);
        fightLabel.getStyleClass().add("fight-label");
        fightLabel.setTranslateX((labelStackPane.getWidth()  - fightLabel.getPrefWidth()) / 2.0d);
        fightLabel.setTranslateY((labelStackPane.getHeight()  - fightLabel.getPrefHeight()) / 2.0d);

        String winner = number > 3 ? source.getColor().name() : target.getColor().name();
        String loser =  number > 3 ? target.getColor().name() : source.getColor().name();

        KeyFrame waitingForDiceThrowKf = new KeyFrame(Duration.seconds(0), (event) -> {
           this.labelStackPane.getChildren().add(fightLabel);
        });
        KeyFrame winnerKf = new KeyFrame(Duration.seconds(2), (event -> {
            fightLabel.setText(String.format("%s defeated %s", winner.replace("PLAYER_", ""), loser.replace("PLAYER_", "")));
        }));
        KeyFrame finishFightKf = new KeyFrame(Duration.seconds(4), (event) -> {
            this.labelStackPane.getChildren().remove(fightLabel);
            if (number > 3) {
                // attacker wins
                Color attackerColor = getColor(source.getColor().getColorAsHexString());
                drawZone(target, attackerColor, mapPw);
            } else {
                // defender wins
                Color defenderColor = getColor(target.getColor().getColorAsHexString());
                drawZone(source, defenderColor, mapPw);
            }
            source = null;
            target = null;
            removeAllOverlaidPixels();
            overlayStackPane.setStyle("-fx-background-color: transparent;");
            hoverActive = true;
        });
        Timeline fightTl = new Timeline(waitingForDiceThrowKf, winnerKf, finishFightKf);
        fightTl.play();
    }

    private void initZoneSquares() {
        // startX, offsetX, startY, offsetY, color, name
        Pattern zoneSquareData = Pattern.compile("sX=([0-9]+), oX=([0-9]+), sY=([0-9]+), oY=([0-9]+), color=([^,]+), name=([^;]+), center=\\(([0-9]+,[0-9]+)\\);");
        final int dataGroupsCount = 7;
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
                        String center = matcher.group(7);
                        String[] centerCoordinates = center.split(",");
                        zoneSquare.setCenter(new Pixel(Integer.parseInt(centerCoordinates[0]), Integer.parseInt(centerCoordinates[1])));
                        Text txt = new Text();
                        txt.setStyle("-fx-fill: lightgray;-fx-font-weight: bold;");
                        txt.setText("0");
                        txt.setTranslateX(zoneSquare.getCenter().getX());
                        txt.setTranslateY(zoneSquare.getCenter().getY());
                        zoneSquare.setTxt(txt);
                        labelStackPane.getChildren().add(txt);
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
        if (source == null || source != null && target != null) {
            overlayStackPane.setStyle("-fx-background-color: transparent;");
            this.removeAllOverlaidPixels();
            source = null;
            target = null;
        }
        long startTime = System.currentTimeMillis();
        int x = (int) mouseEvent.getX();
        int y = (int) mouseEvent.getY();
        boolean markNeighbours = false;
        ZoneSquare sqr = getZoneAtCoordinates(x, y);
        if (sqr != null && sqr.getBorder() != null) {
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
            if (!firstSelect) {
                this.setZoneActive(sqr, overlaidPixels, overlayColor, true);
                overlaidZones.put(sqr.getName(), overlaidPixels);
            } else {
                this.drawZone(sqr, getColor(sqr.getColor().getColorAsHexString()), pw);
                this.setZoneActive(source, overlaidPixels, overlayColor, true);
                overlaidZones.put(source.getName(), overlaidPixels);
            }
            overlayStackPane.setStyle("-fx-background-color: black; -fx-opacity: 0.5;");
            if (source == null) {
                source = sqr;
            } else {
                target = sqr;
                if (labelStackPane.getChildren().size() > zoneSquares.size()) labelStackPane.getChildren().remove(labelStackPane.getChildren().size() - 1);
                hoverActive = false;
            }
            firstSelect = !firstSelect;
        } else {
            source = null;
            target = null;
            this.removeAllOverlaidPixels();
            overlayStackPane.setStyle("-fx-background-color: transparent;");
            firstSelect = false;
        }
        System.out.println(String.format("Click handling took %d ms", System.currentTimeMillis() - startTime));
    }

    private ZoneSquare getZoneAtCoordinates(int x, int y) {
        List<ZoneSquare> containsY = this.zoneSquares.stream()
                .filter((z) ->
                        z.getBorder().stream().map((str) -> str.getY()).collect(Collectors.toList()).contains(y)
                ).collect(Collectors.toList());
        ZoneSquare clickedZone = containsY.stream()
                .filter(z -> z.getBorder().stream()
                        .anyMatch(st -> st.getY() == y && st.getStartX() <= x && st.getEndX() >= x))
                .findFirst().orElse(null);
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
        labelStackPane.setOnMouseClicked(mouseEvent -> this.onMapClick(mouseEvent));
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
        overlaidZones.values().forEach(zone -> zone.forEach(pixel -> pw.setColor(pixel.getX(), pixel.getY(), transparentColor)));
        overlaidZones.clear();
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
        boolean drawingOverlay = pw == this.pw;
        if (drawingOverlay) overlaidZones.put(sqr.getName(), new ArrayList<>());
        for (HorizontalStripe str : sqr.getBorder()) {
            int y = str.getY();
            for (int x = str.getStartX(); x <= str.getEndX(); x++) {
                pw.setColor(x, y, c);
                if (drawingOverlay) {
                    overlaidZones.get(sqr.getName()).add(new Pixel(x, y));
                }
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

