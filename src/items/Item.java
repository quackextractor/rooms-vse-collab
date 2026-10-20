package items;

import java.util.Map;

public class Item {
    private String name;
    private Map<String, Integer> stats;  // Map to store stats associated with the item

    public Item(String name, Map<String, Integer> stats) {
        this.setName(name);
        this.setStats(stats);
    }

    public Map<String, Integer> getStats() {
        return stats;
    }

    @Override
    public String toString() {
        return
                name + " " +
                stats;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStats(Map<String, Integer> stats) {
        this.stats = stats;
    }
}
