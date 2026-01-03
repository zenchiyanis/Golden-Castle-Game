package map;

public enum TileType {
    GRASS(true),
    WATER(false),
    MOUNTAIN(false),
    FOREST(false);

    private final boolean accessible;

    TileType(boolean accessible) {
        this.accessible = accessible;
    }

    public boolean isAccessible() {
        return accessible;
    }
}
