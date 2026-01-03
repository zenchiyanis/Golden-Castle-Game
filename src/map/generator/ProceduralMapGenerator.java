package map.generator;

import map.GameMap;
import map.Position;
import map.Tile;
import map.TileType;

import java.util.Random;

public class ProceduralMapGenerator implements MapGenerator {
    private final int w;
    private final int h;
    private final Random rng = new Random();

    public ProceduralMapGenerator(int w, int h) {
        this.w = w;
        this.h = h;
    }

    @Override
    public GameMap generate() {
        GameMap map = new GameMap(w, h);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                map.setTile(x, y, new Tile(new Position(x, y), rollType()));
            }
        }
        return map;
    }

    private TileType rollType() {
        int r = rng.nextInt(100);
        if (r < 65) return TileType.GRASS;
        if (r < 80) return TileType.FOREST;
        if (r < 92) return TileType.WATER;
        return TileType.MOUNTAIN;
    }
}
