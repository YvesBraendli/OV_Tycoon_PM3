package ch.zhaw.ovtycoon;

import ch.zhaw.ovtycoon.gui.model.Action;
import ch.zhaw.ovtycoon.gui.model.ZoneColor;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Random;

public class TestBackend {
    private int troopsToPlace;
    private int diceThrowResult;
    private SimpleBooleanProperty troopPlacingFinished;
    private SimpleObjectProperty<ZoneColor> currPlayer;
    private Action[] actions = {Action.DEFEND, Action.ATTACK, Action.MOVE};
    private int currActionIndex = 0;
    private ZoneColor[] players = {ZoneColor.PLAYER_RED, ZoneColor.PLAYER_BLUE, ZoneColor.PLAYER_GREEN};
    private int currPlayerIndex = 0;

    public TestBackend() {
        currPlayer = new SimpleObjectProperty<>(players[currPlayerIndex]);
    }

    public ZoneColor getCurrPlayer() {
        return players[currPlayerIndex];
    }

    public void diceThrow() {
        diceThrowResult = new Random().nextInt(6) + 1;
        troopsToPlace = diceThrowResult;
        troopPlacingFinished = new SimpleBooleanProperty(false);
    }

    public void placeTroops(int amount) {
        troopsToPlace -= amount;
        troopPlacingFinished.set(troopsToPlace == 0);
    }

    public SimpleBooleanProperty finishedPlacingTroops() {
        return troopPlacingFinished;
    }

    public int getTroopsToPlace() {
        return troopsToPlace;
    }

    public int getDiceThrowResult() {
        return diceThrowResult;
    }

    public void nextAction() {
        if (currActionIndex == actions.length - 1) {
            currActionIndex = 0;
            nextPlayer();
        } else {
            currActionIndex = currActionIndex + 1;
        }
    }

    private void nextPlayer() {
        currPlayerIndex = currPlayerIndex == players.length - 1 ? 0 : ++currPlayerIndex;
        currPlayer.set(players[currPlayerIndex]);
    }

    public SimpleObjectProperty<ZoneColor> getNextPlayer() {
        return currPlayer;
    }

    public Action getAction() {
        return actions[currActionIndex];
    }

    public ZoneColor[] getPlayers() {
        return players;
    }
}
