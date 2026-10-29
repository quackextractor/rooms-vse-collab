package player;

import items.Item;
import room.Room;
import utilities.Logic;

import java.util.List;

public class PlayerView {
    private PlayerView() {
    }

    public static void showStats(Player player) {
        Logic.printToUser("Stats:");
        for (String name : player.getStats().keySet()) {
            String capitalised = name.substring(0, 1).toUpperCase() + name.substring(1);
            Logic.printToUser("  " + capitalised + " " + player.getStatValue(name));
        }
    }

    public static void showInventory(Player player) {
        Logic.printToUser("Inventory:");
        showNumbered(player.getInventoryItems(), "Empty");
    }

    public static void showItemsInRoom(Room room) {
        Logic.printToUser("Items in the current room:");
        showNumbered(room.getAvailableItems(), "No items in the room.");
    }

    private static void showNumbered(List<Item> items, String whenEmpty) {
        if (items.isEmpty()) {
            Logic.printToUser(whenEmpty);
        }
        for (int i = 0; i < items.size(); i++) {
            Logic.printToUser((i + 1) + ". " + items.get(i));
        }
    }
}
