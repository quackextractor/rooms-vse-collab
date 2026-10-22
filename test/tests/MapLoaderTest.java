package tests;

import myExceptions.ParsingLevelException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import room.Room;
import utilities.MapLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MapLoaderTest {

    @TempDir
    Path folder;

    private String theRealMap() throws Exception {
        return Files.readString(Path.of("rooms.csv"));
    }

    private Map<Integer, Room> load(String mapText) throws Exception {
        Path map = folder.resolve("rooms.csv");
        Files.writeString(map, mapText);
        return MapLoader.loadMap(map.toString());
    }

    private void assertRefused(String mapText, String reason) {
        ParsingLevelException refused = assertThrows(ParsingLevelException.class, () -> load(mapText));
        assertTrue(refused.getMessage().contains(reason), refused.getMessage());
    }

    @Test
    void theMapLoadsEveryRoom() throws Exception {
        Map<Integer, Room> rooms = MapLoader.loadMap("rooms.csv");

        assertEquals(13, rooms.size());
        assertEquals("Castle Harbour", rooms.get(0).getName());
        assertTrue(rooms.get(11).isWinRoom());
        assertTrue(rooms.get(101).isSecret());
    }

    @Test
    void everyRoomKeepsItsEnemies() throws Exception {
        Map<Integer, Room> rooms = MapLoader.loadMap("rooms.csv");

        assertEquals("Ghost", rooms.get(4).getEnemies().get(0).getName());
        assertEquals("Horseless Headless Knight", rooms.get(6).getEnemies().get(0).getName());
        assertEquals(10, rooms.get(9).getEnemies().size());
    }

    @Test
    void aMapSavedWithAByteOrderMarkStillLoads() throws Exception {
        Map<Integer, Room> rooms = load("﻿" + theRealMap());

        assertEquals("Castle Harbour", rooms.get(0).getName());
    }

    @Test
    void aMissingMapIsRefused() {
        assertThrows(ParsingLevelException.class, () -> MapLoader.loadMap(folder.resolve("missing.csv").toString()));
    }

    @Test
    void aRoomIdThatIsNotANumberIsRefused() throws Exception {
        assertRefused(theRealMap().replace("2,L1,", "x2,L1,"), "Invalid map formatting at line 3");
    }

    @Test
    void aMapWithoutTheStartingRoomIsRefused() throws Exception {
        String map = theRealMap();

        assertRefused(map.substring(map.indexOf('\n') + 1), "no room 0");
    }

    @Test
    void anExitToARoomThatDoesNotExistIsRefused() throws Exception {
        assertRefused(theRealMap().replace("0,Castle Harbour,-1,1,", "0,Castle Harbour,-1,99,"), "exit to room 99");
    }

    @Test
    void twoRoomsWithTheSameIdAreRefused() throws Exception {
        assertRefused(theRealMap().strip() + "\n1,Fake,-1,-1,-1,-1,A duplicate.", "Room id 1 is used by more than one room");
    }

    @Test
    void anItemWithoutAStatIsRefused() throws Exception {
        assertRefused(theRealMap().replace("Magical Flower:wisdom:10", "Magical Flower::10"), "Invalid item \"Magical Flower::10\"");
    }

    @Test
    void anEnemyWithNoHealthIsRefused() throws Exception {
        assertRefused(theRealMap().replace("Gargoyle:60:8:6", "Gargoyle:0:8:6"), "Invalid enemy \"Gargoyle:0:8:6\"");
    }

    @Test
    void anEnemyInTheWarningColumnIsRefused() throws Exception {
        assertRefused(theRealMap().replace("Map:wisdom:5,Ghost:40:6:7", "Map:wisdom:5,,Ghost:40:6:7"), "warning message is an enemy definition");
    }
}
