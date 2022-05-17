package ch.zhaw.ovtycoon.gui.controller;

import ch.zhaw.ovtycoon.Config;
import ch.zhaw.ovtycoon.gui.model.Action;
import ch.zhaw.ovtycoon.gui.model.CustomTimeline;
import ch.zhaw.ovtycoon.gui.model.HorizontalStripe;
import ch.zhaw.ovtycoon.gui.model.MapModel;
import ch.zhaw.ovtycoon.gui.model.customnodes.Notification;
import ch.zhaw.ovtycoon.gui.model.NotificationType;
import ch.zhaw.ovtycoon.gui.model.Pixel;
import ch.zhaw.ovtycoon.gui.model.ZoneSquare;
import ch.zhaw.ovtycoon.gui.model.customnodes.FightResultGrid;
import ch.zhaw.ovtycoon.gui.model.customnodes.TroopAmountPopup;
import ch.zhaw.ovtycoon.gui.model.customnodes.ZoneTooltip;
import ch.zhaw.ovtycoon.gui.model.dto.AttackDTO;
import ch.zhaw.ovtycoon.gui.model.dto.FightDTO;
import ch.zhaw.ovtycoon.gui.model.dto.MoveTroopsDTO;
import ch.zhaw.ovtycoon.gui.model.dto.ReinforcementDTO;
import ch.zhaw.ovtycoon.gui.model.dto.ZoneTroopAmountDTO;
import ch.zhaw.ovtycoon.gui.model.dto.ZoneTroopAmountInitDTO;
import ch.zhaw.ovtycoon.gui.service.ColorService;
import ch.zhaw.ovtycoon.gui.service.GameStateService;
import ch.zhaw.ovtycoon.model.Game;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controller for the zones-map-view.
 */
public class MainWindowController {
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
    private final List<ImageView> playersListItems = new ArrayList<>();
    private final SimpleBooleanProperty showingAnimation = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty showingPopup = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty gameWon = new SimpleBooleanProperty(false);
    private final Queue<CustomTimeline> waitingTimelines = new LinkedList<>();
    private final SimpleBooleanProperty clickedActionButton = new SimpleBooleanProperty();
    private final double scale;
    private final MapModel mapModel;
    @FXML
    private ImageView background;
    @FXML
    private AnchorPane anchorPane;
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
    @FXML
    private Button closeButton;
    @FXML
    private Button saveButton;
    private Timeline highlightClickableZonesTl = new Timeline();
    private PixelWriter pw;
    private PixelWriter mapPw;
    private Map<String, List<Pixel>> overlaidPixelsByZone = new HashMap<>();
    private ColorService colorService = new ColorService();
    private Parent parentSceneGraph;
    private GameStateService gameStateService = new GameStateService();

    /**
     * Creates an instance of the controller with the passed model.
     * Sets {@link #scale} to the value provided by the passed model.
     *
     * @param mapModel MapModel to be used by the controller
     */
    public MainWindowController(MapModel mapModel) {
        this.mapModel = mapModel;
        this.scale = mapModel.getScale();
    }

    /**
     * Initializes the zones map view. Rescales boxes and panes based on {@link #scale}.
     * Adds event handlers and binds nodes to properties provided by this class itself
     * or by {@link #mapModel}.
     */
    @FXML
    public void initialize() {
        if (scale != 1.0d) {
            rescale();
        }
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
        saveButton.setOnMouseClicked(event -> saveGame());
        closeButton.setOnMouseClicked(event -> closeGame());
        nextMoveBtn.disableProperty().bind(showingAnimation.or(showingPopup).or(gameWon));
        actionBtn.visibleProperty().bind(mapModel.actionButtonVisibleProperty().or(gameWon));
        actionBtn.disableProperty().bind(mapModel.actionButtonDisabledProperty().or(mapModel.sourceOrTargetNullProperty()).or(clickedActionButton));
        actionBtn.textProperty().bind(mapModel.actionButtonTextProperty());
        addPlayers();
        eliminatePlayer();
        initMapListeners();
        mapModel.setInitialValues(); // TODO ev clean up
        actionBtn.getStyleClass().add("buttonClass");
    }

