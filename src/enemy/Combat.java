package enemy;

import items.Weapon;
import player.Player;
import room.Room;
import utilities.Config;
import utilities.Console;
import utilities.Logic;

import java.util.Collections;
import java.util.List;

public class Combat {
    private Combat() {
    }

    public static void confront(Player player) {
        Room room = player.getCurrentRoom();
        List<Enemy> enemies = room.getAvailableEnemies();
        Collections.shuffle(enemies);

        if (!room.getCombatMessage().isEmpty()) {
            Logic.printToUser(room.getCombatMessage());
        }
        for (Enemy enemy : enemies) {
            Logic.printToUser("- " + enemy);
        }
        if (!Console.confirm("Do you wish to proceed?")) {
            Logic.printToUser("You backtrack...");
            player.retreat();
            return;
        }

        int maxHealth = player.getStatValue(Config.HEALTH);
        int health = maxHealth;
        while (!enemies.isEmpty()) {
            Enemy enemy = enemies.get(0);
            health = fight(player, enemy, health);
            if (enemy.getHealth() > 0) {
                lose(player, enemy, health, maxHealth);
                return;
            }
            Logic.printToUser("You have successfully defeated " + enemy.getName() + "!");
            enemies.remove(enemy);
        }
        recover(health, maxHealth, "Victory swiftly mended any injuries sustained in the heat of combat.");
    }

    private static void lose(Player player, Enemy enemy, int health, int maxHealth) {
        Logic.printToUser("You were beat by " + enemy.getName() + "!");
        enemy.heal();
        Logic.printToUser("You stumble back into the last room.");
        player.retreat();
        recover(health, maxHealth, "You catch your breath and your wounds close.");
    }

    private static void recover(int health, int maxHealth, String message) {
        Logic.printToUser(message + " (" + health + " -> " + maxHealth + ")");
    }

    private static int fight(Player player, Enemy enemy, int health) {
        boolean isPlayerTurn = !enemy.strikesFirstAgainst(player.getStatValue(Config.AGILITY));
        while (enemy.getHealth() > 0 && health > 0) {
            if (isPlayerTurn) {
                int damage = damage(player.getStatValue(Config.STRENGTH), player.getInventoryWeapons());
                Logic.printToUser("You attack " + enemy.getName() + " for " + damage + " damage.");
                enemy.setHealth(enemy.getHealth() - damage);
            } else {
                Logic.printToUser("The " + enemy.getName() + " hits you for " + enemy.getStrength() + " damage.");
                health = Math.max(0, health - enemy.getStrength());
            }
            Logic.printToUser("(" + health + "|" + enemy.getHealth() + ")");
            isPlayerTurn = !isPlayerTurn;
        }
        return health;
    }

    public static int damage(int strength, List<Weapon> weapons) {
        int damage = strength;
        for (Weapon weapon : weapons) {
            damage += weapon.getDamage();
        }
        return Math.max(Config.MIN_DAMAGE, damage);
    }
}
