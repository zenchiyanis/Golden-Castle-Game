package units;

import player.Player;

public class UnitFactory {
    public Unit create(UnitType type, Player owner) {
        return switch (type) {
            case SOLDIER -> new Soldier(owner);
            case ARCHER -> new Archer(owner);
            case CAVALRY -> new Cavalry(owner);
        };
    }
}
