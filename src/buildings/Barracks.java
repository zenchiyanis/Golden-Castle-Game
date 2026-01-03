package buildings;

import player.Player;
import resources.ResourceType;

import java.util.EnumMap;

public class Barracks extends Building {
    public Barracks(Player owner) {
        super(BuildingType.BARRACKS, cost(), 0, owner);
        setHitsRemaining(3);
    }

    private static EnumMap<ResourceType, Integer> cost() {
        EnumMap<ResourceType, Integer> c = new EnumMap<>(ResourceType.class);
        c.put(ResourceType.WOOD, 120);
        c.put(ResourceType.STONE, 80);
        return c;
    }
}
