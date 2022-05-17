package ch.zhaw.ovtycoon.gui.model;

/**
 * Enum representing the actions of the risiko game.
 */
public enum Action {
    ATTACK("Angriff"), MOVE("Bewegung"), DEFEND("Verteidigung");

    private final String actionName;

    private Action(String name) {
        this.actionName = name;
    }

    public String getActionName() {
        return actionName;
    }
}
