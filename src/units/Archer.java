package units;

import player.Player;
import resources.ResourceType;

import java.util.EnumMap;

public class Archer extends Unit {
    public Archer(Player owner) {
        super(UnitType.ARCHER, owner, 3, 2, 15, cost());
    }

    private static EnumMap<ResourceType, Integer> cost() {
        EnumMap<ResourceType, Integer> c = new EnumMap<>(ResourceType.class);
        c.put(ResourceType.FOOD, 18);
        c.put(ResourceType.GOLD, 20);
        c.put(ResourceType.WOOD, 10);
        return c;
    }
}
