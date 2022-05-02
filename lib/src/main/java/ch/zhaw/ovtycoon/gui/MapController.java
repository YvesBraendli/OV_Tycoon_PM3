package ch.zhaw.ovtycoon.gui;

import ch.zhaw.ovtycoon.TestBackend;
import ch.zhaw.ovtycoon.gui.model.Action;
import ch.zhaw.ovtycoon.gui.model.ActionButton;
import ch.zhaw.ovtycoon.gui.model.HorizontalStripe;
import ch.zhaw.ovtycoon.gui.model.Pixel;
import ch.zhaw.ovtycoon.gui.model.TroopAmountPopup;
import ch.zhaw.ovtycoon.gui.model.ZoneColor;
import ch.zhaw.ovtycoon.gui.model.ZoneSquare;
import ch.zhaw.ovtycoon.gui.model.ZoneTooltip;
import ch.zhaw.ovtycoon.gui.service.ColorService;
import ch.zhaw.ovtycoon.gui.service.MapLoaderService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
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
    private HBox buttonHBox;
    @FXML
    private VBox playersVBox;

    private Timeline highlightClickableZonesTl = new Timeline();

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
    private ColorService colorService = new ColorService();
    private MapLoaderService mapLoaderService;

    private final Button nextMoveBtn = new Button();

    private boolean mapClickEnabled = true;

    private List<ZoneSquare> hoverableZones = new ArrayList<>();

    private Map<ZoneSquare, List<ZoneSquare>> zonesWithNeighbours = new HashMap<>();

    private TestBackend testBackend = new TestBackend();

    private ChangeListener<Boolean> placeTroopsListener;

    private final List<Label> playerLabels = new ArrayList<>();

    private List<ZoneSquare> clickableZones = new ArrayList<>();


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
        labelStackPane.setOnMouseMoved(mouseEvent -> handleMapHover(mouseEvent));
        actionBtn = new ActionButton();
        actionBtn.setAlignment(Pos.BOTTOM_CENTER);
        actionBtn.getStyleClass().add("action-btn");
        nextMoveBtn.setText("Phase beenden");
        nextMoveBtn.setOnMouseClicked(event -> nextAction());
        this.buttonHBox.getChildren().add(nextMoveBtn);
        actionBtn.setOnMouseClicked(event -> onActionButtonClick());
        zonesWithNeighbours = mapNeighboursStringMapToZones(mapLoaderService.getNeighboursMap());
        hoverableZones = zoneSquares;
        addPlayers();
        highlightCurrPlayerLarge(testBackend.getCurrPlayer());
        showActionChange();
    }

    List<HBox> plyrs = new ArrayList<>();

    private void highlightCurrPlayerLarge(ZoneColor currPlayer) {
        HBox playerBoxLarge = buildAndGetPlayerHBoxBig(currPlayer);
        playerBoxLarge.setTranslateX((labelStackPane.getMaxWidth() - playerBoxLarge.getPrefWidth()) / 2.0d);
        playerBoxLarge.setTranslateY((labelStackPane.getMaxHeight() - playerBoxLarge.getPrefHeight()) / 2.0d);
        KeyFrame showPlayerKf = new KeyFrame(Duration.seconds(1), event -> labelStackPane.getChildren().add(playerBoxLarge));
        KeyFrame removePlayerKf = new KeyFrame(Duration.seconds(3), event -> labelStackPane.getChildren().remove(playerBoxLarge));
        new Timeline(showPlayerKf, removePlayerKf).play();
    }

    private void addPlayers() {
        for (ZoneColor player : testBackend.getPlayers()) {
            HBox playerHBox = buildAndGetPlayerHBox(player);
            plyrs.add(playerHBox);
            playersVBox.getChildren().add(playerHBox);
            if (player == testBackend.getCurrPlayer()) highlightPlayerTile(player.name());
        }

        testBackend.getNextPlayer().addListener((obs, oldVal, newVal) -> {
            highlightCurrPlayerTile(oldVal.name(), newVal.name());
            highlightCurrPlayerLarge(newVal);
        });
    }

    private void highlightPlayerTile(String id) {
        HBox toBeHighlighted = plyrs.stream().filter(box -> id.equals(box.getId())).findFirst().orElse(null);
        if (toBeHighlighted == null) return;
        toBeHighlighted.setPrefWidth(175.0d);
        toBeHighlighted.setPrefHeight(35.0d);
        toBeHighlighted.setMaxWidth(175.0d);
        toBeHighlighted.setMaxHeight(35.0d);

        ImageView iv = (ImageView) toBeHighlighted.getChildren().get(0);
        iv.setFitHeight(toBeHighlighted.getPrefHeight());
        iv.setFitWidth(toBeHighlighted.getPrefHeight());

        Label l = (Label) toBeHighlighted.getChildren().get(1);
        l.setPrefHeight(toBeHighlighted.getPrefHeight());
        l.setPrefWidth(toBeHighlighted.getPrefWidth() - toBeHighlighted.getPrefHeight());
    }

    private void highlightCurrPlayerTile(String idOld, String idNew) {
        HBox toBeUnHighlighted = plyrs.stream().filter(box -> idOld.equals(box.getId())).findFirst().orElse(null);
        if (toBeUnHighlighted == null) return;
        toBeUnHighlighted.setPrefWidth(150.0d);
        toBeUnHighlighted.setPrefHeight(25.0d);
        toBeUnHighlighted.setMaxWidth(150.0d);
        toBeUnHighlighted.setMaxHeight(25.0d);

        ImageView iv = (ImageView) toBeUnHighlighted.getChildren().get(0);
        iv.setFitHeight(toBeUnHighlighted.getPrefHeight());
        iv.setFitWidth(toBeUnHighlighted.getPrefHeight());

        Label l = (Label) toBeUnHighlighted.getChildren().get(1);
        l.setPrefHeight(toBeUnHighlighted.getPrefHeight());
        l.setPrefWidth(toBeUnHighlighted.getPrefWidth() - toBeUnHighlighted.getPrefHeight());
        highlightPlayerTile(idNew);
    }

    private HBox buildAndGetPlayerHBox(ZoneColor player) {
        String playerColor = player.getColorAsHexString().substring(0, 8).replace("0x", "#");
        HBox playerBox = new HBox();
        playerBox.setPrefHeight(25.0d);
        playerBox.setPrefWidth(150.0d);
        playerBox.setMaxHeight(25.0d);
        playerBox.setMaxWidth(150.0d);
        playerBox.setAlignment(Pos.TOP_RIGHT);
        playerBox.setStyle("-fx-border-width: 0.5px; -fx-border-color: black;");

        String imgName = player.name().toLowerCase();
        Image plrImg = new Image(getClass().getClassLoader().getResource(imgName + ".png").toExternalForm());
        ImageView plyrIV = new ImageView();
        plyrIV.setFitHeight(25.0d);
        plyrIV.setFitWidth(25.0d);
        plyrIV.setImage(plrImg);
        playerBox.getChildren().add(plyrIV);

        Label plrLabel = new Label(player.name());
        plrLabel.setPrefHeight(25.0d);
        plrLabel.setPrefWidth(125.0d);
        plrLabel.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white;", playerColor));
        playerBox.getChildren().add(plrLabel);

        playerBox.setId(player.name());
        return playerBox;
    }

    private HBox buildAndGetPlayerHBoxBig(ZoneColor player) {
        String playerColor = player.getColorAsHexString().substring(0, 8).replace("0x", "#");
        HBox playerBox = new HBox();
        playerBox.setPrefHeight(100.0d);
        playerBox.setPrefWidth(500.0d);
        playerBox.setMaxHeight(100.0d);
        playerBox.setMaxWidth(500.0d);
        playerBox.setAlignment(Pos.TOP_RIGHT);
        playerBox.setStyle("-fx-background-color: white;");

        String imgName = player.name().toLowerCase();
        Image plrImg = new Image(getClass().getClassLoader().getResource(imgName + ".png").toExternalForm());
        ImageView plyrIV = new ImageView();
        plyrIV.setFitHeight(100.0d);
        plyrIV.setFitWidth(100.0d);
        plyrIV.setImage(plrImg);
        playerBox.getChildren().add(plyrIV);

        Label plrLabel = new Label(player.name());
        plrLabel.setPrefHeight(100.0d);
        plrLabel.setPrefWidth(400.0d);
        plrLabel.setAlignment(Pos.CENTER);
        plrLabel.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-font-family: Arial; -fx-font-size: 40px;", playerColor));
        playerBox.getChildren().add(plrLabel);

        playerBox.setId(player.name());
        return playerBox;
    }

    private void nextAction() {
        testBackend.nextAction();
        Action next = testBackend.getAction();
        if (next == Action.DEFEND) {
            this.buttonHBox.getChildren().remove(actionBtn);
        } else if (!this.buttonHBox.getChildren().contains(actionBtn)) {
            this.buttonHBox.getChildren().add(0, actionBtn);
        }
        actionBtn.setAction(testBackend.getAction());
        showActionChange();
    }

    private void showActionChange() {
        Label label = new Label();
        mapClickEnabled = false;
        hoverableZones = new ArrayList<>();
        String text = testBackend.getAction().getActionName();
        label.setText(text);
        label.setPrefWidth(400.0d);
        label.setPrefHeight(100.0d);
        label.getStyleClass().add("fight-label");
        label.setTranslateX((labelStackPane.getMaxWidth()  - label.getPrefWidth()) / 2.0d);
        label.setTranslateY((labelStackPane.getMaxHeight()  - label.getPrefHeight()) / 2.0d);
        overlayStackPane.setStyle("-fx-background-color: black; -fx-opacity: 0.5;");

        highlightClickableZonesTl.stop();
        removeAllOverlaidPixels();
        int showActionChangeDurationSeconds = testBackend.getAction() == Action.DEFEND ? 3 : 0;
        int removeActionChangeLabelDurationSeconds = showActionChangeDurationSeconds + 3;
        KeyFrame showActionChangeLabelKf = new KeyFrame(Duration.seconds(showActionChangeDurationSeconds), (event) -> this.labelStackPane.getChildren().add(label));
        KeyFrame removeActionChangeLabelKf = new KeyFrame(Duration.seconds(removeActionChangeLabelDurationSeconds), event -> {
            this.labelStackPane.getChildren().remove(label);
            overlayStackPane.setStyle("-fx-background-color: transparent;");
            if (testBackend.getAction() == Action.DEFEND) {
                reinforcement();
                labelStackPane.setOnMouseClicked(mouseEvent -> reinforcementClickHandler(mouseEvent));
            } else {
                labelStackPane.setOnMouseClicked(mouseEvent -> onMapClick(mouseEvent));
                hoverableZones = zoneSquares;
                mapClickEnabled = true;
                highLightClickableZones();
            }
        });
        Timeline actionChangeTl = new Timeline(showActionChangeLabelKf, removeActionChangeLabelKf);
        actionChangeTl.play();
    }

    private void handleMapHover(MouseEvent mouseEvent) {
        if (hoverableZones.isEmpty()) return;
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
        String name = hoveredZone.getName().replace("Zone", "Zone ");
        ZoneTooltip t = new ZoneTooltip(name);
        t.setTranslateX(hoveredZone.getCenter().getX());
        t.setTranslateY(hoveredZone.getCenter().getY() - 30.0);
        labelStackPane.getChildren().add(t);
    }

    private void onActionButtonClick() {
        actionBtn.setDisable(true);
        mapClickEnabled = false;
        switch (actionBtn.getAction()) {
            case ATTACK: attack(); break;
            case MOVE: moveTroops(0); break;
            default: break;
        }
    }


    private void moveTroops(int minAmount) {
        if (source == null || target == null) return;
        int sourceTroops = Integer.parseInt(source.getTxt().getText());
        TroopAmountPopup troopAmountPopup = new TroopAmountPopup(minAmount, sourceTroops - 1);
        troopAmountPopup.setTranslateX((labelStackPane.getWidth() - troopAmountPopup.getPrefWidth()) / 2.0d);
        troopAmountPopup.setTranslateY((labelStackPane.getHeight() - troopAmountPopup.getPrefHeight()) / 2.0d);
        labelStackPane.getChildren().add(troopAmountPopup);
        troopAmountPopup.getConfirmBtn().setOnMouseClicked(mouseEvent -> {
            // if move: oldAmt + movedAmt, if attack: movedAmt
            int troopAmtNew = minAmount == 0 ? Integer.parseInt(target.getTxt().getText()) + troopAmountPopup.getTroopAmount() : troopAmountPopup.getTroopAmount();
            target.getTxt().setText(Integer.toString(troopAmtNew));
            source.getTxt().setText(Integer.toString(sourceTroops - troopAmountPopup.getTroopAmount()));
            labelStackPane.getChildren().remove(troopAmountPopup);
            mapClickEnabled = true;
            hoverableZones = zoneSquares;
            source = null;
            target = null;
            removeAllOverlaidPixels();
            overlayStackPane.setStyle("-fx-background-color: transparent;");
        });
    }

    private void highLightClickableZones() {
        highlightClickableZonesTl.stop();
        ZoneColor currPlayerColor = testBackend.getCurrPlayer();
        Color mix = colorService.mixColors(neighbourOverlayColor, colorService.getColor(currPlayerColor.getColorAsHexString()));
        updateClickableZones();
        KeyFrame highlightZonesKf = new KeyFrame(Duration.seconds(1), event -> clickableZones.forEach(zone -> setZoneActive(zone, new ArrayList<>(), mix, false)));
        KeyFrame removeHighlightedZonesKf = new KeyFrame(Duration.seconds(2), event -> removeAllOverlaidPixels());
        highlightClickableZonesTl = new Timeline(highlightZonesKf, removeHighlightedZonesKf);
        highlightClickableZonesTl.setCycleCount(30);
        highlightClickableZonesTl.play();
    }

    // TODO should depend on action
    private void updateClickableZones() {
        ZoneColor currPlayerColor = testBackend.getCurrPlayer();
        clickableZones = zoneSquares.stream().filter(zone -> zone.getColor() == currPlayerColor).collect(Collectors.toList());
    }

    private void reinforcement() {
        testBackend.diceThrow();
        placeTroopsListener = (observable, oldValue, newValue) -> {
            if (newValue) {
                testBackend.finishedPlacingTroops().removeListener(placeTroopsListener);
                mapClickEnabled = false;
                nextAction();
            }
        };
        testBackend.finishedPlacingTroops().addListener(placeTroopsListener);
        Label label = new Label();
        label.setText("Warte auf Wuerfelwurf...");
        label.setPrefWidth(400.0d);
        label.setPrefHeight(100.0d);
        label.setMaxHeight(200.0d);
        label.getStyleClass().add("fight-label");
        label.setTranslateX((labelStackPane.getWidth()  - label.getPrefWidth()) / 2.0d);
        label.setTranslateY((labelStackPane.getHeight()  - label.getPrefHeight()) / 2.0d);

        KeyFrame waitingForDiceThrowKf = new KeyFrame(Duration.seconds(0), (event) -> {
            overlayStackPane.setStyle("-fx-background-color: black; -fx-opacity: 0.5;");
            this.labelStackPane.getChildren().add(label);
        });
        KeyFrame troopsReceivedKf = new KeyFrame(Duration.seconds(2), (event -> label.setText(String.format("Du hast %d Truppen erhalten", testBackend.getTroopsToPlace()))));
        KeyFrame setTroopsKf = new KeyFrame(Duration.seconds(4), (event -> label.setText("Waehle die Zone(n), auf welche du die erhaltenen Truppen setzen moechtest")));
        KeyFrame removeLabelKf = new KeyFrame(Duration.seconds(6), event -> {
            labelStackPane.getChildren().remove(label);
            overlayStackPane.setStyle("-fx-background-color: transparent;");
            hoverableZones = zoneSquares;
            mapClickEnabled = true;
            highLightClickableZones();
        });

        Timeline reinforcementTl = new Timeline(waitingForDiceThrowKf, troopsReceivedKf, setTroopsKf, removeLabelKf);
        reinforcementTl.play();
    }

    private void reinforcementClickHandler(MouseEvent mouseEvent) {
        if (!mapClickEnabled) return;
        int x = (int) mouseEvent.getX();
        int y = (int) mouseEvent.getY();
        ZoneSquare sqr = getZoneAtCoordinates(x, y);
        if (sqr == null || !clickableZones.contains(sqr)) return;
        highlightClickableZonesTl.stop();
        removeAllOverlaidPixels();
        mapClickEnabled = false;
        hoverableZones = new ArrayList<>();
        List<Pixel> overlaidPixels = new ArrayList<>();
        int currTroopsAmt = Integer.parseInt(sqr.getTxt().getText());
        overlayStackPane.setStyle("-fx-background-color: black; -fx-opacity: 0.5;");
        setZoneActive(sqr, overlaidPixels, overlayColor, false);
        overlaidZones.put(sqr.getName(), overlaidPixels);
        TroopAmountPopup troopAmountPopup = new TroopAmountPopup(0, testBackend.getTroopsToPlace());
        troopAmountPopup.setTranslateX((labelStackPane.getWidth() - troopAmountPopup.getPrefWidth()) / 2.0d);
        troopAmountPopup.setTranslateY((labelStackPane.getHeight() - troopAmountPopup.getPrefHeight()) / 2.0d);
        labelStackPane.getChildren().add(troopAmountPopup);
        troopAmountPopup.getConfirmBtn().setOnMouseClicked(mE -> {
            sqr.getTxt().setText(Integer.toString(currTroopsAmt + troopAmountPopup.getTroopAmount()));
            testBackend.placeTroops(troopAmountPopup.getTroopAmount());
            labelStackPane.getChildren().remove(troopAmountPopup);
            mapClickEnabled = true;
            hoverableZones = zoneSquares;
            removeAllOverlaidPixels();
            overlayStackPane.setStyle("-fx-background-color: transparent;");
        });
    }

    private void attack() {
        if (source == null || target == null) return;
        testBackend.diceThrow();
        int diceThrowResult = testBackend.getDiceThrowResult();
        Label label = new Label();
        label.setText("Warte auf Wuerfelwurf...");
        label.setPrefWidth(400.0d);
        label.setPrefHeight(100.0d);
        label.getStyleClass().add("fight-label");
        label.setTranslateX((labelStackPane.getWidth()  - label.getPrefWidth()) / 2.0d);
        label.setTranslateY((labelStackPane.getHeight()  - label.getPrefHeight()) / 2.0d);

        String winner = diceThrowResult > 3 ? source.getColor().name() : target.getColor().name();
        String loser =  diceThrowResult > 3 ? target.getColor().name() : source.getColor().name();

        KeyFrame waitingForDiceThrowKf = new KeyFrame(Duration.seconds(0), (event) -> {
            overlayStackPane.setStyle("-fx-background-color: black; -fx-opacity: 0.5;");
            this.labelStackPane.getChildren().add(label);
        });
        KeyFrame winnerKf = new KeyFrame(Duration.seconds(2), (event -> {
            label.setText(String.format("%s hat eine Zone von %s erobert", winner.replace("PLAYER_", ""), loser.replace("PLAYER_", "")));
        }));
        KeyFrame finishFightKf = new KeyFrame(Duration.seconds(4), (event) -> {
            this.labelStackPane.getChildren().remove(label);
            // attacker wins
            if (diceThrowResult > 3) {
                Color attackerColor = colorService.getColor(source.getColor().getColorAsHexString());
                drawZone(target, attackerColor, mapPw);

                moveTroops(1);
            } else {
                mapClickEnabled = true;
                hoverableZones = zoneSquares;
                source = null;
                target = null;
                removeAllOverlaidPixels();
                overlayStackPane.setStyle("-fx-background-color: transparent;");
            }
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
                case 0: clr = colorService.getColor(ZoneColor.PLAYER_GREEN.getColorAsHexString()); break;
                case 1: clr = colorService.getColor(ZoneColor.PLAYER_BLUE.getColorAsHexString()); break;
                default: clr = colorService.getColor(ZoneColor.PLAYER_RED.getColorAsHexString()); break;
            }
            this.drawZone(zoneSquares.get(i), clr, mapPw);
        }
    }

    // TODO check if clickable zones contains clicked zone
    private void onMapClick(MouseEvent mouseEvent) {
        if (!mapClickEnabled) return;
        if (source == null || source != null && target != null) {
            overlayStackPane.setStyle("-fx-background-color: transparent;");
            this.removeAllOverlaidPixels();
            source = null;
            target = null;
        }
        // uncomment for testing
        // long startTime = System.currentTimeMillis();
        int x = (int) mouseEvent.getX();
        int y = (int) mouseEvent.getY();
        ZoneSquare sqr = getZoneAtCoordinates(x, y);
        if (sqr != null && sqr.getBorder() != null) {
            highlightClickableZonesTl.stop();
            removeAllOverlaidPixels();
            List<Pixel> overlaidPixels = new ArrayList<>();
            if (source == null || sqr == source) {
                source = sqr;
                List< ZoneSquare> validTargets = getValidZonesForAction(testBackend.getAction());
                hoverableZones = new ArrayList<>(validTargets);
                hoverableZones.add(source);
                markNeighbours(validTargets);
                this.setZoneActive(sqr, overlaidPixels, overlayColor, true);
                overlaidZones.put(sqr.getName(), overlaidPixels);
            } else {
                if (!getValidZonesForAction(testBackend.getAction()).contains(sqr)) {
                    System.out.println("You cannot click on " + sqr.getName());
                    return;
                }
                target = sqr;
                this.overlaidZones.keySet().stream().filter((zone) -> !(zone.equals(source.getName()) || zone.equals(target.getName()))).collect(Collectors.toList()).forEach(zoneToInactivate -> inactivateZone(zoneToInactivate));
                if (labelStackPane.getChildren().size() > zoneSquares.size()) labelStackPane.getChildren().remove(labelStackPane.getChildren().size() - 1);
                actionBtn.setDisable(false);
                this.drawZone(sqr, colorService.getColor(sqr.getColor().getColorAsHexString()), pw);
                this.setZoneActive(source, overlaidPixels, overlayColor, true);
                overlaidZones.put(source.getName(), overlaidPixels);
                hoverableZones = new ArrayList<>();
            }
            overlayStackPane.setStyle("-fx-background-color: black; -fx-opacity: 0.5;");
        } else {
            source = null;
            target = null;
            this.removeAllOverlaidPixels();
            overlayStackPane.setStyle("-fx-background-color: transparent;");
            hoverableZones = zoneSquares;
        }
        // uncomment for testing
        // System.out.println(String.format("Click handling took %d ms", System.currentTimeMillis() - startTime));
    }

    // TODO fix npe
    public List<ZoneSquare> getValidZonesForAction(Action action) {
        switch (action) {
            case ATTACK:
                return zonesWithNeighbours.get(source).stream()
                        .filter(neighbour -> neighbour.getColor() != source.getColor())
                        .collect(Collectors.toList());
            case MOVE:
                return zonesWithNeighbours.get(source).stream()
                        .filter(neighbour -> neighbour.getColor() == source.getColor())
                        .collect(Collectors.toList());
            case DEFEND:
                return new ArrayList<>(zonesWithNeighbours.keySet());
            default:
                return new ArrayList<>();
        }
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
        overlaidZones.put(sqr.getName(), overlaidPixels);
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

