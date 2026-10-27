package room;

import enemy.Enemy;
import items.Item;
import items.Weapon;
import player.Player;
import utilities.Logic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Room implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int id;
    private final String name;
    private final String description;
    private final String combatMessage;
    private final boolean isWinRoom;
    private final boolean isSecret;
    private final List<Item> items;
    private final List<Weapon> weapons;
    private final List<RoomCondition> conditions;
    private final List<Enemy> enemies;
    private final List<Item> availableItems;
    private final List<Enemy> availableEnemies;
    private final List<Item> pickedUpItems = new ArrayList<>();
    private final int north;
    private final int east;
    private final int south;
    private final int west;
    private final String winMessage;

    public Room(int id, String name, String description, List<Item> items, List<Enemy> enemies, String combatMessage, List<Weapon> weapons,
                List<RoomCondition> conditions, int north, int east, int south, int west, boolean isWinRoom, String winMessage, boolean isSecret) {
        if (id < 0) {
            throw new IllegalArgumentException("Room id must not be negative, got " + id + ".");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Room " + id + " has no name.");
        }
        this.id = id;
        this.name = name;
        this.description = Objects.requireNonNull(description, "description");
        this.items = List.copyOf(items);
        this.enemies = List.copyOf(enemies);
        this.combatMessage = Objects.requireNonNull(combatMessage, "combatMessage");
        this.weapons = List.copyOf(weapons);
        this.conditions = List.copyOf(conditions);
        this.north = exit(north);
        this.east = exit(east);
        this.south = exit(south);
        this.west = exit(west);
        this.isWinRoom = isWinRoom;
        this.winMessage = Objects.requireNonNull(winMessage, "winMessage");
        this.isSecret = isSecret;
        this.availableItems = new ArrayList<>(items);
        this.availableItems.addAll(weapons);
        this.availableEnemies = new ArrayList<>(enemies);
    }

    private int exit(int roomId) {
        if (roomId < -1) {
            throw new IllegalArgumentException("Room " + id + " has an exit to " + roomId + ". Use -1 for a wall.");
        }
        return roomId;
    }

    public boolean canEnter(Player player) {
        for (RoomCondition condition : getConditions()) {
            int playerStatValue = player.getStatValue(condition.getStat());
            if (playerStatValue < condition.getMinValue()) {
                Logic.printToUser("Cannot enter room: " + condition.getMessage());
                return false;
            }
        }
        return true;
    }

    public String getWinMessage() {
        return winMessage;
    }

    public boolean isWinRoom() {
        return isWinRoom;
    }

    public boolean isSecret() {
        return isSecret;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCombatMessage() {
        return combatMessage;
    }

    public List<Item> getItems() {
        return items;
    }

    public List<Weapon> getWeapons() {
        return weapons;
    }

    public List<RoomCondition> getConditions() {
        return conditions;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public int getNorth() {
        return north;
    }

    public int getEast() {
        return east;
    }

    public int getSouth() {
        return south;
    }

    public int getWest() {
        return west;
    }

    public List<Item> getAvailableItems() {
        return availableItems;
    }

    public List<Item> getPickedUpItems() {
        return pickedUpItems;
    }

    public List<Enemy> getAvailableEnemies() {
        return availableEnemies;
    }

    public String getDescription() {
        return description;
    }
}
