package player;

import buildings.Building;
import units.Unit;

import java.util.ArrayList;
import java.util.List;

public class Inventory {
    private final List<Unit> units = new ArrayList<>();
    private final List<Building> buildings = new ArrayList<>();

    public List<Unit> getUnits() { return units; }
    public List<Building> getBuildings() { return buildings; }
}
