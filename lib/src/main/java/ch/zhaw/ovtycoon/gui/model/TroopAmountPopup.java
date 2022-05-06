package ch.zhaw.ovtycoon.gui.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Popup for moving troops with label, text field and confirm button
 */
public class TroopAmountPopup extends VBox {
    public static final double TROOP_AMOUNT_POPUP_PREF_WIDTH = 200.0d;
    public static final double TROOP_AMOUNT_POPUP_PREF_HEIGHT = 150.0d;
    private int minTrpAmt;
    private final SimpleIntegerProperty maxTrpAmt = new SimpleIntegerProperty();
    private Integer troopAmount = minTrpAmt;
    private final Label troopAmountLabel = new Label();
    private final TextField troopAmountTextField = new TextField();
    private final Button confirmBtn = new Button();
    private String text;

    private final SimpleIntegerProperty troopAmt = new SimpleIntegerProperty(-1); // 'illegal value' so setting 0 troops works because its != initial value

    public TroopAmountPopup(int minTrpAmt, int maxTrpAmt, String text) {
        setPrefWidth(TROOP_AMOUNT_POPUP_PREF_WIDTH);
        setPrefHeight(TROOP_AMOUNT_POPUP_PREF_HEIGHT);
        this.minTrpAmt = minTrpAmt;
        this.maxTrpAmt.setValue(maxTrpAmt);
        this.text = text;
        getStyleClass().add("troop-amount-box");
        troopAmountLabel.getStyleClass().add("troop-amount-label");
        troopAmountLabel.setText(String.format(text, minTrpAmt, maxTrpAmt));
        troopAmountTextField.setText(Integer.toString(troopAmount));
        troopAmountTextField.getStyleClass().add("troop-amount-text-field");
        confirmBtn.getStyleClass().add("confirm-troop-amount-btn");
        confirmBtn.setText("Bestaetigen");
        confirmBtn.setOnMouseClicked(event -> {
            troopAmt.setValue(troopAmount);
            if (maxTrpAmt == 3) troopAmt.setValue(-1); // to 'reset' property // TODO clean up, NPE without unsafe if
            troopAmountTextField.setText(Integer.toString(0));
        });
        confirmBtn.setDisable(troopAmount < minTrpAmt);
        troopAmountTextField.textProperty().addListener((obs, old, valNew) -> {
            try {
                troopAmount = Integer.parseInt(valNew);
                if (troopAmount > this.maxTrpAmt.get()) {
                    troopAmountTextField.setText(Integer.toString(this.maxTrpAmt.get()));
                } else if (troopAmount < minTrpAmt) {
                    troopAmountTextField.setText(Integer.toString(minTrpAmt));
                }
                confirmBtn.setDisable(false);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer");
                confirmBtn.setDisable(true);
            }
        });
        getChildren().addAll(troopAmountLabel, troopAmountTextField, confirmBtn);
    }

    public void setMaxTrpAmt(int maxTrpAmt) {
        this.maxTrpAmt.setValue(maxTrpAmt);
        troopAmountLabel.setText(String.format(text, minTrpAmt, maxTrpAmt));
    }

    public void setText(String text) {
        this.text = text;
        troopAmountLabel.setText(String.format(text, minTrpAmt, maxTrpAmt.get()));
    }

    public SimpleIntegerProperty getTroopAmt() {
        return troopAmt;
    }
}
