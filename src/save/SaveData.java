package save;

import buildings.BuildingType;
import map.TileType;
import resources.ResourceType;
import units.UnitType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class SaveData {
    public int width;
    public int height;

    public TileType[] tiles;

    public int turn;
    public boolean humanTurn;

    public EnumMap<ResourceType, Integer> humanRes = new EnumMap<>(ResourceType.class);
    public EnumMap<ResourceType, Integer> enemyRes = new EnumMap<>(ResourceType.class);

    public static class BuildingRec {
        public boolean human;
        public BuildingType type;
        public int x;
        public int y;
        public int hits;
    }

    public static class UnitRec {
        public boolean human;
        public UnitType type;
        public int x;
        public int y;
        public int hits;
    }

    public List<BuildingRec> buildings = new ArrayList<>();
    public List<UnitRec> units = new ArrayList<>();
}
