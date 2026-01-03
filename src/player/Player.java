package player;

import buildings.Building;
import resources.ResourceStock;
import units.Unit;

import java.util.List;

public class Player {
    private final String name;
    private final Faction faction;
    private final ResourceStock resources;
    private final Inventory inventory = new Inventory();
    private final TerritoryManager territory = new TerritoryManager();

    public Player(String name, Faction faction, ResourceStock resources) {
        this.name = name;
        this.faction = faction;
        this.resources = resources;
    }

    public String getName() { return name; }
    public Faction getFaction() { return faction; }
    public ResourceStock getResources() { return resources; }

    public List<Unit> getUnits() { return inventory.getUnits(); }
    public List<Building> getBuildings() { return inventory.getBuildings(); }

    public TerritoryManager getTerritory() { return territory; }

    public void addUnit(Unit u) { inventory.getUnits().add(u); }
    public void removeUnit(Unit u) { inventory.getUnits().remove(u); }

    public void addBuilding(Building b) { inventory.getBuildings().add(b); }
    public void removeBuilding(Building b) { inventory.getBuildings().remove(b); }
}
