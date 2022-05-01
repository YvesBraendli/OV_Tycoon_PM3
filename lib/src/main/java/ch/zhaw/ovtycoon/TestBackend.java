package ch.zhaw.ovtycoon;

import ch.zhaw.ovtycoon.gui.model.Action;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.Random;

public class TestBackend {
    private int troopsToPlace;
    private int diceThrowResult;
    private SimpleBooleanProperty troopPlacingFinished;
    private Action[] actions = {Action.DEFEND, Action.ATTACK, Action.MOVE};
    private int currActionIndex = 0;


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
        currActionIndex = currActionIndex == actions.length - 1 ? 0 : ++currActionIndex;
    }

    public Action getAction() {
        return actions[currActionIndex];
    }
}
