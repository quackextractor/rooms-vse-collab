package player;

import items.Item;
import items.Weapon;
import room.Room;
import utilities.Logic;

import java.util.*;

public class Player {
    private Room currentRoom;
    private Map<Integer, Room> roomMap;
    private List<Item> inventoryItems;
    private List<Weapon> inventoryWeapons;
    private Map<String, Integer> stats = new LinkedHashMap<>();
    private List<String> existingStats = new ArrayList<>();


    public void setInventoryWeapons(List<Weapon> inventoryWeapons) {
        this.inventoryWeapons = inventoryWeapons;
    }

    public void setDiscardedItems(List<Item> discardedItems) {
        this.discardedItems = discardedItems;
    }

    public List<Weapon> getInventoryWeapons() {
        return inventoryWeapons;
    }

    private List<Item> discardedItems;

    public Player(Room initialRoom, Map<Integer, Room> roomMap) {

        this.currentRoom = initialRoom;
        this.roomMap = roomMap;
        this.inventoryItems = new ArrayList<>();
        this.inventoryWeapons = new ArrayList<>();
        this.discardedItems = new ArrayList<>();

        // Default stats
        stats.put("strength", 10);
        stats.put("wisdom", 10);
        stats.put("agility", 10);
        stats.put("health", 10);

        existingStats.addAll(stats.keySet());
    }

    public void pickUpItem(Item item, Player player) {
        if (currentRoom.getAvailableItems().contains(item)) {
            currentRoom.getAvailableItems().remove(item);
            player.inventoryItems.add(item);

            if (item instanceof Weapon) {
                inventoryWeapons.add((Weapon) item);
            }

            Logic.printToUser("Picked up " + item.getName() + ".");
            // Apply stats from the picked up item
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

    private void applyStatIncrease(String statName, int value) {
        if (value != 0) {
            if (existingStats.contains(statName)) {
                int currentValue = stats.get(statName);
                stats.put(statName, currentValue + value);
            } else {
                stats.put(statName, value);
                existingStats.add(statName);
            }
            if (value > 0) {
                Logic.printToUser("Your " + statName + " has increased to " + stats.get(statName) + ".");
            } else {
                Logic.printToUser("Your " + statName + " has decreased to " + stats.get(statName) + ".");
            }
        }
    }

    public void applyStats(Map<String, Integer> stats) {
        if (stats != null) {
            for (Map.Entry<String, Integer> entry : stats.entrySet()) {
                String statName = entry.getKey();
                int value = entry.getValue();
                applyStatIncrease(statName, value);
            }
        }
    }

    public void setCurrentRoom(Room currentRoom) {
        this.currentRoom = currentRoom;
    }

    private void increaseStat(String statName, int value) {
        int currentStat = getStatValue(statName);
        setStatValue(statName, currentStat + value);
        if (value > 0) {
            Logic.printToUser("Your " + statName + " has increased to " + getStatValue(statName) + ".");
        } else {
            Logic.printToUser("Your " + statName + " has decreased to " + getStatValue(statName) + ".");
        }
    }

    public void discardItem(Item item) {
        if (inventoryItems.contains(item)) {
            inventoryItems.remove(item);
            List<Item> newSetOfItems = currentRoom.getAvailableItems();
            newSetOfItems.add(item);
            currentRoom.setAvailableItems(newSetOfItems);
            if (item instanceof Weapon) {
                inventoryWeapons.remove(item);
            }
            removeStatsFrom(item.getStats());
            Logic.printToUser("Discarded " + item.getName() + ".");
            showStats();
        } else {
            Logic.printToUser("Item not found in the inventory.");
        }
    }

    private void removeStatsFrom(Map<String, Integer> stats) {
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            // Negate the value
            increaseStat(key, -value);
        }
    }

    public int getStatValue(String stat) {
        return stats.getOrDefault(stat, 0);
    }

    public void setStatValue(String stat, int value) {
        stats.put(stat, value);
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
        if (inventoryItems != null && !inventoryItems.isEmpty()) {
            for (int i = 0; i < inventoryItems.size(); i++) {
                Logic.printToUser((i + 1) + ". " + inventoryItems.get(i).getName() + inventoryItems.get(i).getStats());
            }
        }
        if (inventoryWeapons != null && !inventoryWeapons.isEmpty()) {
            Logic.printToUser("Weapons:");
            for (Weapon weapon : inventoryWeapons) {
                Logic.printToUser("- " + weapon.getName() + ", Damage: " + weapon.getDamage());
            }
        }
        if ((inventoryItems == null && inventoryWeapons == null) || inventoryItems.isEmpty() && Objects.requireNonNull(inventoryWeapons).isEmpty()) {
            Logic.printToUser("Empty");
        }
    }

    private void move(int roomId, Player player) {
        if (roomId != -1) {
            Room nextRoom = roomMap.get(roomId);
            if (nextRoom.canEnter(player)) {
                currentRoom = nextRoom;
                if (!currentRoom.getDescription().isEmpty()) {
                    Logic.printToUser(currentRoom.getDescription());
                } else {
                    Logic.printToUser("Moved to " + currentRoom.getName());
                }
            }
        } else {
            Logic.printToUser("You cannot move in that direction from " + currentRoom.getName());
        }
    }

    public List<Item> getInventoryItems() {
        return inventoryItems;
    }

    public void setInventoryItems(List<Item> inventoryItems) {
        this.inventoryItems = inventoryItems;
    }
}