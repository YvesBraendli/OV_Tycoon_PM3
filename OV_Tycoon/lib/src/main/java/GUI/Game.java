package GUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Game extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(MapController.class.getClassLoader().getResource("zones-map-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 1100);
        stage.setTitle("OV-Tycoon");
        stage.setScene(scene);
        stage.show();
    }
}
