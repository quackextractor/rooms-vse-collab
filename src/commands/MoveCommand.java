package commands;

import player.Player;
import room.Direction;
import room.Room;

public class MoveCommand implements Command {
    private static final long serialVersionUID = 1L;
    private final Player player;
    private final Direction direction;
    private Room from;

    public MoveCommand(Player player, Direction direction) {
        this.player = player;
        this.direction = direction;
    }

    @Override
    public boolean execute() {
        from = player.getCurrentRoom();
        return player.move(direction);
    }

    @Override
    public void undo() {
        player.returnTo(from);
    }
}
