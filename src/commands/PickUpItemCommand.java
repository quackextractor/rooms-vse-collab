package commands;

import items.Item;
import player.Player;

import java.util.LinkedHashMap;
import java.util.Map;

public class PickUpItemCommand implements Command {
    private static final long serialVersionUID = 1L;
    private final Player player;
    private final Item item;
    private Map<String, Integer> statsBefore;

    public PickUpItemCommand(Player player, Item item) {
        this.player = player;
        this.item = item;
    }

    @Override
    public boolean execute() {
        statsBefore = new LinkedHashMap<>(player.getStats());
        return player.pickUpItem(item);
    }

    @Override
    public void undo() {
        player.undoPickUp(item, statsBefore);
    }
}
