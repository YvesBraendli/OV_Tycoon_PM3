package ch.zhaw.ovtycoon;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class MainWindowController {

    private Parent parentSceneGraph;

    @FXML
    private VBox BoxPlayerView;


    public void setParentSceneGraph(Parent parentSceneGraph) {
        this.parentSceneGraph = parentSceneGraph;
    }


    public void closeGame(){
        Platform.exit();
    }

    public void saveGame(){

    }

    public void doAction(){

    }
}
