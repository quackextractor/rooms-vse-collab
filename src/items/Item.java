package items;

import java.io.Serializable;
import java.util.Map;

public class Item implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String name;
    private final Map<String, Integer> stats;

    public Item(String name, Map<String, Integer> stats) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Item name must not be blank.");
        }
        this.name = name;
        this.stats = Map.copyOf(stats);
    }

    public Map<String, Integer> getStats() {
        return stats;
    }

    @Override
    public String toString() {
        return name + " " + stats;
    }

    public String getName() {
        return name;
    }
}
