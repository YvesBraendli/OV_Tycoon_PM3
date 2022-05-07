package ch.zhaw.ovtycoon.model;

import ch.zhaw.ovtycoon.Config;

public class Player {
    private String name;
    private Config.PlayerColor color = null;
    private boolean isEliminated;

    public Player(String name){
        this.name = name;
        isEliminated = false;
    }

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
