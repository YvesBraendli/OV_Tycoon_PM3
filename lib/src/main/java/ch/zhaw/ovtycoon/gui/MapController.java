package ch.zhaw.ovtycoon.gui;

import ch.zhaw.ovtycoon.Config;
import ch.zhaw.ovtycoon.TestBackend;
import ch.zhaw.ovtycoon.data.DiceRoll;
import ch.zhaw.ovtycoon.gui.model.Action;
import ch.zhaw.ovtycoon.gui.model.CustomTimeline;
import ch.zhaw.ovtycoon.gui.model.FightResultGrid;
import ch.zhaw.ovtycoon.gui.model.HorizontalStripe;
import ch.zhaw.ovtycoon.gui.model.MapModel;
import ch.zhaw.ovtycoon.gui.model.Notification;
import ch.zhaw.ovtycoon.gui.model.NotificationType;
import ch.zhaw.ovtycoon.gui.model.Pixel;
import ch.zhaw.ovtycoon.gui.model.Scenario;
import ch.zhaw.ovtycoon.gui.model.TroopAmountPopup;
import ch.zhaw.ovtycoon.gui.model.ZoneSquare;
import ch.zhaw.ovtycoon.gui.model.ZoneTooltip;
import ch.zhaw.ovtycoon.gui.service.ColorService;
import ch.zhaw.ovtycoon.model.Player;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class MapController {
    private static final String PLAYER_ELIMINATED = "%s wurde eliminiert!";
    private static final String PLAYER_IMAGE_PREFIX = "player_";
    private static final String RECEIVED_TROOPS = "Du hast %d Truppen erhalten";
    private static final String SELECT_ZONE_TO_DEPLOY_TROOPS = "Waehle die Zone(n), auf welche du die erhaltenen Truppen setzen moechtest";
    private static final String ATTACKER_WON = "%s hat den Kampf gegen %s gewonnen";
    private static final String DEFENDER_WON = "%s hat den Angriff von %s erfolgreich abgewehrt";
    private static final String MOVE_TROOPS_TEXT = "Du kannst %d - %d Truppen verschieben";
    private static final String REINFORCEMENT_TEXT = "Du kannst %d - %d Truppen setzen";
    private static final String ATTACKER_TEXT = "Du kannst mit %d - %d Truppen angreifen";
    private static final String DEFENDER_TEXT = "Du kannst dich mit %d - %d Truppen verteidigen";
    private static final String GAME_WINNER = "%s hat das Spiel gewonnen!";
    private static final int OVERLAY_EFFECT_SHIFT = 5;
    private final Color transparentColor = Color.TRANSPARENT;
    private final Color overlayColor = new Color(0.0d, 0.0d, 0.0d, 0.25d);
    private final Color neighbourOverlayColor = new Color(1, 1, 1, 0.25d);
    private final List<HBox> playersListItems = new ArrayList<>();
    private final SimpleBooleanProperty sourceOrTargetNull = new SimpleBooleanProperty(true);
    private final SimpleBooleanProperty showingAnimation = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty showingPopup = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty gameWon = new SimpleBooleanProperty(false);
    private final Queue<CustomTimeline> waitingTls = new LinkedList<>();
    @FXML
    private StackPane stackPane;
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
    @FXML
    private VBox mapVBox;
    @FXML
    private ImageView imgView;
    @FXML
    private HBox buttonHBox;
    @FXML
    private HBox upperHBox;
    private Timeline highlightClickableZonesTl = new Timeline();
    private PixelWriter pw;
    private PixelWriter mapPw;
    private Map<String, List<Pixel>> overlaidZones = new HashMap<>();
    private List<ZoneSquare> clickableZones = new ArrayList<>();
    private List<ZoneSquare> hoverableZones = new ArrayList<>();
    private boolean mapClickEnabled = true;
    private ZoneSquare source;
    private ZoneSquare target;
    private ZoneSquare currHovered;
    private TestBackend testBackend = new TestBackend();
    private ColorService colorService = new ColorService();
    // private MapLoaderService mapLoaderService;
    private final Map<String, Text> zoneTroopsTexts = new HashMap<>();
    private double scale = 1.0d;
    // TODO only for testing scenarios, e.g. game won
    private final Scenario scenarioToBeTested = Scenario.NONE;

    private final SimpleBooleanProperty clickedActionButton = new SimpleBooleanProperty();

    private MapModel mapModel;
    @FXML
    public void initialize() {
        scale = Screen.getPrimary().getBounds().getHeight() < 1000.0d ? 0.7d : 1.0d;
        if (scale != 1.0d) {
            rescale();
        }
        // TODO should not be initialized in map controller
        mapModel = new MapModel(mapImage, scale, this);
        // mapLoaderService = new MapLoaderService(mapImage, scale);
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        GraphicsContext overlayGc = mapCanvasOverlay.getGraphicsContext2D();
        pw = overlayGc.getPixelWriter();
        mapPw = gc.getPixelWriter();
        // this.mapModel.getZoneSquares() = mapLoaderService.initZoneSquaresFromConfig();
        mapModel.getZoneSquares().forEach(zoneSquare -> labelStackPane.getChildren().add(zoneSquare.getTroopsAmountText()));
        this.addPlayerColorsToZones();
        labelStackPane.setOnMouseMoved(mouseEvent -> handleMapHover(mouseEvent));
        nextMoveBtn.setOnMouseClicked(event -> {
            clickedActionButton.set(false); // TODO cleanup
            mapModel.nextAction();
        });
        actionBtn.setOnMouseClicked(event -> onActionButtonClick());
        nextMoveBtn.disableProperty().bind(showingAnimation.or(showingPopup).or(gameWon));
        actionBtn.visibleProperty().bind(mapModel.actionButtonVisibleProperty().or(gameWon));

        // todo sourceOrTargetNull.or still needed?
        actionBtn.disableProperty().bind(mapModel.actionButtonDisabledProperty().or(mapModel.sourceOrTargetNullProperty()).or(clickedActionButton));
        hoverableZones = mapModel.getZoneSquares();
        addPlayers();
        mapModel.showActionChangeProperty().addListener(((observable, oldValue, newValue) -> showActionChange(newValue)));
        eliminatePlayer();
        mapModel.emitInitialVals(); // TODO not clean like this
        initMapListeners();
    }

    private void initMapListeners() {
        mapModel.darkenBackgroundProperty().addListener(((observable, oldValue, newValue) -> {
            String style = newValue ? "-fx-background-color: black; -fx-opacity: 0.5;" : "-fx-background-color: transparent";
            overlayStackPane.setStyle(style);
        }));
        mapModel.setZoneActiveProperty().addListener(((observable, oldValue, newValue) -> this.setZoneActive(newValue.getZoneSquare(), newValue.getOverlayColor(), newValue.isShift())));
        mapModel.zonesToInactivateProperty().addListener(((observable, oldValue, newValue) -> this.inactivateZone(newValue)));
        mapModel.removeOverlaidPixelsProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue) removeAllOverlaidPixels();
        }));
        mapModel.stopAnimationProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue) {
                stopAnimation();
            }
        }));
        mapModel.highlightNeighboursProperty().addListener(((observable, oldValue, newValue) -> markNeighbours(newValue)));
        mapModel.removeUnnecessaryTooltipsProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue && labelStackPane.getChildren().size() > this.mapModel.getZoneSquares().size())
                labelStackPane.getChildren().remove(labelStackPane.getChildren().size() - 1);
        }));
    }

    private void eliminatePlayer() {
        mapModel.getRisikoController().getEliminatedPlayerProperty().addListener(((observable, oldValue, newValue) -> {
            showNotification(NotificationType.WARNING, String.format(PLAYER_ELIMINATED, newValue.getName()));
            HBox toBeEliminated = playersListItems.stream().filter((hBox -> hBox.getId().equals(newValue.getName()))).findAny().orElse(null);
            if (toBeEliminated != null) toBeEliminated.setStyle("-fx-opacity: 0.25;");
        }));
    }

    // todo keep here
    private void rescale() {
        upperHBox.setPrefHeight(upperHBox.getPrefHeight() * scale);
        upperHBox.setPrefWidth(upperHBox.getPrefWidth() * scale);
        mapVBox.setPrefHeight(mapVBox.getPrefHeight() * scale);
        mapVBox.setPrefWidth(mapVBox.getPrefWidth() * scale);
        imgView.setFitHeight(imgView.getFitHeight() * scale);
        imgView.setFitWidth(imgView.getFitWidth() * scale);
        mapCanvas.setHeight(mapCanvas.getHeight() * scale);
        mapCanvas.setWidth(mapCanvas.getWidth() * scale);
        overlayStackPane.setMaxHeight(overlayStackPane.getMaxHeight() * scale);
        overlayStackPane.setMaxWidth(overlayStackPane.getMaxWidth() * scale);
        overlayStackPane.setMinHeight(overlayStackPane.getMinHeight() * scale);
        overlayStackPane.setMinWidth(overlayStackPane.getMinWidth() * scale);
        mapCanvasOverlay.setHeight(mapCanvasOverlay.getHeight() * scale);
        mapCanvasOverlay.setWidth(mapCanvasOverlay.getWidth() * scale);
        labelStackPane.setMaxHeight(labelStackPane.getMaxHeight() * scale);
        labelStackPane.setMaxWidth(labelStackPane.getMaxWidth() * scale);
        labelStackPane.setMinHeight(labelStackPane.getMinHeight() * scale);
        labelStackPane.setMinWidth(labelStackPane.getMinWidth() * scale);
        playersVBox.setPrefHeight(playersVBox.getPrefHeight() * scale);
        playersVBox.setPrefWidth(playersVBox.getPrefWidth() * scale);
        buttonHBox.setPrefWidth(buttonHBox.getPrefWidth() * scale);
    }

    private void highlightCurrPlayerLarge(Player currPlayer) {
        HBox playerBoxLarge = buildAndGetPlayerHBoxBig(currPlayer);
        centerJavaFXRegion(labelStackPane, playerBoxLarge);
        KeyFrame showPlayerKf = new KeyFrame(Duration.seconds(0), event -> labelStackPane.getChildren().add(playerBoxLarge));
        KeyFrame removePlayerKf = new KeyFrame(Duration.seconds(3), event -> labelStackPane.getChildren().remove(playerBoxLarge));
        Timeline highlightPlayerTl = new Timeline(showPlayerKf, removePlayerKf);
        playAnimation(highlightPlayerTl, true);
    }

    private void stopAnimation() {
        highlightClickableZonesTl.stop();
    }

    private void playAnimation(Timeline tlToPlay, boolean isBlocking) {
        final EventHandler<ActionEvent> handler = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent fin) {
                if (waitingTls.isEmpty()) {
                    showingAnimation.set(false);
                } else {
                    CustomTimeline customTl = waitingTls.remove();
                    if (customTl.isBlocking()) {
                        customTl.getTimeline().setOnFinished(this);
                    } else {
                        showingAnimation.set(false);
                    }
                    if (tlToPlay.getOnFinished() != null) {
                        tlToPlay.setOnFinished(null);
                    }
                    customTl.getTimeline().play();
                }
            }
        };
        // if no blocking animation is currently playing
        if (!showingAnimation.get()) {
            // if animation to play is blocking
            if (isBlocking) {
                CustomTimeline currAnim = new CustomTimeline(tlToPlay, isBlocking);
                showingAnimation.set(true);
                currAnim.getTimeline().setOnFinished(handler);
                currAnim.getTimeline().play();
            } else {
                tlToPlay.play();
            }
        } else { // already playing blocking animation
            waitingTls.add(new CustomTimeline(tlToPlay, isBlocking));
        }
    }

    private void addPlayers() {
        for (Player player : mapModel.getRisikoController().getPlayers()) {
            HBox playerHBox = buildAndGetPlayerHBox(player);
            playersListItems.add(playerHBox);
            playersVBox.getChildren().add(playerHBox);
        }

        mapModel.currPlayerProperty().addListener((obs, oldVal, newVal) -> {
            highlightCurrPlayerTile(oldVal.getName(), newVal.getName());
            highlightCurrPlayerLarge(newVal);
        });
    }

    private void highlightPlayerTile(String id) {
        HBox toBeHighlighted = playersListItems.stream().filter(box -> id.equals(box.getId())).findFirst().orElse(null);
        if (toBeHighlighted == null) return;
        toBeHighlighted.setPrefWidth(175.0d * scale);
        toBeHighlighted.setPrefHeight(35.0d * scale);
    }

    private void highlightCurrPlayerTile(String idOld, String idNew) {
        HBox toBeUnHighlighted = playersListItems.stream().filter(box -> idOld.equals(box.getId())).findFirst().orElse(null);
        highlightPlayerTile(idNew);
        if (toBeUnHighlighted == null) return;
        toBeUnHighlighted.setPrefWidth(150.0d * scale);
        toBeUnHighlighted.setPrefHeight(25.0d * scale);
    }

    // TODO use player color instead
    private HBox buildAndGetPlayerHBox(Player player) {
        String playerColor = player.getColor().getHexValue().substring(0, 8).replace("0x", "#");
        HBox playerBox = new HBox();
        playerBox.setPrefHeight(25.0d * scale);
        playerBox.setPrefWidth(150.0d * scale);
        playerBox.maxHeightProperty().bind(playerBox.prefHeightProperty());
        playerBox.maxWidthProperty().bind(playerBox.prefWidthProperty());
        playerBox.setAlignment(Pos.TOP_RIGHT);
        playerBox.setStyle("-fx-border-width: 0.5px; -fx-border-color: black;");
        String colorName = player.getColor().name().toLowerCase();
        Image plrImg = new Image(getClass().getClassLoader().getResource(PLAYER_IMAGE_PREFIX + colorName + ".png").toExternalForm());
        ImageView plyrIV = new ImageView();
        plyrIV.fitHeightProperty().bind(playerBox.prefHeightProperty());
        plyrIV.fitWidthProperty().bind(playerBox.prefHeightProperty());
        plyrIV.setImage(plrImg);
        playerBox.getChildren().add(plyrIV);

        // Label plrLabel = new Label(player.getName());
        Label plrLabel = new Label();
        plrLabel.prefHeightProperty().bind(playerBox.prefHeightProperty());
        plrLabel.prefWidthProperty().bind(playerBox.prefWidthProperty().subtract(playerBox.prefHeightProperty()));
        plrLabel.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-padding: 0 0 0 5px;", playerColor));
        playerBox.getChildren().add(plrLabel);

        playerBox.setId(player.getName());
        return playerBox;
    }

    private HBox buildAndGetPlayerHBoxBig(Player player) {
        String playerColor = player.getColor().getHexValue().substring(0, 8).replace("0x", "#");
        HBox playerBox = new HBox();
        playerBox.setPrefHeight(100.0d * scale);
        playerBox.setPrefWidth(500.0d * scale);
        playerBox.maxHeightProperty().bind(playerBox.prefHeightProperty());
        playerBox.maxWidthProperty().bind(playerBox.prefWidthProperty());
        playerBox.setAlignment(Pos.TOP_RIGHT);
        playerBox.setStyle("-fx-background-color: white;");

        String colorName = player.getColor().name().toLowerCase();
        Image plrImg = new Image(getClass().getClassLoader().getResource(PLAYER_IMAGE_PREFIX + colorName + ".png").toExternalForm());
        ImageView plyrIV = new ImageView();
        plyrIV.fitHeightProperty().bind(playerBox.prefHeightProperty());
        plyrIV.fitWidthProperty().bind(playerBox.prefHeightProperty());
        plyrIV.setImage(plrImg);
        playerBox.getChildren().add(plyrIV);

        // Label plrLabel = new Label(player.getName());
        Label plrLabel = new Label();
        plrLabel.prefHeightProperty().bind(playerBox.prefHeightProperty());
        plrLabel.prefWidthProperty().bind(playerBox.prefWidthProperty().subtract(playerBox.prefHeightProperty()));
        plrLabel.setAlignment(Pos.CENTER);
        plrLabel.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-font-family: Arial; -fx-font-size: 40px;", playerColor));
        playerBox.getChildren().add(plrLabel);

        playerBox.setId(player.getName());
        return playerBox;
    }

    // TODO curr player from model
    // TODO get action button visible and action button text

    private void showActionChange(String text) {
        Label label = new Label();
        mapClickEnabled = false;
        hoverableZones = new ArrayList<>();
        label.setText(text);
        label.setPrefWidth(400.0d);
        label.setPrefHeight(100.0d);
        label.getStyleClass().add("action-label");
        centerJavaFXRegion(labelStackPane, label);
        overlayStackPane.setStyle("-fx-background-color: black; -fx-opacity: 0.5;");

        stopAnimation();
        removeAllOverlaidPixels();
        int showActionChangeDurationSeconds = 0;
        int removeActionChangeLabelDurationSeconds = showActionChangeDurationSeconds + 3;
        KeyFrame showActionChangeLabelKf = new KeyFrame(Duration.seconds(showActionChangeDurationSeconds), event -> this.labelStackPane.getChildren().add(label));
        KeyFrame removeActionChangeLabelKf = new KeyFrame(Duration.seconds(removeActionChangeLabelDurationSeconds), event -> {
            this.labelStackPane.getChildren().remove(label);
            if (mapModel.getRisikoController().getAction() == Action.DEFEND) {
                reinforcement();
                labelStackPane.setOnMouseClicked(mouseEvent -> reinforcementClickHandler(mouseEvent));
            } else {
                overlayStackPane.setStyle("-fx-background-color: transparent;");
                labelStackPane.setOnMouseClicked(mouseEvent -> onMapClick(mouseEvent));
                hoverableZones = mapModel.getZoneSquares();
                mapClickEnabled = true;
                highLightClickableZones();
            }
        });
        Timeline actionChangeTl = new Timeline(showActionChangeLabelKf, removeActionChangeLabelKf);
        playAnimation(actionChangeTl, true);
    }

    public Map<String, List<Pixel>> getOverlaidZones() {
        return overlaidZones;
    }

    private void handleMapHover(MouseEvent mouseEvent) {
        if (hoverableZones.isEmpty()) return;
        int x = (int) mouseEvent.getX();
        int y = (int) mouseEvent.getY();
        ZoneSquare hoveredZone = mapModel.getZoneAtCoordinates(x, y);
        if (hoveredZone == null || !hoverableZones.contains(hoveredZone)) {
            if (this.labelStackPane.getChildren().size() > this.mapModel.getZoneSquares().size()) {
                for (int i = this.mapModel.getZoneSquares().size(); i < this.labelStackPane.getChildren().size(); i++) {
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
        if (currHovered != null && this.labelStackPane.getChildren().size() > this.mapModel.getZoneSquares().size()) {
            for (int i = this.mapModel.getZoneSquares().size(); i < this.labelStackPane.getChildren().size(); i++) {
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
        clickedActionButton.set(true);
        mapClickEnabled = false;
        switch (mapModel.getRisikoController().getAction()) {
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
        int maxMovableTroops = mapModel.getRisikoController().getMaxMovableTroops(source.getName());
        TroopAmountPopup troopAmountPopup = new TroopAmountPopup(minAmount, maxMovableTroops, MOVE_TROOPS_TEXT);
        centerJavaFXRegion(labelStackPane, troopAmountPopup);
        addPopup(troopAmountPopup);
        troopAmountPopup.getConfirmBtn().setOnMouseClicked(click -> {
            // TODO should be different if attack / move
            mapModel.getRisikoController().moveUnits(source.getName(), target.getName(), troopAmountPopup.getTroopAmount());
            int troopAmtNew = mapModel.getRisikoController().getZoneTroops(target.getName());
            target.updateTroopsAmount(Integer.toString(troopAmtNew));
            source.updateTroopsAmount(Integer.toString(mapModel.getRisikoController().getZoneTroops(source.getName())));
            removePopup(troopAmountPopup);
            mapClickEnabled = true;
            hoverableZones = this.mapModel.getZoneSquares();
            source = null;
            target = null;
            removeAllOverlaidPixels();
            overlayStackPane.setStyle("-fx-background-color: transparent;");
        });
    }

    private void highLightClickableZones() {
        stopAnimation();
        Config.PlayerColor currPlayerColor = mapModel.getRisikoController().getCurrentPlayer().getColor();
        Color mix = colorService.mixColors(neighbourOverlayColor, colorService.getColor(currPlayerColor.getHexValue()));
        updateClickableZones();
        KeyFrame highlightZonesKf = new KeyFrame(Duration.seconds(1), event -> clickableZones.forEach(zone -> setZoneActive(zone, mix, false)));
        KeyFrame removeHighlightedZonesKf = new KeyFrame(Duration.seconds(2), event -> removeAllOverlaidPixels());
        highlightClickableZonesTl = new Timeline(highlightZonesKf, removeHighlightedZonesKf);
        highlightClickableZonesTl.setCycleCount(30);
        playAnimation(highlightClickableZonesTl, false);
    }

    public void updateClickableZones() {
        clickableZones = mapModel.getRisikoController().getValidSourceZoneNames().stream()
                .map(zoneName -> getZsqByName(zoneName)).collect(Collectors.toList());
    }

    private void reinforcement() {
        testBackend.diceThrow();
        Label label = new Label();
        label.setPrefWidth(400.0d);
        label.setPrefHeight(100.0d);
        label.setMaxHeight(200.0d);
        label.getStyleClass().add("action-label");
        centerJavaFXRegion(labelStackPane, label);

        KeyFrame troopsReceivedKf = new KeyFrame(Duration.seconds(0), (event -> {
            this.labelStackPane.getChildren().add(label);
            label.setText(String.format(RECEIVED_TROOPS, testBackend.getTroopsToPlace()));
        }));

        KeyFrame setTroopsKf = new KeyFrame(Duration.seconds(2), (event -> label.setText(SELECT_ZONE_TO_DEPLOY_TROOPS)));
        KeyFrame removeLabelKf = new KeyFrame(Duration.seconds(4), event -> {
            labelStackPane.getChildren().remove(label);
            overlayStackPane.setStyle("-fx-background-color: transparent;");
            hoverableZones = this.mapModel.getZoneSquares();
            mapClickEnabled = true;
            highLightClickableZones();
        });

        Timeline reinforcementTl = new Timeline(troopsReceivedKf, setTroopsKf, removeLabelKf);
        playAnimation(reinforcementTl, true);
    }

    private void reinforcementClickHandler(MouseEvent mouseEvent) {
        if (!mapClickEnabled) return;
        int x = (int) mouseEvent.getX();
        int y = (int) mouseEvent.getY();
        ZoneSquare sqr = mapModel.getZoneAtCoordinates(x, y);
        if (sqr == null || !clickableZones.contains(sqr)) return;
        stopAnimation();
        removeAllOverlaidPixels();
        mapClickEnabled = false;

        clickableZones = new ArrayList<>();

        hoverableZones = new ArrayList<>();
        overlayStackPane.setStyle("-fx-background-color: black; -fx-opacity: 0.5;");
        setZoneActive(sqr, overlayColor, false);
        TroopAmountPopup troopAmountPopup = new TroopAmountPopup(0, testBackend.getTroopsToPlace(), REINFORCEMENT_TEXT);
        centerJavaFXRegion(labelStackPane, troopAmountPopup);

        // remove label of last hovered zone
        if (labelStackPane.getChildren().size() > this.mapModel.getZoneSquares().size()) {
            labelStackPane.getChildren().remove(labelStackPane.getChildren().size() - 1);
        }

        addPopup(troopAmountPopup);
        troopAmountPopup.getConfirmBtn().setOnMouseClicked(click -> {
            mapModel.getRisikoController().updateZoneTroops(sqr.getName(), troopAmountPopup.getTroopAmount());
            sqr.updateTroopsAmount(Integer.toString(mapModel.getRisikoController().getZoneTroops(sqr.getName())));
            testBackend.placeTroops(troopAmountPopup.getTroopAmount());
            removePopup(troopAmountPopup);
            if (testBackend.finishedPlacingTroops().get()) {
                mapClickEnabled = false;
                hoverableZones = new ArrayList<>();
                mapModel.nextAction();
            } else {
                mapClickEnabled = true;
                hoverableZones = this.mapModel.getZoneSquares();
                removeAllOverlaidPixels();
                overlayStackPane.setStyle("-fx-background-color: transparent;");
                updateClickableZones();
            }
        });
    }

    private void initAttack() {
        source = mapModel.getSource();
        target = mapModel.getTarget();
        if (source == null || target == null) return;
        int maxAttackerTroops = mapModel.getRisikoController().getMaxTroopsForAttack(source.getName());
        int maxDefenderTroops = mapModel.getRisikoController().getMaxTroopsForDefending(target.getName());
        TroopAmountPopup popup = promptUserForTroopAmount(maxAttackerTroops, ATTACKER_TEXT);
        popup.visibleProperty().bind(showingAnimation.not());
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
                    popup.reconfigure(maxDefenderTroops, DEFENDER_TEXT);
                    highlightCurrPlayerLarge(playerColorToPlayer(target.getColor()));

                }
            }
        });
    }

    private Player playerColorToPlayer(Config.PlayerColor playerColor) {
        for (Player player : mapModel.getRisikoController().getPlayers()) {
            if (player.getColor() == playerColor) {
                return player;
            }
        }
        return null;
    }

    private void performAttack(int attackerTroops, int defenderTroops) {
        Label label = new Label();
        label.setPrefWidth(400.0d);
        label.setPrefHeight(200.0d);
        label.getStyleClass().add("action-label");
        centerJavaFXRegion(labelStackPane, label);
        Player attacker = playerColorToPlayer(source.getColor());
        Player defender = playerColorToPlayer(target.getColor());
        if (attacker == null || defender == null) return;

        final AtomicReference<Player> fightWinner= new AtomicReference<>(null);
        final AtomicBoolean zoneOvertaken = new AtomicBoolean(false);

        mapModel.getRisikoController().getFightWinner().addListener((new ChangeListener<Player>() {
            @Override
            public void changed(ObservableValue<? extends Player> observable, Player oldValue, Player newValue) {
                if (newValue != null) {
                    fightWinner.set(newValue);
                    mapModel.getRisikoController().getFightWinner().removeListener(this);
                }
            }
        }));

        mapModel.getRisikoController().getZoneOvertaken().addListener((new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue != null) {
                    zoneOvertaken.set(newValue);
                    mapModel.getRisikoController().getZoneOvertaken().removeListener(this);
                }
            }
        }));
        // TODO check if listener removed
        AtomicBoolean overtookRegion = new AtomicBoolean(false);
        mapModel.getRisikoController().getNewRegionOwnerProperty().addListener((new ChangeListener<Config.PlayerColor>() {
            @Override
            public void changed(ObservableValue<? extends Config.PlayerColor> observable, Config.PlayerColor oldValue, Config.PlayerColor newValue) {
                if (newValue != null) {
                    overtookRegion.set(true);
                    System.out.println(newValue);
                    mapModel.getRisikoController().getNewRegionOwnerProperty().removeListener(this);
                }
            }
        }));

        DiceRoll fightRes = mapModel.getRisikoController().runFight(source.getName(), target.getName(), attackerTroops, defenderTroops);
        FightResultGrid grid = new FightResultGrid(fightRes.getAttackerRoll(), fightRes.getDefenderRoll(), scale);
        centerJavaFXRegion(labelStackPane, grid);

        String winner = fightWinner.get().equals(attacker) ?  attacker.getName() : defender.getName();
        String loser = fightWinner.get().equals(attacker) ? defender.getName() : attacker.getName();

        String fightResultText = fightWinner.get().equals(mapModel.getRisikoController().getCurrentPlayer()) ? ATTACKER_WON : DEFENDER_WON;

        KeyFrame diceThrowKf = new KeyFrame(Duration.seconds(0), event -> {
            overlayStackPane.setStyle("-fx-background-color: black; -fx-opacity: 0.5;");
            this.labelStackPane.getChildren().add(grid);
        });
        KeyFrame winnerKf = new KeyFrame(Duration.seconds(5), (event -> {
            labelStackPane.getChildren().remove(grid);
            label.setText(String.format(fightResultText, winner, loser));
            this.labelStackPane.getChildren().add(label);
        }));
        int regionOwnerSeconds = overtookRegion.get() ? 7 : 5;
        KeyFrame overTookRegionKf = new KeyFrame(Duration.seconds(regionOwnerSeconds), event -> {
            label.setText(String.format("%s hat die Region %s uebernommen!", winner, mapModel.getRisikoController().getRegionByOwner(target.getName()).toString()));
        });
        KeyFrame finishFightKf = new KeyFrame(Duration.seconds(regionOwnerSeconds + 3), event -> {
            clickedActionButton.set(false);
            this.labelStackPane.getChildren().remove(label);
            // attacker wins
            if (zoneOvertaken.get()) {
                Color attackerColor = colorService.getColor(source.getColor().getHexValue());
                drawZone(target, attackerColor);
                if (mapModel.getRisikoController().getWinner() == null) {
                    moveTroops(attackerTroops);
                } else {
                    gameWon(attacker.getName());
                }
            } else {
                // update troops on zones after attack
                source.updateTroopsAmount(Integer.toString(mapModel.getRisikoController().getZoneTroops(source.getName())));
                target.updateTroopsAmount(Integer.toString(mapModel.getRisikoController().getZoneTroops(target.getName())));

                // ending attack
                mapClickEnabled = true;
                hoverableZones = this.mapModel.getZoneSquares();
                source = null;
                target = null;
                removeAllOverlaidPixels();
                overlayStackPane.setStyle("-fx-background-color: transparent;");
                updateClickableZones();
                sourceOrTargetNull.set(source == null || target == null);
            }
        });

        Timeline fightTl = new Timeline();
        fightTl.getKeyFrames().add(diceThrowKf);
        fightTl.getKeyFrames().add(winnerKf);
        if (overtookRegion.get()) {
            fightTl.getKeyFrames().add(overTookRegionKf);
        }
        fightTl.getKeyFrames().add(finishFightKf);
        playAnimation(fightTl, true);
    }

    private void addPlayerColorsToZones() {
        Random random = new Random();
        Player[] players = mapModel.getRisikoController().getPlayers();
        if (scenarioToBeTested == Scenario.PLAYER_ELIMINATED) {
            for (int i = 0; i < this.mapModel.getZoneSquares().size() - 1; i++) {
                String name = this.mapModel.getZoneSquares().get(i).getName();
                int troops = Integer.parseInt(this.mapModel.getZoneSquares().get(i).getTroopsAmountText().getText());

                int randomInt = random.nextInt(2);
                mapModel.getRisikoController().setZoneOwner(players[randomInt], this.mapModel.getZoneSquares().get(i).getName());
                Color zoneColor = colorService.getColor(players[randomInt].getColor().getHexValue());
                this.drawZone(this.mapModel.getZoneSquares().get(i), zoneColor);
                mapModel.getRisikoController().updateZoneTroops(name, troops);
            }
            // for testing elimination of player green
            String name = this.mapModel.getZoneSquares().get(42).getName();
            int troops = Integer.parseInt(this.mapModel.getZoneSquares().get(42).getTroopsAmountText().getText());
            mapModel.getRisikoController().setZoneOwner(players[2], this.mapModel.getZoneSquares().get(42).getName());
            Color zoneColor = colorService.getColor(players[2].getColor().getHexValue());
            this.drawZone(this.mapModel.getZoneSquares().get(42), zoneColor);
            mapModel.getRisikoController().updateZoneTroops(name, troops);
        } else if (scenarioToBeTested == Scenario.WIN_GAME) {
            for (int i = 0; i < this.mapModel.getZoneSquares().size() - 1; i++) {
                String name = this.mapModel.getZoneSquares().get(i).getName();
                int troops = Integer.parseInt(this.mapModel.getZoneSquares().get(i).getTroopsAmountText().getText());
                mapModel.getRisikoController().setZoneOwner(players[0], this.mapModel.getZoneSquares().get(i).getName());
                Color zoneColor = colorService.getColor(players[0].getColor().getHexValue());
                this.drawZone(this.mapModel.getZoneSquares().get(i), zoneColor);
                mapModel.getRisikoController().updateZoneTroops(name, troops);
            }
            String name = this.mapModel.getZoneSquares().get(42).getName();
            int troops = Integer.parseInt(this.mapModel.getZoneSquares().get(42).getTroopsAmountText().getText());
            mapModel.getRisikoController().setZoneOwner(players[1], this.mapModel.getZoneSquares().get(42).getName());
            Color zoneColor = colorService.getColor(players[1].getColor().getHexValue());
            this.drawZone(this.mapModel.getZoneSquares().get(42), zoneColor);
            mapModel.getRisikoController().updateZoneTroops(name, troops);
        } else {
            for (int i = 0; i < this.mapModel.getZoneSquares().size(); i++) {
                int randomInt = random.nextInt(3);
                String name = this.mapModel.getZoneSquares().get(i).getName();
                int troops = Integer.parseInt(this.mapModel.getZoneSquares().get(i).getTroopsAmountText().getText());
                mapModel.getRisikoController().setZoneOwner(players[randomInt], this.mapModel.getZoneSquares().get(i).getName());
                Color zoneColor = colorService.getColor(players[randomInt].getColor().getHexValue());
                this.drawZone(this.mapModel.getZoneSquares().get(i), zoneColor);
                // TODO move, currently setting troops here
                mapModel.getRisikoController().updateZoneTroops(name, troops);
            }
        }
    }

    private void onMapClick(MouseEvent mouseEvent) {
        if (!mapClickEnabled) return;
        int x = (int) mouseEvent.getX();
        int y = (int) mouseEvent.getY();
        mapModel.handleMapClick(x, y);
        /*if (source == null || (source != null && target != null)) {
            overlayStackPane.setStyle("-fx-background-color: transparent;");
            this.removeAllOverlaidPixels();
            updateClickableZones();
            source = null;
            target = null;
            sourceOrTargetNull.set(source == null || target == null);
        }
        // uncomment for testing
        // long startTime = System.currentTimeMillis();

        ZoneSquare sqr = mapModel.getZoneAtCoordinates(x, y);
        if (sqr != null && sqr.getBorder() != null && clickableZones.contains(sqr)) {
            stopAnimation();
            removeAllOverlaidPixels();
            if (source == null || sqr == source) {
                source = sqr;
                List<ZoneSquare> validTargets = getTargets(sqr);

                hoverableZones = new ArrayList<>(validTargets);
                hoverableZones.add(source);

                clickableZones = new ArrayList<>(hoverableZones);

                markNeighbours(validTargets);
                this.setZoneActive(sqr, overlayColor, true);

                sourceOrTargetNull.set(source == null || target == null);
            } else {
                if (!getTargets(source).contains(sqr)) {
                    System.out.println("You cannot click on " + sqr.getName());
                    return;
                }
                target = sqr;
                this.overlaidZones.keySet().stream().filter((zone) -> !(zone.equals(source.getName()) || zone.equals(target.getName()))).collect(Collectors.toList()).forEach(zoneToInactivate -> inactivateZone(zoneToInactivate));
                if (labelStackPane.getChildren().size() > this.mapModel.getZoneSquares().size())
                    labelStackPane.getChildren().remove(labelStackPane.getChildren().size() - 1);
                actionBtn.setDisable(false);
                this.setZoneActive(sqr, colorService.getColor(sqr.getColor().getHexValue()), false);
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
            hoverableZones = this.mapModel.getZoneSquares();
            updateClickableZones();

            sourceOrTargetNull.set(source == null || target == null);
        }*/
        // uncomment for testing
        // System.out.println(String.format("Click handling took %d ms", System.currentTimeMillis() - startTime));
    }

    private List<ZoneSquare> getTargets(ZoneSquare sqr) {
        return mapModel.getRisikoController().getValidTargetZoneNames(sqr.getName()).stream()
                .map(zoneName -> getZsqByName(zoneName)).collect(Collectors.toList());
    }

    private ZoneSquare getZsqByName(String name) {
        return this.mapModel.getZoneSquares().stream().filter((zsq) -> name.equals(zsq.getName())).findFirst().orElse(null);
    }

    private void markNeighbours(List<ZoneSquare> neighbours) {
        neighbours.forEach(n -> {
            System.out.println(n);
            Color nOverLay = colorService.mixColors(neighbourOverlayColor, colorService.getColor(n.getColor().getHexValue()));
            setZoneActive(n, nOverLay, false);
        });
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
        Color currColor = colorService.getColor(sqr.getColor().getHexValue());
        if (currColor == null) return;
        Color mix = colorService.mixColors(overlayColor, currColor);
        List<Pixel> overlaidPixels = new ArrayList<>();
        for (HorizontalStripe stripe : sqr.getBorder()) {
            int y = stripe.getY();
            for (int x = stripe.getStartX(); x <= stripe.getEndX(); x++) {
                overlaidPixels.add(new Pixel(x, y));
                pw.setColor(x, y, overlayColor);
                if (shift) {
                    for (int k = 1; k < OVERLAY_EFFECT_SHIFT; k++) {
                        pw.setColor(x - k, y - k, mix);
                        overlaidPixels.add(new Pixel(x - k, y - k));
                    }
                    pw.setColor(x - OVERLAY_EFFECT_SHIFT, y - OVERLAY_EFFECT_SHIFT, currColor);
                    overlaidPixels.add(new Pixel(x - OVERLAY_EFFECT_SHIFT, y - OVERLAY_EFFECT_SHIFT));
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
        sqr.setColor(colorService.getPlayerColor(c.toString()));
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

    private void showNotification(NotificationType type, String text) {
        final double stackPaneWidth = scale * 688.0d;
        Notification notification = new Notification(type, text, stackPaneWidth);
        KeyFrame showNotificationKf = new KeyFrame(Duration.ZERO, event -> stackPane.getChildren().add(notification));
        KeyFrame removeNotificationKf = new KeyFrame(Duration.seconds(2), event -> stackPane.getChildren().remove(notification));
        Timeline notificationTl = new Timeline(showNotificationKf, removeNotificationKf);
        playAnimation(notificationTl, true);
    }

    private void gameWon(String winnerName) {
        gameWon.set(true);
        mapClickEnabled = false;
        hoverableZones = this.mapModel.getZoneSquares();
        source = null;
        target = null;
        removeAllOverlaidPixels();
        overlayStackPane.setStyle("-fx-background-color: transparent;");
        updateClickableZones();
        sourceOrTargetNull.set(source == null || target == null);
        showNotification(NotificationType.INFO, String.format(GAME_WINNER, winnerName));
    }

    public List<ZoneSquare> getClickableZones() {
        return clickableZones;
    }

    public List<ZoneSquare> getHoverableZones() {
        return hoverableZones;
    }

    public void setHoverableZones(List<ZoneSquare> hoverableZones) {
        this.hoverableZones = hoverableZones;
    }

    public void setClickableZones(List<ZoneSquare> clickableZones) {
        this.clickableZones = clickableZones;
    }
}

