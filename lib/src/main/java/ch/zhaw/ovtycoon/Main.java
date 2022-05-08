package ch.zhaw.ovtycoon;

import ch.zhaw.ovtycoon.gui.MapController;
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
            Scene scene = new Scene(fxmlLoader.load(), 1000, 900);
            scene.getStylesheets().add(getClass().getClassLoader().getResource("map-styles.css").toExternalForm());
            stage.setTitle("OV-Tycoon");
            stage.setScene(scene);
            stage.show();
        }
    }
}
