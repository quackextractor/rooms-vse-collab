package utilities;

import room.Direction;
import room.Room;

import java.util.*;

public class MapView {
    private static final int LABEL_WIDTH = 4;
    private static final int CELL_WIDTH = LABEL_WIDTH + 2;

    private MapView() {
    }

    public static void print(Map<Integer, Room> roomMap, Set<Integer> discoveredRoomIds, int currentRoomId) {
        Map<Integer, int[]> positions = computePositions(roomMap);
        Map<String, String> cells = new HashMap<>();
        int minColumn = Integer.MAX_VALUE;
        int maxColumn = Integer.MIN_VALUE;
        int minRow = Integer.MAX_VALUE;
        int maxRow = Integer.MIN_VALUE;

        for (Room room : roomMap.values()) {
            int[] position = positions.get(room.getId());
            String label = position == null ? null : labelFor(room, roomMap, discoveredRoomIds, currentRoomId);
            if (label == null) {
                continue;
            }
            cells.put(key(position[0], position[1]), label);
            minColumn = Math.min(minColumn, position[0]);
            maxColumn = Math.max(maxColumn, position[0]);
            minRow = Math.min(minRow, position[1]);
            maxRow = Math.max(maxRow, position[1]);
        }

        Logic.printToUser("Discovered map (? unexplored exit, * you are here):");
        if (cells.isEmpty()) {
            Logic.printToUser("Nothing discovered yet.");
            return;
        }

        for (int row = minRow; row <= maxRow; row++) {
            StringBuilder line = new StringBuilder();
            for (int column = minColumn; column <= maxColumn; column++) {
                String label = cells.get(key(column, row));
                if (label == null) {
                    line.append(spaces(CELL_WIDTH));
                } else {
                    line.append("[").append(label).append("]");
                }
            }
            Logic.printToUser(stripTrailingSpaces(line.toString()));
        }
    }

    private static String labelFor(Room room, Map<Integer, Room> roomMap, Set<Integer> discoveredRoomIds, int currentRoomId) {
        String id = String.valueOf(room.getId());
        if (discoveredRoomIds.contains(room.getId())) {
            if (room.getId() == currentRoomId) {
                return "*" + rightAlign(id, LABEL_WIDTH - 1);
            }
            return rightAlign(id, LABEL_WIDTH);
        }
        if (!room.isSecret() && isExitOfDiscoveredRoom(room, roomMap, discoveredRoomIds)) {
            return rightAlign("?", LABEL_WIDTH);
        }
        return null;
    }

    private static boolean isExitOfDiscoveredRoom(Room room, Map<Integer, Room> roomMap, Set<Integer> discoveredRoomIds) {
        for (int discoveredId : discoveredRoomIds) {
            for (Direction direction : Direction.values()) {
                if (roomMap.get(discoveredId).getExit(direction) == room.getId()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Map<Integer, int[]> computePositions(Map<Integer, Room> roomMap) {
        Map<Integer, int[]> positions = new HashMap<>();
        if (roomMap.isEmpty()) {
            return positions;
        }

        int origin = Collections.min(roomMap.keySet());
        positions.put(origin, new int[]{0, 0});
        Deque<Integer> pending = new ArrayDeque<>();
        pending.add(origin);

        while (!pending.isEmpty()) {
            int roomId = pending.poll();
            int[] position = positions.get(roomId);
            Room room = roomMap.get(roomId);
            for (Direction direction : Direction.values()) {
                int neighbourId = room.getExit(direction);
                if (neighbourId == Config.WALL ||positions.containsKey(neighbourId) || !roomMap.containsKey(neighbourId)) {
                    continue;
                }
                positions.put(neighbourId, new int[]{
                        position[0] + direction.getColumnStep(),
                        position[1] + direction.getRowStep()});
                pending.add(neighbourId);
            }
        }
        return positions;
    }

    private static String key(int column, int row) {
        return column + "," + row;
    }

    private static String rightAlign(String text, int width) {
        return spaces(width - text.length()) + text;
    }

    private static String stripTrailingSpaces(String text) {
        int end = text.length();
        while (end > 0 && text.charAt(end - 1) == ' ') {
            end--;
        }
        return text.substring(0, end);
    }

    private static String spaces(int width) {
        StringBuilder blank = new StringBuilder();
        for (int i = 0; i < width; i++) {
            blank.append(' ');
        }
        return blank.toString();
    }
}
