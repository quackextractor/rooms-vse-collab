package player;

import commands.*;
import enemy.Combat;
import items.Item;
import utilities.Console;
import utilities.Logic;
import utilities.SaveManager;

import java.util.List;
import java.util.Stack;

public class PlayerController {
    private static final List<String> HELP_ACTIONS = List.of(
            "Enter direction (N, E, S, W) to move around",
            "I to show inventory",
            "P to pick up item",
            "D to discard item",
            "L to look around you",
            "M to show the map",
            "Q to quit"
    );
    private static Stack<Command> commandHistory;
    private Player player;

    public PlayerController(Player player) {
        this.player = player;
        commandHistory = new Stack<>();
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

    public static void undoLastMove() {
        if (!commandHistory.isEmpty()) {
            Command lastCommand = commandHistory.pop();
            lastCommand.undo();
        } else {
            Logic.printToUser("Nowhere to go.");
        }
    }

    public void getPlayerInput(Player player) {

        if (!player.getCurrentRoom().getAvailableEnemies().isEmpty()) {
            Combat.warn(player, player.getCurrentRoom().getAvailableEnemies());
            SaveManager.save(player);
            return;
        }

        if (player.getCurrentRoom().isWinRoom()) {
            SaveManager.deleteSave();
            Logic.printToUser(player.getCurrentRoom().getWinMessage());
            System.exit(0);
        }

        if (player.getStatValue("health") <= 0) {
            SaveManager.deleteSave();
            Logic.printToUser("You died.");
            System.exit(0);
        }

        String input = Console.readLine().toUpperCase();

        switch (input) {
            case "N":
                executeCommand(new MoveNorthCommand(player));
                break;
            case "E":
                executeCommand(new MoveEastCommand(player));
                break;
            case "S":
                executeCommand(new MoveSouthCommand(player));
                break;
            case "W":
                executeCommand(new MoveWestCommand(player));
                break;
            case "I":
                player.showInventory();
                player.showStats();
                break;
            case "P":
                pickUpItem();
                break;
            case "D":
                discardItem();
                break;
            case "L":
                executeCommand(new ShowItemsCommand(player));
                break;
            case "M":
                player.showMap();
                break;
            case "Q":
                SaveManager.save(player);
                Logic.printToUser("Quitting. Goodbye!");
                System.exit(0);
            case "H":
                printHelp();
                break;
            default:
                Logic.printToUser("Invalid input. Please enter N, E, S, W, I, P, D, L, M, H or Q.");
        }
    }

    private void executeCommand(Command command) {
        command.execute();
        commandHistory.push(command);
        SaveManager.save(player);
    }

    private void undoLastAction() {
        if (!commandHistory.isEmpty()) {
            Command lastCommand = commandHistory.pop();
            lastCommand.undo();
            Logic.printToUser("Undone last action.");
        } else {
            Logic.printToUser("Nothing to undo.");
        }
    }

    private void pickUpItem() {
        player.showItemsInRoom();
        Logic.printToUser("Enter the number or name of the item you want to pick up: ");
        Item item = findItem(player.getCurrentRoom().getAvailableItems(), Console.readLine());
        if (item != null) {
            executeCommand(new PickUpItemCommand(player, item));
        } else {
            Logic.printToUser("No such item. Type its number or its name as listed above.");
        }
    }

    private void discardItem() {
        player.showInventory();
        Logic.printToUser("Enter the number or name of the item you want to discard: ");
        Item item = findItem(player.getInventoryItems(), Console.readLine());
        if (item != null) {
            executeCommand(new DiscardItemCommand(player, item));
        } else {
            Logic.printToUser("No such item. Type its number or its name as listed above.");
        }
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
