package tests;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MovementTest {

    @Test
    void thePlayerStartsInTheStartRoom() throws Exception {
        GameSession game = new GameSession();

        assertEquals("Start", game.currentRoom());
    }

    @Test
    void walkingIntoAWallKeepsThePlayerWhereTheyAre() throws Exception {
        GameSession game = new GameSession();

        game.play("S");

        assertEquals("Start", game.currentRoom());
        assertTrue(game.printed("You cannot move in that direction from Start"));
    }

    @Test
    void aLockedRoomCannotBeEnteredWithoutEnoughWisdom() throws Exception {
        GameSession game = new GameSession();

        game.play("E");

        assertEquals("Start", game.currentRoom());
        assertTrue(game.printed("Cannot enter room: The door is locked."));
    }

    @Test
    void readingTheBookOpensTheLockedRoom() throws Exception {
        GameSession game = new GameSession();

        game.play("P", "Book", "E");

        assertEquals("Library", game.currentRoom());
    }

    @Test
    void aRoomOnceEnteredStaysOpenAfterDroppingTheItemThatOpenedIt() throws Exception {
        GameSession game = new GameSession();

        game.play("P", "Book", "E", "D", "Book", "W", "E");

        assertEquals("Library", game.currentRoom());
    }

    @Test
    void walkingBackReturnsToThePreviousRoom() throws Exception {
        GameSession game = new GameSession();

        game.play("P", "Book", "E", "W");

        assertEquals("Start", game.currentRoom());
    }

    @Test
    void theMapMarksTheRoomThePlayerStandsIn() throws Exception {
        GameSession game = new GameSession();

        game.play("P", "Book", "E");

        assertTrue(game.printed("[   0][*  1]"));
    }

    @Test
    void theMapShowsAnExitIntoARoomWithNoWayBack() throws Exception {
        GameSession game = new GameSession();

        game.play("P", "Book", "E");

        assertTrue(game.printed("[   0][*  1][   ?]"));
    }

    @Test
    void aSecretRoomStaysOffTheMapUntilThePlayerEntersIt() throws Exception {
        GameSession game = new GameSession();

        game.play("M");
        assertTrue(game.printed("Discovered map (? unexplored exit, * you are here):", "[*  0][   ?]"));

        game.play("N");
        assertEquals("Secret Room", game.currentRoom());
        assertTrue(game.printed("[*  9]"));
    }

    @Test
    void theExitNeedsStrength30() throws Exception {
        GameSession game = new GameSession(1);

        game.play("E");
        assertEquals("Library", game.currentRoom());
        assertTrue(game.printed("Cannot enter room: The gate is too heavy."));

        game.player().setStatValue("strength", 30);
        game.play("E");
        assertEquals("Exit", game.currentRoom());
    }

    @Test
    void anUnknownCommandIsRejected() throws Exception {
        GameSession game = new GameSession();

        game.play("X");

        assertEquals("Start", game.currentRoom());
        assertTrue(game.printed("Invalid input."));
    }

    @Test
    void theHelpListsEveryCommand() throws Exception {
        GameSession game = new GameSession();

        game.play("H");

        for (String key : new String[]{"I", "P", "D", "L", "M", "U", "Q"}) {
            assertTrue(game.printed("- " + key + " to "), "the help is missing " + key);
        }
    }
}
