package ch.zhaw.ovtycoon.gui.Service;

import ch.zhaw.ovtycoon.Config;
import ch.zhaw.ovtycoon.Config.ZoneName;
import ch.zhaw.ovtycoon.gui.service.GameStateService;
import ch.zhaw.ovtycoon.model.Game;
import ch.zhaw.ovtycoon.model.Player;
import ch.zhaw.ovtycoon.model.Zone;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GameStateServiceTests {
    private GameStateService testee = new GameStateService();

    @Test
    public void saveGameState_successfullySaved_returnsTrue() {
        // Arrange
        Game game = new Game();

        // Act
        boolean result = testee.saveGameState(game);

        // Assert
        assertTrue(result);
    }

    @Test
    public void saveAndReloadGameObject_returnsSameZone() {
        // Arrange
        ZoneName zoneName = Config.ZoneName.Zone140;
        Game game = new Game();
        Zone zone = new Zone(zoneName.toString());
        Player player = new Player("tester");
        player.setColor(Config.PlayerColor.YELLOW);
        game.setZoneOwner(player, zone);

        // Act
        testee.saveGameState(game);
        Game loadedGame = testee.loadGameState();
        Zone loadedZone = loadedGame.getZone(zoneName.toString());

        // Assert
        assertTrue(zone.equals(loadedZone));
    }

    @Test
    public void saveAndReloadGameObject_returnsSamePlayer() {
        // Arrange
        ZoneName zoneName = Config.ZoneName.Zone140;
        String playerName = "tester";
        Config.PlayerColor playerColor = Config.PlayerColor.YELLOW;
        Game game = new Game();
        Zone zone = new Zone(zoneName.toString());
        Player player = new Player(playerName);
        player.setColor(playerColor);
        game.setZoneOwner(player, zone);

        // Act
        testee.saveGameState(game);
        Game loadedGame = testee.loadGameState();
        Zone loadedZone = loadedGame.getZone(zoneName.toString());
        Player loadedPlayer = loadedGame.getZoneOwner(loadedZone);

        // Assert
        assertEquals(playerName ,loadedPlayer.getName());
        assertEquals(playerColor ,loadedPlayer.getColor());
    }
}
