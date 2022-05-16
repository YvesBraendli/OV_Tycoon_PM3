package ch.zhaw.ovtycoon.gui;

import ch.zhaw.ovtycoon.Config;
import ch.zhaw.ovtycoon.RisikoController;
import ch.zhaw.ovtycoon.gui.model.Action;
import ch.zhaw.ovtycoon.gui.model.MapModel;
import ch.zhaw.ovtycoon.gui.model.ZoneSquare;
import ch.zhaw.ovtycoon.gui.model.dto.FightDTO;
import ch.zhaw.ovtycoon.gui.model.dto.TooltipDTO;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;

import static ch.zhaw.ovtycoon.Config.PlayerColor.*;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link MapModel} class.
 */
public class MapModelTest {
    private MapModel uut;
    private final int zonesAmount = 43;
    private final double scaleDefault = 1.0d;
    private final double scaleSmall = 0.7d;
    private Image zoneMapImage;


    @Before
    public void platformStart() {
        JavaFXPlatformRunnable.run();
        zoneMapImage = new Image(getClass().getClassLoader().getResource("zvv_zones_v7.png").toExternalForm());
    }

    public void initializeUUTWithDefaultScale() {
        RisikoController mockRisikoController = mock(RisikoController.class);
        Config.PlayerColor[] playersForTesting = {RED, BLUE, GREEN};
        when(mockRisikoController.getPlayerColors()).thenReturn(playersForTesting);
        uut = new MapModel(zoneMapImage, scaleDefault, mockRisikoController);
    }

    public void initializeUUTWithSmallScale() {
        RisikoController mockRisikoController = mock(RisikoController.class);
        Config.PlayerColor[] playersForTesting = {RED, BLUE, GREEN};
        when(mockRisikoController.getPlayerColors()).thenReturn(playersForTesting);
        uut = new MapModel(zoneMapImage, scaleSmall, mockRisikoController);
    }

    @Test
    public void testZoneSquareAmount() {
        initializeUUTWithDefaultScale();
        assertEquals(uut.getZoneSquares().size(), zonesAmount);
    }

    @Test
    public void testMapHoverOverZoneCenter() {
        initializeUUTWithDefaultScale();
        final String expectedTooltipText = "Zone 110";
        ZoneSquare toHover = uut.getZoneSquares().stream().filter(zone -> zone.getName().equals("Zone110")).findFirst().orElse(null);
        assertNotNull(toHover);
        int hoverXCoordinate = toHover.getCenter().getX();
        int hoverYCoordinate = toHover.getCenter().getY();
        uut.showTooltipProperty().addListener((new ChangeListener<TooltipDTO>() {
            @Override
            public void changed(ObservableValue<? extends TooltipDTO> observable, TooltipDTO oldValue, TooltipDTO newValue) {
                if (newValue != null) {
                    assertEquals(expectedTooltipText, newValue.getTooltipText());
                    assertEquals(uut.getCurrHovered().getTooltipText(), expectedTooltipText);
                    uut.showTooltipProperty().removeListener(this);
                }
            }
        }));
        uut.handleHover(hoverXCoordinate, hoverYCoordinate);
    }

    @Test
    public void testHoverChangeZones() {
        initializeUUTWithDefaultScale();
        final TooltipDTO previouslyHovered = new TooltipDTO(0, 0, "Zone 154");
        uut.setCurrHovered(previouslyHovered);
        final String expectedTooltipText = "Zone 110";
        ZoneSquare toHover = uut.getZoneSquares().stream().filter(zone -> zone.getName().equals("Zone110")).findFirst().orElse(null);
        assertNotNull(toHover);
        int hoverXCoordinate = toHover.getCenter().getX();
        int hoverYCoordinate = toHover.getCenter().getY();
        uut.removeTooltipProperty().addListener((new ChangeListener<TooltipDTO>() {
            @Override
            public void changed(ObservableValue<? extends TooltipDTO> observable, TooltipDTO oldValue, TooltipDTO newValue) {
                assertEquals(previouslyHovered, newValue);
                uut.removeTooltipProperty().removeListener(this);
            }
        }));
        uut.showTooltipProperty().addListener((new ChangeListener<TooltipDTO>() {
            @Override
            public void changed(ObservableValue<? extends TooltipDTO> observable, TooltipDTO oldValue, TooltipDTO newValue) {
                assertEquals(expectedTooltipText, newValue.getTooltipText());
                uut.showTooltipProperty().removeListener(this);
            }
        }));
        uut.handleHover(hoverXCoordinate, hoverYCoordinate);
    }

