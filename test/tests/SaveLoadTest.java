package tests;

import items.Item;
import org.junit.jupiter.api.Test;
import player.Player;
import utilities.SaveManager;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class SaveLoadTest {

    private Player savedAndLoaded(Player player) {
        assertTrue(SaveManager.save(player));
        return SaveManager.load();
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
    void aDamagedSaveSaysItCannotBeRead() throws Exception {
        new GameSession();
        Files.writeString(GameSession.SAVE_FILE, "not a save");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream screen = System.out;
        System.setOut(new PrintStream(output, true));
        try {
            assertNull(SaveManager.load());
        } finally {
            System.setOut(screen);
        }

        assertTrue(output.toString().contains("The saved game is damaged and cannot be loaded"));
    }

    @Test
    void quittingWhenTheSaveFailsAsksFirst() throws Exception {
        GameSession game = new GameSession();
        SaveManager.useFile(GameSession.SAVE_FILE.resolveSibling("no-such-folder").resolve("save.dat"));

        game.play("Q", "N", "L");

        assertTrue(game.printed("Quit anyway? Progress since the last successful save is lost. [Y/N]"));
        assertTrue(game.printed("Items in the current room:"));
    }
}
