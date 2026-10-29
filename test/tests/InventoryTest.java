package tests;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InventoryTest {

    @Test
    void theInventoryStartsEmpty() throws Exception {
        GameSession game = new GameSession();

        assertTrue(game.player().getInventoryItems().isEmpty());
    }

    @Test
    void pickingUpAnItemMovesItFromTheRoomToTheInventory() throws Exception {
        GameSession game = new GameSession();

        game.play("P", "Book");

        assertTrue(game.carries("Book"));
        assertTrue(game.room(0).getAvailableItems().isEmpty());
    }

    @Test
    void anItemCanBePickedUpByItsNumber() throws Exception {
        GameSession game = new GameSession();

        game.play("P", "1");

        assertTrue(game.carries("Book"));
    }

    @Test
    void pickingUpTheBookRaisesWisdom() throws Exception {
        GameSession game = new GameSession();
        assertEquals(10, game.stat("wisdom"));

        game.play("P", "Book");

        assertEquals(20, game.stat("wisdom"));
    }

    @Test
    void anItemThatIsNotInTheRoomCannotBePickedUp() throws Exception {
        GameSession game = new GameSession();

        game.play("P", "Sword");

        assertFalse(game.carries("Sword"));
        assertTrue(game.printed("No such item."));
    }

    @Test
    void discardingAnItemLeavesItInTheRoomAndTakesItsBonusAway() throws Exception {
        GameSession game = new GameSession();

        game.play("P", "Book", "D", "Book");

        assertFalse(game.carries("Book"));
        assertEquals(1, game.room(0).getAvailableItems().size());
        assertEquals(10, game.stat("wisdom"));
    }

    @Test
    void anItemThePlayerDoesNotCarryCannotBeDiscarded() throws Exception {
        GameSession game = new GameSession();

        game.play("D", "Book");

        assertTrue(game.printed("No such item."));
        assertEquals(1, game.room(0).getAvailableItems().size());
    }

    @Test
    void theSwordIsCarriedAsAWeaponAndRaisesStrength() throws Exception {
        GameSession game = new GameSession(1);

        game.play("P", "Sword");

        assertEquals(1, game.player().getInventoryWeapons().size());
        assertEquals(10, game.player().getInventoryWeapons().get(0).getDamage());
        assertEquals(15, game.stat("strength"));
    }

    @Test
    void anItemCanLowerAStat() throws Exception {
        GameSession game = new GameSession(9);

        game.play("P", "Stone");

        assertEquals(5, game.stat("agility"));
    }

    @Test
    void discardingAHealthItemNeverKillsThePlayer() throws Exception {
        GameSession game = new GameSession(1);
        game.play("P", "Potion");
        game.player().setStatValue("health", 10);

        game.play("D", "Potion");

        assertEquals(1, game.stat("health"));
    }

    @Test
    void pickingUpAHarmfulItemLeavesAtLeastOneHealth() throws Exception {
        GameSession game = new GameSession(9);

        game.play("P", "Thorn");

        assertEquals(1, game.stat("health"));
    }

    @Test
    void discardingAHarmfulItemGivesBackOnlyWhatItTook() throws Exception {
        GameSession game = new GameSession(9);

        game.play("P", "Thorn", "D", "Thorn");

        assertEquals(10, game.stat("health"));
    }

    @Test
    void healthNeverReadsBelowOne() throws Exception {
        GameSession game = new GameSession();

        game.player().setStatValue("health", -5);

        assertEquals(1, game.stat("health"));
    }
}
