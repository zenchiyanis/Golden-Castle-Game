package map.generator;

import map.GameMap;
import map.Position;
import map.Tile;
import map.TileType;

public class StaticMapGenerator implements MapGenerator {
    private final int w;
    private final int h;

    public StaticMapGenerator(int w, int h) {
        this.w = w;
        this.h = h;
    }

    @Override
    public GameMap generate() {
        GameMap map = new GameMap(w, h);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                TileType t = TileType.GRASS;
                if (y == 0 || x == 0 || y == h - 1 || x == w - 1) t = TileType.WATER;
                map.setTile(x, y, new Tile(new Position(x, y), t));
            }
        }
        return map;
    }
}
