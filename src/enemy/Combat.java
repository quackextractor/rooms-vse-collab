package enemy;

import items.Weapon;
import player.*;
import room.Room;
import utilities.Console;
import utilities.Logic;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Combat {
    public static void warn(Player player, List<Enemy> enemies) {
        // enemies attack serially in random order
        Collections.shuffle(enemies);
        while (true) {
            Logic.printToUser(player.getCurrentRoom().getCombatMessage());
            Logic.printToUser(enemies.toString());
            Logic.printToUser("Do you wish to proceed? [Y/N]");
            String input = Console.readLine();

            int initialHealth = player.getStatValue("health");
            boolean isDefeated = false;

            if (input.equalsIgnoreCase("N")) {
                Logic.printToUser("You backtrack...");
                PlayerController.undoLastMove();
                break;
            } else if (input.equalsIgnoreCase("Y")) {

                for (int i = 0; i<enemies.size(); i++) {
                    fight(player, enemies.get(i));

                    if (enemies.get(i).getHealth() <= 0) {
                        winDuel(player, enemies.get(i));
                        i--;
                    } else {
                        defeat(player, enemies.get(i), initialHealth);
                        isDefeated = true;
                        break;
                    }
                }

                if (!isDefeated) {
                    totalVictory(player, initialHealth);
                    break;
                }
                break;
            } else {
                Logic.printToUser("Invalid input. Input 'Y' or 'N'.");
            }
        }
    }

    private static void winDuel(Player player, Enemy enemy){
        Logic.printToUser("You have successfully defeated " + enemy.getName() + "!");
        Room roomWithThisEnemyDefeated = player.getCurrentRoom();
        // remove defeated enemy from room
        roomWithThisEnemyDefeated.getAvailableEnemies().remove(enemy);
        player.setCurrentRoom(roomWithThisEnemyDefeated);
    }

    // player won't face the other enemies once defeated
    private static void defeat(Player player, Enemy enemy, int initialHealth) {
        Logic.printToUser("You were beat by " + enemy.getName() + "!");
        Logic.printToUser("You stumble back into the last room.");
        PlayerController.undoLastMove();
        int woundedHealth = initialHealth / 2;
        Logic.printToUser("Defeat left you wounded. " + "(" + initialHealth + " -> " + woundedHealth + ")");
        player.setStatValue("health", woundedHealth);
    }

    // player gets healed after surviving all the duels in room
    private static void totalVictory(Player player, int initialHealth) {
        Logic.printToUser("Victory swiftly mended any injuries sustained in the heat of combat. " + "(" + player.getStatValue("health") + " -> " + initialHealth + ")");
        player.setStatValue("health", initialHealth);
    }

    private static void hit(Player player, Enemy enemy, boolean isPlayerTurn) {
        if (isPlayerTurn) {
            int damage = calculateDamage(player);
            Logic.printToUser("You attack " + enemy.getName() + " for " + damage + " damage.");
            enemy.setHealth(enemy.getHealth() - damage);
        } else {
            Logic.printToUser("The " + enemy.getName() + " hits you for " + enemy.getStrength() + " damage.");
            player.setStatValue("health", player.getStatValue("health") - enemy.getStrength());
        }
    }
    private static int calculateDamage(Player player){
        int damage = (player.getStatValue("strength"));
        for (Weapon weapon : player.getInventoryWeapons()) {
           damage += weapon.getDamage();
        }
        return damage;
    }
    private static void fight(Player player, Enemy enemy) {
        boolean isPlayerTurn = player.getStatValue("agility") > enemy.getAgility();
        while (enemy.getHealth() > 0 && player.getStatValue("health") > 0) {
            hit(player, enemy, isPlayerTurn);
            // print battle progress after each hit
            Logic.printToUser("(" + player.getStatValue("health") + "|" + enemy.getHealth() + ")");
            // turn logic
            isPlayerTurn = !isPlayerTurn;
        }
    }
}
