package ch.zhaw.ovtycoon;

import ch.zhaw.ovtycoon.gui.controller.StartWindowController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main class that initializes the start window of
 * the GUI and sets up the startWindowController.
 */
public class Main extends Application {
    /**
     * Initializes the start window.
     *
     * @param primaryStage
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/StartWindow.fxml"));
            Pane rootPane = fxmlLoader.load();
            StartWindowController startWindowController = fxmlLoader.getController();
            startWindowController.setParentSceneGraph(rootPane);

            Scene scene = new Scene(rootPane);
            scene.getStylesheets().add(getClass().getClassLoader().getResource("button-design.css").toExternalForm());

            primaryStage.setScene(scene);

            primaryStage.setTitle("OV-Tycoon");
            primaryStage.setMinWidth(600);
            primaryStage.setMinHeight(400);
            primaryStage.setResizable(false);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

