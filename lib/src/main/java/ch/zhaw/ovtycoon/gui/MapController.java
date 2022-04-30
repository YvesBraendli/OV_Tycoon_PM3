package ch.zhaw.ovtycoon.gui;

import ch.zhaw.ovtycoon.gui.model.Action;
import ch.zhaw.ovtycoon.gui.model.ActionButton;
import ch.zhaw.ovtycoon.gui.model.HorizontalStripe;
import ch.zhaw.ovtycoon.gui.model.Pixel;
import ch.zhaw.ovtycoon.gui.model.ZoneSquare;
import ch.zhaw.ovtycoon.gui.model.ZoneTooltip;
import ch.zhaw.ovtycoon.gui.service.ColorService;
import ch.zhaw.ovtycoon.gui.service.MapLoaderService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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

    private final Color transparentColor = Color.TRANSPARENT;
    private final Color overlayColor = new Color(0.0d, 0.0d, 0.0d, 0.25d);
    private final Color neighbourOverlayColor = new Color(1, 1, 1, 0.25d);
    private final int overlayEffectShift = 5;

    private ActionButton actionBtn;
    private PixelWriter pw;
    private PixelWriter mapPw;
    private Map<String, List<Pixel>> overlaidZones = new HashMap<>();
    private List<ZoneSquare> zoneSquares;
    private ZoneSquare source;
    private ZoneSquare target;
    private ZoneSquare currHovered;
    private boolean hoverActive = true;
    private ColorService colorService = new ColorService();
    private MapLoaderService mapLoaderService;

    private List<ZoneSquare> hoverableZones = new ArrayList<>();

    private Map<ZoneSquare, List<ZoneSquare>> zonesWithNeighbours = new HashMap<>();


    @FXML
    public void initialize() {
        mapLoaderService = new MapLoaderService(mapImage);
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        GraphicsContext overlayGc = mapCanvasOverlay.getGraphicsContext2D();
        pw = overlayGc.getPixelWriter();
        mapPw = gc.getPixelWriter();
        zoneSquares = mapLoaderService.initZoneSquaresFromConfig();
        zoneSquares.forEach(zoneSquare -> labelStackPane.getChildren().add(zoneSquare.getTxt()));
        this.addPlayerColorsToZones();
        labelStackPane.setOnMouseClicked(mouseEvent -> onMapClick(mouseEvent));
        labelStackPane.setOnMouseMoved(mouseEvent -> handleMapHover(mouseEvent));
        actionBtn = new ActionButton();
        actionBtn.setAlignment(Pos.BOTTOM_CENTER);
        actionBtn.getStyleClass().add("action-btn");
        this.mapVBox.getChildren().add(actionBtn);
        actionBtn.setOnMouseClicked(event -> onActionButtonClick());
        zonesWithNeighbours = mapNeighboursStringMapToZones(mapLoaderService.getNeighboursMap());
    }

    private void handleMapHover(MouseEvent mouseEvent) {
        if (!hoverActive) return;
        int x = (int) mouseEvent.getX();
        int y = (int) mouseEvent.getY();
        ZoneSquare hoveredZone = getZoneAtCoordinates(x, y);
        if (hoveredZone == null || !hoverableZones.contains(hoveredZone)) {
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
                Color attackerColor = colorService.getColor(source.getColor().getColorAsHexString());
                drawZone(target, attackerColor, mapPw);
            }
            source = null;
            target = null;
            removeAllOverlaidPixels();
            overlayStackPane.setStyle("-fx-background-color: transparent;");
            hoverActive = true;
            actionBtn.setDisable(true);
            hoverableZones = zoneSquares;
        });
        Timeline fightTl = new Timeline(waitingForDiceThrowKf, winnerKf, finishFightKf);
        fightTl.play();
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

    // TODO should depend if move = attack, reinforcement or move troops
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
        ZoneSquare sqr = getZoneAtCoordinates(x, y);
        if (sqr != null && sqr.getBorder() != null) {
            List<Pixel> overlaidPixels = new ArrayList<>();
            if (source == null) {
                source = sqr;
                hoverableZones.clear();
                zonesWithNeighbours.get(sqr).forEach(z -> hoverableZones.add(z));
                hoverableZones.add(source);
                markNeighbours(zonesWithNeighbours.get(sqr));
            } else {
                target = sqr;
                this.overlaidZones.keySet().stream().filter((zone) -> !(zone.equals(source.getName()) || zone.equals(target.getName()))).collect(Collectors.toList()).forEach(zoneToInactivate -> inactivateZone(zoneToInactivate));
                if (labelStackPane.getChildren().size() > zoneSquares.size()) labelStackPane.getChildren().remove(labelStackPane.getChildren().size() - 1);
                hoverActive = false;
                actionBtn.setDisable(false);
                hoverableZones.clear();
            }
            if (source == null) {
                this.setZoneActive(sqr, overlaidPixels, overlayColor, true);
                overlaidZones.put(sqr.getName(), overlaidPixels);
            } else {
                this.drawZone(sqr, colorService.getColor(sqr.getColor().getColorAsHexString()), pw);
                this.setZoneActive(source, overlaidPixels, overlayColor, true);
                overlaidZones.put(source.getName(), overlaidPixels);
            }
            overlayStackPane.setStyle("-fx-background-color: black; -fx-opacity: 0.5;");
        } else {
            source = null;
            target = null;
            this.removeAllOverlaidPixels();
            overlayStackPane.setStyle("-fx-background-color: transparent;");
            hoverableZones = zoneSquares;
        }
        System.out.println(String.format("Click handling took %d ms", System.currentTimeMillis() - startTime));
    }

    private Map<ZoneSquare, List<ZoneSquare>> mapNeighboursStringMapToZones(Map<String, List<String>> stringMap) {
        Map<ZoneSquare, List<ZoneSquare>> res = new HashMap<>();
        stringMap.keySet().forEach((key) -> {
            ZoneSquare center = getZsqByName(key);
            List<ZoneSquare> neighbours = stringMap.get(key).stream().map(zsqStr -> getZsqByName(zsqStr)).collect(Collectors.toList());
            res.put(center, neighbours);
        });
        return res;
    }

    private ZoneSquare getZsqByName(String name) {
        return this.zoneSquares.stream().filter((zsq) -> name.equals(zsq.getName())).findFirst().orElse(null);
    }

    // will be handled by backend in future, only here for testing
    private List<ZoneSquare> getNeighbours(ZoneSquare zoneSquare) {
        if (source == zoneSquare && "Zone 163".equals(zoneSquare.getName())) {
            return zoneSquares.stream().filter((zsq) ->
                    // neighbour zones
                    (zsq.getName().equals("Zone 164") || zsq.getName().equals("Zone 120")
                            || zsq.getName().equals("Zone 160"))
                            && zsq.getColor() != zoneSquare.getColor() // if attack -> zone that can be attacked can't have same color as own zone
            ).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private void markNeighbours(List<ZoneSquare> neighbours) {
        neighbours.forEach((n) -> {
            Color nOverLay = colorService.mixColors(neighbourOverlayColor, colorService.getColor(n.getColor().getColorAsHexString()));
            List<Pixel> overlayPixels = new ArrayList<>();
            setZoneActive(n, overlayPixels, nOverLay, false);
            overlaidZones.put(n.getName(), overlayPixels);
        });
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

    private void removeAllOverlaidPixels() {
        overlaidZones.values().forEach(zone -> zone.forEach(pixel -> pw.setColor(pixel.getX(), pixel.getY(), transparentColor)));
        overlaidZones.clear();
    }

    private void inactivateZone(String zoneName) {
        List<Pixel> overlaidPixelsToRemove = overlaidZones.get(zoneName);
        if (overlaidPixelsToRemove == null) return;
        overlaidPixelsToRemove.forEach((pixel) -> pw.setColor(pixel.getX(), pixel.getY(), transparentColor));
        overlaidZones.remove(zoneName);
    }

    private void setZoneActive(ZoneSquare sqr, List<Pixel> overlaidPixels, Color overlayColor, boolean shift) {
        Color currColor = colorService.getColor(sqr.getColor().getColorAsHexString());
        if (currColor == null) return;
        Color mix = colorService.mixColors(overlayColor, currColor);
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
        sqr.setColor(colorService.getZoneColor(c.toString()));
    }
}

