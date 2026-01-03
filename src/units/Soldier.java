package units;

import player.Player;
import resources.ResourceType;

import java.util.EnumMap;

public class Soldier extends Unit {
    public Soldier(Player owner) {
        super(UnitType.SOLDIER, owner, 3, 1, 10, cost());
    }

    private static EnumMap<ResourceType, Integer> cost() {
        EnumMap<ResourceType, Integer> c = new EnumMap<>(ResourceType.class);
        c.put(ResourceType.FOOD, 20);
        c.put(ResourceType.GOLD, 15);
        return c;
    }
}
