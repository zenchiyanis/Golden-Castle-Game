package units;

import player.Player;
import resources.ResourceType;

import java.util.EnumMap;

public class Cavalry extends Unit {
    public Cavalry(Player owner) {
        super(UnitType.CAVALRY, owner, 2, 1, 20, cost());
    }

    private static EnumMap<ResourceType, Integer> cost() {
        EnumMap<ResourceType, Integer> c = new EnumMap<>(ResourceType.class);
        c.put(ResourceType.FOOD, 25);
        c.put(ResourceType.GOLD, 30);
        return c;
    }
}
