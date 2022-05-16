package ch.zhaw.ovtycoon.gui.model.dto;

public class ReinforcementAmountDTO {
    private final int totalAmount;
    private int amountAlreadyPlaced;

    public ReinforcementAmountDTO(int totalAmount, int amountAlreadyPlaced) {
        this.totalAmount = totalAmount;
        this.amountAlreadyPlaced = amountAlreadyPlaced;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public int getAmountAlreadyPlaced() {
        return amountAlreadyPlaced;
    }

    public void addToAmountAlreadyPlaced(int placed) {
        this.amountAlreadyPlaced += placed;
    }
}
