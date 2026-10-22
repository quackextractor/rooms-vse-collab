package utilities;

import java.util.Scanner;

public class Console {
    private static Scanner scanner = new Scanner(System.in);

    private Console() {
    }

    public static void useInput(String input) {
        scanner = new Scanner(input);
    }

    public static boolean hasInput() {
        return scanner.hasNextLine();
    }

    public static String readLine() {
        if (!hasInput()) {
            Logic.printToUser("Input ended. This game needs a terminal: double-click play.cmd on Windows,"
                    + " or run java -jar rooms.jar from one.");
            System.exit(0);
        }
        return scanner.nextLine();
    }
}
