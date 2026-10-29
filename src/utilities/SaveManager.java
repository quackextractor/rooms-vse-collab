package utilities;

import player.Player;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class SaveManager {
    private static Path saveFile = Path.of(Config.SAVE_FILE);

    private SaveManager() {
    }

    public static void useFile(Path file) {
        saveFile = file;
    }

    public static boolean saveExists() {
        return Files.exists(saveFile);
    }

    public static boolean save(Player player) {
        Path draft = Path.of(saveFile + ".tmp");
        try {
            try (OutputStream file = Files.newOutputStream(draft);
                 ObjectOutputStream out = new ObjectOutputStream(file)) {
                out.writeObject(player);
            }
            moveIntoPlace(draft);
            return true;
        } catch (IOException | RuntimeException e) {
            draft.toFile().delete();
            Logic.printToUser("Could not save the game to " + saveFile.toAbsolutePath() + " (" + reason(e) + ").");
            return false;
        }
    }

    private static void moveIntoPlace(Path draft) throws IOException {
        try {
            Files.move(draft, saveFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(draft, saveFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static Player load() {
        try (InputStream file = Files.newInputStream(saveFile);
             ObjectInputStream in = new ObjectInputStream(file)) {
            return (Player) in.readObject();
        } catch (InvalidClassException | ClassNotFoundException | ClassCastException e) {
            Logic.printToUser("The saved game is from another version of Rooms and cannot be loaded."
                    + " Start a new game to replace it.");
        } catch (IOException | RuntimeException e) {
            Logic.printToUser("The saved game is damaged and cannot be loaded (" + reason(e) + ")."
                    + " Start a new game to replace it.");
        }
        return null;
    }

    public static void deleteSave() {
        try {
            Files.deleteIfExists(saveFile);
        } catch (IOException e) {
            Logic.printToUser("Could not delete the saved game (" + reason(e) + ").");
        }
    }

    private static String reason(Exception e) {
        String type = e.getClass().getSimpleName();
        return e.getMessage() == null ? type : type + ": " + e.getMessage();
    }
}
