package ch.zhaw.ovtycoon.gui.model;

import ch.zhaw.ovtycoon.Config;
import ch.zhaw.ovtycoon.RisikoController;
import ch.zhaw.ovtycoon.data.DiceRoll;
import ch.zhaw.ovtycoon.gui.MapController;
import ch.zhaw.ovtycoon.gui.service.ColorService;
import ch.zhaw.ovtycoon.gui.service.MapLoaderService;
import ch.zhaw.ovtycoon.model.Player;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static ch.zhaw.ovtycoon.Config.PlayerColor.BLUE;
import static ch.zhaw.ovtycoon.Config.PlayerColor.GREEN;
import static ch.zhaw.ovtycoon.Config.PlayerColor.RED;

public class MapModel {
    private final List<ZoneSquare> zoneSquares;
    private final MapLoaderService mapLoaderService;
    private final ColorService colorService;
    private final SimpleBooleanProperty sourceOrTargetNull = new SimpleBooleanProperty(true);
    private final SimpleBooleanProperty actionButtonVisible = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty showingPopup = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty gameWon = new SimpleBooleanProperty(false);
    private final SimpleObjectProperty<Player> currPlayer = new SimpleObjectProperty<>(new Player(""));
    private final RisikoController risikoController = new RisikoController(3);
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

    private final Color overlayColor = new Color(0.0d, 0.0d, 0.0d, 0.25d);
    private boolean mapClickEnabled = true;
    private ZoneSquare source = null;
    private ZoneSquare target = null;

    private MapController mapController; // todo remove as soon as cleanup complete

    private final SimpleObjectProperty<Action> actionChange = new SimpleObjectProperty<>();

    public MapModel(Image mapImage, double scale, MapController mapController) {
        mapLoaderService = new MapLoaderService(mapImage, scale);
        colorService = new ColorService();
        zoneSquares = mapLoaderService.initZoneSquaresFromConfig();
        initPlayers();
        this.mapController = mapController;
    }

    public void initializeMovingTroops(int minAmount) {
        if (source == null || target == null) return;
        int maxMovableTroops = risikoController.getMaxMovableTroops(source.getName());
        openMoveTroopsPopup.set(new MoveTroopsDTO(minAmount, maxMovableTroops));
    }

    public void finishMovingTroops(int amountToMove) {
        risikoController.moveUnits(source.getName(), target.getName(), amountToMove);
        int troopAmtNew = risikoController.getZoneTroops(target.getName());
        target.updateTroopsAmount(Integer.toString(troopAmtNew));
        source.updateTroopsAmount(Integer.toString(risikoController.getZoneTroops(source.getName())));
        mapClickEnabled = true;
        mapController.setMapClickEnabled(true);
        mapController.setHoverableZones(new ArrayList<>(zoneSquares));
        mapController.updateClickableZones();
        source = null;
        target = null;
        removeOverlaidPixels.set(true);
        removeOverlaidPixels.set(false);
        darkenBackground.set(false);
    }

    public void handleHover(int x, int y) {

    }

    public FightDTO handleFight(int attackerTroops, int defenderTroops) {
        FightDTO fightDTO = new FightDTO();
        Player attacker = playerColorToPlayer(source.getColor());
        Player defender = playerColorToPlayer(target.getColor());
        if (attacker == null || defender == null) return null;

        final AtomicReference<Player> fightWinner= new AtomicReference<>(null);
        final AtomicBoolean zoneOvertaken = new AtomicBoolean(false);

        risikoController.getFightWinner().addListener((new ChangeListener<Player>() {
            @Override
            public void changed(ObservableValue<? extends Player> observable, Player oldValue, Player newValue) {
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
                    System.out.println(newValue);
                    risikoController.getNewRegionOwnerProperty().removeListener(this);
                }
            }
        }));

