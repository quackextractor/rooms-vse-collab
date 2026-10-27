package tests;

import init.Menu;
import org.junit.jupiter.api.Test;
import utilities.Console;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MenuTest {

    @Test
    void menuSelectsNewGameByNumber() {
        Console.useInput("1\n");
        Menu menu = new Menu();

        assertEquals(Menu.Action.NEW_GAME, menu.promptChoice());
    }

    @Test
    void menuSelectsContinueByNumber() {
        Console.useInput("2\n");
        Menu menu = new Menu();

        assertEquals(Menu.Action.CONTINUE, menu.promptChoice());
    }

    @Test
    void menuSelectsExitByNumber() {
        Console.useInput("3\n");
        Menu menu = new Menu();

        assertEquals(Menu.Action.EXIT, menu.promptChoice());
    }

    @Test
    void menuAcceptsTextOptions() {
        Console.useInput("new game\n");
        assertEquals(Menu.Action.NEW_GAME, new Menu().promptChoice());

        Console.useInput("continue\n");
        assertEquals(Menu.Action.CONTINUE, new Menu().promptChoice());

        Console.useInput("exit\n");
        assertEquals(Menu.Action.EXIT, new Menu().promptChoice());
    }

    @Test
    void menuAcceptsSingleLetterOptions() {
        Console.useInput("n\n");
        assertEquals(Menu.Action.NEW_GAME, new Menu().promptChoice());

        Console.useInput("c\n");
        assertEquals(Menu.Action.CONTINUE, new Menu().promptChoice());

        Console.useInput("q\n");
        assertEquals(Menu.Action.EXIT, new Menu().promptChoice());
    }

    @Test
    void menuRejectsInvalidInputBeforeAcceptingValidChoice() {
        Console.useInput("invalid\nxyz\n1\n");
        Menu menu = new Menu();

        assertEquals(Menu.Action.NEW_GAME, menu.promptChoice());
    }
}
