package utilities;

import player.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class SaveManager {
    public static final String DEFAULT_SAVE_FILE = "savegame.dat";

    private SaveManager() {
    }

    public static boolean saveExists() {
        return saveExists(DEFAULT_SAVE_FILE);
    }

    public static boolean saveExists(String path) {
        return new File(path).exists();
    }

    public static boolean save(Player player) {
        return save(player, DEFAULT_SAVE_FILE);
    }

    public static boolean save(Player player, String path) {
        Path target = Path.of(path);
        Path draft = Path.of(path + ".tmp");
        try {
            try (OutputStream file = Files.newOutputStream(draft);
                 ObjectOutputStream out = new ObjectOutputStream(file)) {
                out.writeObject(player);
            }
            Files.move(draft, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            return true;
        } catch (Exception e) {
            Logic.printToUser("Could not save the game: " + e.getMessage());
            return false;
        }
    }

    public static Player load() {
        return load(DEFAULT_SAVE_FILE);
    }

    public static Player load(String path) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(path))) {
            return (Player) in.readObject();
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean deleteSave() {
        return deleteSave(DEFAULT_SAVE_FILE);
    }

    public static boolean deleteSave(String path) {
        File file = new File(path);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }
}
