package utilities;

import enemy.Combat;
import enemy.Enemy;
import items.Item;
import items.Weapon;
import myExceptions.ParsingLevelException;
import room.Direction;
import room.Room;
import room.RoomCondition;
import utilities.Config.Column;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

public class MapLoader {
    private static final String BYTE_ORDER_MARK = "﻿";

    public static Map<Integer, Room> loadMap(String filePath) throws ParsingLevelException {
        Map<Integer, Room> roomMap = new HashMap<>();
        int lineCount = 0;
        String line;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8))) {
            while ((line = br.readLine()) != null) {
                lineCount++;
                if (lineCount == 1 && line.startsWith(BYTE_ORDER_MARK)) {
                    line = line.substring(BYTE_ORDER_MARK.length());
                }
                if (line.isBlank()) {
                    continue;
                }
                Room room = parseRoom(line);
                if (roomMap.containsKey(room.getId())) {
                    throw new ParsingLevelException("Room id " + room.getId() + " is used by more than one room.");
                }
                roomMap.put(room.getId(), room);
            }
        } catch (FileNotFoundException e) {
            throw new ParsingLevelException("Map file not found: " + Path.of(filePath).toAbsolutePath(), e);
        } catch (IOException e) {
            throw new ParsingLevelException("Could not read " + filePath + " at line " + lineCount + ": " + e.getMessage(), e);
        } catch (ParsingLevelException | IllegalArgumentException e) {
            throw new ParsingLevelException("Invalid map formatting at line " + lineCount + ": " + e.getMessage(), e);
        }

        validateLinks(roomMap);
        validateEveryRoomCanBeEntered(roomMap);
        return roomMap;
    }

    private static Room parseRoom(String line) throws ParsingLevelException {
        String[] parts = line.split(",(?!\\s)");

        if (parts.length < Config.MANDATORY_COLUMNS) {
            throw new ParsingLevelException("Invalid format in the map file, the first " + Config.MANDATORY_COLUMNS
                    + " columns are mandatory (roomID, room name and the 4 neighboring rooms).");
        }
        if (parts.length > Column.values().length) {
            throw new ParsingLevelException("The line has " + parts.length + " columns, but a room has at most "
                    + Column.values().length + ". A comma with no space after it splits a column in two.");
        }

        int id = number(parts, Column.ID);
        String name = column(parts, Column.NAME);
        int north = number(parts, Column.NORTH);
        int east = number(parts, Column.EAST);
        int south = number(parts, Column.SOUTH);
        int west = number(parts, Column.WEST);
        String description = column(parts, Column.DESCRIPTION);
        List<Item> items = parseList(column(parts, Column.ITEMS), "item", "name:stat:value",
                fields -> new Item(fields[0], statOf(fields[1], fields[2])));
        List<Enemy> enemies = parseList(column(parts, Column.ENEMIES), "enemy", "name:health:strength:agility",
                fields -> new Enemy(fields[0], Integer.parseInt(fields[1]), Integer.parseInt(fields[2]), Integer.parseInt(fields[3])));
        String warningMessage = column(parts, Column.WARNING_MESSAGE);

        if (warningMessage.matches("[^:;]+:\\d+:\\d+:\\d+(;[^:;]+:\\d+:\\d+:\\d+)*")) {
            throw new ParsingLevelException("The warning message is an enemy definition: \""
                    + warningMessage + "\". The enemies column is empty, which means one comma"
                    + " too many before it - every column after that is shifted by one.");
        }

        List<Weapon> weapons = parseList(column(parts, Column.WEAPONS), "weapon", "name:damage:stat:value",
                fields -> new Weapon(fields[0], Integer.parseInt(fields[1]), statOf(fields[2], fields[3])));
        List<RoomCondition> conditions = parseList(column(parts, Column.CONDITIONS), "condition", "stat:value:message",
                fields -> new RoomCondition(knownStat(fields[0]), Integer.parseInt(fields[1]), fields[2]));
        boolean isWinRoom = flag(parts, Column.IS_WIN_ROOM);
        String winMessage = column(parts, Column.WIN_MESSAGE);
        boolean isSecret = flag(parts, Column.IS_SECRET);

        return new Room(id, name, description, items, enemies, warningMessage, weapons, conditions, north, east, south, west, isWinRoom, winMessage, isSecret);
    }

    private static String column(String[] parts, Column column) {
        return parts.length > column.ordinal() ? parts[column.ordinal()] : "";
    }

    private static int number(String[] parts, Column column) throws ParsingLevelException {
        String value = column(parts, column);
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new ParsingLevelException("Column " + column + " must be a whole number, not \"" + value + "\".");
        }
    }

    private static boolean flag(String[] parts, Column column) throws ParsingLevelException {
        String value = column(parts, column);
        String flag = value.trim().toLowerCase();
        if (flag.isEmpty() || flag.equals("false")) {
            return false;
        }
        if (flag.equals("true")) {
            return true;
        }
        throw new ParsingLevelException("Column " + column + " must be true or false, not \"" + value + "\".");
    }

    private static String knownStat(String stat) {
        if (!Config.STATS.contains(stat)) {
            throw new IllegalArgumentException("Unknown stat \"" + stat + "\", use one of " + Config.STATS + ".");
        }
        return stat;
    }

    private static Map<String, Integer> statOf(String stat, String value) {
        return Map.of(knownStat(stat), Integer.parseInt(value));
    }

    private static <T> List<T> parseList(String column, String kind, String format, Function<String[], T> factory) throws ParsingLevelException {
        List<T> entries = new ArrayList<>();
        if (column.isEmpty()) {
            return entries;
        }

        int fieldCount = format.split(":").length;
        for (String entry : column.split(";")) {
            String[] fields = Arrays.stream(entry.split(":", fieldCount)).map(String::trim).toArray(String[]::new);
            String problem = "Invalid " + kind + " \"" + entry + "\", expected " + format + ".";
            if (fields.length < fieldCount || Arrays.stream(fields).anyMatch(String::isBlank)) {
                throw new ParsingLevelException(problem);
            }
            try {
                entries.add(factory.apply(fields));
            } catch (IllegalArgumentException e) {
                throw new ParsingLevelException(problem + " " + e.getMessage(), e);
            }
        }
        return entries;
    }

    private static void validateLinks(Map<Integer, Room> roomMap) throws ParsingLevelException {
        if (!roomMap.containsKey(Config.START_ROOM_ID)) {
            throw new ParsingLevelException("The map has no room " + Config.START_ROOM_ID + ", which is where the game starts.");
        }
        if (!roomMap.get(Config.START_ROOM_ID).getAvailableEnemies().isEmpty()) {
            throw new ParsingLevelException("Room " + Config.START_ROOM_ID
                    + " has enemies, but the start room has no room to retreat to.");
        }
        for (Room room : roomMap.values()) {
            for (Direction direction : Direction.values()) {
                int exit = room.getExit(direction);
                if (exit != Config.WALL && !roomMap.containsKey(exit)) {
                    throw new ParsingLevelException("Room " + room.getId() + " has an exit to room " + exit
                            + ", which does not exist. Use " + Config.WALL + " for a wall.");
                }
            }
        }
        if (roomMap.values().stream().noneMatch(Room::isWinRoom)) {
            throw new ParsingLevelException("The map has no win room, so the game cannot be won.");
        }
        if (reachableFromStart(roomMap).stream().noneMatch(id -> roomMap.get(id).isWinRoom())) {
            throw new ParsingLevelException("No path of exits leads from room " + Config.START_ROOM_ID
                    + " to a win room, so the game cannot be won.");
        }
    }

    private static Set<Integer> reachableFromStart(Map<Integer, Room> roomMap) {
        Set<Integer> reached = new HashSet<>(List.of(Config.START_ROOM_ID));
        Deque<Integer> pending = new ArrayDeque<>(reached);
        while (!pending.isEmpty()) {
            Room room = roomMap.get(pending.poll());
            for (Direction direction : Direction.values()) {
                int exit = room.getExit(direction);
                if (exit != Config.WALL && reached.add(exit)) {
                    pending.add(exit);
                }
            }
        }
        return reached;
    }

    private static void validateEveryRoomCanBeEntered(Map<Integer, Room> roomMap) throws ParsingLevelException {
        Set<Integer> entered = new HashSet<>(List.of(Config.START_ROOM_ID));
        boolean progress = true;
        while (progress) {
            progress = false;
            List<Item> items = itemsIn(roomMap, entered);
            for (Room room : frontier(roomMap, entered)) {
                if (whyBlocked(room, items).isEmpty()) {
                    entered.add(room.getId());
                    progress = true;
                }
            }
        }
        Optional<Room> blocked = frontier(roomMap, entered).stream().findFirst();
        if (blocked.isPresent()) {
            throw new ParsingLevelException(whyBlocked(blocked.get(), itemsIn(roomMap, entered)).orElseThrow());
        }
    }

    private static List<Room> frontier(Map<Integer, Room> roomMap, Set<Integer> entered) {
        return entered.stream()
                .map(roomMap::get)
                .flatMap(room -> Arrays.stream(Direction.values()).map(room::getExit))
                .filter(exit -> exit != Config.WALL && !entered.contains(exit))
                .distinct()
                .sorted()
                .map(roomMap::get)
                .toList();
    }

    private static List<Item> itemsIn(Map<Integer, Room> roomMap, Set<Integer> roomIds) {
        return roomIds.stream().flatMap(id -> roomMap.get(id).getAvailableItems().stream()).toList();
    }

    private static Optional<String> whyBlocked(Room room, List<Item> items) {
        String reachable = "the items the player can reach before it raise ";
        for (RoomCondition condition : room.getConditions()) {
            int highest = highestPossible(items, condition.getStat());
            if (condition.getMinValue() > highest) {
                return Optional.of("Room " + room.getId() + " needs " + condition.getStat() + " " + condition.getMinValue()
                        + ", but " + reachable + "it to at most " + highest + ".");
            }
        }
        List<Weapon> weapons = items.stream().filter(Weapon.class::isInstance).map(Weapon.class::cast).toList();
        int damage = Combat.damage(highestPossible(items, Config.STRENGTH), weapons);
        int health = highestPossible(items, Config.HEALTH);
        for (Enemy enemy : room.getAvailableEnemies()) {
            long damageTaken = enemy.damageBeforeFalling(damage, highestPossible(items, Config.AGILITY));
            if (damageTaken >= health) {
                return Optional.of("Room " + room.getId() + " cannot be won: " + enemy.getName() + " deals " + damageTaken
                        + " damage before it falls, but " + reachable + "health to at most " + health + ".");
            }
        }
        return Optional.empty();
    }

    private static int highestPossible(List<Item> items, String stat) {
        return Config.STARTING_STAT_VALUE + items.stream().mapToInt(item -> Math.max(0, item.getStats().getOrDefault(stat, 0))).sum();
    }
}
