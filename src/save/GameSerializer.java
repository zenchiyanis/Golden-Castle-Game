package save;

import buildings.BuildingType;
import map.TileType;
import resources.ResourceType;
import units.UnitType;

import java.util.EnumMap;
import java.util.StringTokenizer;

public class GameSerializer {
    public String serialize(SaveData d) {
        StringBuilder sb = new StringBuilder();

        sb.append("W ").append(d.width).append(" ").append(d.height).append("\n");
        sb.append("T ").append(d.turn).append(" ").append(d.humanTurn ? 1 : 0).append("\n");

        sb.append("R H ")
                .append(val(d.humanRes, ResourceType.GOLD)).append(" ")
                .append(val(d.humanRes, ResourceType.WOOD)).append(" ")
                .append(val(d.humanRes, ResourceType.STONE)).append(" ")
                .append(val(d.humanRes, ResourceType.FOOD)).append("\n");

        sb.append("R E ")
                .append(val(d.enemyRes, ResourceType.GOLD)).append(" ")
                .append(val(d.enemyRes, ResourceType.WOOD)).append(" ")
                .append(val(d.enemyRes, ResourceType.STONE)).append(" ")
                .append(val(d.enemyRes, ResourceType.FOOD)).append("\n");

        sb.append("MAP ");
        for (int i = 0; i < d.tiles.length; i++) {
            sb.append(d.tiles[i].name());
            if (i + 1 < d.tiles.length) sb.append(",");
        }
        sb.append("\n");

        sb.append("B ").append(d.buildings.size()).append("\n");
        for (var b : d.buildings) {
            sb.append(b.human ? "H" : "E").append(" ")
                    .append(b.type.name()).append(" ")
                    .append(b.x).append(" ").append(b.y).append(" ")
                    .append(b.hits).append("\n");
        }

        sb.append("U ").append(d.units.size()).append("\n");
        for (var u : d.units) {
            sb.append(u.human ? "H" : "E").append(" ")
                    .append(u.type.name()).append(" ")
                    .append(u.x).append(" ").append(u.y).append(" ")
                    .append(u.hits).append("\n");
        }

        return sb.toString();
    }

    private int val(EnumMap<ResourceType, Integer> m, ResourceType t) {
        return m.getOrDefault(t, 0);
    }

    public SaveData deserialize(String raw) {
        SaveData d = new SaveData();
        String[] lines = raw.split("\n");

        int idx = 0;

        {
            StringTokenizer st = new StringTokenizer(lines[idx++]);
            st.nextToken();
            d.width = Integer.parseInt(st.nextToken());
            d.height = Integer.parseInt(st.nextToken());
        }

        {
            StringTokenizer st = new StringTokenizer(lines[idx++]);
            st.nextToken();
            d.turn = Integer.parseInt(st.nextToken());
            d.humanTurn = Integer.parseInt(st.nextToken()) == 1;
        }

        {
            StringTokenizer st = new StringTokenizer(lines[idx++]);
            st.nextToken();
            st.nextToken();
            d.humanRes.put(ResourceType.GOLD, Integer.parseInt(st.nextToken()));
            d.humanRes.put(ResourceType.WOOD, Integer.parseInt(st.nextToken()));
            d.humanRes.put(ResourceType.STONE, Integer.parseInt(st.nextToken()));
            d.humanRes.put(ResourceType.FOOD, Integer.parseInt(st.nextToken()));
        }

        {
            StringTokenizer st = new StringTokenizer(lines[idx++]);
            st.nextToken();
            st.nextToken();
            d.enemyRes.put(ResourceType.GOLD, Integer.parseInt(st.nextToken()));
            d.enemyRes.put(ResourceType.WOOD, Integer.parseInt(st.nextToken()));
            d.enemyRes.put(ResourceType.STONE, Integer.parseInt(st.nextToken()));
            d.enemyRes.put(ResourceType.FOOD, Integer.parseInt(st.nextToken()));
        }

        {
            String line = lines[idx++];
            String mapPart = line.substring("MAP ".length());
            String[] parts = mapPart.split(",");
            d.tiles = new TileType[parts.length];
            for (int i = 0; i < parts.length; i++) d.tiles[i] = TileType.valueOf(parts[i]);
        }

        int bCount;
        {
            StringTokenizer st = new StringTokenizer(lines[idx++]);
            st.nextToken();
            bCount = Integer.parseInt(st.nextToken());
        }
        for (int i = 0; i < bCount; i++) {
            StringTokenizer st = new StringTokenizer(lines[idx++]);
            SaveData.BuildingRec br = new SaveData.BuildingRec();
            br.human = st.nextToken().equals("H");
            br.type = BuildingType.valueOf(st.nextToken());
            br.x = Integer.parseInt(st.nextToken());
            br.y = Integer.parseInt(st.nextToken());
            br.hits = Integer.parseInt(st.nextToken());
            d.buildings.add(br);
        }

        int uCount;
        {
            StringTokenizer st = new StringTokenizer(lines[idx++]);
            st.nextToken();
            uCount = Integer.parseInt(st.nextToken());
        }
        for (int i = 0; i < uCount; i++) {
            StringTokenizer st = new StringTokenizer(lines[idx++]);
            SaveData.UnitRec ur = new SaveData.UnitRec();
            ur.human = st.nextToken().equals("H");
            ur.type = UnitType.valueOf(st.nextToken());
            ur.x = Integer.parseInt(st.nextToken());
            ur.y = Integer.parseInt(st.nextToken());
            ur.hits = Integer.parseInt(st.nextToken());
            d.units.add(ur);
        }

        return d;
    }
}
