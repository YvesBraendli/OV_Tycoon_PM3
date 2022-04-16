package ch.zhaw.ovtycoon;

/**
 * This class handles all actions of a specific player
 */
public class Player {
    String name;
    Config.PlayerColor color = null;

    private Player(String name){
        //TODO: Check if name has already been taken
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String newName){
        //TODO: Check if name has already been taken
        this.name = newName;
    }

    public Config.PlayerColor getColor() {
        return color;
    }

    public void setColor(Config.PlayerColor color){
        //TODO: Check if color has already been taken
        this.color = color;
    }
}
