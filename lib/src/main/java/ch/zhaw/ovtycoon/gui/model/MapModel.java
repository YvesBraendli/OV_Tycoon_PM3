package ch.zhaw.ovtycoon.gui.model;

import ch.zhaw.ovtycoon.Config;
import ch.zhaw.ovtycoon.RisikoController;
import ch.zhaw.ovtycoon.data.DiceRoll;
import ch.zhaw.ovtycoon.data.Player;
import ch.zhaw.ovtycoon.gui.model.dto.ActivateZoneDTO;
import ch.zhaw.ovtycoon.gui.model.dto.AttackDTO;
import ch.zhaw.ovtycoon.gui.model.dto.DrawZoneDTO;
import ch.zhaw.ovtycoon.gui.model.dto.FightDTO;
import ch.zhaw.ovtycoon.gui.model.dto.MoveTroopsDTO;
import ch.zhaw.ovtycoon.gui.model.dto.ReinforcementAmountDTO;
import ch.zhaw.ovtycoon.gui.model.dto.ReinforcementDTO;
import ch.zhaw.ovtycoon.gui.model.dto.TooltipDTO;
import ch.zhaw.ovtycoon.gui.model.dto.ZoneTroopAmountDTO;
import ch.zhaw.ovtycoon.gui.model.dto.ZoneTroopAmountInitDTO;
import ch.zhaw.ovtycoon.gui.service.ColorService;
import ch.zhaw.ovtycoon.gui.service.MapLoaderService;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static ch.zhaw.ovtycoon.Config.PlayerColor.BLUE;
import static ch.zhaw.ovtycoon.Config.PlayerColor.GREEN;
import static ch.zhaw.ovtycoon.Config.PlayerColor.RED;

/**
 * Model for the zone map and the main view.
 * Has properties used by the {@link ch.zhaw.ovtycoon.gui.MapController} to update the view.
 */
public class MapModel {
    private final List<ZoneSquare> zoneSquares;
    private final MapLoaderService mapLoaderService;
    private final ColorService colorService;
    private final RisikoController risikoController;
    private final SimpleBooleanProperty sourceOrTargetNull = new SimpleBooleanProperty(true);
    private final SimpleBooleanProperty actionButtonVisible = new SimpleBooleanProperty(false);
    private final SimpleObjectProperty<Config.PlayerColor> currPlayer; // TODO remove default value after player init view merged
    private final SimpleStringProperty actionButtonText = new SimpleStringProperty();
    private final SimpleStringProperty showActionChange = new SimpleStringProperty();
    private final SimpleBooleanProperty darkenBackground = new SimpleBooleanProperty();
    private final SimpleStringProperty zoneToInactivate = new SimpleStringProperty();
    private final SimpleObjectProperty<ActivateZoneDTO> setZoneActive = new SimpleObjectProperty<>();
    private final SimpleBooleanProperty removeOverlaidPixels = new SimpleBooleanProperty();
    private final SimpleBooleanProperty stopAnimation = new SimpleBooleanProperty();
    private final SimpleObjectProperty<List<ZoneSquare>> highlightNeighbours = new SimpleObjectProperty<>();
    private final SimpleBooleanProperty actionButtonDisabled = new SimpleBooleanProperty();
    private final SimpleBooleanProperty removeUnnecessaryTooltips = new SimpleBooleanProperty();
    private final SimpleIntegerProperty moveTroops = new SimpleIntegerProperty();
    private final SimpleStringProperty gameWinner = new SimpleStringProperty();
    private final SimpleObjectProperty<DrawZoneDTO> drawZone = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<MoveTroopsDTO> openMoveTroopsPopup = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<TooltipDTO> showTooltip = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<TooltipDTO> removeTooltip = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Config.PlayerColor> highlightPlayer = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<ZoneTroopAmountInitDTO> initializeZoneTroopsText = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<ZoneTroopAmountDTO> updateZoneTroopsText = new SimpleObjectProperty<>();
    private final Color overlayColor = new Color(0.0d, 0.0d, 0.0d, 0.25d);
    private boolean mapClickEnabled = true;
    private List<ZoneSquare> clickableZones = new ArrayList<>();
    private List<ZoneSquare> hoverableZones = new ArrayList<>();
    private ZoneSquare source = null;
    private ZoneSquare target = null;
    private TooltipDTO currHovered = null;
    private final double scale;
    private final Scenario scenarioToBeTested = Scenario.PLAYER_ELIMINATED; // Only for initializing the zones and players to test certain scenarios, e.g. win game
    private ReinforcementAmountDTO currentReinforcement = null;

