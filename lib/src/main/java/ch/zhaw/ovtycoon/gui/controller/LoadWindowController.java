package ch.zhaw.ovtycoon.gui.controller;


import ch.zhaw.ovtycoon.gui.model.MapModel;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
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

    private boolean loadGame = false;



    public LoadWindowController(boolean isLoadingGame){
        this.loadGame = isLoadingGame;
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

            double scale = Screen.getPrimary().getBounds().getHeight() < 1000.0d ? 0.7d : 1.0d;
            Image mapImage = new Image(getClass().getClassLoader().getResource("zvv_zones_v11.png").toExternalForm());
            MapModel mapModel = loadGame ? new MapModel(mapImage, scale, true) : new MapModel(mapImage, scale);
            MainWindowController mainWindowController = new MainWindowController(mapModel);

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/MainWindow.fxml"));
            fxmlLoader.setController(mainWindowController);
            Pane rootPane = fxmlLoader.load();
            mainWindowController.setParentSceneGraph(rootPane);
            mainWindowController.setParentSceneGraph(parentSceneGraph);

            Scene scene = new Scene(rootPane);
            scene.getStylesheets().add(getClass().getClassLoader().getResource("map-styles.css").toExternalForm());

            Stage mainWindow = new Stage();

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

