package tests;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WalkthroughTest {

    @Test
    void thePlayerCanWinTheGame() throws Exception {
        GameSession game = new GameSession();

        game.play("P", "Book", "E");
        assertEquals("Library", game.currentRoom());
        assertEquals(20, game.stat("wisdom"));

        game.play("P", "Sword", "P", "Potion");
        assertEquals(15, game.stat("strength"));
        assertEquals(40, game.stat("health"));

        game.play("E");
        assertEquals("Library", game.currentRoom(), "the exit needs more strength than the sword gives");

        game.play("N", "Y");
        assertEquals("Arena", game.currentRoom());
        assertTrue(game.printed("You have successfully defeated Goblin!"));

        game.play("P", "Gauntlets");
        assertEquals(30, game.stat("strength"));

        game.play("S", "E");
        assertEquals("Exit", game.currentRoom());
        assertTrue(game.player().getCurrentRoom().isWinRoom());
    }
}
