package enemy;

public class Enemy {
    private final String name;
    private int health;
    private final int strength;
    private final int agility;

    public Enemy(String name, int health, int strength, int agility) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Enemy name must not be blank.");
        }
        if (health <= 0 || strength < 0 || agility < 0) {
            throw new IllegalArgumentException("Enemy " + name + " needs health above 0 and non-negative strength and agility.");
        }
        this.name = name;
        this.health = health;
        this.strength = strength;
        this.agility = agility;
    }

    @Override
    public String toString() {
        return getName() + " (health " + getHealth()
                + ", strength " + getStrength()
                + ", agility " + getAgility() + ")";
    }

    public String getName() {
        return name;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = Math.max(0, health);
    }

    public int getStrength() {
        return strength;
    }

    public int getAgility() {
        return agility;
    }
}
