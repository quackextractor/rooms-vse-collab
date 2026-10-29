import init.Game;
import utilities.Logic;

public class Main {
    public static void main(String[] args) {
        int exitCode = 0;
        try {
            new Game().run();
        } catch (Exception e) {
            Logic.printToUser("Unexpected error: " + e);
            exitCode = 1;
        } finally {
            Logic.printToUser("Goodbye!");
        }
        System.exit(exitCode);
    }
}
