package resources;

import player.Player;

import java.util.EnumMap;

public class EconomyService {
    public boolean trySpend(Player p, EnumMap<ResourceType, Integer> cost) {
        if (!p.getResources().canAfford(cost)) return false;
        p.getResources().spend(cost);
        return true;
    }

    public void grant(Player p, ResourceType t, int amount) {
        p.getResources().add(t, amount);
    }
}
