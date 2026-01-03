package units;

import map.Position;
import player.Player;
import resources.ResourceType;

import java.util.EnumMap;

public class Unit {
    private final UnitType type;
    private final Player owner;
    private final EnumMap<ResourceType, Integer> cost;

    private Position pos;
    private boolean big;
    private int hitsRemaining;
    private int range;
    private int hitPower;

    public Unit(UnitType type, Player owner, int hitsRemaining, int range, int hitPower, EnumMap<ResourceType, Integer> cost) {
        this.type = type;
        this.owner = owner;
        this.hitsRemaining = Math.max(1, hitsRemaining);
        this.range = Math.max(1, range);
        this.hitPower = Math.max(1, hitPower);
        this.cost = cost;
        this.big = false;
    }

    public UnitType getType() { return type; }
    public Player getOwner() { return owner; }
    public EnumMap<ResourceType, Integer> getCost() { return cost; }

    public Position getPos() { return pos; }
    public void setPos(Position pos) { this.pos = pos; }

    public boolean isBig() { return big; }
    public void setBig(boolean big) { this.big = big; }

    public int getRange() { return range; }
    public int hitPower() { return hitPower; }

    public int getHitsRemaining() { return hitsRemaining; }
    public void setHitsRemaining(int hitsRemaining) { this.hitsRemaining = Math.max(0, hitsRemaining); }

    public void takeHit() { setHitsRemaining(hitsRemaining - 1); }
    public boolean isDead() { return hitsRemaining <= 0; }
}
