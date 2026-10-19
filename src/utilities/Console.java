package utilities;

import java.util.Scanner;

public class Console {
    private static final Scanner SCANNER = new Scanner(System.in);

    private Console() {
    }

    public static String readLine() {
        if (!SCANNER.hasNextLine()) {
            Logic.printToUser("Input ended. This game needs a terminal: double-click play.cmd on Windows,"
                    + " or run java -jar rooms.jar from one.");
            System.exit(0);
        }
        return SCANNER.nextLine();
    }
}