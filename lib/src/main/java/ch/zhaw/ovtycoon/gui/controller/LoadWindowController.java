package ch.zhaw.ovtycoon.gui.controller;


import ch.zhaw.ovtycoon.Config;
import ch.zhaw.ovtycoon.RisikoController;
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
    private ArrayList<Config.PlayerColor> listOfColours = new ArrayList<>();



    public LoadWindowController(boolean isLoadingGame){
        this.loadGame = isLoadingGame;
    }

    public LoadWindowController( ArrayList<String> listOfPlayers){
        for (String colour : listOfPlayers) {
            listOfColours.add(Config.PlayerColor.valueOf(colour.toUpperCase()));
        }

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

    private void openMainWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/MainWindow.fxml"));
            final Image mapImage = new Image(getClass().getResource("/zvv_zones_v11.png").toExternalForm());
            final double  scale = Screen.getPrimary().getBounds().getHeight() < 1000.0d ? 0.7d : 1.0d;
            final RisikoController risikoController = loadGame ? new RisikoController() : new RisikoController(listOfColours);
            final MapModel mapModel = new MapModel(mapImage, scale, risikoController);
            MainWindowController mainWindowController = new MainWindowController(mapModel);
            fxmlLoader.setController(mainWindowController);
            Pane rootPane = fxmlLoader.load();

            Scene scene = new Scene(rootPane);
            scene.getStylesheets().add(getClass().getClassLoader().getResource("map-styles.css").toExternalForm());

            double stageWidth = scale * 1027.0d;
            double stageHeight = scale * 969.0d;

            Stage mainWindow = new Stage();
            mainWindow.setWidth(stageWidth);
            mainWindow.setHeight(stageHeight);

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

