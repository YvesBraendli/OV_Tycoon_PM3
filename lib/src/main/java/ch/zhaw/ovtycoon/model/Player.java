package ch.zhaw.ovtycoon.model;

import ch.zhaw.ovtycoon.Config;

import java.io.Serializable;

public class Player implements Serializable {
    private String name;
    private Config.PlayerColor color = null;

    public Player(String name){
        this.name = name;
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
}
