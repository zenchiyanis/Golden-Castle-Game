package game.rules;

import player.Player;

public class WinLoseRules {
    public boolean isDefeated(Player p) {
        return p.getUnits().isEmpty() && p.getBuildings().isEmpty();
    }
}
