package player;

import commands.*;
import enemy.Combat;
import items.Item;
import room.Direction;
import room.Room;
import utilities.Console;
import utilities.Logic;
import utilities.SaveManager;

import java.util.List;
import java.util.Optional;

public class PlayerController {
    private static final List<String> HELP_ACTIONS = List.of(
            "Enter direction (N, E, S, W) to move around",
            "I to show inventory",
            "P to pick up item",
            "D to discard item",
            "L to look around you",
            "M to show the map",
            "U to undo the last move, pick-up or discard (not past a fight)",
            "Q to save and quit"
    );
    private final Player player;

    public PlayerController(Player player) {
        this.player = player;
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
        if (player.getStatValue("health") <= 0) {
            return endGame("You died.");
        }
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
        switch (input) {
            case "N":
                execute(new MoveCommand(player, Direction.NORTH));
                break;
            case "E":
                execute(new MoveCommand(player, Direction.EAST));
                break;
            case "S":
                execute(new MoveCommand(player, Direction.SOUTH));
                break;
            case "W":
                execute(new MoveCommand(player, Direction.WEST));
                break;
            case "I":
                PlayerView.showInventory(player);
                PlayerView.showStats(player);
                break;
            case "P":
                pickUpItem();
                break;
            case "D":
                discardItem();
                break;
            case "L":
                PlayerView.showItemsInRoom(player.getCurrentRoom());
                break;
            case "M":
                player.showMap();
                break;
            case "U":
                undo();
                break;
            case "H":
                printHelp();
                break;
            case "Q":
                if (SaveManager.save(player)) {
                    Logic.printToUser("Game saved.");
                }
                return false;
            default:
                Logic.printToUser("Invalid input. Please enter N, E, S, W, I, P, D, L, M, U, H or Q.");
        }
        return true;
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
