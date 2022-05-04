package ch.zhaw.ovtycoon;

import ch.zhaw.ovtycoon.gui.MapController;
import ch.zhaw.ovtycoon.model.Game;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main {

    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
    
    public static class App extends Application{
        @Override
        public void start(Stage stage) throws Exception {      	
            FXMLLoader fxmlLoader = new FXMLLoader(MapController.class.getClassLoader().getResource("zones-map-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1000, 1000);
            stage.setTitle("OV-Tycoon");
            stage.setScene(scene);
            stage.show();
        }
    }
}
