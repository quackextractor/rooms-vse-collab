package player;

import commands.Command;
import items.Item;
import items.Weapon;
import room.Direction;
import room.Room;
import room.RoomCondition;
import utilities.Logic;
import utilities.MapView;

import java.io.Serializable;
import java.util.*;

public class Player implements Serializable {
    private static final long serialVersionUID = 3L;
    private final Map<Integer, Room> roomMap;
    private final List<Item> inventoryItems = new ArrayList<>();
    private final Map<String, Integer> stats = new LinkedHashMap<>();
    private final Set<Integer> discoveredRoomIds = new LinkedHashSet<>();
    private final Deque<Command> history = new ArrayDeque<>();
    private Room currentRoom;
    private Room previousRoom;

    public Player(Room initialRoom, Map<Integer, Room> roomMap) {
        this.currentRoom = Objects.requireNonNull(initialRoom, "initialRoom");
        this.roomMap = Objects.requireNonNull(roomMap, "roomMap");
        this.discoveredRoomIds.add(initialRoom.getId());

        stats.put("strength", 10);
        stats.put("wisdom", 10);
        stats.put("agility", 10);
        stats.put("health", 10);
    }

    public List<Item> getInventoryItems() {
        return Collections.unmodifiableList(inventoryItems);
    }

    public List<Weapon> getInventoryWeapons() {
        return inventoryItems.stream()
                .filter(Weapon.class::isInstance)
                .map(Weapon.class::cast)
                .toList();
    }

    public Map<String, Integer> getStats() {
        return Collections.unmodifiableMap(stats);
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

    public boolean pickUpItem(Item item) {
        if (!currentRoom.getAvailableItems().contains(item)) {
            Logic.printToUser("Item not found in the room.");
            return false;
        }
        carry(item);
        Logic.printToUser("Picked up " + item.getName() + ".");
        for (Map.Entry<String, Integer> bonus : item.getStats().entrySet()) {
            changeStat(bonus.getKey(), bonus.getValue());
        }
        return true;
    }

    public boolean discardItem(Item item) {
        if (!inventoryItems.contains(item)) {
            Logic.printToUser("Item not found in the inventory.");
            return false;
        }
        drop(item);
        for (Map.Entry<String, Integer> bonus : item.getStats().entrySet()) {
            changeStat(bonus.getKey(), -lossOnDiscard(bonus.getKey(), bonus.getValue()));
        }
        Logic.printToUser("Discarded " + item.getName() + ".");
        return true;
    }

    public void undoPickUp(Item item, Map<String, Integer> statsBefore) {
        drop(item);
        Logic.printToUser("Put back " + item.getName() + ".");
        restoreStats(statsBefore);
    }

    public void undoDiscard(Item item, Map<String, Integer> statsBefore) {
        carry(item);
        Logic.printToUser("Took back " + item.getName() + ".");
        restoreStats(statsBefore);
    }

    private void carry(Item item) {
        currentRoom.getAvailableItems().remove(item);
        inventoryItems.add(item);
    }

    private void drop(Item item) {
        inventoryItems.remove(item);
        currentRoom.getAvailableItems().add(item);
    }

    private void restoreStats(Map<String, Integer> statsBefore) {
        Set<String> names = new LinkedHashSet<>(stats.keySet());
        names.addAll(statsBefore.keySet());
        for (String name : names) {
            changeStat(name, statsBefore.getOrDefault(name, 0) - getStatValue(name));
        }
    }

    private int lossOnDiscard(String stat, int bonus) {
        if (stat.equals("health")) {
            return Math.min(bonus, getStatValue("health") - 1);
        }
        return bonus;
    }

    private void changeStat(String statName, int value) {
        if (value == 0) {
            return;
        }
        setStatValue(statName, getStatValue(statName) + value);
        String direction = value > 0 ? "increased" : "decreased";
        Logic.printToUser("Your " + statName + " has " + direction + " to " + getStatValue(statName) + ".");
    }

    public boolean move(Direction direction) {
        int roomId = currentRoom.getExit(direction);
        if (roomId == -1) {
            Logic.printToUser("You cannot move in that direction from " + currentRoom.getName());
            return false;
        }
        Room nextRoom = roomMap.get(roomId);
        Optional<RoomCondition> unmet = nextRoom.unmetCondition(this);
        if (unmet.isPresent()) {
            Logic.printToUser("Cannot enter room: " + unmet.get().getMessage());
            return false;
        }
        enter(nextRoom);
        return true;
    }

    public void retreat() {
        if (previousRoom == null) {
            Logic.printToUser("Nowhere to go.");
            return;
        }
        enter(previousRoom);
    }

    public void returnTo(Room room) {
        enter(room);
    }

    private void enter(Room room) {
        previousRoom = currentRoom;
        currentRoom = room;
        discoveredRoomIds.add(room.getId());
        Logic.printToUser(room.getDescription().isEmpty() ? "Moved to " + room.getName() : room.getDescription());
        showMap();
    }

    public void showMap() {
        MapView.print(roomMap, discoveredRoomIds, currentRoom.getId());
    }

    public void record(Command command) {
        history.push(command);
    }

    public Optional<Command> takeLastCommand() {
        return Optional.ofNullable(history.poll());
    }

    public void forgetHistory() {
        history.clear();
    }
}
