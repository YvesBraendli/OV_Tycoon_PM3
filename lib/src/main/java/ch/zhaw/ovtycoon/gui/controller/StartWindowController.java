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
    private ImageView flugzeug;

    @FXML
    private void initialize(){
        moveFlugzeug();
        newGameButton.getStyleClass().add("buttonClass");
    }

    public void setParentSceneGraph(Parent parentSceneGraph) {
        this.parentSceneGraph = parentSceneGraph;
    }

    public void doNewGame(){
        openSelectPlayerWindow();
        Stage stage = (Stage) newGameButton.getScene().getWindow();
        stage.close();

    }

    public void loadGame(){
        openLoadWindow();
        Stage stage = (Stage) newGameButton.getScene().getWindow();
        stage.close();
    }

    public void closeGame(){
        Platform.exit();
    }

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
            selectPlayerWindow.setTitle("Loading...");
            selectPlayerWindow.setResizable(false);
            selectPlayerWindow.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void moveFlugzeug(){
        TranslateTransition flugzeugAnimation = new TranslateTransition();
        flugzeugAnimation.setNode(flugzeug);
        flugzeugAnimation.setDuration(Duration.seconds(40));
        flugzeugAnimation.setByX(-200);
        flugzeugAnimation.setByY(-80);
        flugzeugAnimation.play();
    }
}
