import init.Game;
import myExceptions.ParsingLevelException;
import utilities.Logic;

public class Main {
    public static void main(String[] args) {
        int exitCode = 0;
        try {
            Game game = new Game();
            game.run();
        } catch (ParsingLevelException p) {
            Logic.printToUser(p.getMessage());
            Logic.printToUser("Unable to load game. Quitting.");
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