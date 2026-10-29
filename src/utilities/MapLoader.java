package utilities;

import enemy.Enemy;
import items.Item;
import items.Weapon;
import myExceptions.ParsingLevelException;
import room.Direction;
import room.Room;
import room.RoomCondition;

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
        validateConditions(roomMap);
        return roomMap;
    }

    private static Room parseRoom(String line) throws ParsingLevelException {
        String[] parts = line.split(",(?!\\s)");

        if (parts.length < 6) {
            throw new ParsingLevelException("Invalid format in the map file, first 6 parameters are mandatory (roomID, room name and the 4 neighboring rooms).");
        }

        int id = number(parts[0], "id");
        String name = parts[1];
        int north = number(parts[2], "north");
        int east = number(parts[3], "east");
        int south = number(parts[4], "south");
        int west = number(parts[5], "west");
        String description = column(parts, 6);
        List<Item> items = parseList(column(parts, 7), "item", "name:stat:value",
                fields -> new Item(fields[0], statOf(fields[1], fields[2])));
        List<Enemy> enemies = parseList(column(parts, 8), "enemy", "name:health:strength:agility",
                fields -> new Enemy(fields[0], Integer.parseInt(fields[1]), Integer.parseInt(fields[2]), Integer.parseInt(fields[3])));
        String warningMessage = column(parts, 9);

        if (warningMessage.matches("[^:;]+:\\d+:\\d+:\\d+(;[^:;]+:\\d+:\\d+:\\d+)*")) {
            throw new ParsingLevelException("The warning message is an enemy definition: \""
                    + warningMessage + "\". The enemies column is empty, which means one comma"
                    + " too many before it - every column after that is shifted by one.");
        }

        List<Weapon> weapons = parseList(column(parts, 10), "weapon", "name:damage:stat:value",
                fields -> new Weapon(fields[0], Integer.parseInt(fields[1]), statOf(fields[2], fields[3])));
        List<RoomCondition> conditions = parseList(column(parts, 11), "condition", "stat:value:message",
                fields -> new RoomCondition(knownStat(fields[0]), Integer.parseInt(fields[1]), fields[2]));
        boolean isWinRoom = flag(column(parts, 12), "isWinRoom");
        String winMessage = column(parts, 13);
        boolean isSecret = flag(column(parts, 14), "isSecret");

        return new Room(id, name, description, items, enemies, warningMessage, weapons, conditions, north, east, south, west, isWinRoom, winMessage, isSecret);
    }

    private static String column(String[] parts, int index) {
        return parts.length > index ? parts[index] : "";
    }

    private static int number(String value, String columnName) throws ParsingLevelException {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new ParsingLevelException("Column " + columnName + " must be a whole number, not \"" + value + "\".");
        }
    }

    private static boolean flag(String value, String columnName) throws ParsingLevelException {
        String flag = value.trim().toLowerCase();
        if (flag.isEmpty() || flag.equals("false")) {
            return false;
        }
        if (flag.equals("true")) {
            return true;
        }
        throw new ParsingLevelException("Column " + columnName + " must be true or false, not \"" + value + "\".");
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
            String[] fields = entry.split(":", fieldCount);
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

    private static void validateConditions(Map<Integer, Room> roomMap) throws ParsingLevelException {
        for (Room room : roomMap.values()) {
            for (RoomCondition condition : room.getConditions()) {
                int highest = highestPossible(roomMap, condition.getStat());
                if (condition.getMinValue() > highest) {
                    throw new ParsingLevelException("Room " + room.getId() + " needs " + condition.getStat() + " "
                            + condition.getMinValue() + ", but the items on the map raise it to at most " + highest + ".");
                }
            }
        }
    }

    private static int highestPossible(Map<Integer, Room> roomMap, String stat) {
        int highest = Config.STARTING_STAT_VALUE;
        for (Room room : roomMap.values()) {
            for (Item item : room.getAvailableItems()) {
                highest += Math.max(0, item.getStats().getOrDefault(stat, 0));
            }
        }
        return highest;
    }
}
