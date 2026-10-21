package init;

import myExceptions.ParsingLevelException;
import player.Player;
import player.PlayerController;
import room.Room;
import utilities.Logic;
import utilities.MapLoader;

import java.util.Map;

public class Game {
    public void run() throws ParsingLevelException {
        Logic.printToUser(" \n");
        Map<Integer, Room> roomMap = MapLoader.loadMap("rooms.csv");

        Room initialRoom = roomMap.get(0);
        Player player = new Player(initialRoom, roomMap);

        PlayerController playerController = new PlayerController(player);

        PlayerController.printHelp();

        Logic.printToUser("");
        Logic.printToUser(initialRoom.getDescription());
        player.showMap();

        while (true) {
            playerController.getPlayerInput(player);
        }
    }
}
