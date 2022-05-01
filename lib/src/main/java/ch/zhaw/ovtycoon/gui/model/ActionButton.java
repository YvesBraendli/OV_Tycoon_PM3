package ch.zhaw.ovtycoon.gui.model;

import javafx.scene.control.Button;


public class ActionButton extends Button {
    private Action action = Action.ATTACK;

    public ActionButton() {
        setText(action.name().toLowerCase());
        setDisable(true);
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
        setText(action.getActionName());
    }
}
