import commands.Command;
import commands.*;
import init.Game;
import items.Item;
import myExceptions.ParsingLevelException;
import player.Player;
import player.PlayerController;
import room.Room;
import utilities.*;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        int exitCode = 0;
        try {
            Game game = new Game();
            game.run();
        } catch (ParsingLevelException p) {
            Logic.printToUser(p.getMessage());
            Logic.printToUser("Unable to load game. Quitting.");
            exitCode = 1;
        } catch (Exception e) {
            Logic.printToUser("Unexpected error: " + e);
            exitCode = 1;
        } finally {
            Logic.printToUser("Goodbye!");
        }
        System.exit(exitCode);
    }
}