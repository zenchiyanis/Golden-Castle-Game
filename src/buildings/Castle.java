package buildings;

import player.Player;
import resources.ResourceType;

import java.util.EnumMap;

public class Castle extends Building {
    public Castle(Player owner) {
        super(BuildingType.CASTLE, cost(), 0, owner);
        setHitsRemaining(6);
    }

    private static EnumMap<ResourceType, Integer> cost() {
        EnumMap<ResourceType, Integer> c = new EnumMap<>(ResourceType.class);
        c.put(ResourceType.WOOD, 0);
        c.put(ResourceType.STONE, 0);
        c.put(ResourceType.GOLD, 0);
        c.put(ResourceType.FOOD, 0);
        return c;
    }
}
