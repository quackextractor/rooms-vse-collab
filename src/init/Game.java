package init;

import myExceptions.ParsingLevelException;
import player.Player;
import player.PlayerController;
import room.Room;
import utilities.Console;
import utilities.Logic;
import utilities.MapLoader;
import utilities.SaveManager;

import java.util.Map;

public class Game {
    private static final String MAP_FILE = "rooms.csv";
    private final Menu menu;

    public Game() {
        this(new Menu());
    }

    public Game(Menu menu) {
        this.menu = menu;
    }

    public void run() throws ParsingLevelException {
        Map<Integer, Room> roomMap = MapLoader.loadMap(MAP_FILE);

        while (true) {
            switch (menu.promptChoice()) {
                case NEW_GAME:
                    if (SaveManager.saveExists() && !Console.confirm("This replaces your saved game. Start a new game?")) {
                        break;
                    }
                    Player player = new Player(roomMap.get(0), roomMap);
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
