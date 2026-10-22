package tests;

import items.Item;
import myExceptions.ParsingLevelException;
import player.Player;
import player.PlayerController;
import room.Room;
import utilities.Console;
import utilities.MapLoader;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;

public class GameSession {
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private final Map<Integer, Room> rooms;
    private final Player player;
    private final PlayerController controller;

    public GameSession() throws ParsingLevelException {
        this(0);
    }

    public GameSession(int startRoomId) throws ParsingLevelException {
        rooms = MapLoader.loadMap("rooms.csv");
        player = new Player(rooms.get(startRoomId), rooms);
        controller = new PlayerController(player);
    }

    public void play(String... lines) {
        PrintStream screen = System.out;
        System.setOut(new PrintStream(output, true));
        try {
            Console.useInput(String.join("\n", lines) + "\n");
            while (Console.hasInput()) {
                controller.getPlayerInput(player);
            }
        } finally {
            System.setOut(screen);
        }
    }

    public Player player() {
        return player;
    }

    public Room room(int id) {
        return rooms.get(id);
    }

    public String currentRoom() {
        return player.getCurrentRoom().getName();
    }

    public int stat(String name) {
        return player.getStatValue(name);
    }

    public boolean carries(String itemName) {
        return player.getInventoryItems().stream().map(Item::getName).anyMatch(itemName::equals);
    }

    public boolean printed(String text) {
        return output.toString().contains(text);
    }
}