    public void setParentSceneGraph(Parent parentSceneGraph) {
        this.parentSceneGraph = parentSceneGraph;
    }

    /**
     * Creates an instance of the javafx text node and adds it to the {@link #labelStackPane} at the position
     * provided by the passed dto and with the text string from the dto.
     *
     * @param zoneTroopAmountInitDTO dto containing properties to be set on the text node created by the method
     */
    private void initTroopAmountText(ZoneTroopAmountInitDTO zoneTroopAmountInitDTO) {
        Text troopAmountText = new Text();
        troopAmountText.setId(zoneTroopAmountInitDTO.getZoneName());
        troopAmountText.setText(Integer.toString(zoneTroopAmountInitDTO.getTroopAmount()));
        troopAmountText.setTranslateX(zoneTroopAmountInitDTO.getTranslateX());
        troopAmountText.setTranslateY(zoneTroopAmountInitDTO.getTranslateY());
        troopAmountText.setStyle("-fx-fill: lightgray;-fx-font-weight: bold;");
        labelStackPane.getChildren().add(troopAmountText);
    }

    /**
     * Updates the text property of the text node with the id equal to the zoneName property of the passed dto.
     *
     * @param zoneTroopAmountDTO dto containing the updated troop amount and the name of the zone of which the troop
     *                           amount should be updated.
     */
    private void updateTroopAmountText(ZoneTroopAmountDTO zoneTroopAmountDTO) {
        Text toUpdate = (Text) labelStackPane.getChildren().stream()
                .filter(node -> node instanceof Text && node.getId() != null && node.getId().equals(zoneTroopAmountDTO.getZoneName()))
                .findFirst()
                .orElse(null);
        if (toUpdate == null) return;
        toUpdate.setText(Integer.toString(zoneTroopAmountDTO.getTroopAmount()));
    }

