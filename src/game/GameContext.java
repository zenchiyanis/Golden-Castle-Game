package game;

import ai.AIController;
import buildings.*;
import combat.CombatService;
import map.GameMap;
import map.Position;
import map.Tile;
import map.TileType;
import player.Player;
import resources.EconomyService;
import resources.ResourceType;
import units.Unit;
import units.UnitFactory;
import units.UnitType;

import java.util.EnumMap;

public class GameContext {
    private final GameMap map;
    private final Player human;
    private final Player enemy;
    private final EconomyService economy;
    private final CombatService combat;
    private final AIController ai;

    public GameContext(GameMap map, Player human, Player enemy, EconomyService economy, CombatService combat, AIController ai) {
        this.map = map;
        this.human = human;
        this.enemy = enemy;
        this.economy = economy;
        this.combat = combat;
        this.ai = ai;
    }

    public GameMap getMap() { return map; }
    public Player getHuman() { return human; }
    public Player getEnemy() { return enemy; }
    public EconomyService getEconomy() { return economy; }
    public CombatService getCombat() { return combat; }
    public AIController getAI() { return ai; }

    public static EnumMap<ResourceType, Integer> initialResources() {
        EnumMap<ResourceType, Integer> m = new EnumMap<>(ResourceType.class);
        m.put(ResourceType.GOLD, 65);
        m.put(ResourceType.FOOD, 63);
        m.put(ResourceType.WOOD, 10);
        m.put(ResourceType.STONE, 0);
        return m;
    }

    public Unit createUnit(UnitType type, Player owner) {
        return new UnitFactory().create(type, owner);
    }

    public Unit unitAt(Position p) {
        Tile t = map.getTile(p.x, p.y);
        return t == null ? null : t.getUnit();
    }

    public boolean isFreeForUnit(Position p, boolean big) {
        Tile t = map.getTile(p.x, p.y);
        if (t == null) return false;
        if (!t.getType().isAccessible()) return false;
        if (t.getBuilding() != null) return false;
        if (t.getUnit() != null) return false;
        return true;
    }

    public boolean moveUnit(Player mover, Position from, Position to) {
        Tile a = map.getTile(from.x, from.y);
        Tile b = map.getTile(to.x, to.y);
        if (a == null || b == null) return false;
        Unit u = a.getUnit();
        if (u == null || u.getOwner() != mover) return false;
        if (!b.getType().isAccessible()) return false;
        if (b.getBuilding() != null) return false;
        if (b.getUnit() != null) return false;

        a.setUnit(null);
        b.setUnit(u);
        u.setPos(to);
        return true;
    }

    public Position findBuildingTopLeft(Player p, BuildingType type) {
        for (Building b : p.getBuildings()) {
            if (b.getType() == type && b.getPosition() != null) return b.getPosition();
        }
        return null;
    }

    public Position findBarracksSpawn(Player p, boolean humanSide) {
        Position tl = findBuildingTopLeft(p, BuildingType.BARRACKS);
        if (tl == null) return null;

        Position spawn = humanSide ? new Position(tl.x, tl.y - 1) : new Position(tl.x, tl.y + 2);
        if (isFreeForUnit(spawn, false)) return spawn;

        Position alt = humanSide ? new Position(tl.x + 1, tl.y - 1) : new Position(tl.x + 1, tl.y + 2);
        if (isFreeForUnit(alt, false)) return alt;

        return null;
    }

    public boolean canPlaceBuilding(Building b, Position topLeft, Player p) {
        if (b == null || topLeft == null) return false;
        for (int yy = topLeft.y; yy < topLeft.y + 2; yy++) {
            for (int xx = topLeft.x; xx < topLeft.x + 2; xx++) {
                Tile t = map.getTile(xx, yy);
                if (t == null) return false;
                if (t.getType() != TileType.GRASS) return false;
                if (t.getBuilding() != null) return false;
                if (t.getUnit() != null) return false;
            }
        }
        return true;
    }

    public void placeBuilding(Building b, Position topLeft, Player p) {
        b.setPosition(topLeft);
        p.addBuilding(b);
        for (int yy = topLeft.y; yy < topLeft.y + 2; yy++) {
            for (int xx = topLeft.x; xx < topLeft.x + 2; xx++) {
                Tile t = map.getTile(xx, yy);
                t.setBuilding(b);
                t.setOwner(p);
            }
        }
    }

