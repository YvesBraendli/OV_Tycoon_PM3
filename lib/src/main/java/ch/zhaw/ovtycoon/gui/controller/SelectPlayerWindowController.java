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


/**
 * Controller for the select player window.
 */
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

    /**
     * Initialize the controller and checks when the player are clicked.
     */
    @FXML
    public void initialize() {
        selectPlayerPane.getChildren().stream().filter((node -> node.getId() != null && node.getId().toLowerCase().contains("player"))).forEach((node) -> node.setOnMouseClicked(event -> {
            resize(node.getId());
            amountOfPlayers(node.getId());
        }));
    }

    /**
     * Sets the parent scene graph.
     */
    public void setParentSceneGraph(Parent parentSceneGraph) {
        this.parentSceneGraph = parentSceneGraph;
    }


    /**
     * Resize the player image, every time it get clicked.
     *
     * @param id is the color of the player in a String
     */
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

    /**
     * Put the player color string in a list and gives feedback to the user if the player are added or not.
     *
     * @param id is the color of the player in a String
     */
    public void amountOfPlayers(String id) {
        String colour = id.substring(6);

        if(listOfPlayer.indexOf(colour) == -1){
            listOfPlayer.add(colour);
            infoText.setText("Spieler " + colour + " ist bereit!");
        }else{
            listOfPlayer.remove(listOfPlayer.indexOf(colour));
            infoText.setText("Spieler " + colour + " ist nicht bereit!");
        }
    }

    /**
     * Opens the load window and set up the controller for this window.
     */
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

    /**
     * When the next button is clicked, it checks, if there are more the one player in the list.
     */
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
