package buildings;

import player.Player;
import resources.ResourceType;

import java.util.EnumMap;

public class Farm extends Building {
    public Farm(Player owner) {
        super(BuildingType.FARM, cost(), 0, owner);
        setHitsRemaining(2);
    }

    private static EnumMap<ResourceType, Integer> cost() {
        EnumMap<ResourceType, Integer> c = new EnumMap<>(ResourceType.class);
        c.put(ResourceType.WOOD, 80);
        return c;
    }
}
