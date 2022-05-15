package ch.zhaw.ovtycoon;

import ch.zhaw.ovtycoon.gui.MapController;
import ch.zhaw.ovtycoon.gui.model.MapModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main {
    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
    
    public static class App extends Application {
        @Override
        public void start(Stage stage) throws Exception {
            FXMLLoader fxmlLoader = new FXMLLoader(MapController.class.getClassLoader().getResource("zones-map-view.fxml"));
            double scale = Screen.getPrimary().getBounds().getHeight() < 1000 ? 0.7d : 1.0d;
            final Image zoneImage = new Image(MapController.class.getClassLoader().getResource("zvv_zones_v7.png").toExternalForm());
            final MapModel mapModel = new MapModel(zoneImage, scale);
            final MapController controller = new MapController(mapModel);
            fxmlLoader.setController(controller);
            Scene scene = new Scene(fxmlLoader.load(), scale * 1000, scale * 900);
            scene.getStylesheets().add(getClass().getClassLoader().getResource("map-styles.css").toExternalForm());
            stage.setTitle("OV-Tycoon");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        }
    }
}
