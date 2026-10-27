package init;

import utilities.Console;
import utilities.Logic;

public class Menu {
    public enum Action {
        NEW_GAME,
        CONTINUE,
        EXIT
    }

    public Action promptChoice() {
        while (true) {
            Logic.printToUser("=== Rooms ===");
            Logic.printToUser("1. New Game");
            Logic.printToUser("2. Continue");
            Logic.printToUser("3. Exit");
            Logic.printToUser("Choose an option: ");
            String input = Console.readLine().trim().toUpperCase();
            switch (input) {
                case "1":
                case "NEW":
                case "NEW GAME":
                    return Action.NEW_GAME;
                case "2":
                case "CONTINUE":
                case "C":
                    return Action.CONTINUE;
                case "3":
                case "EXIT":
                case "QUIT":
                case "Q":
                    return Action.EXIT;
                default:
                    Logic.printToUser("Invalid option. Please enter 1, 2, or 3.");
            }
        }
    }
}
