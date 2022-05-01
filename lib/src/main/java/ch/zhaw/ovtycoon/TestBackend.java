package ch.zhaw.ovtycoon;

import ch.zhaw.ovtycoon.gui.model.ZoneColor;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.Random;

public class TestBackend {
    private int troopsToPlace;
    private int diceThrowResult;
    private SimpleBooleanProperty troopPlacingFinished;


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
}
