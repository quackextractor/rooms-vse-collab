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

public class MapLoader {

    public static Map<Integer, Room> loadMap(String filePath) throws IOException, NumberFormatException, ArrayIndexOutOfBoundsException, ParsingLevelException {
        Map<Integer, Room> roomMap = new HashMap<>();
        int lineCount = 0;
        String line;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            while ((line = br.readLine()) != null) {
                lineCount++;
                // regex makes sure it won't slit in between description sentences
                String[] parts = line.split(",(?!\\s)");

                if (parts.length < 6) {
                   throw new IOException("Invalid format in the map file, first 6 parameters are mandatory (roomID, room name and the 4 neighboring rooms).");
               }

                int id = Integer.parseInt(parts[0]);
                String name = parts[1];
                int north = Integer.parseInt(parts[2]);
                int east = Integer.parseInt(parts[3]);
                int south = Integer.parseInt(parts[4]);
                int west = Integer.parseInt(parts[5]);
                String description = (parts.length > 6) ? parts[6] : "";
                List<Item> items = parseItems(parts.length > 7 ? parts[7] : "");
                List<Enemy> enemies = parseEnemies(parts.length > 8 ? parts[8] : "");           // add to csv file
                String warningMessage = (parts.length > 9 ? parts[9] : "");                     // add to csv file
                List<Weapon> weapons = parseWeapons(parts.length > 10 ? parts[10] : "");
                List<RoomCondition> conditions = parseConditions(parts.length > 11 ? parts[11] : "");
                boolean isWinRoom = parts.length > 12 && Boolean.parseBoolean(parts[12]);       // add to csv file
                String winMessage = (parts.length > 13) ? parts[13] : "";                       // add to csv file

                Room room = new Room(id, name, description, items, enemies, warningMessage, weapons, conditions, north, east, south, west, isWinRoom, winMessage);
                roomMap.put(id, room);
            }
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("File not found: " + filePath);
        } catch (IOException e) {
            throw new IOException("Invalid map formatting at line " + lineCount + ": " + e.getMessage());
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Invalid map formatting at line " + lineCount + ": " + e.getMessage());
        } catch (Exception e){
          Logic.printToUser("Invalid map formatting at line " + lineCount);
         Logic.printToUser(e.getMessage());
         throw new ParsingLevelException("Failed to load level.");
        }

        return roomMap;
    }

    private static List<Enemy> parseEnemies(String enemiesStr) throws NumberFormatException, ArrayIndexOutOfBoundsException {
        List<Enemy> enemies = new ArrayList<>();
        try {
            if (!enemiesStr.isEmpty()) {
                String[] enemyArray = enemiesStr.split(";"); // Enemies are separated by semicolon
                for (String enemy : enemyArray) {
                    String[] enemyInfo = enemy.split(":"); // Enemy information is separated by colon

                    enemies.add(new Enemy(enemyInfo[0], Integer.parseInt(enemyInfo[1]), Integer.parseInt(enemyInfo[2]), Integer.parseInt(enemyInfo[3])));

                }
            }
        }catch(Exception e){
            Logic.printToUser("An Error occurred while parsing enemies: \n" + enemiesStr);
            throw e;
        }
        return enemies;
    }

    private static List<RoomCondition> parseConditions(String conditionsStr) throws NumberFormatException, ArrayIndexOutOfBoundsException {
        List<RoomCondition> conditions = new ArrayList<>();
        try {
        if (!conditionsStr.isEmpty()) {
            String[] conditionArray = conditionsStr.split(";"); // Conditions are separated by semicolon
            for (String condition : conditionArray) {
                String[] conditionInfo = condition.split(":"); // Condition information is separated by colon

                    conditions.add(new RoomCondition(
                            conditionInfo[0],
                            Integer.parseInt(conditionInfo[1]),
                            conditionInfo[2]
                    ));

            }
        }
    }catch(Exception e){
        Logic.printToUser("An Error occurred while parsing room conditions: \n" + conditionsStr);
        throw e;
    }
        return conditions;
    }
    private static List<Item> parseItems(String itemsStr) throws NumberFormatException, ArrayIndexOutOfBoundsException {
        List<Item> items = new ArrayList<>();
        try {
        if (!itemsStr.isEmpty()) {
            String[] itemArray = itemsStr.split(";"); // Items are separated by semicolon
            for (String item : itemArray) {
                String[] itemInfo = item.split(":"); // Item information is separated by colon


                    HashMap<String, Integer> parsedStats = new HashMap<>();
                    parsedStats.put(itemInfo[1], Integer.valueOf(itemInfo[2]));
                    items.add(new Item(itemInfo[0], parsedStats));

            }
        }
    }catch(Exception e){
        Logic.printToUser("An Error occurred while parsing items: \n" + itemsStr);
        throw e;
    }
        return items;
    }
    private static List<Weapon> parseWeapons(String weaponsStr) throws NumberFormatException, ArrayIndexOutOfBoundsException {

            List<Weapon> weapons = new ArrayList<>();
        try {
            if (!weaponsStr.isEmpty()) {
                String[] weaponArray = weaponsStr.split(";"); // Weapons are separated by semicolon
                for (String weapon : weaponArray) {
                    String[] weaponInfo = weapon.split(":"); // Weapon information is separated by colon

                    HashMap<String, Integer> parsedStats = new HashMap<>();
                    // stats
                    parsedStats.put(weaponInfo[2], Integer.valueOf(weaponInfo[3]));
                    weapons.add(new Weapon(weaponInfo[0], Integer.parseInt(weaponInfo[1]), parsedStats));

                }
            }
        }catch(Exception e){
            Logic.printToUser("An Error occurred while parsing weapons: \n" + weaponsStr);
            throw e;
        }
        return weapons;
    }
}