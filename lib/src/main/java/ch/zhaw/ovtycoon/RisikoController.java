package ch.zhaw.ovtycoon;

import ch.zhaw.ovtycoon.gui.MapController;
import ch.zhaw.ovtycoon.model.Dice;
import ch.zhaw.ovtycoon.model.Game;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RisikoController extends Application {

	private Game game;
	
    @Override
    public void start(Stage stage) throws Exception {
    	game = new Game();
    	game.initGame(2);
    	
    	game.dicePropertyProperty().addListener(new ChangeListener<Dice>() {
    			@Override
    	    	public void changed(ObservableValue<? extends Dice> observable, Dice oldDice, Dice newDice) {
    				int[] roll = newDice.getRolledDice();
    				for(int num : roll) {
    					System.out.println(num);
    				}
    	    	}
    	});
    	
    	
    	
        FXMLLoader fxmlLoader = new FXMLLoader(MapController.class.getClassLoader().getResource("zones-map-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 1000);
        stage.setTitle("OV-Tycoon");
        stage.setScene(scene);
        stage.show();
    }
    
    
}
