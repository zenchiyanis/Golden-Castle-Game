package buildings;

import player.Player;
import resources.ResourceType;

import java.util.EnumMap;

public class Mine extends Building {
    public Mine(Player owner) {
        super(BuildingType.MINE, cost(), 0, owner);
        setHitsRemaining(2);
    }

    private static EnumMap<ResourceType, Integer> cost() {
        EnumMap<ResourceType, Integer> c = new EnumMap<>(ResourceType.class);
        c.put(ResourceType.WOOD, 60);
        c.put(ResourceType.STONE, 80);
        return c;
    }
}
