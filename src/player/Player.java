package player;

import items.Item;
import items.Weapon;
import room.Room;
import utilities.Logic;
import utilities.MapView;

import java.util.*;

public class Player {
    private final Map<Integer, Room> roomMap;
    private final List<Item> inventoryItems = new ArrayList<>();
    private final List<Weapon> inventoryWeapons = new ArrayList<>();
    private final Map<String, Integer> stats = new LinkedHashMap<>();
    private final Set<Integer> discoveredRoomIds = new LinkedHashSet<>();
    private Room currentRoom;

    public Player(Room initialRoom, Map<Integer, Room> roomMap) {
        this.currentRoom = Objects.requireNonNull(initialRoom, "initialRoom");
        this.roomMap = Objects.requireNonNull(roomMap, "roomMap");
        this.discoveredRoomIds.add(initialRoom.getId());

        stats.put("strength", 10);
        stats.put("wisdom", 10);
        stats.put("agility", 10);
        stats.put("health", 10);
    }

    public List<Weapon> getInventoryWeapons() {
        return Collections.unmodifiableList(inventoryWeapons);
    }

    public List<Item> getInventoryItems() {
        return Collections.unmodifiableList(inventoryItems);
    }

    public void pickUpItem(Item item, Player player) {
        if (currentRoom.getAvailableItems().contains(item)) {
            currentRoom.getAvailableItems().remove(item);
            player.inventoryItems.add(item);

            if (item instanceof Weapon) {
                inventoryWeapons.add((Weapon) item);
            }

            Logic.printToUser("Picked up " + item.getName() + ".");
            applyStats(item.getStats());
            showStats();
            showItemsInRoom();
        } else {
            Logic.printToUser("Item not found in the room.");
        }
    }

    public void showItemsInRoom() {
        Logic.printToUser("Items in the current room:");
        List<Item> availableItems = currentRoom.getAvailableItems();
        for (int i = 0; i < availableItems.size(); i++) {
            Logic.printToUser((i + 1) + ". " + availableItems.get(i));
        }

        if (availableItems.isEmpty()) {
            Logic.printToUser("No items in the room.");
        }
    }

    public void showStats() {
        Logic.printToUser("Stats:");
        for (Map.Entry<String, Integer> stat : stats.entrySet()) {
            String name = stat.getKey();
            String capitalised = name.substring(0, 1).toUpperCase() + name.substring(1);
            Logic.printToUser("  " + capitalised + " " + stat.getValue());
        }
    }

    public void applyStats(Map<String, Integer> stats) {
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            changeStat(entry.getKey(), entry.getValue());
        }
    }

    private void changeStat(String statName, int value) {
        if (value == 0) {
            return;
        }
        setStatValue(statName, getStatValue(statName) + value);
        String direction = value > 0 ? "increased" : "decreased";
        Logic.printToUser("Your " + statName + " has " + direction + " to " + getStatValue(statName) + ".");
    }

    public void discardItem(Item item) {
        if (inventoryItems.contains(item)) {
            inventoryItems.remove(item);
            currentRoom.getAvailableItems().add(item);
            if (item instanceof Weapon) {
                inventoryWeapons.remove(item);
            }
            for (Map.Entry<String, Integer> entry : item.getStats().entrySet()) {
                changeStat(entry.getKey(), -entry.getValue());
            }
            Logic.printToUser("Discarded " + item.getName() + ".");
            showStats();
        } else {
            Logic.printToUser("Item not found in the inventory.");
        }
    }

    public int getStatValue(String stat) {
        return stats.getOrDefault(stat, 0);
    }

    public void setStatValue(String stat, int value) {
        if (stat == null || stat.isBlank()) {
            throw new IllegalArgumentException("Stat name must not be blank.");
        }
        stats.put(stat, stat.equals("health") ? Math.max(0, value) : value);
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void moveNorth(Player player) {
        int northRoomId = currentRoom.getNorth();
        move(northRoomId, player);
    }

    public void moveEast(Player player) {
        int eastRoomId = currentRoom.getEast();
        move(eastRoomId, player);
    }

    public void moveSouth(Player player) {
        int southRoomId = currentRoom.getSouth();
        move(southRoomId, player);
    }

    public void moveWest(Player player) {
        int westRoomId = currentRoom.getWest();
        move(westRoomId, player);
    }

    public void showInventory() {
        Logic.printToUser("Inventory:");
        Logic.printToUser("Items:");
        for (int i = 0; i < inventoryItems.size(); i++) {
            Logic.printToUser((i + 1) + ". " + inventoryItems.get(i).getName() + inventoryItems.get(i).getStats());
        }
        if (!inventoryWeapons.isEmpty()) {
            Logic.printToUser("Weapons:");
            for (Weapon weapon : inventoryWeapons) {
                Logic.printToUser("- " + weapon.getName() + ", Damage: " + weapon.getDamage());
            }
        }
        if (inventoryItems.isEmpty()) {
            Logic.printToUser("Empty");
        }
    }

    private void move(int roomId, Player player) {
        if (roomId != -1) {
            Room nextRoom = roomMap.get(roomId);
            if (nextRoom.canEnter(player)) {
                currentRoom = nextRoom;
                discoveredRoomIds.add(currentRoom.getId());
                if (!currentRoom.getDescription().isEmpty()) {
                    Logic.printToUser(currentRoom.getDescription());
                } else {
                    Logic.printToUser("Moved to " + currentRoom.getName());
                }
                showMap();
            }
        } else {
            Logic.printToUser("You cannot move in that direction from " + currentRoom.getName());
        }
    }

    public void showMap() {
        MapView.print(roomMap, discoveredRoomIds, currentRoom.getId());
    }
}
