import init.Game;
import myExceptions.InputEndedException;
import utilities.Logic;

public class Main {
    public static void main(String[] args) {
        int exitCode = 0;
        try {
            new Game().run();
        } catch (InputEndedException e) {
            Logic.printToUser(e.getMessage());
            exitCode = 1;
        } catch (Exception e) {
            Logic.printToUser("Unexpected error: " + e);
            exitCode = 1;
        } finally {
            Logic.printToUser("Goodbye!");
        }
        System.exit(exitCode);
    }
}
