package ch.zhaw.ovtycoon.gui;

import ch.zhaw.ovtycoon.TestBackend;
import ch.zhaw.ovtycoon.gui.model.Action;
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
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
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
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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
    private VBox playersVBox;
    @FXML
    private Button actionBtn;
    @FXML
    private Button nextMoveBtn;

    private final Color transparentColor = Color.TRANSPARENT;
    private final Color overlayColor = new Color(0.0d, 0.0d, 0.0d, 0.25d);
    private final Color neighbourOverlayColor = new Color(1, 1, 1, 0.25d);
    private final List<HBox> playersListItems = new ArrayList<>();
    private Timeline highlightClickableZonesTl = new Timeline();
    private PixelWriter pw;
    private PixelWriter mapPw;
    private SimpleBooleanProperty sourceOrTargetNull = new SimpleBooleanProperty(true);
    private SimpleBooleanProperty showingAnimation = new SimpleBooleanProperty(false);
    private SimpleBooleanProperty actionButtonVisible = new SimpleBooleanProperty(false);
    private SimpleBooleanProperty showingPopup = new SimpleBooleanProperty(false);

    private final int overlayEffectShift = 5;
    private List<ZoneSquare> zoneSquares;
    private Map<ZoneSquare, List<ZoneSquare>> zonesWithNeighbours = new HashMap<>();
    private Map<String, List<Pixel>> overlaidZones = new HashMap<>();

    private List<ZoneSquare> clickableZones = new ArrayList<>();
    private List<ZoneSquare> hoverableZones = new ArrayList<>();
    private boolean mapClickEnabled = true;

    private ZoneSquare source;
    private ZoneSquare target;
    private ZoneSquare currHovered;
    private TestBackend testBackend = new TestBackend();
    private ColorService colorService = new ColorService();
    private MapLoaderService mapLoaderService;

    private final String moveTroopsText = "Du kannst %d - %d Truppen verschieben";
    private final String reinforcementText = "Du kannst %d - %d Truppen setzen";
    private final String attackerText = "Du kannst mit %d - %d Truppen angreifen";
    private final String defenderText = "Du kannst dich mit %d - %d Truppen verteidigen";


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
        nextMoveBtn.setText("Phase beenden");
        nextMoveBtn.setOnMouseClicked(event -> nextAction());
        actionBtn.setOnMouseClicked(event -> onActionButtonClick());
        nextMoveBtn.disableProperty().bind(showingAnimation.or(showingPopup));
        actionBtn.visibleProperty().bind(actionButtonVisible);
        actionBtn.disableProperty().bindBidirectional(sourceOrTargetNull);
        zonesWithNeighbours = mapNeighboursStringMapToZones(mapLoaderService.getNeighboursMap());
        hoverableZones = zoneSquares;
        addPlayers();
        highlightCurrPlayerLarge(testBackend.getCurrPlayer());
        showActionChange();
    }

    private void highlightCurrPlayerLarge(ZoneColor currPlayer) {
        HBox playerBoxLarge = buildAndGetPlayerHBoxBig(currPlayer);
        centerJavaFXRegion(labelStackPane, playerBoxLarge);
        KeyFrame showPlayerKf = new KeyFrame(Duration.seconds(0), event -> labelStackPane.getChildren().add(playerBoxLarge));
        KeyFrame removePlayerKf = new KeyFrame(Duration.seconds(3), event -> labelStackPane.getChildren().remove(playerBoxLarge));
        Timeline highlightPlayerTl = new Timeline(showPlayerKf, removePlayerKf);
        playAnimation(highlightPlayerTl, true);
    }

    private void stopAnimation(Timeline tlPlaying) {
        tlPlaying.stop();
        // showingAnimation.set(false);
    }

    private void playAnimation(Timeline tlToPlay, boolean isBlocking) {
        final EventHandler<ActionEvent> finishedPlayingAnimation = fin -> {
          if (isBlocking) {
              showingAnimation.set(false);
          }
          tlToPlay.setOnFinished(null);
        };
        // if no animation is currently playing
        if (!showingAnimation.get()) {
            if (isBlocking) {
                showingAnimation.set(true);
            }
            tlToPlay.setOnFinished(finishedPlayingAnimation);
            tlToPlay.play();
        } else {
            final ChangeListener<Boolean> listener = new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> obs, Boolean oldVal, Boolean newVal) {
                    if (!newVal) {
                        if (isBlocking) {
                            showingAnimation.set(true);
                        }
                        tlToPlay.setOnFinished(finishedPlayingAnimation);
                        tlToPlay.play();
                        showingAnimation.removeListener(this);
                    }
                }
            };
            showingAnimation.addListener(listener);
        }
    }

    private void addPlayers() {
        for (ZoneColor player : testBackend.getPlayers()) {
            HBox playerHBox = buildAndGetPlayerHBox(player);
            playersListItems.add(playerHBox);
            playersVBox.getChildren().add(playerHBox);
            if (player == testBackend.getCurrPlayer()) highlightPlayerTile(player.name());
        }

        testBackend.getNextPlayer().addListener((obs, oldVal, newVal) -> {
            highlightCurrPlayerTile(oldVal.name(), newVal.name());
            highlightCurrPlayerLarge(newVal);
        });
    }

    private void highlightPlayerTile(String id) {
        HBox toBeHighlighted = playersListItems.stream().filter(box -> id.equals(box.getId())).findFirst().orElse(null);
        if (toBeHighlighted == null) return;
        toBeHighlighted.setPrefWidth(175.0d);
        toBeHighlighted.setPrefHeight(35.0d);
    }

    private void highlightCurrPlayerTile(String idOld, String idNew) {
        HBox toBeUnHighlighted = playersListItems.stream().filter(box -> idOld.equals(box.getId())).findFirst().orElse(null);
        if (toBeUnHighlighted == null) return;
        toBeUnHighlighted.setPrefWidth(150.0d);
        toBeUnHighlighted.setPrefHeight(25.0d);
        highlightPlayerTile(idNew);
    }

    private HBox buildAndGetPlayerHBox(ZoneColor player) {
        String playerColor = player.getColorAsHexString().substring(0, 8).replace("0x", "#");
        HBox playerBox = new HBox();
        playerBox.setPrefHeight(25.0d);
        playerBox.setPrefWidth(150.0d);
        playerBox.maxHeightProperty().bind(playerBox.prefHeightProperty());
        playerBox.maxWidthProperty().bind(playerBox.prefWidthProperty());
        playerBox.setAlignment(Pos.TOP_RIGHT);
        playerBox.setStyle("-fx-border-width: 0.5px; -fx-border-color: black;");
        String imgName = player.name().toLowerCase();
        Image plrImg = new Image(getClass().getClassLoader().getResource(imgName + ".png").toExternalForm());
        ImageView plyrIV = new ImageView();
        plyrIV.fitHeightProperty().bind(playerBox.prefHeightProperty());
        plyrIV.fitWidthProperty().bind(playerBox.prefHeightProperty());
        plyrIV.setImage(plrImg);
        playerBox.getChildren().add(plyrIV);

        Label plrLabel = new Label(player.name());
        plrLabel.prefHeightProperty().bind(playerBox.prefHeightProperty());
        plrLabel.prefWidthProperty().bind(playerBox.prefWidthProperty().subtract(playerBox.prefHeightProperty()));
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
        playerBox.maxHeightProperty().bind(playerBox.prefHeightProperty());
        playerBox.maxWidthProperty().bind(playerBox.prefWidthProperty());
        playerBox.setAlignment(Pos.TOP_RIGHT);
        playerBox.setStyle("-fx-background-color: white;");

        String imgName = player.name().toLowerCase();
        Image plrImg = new Image(getClass().getClassLoader().getResource(imgName + ".png").toExternalForm());
        ImageView plyrIV = new ImageView();
        plyrIV.fitHeightProperty().bind(playerBox.prefHeightProperty());
        plyrIV.fitWidthProperty().bind(playerBox.prefHeightProperty());
        plyrIV.setImage(plrImg);
        playerBox.getChildren().add(plyrIV);

        Label plrLabel = new Label(player.name());
        plrLabel.prefHeightProperty().bind(playerBox.prefHeightProperty());
        plrLabel.prefWidthProperty().bind(playerBox.prefWidthProperty().subtract(playerBox.prefHeightProperty()));
        plrLabel.setAlignment(Pos.CENTER);
        plrLabel.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-font-family: Arial; -fx-font-size: 40px;", playerColor));
        playerBox.getChildren().add(plrLabel);

        playerBox.setId(player.name());
        return playerBox;
    }

    private void nextAction() {
        source = null;
        target = null;
        sourceOrTargetNull.set(true);
        testBackend.nextAction();
        Action next = testBackend.getAction();
        actionButtonVisible.set(next != Action.DEFEND);
        actionBtn.setText(testBackend.getAction().getActionName());
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
        label.getStyleClass().add("action-label");
        centerJavaFXRegion(labelStackPane, label);
        overlayStackPane.setStyle("-fx-background-color: black; -fx-opacity: 0.5;");

        stopAnimation(highlightClickableZonesTl);
        removeAllOverlaidPixels();
        int showActionChangeDurationSeconds = 0;
        int removeActionChangeLabelDurationSeconds = showActionChangeDurationSeconds + 3;
        KeyFrame showActionChangeLabelKf = new KeyFrame(Duration.seconds(showActionChangeDurationSeconds), (event) -> this.labelStackPane.getChildren().add(label));
        KeyFrame removeActionChangeLabelKf = new KeyFrame(Duration.seconds(removeActionChangeLabelDurationSeconds), event -> {
            this.labelStackPane.getChildren().remove(label);
            if (testBackend.getAction() == Action.DEFEND) {
                reinforcement();
                labelStackPane.setOnMouseClicked(mouseEvent -> reinforcementClickHandler(mouseEvent));
            } else {
                overlayStackPane.setStyle("-fx-background-color: transparent;");
                labelStackPane.setOnMouseClicked(mouseEvent -> onMapClick(mouseEvent));
                hoverableZones = zoneSquares;
                mapClickEnabled = true;
                highLightClickableZones();
            }
        });
        Timeline actionChangeTl = new Timeline(showActionChangeLabelKf, removeActionChangeLabelKf);
        playAnimation(actionChangeTl, true);
    }

    private void handleMapHover(MouseEvent mouseEvent) {
        if (hoverableZones.isEmpty()) return;
        int x = (int) mouseEvent.getX();
        int y = (int) mouseEvent.getY();
        ZoneSquare hoveredZone = getZoneAtCoordinates(x, y);
        if (hoveredZone == null || !hoverableZones.contains(hoveredZone)) {
            if (this.labelStackPane.getChildren().size() > this.zoneSquares.size()) {
                for (int i = this.zoneSquares.size(); i < this.labelStackPane.getChildren().size(); i++) {
                    if (this.labelStackPane.getChildren().get(i) instanceof ZoneTooltip) { // to prevent removing labels showing the current move
                        this.labelStackPane.getChildren().remove(i);
                        break;
                    }
                }
                currHovered = null;
            }
            return;
        } else if (hoveredZone.equals(currHovered)) {
            return;
        }
        if (currHovered != null && this.labelStackPane.getChildren().size() > this.zoneSquares.size()) {
            for (int i = this.zoneSquares.size(); i < this.labelStackPane.getChildren().size(); i++) {
                if (this.labelStackPane.getChildren().get(i) instanceof ZoneTooltip) {
                    this.labelStackPane.getChildren().remove(i);
                    break;
                }
            }
        }
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
        switch (testBackend.getAction()) {
            case ATTACK:
                initAttack();
                break;
            case MOVE:
                moveTroops(0);
                break;
            default:
                break;
        }
    }


    private void moveTroops(int minAmount) {
        if (source == null || target == null) return;
        int sourceTroops = Integer.parseInt(source.getTxt().getText());
        TroopAmountPopup troopAmountPopup = new TroopAmountPopup(minAmount, sourceTroops - 1, moveTroopsText);
        centerJavaFXRegion(labelStackPane, troopAmountPopup);
        addPopup(troopAmountPopup);
        troopAmountPopup.getConfirmBtn().setOnMouseClicked(click -> {
            // if move: oldAmt + movedAmt, if attack: movedAmt
            int troopAmtNew = minAmount == 0 ? Integer.parseInt(target.getTxt().getText()) + troopAmountPopup.getTroopAmount() : troopAmountPopup.getTroopAmount();
            target.getTxt().setText(Integer.toString(troopAmtNew));
            source.getTxt().setText(Integer.toString(sourceTroops - troopAmountPopup.getTroopAmount()));
            removePopup(troopAmountPopup);
            mapClickEnabled = true;
            hoverableZones = zoneSquares;

            source = null;
            target = null;
            removeAllOverlaidPixels();
            overlayStackPane.setStyle("-fx-background-color: transparent;");
        });
    }

    private void highLightClickableZones() {
        stopAnimation(highlightClickableZonesTl);
        ZoneColor currPlayerColor = testBackend.getCurrPlayer();
        Color mix = colorService.mixColors(neighbourOverlayColor, colorService.getColor(currPlayerColor.getColorAsHexString()));
        updateClickableZones();
        KeyFrame highlightZonesKf = new KeyFrame(Duration.seconds(1), event -> clickableZones.forEach(zone -> setZoneActive(zone, mix, false)));
        KeyFrame removeHighlightedZonesKf = new KeyFrame(Duration.seconds(2), event -> removeAllOverlaidPixels());
        highlightClickableZonesTl = new Timeline(highlightZonesKf, removeHighlightedZonesKf);
        highlightClickableZonesTl.setCycleCount(30);
        playAnimation(highlightClickableZonesTl, false);
    }

    private void updateClickableZones() {
        ZoneColor currPlayerColor = testBackend.getCurrPlayer();
        int minTroopsForZoneToBeClickable = testBackend.getAction() == Action.DEFEND ? 0 : 2;
        clickableZones = zoneSquares.stream()
                .filter(zone -> zone.getColor() == currPlayerColor && getZoneTroopsAmount(zone) >= minTroopsForZoneToBeClickable)
                .collect(Collectors.toList());
    }

    private void reinforcement() {
        testBackend.diceThrow();
        Label label = new Label();
        label.setText("Warte auf Wuerfelwurf...");
        label.setPrefWidth(400.0d);
        label.setPrefHeight(100.0d);
        label.setMaxHeight(200.0d);
        label.getStyleClass().add("action-label");
        centerJavaFXRegion(labelStackPane, label);

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
        playAnimation(reinforcementTl, true);
    }

    private void reinforcementClickHandler(MouseEvent mouseEvent) {
        if (!mapClickEnabled) return;
        int x = (int) mouseEvent.getX();
        int y = (int) mouseEvent.getY();
        ZoneSquare sqr = getZoneAtCoordinates(x, y);
        if (sqr == null || !clickableZones.contains(sqr)) return;
        stopAnimation(highlightClickableZonesTl);
        removeAllOverlaidPixels();
        mapClickEnabled = false;

        clickableZones = new ArrayList<>();

        hoverableZones = new ArrayList<>();
        int currTroopsAmt = Integer.parseInt(sqr.getTxt().getText());
        overlayStackPane.setStyle("-fx-background-color: black; -fx-opacity: 0.5;");
        setZoneActive(sqr, overlayColor, false);
        TroopAmountPopup troopAmountPopup = new TroopAmountPopup(0, testBackend.getTroopsToPlace(), reinforcementText);
        centerJavaFXRegion(labelStackPane, troopAmountPopup);

        // remove label of last hovered zone
        if (labelStackPane.getChildren().size() > zoneSquares.size()) {
            labelStackPane.getChildren().remove(labelStackPane.getChildren().size() - 1);
        }

        addPopup(troopAmountPopup);
        troopAmountPopup.getConfirmBtn().setOnMouseClicked(click -> {
            sqr.getTxt().setText(Integer.toString(currTroopsAmt + troopAmountPopup.getTroopAmount()));
            testBackend.placeTroops(troopAmountPopup.getTroopAmount());
            removePopup(troopAmountPopup);
            if (testBackend.finishedPlacingTroops().get()) {
                mapClickEnabled = false;
                hoverableZones = new ArrayList<>();
                nextAction();
            } else {
                mapClickEnabled = true;
                hoverableZones = zoneSquares;
                removeAllOverlaidPixels();
                overlayStackPane.setStyle("-fx-background-color: transparent;");
                updateClickableZones();
            }
        });
    }

    private void initAttack() {
        if (source == null || target == null) return;
        int maxAttackerTroops = Math.min(getZoneTroopsAmount(source), 3);
        int maxDefenderTroops = Math.min(getZoneTroopsAmount(target), 2);
        TroopAmountPopup popup = promptUserForTroopAmount(maxAttackerTroops, attackerText);
        AtomicBoolean promptingDefender = new AtomicBoolean(false);
        AtomicInteger attackerTroops = new AtomicInteger(1);
        popup.getConfirmBtn().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {
                if (promptingDefender.get()) {
                    removePopup(popup);
                    popup.getConfirmBtn().removeEventHandler(MouseEvent.ANY, this);
                    performAttack(attackerTroops.get(), popup.getTroopAmount());
                } else {
                    promptingDefender.set(true);
                    attackerTroops.set(popup.getTroopAmount());
                    popup.reconfigure(maxDefenderTroops, defenderText);
                    highlightCurrPlayerLarge(target.getColor());

                }
            }
        });
    }

    private void performAttack(int attackerTroops, int defenderTroops) {
        testBackend.diceThrow();
        int diceThrowResult = testBackend.getDiceThrowResult();
        Label label = new Label();
        label.setText("Warte auf Wuerfelwurf...");
        label.setPrefWidth(400.0d);
        label.setPrefHeight(200.0d);
        label.getStyleClass().add("action-label");
        centerJavaFXRegion(labelStackPane, label);

        String winner = diceThrowResult > 3 ? source.getColor().name() : target.getColor().name();
        String loser = diceThrowResult > 3 ? target.getColor().name() : source.getColor().name();

        String fightResult = diceThrowResult > 3 ? "%s hat eine Zone von %s erobert" : "%s hat den Angriff von %s erfolgreich abgewehrt";

        KeyFrame waitingForDiceThrowKf = new KeyFrame(Duration.seconds(0), (event) -> {
            overlayStackPane.setStyle("-fx-background-color: black; -fx-opacity: 0.5;");
            this.labelStackPane.getChildren().add(label);
        });
        KeyFrame winnerKf = new KeyFrame(Duration.seconds(2), (event -> {
            label.setText(String.format(fightResult, winner.replace("PLAYER_", ""), loser.replace("PLAYER_", "")));
        }));
        KeyFrame finishFightKf = new KeyFrame(Duration.seconds(4), (event) -> {
            this.labelStackPane.getChildren().remove(label);
            // attacker wins
            if (diceThrowResult > 3) {
                Color attackerColor = colorService.getColor(source.getColor().getColorAsHexString());
                drawZone(target, attackerColor);

                moveTroops(attackerTroops);
            } else {
                mapClickEnabled = true;
                hoverableZones = zoneSquares;
                source = null;
                target = null;
                removeAllOverlaidPixels();
                overlayStackPane.setStyle("-fx-background-color: transparent;");
                updateClickableZones();

                sourceOrTargetNull.set(source == null || target == null);
            }
        });
        Timeline fightTl = new Timeline(waitingForDiceThrowKf, winnerKf, finishFightKf);
        playAnimation(fightTl, true);
    }

    private void addPlayerColorsToZones() {
        Random random = new Random();
        for (int i = 0; i < zoneSquares.size(); i++) {
            int randomInt = random.nextInt(3);
            Color clr = null;
            switch (randomInt) {
                case 0:
                    clr = colorService.getColor(ZoneColor.PLAYER_GREEN.getColorAsHexString());
                    break;
                case 1:
                    clr = colorService.getColor(ZoneColor.PLAYER_BLUE.getColorAsHexString());
                    break;
                default:
                    clr = colorService.getColor(ZoneColor.PLAYER_RED.getColorAsHexString());
                    break;
            }
            this.drawZone(zoneSquares.get(i), clr);
        }
    }

    private void onMapClick(MouseEvent mouseEvent) {
        if (!mapClickEnabled) return;
        if (source == null || (source != null && target != null)) {
            overlayStackPane.setStyle("-fx-background-color: transparent;");
            this.removeAllOverlaidPixels();
            updateClickableZones();
            source = null;
            target = null;
            sourceOrTargetNull.set(source == null || target == null);
        }
        // uncomment for testing
        // long startTime = System.currentTimeMillis();
        int x = (int) mouseEvent.getX();
        int y = (int) mouseEvent.getY();

        ZoneSquare sqr = getZoneAtCoordinates(x, y);
        if (sqr != null && sqr.getBorder() != null && clickableZones.contains(sqr)) {
            stopAnimation(highlightClickableZonesTl);
            removeAllOverlaidPixels();
            if (source == null || sqr == source) {
                source = sqr;
                List<ZoneSquare> validTargets = new ArrayList<>(getValidNeighboursForAction(testBackend.getAction()));
                hoverableZones = new ArrayList<>(validTargets);
                hoverableZones.add(source);

                clickableZones = new ArrayList<>(hoverableZones);

                markNeighbours(validTargets);
                this.setZoneActive(sqr, overlayColor, true);

                sourceOrTargetNull.set(source == null || target == null);
            } else {
                if (!getValidNeighboursForAction(testBackend.getAction()).contains(sqr)) {
                    System.out.println("You cannot click on " + sqr.getName());
                    return;
                }
                target = sqr;
                this.overlaidZones.keySet().stream().filter((zone) -> !(zone.equals(source.getName()) || zone.equals(target.getName()))).collect(Collectors.toList()).forEach(zoneToInactivate -> inactivateZone(zoneToInactivate));
                if (labelStackPane.getChildren().size() > zoneSquares.size())
                    labelStackPane.getChildren().remove(labelStackPane.getChildren().size() - 1);
                actionBtn.setDisable(false);
                this.setZoneActive(sqr, colorService.getColor(sqr.getColor().getColorAsHexString()), false);
                this.setZoneActive(source, overlayColor, true);
                hoverableZones = new ArrayList<>();
                clickableZones = new ArrayList<>();

                sourceOrTargetNull.set(source == null || target == null);
            }
            overlayStackPane.setStyle("-fx-background-color: black; -fx-opacity: 0.5;");
        } else {
            source = null;
            target = null;
            this.removeAllOverlaidPixels();
            overlayStackPane.setStyle("-fx-background-color: transparent;");
            hoverableZones = zoneSquares;
            updateClickableZones();

            sourceOrTargetNull.set(source == null || target == null);
        }
        // uncomment for testing
        // System.out.println(String.format("Click handling took %d ms", System.currentTimeMillis() - startTime));
    }

    public List<ZoneSquare> getValidNeighboursForAction(Action action) {
        switch (action) {
            case ATTACK:
                return zonesWithNeighbours.get(source).stream()
                        .filter(neighbour -> neighbour.getColor() != source.getColor())
                        .collect(Collectors.toList());
            case MOVE:
                return zonesWithNeighbours.get(source).stream()
                        .filter(neighbour -> neighbour.getColor() == source.getColor())
                        .collect(Collectors.toList());
            default:
                return new ArrayList<>();
        }
    }

    private int getZoneTroopsAmount(ZoneSquare zsq) {
        return Integer.parseInt(zsq.getTxt().getText());
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
            setZoneActive(n, nOverLay, false);
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
        overlaidPixelsToRemove.forEach(pixel -> pw.setColor(pixel.getX(), pixel.getY(), transparentColor));
        overlaidZones.remove(zoneName);
    }

    private void setZoneActive(ZoneSquare sqr, Color overlayColor, boolean shift) {
        Color currColor = colorService.getColor(sqr.getColor().getColorAsHexString());
        if (currColor == null) return;
        Color mix = colorService.mixColors(overlayColor, currColor);
        List<Pixel> overlaidPixels = new ArrayList<>();
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

    private void drawZone(ZoneSquare sqr, Color c) {
        for (HorizontalStripe str : sqr.getBorder()) {
            int y = str.getY();
            for (int x = str.getStartX(); x <= str.getEndX(); x++) {
                mapPw.setColor(x, y, c);
            }
        }
        sqr.setColor(colorService.getZoneColor(c.toString()));
    }

    private TroopAmountPopup promptUserForTroopAmount(int maxAmt, String text) {
        TroopAmountPopup popup = new TroopAmountPopup(1, maxAmt, text);
        centerJavaFXRegion(labelStackPane, popup);
        addPopup(popup);
        return popup;
    }

    private void centerJavaFXRegion(Pane pane, Region region) {
        region.setTranslateX((pane.getMaxWidth() - region.getPrefWidth()) / 2.0d);
        region.setTranslateY((pane.getMaxHeight() - region.getPrefHeight()) / 2.0d);
    }

    private void addPopup(TroopAmountPopup popup) {
        this.showingPopup.set(true);
        labelStackPane.getChildren().add(popup);
    }

    private void removePopup(TroopAmountPopup popup) {
        this.showingPopup.set(false);
        labelStackPane.getChildren().remove(popup);
    }
}

