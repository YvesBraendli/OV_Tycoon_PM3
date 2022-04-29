package ch.zhaw.ovtycoon.model;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class TroopHandlerTest {

    private TroopHandler troopHandlerTestOnePlayer;
    private TroopHandler getTroopHandlerTestMultiplePlayers;
    private Game gameInstance;
    private Player playerTest;

    @Before
    public void init(){
        troopHandlerTestOnePlayer = new TroopHandler(1);
        gameInstance = new Game();
        playerTest = new Player("Versuchskaninchen");
    }

    @Test
    public void moveUnitsNoDirectConnection(){

    }

    @Test
    public void moveUnitsWithToLessTroopsInOrigin(){

    }

    @Test
    public void moveUnitsSuccessfully(){

    }

    @Test
    public void playerIsNotZoneOwner(){

    }

    @Test
    public void unitDistributionWithOddBigPlayerNumberRight(){

    }

    @Test
    public void unitDistributionWithEvenBigPlayerNumberRight(){

    }
}