        DiceRoll fightRes = risikoController.runFight(source.getName(), target.getName(), attackerTroops, defenderTroops);
        fightDTO.setAttackerDiceRoll(fightRes.getAttackerRoll());
        fightDTO.setDefenderDiceRoll(fightRes.getDefenderRoll());

        boolean attackerWon =  fightWinner.get().equals(attacker);

        String winner = attackerWon ? attacker.getName() : defender.getName();
        String loser = attackerWon ? defender.getName() : attacker.getName();
        fightDTO.setFightWinner(winner);
        fightDTO.setFightLoser(loser);

        fightDTO.setOvertookZone(zoneOvertaken.get());
        fightDTO.setOverTookRegion(overtookRegion.get());

        fightDTO.setAttacker(attacker.getName());
        fightDTO.setDefender(defender.getName());

        fightDTO.setAttackerWon(attackerWon);

        fightDTO.setAttackerTroops(attackerTroops);


        return fightDTO;
    }

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
            source.updateTroopsAmount(Integer.toString(risikoController.getZoneTroops(source.getName())));
            target.updateTroopsAmount(Integer.toString(risikoController.getZoneTroops(target.getName())));

            // ending attack
            mapClickEnabled = true;
            mapController.setMapClickEnabled(true);
            mapController.setHoverableZones(new ArrayList<>(zoneSquares));
            source = null;
            target = null;
            removeOverlaidPixels.set(true);
            removeOverlaidPixels.set(false);
            darkenBackground.set(false);
            mapController.updateClickableZones();
            sourceOrTargetNull.set(source == null || target == null);
        }
    }

    private void gameWon(String winnerName) {
        mapClickEnabled = false;
        mapController.setMapClickEnabled(false);
        mapController.setHoverableZones(new ArrayList<>(zoneSquares));
        source = null;
        target = null;
        removeOverlaidPixels.set(true);
        removeOverlaidPixels.set(false);
        darkenBackground.set(false);
        mapController.updateClickableZones();
        sourceOrTargetNull.set(source == null || target == null);
        gameWinner.set(winnerName);
    }

    private Player playerColorToPlayer(Config.PlayerColor playerColor) {
        for (Player player : risikoController.getPlayers()) {
            if (player.getColor() == playerColor) {
                return player;
            }
        }
        return null;
    }

    public void handleMapClick(int x, int y) {
        if (source == null || (source != null && target != null)) {
            darkenBackground.set(false);

            // todo clean up
            removeOverlaidPixelsProperty().set(true);
            removeOverlaidPixelsProperty().set(false);

            mapController.updateClickableZones();
            source = null;
            target = null;
            sourceOrTargetNull.set(source == null || target == null);
        }
        // uncomment for testing
        // long startTime = System.currentTimeMillis();

        ZoneSquare sqr = getZoneAtCoordinates(x, y);
        if (sqr != null && sqr.getBorder() != null && mapController.getClickableZones().contains(sqr)) {
            stopAnimation.setValue(true);
            stopAnimation.set(false);
            removeOverlaidPixelsProperty().set(true);
            removeOverlaidPixelsProperty().set(false);
            if (source == null || sqr == source) {
                source = sqr;
                List<ZoneSquare> validTargets = getTargets(sqr);

                List<ZoneSquare> hoverableZones = new ArrayList<>(validTargets);
                hoverableZones.add(source);
                mapController.setHoverableZones(hoverableZones);

                mapController.setClickableZones(new ArrayList<>(hoverableZones));

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
                mapController.getOverlaidZones().keySet().stream().filter((zone) -> !(zone.equals(source.getName()) || zone.equals(target.getName()))).collect(Collectors.toList()).forEach(zoneToInactivate -> this.zoneToInactivate.set(zoneToInactivate));
                removeUnnecessaryTooltips.set(true);
                removeUnnecessaryTooltips.set(false);
                // TODO bind in controller
                actionButtonDisabled.set(false);
                setZoneActive.set(new ActivateZoneDTO(sqr, colorService.getColor(sqr.getColor().getHexValue()), false));
                setZoneActive.set(new ActivateZoneDTO(source, overlayColor, true));
                mapController.setHoverableZones(new ArrayList<>());
                mapController.setClickableZones(new ArrayList<>());

                sourceOrTargetNull.set(source == null || target == null);
            }
            darkenBackground.set(true);
        } else {
            source = null;
            target = null;
            removeOverlaidPixels.set(true);
            removeOverlaidPixels.set(false);
            darkenBackground.set(false);
            mapController.setHoverableZones(new ArrayList<>(zoneSquares));
            mapController.updateClickableZones();

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

    public SimpleObjectProperty<TooltipDTO> showTooltipProperty() {
        return showTooltip;
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

    public void emitInitialVals() {
        currPlayer.set(risikoController.getCurrentPlayer());
        showActionChange.set(risikoController.getAction().getActionName());
    }

    public void nextAction() {
        source = null;
        target = null;
        sourceOrTargetNull.set(true);
        Player currentPlayerBeforeActionSwitch = risikoController.getCurrentPlayer();
        risikoController.nextAction();
        Player playerAfterActionSwitch = risikoController.getCurrentPlayer();
        if (currentPlayerBeforeActionSwitch != playerAfterActionSwitch) {
            currPlayer.set(risikoController.getCurrentPlayer()); // TODO ev bind to property in mapModel.getRisikoController()
        }
        Action next = risikoController.getAction();
        actionButtonVisible.set(next != Action.DEFEND);
        actionButtonText.set(risikoController.getAction().getActionName());
        showActionChange.set(risikoController.getAction().getActionName());
    }

    public ZoneSquare getZoneAtCoordinates(int x, int y) {
        List<ZoneSquare> containsY = this.zoneSquares.stream()
                .filter(zone ->
                        zone.getBorder().stream().map(str -> str.getY()).collect(Collectors.toList()).contains(y)
                ).collect(Collectors.toList());
        return containsY.stream()
                .filter(zone -> zone.getBorder().stream()
                        .anyMatch(st -> st.getY() == y && st.getStartX() <= x && st.getEndX() >= x))
                .findFirst().orElse(null);
    }

    public SimpleBooleanProperty darkenBackgroundProperty() {
        return darkenBackground;
    }

    public void setSource(ZoneSquare source) {
        this.source = source;
    }

    public void setTarget(ZoneSquare target) {
        this.target = target;
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

    public boolean isSourceOrTargetNull() {
        return sourceOrTargetNull.get();
    }

    public SimpleBooleanProperty sourceOrTargetNullProperty() {
        return sourceOrTargetNull;
    }

    public boolean isActionButtonVisible() {
        return actionButtonVisible.get();
    }

    public SimpleBooleanProperty actionButtonVisibleProperty() {
        return actionButtonVisible;
    }

    public boolean isShowingPopup() {
        return showingPopup.get();
    }

    public SimpleBooleanProperty showingPopupProperty() {
        return showingPopup;
    }

    public boolean isGameWon() {
        return gameWon.get();
    }

    public SimpleBooleanProperty gameWonProperty() {
        return gameWon;
    }

    public Player getCurrPlayer() {
        return currPlayer.get();
    }

    public SimpleObjectProperty<Player> currPlayerProperty() {
        return currPlayer;
    }

    public boolean isMapClickEnabled() {
        return mapClickEnabled;
    }

    public ZoneSquare getSource() {
        return source;
    }

    public ZoneSquare getTarget() {
        return target;
    }

    public RisikoController getRisikoController() {
        return risikoController;
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

    // todo to backend
    private void initPlayers() {
        risikoController.getPlayers()[0] = new Player("Player a");
        risikoController.getPlayers()[0].setColor(RED);
        risikoController.getPlayers()[1] = new Player("Player b");
        risikoController.getPlayers()[1].setColor(BLUE);
        risikoController.getPlayers()[2] = new Player("Player c");
        risikoController.getPlayers()[2].setColor(GREEN);
    }
}
