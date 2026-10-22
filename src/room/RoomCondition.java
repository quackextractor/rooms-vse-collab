package room;

import java.util.Objects;

public class RoomCondition {
    private final String stat;
    private final int minValue;
    private final String message;

    public RoomCondition(String stat, int minValue, String message) {
        if (stat == null || stat.isBlank()) {
            throw new IllegalArgumentException("Condition stat must not be blank.");
        }
        this.stat = stat;
        this.minValue = minValue;
        this.message = Objects.requireNonNull(message, "message");
    }

    public String getStat() {
        return stat;
    }

    public int getMinValue() {
        return minValue;
    }

    public String getMessage() {
        return message;
    }
}
