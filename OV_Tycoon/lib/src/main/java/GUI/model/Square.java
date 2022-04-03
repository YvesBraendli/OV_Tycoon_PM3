package GUI.model;

public class Square {
    private int startX;
    private int endX;
    private int startY;
    private int endY;

    public Square(int startX, int offsetX, int startY, int offsetY) {
        this.startX = startX;
        this.endX = startX + offsetX;
        this.startY = startY;
        this.endY = startY + offsetY;
    }

    public Square() {}

    public int getStartX() {
        return startX;
    }

    public int getEndX() {
        return endX;
    }

    public int getStartY() {
        return startY;
    }

    public int getEndY() {
        return endY;
    }

    public void setStartX(int startX) {
        this.startX = startX;
    }

    public void setEndX(int offsetX) {
        this.endX = startX + offsetX;
    }

    public void setStartY(int startY) {
        this.startY = startY;
    }

    public void setEndY(int offsetY) {
        this.endY = startY + offsetY;
    }
}
