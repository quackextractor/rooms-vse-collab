package tests;

import init.Menu;
import org.junit.jupiter.api.Test;
import utilities.Console;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MenuTest {

    private Menu.Action choose(String input) {
        Console.useInput(input + "\n");
        return new Menu().promptChoice();
    }

    @Test
    void eachOptionCanBeChosenByNumber() {
        assertEquals(Menu.Action.NEW_GAME, choose("1"));
        assertEquals(Menu.Action.CONTINUE, choose("2"));
        assertEquals(Menu.Action.EXIT, choose("3"));
    }

    @Test
    void eachOptionCanBeChosenByName() {
        assertEquals(Menu.Action.NEW_GAME, choose("new game"));
        assertEquals(Menu.Action.CONTINUE, choose("c"));
        assertEquals(Menu.Action.EXIT, choose("q"));
    }

    @Test
    void movementKeysAndOtherInputAreAskedAgain() {
        assertEquals(Menu.Action.CONTINUE, choose("n\ne\nxyz\n2"));
    }
}
