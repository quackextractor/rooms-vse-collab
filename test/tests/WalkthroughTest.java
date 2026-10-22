package tests;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WalkthroughTest {

    @Test
    void thePlayerCanWinTheGame() throws Exception {
        GameSession game = new GameSession();

        game.play("P", "Magical Flower", "E");
        assertEquals("Castle Lobby", game.currentRoom());

        game.play("P", "Shield", "P", "Sword", "N", "P", "Scroll", "P", "Potion");
        assertEquals("L1", game.currentRoom());
        assertEquals(45, game.stat("health"));
        assertEquals(22, game.stat("strength"));

        game.play("E", "Y");
        assertEquals("L2", game.currentRoom());
        assertTrue(game.printed("You have successfully defeated Gargoyle!"));

        game.play("E", "Y", "P", "Axe", "P", "Map");
        assertEquals("L3", game.currentRoom());
        assertTrue(game.printed("You have successfully defeated Ghost!"));
        assertEquals(30, game.stat("wisdom"));

        game.play("S", "Y");
        assertEquals("Crossroads", game.currentRoom());
        assertTrue(game.printed("You have successfully defeated Spider!"));

        game.play("W", "P", "Heavy Claymore");
        assertEquals("Secret Chamber", game.currentRoom());
        assertTrue(game.carries("Heavy Claymore"));

        game.play("E", "E", "Y", "P", "Bow", "P", "Arrows");
        assertEquals("Judgment Hall", game.currentRoom());
        assertTrue(game.printed("You have successfully defeated Horseless Headless Knight!"));

        game.play("E", "Y");
        assertEquals("Large Flooded Room", game.currentRoom());
        assertTrue(game.printed("You have successfully defeated Siren King!"));

        game.play("E");
        assertEquals("Large Flooded Room", game.currentRoom(), "the claymore leaves too little strength for the gates");

        game.play("D", "Heavy Claymore", "E");
        assertEquals("Villedor", game.currentRoom());
        assertTrue(game.player().getCurrentRoom().isWinRoom());
    }
}
