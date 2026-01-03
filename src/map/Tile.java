package map;

import buildings.Building;
import player.Player;
import units.Unit;

public class Tile {
    private final Position pos;
    private TileType type;
    private Player owner;
    private Building building;
    private Unit unit;

    public Tile(Position pos, TileType type) {
        this.pos = pos;
        this.type = type;
    }

    public Position getPos() { return pos; }
    public TileType getType() { return type; }
    public void setType(TileType type) { this.type = type; }

    public Player getOwner() { return owner; }
    public void setOwner(Player owner) { this.owner = owner; }

    public Building getBuilding() { return building; }
    public void setBuilding(Building building) { this.building = building; }

    public Unit getUnit() { return unit; }
    public void setUnit(Unit unit) { this.unit = unit; }
}
