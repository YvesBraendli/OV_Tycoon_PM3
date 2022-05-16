package ch.zhaw.ovtycoon.gui;

import ch.zhaw.ovtycoon.Config;
import ch.zhaw.ovtycoon.gui.model.Action;
import ch.zhaw.ovtycoon.gui.model.dto.AttackDTO;
import ch.zhaw.ovtycoon.gui.model.CustomTimeline;
import ch.zhaw.ovtycoon.gui.model.dto.FightDTO;
import ch.zhaw.ovtycoon.gui.model.customnodes.FightResultGrid;
import ch.zhaw.ovtycoon.gui.model.HorizontalStripe;
import ch.zhaw.ovtycoon.gui.model.MapModel;
import ch.zhaw.ovtycoon.gui.model.dto.MoveTroopsDTO;
import ch.zhaw.ovtycoon.gui.model.Notification;
import ch.zhaw.ovtycoon.gui.model.NotificationType;
import ch.zhaw.ovtycoon.gui.model.Pixel;
import ch.zhaw.ovtycoon.gui.model.dto.ReinforcementDTO;
import ch.zhaw.ovtycoon.gui.model.customnodes.TroopAmountPopup;
import ch.zhaw.ovtycoon.gui.model.ZoneSquare;
import ch.zhaw.ovtycoon.gui.model.customnodes.ZoneTooltip;
import ch.zhaw.ovtycoon.gui.model.dto.ZoneTroopAmountDTO;
import ch.zhaw.ovtycoon.gui.model.dto.ZoneTroopAmountInitDTO;
import ch.zhaw.ovtycoon.gui.service.ColorService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
    private static final String REGION_OVERTAKEN = "%s hat die Region %s uebernommen!";
    private static final double DEFAULT_STACK_PANE_WIDTH = 688.0d;
    private static final int OVERLAY_EFFECT_SHIFT_PIXELS = 5;
    private final Color transparentColor = Color.TRANSPARENT;
    private final Color neighbourOverlayColor = new Color(1, 1, 1, 0.25d);
    private final List<HBox> playersListItems = new ArrayList<>();
    private final SimpleBooleanProperty showingAnimation = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty showingPopup = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty gameWon = new SimpleBooleanProperty(false);
    private final Queue<CustomTimeline> waitingTimelines = new LinkedList<>();
    private final SimpleBooleanProperty clickedActionButton = new SimpleBooleanProperty();

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
    private Map<String, List<Pixel>> overlaidPixelsByZone = new HashMap<>();
    private ColorService colorService = new ColorService();
    private double scale = 1.0d;
    private MapModel mapModel;

    @FXML
    public void initialize() {
        scale = Screen.getPrimary().getBounds().getHeight() < 1000.0d ? 0.7d : 1.0d;
        if (scale != 1.0d) {
            rescale();
        }
        // TODO should not be initialized in map controller
        mapModel = new MapModel(mapImage, scale);
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        GraphicsContext overlayGc = mapCanvasOverlay.getGraphicsContext2D();
        pw = overlayGc.getPixelWriter();
        mapPw = gc.getPixelWriter();
        labelStackPane.setOnMouseMoved(mouseEvent -> handleMapHover(mouseEvent));
        nextMoveBtn.setOnMouseClicked(event -> {
            clickedActionButton.set(false); // TODO cleanup
            mapModel.nextAction();
        });
        actionBtn.setOnMouseClicked(event -> onActionButtonClick());
        nextMoveBtn.disableProperty().bind(showingAnimation.or(showingPopup).or(gameWon));
        actionBtn.visibleProperty().bind(mapModel.actionButtonVisibleProperty().or(gameWon));
        actionBtn.disableProperty().bind(mapModel.actionButtonDisabledProperty().or(mapModel.sourceOrTargetNullProperty()).or(clickedActionButton));
        actionBtn.textProperty().bind(mapModel.actionButtonTextProperty());
        addPlayers();
        mapModel.showActionChangeProperty().addListener(((observable, oldValue, newValue) -> showActionChange(newValue)));
        eliminatePlayer();
        initMapListeners();
        mapModel.setInitialValues(); // TODO ev clean up
    }

    private void initTroopAmountText(ZoneTroopAmountInitDTO zoneTroopAmountInitDTO) {
        Text troopAmountText = new Text();
        troopAmountText.setId(zoneTroopAmountInitDTO.getZoneName());
        troopAmountText.setText(Integer.toString(zoneTroopAmountInitDTO.getTroopAmount()));
        troopAmountText.setTranslateX(zoneTroopAmountInitDTO.getTranslateX());
        troopAmountText.setTranslateY(zoneTroopAmountInitDTO.getTranslateY());
        troopAmountText.setStyle("-fx-fill: lightgray;-fx-font-weight: bold;");
        labelStackPane.getChildren().add(troopAmountText);
    }

    private void updateTroopAmountText(ZoneTroopAmountDTO zoneTroopAmountDTO) {
        Text toUpdate = (Text) labelStackPane.getChildren().stream()
                .filter(node -> node instanceof Text && node.getId() != null && node.getId().equals(zoneTroopAmountDTO.getZoneName()))
                .findFirst()
                .orElse(null);
        if (toUpdate == null) return;
        toUpdate.setText(Integer.toString(zoneTroopAmountDTO.getTroopAmount()));
    }

    private void initMapListeners() {
        mapModel.initializeZoneTroopsTextProperty().addListener(((observable, oldValue, newValue) -> initTroopAmountText(newValue)));
        mapModel.updateZoneTroopsTextProperty().addListener(((observable, oldValue, newValue) -> updateTroopAmountText(newValue)));
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
                stopHighlightClickableZonesAnimation();
            }
        }));
        mapModel.highlightNeighboursProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null) {
                markNeighbours(newValue);
            }
        }));
        mapModel.removeUnnecessaryTooltipsProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue && labelStackPane.getChildren().size() > this.mapModel.getZoneSquares().size())
                for (int i = mapModel.getZoneSquares().size(); i < labelStackPane.getChildren().size(); i++) {
                    if (labelStackPane.getChildren().get(i) instanceof ZoneTooltip) {
                        labelStackPane.getChildren().remove(i); // TODO check if error possible when removing + iterating
                    }
                }
        }));
        mapModel.moveTroopsProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue.intValue() != -1) initMoveTroops(newValue.intValue());
        }));
        mapModel.gameWinnerProperty().addListener(((observable, oldValue, newValue) -> gameWon(newValue)));
        mapModel.drawZoneProperty().addListener(((observable, oldValue, newValue) -> drawZone(newValue.getZoneToDraw(), newValue.getColor())));
        mapModel.openMoveTroopsPopupProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null) { // TODO check if can be omitted
                moveTroops(newValue);
            }
        }));
        mapModel.showTooltipProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ZoneTooltip t = new ZoneTooltip(newValue.getTooltipText());
                t.setTranslateX(newValue.getTranslateX());
                t.setTranslateY(newValue.getTranslateY());
                labelStackPane.getChildren().add(t);
            }
        }));
        mapModel.removeTooltipProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Node toRemove = labelStackPane.getChildren().stream().filter(node -> node instanceof ZoneTooltip && newValue.getTooltipText().equals(node.getId())).findFirst().orElse(null);
                if (toRemove != null) labelStackPane.getChildren().remove(toRemove);
            }
        }));
        mapModel.highlightPlayerProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null) {
                highlightCurrPlayerLarge(newValue);
            }
        }));
    }

    // TODO get current player from map model
    private void eliminatePlayer() {
        mapModel.getRisikoController().getEliminatedPlayerProperty().addListener(((observable, oldValue, newValue) -> {
            showNotification(NotificationType.WARNING, String.format(PLAYER_ELIMINATED, newValue.name().toLowerCase()));
            HBox toBeEliminated = playersListItems.stream().filter((hBox -> hBox.getId().equals(newValue.name().toLowerCase()))).findAny().orElse(null);
            if (toBeEliminated != null) toBeEliminated.setStyle("-fx-opacity: 0.25;");
        }));
    }

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

    private void highlightCurrPlayerLarge(Config.PlayerColor currPlayer) {
        HBox playerBoxLarge = buildAndGetPlayerHBoxBig(currPlayer);
        centerJavaFXRegion(labelStackPane, playerBoxLarge);
        KeyFrame showPlayerKf = new KeyFrame(Duration.seconds(0), event -> labelStackPane.getChildren().add(playerBoxLarge));
        KeyFrame removePlayerKf = new KeyFrame(Duration.seconds(3), event -> labelStackPane.getChildren().remove(playerBoxLarge));
        Timeline highlightPlayerTl = new Timeline(showPlayerKf, removePlayerKf);
        playAnimation(highlightPlayerTl, true);
    }

    private void stopHighlightClickableZonesAnimation() {
        highlightClickableZonesTl.stop();
    }

    private void playAnimation(Timeline tlToPlay, boolean isBlocking) {
        final EventHandler<ActionEvent> handler = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent fin) {
                if (waitingTimelines.isEmpty()) {
                    showingAnimation.set(false);
                } else {
                    CustomTimeline customTl = waitingTimelines.remove();
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
            waitingTimelines.add(new CustomTimeline(tlToPlay, isBlocking));
        }
    }

    private void addPlayers() {
        for (Config.PlayerColor playerColor : mapModel.getRisikoController().getPlayerColors()) {
            HBox playerHBox = buildAndGetPlayerHBox(playerColor);
            playersListItems.add(playerHBox);
            playersVBox.getChildren().add(playerHBox);
        }

        mapModel.currPlayerProperty().addListener((obs, oldVal, newVal) -> {
            highlightCurrPlayerTile(oldVal.name().toLowerCase(), newVal.name().toLowerCase());
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
    private HBox buildAndGetPlayerHBox(Config.PlayerColor player) {
        String playerColor = player.getHexValue().substring(0, 8).replace("0x", "#");
        HBox playerBox = new HBox();
        playerBox.setPrefHeight(25.0d * scale);
        playerBox.setPrefWidth(150.0d * scale);
        playerBox.maxHeightProperty().bind(playerBox.prefHeightProperty());
        playerBox.maxWidthProperty().bind(playerBox.prefWidthProperty());
        playerBox.setAlignment(Pos.TOP_RIGHT);
        playerBox.setStyle("-fx-border-width: 0.5px; -fx-border-color: black;");
        String colorName = player.name().toLowerCase();

        Image playerImage = new Image(getClass().getClassLoader().getResource(PLAYER_IMAGE_PREFIX + colorName + ".png").toExternalForm());
        ImageView playerImageView = new ImageView();
        playerImageView.fitHeightProperty().bind(playerBox.prefHeightProperty());
        playerImageView.fitWidthProperty().bind(playerBox.prefHeightProperty());
        playerImageView.setImage(playerImage);
        playerBox.getChildren().add(playerImageView);
        Label plrLabel = new Label(player.name().toLowerCase());
        plrLabel.prefHeightProperty().bind(playerBox.prefHeightProperty());
        plrLabel.prefWidthProperty().bind(playerBox.prefWidthProperty().subtract(playerBox.prefHeightProperty()));
        plrLabel.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-padding: 0 0 0 5px;", playerColor));
        playerBox.getChildren().add(plrLabel);

        playerBox.setId(colorName);
        return playerBox;
    }

    private HBox buildAndGetPlayerHBoxBig(Config.PlayerColor player) {
        String playerColor = player.getHexValue().substring(0, 8).replace("0x", "#");
        HBox playerBox = new HBox();
        playerBox.setPrefHeight(100.0d * scale);
        playerBox.setPrefWidth(500.0d * scale);
        playerBox.maxHeightProperty().bind(playerBox.prefHeightProperty());
        playerBox.maxWidthProperty().bind(playerBox.prefWidthProperty());
        playerBox.setAlignment(Pos.TOP_RIGHT);
        playerBox.setStyle("-fx-background-color: white;");

        String colorName = player.name().toLowerCase();
        Image playerImage = new Image(getClass().getClassLoader().getResource(PLAYER_IMAGE_PREFIX + colorName + ".png").toExternalForm());
        ImageView playerImageView = new ImageView();
        playerImageView.fitHeightProperty().bind(playerBox.prefHeightProperty());
        playerImageView.fitWidthProperty().bind(playerBox.prefHeightProperty());
        playerImageView.setImage(playerImage);
        playerBox.getChildren().add(playerImageView);

        Label plrLabel = new Label(player.name().toLowerCase());
        plrLabel.prefHeightProperty().bind(playerBox.prefHeightProperty());
        plrLabel.prefWidthProperty().bind(playerBox.prefWidthProperty().subtract(playerBox.prefHeightProperty()));
        plrLabel.setAlignment(Pos.CENTER);
        plrLabel.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-font-family: Arial; -fx-font-size: 40px;", playerColor));
        playerBox.getChildren().add(plrLabel);

        playerBox.setId(colorName);
        return playerBox;
    }

    private void showActionChange(String text) {
        Label label = new Label();
        label.setText(text);
        label.setPrefWidth(400.0d);
        label.setPrefHeight(100.0d);
        label.getStyleClass().add("action-label");
        centerJavaFXRegion(labelStackPane, label);
        overlayStackPane.setStyle("-fx-background-color: black; -fx-opacity: 0.5;");

        stopHighlightClickableZonesAnimation();
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
                mapModel.resetHoverableZones();
                mapModel.setMapClickEnabled(true);
                highLightClickableZones();
            }
        });
        Timeline actionChangeTl = new Timeline(showActionChangeLabelKf, removeActionChangeLabelKf);
        playAnimation(actionChangeTl, true);
    }

    private void handleMapHover(MouseEvent mouseEvent) {
        int x = (int) mouseEvent.getX();
        int y = (int) mouseEvent.getY();
        mapModel.handleHover(x, y);
    }

    private void onActionButtonClick() {
        clickedActionButton.set(true);
        mapModel.setMapClickEnabled(false);
        switch (mapModel.getRisikoController().getAction()) {
            case ATTACK:
                initAttack();
                break;
            case MOVE:
                initMoveTroops(0);
                break;
            default:
                break;
        }
    }

    private void initMoveTroops(int minAmount) {
        mapModel.initializeMovingTroops(minAmount);
    }

    private void moveTroops(MoveTroopsDTO moveTroopsDTO) {
        TroopAmountPopup troopAmountPopup = new TroopAmountPopup(moveTroopsDTO.getMinAmount(), moveTroopsDTO.getMaxMovableTroops(), MOVE_TROOPS_TEXT);
        centerJavaFXRegion(labelStackPane, troopAmountPopup);
        addPopup(troopAmountPopup);
        troopAmountPopup.getConfirmBtn().setOnMouseClicked(click -> {
            // TODO should be different if attack / move
            mapModel.finishMovingTroops(troopAmountPopup.getTroopAmount());
            removePopup(troopAmountPopup);
            clickedActionButton.set(false);
        });
    }

    private void highLightClickableZones() {
        stopHighlightClickableZonesAnimation();
        Config.PlayerColor currPlayerColor = mapModel.getCurrPlayer();
        Color mix = colorService.mixColors(neighbourOverlayColor, colorService.getColor(currPlayerColor.getHexValue()));
        mapModel.updateClickableZones();
        KeyFrame highlightZonesKf = new KeyFrame(Duration.seconds(1), event -> mapModel.getClickableZones().forEach(zone -> setZoneActive(zone, mix, false)));
        KeyFrame removeHighlightedZonesKf = new KeyFrame(Duration.seconds(2), event -> removeAllOverlaidPixels());
        highlightClickableZonesTl = new Timeline(highlightZonesKf, removeHighlightedZonesKf);
        highlightClickableZonesTl.setCycleCount(30);
        playAnimation(highlightClickableZonesTl, false);
    }

    private void reinforcement() {
        int troopsToPlace = mapModel.initializeReinforcement();
        Label label = new Label();
        label.setPrefWidth(400.0d);
        label.setPrefHeight(100.0d);
        label.setMaxHeight(200.0d);
        label.getStyleClass().add("action-label");
        centerJavaFXRegion(labelStackPane, label);

        KeyFrame troopsReceivedKf = new KeyFrame(Duration.seconds(0), (event -> {
            this.labelStackPane.getChildren().add(label);
            label.setText(String.format(RECEIVED_TROOPS, troopsToPlace));
        }));

        KeyFrame setTroopsKf = new KeyFrame(Duration.seconds(2), (event -> label.setText(SELECT_ZONE_TO_DEPLOY_TROOPS)));
        KeyFrame removeLabelKf = new KeyFrame(Duration.seconds(4), event -> {
            labelStackPane.getChildren().remove(label);
            overlayStackPane.setStyle("-fx-background-color: transparent;");
            mapModel.resetHoverableZones();
            mapModel.setMapClickEnabled(true);
            highLightClickableZones();
        });

        Timeline reinforcementTl = new Timeline(troopsReceivedKf, setTroopsKf, removeLabelKf);
        playAnimation(reinforcementTl, true);
    }

    private void reinforcementClickHandler(MouseEvent mouseEvent) {
        if (!mapModel.isMapClickEnabled()) return;
        int x = (int) mouseEvent.getX();
        int y = (int) mouseEvent.getY();
        ReinforcementDTO reinforcementDTO = mapModel.reinforcementClick(x, y);
        if (reinforcementDTO == null) return;
        ZoneSquare sqr = reinforcementDTO.getZoneSquare();
        TroopAmountPopup troopAmountPopup = new TroopAmountPopup(0, reinforcementDTO.getMaxPlacableTroopAmount(), REINFORCEMENT_TEXT);
        centerJavaFXRegion(labelStackPane, troopAmountPopup);
        addPopup(troopAmountPopup);
        troopAmountPopup.getConfirmBtn().setOnMouseClicked(click -> {
            mapModel.placeReinforcementTroops(sqr.getName(), troopAmountPopup.getTroopAmount());
            removePopup(troopAmountPopup);
        });
    }

    private void initAttack() {
        AttackDTO attackDTO = mapModel.initializeAttack();
        if (attackDTO == null) return;
        int maxAttackerTroops = attackDTO.getMaxAttackerTroops();
        int maxDefenderTroops = attackDTO.getMaxDefenderTroops();
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
                    mapModel.notifyDefender();
                }
            }
        });
    }

    private void performAttack(int attackerTroops, int defenderTroops) {
        Label label = new Label();
        label.setPrefWidth(400.0d);
        label.setPrefHeight(200.0d);
        label.getStyleClass().add("action-label");
        centerJavaFXRegion(labelStackPane, label);

        FightDTO fightDTO = mapModel.handleFight(attackerTroops, defenderTroops);

        FightResultGrid grid = new FightResultGrid(fightDTO.getAttackerDiceRoll(), fightDTO.getDefenderDiceRoll(), scale);
        centerJavaFXRegion(labelStackPane, grid);

        String fightResultText = fightDTO.isAttackerWon() ? ATTACKER_WON : DEFENDER_WON;

        KeyFrame diceThrowKf = new KeyFrame(Duration.seconds(0), event -> {
            overlayStackPane.setStyle("-fx-background-color: black; -fx-opacity: 0.5;");
            this.labelStackPane.getChildren().add(grid);
        });
        KeyFrame winnerKf = new KeyFrame(Duration.seconds(5), (event -> {
            labelStackPane.getChildren().remove(grid);
            label.setText(String.format(fightResultText, fightDTO.getFightWinner(), fightDTO.getFightLoser()));
            this.labelStackPane.getChildren().add(label);
        }));
        double regionOwnerSeconds = fightDTO.isOverTookRegion() ? 7.0d : 5.0d;
        KeyFrame overTookRegionKf = new KeyFrame(Duration.seconds(regionOwnerSeconds), event -> {
            label.setText(String.format(REGION_OVERTAKEN, fightDTO.getFightWinner(), fightDTO.getOvertakenRegionName()));
        });
        KeyFrame finishFightKf = new KeyFrame(Duration.seconds(regionOwnerSeconds + 3.0d), event -> {
            clickedActionButton.set(false);
            this.labelStackPane.getChildren().remove(label);
            mapModel.finishFight(fightDTO);
        });

        Timeline fightTl = new Timeline();
        fightTl.getKeyFrames().add(diceThrowKf);
        fightTl.getKeyFrames().add(winnerKf);
        if (fightDTO.isOverTookRegion()) {
            fightTl.getKeyFrames().add(overTookRegionKf);
        }
        fightTl.getKeyFrames().add(finishFightKf);
        playAnimation(fightTl, true);
    }

    private void onMapClick(MouseEvent mouseEvent) {
        if (!mapModel.isMapClickEnabled()) return;
        int x = (int) mouseEvent.getX();
        int y = (int) mouseEvent.getY();
        mapModel.handleMapClick(x, y);
    }

    private void markNeighbours(List<ZoneSquare> neighbours) {
        neighbours.forEach(n -> {
            Color nOverLay = colorService.mixColors(neighbourOverlayColor, colorService.getColor(n.getColor().getHexValue()));
            setZoneActive(n, nOverLay, false);
        });
    }

    private void removeAllOverlaidPixels() {
        overlaidPixelsByZone.values().forEach(zone -> zone.forEach(pixel -> pw.setColor(pixel.getX(), pixel.getY(), transparentColor)));
        overlaidPixelsByZone.clear();
    }

    private void inactivateZone(String zoneName) {
        List<Pixel> overlaidPixelsToRemove = overlaidPixelsByZone.get(zoneName);
        if (overlaidPixelsToRemove == null) return;
        overlaidPixelsToRemove.forEach(pixel -> pw.setColor(pixel.getX(), pixel.getY(), transparentColor));
        overlaidPixelsByZone.remove(zoneName);
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
                    for (int k = 1; k < OVERLAY_EFFECT_SHIFT_PIXELS; k++) {
                        pw.setColor(x - k, y - k, mix);
                        overlaidPixels.add(new Pixel(x - k, y - k));
                    }
                    pw.setColor(x - OVERLAY_EFFECT_SHIFT_PIXELS, y - OVERLAY_EFFECT_SHIFT_PIXELS, currColor);
                    overlaidPixels.add(new Pixel(x - OVERLAY_EFFECT_SHIFT_PIXELS, y - OVERLAY_EFFECT_SHIFT_PIXELS));
                }

            }
        }
        overlaidPixelsByZone.put(sqr.getName(), overlaidPixels);
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
        final double stackPaneWidth = scale * DEFAULT_STACK_PANE_WIDTH;
        Notification notification = new Notification(type, text, stackPaneWidth);
        KeyFrame showNotificationKf = new KeyFrame(Duration.ZERO, event -> stackPane.getChildren().add(notification));
        KeyFrame removeNotificationKf = new KeyFrame(Duration.seconds(2), event -> stackPane.getChildren().remove(notification));
        Timeline notificationTl = new Timeline(showNotificationKf, removeNotificationKf);
        playAnimation(notificationTl, true);
    }

    private void gameWon(String winnerName) {
        gameWon.set(true);
        showNotification(NotificationType.INFO, String.format(GAME_WINNER, winnerName));
    }
}

