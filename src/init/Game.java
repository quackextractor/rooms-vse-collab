package init;

import myExceptions.ParsingLevelException;
import player.Player;
import player.PlayerController;
import room.Room;
import utilities.Logic;
import utilities.MapLoader;

import java.io.IOException;
import java.util.Map;

public class Game {
    public void run() throws ParsingLevelException, IOException {
        Logic.printToUser(" \n");
        Map<Integer, Room> roomMap = MapLoader.loadMap("rooms.csv");

        // Starting from room with ID 0
        Room initialRoom = roomMap.get(0);
        Player player = new Player(initialRoom, roomMap);

        PlayerController playerController = new PlayerController(player);

        Logic.printToUser("Enter direction (N, E, S, W) to move around, I to show inventory, " +
                "P to pick up item, D to discard item, L to look around you, M to show the map " +
                "or Q to quit. Press H to bring up this help.");

        Logic.printToUser("");
        // print description of starting room
        Logic.printToUser(initialRoom.getDescription());
        player.showMap();

        // Game loop
        while (true) {
            // Set commands based on user input
            playerController.getPlayerInput(player);
        }
    }
}
