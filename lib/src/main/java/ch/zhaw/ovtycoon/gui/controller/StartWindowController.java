package ch.zhaw.ovtycoon.gui.controller;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

/**
 * Controller for the start window.
 */
public class StartWindowController {


    /**
     * To set the parent.
     */
    private Parent parentSceneGraph;

    @FXML
    private Button newGameButton;
    @FXML
    private Button loadGameButton;
    @FXML
    private Button closeButton;
    @FXML
    private ImageView plane;

    /**
     * Initializes the start window controller.
     */
    @FXML
    private void initialize(){
        movePlane();
        newGameButton.getStyleClass().add("buttonClass");
    }

    /**
     * Sets the parent scene graph.
     */
    public void setParentSceneGraph(Parent parentSceneGraph) {
        this.parentSceneGraph = parentSceneGraph;
    }

    /**
     * Opens a new game.
     */
    public void doNewGame(){
        openSelectPlayerWindow();
        Stage stage = (Stage) newGameButton.getScene().getWindow();
        stage.close();
    }
    /**
     * Loads a saved game.
     */
    public void loadGame(){
        openLoadWindow();
        Stage stage = (Stage) newGameButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Close the start window and terminate the programm.
     */
    public void closeGame(){
        Platform.exit();
    }


    /**
     * Opens the select player window and set up the controller for this window.
     */
    void openSelectPlayerWindow(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/SelectPlayerWindow.fxml"));
            Pane rootPane = fxmlLoader.load();

            Scene scene = new Scene(rootPane);
            Stage selectPlayerWindow = new Stage();

            SelectPlayerWindowController selectPlayerWindowController = fxmlLoader.getController();
            selectPlayerWindowController.setParentSceneGraph(rootPane);
            selectPlayerWindowController.setParentSceneGraph(parentSceneGraph);

            selectPlayerWindow.setScene(scene);
            selectPlayerWindow.setTitle("Select Player Window");
            selectPlayerWindow.setResizable(false);
            selectPlayerWindow.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the load window and set up the controller for this window.
     */
    void openLoadWindow(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/LoadWindow.fxml"));
            LoadWindowController loadWindowController = new LoadWindowController(true);
            fxmlLoader.setController(loadWindowController);
            Pane rootPane = fxmlLoader.load();

            Scene scene = new Scene(rootPane);
            Stage selectPlayerWindow = new Stage();

            loadWindowController.setParentSceneGraph(rootPane);
            loadWindowController.setParentSceneGraph(parentSceneGraph);

            selectPlayerWindow.setScene(scene);
            selectPlayerWindow.setTitle("Ladet...");
            selectPlayerWindow.setResizable(false);
            selectPlayerWindow.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Opens the load window and set up the controller for this window.
     */
    public void movePlane(){
        TranslateTransition flugzeugAnimation = new TranslateTransition();
        flugzeugAnimation.setNode(plane);
        flugzeugAnimation.setDuration(Duration.seconds(40));
        flugzeugAnimation.setByX(-200);
        flugzeugAnimation.setByY(-80);
        flugzeugAnimation.play();
    }
}