    /**
     * Adds change listeners to properties provided by {@link #mapModel}
     */
    private void initMapListeners() {
        mapModel.showActionChangeProperty().addListener(((observable, oldValue, newValue) -> showActionChange(newValue)));
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

    /**
     * Adds a change listener to the eliminated player property provided by {@link #mapModel}
     */
    private void eliminatePlayer() {
        mapModel.getRisikoController().getEliminatedPlayerProperty().addListener(((observable, oldValue, newValue) -> {
            showNotification(NotificationType.WARNING, String.format(PLAYER_ELIMINATED, newValue.name().toLowerCase()));
            ImageView toBeEliminated = playersListItems.stream().filter((hBox -> hBox.getId().equals(newValue.name().toLowerCase()))).findAny().orElse(null);
            if (toBeEliminated != null) toBeEliminated.setStyle("-fx-opacity: 0.25;");
        }));
    }

    /**
     * Rescales the zones map view based on {@link #scale}
     */
    private void rescale() {
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

        background.setFitHeight(background.getFitHeight() * scale);
        background.setFitWidth(background.getFitWidth() * scale);

        nextMoveBtn.setLayoutX(nextMoveBtn.getLayoutX() * scale);
        nextMoveBtn.setLayoutY(nextMoveBtn.getLayoutY() * scale);
        nextMoveBtn.setPrefHeight(actionBtn.getPrefHeight() * scale);
        nextMoveBtn.setPrefWidth(actionBtn.getPrefWidth() * scale);
        nextMoveBtn.setStyle("-fx-font-family: Arial; -fx-font-weight: bold; -fx-font-size: 10px;");

        actionBtn.setLayoutX(actionBtn.getLayoutX() * scale);
        actionBtn.setLayoutY(actionBtn.getLayoutY() * scale);
        actionBtn.setPrefHeight(actionBtn.getPrefHeight() * scale);
        actionBtn.setPrefWidth(actionBtn.getPrefWidth() * scale);
        actionBtn.setStyle("-fx-font-family: Arial; -fx-font-weight: bold; -fx-font-size: 10px;");


        closeButton.setLayoutX(closeButton.getLayoutX() * scale);
        closeButton.setLayoutY(closeButton.getLayoutY() * scale);
        closeButton.setPrefHeight(closeButton.getPrefHeight() * scale);
        closeButton.setPrefWidth(closeButton.getPrefWidth() * scale);
        closeButton.setStyle("-fx-font-family: Arial; -fx-font-weight: bold; -fx-font-size: 7px;");

        saveButton.setLayoutX(saveButton.getLayoutX() * scale);
        saveButton.setLayoutY(saveButton.getLayoutY() * scale);
        saveButton.setPrefHeight(saveButton.getPrefHeight() * scale);
        saveButton.setPrefWidth(saveButton.getPrefWidth() * scale);
        saveButton.setStyle("-fx-font-family: Arial; -fx-font-weight: bold; -fx-font-size: 8px;");

        playersVBox.setLayoutX(playersVBox.getLayoutX() * scale);
        playersVBox.setLayoutY(playersVBox.getLayoutY() * scale);

    }

    /**
     * Adds a HBox to the {@link #labelStackPane} with information about the passed player.
     * Removes the HBox after 3 seconds.
     *
     * @param currPlayer player about whom the information should be displayed
     */
    private void highlightCurrPlayerLarge(Config.PlayerColor currPlayer) {
        ImageView playerBoxLarge = buildAndGetPlayerHBoxBig(currPlayer);
        playerBoxLarge.setTranslateX((labelStackPane.getMaxWidth() - playerBoxLarge.getFitWidth()) / 2.0d);
        playerBoxLarge.setTranslateY((labelStackPane.getMaxHeight() - playerBoxLarge.getFitHeight()) / 2.0d);
        KeyFrame showPlayerKf = new KeyFrame(Duration.seconds(0), event -> labelStackPane.getChildren().add(playerBoxLarge));
        KeyFrame removePlayerKf = new KeyFrame(Duration.seconds(3), event -> labelStackPane.getChildren().remove(playerBoxLarge));
        Timeline highlightPlayerTl = new Timeline(showPlayerKf, removePlayerKf);
        playAnimation(highlightPlayerTl, true);
    }

    /**
     * Stops the {@link #highlightClickableZonesTl} timeline.
     */
    private void stopHighlightClickableZonesAnimation() {
        highlightClickableZonesTl.stop();
    }

    /**
     * Plays the passed timeline. If a blocking timeline is already playing, the passed timeline
     * gets played as soon as the currently playing timeline finishes playing.
     *
     * @param tlToPlay   timeline to be played
     * @param isBlocking whether the passed timeline should block other later passed timelines
     *                   from being played until itself finished playing or not.
     */
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

    /**
     * Adds HBoxes representing the player colors provided by the {@link #mapModel} to the {@link #playersListItems} - list
     * abd the {@link #playersVBox}. Adds an action listener to the currPlayerProperty provided by the {@link #mapModel},
     * highlighting the set player when it changes.
     */
    private void addPlayers() {
        for (Config.PlayerColor playerColor : mapModel.getRisikoController().getPlayerColors()) {
            ImageView playerHBox = buildAndGetPlayerHBox(playerColor);
            playersListItems.add(playerHBox);
            playersVBox.getChildren().add(playerHBox);
        }

        mapModel.currPlayerProperty().addListener((obs, oldVal, newVal) -> {
            highlightCurrPlayerTile(oldVal.name().toLowerCase(), newVal.name().toLowerCase());
            highlightCurrPlayerLarge(newVal);
        });
    }

    /**
     * Highlights the HBox with the passed idNew and unhighlights (resets its size) of the HBox with the id equal to idOld.
     *
     * @param idOld id of the HBox to be unhighlighted
     * @param idNew id of the HBox to be highlighted
     */
    private void highlightCurrPlayerTile(String idOld, String idNew) {
        ImageView toBeUnHighlighted = playersListItems.stream().filter(box -> idOld.equals(box.getId())).findFirst().orElse(null);
        highlightPlayerTile(idNew);
        if (toBeUnHighlighted == null) return;
        toBeUnHighlighted.setFitWidth(70.0d * scale);
        toBeUnHighlighted.setFitHeight(70.0d * scale);
    }

    /**
     * Highlights the HBox with the passed id representing a player color by enlarging it.
     *
     * @param id id of the HBox to be highlighted
     */
    private void highlightPlayerTile(String id) {
        ImageView toBeHighlighted = playersListItems.stream().filter(box -> id.equals(box.getId())).findFirst().orElse(null);
        if (toBeHighlighted == null) return;
        toBeHighlighted.setFitWidth(100.0d * scale);
        toBeHighlighted.setFitHeight(100.0d * scale);
    }

    /**
     * Creates a HBox representing the passed player color, then returns it.
     *
     * @param player player color
     * @return HBox created
     */
    private ImageView buildAndGetPlayerHBox(Config.PlayerColor player) {
        String colorName = player.name().toLowerCase();
        Image playerImage = new Image(getClass().getClassLoader().getResource(PLAYER_IMAGE_PREFIX + colorName + ".png").toExternalForm());
        ImageView playerImageView = new ImageView();
        playerImageView.setFitWidth(70.0d * scale);
        playerImageView.setFitHeight(70.0d * scale);
        playerImageView.setImage(playerImage);
        playerImageView.setId(colorName);
        return playerImageView;

    }


    /**
     * Creates a big ImageView representing the passed player color, then returns it.
     *
     * @param player player color
     * @return Big ImageView created
     */
    private ImageView buildAndGetPlayerHBoxBig(Config.PlayerColor player) {
        String colorName = player.name().toLowerCase();
        Image playerImage = new Image(getClass().getClassLoader().getResource(PLAYER_IMAGE_PREFIX + colorName + ".png").toExternalForm());
        ImageView playerImageView = new ImageView();
        playerImageView.setFitWidth(200.0d * scale);
        playerImageView.setFitHeight(200.0d * scale);
        playerImageView.setImage(playerImage);
        playerImageView.setId(colorName);
        return playerImageView;
    }

    /**
     * Displays a label with the name of the newly set current action for 3 seconds.
     * Updates the click handler of {@link #labelStackPane} based on the newly set current action.
     *
     * @param text Name of the action after the action changed
     */
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

    /**
     * Handler for the mouse move event. Passes the coordinates of the mouseEvent parameter casted to integers
     * to the handleHover - method of {@link #mapModel}
     *
     * @param mouseEvent MouseEvent to be handled
     */
    private void handleMapHover(MouseEvent mouseEvent) {
        int x = (int) mouseEvent.getX();
        int y = (int) mouseEvent.getY();
        mapModel.handleHover(x, y);
    }

    /**
     * Click handler for the {@link #actionBtn}. Calls the initAttack or initMoveTroops - method based on
     * the current action provided by the {@link #mapModel}
     */
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

    /**
     * Redirects the handling of initializing move troops to the initializeMovingTroops - method of {@link #mapModel}.
     *
     * @param minAmount Minimal amount of troops which can be moved
     */
    private void initMoveTroops(int minAmount) {
        mapModel.initializeMovingTroops(minAmount);
    }

    /**
     * Adds a {@link TroopAmountPopup} to the {@link #labelStackPane} with data from the passed dto.
     * Adds a mouse click handler to the confirm button of the popup.
     *
     * @param moveTroopsDTO dto containing the data for the popup.
     */
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

    /**
     * Highlights all zones on which a mouse click is allowed during the current action.
     * Plays a timeline changing the color of the clickable zones on the {@link #mapCanvasOverlay} for 1 second.
     * The timeline stops after 30 cycles.
     */
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

    /**
     * Method for the reinforcement action.
     * Displays a label with the amount of troops received provided by the {@link #mapModel} for 2 seconds.
     * Displays an info label for 2 seconds.
     */
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

    /**
     * Click handler for a click on the {@link #labelStackPane} during the reinforcement action.
     * Gets a dto from the @{@link #mapModel}, adds a {@link TroopAmountPopup} to the {@link #labelStackPane}
     * and adds a click handler to the popup's confirm button, removing the popup as soon as the button gets clicked.
     *
     * @param mouseEvent MouseEvent to be handled
     */
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

    /**
     * Initialized an attack based on the dto received by the {@link #mapModel}.
     * Prompts the attacker and defender for the troop amount to be used in the fight.
     * Notifies the defender. Performs the attack as soon as the attacker and defender
     * both confirmed the amount of troops to be used.
     */
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

    /**
     * Displays an attack based on a dto received from the {@link #mapModel}.
     * Plays a timeline with the fight result.
     *
     * @param attackerTroops amount of troops used by the attacker
     * @param defenderTroops amount of troops used by the defender
     */
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

    /**
     * Handles a click on the {@link #labelStackPane} during the move and attack action.
     * Casts the x and y coordinate from the mouseEvent parameter to integers and passes
     * those values to the handleMapClick - method from the {@link #mapModel}.
     *
     * @param mouseEvent MouseEvent to be handled
     */
    private void onMapClick(MouseEvent mouseEvent) {
        if (!mapModel.isMapClickEnabled()) return;
        int x = (int) mouseEvent.getX();
        int y = (int) mouseEvent.getY();
        mapModel.handleMapClick(x, y);
    }

    /**
     * Marks the pixels of each {@link ZoneSquare} by setting their color on the {@link #mapCanvasOverlay}
     * to a mix of their own color with the {@link #neighbourOverlayColor}.
     *
     * @param neighbours list of {@link ZoneSquare} to be marked
     */
    private void markNeighbours(List<ZoneSquare> neighbours) {
        neighbours.forEach(n -> {
            Color nOverLay = colorService.mixColors(neighbourOverlayColor, colorService.getColor(n.getColor().getHexValue()));
            setZoneActive(n, nOverLay, false);
        });
    }

    /**
     * Clears the {@link #mapCanvasOverlay} by setting the color of each pixel in the {@link #overlaidPixelsByZone} - map
     * to {@link #transparentColor}. Clears the {@link #overlaidPixelsByZone} - map afterwards.
     */
    private void removeAllOverlaidPixels() {
        overlaidPixelsByZone.values().forEach(zone -> zone.forEach(pixel -> pw.setColor(pixel.getX(), pixel.getY(), transparentColor)));
        overlaidPixelsByZone.clear();
    }

    /**
     * "Inactivates" the zone with the passed name by setting the drawn pixels on the {@link #mapCanvasOverlay} belonging to the
     * zone to {@link #transparentColor}. Removes the value of the passed zoneName from the {@link #overlaidPixelsByZone} - map afterwards.
     *
     * @param zoneName
     */
    private void inactivateZone(String zoneName) {
        List<Pixel> overlaidPixelsToRemove = overlaidPixelsByZone.get(zoneName);
        if (overlaidPixelsToRemove == null) return;
        overlaidPixelsToRemove.forEach(pixel -> pw.setColor(pixel.getX(), pixel.getY(), transparentColor));
        overlaidPixelsByZone.remove(zoneName);
    }

    /**
     * Sets a zone active by drawing the pixels belonging to the zone on the {@link #mapCanvasOverlay}.
     * All drawn pixels get added to the {@link #overlaidPixelsByZone} - map with the name of the zone as key.
     * If the shift parameter is true, the zones pixels get drawn shifted by {@link #OVERLAY_EFFECT_SHIFT_PIXELS},
     * and the pixels between the zone pixel and the shifted pixel get drawn in a color mixed from the zone color
     * and the passed overlay color, creating a 3D like effect.
     *
     * @param sqr          Zone which should be set active
     * @param overlayColor Color for the overlay shift effect.
     * @param shift        If zone activated should get shifted (highlighted with a 3D - like effect or not)
     */
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
                    for (int k = 1; k < OVERLAY_EFFECT_SHIFT_PIXELS; k++) { // TODO check for index out of bounds
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

    /**
     * Draws the pixels of a {@link ZoneSquare} in the passed color on the {@link #mapCanvas}, then sets the color
     * of the passed zone to the passed color.
     *
     * @param sqr Zone to be drawn
     * @param c   Color in which the passed zone should be drawn
     */
    private void drawZone(ZoneSquare sqr, Color c) {
        for (HorizontalStripe str : sqr.getBorder()) {
            int y = str.getY();
            for (int x = str.getStartX(); x <= str.getEndX(); x++) {
                mapPw.setColor(x, y, c);
            }
        }
        sqr.setColor(colorService.getPlayerColor(c.toString()));
    }

    /**
     * Adds a {@link TroopAmountPopup} to the {@link #labelStackPane} with the properties passed to the method
     * prompting the user to enter an amount of troops. The minimal amount of troops is set to 1.
     *
     * @param maxAmt maximal amount of troops which can be set
     * @param text   Text to be displayed on the popup.
     * @return created troop amount popup
     */
    private TroopAmountPopup promptUserForTroopAmount(int maxAmt, String text) {
        TroopAmountPopup popup = new TroopAmountPopup(1, maxAmt, text);
        centerJavaFXRegion(labelStackPane, popup);
        addPopup(popup);
        return popup;
    }

    /**
     * Sets the translateX and translateY properties to a certain value
     * so the passed JavaFX region will be centered on the passed JavaFX pane when its added to it.
     *
     * @param pane   JavaFX pane on which the region should be centered
     * @param region JavaFX region to be centered
     */
    private void centerJavaFXRegion(Pane pane, Region region) {
        region.setTranslateX((pane.getMaxWidth() - region.getPrefWidth()) / 2.0d);
        region.setTranslateY((pane.getMaxHeight() - region.getPrefHeight()) / 2.0d);
    }

    /**
     * Sets the {@link #showingPopup} property to true and adds the passed {@link TroopAmountPopup}
     * to the {@link #labelStackPane}
     *
     * @param popup troop amount popup to be added to the {@link #labelStackPane}
     */
    private void addPopup(TroopAmountPopup popup) {
        this.showingPopup.set(true);
        labelStackPane.getChildren().add(popup);
    }

    /**
     * Sets the {@link #showingPopup} property to false and removes the passed {@link TroopAmountPopup}
     * from the {@link #labelStackPane}
     *
     * @param popup troop amount popup to be removed from the {@link #labelStackPane}
     */
    private void removePopup(TroopAmountPopup popup) {
        this.showingPopup.set(false);
        labelStackPane.getChildren().remove(popup);
    }

    /**
     * Creates a {@link Notification} with the properties passed. Plays a timeline showing the notification for 2 seconds.
     *
     * @param type Type of the notification. See {@link NotificationType}.
     * @param text Text to be displayed in the notification.
     */
    private void showNotification(NotificationType type, String text) {
        final double stackPaneWidth = scale * DEFAULT_STACK_PANE_WIDTH;
        Notification notification = new Notification(type, text, stackPaneWidth);
        KeyFrame showNotificationKf = new KeyFrame(Duration.ZERO, event -> stackPane.getChildren().add(notification));
        KeyFrame removeNotificationKf = new KeyFrame(Duration.seconds(2), event -> stackPane.getChildren().remove(notification));
        Timeline notificationTl = new Timeline(showNotificationKf, removeNotificationKf);
        playAnimation(notificationTl, true);
    }

    /**
     * Sets the {@link #gameWon} property to true and shows a notification with the name of the winner.
     * @param winnerName Name of the game winner.
     */
    private void gameWon(String winnerName) {
        gameWon.set(true);
        showNotification(NotificationType.INFO, String.format(GAME_WINNER, winnerName));
    }


    public void closeGame(){
        Platform.exit();
    }

    public void saveGame(){
        this.gameStateService.saveGameState(mapModel.getRisikoController().getGame());
        showNotification(NotificationType.INFO, "Spielstand gespeichert");
    }

}
