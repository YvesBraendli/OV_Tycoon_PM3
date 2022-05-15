package ch.zhaw.ovtycoon.data;

import ch.zhaw.ovtycoon.Config;

import java.io.Serializable;

public class Player implements Serializable {
    private String name;
    private Config.PlayerColor color = null;
    private boolean isEliminated = false;

    /**
     * Inits player with name from argument
     * @param name player name
     */
    public Player(String name){
        this.name = name;
    }

    /**
     * Inits player with color from argument
     * @param color player color
     */
    public Player(Config.PlayerColor color) { this.color = color;}

    public String getName() {
        return name;
    }

    public Config.PlayerColor getColor() {
        return color;
    }

    public void setColor(Config.PlayerColor color){
        this.color = color;
    }

	public boolean isEliminated() {
		return isEliminated;
	}

	public void setEliminated() {
		isEliminated = true;
	}
}
