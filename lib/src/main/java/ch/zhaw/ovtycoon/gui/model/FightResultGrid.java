package ch.zhaw.ovtycoon.gui.model;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import javax.swing.*;

public class FightResultGrid extends GridPane {
    private double width = 500.0d;
    private double height = 200.0d;
    private double scale = 1.0d;
    private final int[] attackerDiceRolls;
    private final int[] defenderDiceRolls;

    public FightResultGrid(int[] attackerDiceRolls, int[] defenderDiceRolls, double scale) {
        this.attackerDiceRolls = attackerDiceRolls;
        this.defenderDiceRolls = defenderDiceRolls;
        this.scale = scale;
        if (scale != 1.0d) {
            width = width * scale;
            height = height * scale;
        }
        setPrefWidth(width);
        setMinWidth(width);
        setMaxWidth(width);
        setPrefHeight(height);
        setMinHeight(height);
        setMaxHeight(height);
        setStyle("-fx-background-color: black; -fx-padding: 20px 0 20px 0;");
        setGridLinesVisible(true);
        Label attackerLabel = new Label("Angreifer");
        attackerLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px");
        Label defenderLabel = new Label("Verteidiger");
        defenderLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px;");

        HBox attackerHBox = getHBox();
        addLabelsToHBox(attackerDiceRolls, attackerHBox);

        HBox defenderHBox = getHBox();
        addLabelsToHBox(defenderDiceRolls, defenderHBox);

        addColumn(0, attackerLabel, attackerHBox);
        addColumn(1, defenderLabel, defenderHBox);
        ColumnConstraints col1 = new ColumnConstraints(width / 2);
        col1.setHalignment(HPos.CENTER);
        ColumnConstraints col2 = new ColumnConstraints(width / 2);
        col2.setHalignment(HPos.CENTER);
        getColumnConstraints().addAll(col1, col2);
    }

    private HBox getHBox() {
        HBox hBox = new HBox(20.0d * scale);
        hBox.setPrefHeight(100.0d * scale);
        hBox.setPrefWidth(250.0d * scale);
        hBox.setAlignment(Pos.CENTER);
        return hBox;
    }

    private void addLabelsToHBox(int[] numbers, HBox parent) {
        for (int diceNumber : numbers) {
            Label l = new Label(Integer.toString(diceNumber));
            l.setPrefWidth(50.0d * scale);
            l.setPrefHeight(50.0d * scale);
            l.setAlignment(Pos.CENTER);
            l.setStyle("-fx-text-fill: white; -fx-border-color: white; -fx-border-width: 1px; -fx-font-size: 20px;");
            parent.getChildren().add(l);
        }
    }
}
