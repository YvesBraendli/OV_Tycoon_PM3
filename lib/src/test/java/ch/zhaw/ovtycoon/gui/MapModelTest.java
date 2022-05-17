package ch.zhaw.ovtycoon.gui;

import ch.zhaw.ovtycoon.Config;
import ch.zhaw.ovtycoon.RisikoController;
import ch.zhaw.ovtycoon.gui.model.Action;
import ch.zhaw.ovtycoon.gui.model.MapModel;
import ch.zhaw.ovtycoon.gui.model.ZoneSquare;
import ch.zhaw.ovtycoon.gui.model.dto.ActivateZoneDTO;
import ch.zhaw.ovtycoon.gui.model.dto.FightDTO;
import ch.zhaw.ovtycoon.gui.model.dto.ReinforcementAmountDTO;
import ch.zhaw.ovtycoon.gui.model.dto.ReinforcementDTO;
import ch.zhaw.ovtycoon.gui.model.dto.TooltipDTO;
import ch.zhaw.ovtycoon.gui.model.dto.ZoneTroopAmountDTO;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
    private final int zonesAmount = 43;
    private final double scaleDefault = 1.0d;
    private final double scaleSmall = 0.7d;
    private MapModel uut;
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

    public void testFightDefenderWins() {
    }

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

    public void testFightAttackerWinsGame() {
    }

    @Test
    public void testClickOnZoneSetSource() {
        initializeUUTWithDefaultScale();
        assertNull(uut.getSource());
        ZoneSquare toClick = uut.getZoneSquares().stream().filter(zone -> zone.getName().equals("Zone110")).findFirst().orElse(null);
        ZoneSquare validTarget = uut.getZoneSquares().stream().filter(zone -> zone.getName().equals("Zone154")).findFirst().orElse(null);
        AtomicInteger sourceOrTargetPropertyUpdateCount = new AtomicInteger(0);
        AtomicInteger darkenBackgroundPropertyUpdateCount = new AtomicInteger(0);
        assertNotNull(toClick);
        assertNotNull(validTarget);

        int toClickX = toClick.getCenter().getX();
        int toClickY = toClick.getCenter().getY();
        final List<String> clickableZonesNames = new ArrayList<>();
        clickableZonesNames.add(toClick.getName());
        RisikoController mockRisikoController = mock(RisikoController.class);
        uut.setRisikoController(mockRisikoController);
        uut.darkenBackgroundProperty().set(true);
        uut.sourceOrTargetNullProperty().set(false);
        when(mockRisikoController.getValidSourceZoneNames()).thenReturn(clickableZonesNames);
        when(mockRisikoController.getValidTargetZoneNames(toClick.getName())).thenReturn(List.of(validTarget.getName()));

        uut.darkenBackgroundProperty().addListener((new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (darkenBackgroundPropertyUpdateCount.get() == 0) {
                    assertFalse(newValue);
                    darkenBackgroundPropertyUpdateCount.set(1);
                } else {
                    assertTrue(newValue);
                    uut.darkenBackgroundProperty().removeListener(this);
                }
            }
        }));
        uut.removeOverlaidPixelsProperty().addListener((new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                assertTrue(newValue);
                uut.removeOverlaidPixelsProperty().removeListener(this);
            }
        }));
        ChangeListener<Boolean> sourceOrTargetNullActionListener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (sourceOrTargetPropertyUpdateCount.get() == 0) {
                    assertTrue(newValue);
                }
            }
        };
        uut.sourceOrTargetNullProperty().addListener(sourceOrTargetNullActionListener);
        uut.highlightNeighboursProperty().addListener((new ChangeListener<List<ZoneSquare>>() {
            @Override
            public void changed(ObservableValue<? extends List<ZoneSquare>> observable, List<ZoneSquare> oldValue, List<ZoneSquare> newValue) {
                assertTrue(newValue.size() > 0);
                assertEquals(validTarget, newValue.get(0));
                uut.highlightNeighboursProperty().removeListener(this);
            }
        }));
        uut.setZoneActiveProperty().addListener((new ChangeListener<ActivateZoneDTO>() {
            @Override
            public void changed(ObservableValue<? extends ActivateZoneDTO> observable, ActivateZoneDTO oldValue, ActivateZoneDTO newValue) {
                assertEquals(toClick, newValue.getZoneSquare());
                assertTrue(newValue.isShift());
                uut.setZoneActiveProperty().removeListener(this);
            }
        }));
        uut.stopAnimationProperty().addListener((new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                assertTrue(newValue);
                uut.stopAnimationProperty().removeListener(this);
            }
        }));
        uut.handleMapClick(toClickX, toClickY);
        assertEquals(toClick, uut.getSource());
        assertTrue(uut.getHoverableZones().contains(toClick));
        assertTrue(uut.getHoverableZones().contains(validTarget));
        assertTrue(uut.getClickableZones().contains(toClick));
        assertTrue(uut.getClickableZones().contains(validTarget));
        uut.sourceOrTargetNullProperty().removeListener(sourceOrTargetNullActionListener);
    }

    @Test
    public void testClickShouldUpdateTarget() {
        initializeUUTWithDefaultScale();
        assertNull(uut.getTarget());
        ZoneSquare source = uut.getZoneSquares().stream().filter(zone -> zone.getName().equals("Zone110")).findFirst().orElse(null);
        ZoneSquare target = uut.getZoneSquares().stream().filter(zone -> zone.getName().equals("Zone154")).findFirst().orElse(null);
        uut.setSource(source);
        AtomicInteger sourceOrTargetPropertyUpdateCount = new AtomicInteger(0);
        AtomicInteger darkenBackgroundPropertyUpdateCount = new AtomicInteger(0);
        assertNotNull(source);
        assertNotNull(target);

        int toClickX = target.getCenter().getX();
        int toClickY = target.getCenter().getY();
        final List<String> clickableZonesNames = new ArrayList<>();
        clickableZonesNames.add(target.getName());
        RisikoController mockRisikoController = mock(RisikoController.class);
        uut.setRisikoController(mockRisikoController);
        when(mockRisikoController.getValidTargetZoneNames(target.getName())).thenReturn(List.of(target.getName()));

        uut.handleMapClick(toClickX, toClickY);

        uut.darkenBackgroundProperty().addListener((new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (darkenBackgroundPropertyUpdateCount.get() == 0) {
                    assertFalse(newValue);
                    darkenBackgroundPropertyUpdateCount.set(1);
                } else {
                    assertTrue(newValue);
                    uut.darkenBackgroundProperty().removeListener(this);
                }
            }
        }));
        uut.removeOverlaidPixelsProperty().addListener((new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                assertTrue(newValue);
                uut.removeOverlaidPixelsProperty().removeListener(this);
            }
        }));
        uut.sourceOrTargetNullProperty().addListener((new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (sourceOrTargetPropertyUpdateCount.get() == 0) {
                    assertTrue(newValue);
                    sourceOrTargetPropertyUpdateCount.set(1);
                } else {
                    assertFalse(newValue);
                    uut.sourceOrTargetNullProperty().removeListener(this);
                }
            }
        }));
        uut.removeUnnecessaryTooltipsProperty().addListener((new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                assertTrue(newValue);
                uut.removeUnnecessaryTooltipsProperty().removeListener(this);
            }
        }));

    }

    public void testClickShouldUnsetTarget() {

    }

    public void testClickShouldUnsetSourceAndTarget() {

    }

    public void testClickOnNoZone() {
    }

    public void testClickOnPositionOutOfMapBounds() {
    }

    public void testClickMapClickDisabled() {
    }

    public void testClickNoClickableZones() {
    }

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

    @Test
    public void testReinforcementClickOnZone() {
        initializeUUTWithDefaultScale();
        ZoneSquare toReinforce = uut.getZoneSquares().stream().filter(zone -> zone.getName().equals("Zone110")).findFirst().orElse(null);
        assertNotNull(toReinforce);
        final int clickableZonesAmountAfterReinforcementClick = 0;
        final int hoverableZonesAmountAfterReinforcementClick = 0;
        final List<ZoneSquare> clickableZones = new ArrayList<>();
        clickableZones.add(toReinforce);
        uut.setClickableZones(clickableZones);
        int toReinforceX = toReinforce.getCenter().getX();
        int toReinforceY = toReinforce.getCenter().getY();
        uut.stopAnimationProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                assertTrue(newValue);
                uut.stopAnimationProperty().removeListener(this);
            }
        });
        uut.removeOverlaidPixelsProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                assertTrue(newValue);
                uut.removeOverlaidPixelsProperty().removeListener(this);
            }
        });
        uut.darkenBackgroundProperty().addListener((new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                assertTrue(newValue);
                uut.darkenBackgroundProperty().removeListener(this);
            }
        }));
        uut.setZoneActiveProperty().addListener((new ChangeListener<ActivateZoneDTO>() {
            @Override
            public void changed(ObservableValue<? extends ActivateZoneDTO> observable, ActivateZoneDTO oldValue, ActivateZoneDTO newValue) {
                assertEquals(newValue.getZoneSquare(), toReinforce);
                assertFalse(newValue.isShift());
                uut.setZoneActiveProperty().removeListener(this);
            }
        }));
        uut.removeTooltipProperty().addListener((new ChangeListener<TooltipDTO>() {
            @Override
            public void changed(ObservableValue<? extends TooltipDTO> observable, TooltipDTO oldValue, TooltipDTO newValue) {
                assertEquals(uut.getCurrHovered(), newValue);
                uut.removeTooltipProperty().removeListener(this);
            }
        }));
        ReinforcementDTO dto = uut.reinforcementClick(toReinforceX, toReinforceY);
        assertEquals(toReinforce, dto.getZoneSquare());
        assertFalse(uut.stopAnimationProperty().get());
        assertFalse(uut.removeOverlaidPixelsProperty().get());
        assertFalse(uut.isMapClickEnabled());
        assertEquals(clickableZonesAmountAfterReinforcementClick, uut.getClickableZones().size());
        assertEquals(hoverableZonesAmountAfterReinforcementClick, uut.getHoverableZones().size());

    }

    @Test
    public void testReinforcementClickOnInvalidZone() {
        initializeUUTWithDefaultScale();
        ZoneSquare clickableZone = uut.getZoneSquares().stream().filter(zone -> zone.getName().equals("Zone154")).findFirst().orElse(null);
        ZoneSquare toReinforce = uut.getZoneSquares().stream().filter(zone -> zone.getName().equals("Zone110")).findFirst().orElse(null);
        assertNotNull(toReinforce);
        final List<ZoneSquare> clickableZones = new ArrayList<>();
        clickableZones.add(clickableZone);
        uut.setClickableZones(clickableZones);
        int toReinforceX = toReinforce.getCenter().getX();
        int toReinforceY = toReinforce.getCenter().getY();
        assertNull(uut.reinforcementClick(toReinforceX, toReinforceY));
    }

    @Test
    public void testReinforcementClickWithNoClickableZones() {
        initializeUUTWithDefaultScale();
        ZoneSquare toReinforce = uut.getZoneSquares().stream().filter(zone -> zone.getName().equals("Zone110")).findFirst().orElse(null);
        assertNotNull(toReinforce);
        final List<ZoneSquare> clickableZones = new ArrayList<>();
        uut.setClickableZones(clickableZones);
        int toReinforceX = toReinforce.getCenter().getX();
        int toReinforceY = toReinforce.getCenter().getY();
        assertNull(uut.reinforcementClick(toReinforceX, toReinforceY));
    }

    @Test
    public void testReinforcementPlaceTroops() {
        initializeUUTWithDefaultScale();
        final String zoneToReinforceName = "Zone110";
        final int troopAmountReceived = 5;
        final int troopAmountAlreadyPlaced = 0;
        final ReinforcementAmountDTO reinforcementAmountDTO = new ReinforcementAmountDTO(troopAmountReceived, troopAmountAlreadyPlaced);
        final int previousTroopAmount = 2;
        final int troopAmountAddedWithReinforcement = 3;
        final RisikoController mockRisikoController = mock(RisikoController.class);
        uut.setRisikoController(mockRisikoController);
        uut.setCurrentReinforcement(reinforcementAmountDTO);
        when(mockRisikoController.getZoneTroops(zoneToReinforceName)).thenReturn(previousTroopAmount + troopAmountAddedWithReinforcement);
        uut.updateZoneTroopsTextProperty().addListener((new ChangeListener<ZoneTroopAmountDTO>() {
            @Override
            public void changed(ObservableValue<? extends ZoneTroopAmountDTO> observable, ZoneTroopAmountDTO oldValue, ZoneTroopAmountDTO newValue) {
                assertEquals(zoneToReinforceName, newValue.getZoneName());
                assertEquals(previousTroopAmount + troopAmountAddedWithReinforcement, newValue.getTroopAmount());
                uut.updateZoneTroopsTextProperty().removeListener(this);
            }
        }));
        uut.removeOverlaidPixelsProperty().addListener((new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                assertTrue(newValue);
                uut.removeOverlaidPixelsProperty().removeListener(this);
            }
        }));
        uut.darkenBackgroundProperty().addListener((new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                assertFalse(newValue);
            }
        }));
        uut.placeReinforcementTroops(zoneToReinforceName, troopAmountAddedWithReinforcement);
        assertTrue(uut.isMapClickEnabled());
        assertFalse(uut.removeOverlaidPixelsProperty().get());
        assertEquals(zonesAmount, uut.getHoverableZones().size());
        assertEquals(troopAmountAddedWithReinforcement, uut.getCurrentReinforcement().getAmountAlreadyPlaced());

    }

    @Test
    public void testReinforcementPlaceTroopsReinforcementFinished() {
        initializeUUTWithDefaultScale();
        final String zoneToReinforceName = "Zone110";
        final int troopAmountReceived = 5;
        final int troopAmountAlreadyPlaced = 2;
        final int hoverableZonesAfterTroopPlacingFinished = 0;
        final int troopAmountAddedWithReinforcement = 3;
        final ReinforcementAmountDTO reinforcementAmountDTO = new ReinforcementAmountDTO(troopAmountReceived, troopAmountAlreadyPlaced);
        uut.setCurrentReinforcement(reinforcementAmountDTO);
        uut.placeReinforcementTroops(zoneToReinforceName, troopAmountAddedWithReinforcement);
        assertNull(uut.getCurrentReinforcement());
        assertFalse(uut.isMapClickEnabled());
        assertEquals(hoverableZonesAfterTroopPlacingFinished, uut.getHoverableZones().size());
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
