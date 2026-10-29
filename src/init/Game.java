package init;

import myExceptions.ParsingLevelException;
import player.Player;
import player.PlayerController;
import room.Room;
import utilities.Config;
import utilities.Console;
import utilities.Logic;
import utilities.MapLoader;
import utilities.SaveManager;

import java.util.Map;

public class Game {
    private final Menu menu;

    public Game() {
        this(new Menu());
    }

    public Game(Menu menu) {
        this.menu = menu;
    }

    public void run() {
        while (true) {
            switch (menu.promptChoice()) {
                case NEW_GAME:
                    if (SaveManager.saveExists() && !Console.confirm("This replaces your saved game. Start a new game?")) {
                        break;
                    }
                    Player player = newPlayer();
                    if (player == null) {
                        break;
                    }
                    SaveManager.save(player);
                    play(player);
                    return;
                case CONTINUE:
                    if (!SaveManager.saveExists()) {
                        Logic.printToUser("No saved game found.");
                        break;
                    }
                    Player savedPlayer = SaveManager.load();
                    if (savedPlayer == null) {
                        break;
                    }
                    Logic.printToUser("Continuing game...");
                    play(savedPlayer);
                    return;
                case EXIT:
                    return;
            }
        }
    }

    private Player newPlayer() {
        try {
            Map<Integer, Room> roomMap = MapLoader.loadMap(Config.MAP_FILE);
            return new Player(roomMap.get(Config.START_ROOM_ID), roomMap);
        } catch (ParsingLevelException e) {
            Logic.printToUser(e.getMessage());
            Logic.printToUser("A new game needs a valid " + Config.MAP_FILE + ". A saved game can still be continued.");
            return null;
        }
    }

    private void play(Player player) {
        PlayerController controller = new PlayerController(player);
        PlayerController.printHelp();
        Logic.printToUser("");
        String description = player.getCurrentRoom().getDescription();
        if (!description.isEmpty()) {
            Logic.printToUser(description);
        }
        player.showMap();

        boolean playing = true;
        while (playing) {
            playing = controller.playTurn();
        }
    }
}
