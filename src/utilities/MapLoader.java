package utilities;

import enemy.Enemy;
import items.Item;
import items.Weapon;
import myExceptions.ParsingLevelException;
import room.Room;
import room.RoomCondition;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class MapLoader {
    private static final String BYTE_ORDER_MARK = "﻿";

    public static Map<Integer, Room> loadMap(String filePath) throws ParsingLevelException {
        Map<Integer, Room> roomMap = new HashMap<>();
        int lineCount = 0;
        String line;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            while ((line = br.readLine()) != null) {
                lineCount++;
                if (lineCount == 1 && line.startsWith(BYTE_ORDER_MARK)) {
                    line = line.substring(BYTE_ORDER_MARK.length());
                }
                Room room = parseRoom(line);
                if (roomMap.containsKey(room.getId())) {
                    throw new ParsingLevelException("Room id " + room.getId() + " is used by more than one room.");
                }
                roomMap.put(room.getId(), room);
            }
        } catch (FileNotFoundException e) {
            throw new ParsingLevelException("File not found: " + filePath, e);
        } catch (IOException e) {
            throw new ParsingLevelException("Could not read " + filePath + " at line " + lineCount + ": " + e.getMessage(), e);
        } catch (ParsingLevelException | IllegalArgumentException e) {
            throw new ParsingLevelException("Invalid map formatting at line " + lineCount + ": " + e.getMessage(), e);
        }

        validateLinks(roomMap);
        return roomMap;
    }

    private static Room parseRoom(String line) throws ParsingLevelException {
        String[] parts = line.split(",(?!\\s)");

        if (parts.length < 6) {
            throw new ParsingLevelException("Invalid format in the map file, first 6 parameters are mandatory (roomID, room name and the 4 neighboring rooms).");
        }

        int id = Integer.parseInt(parts[0]);
        String name = parts[1];
        int north = Integer.parseInt(parts[2]);
        int east = Integer.parseInt(parts[3]);
        int south = Integer.parseInt(parts[4]);
        int west = Integer.parseInt(parts[5]);
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
                fields -> new RoomCondition(fields[0], Integer.parseInt(fields[1]), fields[2]));
        boolean isWinRoom = Boolean.parseBoolean(column(parts, 12).trim());
        String winMessage = column(parts, 13);
        boolean isSecret = Boolean.parseBoolean(column(parts, 14).trim());

        return new Room(id, name, description, items, enemies, warningMessage, weapons, conditions, north, east, south, west, isWinRoom, winMessage, isSecret);
    }

    private static String column(String[] parts, int index) {
        return parts.length > index ? parts[index] : "";
    }

    private static Map<String, Integer> statOf(String stat, String value) {
        return Map.of(stat, Integer.parseInt(value));
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
        if (!roomMap.containsKey(0)) {
            throw new ParsingLevelException("The map has no room 0, which is where the game starts.");
        }
        for (Room room : roomMap.values()) {
            for (int exit : new int[]{room.getNorth(), room.getEast(), room.getSouth(), room.getWest()}) {
                if (exit != -1 && !roomMap.containsKey(exit)) {
                    throw new ParsingLevelException("Room " + room.getId() + " has an exit to room " + exit
                            + ", which does not exist. Use -1 for a wall.");
                }
            }
        }
    }
}
