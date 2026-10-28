package tests;

import items.Item;
import player.Player;
import player.PlayerController;
import room.Room;
import utilities.Console;
import utilities.MapLoader;
import utilities.SaveManager;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Map;

public class GameSession {
    public static final Path SAVE_FILE = Path.of(System.getProperty("java.io.tmpdir"), "rooms-test-savegame.dat");
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private final Map<Integer, Room> rooms;
    private final Player player;
    private final PlayerController controller;

    public GameSession() throws Exception {
        this(0);
    }

    public GameSession(int startRoomId) throws Exception {
        SaveManager.useFile(SAVE_FILE);
        rooms = MapLoader.loadMap(TestMap.write(TestMap.ROOMS));
        player = new Player(rooms.get(startRoomId), rooms);
        controller = new PlayerController(player);
    }

    public void play(String... lines) {
        PrintStream screen = System.out;
        System.setOut(new PrintStream(output, true));
        try {
            Console.useInput(String.join("\n", lines) + "\n");
            boolean playing = true;
            while (playing && Console.hasInput()) {
                playing = controller.playTurn();
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

    public boolean printed(String... lines) {
        return output.toString().contains(String.join(System.lineSeparator(), lines));
    }
}