    /**
     * Creates an instance of the map model. Initializes {@link #zoneSquares} depending on the
     * passed image and scale.
     *
     * @param mapImage Image to be used as the game map
     * @param scale    for scaling the map for smaller screens
     */
    public MapModel(Image mapImage, double scale) {
        this.scale = scale;
        mapLoaderService = new MapLoaderService(mapImage, scale);
        colorService = new ColorService();
        zoneSquares = mapLoaderService.initZoneSquaresFromConfig();
        // TODO remove after player init view merged
        ArrayList<Config.PlayerColor> playersForTesting = new ArrayList<>();
        playersForTesting.add(RED);
        playersForTesting.add(BLUE);
        playersForTesting.add(GREEN);
        currPlayer = new SimpleObjectProperty<>(playersForTesting.get(playersForTesting.size() - 1));
        risikoController = new RisikoController(playersForTesting);
    }

    /**
     * Sets the initial values of {@link #currPlayer} and {@link #showActionChange}.
     * Colors all zones in the player color of the player they are owned by. Sets the troop amount of all
     * zones to the values provided by {@link #risikoController}.
     */
    public void setInitialValues() {
        hoverableZones = new ArrayList<>(zoneSquares);
        drawZonesInPlayerColors();
        initTroopAmountText();
        currPlayer.set(risikoController.getCurrentPlayer());
        showActionChange.set(risikoController.getAction().getActionName());
    }

    private void drawZonesInPlayerColors() {
        zoneSquares.forEach(zone -> {
            Color zoneColor = colorService.getColor(risikoController.getZoneOwner(zone.getName()).getHexValue());
            drawZone.set(new DrawZoneDTO(zone, zoneColor));
        });
    }

    /**
     * Sets {@link #highlightPlayer} to the player color of the player owning the {@link #target} zone.
     * Used to notify the defending player in an attack.
     */
    public void notifyDefender() {
        Config.PlayerColor defender = risikoController.getZoneOwner(target.getName());
        highlightPlayer.set(defender);
        highlightPlayer.set(null);
    }

    /**
     * Initializes an attack by getting the maximal available troops of the {@link #source} (attacking zone)
     * and {@link #target} (defending zone).
     *
     * @return DTO containing data for the attack to proceed, null if either {@link #source} or {@link #target} is null
     */
    public AttackDTO initializeAttack() {
        if (source == null || target == null) return null;
        int maxAttackerTroops = risikoController.getMaxTroopsForAttack(source.getName());
        int maxDefenderTroops = risikoController.getMaxTroopsForDefending(target.getName());
        return new AttackDTO(maxAttackerTroops, maxDefenderTroops);
    }

    /**
     * Starts the reinforcement phase.
     *
     * @return Amount of troops which can be placed during the reinforcement game phase.
     */
    public int initializeReinforcement() {
        int reinforcementsReceived = risikoController.getAmountOfReinforcements();
        currentReinforcement = new ReinforcementAmountDTO(reinforcementsReceived, 0);
        return reinforcementsReceived;
    }

