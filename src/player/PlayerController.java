package player;

import enemy.Combat;
import commands.*;
import items.Item;
import utilities.Console;
import utilities.Logic;

import java.util.List;
import java.util.Stack;

public class PlayerController {
    private Player player;
    private static Stack<Command> commandHistory;

    public PlayerController(Player player) {
        this.player = player;
        commandHistory = new Stack<>();
    }

    public void getPlayerInput(Player player) {

        if (!player.getCurrentRoom().getAvailableEnemies().isEmpty()){
            Combat.warn(player, player.getCurrentRoom().getAvailableEnemies());
        }

        if (player.getCurrentRoom().isWinRoom()) {
            Logic.printToUser(player.getCurrentRoom().getWinMessage());
            System.exit(0);
        }

        if (player.getStatValue("health") <= 0){
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
                Logic.printToUser("Quitting. Goodbye!");
                System.exit(0);
            case "H":
                Logic.printToUser("Enter direction (N, E, S, W), I to show inventory, " +
                        "P to pick up item, D to discard item, L to look around you, M to show the map " +
                        "or Q to quit. Press H to bring up this help.");
                break;
            default:
                Logic.printToUser("Invalid input. Please enter N, E, S, W, I, P, D, L, M, H or Q.");
        }
    }




    private void executeCommand(Command command) {
        command.execute();
        commandHistory.push(command);
    }

    public static void undoLastMove() {
        if (!commandHistory.isEmpty()) {
            Command lastCommand = commandHistory.pop();
            lastCommand.undo();
        } else {
            Logic.printToUser("Nowhere to go.");
        }
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
