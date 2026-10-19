package player;

import enemy.Combat;
import commands.*;
import items.Item;
import utilities.Logic;

import java.util.Scanner;
import java.util.Stack;

public class PlayerController {
    private Player player;
    private Scanner scanner;
    private static Stack<Command> commandHistory;

    public PlayerController(Player player) {
        this.player = player;
        this.scanner = new Scanner(System.in);
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

        String input = readLine().toUpperCase();

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
            case "Q":
                Logic.printToUser("Quitting. Goodbye!");
                System.exit(0);
            case "H":
                Logic.printToUser("Enter direction (N, E, S, W), I to show inventory, " +
                        "P to pick up item, D to discard item, L to look around you or Q to quit. Press H to bring up this help.");
                break;
            default:
                Logic.printToUser("Invalid input. Please enter N, E, S, W, I, P, D, H or Q.");
        }
    }



    private String readLine() {
        if (!scanner.hasNextLine()) {
            Logic.printToUser("Input ended. This game needs a terminal: double-click play.cmd on Windows,"
                    + " or run java -jar rooms.jar from one.");
            System.exit(0);
        }
        return scanner.nextLine();
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
        Logic.printToUser("Enter the name of the item you want to pick up: ");
        String itemName = readLine();
        Item item = findItemInRoom(itemName);
        if (item != null) {
            Command pickUpCommand = new PickUpItemCommand(player, item);
            executeCommand(pickUpCommand);
        } else {
            Logic.printToUser("Item not found in the room.");
        }
    }

    private void discardItem() {
        Logic.printToUser("Enter the name of the item you want to discard: ");
        String itemName = readLine();
        Item item = findItemInInventory(itemName);
        if (item != null) {
            Command discardCommand = new DiscardItemCommand(player, item);
            executeCommand(discardCommand);
        } else {
            Logic.printToUser("Item not found in the inventory.");
        }
    }

    private Item findItemInRoom(String itemName) {
        for (Item item : player.getCurrentRoom().getAvailableItems()) {
            if (item.getName().equalsIgnoreCase(itemName)) {
                return item;
            }
        }
        return null;
    }

    private Item findItemInInventory(String itemName) {
        for (Item item : player.getInventoryItems()) {
            if (item.getName().equalsIgnoreCase(itemName)) {
                return item;
            }
        }
        return null;
    }
}
