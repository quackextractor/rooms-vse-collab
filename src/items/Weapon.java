package items;

import java.util.Map;

public class Weapon extends Item {
    private final int damage;

    public Weapon(String name, int damage, Map<String, Integer> stats) {
        super(name, stats);
        if (damage < 0) {
            throw new IllegalArgumentException("Weapon damage must not be negative, got " + damage + ".");
        }
        this.damage = damage;
    }

    @Override
    public String toString() {
        String base = super.toString();
        return base.substring(0, base.length() - 1) + ", damage " + getDamage() + "}";
    }

    public int getDamage() {
        return damage;
    }
}
