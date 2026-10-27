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

    @Test
    void savingPlayerPersistsStatsAndInventory() throws Exception {
        GameSession session = new GameSession();
        session.play("P", "Book", "E");

        Player originalPlayer = session.player();
        assertTrue(SaveManager.save(originalPlayer, TEST_SAVE));
        assertTrue(SaveManager.saveExists(TEST_SAVE));

        Player loadedPlayer = SaveManager.load(TEST_SAVE);
        assertNotNull(loadedPlayer);
        assertEquals("Library", loadedPlayer.getCurrentRoom().getName());
        assertEquals(20, loadedPlayer.getStatValue("wisdom"));
        assertTrue(loadedPlayer.getInventoryItems().stream().map(Item::getName).anyMatch("Book"::equals));
    }

    @Test
    void nonExistentSaveReturnsNullAndReportsFalse() {
        assertFalse(SaveManager.saveExists("non_existent.dat"));
        assertNull(SaveManager.load("non_existent.dat"));
    }

    @Test
    void deletingSaveRemovesFile() throws Exception {
        GameSession session = new GameSession();
        SaveManager.save(session.player(), TEST_SAVE);
        assertTrue(SaveManager.saveExists(TEST_SAVE));

        assertTrue(SaveManager.deleteSave(TEST_SAVE));
        assertFalse(SaveManager.saveExists(TEST_SAVE));
    }
}
