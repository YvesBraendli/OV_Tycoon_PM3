package ch.zhaw.ovtycoon.gui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class SelectPlayerWindowController {

    /**
     * To set the parent.
     */
    private Parent parentSceneGraph;

    private ArrayList<String> listOfPlayer = new ArrayList<>();


    @FXML
    private Button nextButton;

    @FXML
    private TextField infoText;

    @FXML
    private AnchorPane selectPlayerPane;

    public void setParentSceneGraph(Parent parentSceneGraph) {
        this.parentSceneGraph = parentSceneGraph;
    }

    @FXML
    public void initialize() {
        selectPlayerPane.getChildren().stream().filter((node -> node.getId() != null && node.getId().toLowerCase().contains("player"))).forEach((node) -> node.setOnMouseClicked(event -> {
            resize(node.getId());
            amountOfPlayers(node.getId());
        }));
    }

    public void resize(String id) {
        ImageView toResize = (ImageView) selectPlayerPane.getChildren().stream().filter((node -> node.getId() != null && node.getId().equals(id))).findFirst().orElse(null);
        if (toResize == null) return;
        boolean enlarge = toResize.getFitWidth() == 100.0d;
        double resizedWidth = enlarge ? 130.0d : 100.0d;
        double resizedHeight = enlarge ? 130.0d : 150.0d;

        toResize.setFitHeight(resizedHeight);
        toResize.setFitWidth(resizedWidth);
        double layoutXYSummand = enlarge ? -10 : 10;
        toResize.setX(toResize.getX() + layoutXYSummand);
        toResize.setY(toResize.getY() + layoutXYSummand);
    }

    public void amountOfPlayers(String id) {
        String colour = id.substring(6);

        if(listOfPlayer.indexOf(colour) == -1){
            listOfPlayer.add(colour);
            infoText.setText("Player " + colour + " is ready");
        }else{
            listOfPlayer.remove(listOfPlayer.indexOf(colour));
            infoText.setText("Player " + colour + " is not ready");
        }
    }




    private void openLoadWindow(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/LoadWindow.fxml"));
            LoadWindowController loadWindowController = new LoadWindowController(listOfPlayer);
            fxmlLoader.setController(loadWindowController);
            Pane rootPane = fxmlLoader.load();

            Scene scene = new Scene(rootPane);
            Stage loadWindow = new Stage();

            LoadWindowController loadWindowsController = fxmlLoader.getController();
            loadWindowsController.setParentSceneGraph(rootPane);
            loadWindowsController.setParentSceneGraph(parentSceneGraph);

            loadWindow.setScene(scene);
            loadWindow.setTitle("Loading...");
            loadWindow.setResizable(false);
            loadWindow.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void doNext(){
        if(listOfPlayer.size()<2){
            infoText.setText("Too few player");
        }else{
            openLoadWindow();
            Stage stage = (Stage) nextButton.getScene().getWindow();
            stage.close();
        }
    }
}
