package player;

import commands.*;
import enemy.Combat;
import items.Item;
import room.Direction;
import room.Room;
import utilities.Console;
import utilities.Logic;
import utilities.SaveManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;

public class PlayerController {
    private static final List<String> HELP_ACTIONS = List.of(
            "Enter direction (N, E, S, W) to move around",
            "I to show inventory",
            "P to pick up item",
            "D to discard item",
            "L to look around you",
            "M to show the map",
            "U to undo the last move, pick-up or discard (not past a fight)",
            "Q to save and quit");
    private final Player player;
    private final Map<String, BooleanSupplier> actions = new HashMap<>();

    public PlayerController(Player player) {
        this.player = player;
        registerActions();
    }

    private void registerActions() {
        registerAction("N", () -> execute(new MoveCommand(player, Direction.NORTH)));
        registerAction("E", () -> execute(new MoveCommand(player, Direction.EAST)));
        registerAction("S", () -> execute(new MoveCommand(player, Direction.SOUTH)));
        registerAction("W", () -> execute(new MoveCommand(player, Direction.WEST)));
        registerAction("I", () -> showInventory());
        registerAction("P", () -> pickUpItem());
        registerAction("D", () -> discardItem());
        registerAction("L", () -> lookAround());
        registerAction("M", () -> player.showMap());
        registerAction("U", () -> undo());
        registerAction("H", () -> printHelp());
        registerAction("Q", () -> quit());
    }

    private void registerAction(String key, Runnable action) {
        actions.put(key, () -> {
            action.run();
            return true;
        });
    }

    private void registerAction(String key, BooleanSupplier action) {
        actions.put(key, action);
    }

    public static void printHelp() {
        Logic.printToUser("You can do the following actions:");
        Logic.printToUser("");
        for (String action : HELP_ACTIONS) {
            Logic.printToUser("- " + action);
        }
        Logic.printToUser("");
        Logic.printToUser("Press H to bring up this help.");
    }

    public boolean playTurn() {
        Room room = player.getCurrentRoom();
        if (room.isWinRoom()) {
            return endGame(room.getWinMessage());
        }
        if (!room.getAvailableEnemies().isEmpty()) {
            player.forgetHistory();
            Combat.confront(player);
            SaveManager.save(player);
            return true;
        }
        return handle(Console.readLine().trim().toUpperCase());
    }

    private boolean endGame(String message) {
        SaveManager.deleteSave();
        Logic.printToUser(message);
        return false;
    }

    private boolean handle(String input) {
        BooleanSupplier action = actions.get(input);
        if (action == null) {
            Logic.printToUser("Invalid input. Please enter N, E, S, W, I, P, D, L, M, U, H or Q.");
            return true;
        }
        return action.getAsBoolean();
    }

    private boolean quit() {
        if (SaveManager.save(player)) {
            Logic.printToUser("Game saved.");
            return false;
        }
        return !Console.confirm("Quit anyway? Progress since the last successful save is lost.");
    }

    private void execute(Command command) {
        if (command.execute()) {
            player.record(command);
            SaveManager.save(player);
        }
    }

    private void undo() {
        Optional<Command> last = player.takeLastCommand();
        if (last.isEmpty()) {
            Logic.printToUser("Nothing to undo.");
            return;
        }
        last.get().undo();
        SaveManager.save(player);
    }

    private void showInventory() {
        PlayerView.showInventory(player);
        PlayerView.showStats(player);
    }

    private void lookAround() {
        PlayerView.showItemsInRoom(player.getCurrentRoom());
    }

    private void pickUpItem() {
        PlayerView.showItemsInRoom(player.getCurrentRoom());
        Logic.printToUser("Enter the number or name of the item you want to pick up: ");
        Item item = findItem(player.getCurrentRoom().getAvailableItems(), Console.readLine());
        if (item == null) {
            Logic.printToUser("No such item. Type its number or its name as listed above.");
            return;
        }
        execute(new PickUpItemCommand(player, item));
        PlayerView.showStats(player);
        PlayerView.showItemsInRoom(player.getCurrentRoom());
    }

    private void discardItem() {
        PlayerView.showInventory(player);
        Logic.printToUser("Enter the number or name of the item you want to discard: ");
        Item item = findItem(player.getInventoryItems(), Console.readLine());
        if (item == null) {
            Logic.printToUser("No such item. Type its number or its name as listed above.");
            return;
        }
        execute(new DiscardItemCommand(player, item));
        PlayerView.showStats(player);
    }

    private Item findItem(List<Item> items, String input) {
        try {
            int number = Integer.parseInt(input.trim());
            if (number >= 1 && number <= items.size()) {
                return items.get(number - 1);
            }
        } catch (NumberFormatException notANumber) {
        }

        for (Item item : items) {
            if (item.getName().equalsIgnoreCase(input.trim())) {
                return item;
            }
        }
        return null;
    }
}
