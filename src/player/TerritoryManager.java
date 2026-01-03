package player;

import map.Position;

import java.util.HashSet;
import java.util.Set;

public class TerritoryManager {
    private final Set<Position> owned = new HashSet<>();

    public void claim(Position p) { owned.add(p); }
    public boolean owns(Position p) { return owned.contains(p); }
    public Set<Position> snapshot() { return new HashSet<>(owned); }
}
