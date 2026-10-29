package room;

import enemy.Enemy;
import items.Item;
import items.Weapon;
import player.Player;
import utilities.Config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Room implements Serializable {
    private static final long serialVersionUID = 2L;
    private final int id;
    private final String name;
    private final String description;
    private final String combatMessage;
    private final boolean isWinRoom;
    private final boolean isSecret;
    private final List<RoomCondition> conditions;
    private final List<Item> availableItems;
    private final List<Enemy> availableEnemies;
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
        this.combatMessage = Objects.requireNonNull(combatMessage, "combatMessage");
        this.conditions = List.copyOf(conditions);
        this.north = checkExit(north);
        this.east = checkExit(east);
        this.south = checkExit(south);
        this.west = checkExit(west);
        this.isWinRoom = isWinRoom;
        this.winMessage = Objects.requireNonNull(winMessage, "winMessage");
        this.isSecret = isSecret;
        this.availableItems = new ArrayList<>(items);
        this.availableItems.addAll(weapons);
        this.availableEnemies = new ArrayList<>(enemies);
    }

    private int checkExit(int roomId) {
        if (roomId < Config.WALL) {
            throw new IllegalArgumentException("Room " + id + " has an exit to " + roomId + ". Use " + Config.WALL + " for a wall.");
        }
        return roomId;
    }

    public Optional<RoomCondition> unmetCondition(Player player) {
        return conditions.stream()
                .filter(condition -> player.getStatValue(condition.getStat()) < condition.getMinValue())
                .findFirst();
    }

    public int getExit(Direction direction) {
        return switch (direction) {
            case NORTH -> north;
            case EAST -> east;
            case SOUTH -> south;
            case WEST -> west;
        };
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

    public List<RoomCondition> getConditions() {
        return conditions;
    }

    public List<Item> getAvailableItems() {
        return availableItems;
    }

    public List<Enemy> getAvailableEnemies() {
        return availableEnemies;
    }

    public String getDescription() {
        return description;
    }
}
