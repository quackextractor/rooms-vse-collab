package tests;

import enemy.Enemy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CombatTest {

    private GameSession nextToTheArena() throws Exception {
        GameSession game = new GameSession();
        game.play("P", "Book", "E");
        return game;
    }

    private Enemy goblin(GameSession game) {
        return game.room(2).getAvailableEnemies().get(0);
    }

    @Test
    void enteringARoomWithAnEnemyAsksThePlayerToFight() throws Exception {
        GameSession game = nextToTheArena();

        game.play("N", "N");

        assertTrue(game.printed("A goblin blocks the way.", "- Goblin (health 30, strength 6, agility 5)", "Do you wish to proceed? [Y/N]"));
    }

    @Test
    void refusingToFightTakesThePlayerBackEvenThroughALockedDoor() throws Exception {
        GameSession game = nextToTheArena();
        game.play("D", "Book");

        game.play("N", "N");

        assertEquals("Library", game.currentRoom());
        assertEquals(30, goblin(game).getHealth());
    }

    @Test
    void anAnswerOtherThanYesOrNoIsAskedAgain() throws Exception {
        GameSession game = nextToTheArena();

        game.play("N", "maybe", "N");

        assertTrue(game.printed("Invalid input. Input 'Y' or 'N'."));
        assertEquals("Library", game.currentRoom());
    }

    @Test
    void losingAFightSendsThePlayerBackWithHalfTheirHealth() throws Exception {
        GameSession game = nextToTheArena();

        game.play("N", "Y");

        assertTrue(game.printed("You were beat by Goblin!"));
        assertEquals("Library", game.currentRoom());
        assertEquals(5, game.stat("health"));
        assertEquals(10, goblin(game).getHealth());
    }

    @Test
    void winningAFightRemovesTheEnemyAndHealsThePlayer() throws Exception {
        GameSession game = nextToTheArena();
        game.play("P", "Sword", "P", "Potion");

        game.play("N", "Y");

        assertTrue(game.printed("You have successfully defeated Goblin!"));
        assertEquals("Arena", game.currentRoom());
        assertTrue(game.room(2).getAvailableEnemies().isEmpty());
        assertEquals(40, game.stat("health"));
    }

    @Test
    void theFasterFighterStrikesFirst() throws Exception {
        GameSession game = nextToTheArena();

        game.play("N", "Y");

        assertTrue(game.printed("Do you wish to proceed? [Y/N]", "You attack Goblin for 10 damage.", "(10|20)"));
    }

    @Test
    void aPlayerWithNoHealthDiesEvenFacingAnEnemy() throws Exception {
        GameSession game = new GameSession(2);
        game.player().setStatValue("health", 0);

        game.play("Y");

        assertTrue(game.printed("You died."));
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
