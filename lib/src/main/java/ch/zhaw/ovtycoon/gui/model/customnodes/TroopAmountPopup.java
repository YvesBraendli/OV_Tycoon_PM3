package ch.zhaw.ovtycoon.gui.model.customnodes;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Popup for getting an amount of troops with label, text field and confirm button.
 * Extends {@link VBox}
 */
public class TroopAmountPopup extends VBox {
    public static final double TROOP_AMOUNT_POPUP_PREF_WIDTH = 200.0d;
    public static final double TROOP_AMOUNT_POPUP_PREF_HEIGHT = 150.0d;
    private final SimpleIntegerProperty maxTrpAmt = new SimpleIntegerProperty();
    private final Label troopAmountLabel = new Label();
    private final TextField troopAmountTextField = new TextField();
    private final Button confirmBtn = new Button();
    private int minTrpAmt;
    private Integer troopAmount;
    private String labelText;

    /**
     * Creates an instance of the popup. Adds an action listener to the {@link #troopAmountTextField}'s text property.
     * Disables the {@link #confirmBtn} based on the value of the text property.
     *
     * @param minTrpAmt Minimal amount of troops which can be entered
     * @param maxTrpAmt Maximal amount of troops which can be entered
     * @param labelText Text of the popup's label
     */
    public TroopAmountPopup(int minTrpAmt, int maxTrpAmt, String labelText) {
        setPrefWidth(TROOP_AMOUNT_POPUP_PREF_WIDTH);
        setPrefHeight(TROOP_AMOUNT_POPUP_PREF_HEIGHT);
        this.minTrpAmt = minTrpAmt;
        this.maxTrpAmt.setValue(maxTrpAmt);
        this.labelText = labelText;
        this.troopAmount = minTrpAmt;
        getStyleClass().add("troop-amount-box");
        troopAmountLabel.getStyleClass().add("troop-amount-label");
        troopAmountLabel.setText(String.format(labelText, minTrpAmt, maxTrpAmt));
        troopAmountTextField.setText(Integer.toString(troopAmount));
        troopAmountTextField.getStyleClass().add("troop-amount-text-field");
        confirmBtn.getStyleClass().add("confirm-troop-amount-btn");
        confirmBtn.setText("Bestaetigen");
        confirmBtn.setDisable(troopAmount < minTrpAmt || troopAmount > this.maxTrpAmt.get());
        troopAmountTextField.textProperty().addListener((obs, old, valNew) -> {
            try {
                troopAmount = Integer.parseInt(valNew);
                troopAmountTextField.setText(Integer.toString(troopAmount));
                confirmBtn.setDisable(troopAmount < minTrpAmt || troopAmount > this.maxTrpAmt.get());

            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer");
                confirmBtn.setDisable(true);
            }
        });
        getChildren().addAll(troopAmountLabel, troopAmountTextField, confirmBtn);
    }

    /**
     * Reconfigure the popup by updating the max troop amount and the label text.
     *
     * @param maxTrpAmt Maximal amount of troops which can be entered
     * @param labelText Text of the label
     */
    public void reconfigure(int maxTrpAmt, String labelText) {
        this.maxTrpAmt.setValue(maxTrpAmt);
        this.labelText = labelText;
        troopAmountLabel.setText(String.format(labelText, minTrpAmt, maxTrpAmt));
        troopAmountTextField.setText(Integer.toString(minTrpAmt)); // reset value in text field to min amount
    }

    public Integer getTroopAmount() {
        return troopAmount;
    }

    public Button getConfirmBtn() {
        return confirmBtn;
    }
}
