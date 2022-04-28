package ch.zhaw.ovtycoon;

import ch.zhaw.ovtycoon.model.Game;
import javafx.application.Application;

public class Main {

    public static void main(String[] args) {
    	Game game = new Game();
    	game.initGame(0);
        Application.launch(RisikoController.class, args);
    }
}