    /**
     * Method for placing an amount of troops on a certain zone. Proceeds to next action if all available troops got placed,
     * else resets the map (resets darkening of background and highlighted zone). Updates the zones troop amount text with the passed amount.
     *
     * @param zoneSquareName Name of the zone on which the troops should be placed
     * @param amount         Amount of troops to be placed
     */
    public void placeReinforcementTroops(String zoneSquareName, int amount) {
        ZoneSquare sqr = getZsqByName(zoneSquareName);
        if (sqr == null || currentReinforcement == null) return;
        risikoController.reinforce(amount, zoneSquareName);
        currentReinforcement.addToAmountAlreadyPlaced(amount);
        updateZoneTroopsText.set(new ZoneTroopAmountDTO(zoneSquareName, risikoController.getZoneTroops(zoneSquareName)));
        if (currentReinforcement.getAmountAlreadyPlaced() == currentReinforcement.getTotalAmount()) {
            resetReinforcementDTO();
            mapClickEnabled = false;
            hoverableZones = new ArrayList<>();
            nextAction();
        } else {
            mapClickEnabled = true;
            hoverableZones = new ArrayList<>(zoneSquares);
            removeOverlaidPixels.set(true);
            removeOverlaidPixels.set(false);
            darkenBackground.set(false);
            updateClickableZones();
        }
    }

    /**
     * Generates a dto based on the passed x and y coordinate.
     * Gets the zone at the passed x and y coordinate and sets it active if the zone can be clicked.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return DTO containing the clicked zone square and the amount of troops which can be placed. Null
     * if no zone was found at the passed coordinates.
     */
    public ReinforcementDTO reinforcementClick(int x, int y) {
        ZoneSquare sqr = getZoneAtCoordinates(x, y);
        if (sqr == null || !clickableZones.contains(sqr)) return null;
        stopAnimation.set(true);
        stopAnimation.set(false);
        removeOverlaidPixels.set(true);
        removeOverlaidPixels.set(false);
        mapClickEnabled = false;

        clickableZones = new ArrayList<>();
        hoverableZones = new ArrayList<>();
        darkenBackground.set(true);
        setZoneActive.set(new ActivateZoneDTO(sqr, overlayColor, false));
        removeTooltip.set(currHovered);
        return new ReinforcementDTO(sqr, getPlacableTroopsForReinforcement());
    }

    /**
     * Initializes moving troops by signaling to open a new popup. Nothing happens if either
     * {@link #source} or {@link #target} is null.
     *
     * @param minAmount Minimal amount of troops which can be placed
     */
    public void initializeMovingTroops(int minAmount) {
        if (source == null || target == null) return;
        int maxMovableTroops = risikoController.getMaxMovableTroops(source.getName());
        openMoveTroopsPopup.set(new MoveTroopsDTO(minAmount, maxMovableTroops));
    }

    /**
     * Finishes moving troops by calling the moveUnits method of {@link #risikoController}.
     * Updates the involved zone's troop count. Resets the map's state (clickable / hoverable zones,
     * map click enabled, darkened background, overlaid pixels, source and target) to the values before
     * the troop moving started.
     *
     * @param amountToMove Amount of troops which should be moved from the {@link #source} zone to the {@link #target} zone.
     */
    public void finishMovingTroops(int amountToMove) {
        risikoController.moveUnits(source.getName(), target.getName(), amountToMove);
        int troopAmtNew = risikoController.getZoneTroops(target.getName());
        updateZoneTroopsText.set(new ZoneTroopAmountDTO(target.getName(), troopAmtNew));
        updateZoneTroopsText.set(new ZoneTroopAmountDTO(source.getName(), risikoController.getZoneTroops(source.getName())));
        mapClickEnabled = true;
        hoverableZones = new ArrayList<>(zoneSquares);
        updateClickableZones();
        source = null;
        target = null;
        sourceOrTargetNull.set(true);
        removeOverlaidPixels.set(true);
        removeOverlaidPixels.set(false);
        darkenBackground.set(false);
    }

    /**
     * Updates {@link #clickableZones} with the values from {@link #risikoController}.
     */
    public void updateClickableZones() {
        clickableZones = risikoController.getValidSourceZoneNames().stream()
                .map(zoneName -> getZsqByName(zoneName)).collect(Collectors.toList());
    }

