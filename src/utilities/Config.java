package utilities;

import java.util.List;

public final class Config {
    public static final String MAP_FILE = "rooms.csv";
    public static final String SAVE_FILE = "savegame.dat";
    public static final String LOG_FILE = "log.txt";

    public static final String STRENGTH = "strength";
    public static final String WISDOM = "wisdom";
    public static final String AGILITY = "agility";
    public static final String HEALTH = "health";
    public static final List<String> STATS = List.of(STRENGTH, WISDOM, AGILITY, HEALTH);
    public static final int STARTING_STAT_VALUE = 10;

    public static final int START_ROOM_ID = 0;
    public static final int WALL = -1;
    public static final int MIN_HEALTH = 1;
    public static final int MIN_DAMAGE = 1;

    public static final int MAP_LABEL_WIDTH = 4;

    public enum Column {
        ID("id"), NAME("name"), NORTH("north"), EAST("east"), SOUTH("south"), WEST("west"),
        DESCRIPTION("description"), ITEMS("items"), ENEMIES("enemies"), WARNING_MESSAGE("warning message"),
        WEAPONS("weapons"), CONDITIONS("conditions"), IS_WIN_ROOM("isWinRoom"), WIN_MESSAGE("winMessage"),
        IS_SECRET("isSecret");

        private final String label;

        Column(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public static final int MANDATORY_COLUMNS = Column.DESCRIPTION.ordinal();

    private Config() {
    }
}
