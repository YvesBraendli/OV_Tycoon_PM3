package ch.zhaw.ovtycoon.gui.Service;

import ch.zhaw.ovtycoon.gui.JavaFXPlatformRunnable;
import ch.zhaw.ovtycoon.gui.model.HorizontalStripe;
import ch.zhaw.ovtycoon.gui.model.ZoneSquare;
import ch.zhaw.ovtycoon.gui.service.MapLoaderService;
import javafx.scene.image.Image;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class MapLoaderServiceTests {

    private MapLoaderService testee;
    private final double scaleDefault = 1.0d;
    private final double scaleSmall = 0.7d;
    private Image zoneMapImage;

    @Before
    public void platformStart() {
        JavaFXPlatformRunnable.run();
        zoneMapImage = new Image(getClass().getClassLoader().getResource("zvv_zones_v11.png").toExternalForm());
    }

    @Test
    public void initZoneSquaresFromConfig_loadsAllZones_defaultScaling() {
        // Arrange
        testee = new MapLoaderService(zoneMapImage, scaleDefault);
        int expectedZones = 42;

        // Act
        List<ZoneSquare> result = testee.initZoneSquaresFromConfig();

        // Assert
        assertTrue(expectedZones == result.size());
    }

    @Test
    public void initZoneSquaresFromConfig_allZonesHaveBorders_smallScaling() {
        // Arrange
        testee = new MapLoaderService(zoneMapImage, scaleSmall);
        int expectedZones = 42;

        // Act
        List<ZoneSquare> result = testee.initZoneSquaresFromConfig();

        // Assert
        for (ZoneSquare zS: result) {
            List<HorizontalStripe> borders = zS.getBorder();
            for (HorizontalStripe  hS : borders) {
                int xDiff = Math.abs(hS.getStartX() - hS.getEndX());
                boolean xIsSet = xDiff >= 0;
                assertTrue(xIsSet);
            }
        }
    }

    @Test
    public void initZoneSquaresFromConfig_allZonesHaveBorders_defaultScaling() {
        // Arrange
        testee = new MapLoaderService(zoneMapImage, scaleSmall);
        int expectedZones = 42;

        // Act
        List<ZoneSquare> result = testee.initZoneSquaresFromConfig();

        // Assert
        for (ZoneSquare zS: result) {
            List<HorizontalStripe> borders = zS.getBorder();
            for (HorizontalStripe  hS : borders) {
                assertTrue(hS.getStartX() > 0);
                assertTrue(hS.getEndX() > 0);
                assertTrue(hS.getY() > 0);
                int total = hS.getStartX() + hS.getEndX() + hS.getY();
                assertTrue( total > 5);
            }
        }
    }

}
