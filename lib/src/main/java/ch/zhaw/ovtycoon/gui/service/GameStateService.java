package ch.zhaw.ovtycoon.gui.service;

import ch.zhaw.ovtycoon.model.Game;
import java.io.*;

/**
 * Service to use for persisting and reloading the GameState.
 */
public class GameStateService {
    private final String fileName = "serializedGameState.txt";

    /**
     * Deserializes the current game state from a textfile, and makes it possible to reload the whole game object.
     * @return the loaded game object, returns null if the object does not exist or couldn't be loaded
     */
    public Game loadGameState(){
        Game game = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(fileName);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            game = (Game) objectInputStream.readObject();
            objectInputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println(e);
            return null;
        }
        return game;
    }

    /**
     * Serializes the current game state to a textfile, and persists the whole object.
     * @param game the object to be serialized
     * @return  true if the object could be successfully persisted, false if not
     */
    public boolean saveGameState(Game game){
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(game);
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (IOException e) {
            System.err.println(e);
            return false;
        }
        return true;
    }
}
