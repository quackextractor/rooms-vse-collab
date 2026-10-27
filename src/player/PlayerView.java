package player;

import items.Item;
import room.Room;
import utilities.Logic;

import java.util.List;
import java.util.Map;

public class PlayerView {
    private PlayerView() {
    }

    public static void showStats(Player player) {
        Logic.printToUser("Stats:");
        for (Map.Entry<String, Integer> stat : player.getStats().entrySet()) {
            String name = stat.getKey();
            String capitalised = name.substring(0, 1).toUpperCase() + name.substring(1);
            Logic.printToUser("  " + capitalised + " " + stat.getValue());
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
