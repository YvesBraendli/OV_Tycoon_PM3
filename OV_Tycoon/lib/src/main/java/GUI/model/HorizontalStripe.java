package GUI.model;

public class HorizontalStripe {
    private int startX;
    private int endX;
    private int y;

    public HorizontalStripe() {}

    public void setStartX(int startX) {
        this.startX = startX;
    }

    public void setEndX(int endX) {
        this.endX = endX;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getStartX() {
        return startX;
    }

    public int getEndX() {
        return endX;
    }

    public int getY() {
        return y;
    }
}
