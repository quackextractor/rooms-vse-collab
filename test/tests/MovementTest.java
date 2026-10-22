package tests;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MovementTest {

    @Test
    void thePlayerStartsInTheCastleHarbour() throws Exception {
        GameSession game = new GameSession();

        assertEquals("Castle Harbour", game.currentRoom());
    }

    @Test
    void walkingIntoAWallKeepsThePlayerWhereTheyAre() throws Exception {
        GameSession game = new GameSession();

        game.play("N");

        assertEquals("Castle Harbour", game.currentRoom());
        assertTrue(game.printed("You cannot move in that direction from Castle Harbour"));
    }

    @Test
    void theLobbyIsLockedWithoutEnoughWisdom() throws Exception {
        GameSession game = new GameSession();

        game.play("E");

        assertEquals("Castle Harbour", game.currentRoom());
        assertTrue(game.printed("Cannot enter room: You can't figure out how to get into the castle."));
    }

    @Test
    void theMagicalFlowerOpensTheLobby() throws Exception {
        GameSession game = new GameSession();

        game.play("P", "Magical Flower", "E");

        assertEquals("Castle Lobby", game.currentRoom());
    }

    @Test
    void walkingBackReturnsToThePreviousRoom() throws Exception {
        GameSession game = new GameSession();

        game.play("P", "Magical Flower", "E", "W");

        assertEquals("Castle Harbour", game.currentRoom());
    }

    @Test
    void theMapMarksTheRoomThePlayerStandsIn() throws Exception {
        GameSession game = new GameSession();

        game.play("P", "Magical Flower", "E");

        assertTrue(game.printed("[*  1]"));
    }

    @Test
    void theSecretChamberNeedsWisdom30() throws Exception {
        GameSession game = new GameSession(5);
        game.room(5).getAvailableEnemies().clear();

        game.play("W");

        assertEquals("Crossroads", game.currentRoom());
        assertTrue(game.printed("Cannot enter room: You must be smart to uncover the secret."));
    }

    @Test
    void theSecretChamberStaysOffTheMapUntilThePlayerEntersIt() throws Exception {
        GameSession game = new GameSession(5);
        game.room(5).getAvailableEnemies().clear();

        game.play("M");
        assertFalse(game.printed("101"));

        game.player().setStatValue("wisdom", 30);
        game.play("W");

        assertEquals("Secret Chamber", game.currentRoom());
        assertTrue(game.printed("[*101]"));
    }

    @Test
    void theCastleGatesNeedStrength30() throws Exception {
        GameSession game = new GameSession(7);
        game.room(7).getAvailableEnemies().clear();

        game.play("E");
        assertEquals("Large Flooded Room", game.currentRoom());
        assertTrue(game.printed("Cannot enter room: You need great strength to open the gates"));

        game.player().setStatValue("strength", 30);
        game.play("E");
        assertEquals("Villedor", game.currentRoom());
    }

    @Test
    void anUnknownCommandIsRejected() throws Exception {
        GameSession game = new GameSession();

        game.play("X");

        assertEquals("Castle Harbour", game.currentRoom());
        assertTrue(game.printed("Invalid input."));
    }

    @Test
    void theHelpListsEveryCommand() throws Exception {
        GameSession game = new GameSession();

        game.play("H");

        for (String key : new String[]{"I", "P", "D", "L", "M", "Q"}) {
            assertTrue(game.printed("- " + key + " to "), "the help is missing " + key);
        }
    }
}
