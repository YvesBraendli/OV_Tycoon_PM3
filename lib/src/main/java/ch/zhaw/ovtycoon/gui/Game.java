package ch.zhaw.ovtycoon.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Game extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(MapController.class.getClassLoader().getResource("zones-map-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 1000);
        scene.getStylesheets().add(getClass().getClassLoader().getResource("map-styles.css").toExternalForm());
        PerspectiveCamera cam = new PerspectiveCamera();
        scene.setCamera(cam);
        stage.setTitle("OV-Tycoon");
        stage.setScene(scene);
        stage.show();
    }
}