    public boolean canAttackTarget(Player attackerOwner, Position from, Position target) {
        Tile a = map.getTile(from.x, from.y);
        Tile t = map.getTile(target.x, target.y);
        if (a == null || t == null) return false;

        Unit attacker = a.getUnit();
        if (attacker == null || attacker.getOwner() != attackerOwner) return false;
        if (!combat.inRange(attacker, target)) return false;

        if (t.getUnit() != null && t.getUnit().getOwner() != attackerOwner) return true;
        if (t.getBuilding() != null && t.getBuilding().getOwner() != attackerOwner) return true;

        return false;
    }

    public void attackTargetAndMoveIfKilled(Player attackerOwner, Position from, Position target) {
        Tile a = map.getTile(from.x, from.y);
        Tile t = map.getTile(target.x, target.y);
        if (a == null || t == null) return;

        Unit attacker = a.getUnit();
        if (attacker == null || attacker.getOwner() != attackerOwner) return;

        if (t.getUnit() != null && t.getUnit().getOwner() != attackerOwner) {
            Unit def = t.getUnit();
            combat.attack(attacker, def);
            if (def.isDead()) {
                def.getOwner().removeUnit(def);
                t.setUnit(null);
                moveUnit(attackerOwner, from, target);
            }
            return;
        }

        if (t.getBuilding() != null && t.getBuilding().getOwner() != attackerOwner) {
            Building b = t.getBuilding();
            combat.attack(attacker, b);
            if (b.isDestroyed()) {
                b.getOwner().removeBuilding(b);
                clearBuildingTiles(b);
                moveUnit(attackerOwner, from, target);
            }
        }
    }

    private void clearBuildingTiles(Building b) {
        Position tl = b.getPosition();
        if (tl == null) return;
        for (int yy = tl.y; yy < tl.y + 2; yy++) {
            for (int xx = tl.x; xx < tl.x + 2; xx++) {
                Tile t = map.getTile(xx, yy);
                if (t != null && t.getBuilding() == b) t.setBuilding(null);
            }
        }
    }

    public boolean canCollectTarget(Position from, Position target) {
        Tile a = map.getTile(from.x, from.y);
        Tile t = map.getTile(target.x, target.y);
        if (a == null || t == null) return false;

        Unit u = a.getUnit();
        if (u == null) return false;

        int dist = Math.abs(from.x - target.x) + Math.abs(from.y - target.y);
        int allowed = (u.getType() == UnitType.ARCHER) ? 2 : 1;
        if (dist > allowed || dist == 0) return false;

        TileType tt = t.getType();
        return tt == TileType.FOREST || tt == TileType.MOUNTAIN || tt == TileType.WATER;
    }

    public boolean collectTargetAndMove(Position from, Position target) {
        Tile a = map.getTile(from.x, from.y);
        Tile t = map.getTile(target.x, target.y);
        if (a == null || t == null) return false;

        Unit u = a.getUnit();
        if (u == null) return false;

        if (!canCollectTarget(from, target)) return false;
        if (t.getBuilding() != null) return false;
        if (t.getUnit() != null) return false;

        TileType tt = t.getType();

        if (tt == TileType.FOREST)
            economy.grant(u.getOwner(), ResourceType.WOOD, 30);
        else if (tt == TileType.MOUNTAIN)
            economy.grant(u.getOwner(), ResourceType.STONE, 30);
        else if (tt == TileType.WATER)
            economy.grant(u.getOwner(), ResourceType.FOOD, 30);
        else
            return false;

        t.setType(TileType.GRASS);

        a.setUnit(null);
        t.setUnit(u);
        u.setPos(target);

        return true;
    }

    public void grantAutoBuildingIncome(Player p) {
        int farms = 0;
        int mines = 0;

        for (Building b : p.getBuildings()) {
            if (b.getType() == BuildingType.FARM) farms++;
            if (b.getType() == BuildingType.MINE) mines++;
        }

        if (farms > 0) economy.grant(p, ResourceType.FOOD, farms * 10);
        if (mines > 0) economy.grant(p, ResourceType.GOLD, mines * 10);
    }
}
