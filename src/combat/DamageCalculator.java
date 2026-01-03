package combat;

import units.Unit;

public class DamageCalculator {
    public int compute(Unit attacker) {
        return Math.max(1, attacker.hitPower());
    }
}
