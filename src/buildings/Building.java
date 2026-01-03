package buildings;

import map.Position;
import player.Player;
import resources.ResourceType;

import java.util.EnumMap;

public abstract class Building {
    private final BuildingType type;
    private final EnumMap<ResourceType, Integer> cost;
    private final int buildTime;
    private final Player owner;

    private Position position;
    private int hitsRemaining = 3;

    protected Building(BuildingType type, EnumMap<ResourceType, Integer> cost, int buildTime, Player owner) {
        this.type = type;
        this.cost = cost;
        this.buildTime = buildTime;
        this.owner = owner;
    }

    public BuildingType getType() { return type; }
    public EnumMap<ResourceType, Integer> getCost() { return cost; }
    public int getBuildTime() { return buildTime; }
    public Player getOwner() { return owner; }

    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }

    public int getHitsRemaining() { return hitsRemaining; }
    public void setHitsRemaining(int hitsRemaining) { this.hitsRemaining = Math.max(0, hitsRemaining); }

    public void takeHit() { setHitsRemaining(hitsRemaining - 1); }
    public boolean isDestroyed() { return hitsRemaining <= 0; }
}