    @Test
    public void hoverOverZoneWithSmallerScale() {
        initializeUUTWithSmallScale();
        final String expectedTooltipText = "Zone 110";
        ZoneSquare toHover = uut.getZoneSquares().stream().filter(zone -> zone.getName().equals("Zone110")).findFirst().orElse(null);
        assertNotNull(toHover);
        int hoverXCoordinate = toHover.getCenter().getX();
        int hoverYCoordinate = toHover.getCenter().getY();
        uut.showTooltipProperty().addListener((new ChangeListener<TooltipDTO>() {
            @Override
            public void changed(ObservableValue<? extends TooltipDTO> observable, TooltipDTO oldValue, TooltipDTO newValue) {
                if (newValue != null) {
                    assertEquals(expectedTooltipText, newValue.getTooltipText());
                    assertEquals(uut.getCurrHovered().getTooltipText(), expectedTooltipText);
                    uut.showTooltipProperty().removeListener(this);
                }
            }
        }));
        uut.handleHover(hoverXCoordinate, hoverYCoordinate);
    }

    @Test
    public void testMapHoverOverNoZone() {
        initializeUUTWithDefaultScale();
        final int xCoordinateWithNoZone = 0;
        final int yCoordinateWithNoZone = 0;
        uut.showTooltipProperty().addListener((new ChangeListener<TooltipDTO>() {
            @Override
            public void changed(ObservableValue<? extends TooltipDTO> observable, TooltipDTO oldValue, TooltipDTO newValue) {
                assertNull(newValue);
                assertNull(uut.getCurrHovered());
                uut.showTooltipProperty().removeListener(this);
            }
        }));
        uut.handleHover(xCoordinateWithNoZone, yCoordinateWithNoZone);
    }

    @Test
    public void testMapHoverWithNoHoverableZones() {
        initializeUUTWithDefaultScale();
        ZoneSquare toHover = uut.getZoneSquares().stream().filter(zone -> zone.getName().equals("Zone110")).findFirst().orElse(null);
        assertNotNull(toHover);
        int hoverXCoordinate = toHover.getCenter().getX();
        int hoverYCoordinate = toHover.getCenter().getY();
        uut.setHoverableZones(new ArrayList<>());
        uut.handleHover(hoverXCoordinate, hoverYCoordinate);
        assertNull(uut.getCurrHovered());
    }

    @Test
    public void testMapHoverWithCoordinatesOutOfMapBounds() {
        initializeUUTWithDefaultScale();
        int hoverXCoordinateOutOfMapBounds = -1;
        int hoverYCoordinateOutOfMapBounds = -1;
        uut.removeTooltipProperty().addListener((new ChangeListener<TooltipDTO>() {
            @Override
            public void changed(ObservableValue<? extends TooltipDTO> observable, TooltipDTO oldValue, TooltipDTO newValue) {
                assertNull(newValue);
                assertNull(uut.getCurrHovered());
                uut.removeTooltipProperty().removeListener(this);
            }
        }));
        uut.handleHover(hoverXCoordinateOutOfMapBounds, hoverYCoordinateOutOfMapBounds);
    }

    private void initializeSourceAndTargetZoneSquare() {
        ZoneSquare source = uut.getZoneSquares().stream().filter(zone -> zone.getName().equals("Zone110")).findFirst().orElse(null);
        ZoneSquare target = uut.getZoneSquares().stream().filter(zone -> zone.getName().equals("Zone154")).findFirst().orElse(null);
        source.setColor(Config.PlayerColor.RED);
        target.setColor(Config.PlayerColor.BLUE);
        assert source != null;
        assert target != null;
        uut.setSource(source);
        uut.setTarget(target);

    }

    public void testFightAttackerWins() {

    }

    public void testFightDefenderWins() {}

