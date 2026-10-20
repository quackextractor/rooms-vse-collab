package items;

import java.util.Map;

public class Weapon extends Item {
    private int damage;

    public Weapon(String name, int damage, Map<String, Integer> stats) {
        super(name, stats);
        this.setDamage(damage);
    }

    @Override
    public String toString() {
        String base = super.toString();
        return base.substring(0, base.length() - 1) + ", damage " + getDamage() + "}";
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }
}