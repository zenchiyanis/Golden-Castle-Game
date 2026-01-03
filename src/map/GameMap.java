package map;

public class GameMap {
    private final int width;
    private final int height;
    private final Tile[][] tiles;

    public GameMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new Tile[height][width];
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public Tile getTile(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) return null;
        return tiles[y][x];
    }

    public void setTile(int x, int y, Tile t) {
        tiles[y][x] = t;
    }
}