    @Test
    public void testFinishFightZoneOvertaken() {
        final int attackerTroops = 3;
        initializeUUTWithDefaultScale();
        initializeSourceAndTargetZoneSquare();

        RisikoController mockRisikoController = mock(RisikoController.class);
        when(mockRisikoController.getWinner()).thenReturn(null);
        uut.setRisikoController(mockRisikoController);

        FightDTO attackerOvertakesZone = new FightDTO();
        attackerOvertakesZone.setAttackerTroops(attackerTroops);
        attackerOvertakesZone.setOvertookZone(true);

        uut.moveTroopsProperty().addListener((new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                assertEquals(attackerTroops, newValue.intValue());
                uut.moveTroopsProperty().removeListener(this);
            }
        }));
        uut.finishFight(attackerOvertakesZone);
    }

    public void testFightRegionOvertaken() {

    }

    public void testFightAttackerWinsGame() {}

    public void testClickOnZone(){

    }

    public void testClickOnNoZone(){}

    public void testClickOnPositionOutOfMapBounds(){}

    public void testClickMapClickDisabled() {}

    public void testClickNoClickableZones() {}

    @Test
    public void testNextActionValuesSetCorrectly() {
        initializeUUTWithDefaultScale();
        initializeSourceAndTargetZoneSquare();
        final Config.PlayerColor currPlayerColor = Config.PlayerColor.RED;
        final int amountOfHoverableZonesAfterNextActionCalled = 0;
        final String actionNameAfterNextActionCalled = Action.ATTACK.getActionName();
        RisikoController mockRisikoController = mock(RisikoController.class);
        uut.setRisikoController(mockRisikoController);
        when(mockRisikoController.getAction()).thenReturn(Action.ATTACK);
        when(mockRisikoController.getCurrentPlayer()).thenReturn(currPlayerColor);
        uut.nextAction();
        assertNull(uut.getSource());
        assertNull(uut.getTarget());
        assertTrue(uut.sourceOrTargetNullProperty().get());
        assertFalse(uut.isMapClickEnabled());
        assertEquals(amountOfHoverableZonesAfterNextActionCalled, uut.getHoverableZones().size());
        assertTrue(uut.actionButtonVisibleProperty().get());
        assertEquals(actionNameAfterNextActionCalled, uut.actionButtonTextProperty().get());
        assertEquals(actionNameAfterNextActionCalled, uut.showActionChangeProperty().get());
    }

    @Test
    public void testNextActionWithPlayerSwitch() {
        initializeUUTWithDefaultScale();
        initializeSourceAndTargetZoneSquare();
        final Config.PlayerColor currPlayerBeforeNextActionCalled = Config.PlayerColor.RED;
        final Config.PlayerColor currPlayerAfterNextActionCalled = Config.PlayerColor.BLUE;
        RisikoController mockRisikoController = mock(RisikoController.class);
        uut.setRisikoController(mockRisikoController);
        when(mockRisikoController.getCurrentPlayer()).thenReturn(currPlayerBeforeNextActionCalled, currPlayerAfterNextActionCalled);
        uut.currPlayerProperty().addListener((new ChangeListener<Config.PlayerColor>() {
            @Override
            public void changed(ObservableValue<? extends Config.PlayerColor> observable, Config.PlayerColor oldValue, Config.PlayerColor newValue) {
                assertEquals(currPlayerAfterNextActionCalled, newValue);
                uut.currPlayerProperty().removeListener(this);
            }
        }));
    }

    @Test
    public void testResetHoverableZones() {
        initializeUUTWithDefaultScale();
        uut.setHoverableZones(new ArrayList<>());
        final int amountOfHoverableZonesAfterReset = uut.getZoneSquares().size();
        uut.resetHoverableZones();
        assertEquals(amountOfHoverableZonesAfterReset, uut.getHoverableZones().size());
    }

    public void testUpdateClickableZones() {}

    public void testReinforcement() {}

    public void testReinforcementClickOnZone() {}

    public void testReinforcementClickOnInvalidZone() {}

    public void testReinforcementClickWithNoClickableZones() {}

    public void testPlaceTroops() {
    }

    @Test
    public void testNotifyDefender() {
        initializeUUTWithDefaultScale();
        final Config.PlayerColor defenderPlayerColor = Config.PlayerColor.RED;
        ZoneSquare target = uut.getZoneSquares().stream().filter(zone -> zone.getName().equals("Zone110")).findFirst().orElse(null);
        target.setColor(Config.PlayerColor.RED);
        uut.setTarget(target);
        RisikoController mockRisikoController = mock(RisikoController.class);
        uut.setRisikoController(mockRisikoController);
        when(mockRisikoController.getZoneOwner(target.getName())).thenReturn(defenderPlayerColor);
        uut.highlightPlayerProperty().addListener((new ChangeListener<Config.PlayerColor>() {
            @Override
            public void changed(ObservableValue<? extends Config.PlayerColor> observable, Config.PlayerColor oldValue, Config.PlayerColor newValue) {
                assertEquals(target.getColor(), newValue);
                uut.highlightPlayerProperty().removeListener(this);
            }
        }));
        uut.notifyDefender();
    }
}