    /**
     * Creates a {@link TooltipDTO} based on the zone at the passed coordinates.
     * Updates {@link #removeTooltip} and {@link #showTooltip} based on the zone hovered
     * and {@link #currHovered}.
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    public void handleHover(int x, int y) {
        if (hoverableZones.isEmpty()) return;
        ZoneSquare hoveredZone = getZoneAtCoordinates(x, y);
        boolean alreadyHovered = hoveredZone == null || (currHovered != null && hoveredZone.getName().replace("Zone", "Zone ").equals(currHovered.getTooltipText()));
        if (hoveredZone == null || !hoverableZones.contains(hoveredZone)) {
            removeTooltip.set(currHovered);
            removeTooltip.set(null);
            currHovered = null;
            return;
        } else if (alreadyHovered) {
            return;
        } else if (currHovered != null) { // remove old tooltip
            removeTooltip.set(currHovered);
            removeTooltip.set(null);
        }
        int tooltipX = hoveredZone.getCenter().getX();
        int tooltipY = hoveredZone.getCenter().getY() - 30;
        String tooltipText = hoveredZone.getName().replace("Zone", "Zone ");
        currHovered = new TooltipDTO(tooltipX, tooltipY, tooltipText);
        showTooltip.set(currHovered);
        showTooltip.set(null);
    }

    /**
     * Starts the fight and collects the data from the fight to a {@link FightDTO}.
     *
     * @param attackerTroops amount of troops used by the attacker
     * @param defenderTroops amount of troops used by the defender.
     * @return {@link FightDTO} with the data of the fight for processing.
     */
    public FightDTO handleFight(int attackerTroops, int defenderTroops) {
        FightDTO fightDTO = new FightDTO();
        Config.PlayerColor attacker = source.getColor();
        Config.PlayerColor defender = target.getColor();
        if (attacker == null || defender == null) return null;

        final AtomicReference<Config.PlayerColor> fightWinner = new AtomicReference<>(null);
        final AtomicBoolean zoneOvertaken = new AtomicBoolean(false);

        risikoController.getFightWinner().addListener((new ChangeListener<Config.PlayerColor>() {
            @Override
            public void changed(ObservableValue<? extends Config.PlayerColor> observable, Config.PlayerColor oldValue, Config.PlayerColor newValue) {
                if (newValue != null) {
                    fightWinner.set(newValue);
                    risikoController.getFightWinner().removeListener(this);
                }
            }
        }));

        risikoController.getZoneOvertaken().addListener((new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue != null) {
                    zoneOvertaken.set(newValue);
                    risikoController.getZoneOvertaken().removeListener(this);
                }
            }
        }));
        // TODO check if listener removed
        AtomicBoolean overtookRegion = new AtomicBoolean(false);
        risikoController.getNewRegionOwnerProperty().addListener((new ChangeListener<Config.PlayerColor>() {
            @Override
            public void changed(ObservableValue<? extends Config.PlayerColor> observable, Config.PlayerColor oldValue, Config.PlayerColor newValue) {
                if (newValue != null) {
                    overtookRegion.set(true);
                    risikoController.getNewRegionOwnerProperty().removeListener(this);
                }
            }
        }));

        DiceRoll fightRes = risikoController.runFight(source.getName(), target.getName(), attackerTroops, defenderTroops);
        fightDTO.setAttackerDiceRoll(fightRes.getAttackerRoll());
        fightDTO.setDefenderDiceRoll(fightRes.getDefenderRoll());
        boolean attackerWon = fightWinner.get().equals(attacker);
        String winner = attackerWon ? attacker.name().toLowerCase() : defender.name().toLowerCase();
        String loser = attackerWon ? defender.name().toLowerCase() : attacker.name().toLowerCase();
        fightDTO.setFightWinner(winner);
        fightDTO.setFightLoser(loser);
        fightDTO.setOvertookZone(zoneOvertaken.get());
        fightDTO.setOverTookRegion(overtookRegion.get());
        if (overtookRegion.get()) {
            fightDTO.setOvertakenRegionName(risikoController.getRegionOfZone(target.getName()).toString());
        }
        fightDTO.setAttacker(attacker.name().toLowerCase());
        fightDTO.setDefender(defender.name().toLowerCase());
        fightDTO.setAttackerWon(attackerWon);
        fightDTO.setAttackerTroops(attackerTroops);
        return fightDTO;
    }

    /**
     * Finishes fight by setting properties based on properties of the based dto.
     *
     * @param fightDTO dto containing data about the fight
     */
    public void finishFight(FightDTO fightDTO) {
        if (fightDTO.isOvertookZone()) {
            Color attackerColor = colorService.getColor(source.getColor().getHexValue());
            drawZone.set(new DrawZoneDTO(target, attackerColor));
            if (risikoController.getWinner() == null) {
                moveTroops.set(fightDTO.getAttackerTroops());
                moveTroops.set(-1);
            } else {
                gameWon(fightDTO.getAttacker());
            }
        } else {
            // update troops on zones after attack
            updateZoneTroopsText.set(new ZoneTroopAmountDTO(source.getName(), risikoController.getZoneTroops(source.getName())));
            updateZoneTroopsText.set(new ZoneTroopAmountDTO(target.getName(), risikoController.getZoneTroops(target.getName())));
            // ending attack
            mapClickEnabled = true;
            hoverableZones = new ArrayList<>(zoneSquares);
            source = null;
            target = null;
            removeOverlaidPixels.set(true);
            removeOverlaidPixels.set(false);
            darkenBackground.set(false);
            updateClickableZones();
            sourceOrTargetNull.set(source == null || target == null);
        }
    }

    /**
     * Sets {@link #gameWinner} to the name of the winner. Resets {@link #source}, {@link #target}, {@link #sourceOrTargetNull}
     * {@link #hoverableZones}, {@link #darkenBackground} and removes the overlaid pixels. Sets {@link #mapClickEnabled} to false.
     *
     * @param winnerName Name of the winner
     */
    private void gameWon(String winnerName) {
        mapClickEnabled = false;
        hoverableZones = new ArrayList<>(zoneSquares);
        source = null;
        target = null;
        removeOverlaidPixels.set(true);
        removeOverlaidPixels.set(false);
        darkenBackground.set(false);
        updateClickableZones();
        sourceOrTargetNull.set(source == null || target == null);
        gameWinner.set(winnerName);
    }

    /**
     * Gets the zone at the passed x and y coordinate and updates {@link #source}, {@link #target} and {@link #sourceOrTargetNull}
     * based on the values of source and target. Sets properties for visual feedback of the click.
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    public void handleMapClick(int x, int y) {
        if (source == null || (source != null && target != null)) {
            darkenBackground.set(false);
            removeOverlaidPixelsProperty().set(true);
            removeOverlaidPixelsProperty().set(false);
            updateClickableZones();
            source = null;
            target = null;
            sourceOrTargetNull.set(source == null || target == null);
        }
        ZoneSquare sqr = getZoneAtCoordinates(x, y);
        if (sqr != null && sqr.getBorder() != null && clickableZones.contains(sqr)) {
            stopAnimation.setValue(true);
            stopAnimation.set(false);

            removeOverlaidPixelsProperty().set(true);
            removeOverlaidPixelsProperty().set(false);
            if (source == null || sqr == source) {
                source = sqr;
                List<ZoneSquare> validTargets = getTargets(sqr);

                hoverableZones = new ArrayList<>(validTargets);
                hoverableZones.add(source);

                clickableZones = new ArrayList<>(hoverableZones);

                highlightNeighbours.set(validTargets);
                highlightNeighbours.set(null);
                setZoneActive.set(new ActivateZoneDTO(sqr, overlayColor, true));

                sourceOrTargetNull.set(source == null || target == null);
            } else {
                if (!getTargets(source).contains(sqr)) {
                    System.out.println("You cannot click on " + sqr.getName());
                    return;
                }
                target = sqr;
                clickableZones.stream()
                        .map(zone -> zone.getName())
                        .filter(zoneName -> !(zoneName.equals(source.getName()) || zoneName.equals(target.getName())))
                        .forEach(zoneToBeInactivated -> this.zoneToInactivate.set(zoneToBeInactivated));
                removeUnnecessaryTooltips.set(true);
                removeUnnecessaryTooltips.set(false);
                actionButtonDisabled.set(false);
                setZoneActive.set(new ActivateZoneDTO(sqr, colorService.getColor(sqr.getColor().getHexValue()), false));
                setZoneActive.set(new ActivateZoneDTO(source, overlayColor, true));
                hoverableZones = new ArrayList<>();
                clickableZones = new ArrayList<>();

                sourceOrTargetNull.set(source == null || target == null);
            }
            darkenBackground.set(true);
        } else {
            source = null;
            target = null;
            removeOverlaidPixels.set(true);
            removeOverlaidPixels.set(false);
            darkenBackground.set(false);
            hoverableZones = new ArrayList<>(zoneSquares);
            updateClickableZones();

            sourceOrTargetNull.set(source == null || target == null);
        }
        // uncomment for testing
        // System.out.println(String.format("Click handling took %d ms", System.currentTimeMillis() - startTime));
    }

    private List<ZoneSquare> getTargets(ZoneSquare sqr) {
        return risikoController.getValidTargetZoneNames(sqr.getName()).stream()
                .map(zoneName -> getZsqByName(zoneName)).collect(Collectors.toList());
    }

    private ZoneSquare getZsqByName(String name) {
        return zoneSquares.stream().filter(zsq -> name.equals(zsq.getName())).findFirst().orElse(null);
    }

    public List<ZoneSquare> getClickableZones() {
        return clickableZones;
    }

    public SimpleObjectProperty<TooltipDTO> showTooltipProperty() {
        return showTooltip;
    }

    public SimpleObjectProperty<Config.PlayerColor> highlightPlayerProperty() {
        return highlightPlayer;
    }

    // TODO check if can be omitted
    public SimpleObjectProperty<TooltipDTO> removeTooltipProperty() {
        return removeTooltip;
    }

    public SimpleIntegerProperty moveTroopsProperty() {
        return moveTroops;
    }

    public SimpleObjectProperty<DrawZoneDTO> drawZoneProperty() {
        return drawZone;
    }

    public SimpleStringProperty gameWinnerProperty() {
        return gameWinner;
    }

    public SimpleObjectProperty<MoveTroopsDTO> openMoveTroopsPopupProperty() {
        return openMoveTroopsPopup;
    }

    /**
     * Sets {@link #showActionChange} to the current action. Sets {@link #currPlayer} to the current player
     * if the player switched after switching the action. Resets {@link #source}, {@link #target}, {@link #sourceOrTargetNull}
     * and {@link #hoverableZones}. Sets {@link #actionButtonText} to the name of the current action.
     */
    public void nextAction() {
        source = null;
        target = null;
        sourceOrTargetNull.set(true);
        Config.PlayerColor currentPlayerBeforeActionSwitch = risikoController.getCurrentPlayer();
        if (risikoController.getAction() == Action.DEFEND) {
            resetReinforcementDTO();
        }
        risikoController.nextAction();
        Config.PlayerColor playerAfterActionSwitch = risikoController.getCurrentPlayer();
        if (currentPlayerBeforeActionSwitch != playerAfterActionSwitch) {
            currPlayer.set(risikoController.getCurrentPlayer()); // TODO ev bind to property in mapModel.getRisikoController()
        }
        Action next = risikoController.getAction();
        actionButtonVisible.set(next != Action.DEFEND);
        actionButtonText.set(risikoController.getAction().getActionName());
        mapClickEnabled = false;
        hoverableZones = new ArrayList<>();
        showActionChange.set(risikoController.getAction().getActionName());
    }

    private ZoneSquare getZoneAtCoordinates(int x, int y) {
        List<ZoneSquare> containsY = this.zoneSquares.stream()
                .filter(zone ->
                        zone.getBorder().stream().map(str -> str.getY()).collect(Collectors.toList()).contains(y)
                ).collect(Collectors.toList());
        return containsY.stream()
                .filter(zone -> zone.getBorder().stream()
                        .anyMatch(st -> st.getY() == y && st.getStartX() <= x && st.getEndX() >= x))
                .findFirst().orElse(null);
    }

    private void resetReinforcementDTO() {
        currentReinforcement = null;
    }

    /**
     * Sets {@link #hoverableZones} to a new array list containing {@link #zoneSquares}.
     */
    public void resetHoverableZones() {
        hoverableZones = new ArrayList<>(zoneSquares);
    }

    public List<ZoneSquare> getClickableZones() {
        return clickableZones;
    }

    public SimpleObjectProperty<TooltipDTO> showTooltipProperty() {
        return showTooltip;
    }

    public SimpleObjectProperty<Config.PlayerColor> highlightPlayerProperty() {
        return highlightPlayer;
    }

    // TODO check if can be omitted
    public SimpleObjectProperty<TooltipDTO> removeTooltipProperty() {
        return removeTooltip;
    }

    public SimpleIntegerProperty moveTroopsProperty() {
        return moveTroops;
    }

    public SimpleObjectProperty<DrawZoneDTO> drawZoneProperty() {
        return drawZone;
    }

    public SimpleStringProperty gameWinnerProperty() {
        return gameWinner;
    }

    public SimpleObjectProperty<MoveTroopsDTO> openMoveTroopsPopupProperty() {
        return openMoveTroopsPopup;
    }

    public SimpleBooleanProperty darkenBackgroundProperty() {
        return darkenBackground;
    }

    public SimpleStringProperty actionButtonTextProperty() {
        return actionButtonText;
    }

    public SimpleStringProperty showActionChangeProperty() {
        return showActionChange;
    }

    public List<ZoneSquare> getZoneSquares() {
        return zoneSquares;
    }

    public SimpleBooleanProperty sourceOrTargetNullProperty() {
        return sourceOrTargetNull;
    }

    public SimpleBooleanProperty actionButtonVisibleProperty() {
        return actionButtonVisible;
    }

    public SimpleObjectProperty<Config.PlayerColor> currPlayerProperty() {
        return currPlayer;
    }

    public Config.PlayerColor getCurrPlayer() {
        return currPlayer.get();
    }

    public boolean isMapClickEnabled() {
        return mapClickEnabled;
    }

    public void setMapClickEnabled(boolean mapClickEnabled) {
        this.mapClickEnabled = mapClickEnabled;
    }

    public RisikoController getRisikoController() {
        return risikoController;
    }

    public void setRisikoController(RisikoController risikoController) {
        this.risikoController = risikoController;
    }

    public SimpleObjectProperty<ActivateZoneDTO> setZoneActiveProperty() {
        return setZoneActive;
    }

    public SimpleBooleanProperty removeOverlaidPixelsProperty() {
        return removeOverlaidPixels;
    }

    public SimpleStringProperty zonesToInactivateProperty() {
        return zoneToInactivate;
    }

    public SimpleBooleanProperty stopAnimationProperty() {
        return stopAnimation;
    }

    public SimpleObjectProperty<List<ZoneSquare>> highlightNeighboursProperty() {
        return highlightNeighbours;
    }

    public SimpleBooleanProperty actionButtonDisabledProperty() {
        return actionButtonDisabled;
    }

    public SimpleBooleanProperty removeUnnecessaryTooltipsProperty() {
        return removeUnnecessaryTooltips;
    }

    public SimpleObjectProperty<ZoneTroopAmountInitDTO> initializeZoneTroopsTextProperty() {
        return initializeZoneTroopsText;
    }

    public ZoneSquare getSource() {
        return source;
    }

    public void setSource(ZoneSquare source) {
        this.source = source;
    }

    public ZoneSquare getTarget() {
        return target;
    }

    public void setTarget(ZoneSquare target) {
        this.target = target;
    }

    public TooltipDTO getCurrHovered() {
        return currHovered;
    }

    public void setCurrHovered(TooltipDTO currHovered) {
        this.currHovered = currHovered;
    }

    public void setHoverableZones(List<ZoneSquare> hoverableZones) {
        this.hoverableZones = hoverableZones;
    }

    public SimpleObjectProperty<ZoneTroopAmountDTO> updateZoneTroopsTextProperty() {
        return updateZoneTroopsText;
    }

    private int getPlacableTroopsForReinforcement() {
        if (currentReinforcement == null) return 0;
        return currentReinforcement.getTotalAmount() - currentReinforcement.getAmountAlreadyPlaced();
    }

    public double getScale() {
        return scale;
    }

    public List<ZoneSquare> getHoverableZones() {
        return hoverableZones;
    }

    private ZoneSquare getZoneAtCoordinates(int x, int y) {
        List<ZoneSquare> containsY = this.zoneSquares.stream()
                .filter(zone ->
                        zone.getBorder().stream().map(str -> str.getY()).collect(Collectors.toList()).contains(y)
                ).collect(Collectors.toList());
        return containsY.stream()
                .filter(zone -> zone.getBorder().stream()
                        .anyMatch(st -> st.getY() == y && st.getStartX() <= x && st.getEndX() >= x))
                .findFirst().orElse(null);
    }

    private List<ZoneSquare> getTargets(ZoneSquare sqr) {
        return risikoController.getValidTargetZoneNames(sqr.getName()).stream()
                .map(zoneName -> getZsqByName(zoneName)).collect(Collectors.toList());
    }

    private ZoneSquare getZsqByName(String name) {
        return zoneSquares.stream().filter(zsq -> name.equals(zsq.getName())).findFirst().orElse(null);
    }

    private void initTroopAmountText() {
        zoneSquares.forEach(zoneSquare -> {
            int troops = risikoController.getZoneTroops(zoneSquare.getName());
            int translateX = zoneSquare.getCenter().getX();
            int translateY = zoneSquare.getCenter().getY();
            initializeZoneTroopsText.set(new ZoneTroopAmountInitDTO(zoneSquare.getName(), troops, translateX, translateY));
        });
    }

    // TODO Should happen in backend: methods below will be removed as soon as player initialization is implemented in the backend -------------------------------

    private void addPlayerColorsToZones() {
        Random random = new Random();
        Player[] players = risikoController.getPlayers();
        if (scenarioToBeTested == Scenario.PLAYER_ELIMINATED) {
            for (int i = 0; i < zoneSquares.size() - 1; i++) {
                String name = zoneSquares.get(i).getName();
                int troops = random.nextInt(3) + 1;

                int randomInt = random.nextInt(2);
                risikoController.setZoneOwner(players[randomInt], zoneSquares.get(i).getName());
                Color zoneColor = colorService.getColor(players[randomInt].getColor().getHexValue());
                drawZone.set(new DrawZoneDTO(zoneSquares.get(i), zoneColor));
                risikoController.updateZoneTroops(name, troops);
            }
            // for testing elimination of player green
            String name = zoneSquares.get(42).getName();
            int troops = random.nextInt(3) + 1;
            risikoController.setZoneOwner(players[2], zoneSquares.get(42).getName());
            Color zoneColor = colorService.getColor(players[2].getColor().getHexValue());
            this.drawZone.set(new DrawZoneDTO(zoneSquares.get(42), zoneColor));
            risikoController.updateZoneTroops(name, troops);
        } else if (scenarioToBeTested == Scenario.WIN_GAME) {
            for (int i = 0; i < zoneSquares.size() - 1; i++) {
                String name = zoneSquares.get(i).getName();
                int troops = random.nextInt(3) + 1;
                risikoController.setZoneOwner(players[0], zoneSquares.get(i).getName());
                Color zoneColor = colorService.getColor(players[0].getColor().getHexValue());
                this.drawZone.set(new DrawZoneDTO(zoneSquares.get(i), zoneColor));
                risikoController.updateZoneTroops(name, troops);
            }
            String name = zoneSquares.get(42).getName();
            int troops = random.nextInt(3) + 1;
            risikoController.setZoneOwner(players[1], zoneSquares.get(42).getName());
            Color zoneColor = colorService.getColor(players[1].getColor().getHexValue());
            this.drawZone.set(new DrawZoneDTO(zoneSquares.get(42), zoneColor));
            risikoController.updateZoneTroops(name, troops);
        } else {
            for (int i = 0; i < zoneSquares.size(); i++) {
                int randomInt = random.nextInt(3);
                String name = zoneSquares.get(i).getName();
                int troops = random.nextInt(3) + 1;
                risikoController.setZoneOwner(players[randomInt], zoneSquares.get(i).getName());
                Color zoneColor = colorService.getColor(players[randomInt].getColor().getHexValue());
                this.drawZone.set(new DrawZoneDTO(zoneSquares.get(i), zoneColor));
                risikoController.updateZoneTroops(name, troops);
            }
        }
    }
}
