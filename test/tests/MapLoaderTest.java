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
        assertEquals("[Potion {health=30}, Sword {strength=5, damage 10}]", rooms.get(1).getAvailableItems().toString());
        assertEquals("The door is locked.", rooms.get(1).getConditions().get(0).getMessage());
        assertEquals("Goblin", rooms.get(2).getAvailableEnemies().get(0).getName());
        assertTrue(rooms.get(5).isWinRoom());
        assertTrue(rooms.get(9).isSecret());
    }

    @Test
    void aDescriptionMayContainACommaFollowedByASpace() throws Exception {
        Map<Integer, Room> rooms = load("0,Start,-1,-1,-1,-1,A nice room, full of nice things.,,,,,,true");

        assertEquals("A nice room, full of nice things.", rooms.get(0).getDescription());
    }

    @Test
    void severalEnemiesCanShareARoom() throws Exception {
        Map<Integer, Room> rooms = load("0,Start,-1,-1,-1,-1,Rats.,,Rat:5:3:25;Rat:5:3:25;Fat Rat:20:10:0,,,,true");

        assertEquals(3, rooms.get(0).getAvailableEnemies().size());
    }

    @Test
    void aMapSavedWithAByteOrderMarkStillLoads() throws Exception {
        Map<Integer, Room> rooms = load("﻿0,Start,-1,-1,-1,-1,The start.,,,,,,true");

        assertEquals("Start", rooms.get(0).getName());
    }

    @Test
    void aMissingMapIsRefused() {
        assertThrows(ParsingLevelException.class, () -> MapLoader.loadMap("no-such-map.csv"));
    }

    @Test
    void aRoomIdThatIsNotANumberIsRefused() {
        assertRefused("line 2: Column id must be a whole number, not \"x1\"",
                "0,Start,-1,1,-1,-1,The start.,,,,,,true",
                "x1,Hall,-1,-1,-1,0,A hall.");
    }

    @Test
    void blankLinesAreSkipped() throws Exception {
        Map<Integer, Room> rooms = load("0,Start,-1,-1,-1,-1,The start.,,,,,,true", "", "  ");

        assertEquals(1, rooms.size());
    }

    @Test
    void aMisspelledTrueOrFalseIsRefused() {
        assertRefused("Column isSecret must be true or false, not \"ture\"",
                "0,Start,-1,-1,-1,-1,The start.,,,,,,true,You win!,ture");
    }

    @Test
    void anUnknownStatIsRefused() {
        assertRefused("Unknown stat \"Strength\"", "0,Start,-1,-1,-1,-1,The start.,Rock:Strength:5,,,,,true");
    }

    @Test
    void aWinRoomNoExitLeadsToIsRefused() {
        assertRefused("No path of exits leads from room 0 to a win room",
                "0,Start,-1,-1,-1,-1,The start.",
                "1,Exit,-1,-1,-1,0,The way out.,,,,,,true");
    }

    @Test
    void aConditionNoItemsCanMeetIsRefused() {
        assertRefused("Room 1 needs strength 30, but the items on the map raise it to at most 15",
                "0,Start,-1,1,-1,-1,The start.,Rock:strength:5",
                "1,Exit,-1,-1,-1,0,The way out.,,,,,strength:30:Too heavy.,true");
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
    void aMapWithoutAWinRoomIsRefused() {
        assertRefused("no win room", "0,Start,-1,-1,-1,-1,The start.");
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
