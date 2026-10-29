package enemy;

import items.Weapon;
import player.Player;
import room.Room;
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

        Logic.printToUser(room.getCombatMessage());
        for (Enemy enemy : enemies) {
            Logic.printToUser("- " + enemy);
        }
        if (!Console.confirm("Do you wish to proceed?")) {
            Logic.printToUser("You backtrack...");
            player.retreat();
            return;
        }

        int maxHealth = player.getStatValue("health");
        while (!enemies.isEmpty()) {
            Enemy enemy = enemies.get(0);
            fight(player, enemy);
            if (enemy.getHealth() > 0) {
                lose(player, enemy, maxHealth);
                return;
            }
            Logic.printToUser("You have successfully defeated " + enemy.getName() + "!");
            enemies.remove(enemy);
        }
        recover(player, maxHealth, "Victory swiftly mended any injuries sustained in the heat of combat.");
    }

    private static void lose(Player player, Enemy enemy, int maxHealth) {
        Logic.printToUser("You were beat by " + enemy.getName() + "!");
        enemy.heal();
        Logic.printToUser("You stumble back into the last room.");
        player.retreat();
        recover(player, maxHealth, "You catch your breath and your wounds close.");
    }

    private static void recover(Player player, int maxHealth, String message) {
        Logic.printToUser(message + " (" + player.getStatValue("health") + " -> " + maxHealth + ")");
        player.setStatValue("health", maxHealth);
    }

    private static void fight(Player player, Enemy enemy) {
        boolean isPlayerTurn = player.getStatValue("agility") > enemy.getAgility();
        while (enemy.getHealth() > 0 && player.getStatValue("health") > 0) {
            if (isPlayerTurn) {
                int damage = damageOf(player);
                Logic.printToUser("You attack " + enemy.getName() + " for " + damage + " damage.");
                enemy.setHealth(enemy.getHealth() - damage);
            } else {
                Logic.printToUser("The " + enemy.getName() + " hits you for " + enemy.getStrength() + " damage.");
                player.setStatValue("health", player.getStatValue("health") - enemy.getStrength());
            }
            Logic.printToUser("(" + player.getStatValue("health") + "|" + enemy.getHealth() + ")");
            isPlayerTurn = !isPlayerTurn;
        }
    }

    private static int damageOf(Player player) {
        int damage = player.getStatValue("strength");
        for (Weapon weapon : player.getInventoryWeapons()) {
            damage += weapon.getDamage();
        }
        return damage;
    }
}
