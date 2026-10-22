package tests;

import myExceptions.ParsingLevelException;
import org.junit.jupiter.api.Test;
import room.Room;
import utilities.MapLoader;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MapLoaderTest {

    private Map<Integer, Room> load(String... rooms) throws Exception {
        return MapLoader.loadMap(TestMap.write(List.of(rooms)));
    }

    private void assertRefused(String reason, String... rooms) {
        ParsingLevelException refused = assertThrows(ParsingLevelException.class, () -> load(rooms));
        assertTrue(refused.getMessage().contains(reason), refused.getMessage());
    }

    @Test
    void theGamesOwnMapLoads() throws Exception {
        Map<Integer, Room> rooms = MapLoader.loadMap("rooms.csv");

        assertTrue(rooms.containsKey(0));
    }

    @Test
    void everyRoomIsLoadedWithItsContents() throws Exception {
        Map<Integer, Room> rooms = MapLoader.loadMap(TestMap.write(TestMap.ROOMS));

        assertEquals(5, rooms.size());
        assertEquals("Library", rooms.get(1).getName());
        assertEquals("Sword", rooms.get(1).getWeapons().get(0).getName());
        assertEquals("The door is locked.", rooms.get(1).getConditions().get(0).getMessage());
        assertEquals("Goblin", rooms.get(2).getEnemies().get(0).getName());
        assertTrue(rooms.get(5).isWinRoom());
        assertTrue(rooms.get(9).isSecret());
    }

    @Test
    void aDescriptionMayContainACommaFollowedByASpace() throws Exception {
        Map<Integer, Room> rooms = load("0,Start,-1,-1,-1,-1,A nice room, full of nice things.");

        assertEquals("A nice room, full of nice things.", rooms.get(0).getDescription());
    }

    @Test
    void severalEnemiesCanShareARoom() throws Exception {
        Map<Integer, Room> rooms = load("0,Start,-1,-1,-1,-1,Rats.,,Rat:5:3:25;Rat:5:3:25;Fat Rat:20:10:0");

        assertEquals(3, rooms.get(0).getEnemies().size());
    }

    @Test
    void aMapSavedWithAByteOrderMarkStillLoads() throws Exception {
        Map<Integer, Room> rooms = load("﻿0,Start,-1,-1,-1,-1,The start.");

        assertEquals("Start", rooms.get(0).getName());
    }

    @Test
    void aMissingMapIsRefused() {
        assertThrows(ParsingLevelException.class, () -> MapLoader.loadMap("no-such-map.csv"));
    }

    @Test
    void aRoomIdThatIsNotANumberIsRefused() {
        assertRefused("Invalid map formatting at line 2",
                "0,Start,-1,1,-1,-1,The start.",
                "x1,Hall,-1,-1,-1,0,A hall.");
    }

    @Test
    void aMapWithoutTheStartingRoomIsRefused() {
        assertRefused("no room 0", "1,Hall,-1,-1,-1,-1,A hall.");
    }

    @Test
    void anExitToARoomThatDoesNotExistIsRefused() {
        assertRefused("exit to room 99", "0,Start,-1,99,-1,-1,The start.");
    }

    @Test
    void twoRoomsWithTheSameIdAreRefused() {
        assertRefused("Room id 0 is used by more than one room",
                "0,Start,-1,-1,-1,-1,The start.",
                "0,Copy,-1,-1,-1,-1,A copy.");
    }

    @Test
    void anItemWithoutAStatIsRefused() {
        assertRefused("Invalid item \"Book::10\"", "0,Start,-1,-1,-1,-1,The start.,Book::10");
    }

    @Test
    void anEnemyWithNoHealthIsRefused() {
        assertRefused("Invalid enemy \"Goblin:0:6:5\"", "0,Start,-1,-1,-1,-1,The start.,,Goblin:0:6:5");
    }

    @Test
    void anEnemyInTheWarningColumnIsRefused() {
        assertRefused("warning message is an enemy definition", "0,Start,-1,-1,-1,-1,The start.,,,Goblin:30:6:5");
    }
}
