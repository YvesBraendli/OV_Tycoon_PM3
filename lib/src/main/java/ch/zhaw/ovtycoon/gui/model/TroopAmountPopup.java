package ch.zhaw.ovtycoon.gui.model;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Popup for moving troops with label, text field and confirm button
 */
// TODO should depend on current move when other moves implemented
public class TroopAmountPopup extends VBox {
    private final int minTrpAmt = 1;
    private int maxTrpAmt;
    private Integer troopAmount = minTrpAmt;
    private final Label troopAmountLabel = new Label();
    private final TextField troopAmountTextField = new TextField();
    private final Button confirmBtn = new Button();

    public TroopAmountPopup(int maxTrpAmt) {
        setPrefWidth(200.0d);
        setPrefHeight(150.0d);
        this.maxTrpAmt = maxTrpAmt;
        getStyleClass().add("troop-amount-box");
        troopAmountLabel.getStyleClass().add("troop-amount-label");
        troopAmountLabel.setText(String.format("Anzahl Truppen\nDu kannst %d - %d Truppen verschieben", minTrpAmt, maxTrpAmt));
        troopAmountTextField.setId("troopsAmount");
        troopAmountTextField.setText(Integer.toString(troopAmount));
        troopAmountTextField.getStyleClass().add("troop-amount-text-field");
        confirmBtn.setId("confirmTroopAmount");
        confirmBtn.getStyleClass().add("confirm-troop-amount-btn");
        confirmBtn.setText("Bestaetigen");
        troopAmountTextField.textProperty().addListener((obs, old, valNew) -> {
            try {
                troopAmount = Integer.parseInt(valNew);
                System.out.println(troopAmount);
                if (troopAmount > maxTrpAmt) {
                    troopAmountTextField.setText(Integer.toString(maxTrpAmt));
                } else if (troopAmount < minTrpAmt) {
                    troopAmountTextField.setText(Integer.toString(minTrpAmt));
                }
                confirmBtn.setDisable(false);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer");
                confirmBtn.setDisable(true);
            }
        });
        troopAmountTextField.getOnInputMethodTextChanged();
        getChildren().addAll(troopAmountLabel, troopAmountTextField, confirmBtn);
    }

    public Button getConfirmBtn() {
        return confirmBtn;
    }

    public Integer getTroopAmount() {
        return troopAmount;
    }
}
