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

        game.play("P", "Magical Flower");

        assertTrue(game.carries("Magical Flower"));
        assertTrue(game.room(0).getAvailableItems().isEmpty());
    }

    @Test
    void anItemCanBePickedUpByItsNumber() throws Exception {
        GameSession game = new GameSession();

        game.play("P", "1");

        assertTrue(game.carries("Magical Flower"));
    }

    @Test
    void pickingUpTheMagicalFlowerRaisesWisdom() throws Exception {
        GameSession game = new GameSession();
        assertEquals(10, game.stat("wisdom"));

        game.play("P", "Magical Flower");

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

        game.play("P", "Magical Flower", "D", "Magical Flower");

        assertFalse(game.carries("Magical Flower"));
        assertEquals(1, game.room(0).getAvailableItems().size());
        assertEquals(10, game.stat("wisdom"));
    }

    @Test
    void anItemThePlayerDoesNotCarryCannotBeDiscarded() throws Exception {
        GameSession game = new GameSession();

        game.play("D", "Magical Flower");

        assertTrue(game.printed("No such item."));
        assertEquals(1, game.room(0).getAvailableItems().size());
    }

    @Test
    void theSwordIsCarriedAsAWeaponAndRaisesStrength() throws Exception {
        GameSession game = new GameSession(1);

        game.play("P", "Sword");

        assertEquals(1, game.player().getInventoryWeapons().size());
        assertEquals(10, game.player().getInventoryWeapons().get(0).getDamage());
        assertEquals(22, game.stat("strength"));
    }

    @Test
    void theHeavyClaymoreLowersStrength() throws Exception {
        GameSession game = new GameSession(101);

        game.play("P", "Heavy Claymore");

        assertEquals(-10, game.stat("strength"));
    }

    @Test
    void healthNeverDropsBelowZero() throws Exception {
        GameSession game = new GameSession();

        game.player().setStatValue("health", -5);

        assertEquals(0, game.stat("health"));
    }
}
