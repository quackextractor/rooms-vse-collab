package tests;

import items.Item;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import player.Player;
import utilities.SaveManager;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class SaveLoadTest {
    private static final String TEST_SAVE = "test_savegame.dat";

    @BeforeEach
    @AfterEach
    void cleanUp() {
        new File(TEST_SAVE).delete();
        SaveManager.deleteSave();
    }

    private Player savedAndLoaded(Player player) {
        assertTrue(SaveManager.save(player, TEST_SAVE));
        return SaveManager.load(TEST_SAVE);
    }

    @Test
    void aSavedGameKeepsTheRoomStatsAndInventory() throws Exception {
        GameSession game = new GameSession();
        game.play("P", "Book", "E");

        Player loaded = savedAndLoaded(game.player());

        assertEquals("Library", loaded.getCurrentRoom().getName());
        assertEquals(20, loaded.getStatValue("wisdom"));
        assertTrue(loaded.getInventoryItems().stream().map(Item::getName).anyMatch("Book"::equals));
    }

    @Test
    void aSavedGameRemembersTheRoomToRetreatTo() throws Exception {
        GameSession game = new GameSession();
        game.play("P", "Book", "E");

        Player loaded = savedAndLoaded(game.player());
        loaded.retreat();

        assertEquals("Start", loaded.getCurrentRoom().getName());
    }

    @Test
    void aSavedGameCanStillUndo() throws Exception {
        GameSession game = new GameSession();
        game.play("P", "Book", "E");

        Player loaded = savedAndLoaded(game.player());
        loaded.takeLastCommand().orElseThrow().undo();

        assertEquals("Start", loaded.getCurrentRoom().getName());
    }

    @Test
    void aMissingSaveLoadsAsNothing() {
        assertFalse(SaveManager.saveExists("non_existent.dat"));
        assertNull(SaveManager.load("non_existent.dat"));
    }
}
