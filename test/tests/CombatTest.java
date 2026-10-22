package tests;

import enemy.Enemy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CombatTest {

    private GameSession nextToTheGargoyle() throws Exception {
        GameSession game = new GameSession();
        game.play("P", "Magical Flower", "E", "N");
        return game;
    }

    private Enemy gargoyle(GameSession game) {
        return game.room(3).getAvailableEnemies().get(0);
    }

    @Test
    void enteringARoomWithAnEnemyAsksThePlayerToFight() throws Exception {
        GameSession game = nextToTheGargoyle();

        game.play("E", "N");

        assertTrue(game.printed("- Gargoyle (health 60, strength 8, agility 6)"));
        assertTrue(game.printed("Do you wish to proceed? [Y/N]"));
    }

    @Test
    void refusingToFightTakesThePlayerBack() throws Exception {
        GameSession game = nextToTheGargoyle();

        game.play("E", "N");

        assertEquals("L1", game.currentRoom());
        assertEquals(60, gargoyle(game).getHealth());
    }

    @Test
    void anAnswerOtherThanYesOrNoIsAskedAgain() throws Exception {
        GameSession game = nextToTheGargoyle();

        game.play("E", "maybe", "N");

        assertTrue(game.printed("Invalid input. Input 'Y' or 'N'."));
        assertEquals("L1", game.currentRoom());
    }

    @Test
    void losingAFightSendsThePlayerBackWithHalfTheirHealth() throws Exception {
        GameSession game = nextToTheGargoyle();

        game.play("E", "Y");

        assertTrue(game.printed("You were beat by Gargoyle!"));
        assertEquals("L1", game.currentRoom());
        assertEquals(5, game.stat("health"));
        assertEquals(40, gargoyle(game).getHealth());
    }

    @Test
    void winningAFightRemovesTheEnemyAndHealsThePlayer() throws Exception {
        GameSession game = new GameSession();
        game.play("P", "Magical Flower", "E", "P", "Shield", "P", "Sword", "N", "P", "Potion");

        game.play("E", "Y");

        assertTrue(game.printed("You have successfully defeated Gargoyle!"));
        assertEquals("L2", game.currentRoom());
        assertTrue(game.room(3).getAvailableEnemies().isEmpty());
        assertEquals(45, game.stat("health"));
    }

    @Test
    void theFasterFighterStrikesFirst() throws Exception {
        GameSession game = nextToTheGargoyle();

        game.play("E", "Y");

        assertTrue(game.printed("You attack Gargoyle for 10 damage." + System.lineSeparator() + "(10|50)"));
    }

    @Test
    void anEnemyIsDescribedByItsStats() {
        Enemy ghost = new Enemy("Ghost", 40, 6, 7);

        assertEquals("Ghost (health 40, strength 6, agility 7)", ghost.toString());
    }

    @Test
    void anEnemysHealthNeverDropsBelowZero() {
        Enemy rat = new Enemy("Rat", 5, 3, 25);

        rat.setHealth(-10);

        assertEquals(0, rat.getHealth());
    }
}
