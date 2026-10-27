package room;

public enum Direction {
    NORTH(0, -1),
    EAST(1, 0),
    SOUTH(0, 1),
    WEST(-1, 0);

    private final int columnStep;
    private final int rowStep;

    Direction(int columnStep, int rowStep) {
        this.columnStep = columnStep;
        this.rowStep = rowStep;
    }

    public int getColumnStep() {
        return columnStep;
    }

    public int getRowStep() {
        return rowStep;
    }
}
