package combat;

import buildings.Building;
import map.Position;
import units.Unit;
import units.UnitType;

public class CombatService {
    private final DamageCalculator calc;

    public CombatService(DamageCalculator calc) {
        this.calc = calc;
    }

    public boolean inRange(Unit a, Position target) {
        Position p1 = a.getPos();
        if (p1 == null || target == null) return false;
        int dx = Math.abs(p1.x - target.x);
        int dy = Math.abs(p1.y - target.y);
        return dx + dy <= a.getRange();
    }

    public void attack(Unit attacker, Unit defender) {
        int dmg = calc.compute(attacker);
        if (attacker.getType() == UnitType.CAVALRY) dmg = 9999;
        defender.setHitsRemaining(defender.getHitsRemaining() - dmg);
    }

    public void attack(Unit attacker, Building building) {
        int dmg = calc.compute(attacker);
        if (attacker.getType() == UnitType.CAVALRY) dmg = 9999;
        building.setHitsRemaining(building.getHitsRemaining() - dmg);
    }
}
