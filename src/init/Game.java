package init;

import myExceptions.ParsingLevelException;
import player.Player;
import player.PlayerController;
import room.Room;
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

    public void run() throws ParsingLevelException {
        Logic.printToUser(" \n");
        MapLoader.loadMap("rooms.csv");

        while (true) {
            Menu.Action action = menu.promptChoice();
            switch (action) {
                case NEW_GAME:
                    startNewGame();
                    return;
                case CONTINUE:
                    if (!SaveManager.saveExists()) {
                        Logic.printToUser("No saved game found.");
                        break;
                    }
                    Player savedPlayer = SaveManager.load();
                    if (savedPlayer == null) {
                        Logic.printToUser("Failed to load saved game.");
                        break;
                    }
                    continueGame(savedPlayer);
                    return;
                case EXIT:
                    Logic.printToUser("Quitting. Goodbye!");
                    System.exit(0);
                    return;
            }
        }
    }

    private void startNewGame() throws ParsingLevelException {
        Map<Integer, Room> roomMap = MapLoader.loadMap("rooms.csv");
        Room initialRoom = roomMap.get(0);
        Player player = new Player(initialRoom, roomMap);
        SaveManager.save(player);
        playGame(player, initialRoom.getDescription());
    }

    private void continueGame(Player player) {
        Logic.printToUser("Continuing game...");
        playGame(player, player.getCurrentRoom().getDescription());
    }

    private void playGame(Player player, String openingDescription) {
        PlayerController playerController = new PlayerController(player);
        PlayerController.printHelp();
        Logic.printToUser("");
        if (!openingDescription.isEmpty()) {
            Logic.printToUser(openingDescription);
        }
        player.showMap();

        while (true) {
            playerController.getPlayerInput(player);
        }
    }
}
