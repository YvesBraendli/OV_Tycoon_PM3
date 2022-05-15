package ch.zhaw.ovtycoon.gui;

import ch.zhaw.ovtycoon.gui.model.MapModel;
import ch.zhaw.ovtycoon.gui.model.ZoneSquare;
import ch.zhaw.ovtycoon.gui.model.dto.TooltipDTO;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;

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
        uut = new MapModel(zoneMapImage, scaleDefault);
    }

    public void initializeUUTWithSmallScale() {
        uut = new MapModel(zoneMapImage, scaleSmall);
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
        ZoneSquare target = uut.getZoneSquares().stream().filter(zone -> zone.getName().equals("Zone110")).findFirst().orElse(null);
        assert source != null;
        assert target != null;
        uut.setSource(source);
        uut.setTarget(target);

    }

    public void testFightAttackerWins() {
        initializeUUTWithDefaultScale();
        initializeSourceAndTargetZoneSquare();
    }

    public void testFightDefenderWins() {}

    public void testFightZoneOvertaken() {}

    public void testFightRegionOvertaken() {}

    public void testFightAttackerWinsGame() {}

    public void testClickOnZone(){}

    public void testClickOnNoZone(){}

    public void testClickOnPositionOutOfMapBounds(){}

    public void testClickMapClickDisabled() {}

    public void testClickNoClickableZones() {}

    public void testReinforcement() {}

    public void testNextAction() {}

    public void testNextActionWithPlayerSwitch() {}

    public void testResetHoverableZones() {}

    public void testUpdateClickableZones() {}

    public void testInitializeMovingTroops() {}

    public void testReinforcementClickOnZone() {}

    public void testReinforcementClickOnInvalidZone() {}

    public void testReinforcementClickWithNoClickableZones() {}

    public void testInitializeAttack() {}

    public void testPlaceTroops() {}

    public void testNotifyDefender() {}
}
