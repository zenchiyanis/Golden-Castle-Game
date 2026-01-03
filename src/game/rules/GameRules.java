package game.rules;

public class GameRules {
    private final int maxUnitsPerPlayer;

    public GameRules(int maxUnitsPerPlayer) {
        this.maxUnitsPerPlayer = maxUnitsPerPlayer;
    }

    public int getMaxUnitsPerPlayer() { return maxUnitsPerPlayer; }
}
