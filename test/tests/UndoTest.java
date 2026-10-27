package tests;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UndoTest {

    @Test
    void undoingAMoveGoesBackEvenThroughALockedDoor() throws Exception {
        GameSession game = new GameSession();
        game.play("P", "Book", "E", "W");
        game.player().setStatValue("wisdom", 10);

        game.play("U");

        assertEquals("Library", game.currentRoom());
    }

    @Test
    void undoSkipsCommandsThatChangedNothing() throws Exception {
        GameSession game = new GameSession();

        game.play("P", "Book", "S", "U");

        assertFalse(game.carries("Book"));
        assertEquals(1, game.room(0).getAvailableItems().size());
        assertEquals(10, game.stat("wisdom"));
    }

    @Test
    void undoingADiscardRestoresTheStatsExactly() throws Exception {
        GameSession game = new GameSession(1);
        game.play("P", "Potion");
        game.player().setStatValue("health", 10);

        game.play("D", "Potion", "U");

        assertTrue(game.carries("Potion"));
        assertEquals(10, game.stat("health"));
    }

    @Test
    void aFightCannotBeUndone() throws Exception {
        GameSession game = new GameSession();
        game.play("P", "Book", "E");

        game.play("N", "N", "U");

        assertEquals("Library", game.currentRoom());
        assertTrue(game.printed("Nothing to undo."));
    }
}
