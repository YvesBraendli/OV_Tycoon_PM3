package ch.zhaw.ovtycoon.gui.service;

import ch.zhaw.ovtycoon.model.Game;
import java.io.*;

public class GameStateService {
    private final String fileName = "yourfile2.txt";

    public Game loadGameState(){
        Game game = null;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(fileName);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            game = (Game) objectInputStream.readObject();
            objectInputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println(e);
            return null;
        }
        return game;
    }

    public boolean saveGameState(Game game){
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(fileName);
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
