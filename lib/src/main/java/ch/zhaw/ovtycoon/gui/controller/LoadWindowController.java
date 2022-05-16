package ch.zhaw.ovtycoon.gui.controller;


import javafx.animation.TranslateTransition;
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
import java.util.ArrayList;


public class LoadWindowController {

    private Parent parentSceneGraph;

    @FXML
    private ImageView tramImage;
    @FXML
    private Button startButton;


    public LoadWindowController(){
    }

    public LoadWindowController(boolean isLoadingGame){
        if(isLoadingGame){
            System.out.println("LOADING");
            //initRisikoControler //TODO
            //loading
        }
    }
    public LoadWindowController( ArrayList listOfPlayers){
            System.out.println(listOfPlayers.size());
           //initRisikoControler //TODO
    }

    @FXML
    public void initialize() {
        moveTram();
    }

    public void setParentSceneGraph(Parent parentSceneGraph) {
        this.parentSceneGraph = parentSceneGraph;
    }

    public void moveTram() {
        TranslateTransition tramAnimation = new TranslateTransition();
        tramAnimation.setNode(tramImage);
        tramAnimation.setDuration(Duration.seconds(5));
        tramAnimation.setByX(600);
        tramAnimation.setOnFinished(event -> openMainWindow());
        tramAnimation.play();
    }

    private void openMainWindow(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/MainWindow.fxml"));
            Pane rootPane = fxmlLoader.load();

            Scene scene = new Scene(rootPane);
            scene.getStylesheets().add(getClass().getClassLoader().getResource("map-styles.css").toExternalForm());

            Stage mainWindow = new Stage();

            MainWindowController mainWindowController = fxmlLoader.getController();
            mainWindowController.setParentSceneGraph(rootPane);
            mainWindowController.setParentSceneGraph(parentSceneGraph);

            mainWindow.setScene(scene);
            mainWindow.setTitle("OV-Tycoon");
            mainWindow.setResizable(false);
            mainWindow.show();

            Stage stage = (Stage) tramImage.getScene().getWindow();
            stage.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

 }

