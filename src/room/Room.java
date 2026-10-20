package room;

import enemy.Enemy;
import items.Item;
import items.Weapon;
import player.Player;
import utilities.Logic;

import java.util.ArrayList;
import java.util.List;

// oh my god
public class Room {
    private int id;
    private String name;
    private String description;
    private String combatMessage;
    private boolean isWinRoom;
    private List<Item> items;
    private List<Weapon> weapons;
    private List<RoomCondition> conditions;
    private List<Enemy> enemies;
    private List<Item> availableItems;
    private List<Enemy> availableEnemies;
    private List<Item> pickedUpItems;
    private int north;
    private int east;
    private int south;
    private int west;
    private String winMessage;

    public Room(int id, String name, String description, List<Item> items, List<Enemy> enemies, String combatMessage, List<Weapon> weapons,
                List<RoomCondition> conditions, int north, int east, int south, int west, boolean isWinRoom, String winMessage) {
        this.setId(id);
        this.setName(name);
        setDescription(description);
        this.setItems(items);
        this.setEnemies(enemies);
        this.setCombatMessage(combatMessage);
        this.setWeapons(weapons);
        this.setConditions(conditions);
        this.setNorth(north);
        this.setEast(east);
        this.setSouth(south);
        this.setWest(west);
        this.setWinRoom(isWinRoom);
        this.setWinMessage(winMessage);
        this.setAvailableItems(new ArrayList<>(items));
        getAvailableItems().addAll(weapons);
        this.setAvailableEnemies(new ArrayList<>(enemies));
        this.setPickedUpItems(new ArrayList<>());
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

    public void setWinMessage(String winMessage) {
        this.winMessage = winMessage;
    }

    public boolean isWinRoom() {
        return isWinRoom;
    }

    public void setWinRoom(boolean winRoom) {
        isWinRoom = winRoom;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCombatMessage() {
        return combatMessage;
    }

    public void setCombatMessage(String combatMessage) {
        this.combatMessage = combatMessage;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public List<Weapon> getWeapons() {
        return weapons;
    }

    public void setWeapons(List<Weapon> weapons) {
        this.weapons = weapons;
    }

    public List<RoomCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<RoomCondition> conditions) {
        this.conditions = conditions;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public void setEnemies(List<Enemy> enemies) {
        this.enemies = enemies;
    }

    public void setAvailableItems(List<Item> availableItems) {
        this.availableItems = availableItems;
    }

    public void setPickedUpItems(List<Item> pickedUpItems) {
        this.pickedUpItems = pickedUpItems;
    }

    public int getNorth() {
        return north;
    }

    public void setNorth(int north) {
        this.north = north;
    }

    public int getEast() {
        return east;
    }

    public void setEast(int east) {
        this.east = east;
    }

    public int getSouth() {
        return south;
    }

    public void setSouth(int south) {
        this.south = south;
    }

    public int getWest() {
        return west;
    }

    public void setWest(int west) {
        this.west = west;
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

    public void setAvailableEnemies(List<Enemy> availableEnemies) {
        this.availableEnemies = availableEnemies;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
