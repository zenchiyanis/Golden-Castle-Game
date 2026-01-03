package ai;

import buildings.BuildingType;
import game.GameContext;
import map.GameMap;
import map.Position;
import map.Tile;
import map.TileType;
import player.Player;
import units.Unit;
import units.UnitType;

import java.util.*;

public class SimpleAI implements AIController {
    private final Random rng = new Random();

    @Override
    public void playTurn(GameContext ctx) {
        Player enemy = ctx.getEnemy();
        Player human = ctx.getHuman();

        // ONE action per AI turn: build OR train OR unit-action
        if (tryBuild(ctx, enemy)) return;
        if (tryTrain(ctx, enemy)) return;

        if (enemy.getUnits().isEmpty()) return;

        Unit u = enemy.getUnits().get(0);
        Position from = u.getPos();
        if (from == null) return;

        // Try collect as the single action
        Position collectTarget = findCollectTarget(ctx, from);
        if (collectTarget != null) {
            if (ctx.collectTargetAndMove(from, collectTarget)) return;
        }

        // Try attack as the single action
        Position attackTarget = findAttackTarget(ctx, enemy, from);
        if (attackTarget != null) {
            ctx.attackTargetAndMoveIfKilled(enemy, from, attackTarget);
            return;
        }

        // Otherwise move as the single action
        Position humanCastle = ctx.findBuildingTopLeft(human, BuildingType.CASTLE);
        if (humanCastle == null) return;

        int steps = (u.getType() == UnitType.CAVALRY) ? 2 : 1;
        Position step = nextStepTowardCastleWithBFS(ctx, from, humanCastle, steps);
        if (step != null) ctx.moveUnit(enemy, from, step);
    }

    private boolean tryTrain(GameContext ctx, Player enemy) {
        Position spawn = ctx.findBarracksSpawn(enemy, false);
        if (spawn == null) return false;

        UnitType pick = switch (rng.nextInt(3)) {
            case 0 -> UnitType.SOLDIER;
            case 1 -> UnitType.ARCHER;
            default -> UnitType.CAVALRY;
        };

        Unit u = ctx.createUnit(pick, enemy);

        // Only train if affordable and tile is free (single action)
        if (!ctx.getEconomy().trySpend(enemy, u.getCost())) return false;
        if (!ctx.isFreeForUnit(spawn, false)) return false;

        Tile t = ctx.getMap().getTile(spawn.x, spawn.y);
        if (t == null) return false;

        t.setUnit(u);
        u.setPos(spawn);
        enemy.addUnit(u);
        return true;
    }

    private boolean tryBuild(GameContext ctx, Player enemy) {
        int farms = count(enemy, BuildingType.FARM);
        int mines = count(enemy, BuildingType.MINE);
        int barr = count(enemy, BuildingType.BARRACKS);

        if (farms < 1) return attemptBuild(ctx, enemy, BuildingType.FARM);
        if (mines < 1) return attemptBuild(ctx, enemy, BuildingType.MINE);
        if (barr < 2) return attemptBuild(ctx, enemy, BuildingType.BARRACKS);

        return false;
    }

    private int count(Player p, BuildingType type) {
        int c = 0;
        for (var b : p.getBuildings()) if (b.getType() == type) c++;
        return c;
    }

    private boolean attemptBuild(GameContext ctx, Player enemy, BuildingType type) {
        var b = switch (type) {
            case FARM -> new buildings.Farm(enemy);
            case MINE -> new buildings.Mine(enemy);
            case BARRACKS -> new buildings.Barracks(enemy);
            default -> null;
        };
        if (b == null) return false;

        // Find a valid placement FIRST, then spend (prevents losing resources if no spot)
        Position spot = null;
        for (int y = 0; y < ctx.getMap().getHeight(); y++) {
            for (int x = 0; x < ctx.getMap().getWidth(); x++) {
                Position p = new Position(x, y);
                if (ctx.canPlaceBuilding(b, p, enemy)) {
                    spot = p;
                    break;
                }
            }
            if (spot != null) break;
        }
        if (spot == null) return false;

        if (!ctx.getEconomy().trySpend(enemy, b.getCost())) return false;

        ctx.placeBuilding(b, spot, enemy);
        return true;
    }

    private Position findCollectTarget(GameContext ctx, Position from) {
        Position best = null;
        int bestScore = Integer.MIN_VALUE;

        for (int y = 0; y < ctx.getMap().getHeight(); y++) {
            for (int x = 0; x < ctx.getMap().getWidth(); x++) {
                Position p = new Position(x, y);
                if (!ctx.canCollectTarget(from, p)) continue;

                TileType tt = ctx.getMap().getTile(x, y).getType();

                int score = switch (tt) {
                    case MOUNTAIN -> 30;
                    case FOREST -> 20;
                    case WATER -> 10;
                    default -> 0;
                };

                int d = Math.abs(x - from.x) + Math.abs(y - from.y);
                score -= d;

                if (score > bestScore) {
                    bestScore = score;
                    best = p;
                }
            }
        }
        return best;
    }

    private Position findAttackTarget(GameContext ctx, Player enemy, Position from) {
        Position best = null;
        int bestScore = Integer.MIN_VALUE;

        for (int y = 0; y < ctx.getMap().getHeight(); y++) {
            for (int x = 0; x < ctx.getMap().getWidth(); x++) {
                Position p = new Position(x, y);
                if (!ctx.canAttackTarget(enemy, from, p)) continue;

                Tile t = ctx.getMap().getTile(x, y);
                int score = 0;

                if (t.getBuilding() != null) {
                    score += 1000;
                    if (t.getBuilding().getType() == BuildingType.CASTLE) score += 10000;
                } else if (t.getUnit() != null) {
                    score += 500;
                }

                int d = Math.abs(x - from.x) + Math.abs(y - from.y);
                score -= d;

                if (score > bestScore) {
                    bestScore = score;
                    best = p;
                }
            }
        }
        return best;
    }

    private Position nextStepTowardCastleWithBFS(GameContext ctx, Position start, Position castleTopLeft, int maxSteps) {
        GameMap map = ctx.getMap();

        List<Position> goals = Arrays.asList(
                castleTopLeft,
                new Position(castleTopLeft.x + 1, castleTopLeft.y),
                new Position(castleTopLeft.x, castleTopLeft.y + 1),
                new Position(castleTopLeft.x + 1, castleTopLeft.y + 1)
        );

        Set<Position> goalAdj = new HashSet<>();
        for (Position g : goals) {
            if (map.getTile(g.x, g.y) != null) goalAdj.add(g);
            addIfInside(map, goalAdj, new Position(g.x + 1, g.y));
            addIfInside(map, goalAdj, new Position(g.x - 1, g.y));
            addIfInside(map, goalAdj, new Position(g.x, g.y + 1));
            addIfInside(map, goalAdj, new Position(g.x, g.y - 1));
        }

        BFSResult res = bfsToNearestGoal(ctx, start, goalAdj);

        if (res.reached != null) {
            Position step = firstStepFromStart(res.parent, start, res.reached);
            if (step == null) return null;
            if (maxSteps <= 1) return step;
            Position step2 = secondStepIfPossible(ctx, start, step, goalAdj);
            return (step2 != null) ? step2 : step;
        }

        if (res.bestNode != null && !res.bestNode.equals(start)) {
            Position step = firstStepFromStart(res.parent, start, res.bestNode);
            if (step == null) return null;
            if (maxSteps <= 1) return step;
            Position step2 = secondStepIfPossible(ctx, start, step, goalAdj);
            return (step2 != null) ? step2 : step;
        }

        return null;
    }

    private Position secondStepIfPossible(GameContext ctx, Position start, Position step1, Set<Position> goals) {
        BFSResult res2 = bfsToNearestGoal(ctx, step1, goals);
        if (res2.reached != null) {
            Position s2 = firstStepFromStart(res2.parent, step1, res2.reached);
            if (s2 != null && !s2.equals(step1) && !s2.equals(start)) return s2;
        }
        if (res2.bestNode != null && !res2.bestNode.equals(step1)) {
            Position s2 = firstStepFromStart(res2.parent, step1, res2.bestNode);
            if (s2 != null && !s2.equals(step1) && !s2.equals(start)) return s2;
        }
        return null;
    }

    private void addIfInside(GameMap map, Set<Position> set, Position p) {
        if (map.getTile(p.x, p.y) != null) set.add(p);
    }

    private static class BFSResult {
        Map<Position, Position> parent;
        Position reached;
        Position bestNode;
    }

    private BFSResult bfsToNearestGoal(GameContext ctx, Position start, Set<Position> goals) {
        GameMap map = ctx.getMap();

        ArrayDeque<Position> q = new ArrayDeque<>();
        Map<Position, Position> parent = new HashMap<>();
        Set<Position> visited = new HashSet<>();

        q.add(start);
        visited.add(start);
        parent.put(start, null);

        Position foundGoal = null;

        Position bestNode = start;
        int bestDist = minManhattanToGoals(start, goals);

        while (!q.isEmpty()) {
            Position cur = q.removeFirst();

            int dGoal = minManhattanToGoals(cur, goals);
            if (dGoal < bestDist) {
                bestDist = dGoal;
                bestNode = cur;
            }

            if (goals.contains(cur) && !cur.equals(start)) {
                foundGoal = cur;
                break;
            }

            for (Position nb : neighbors4(cur)) {
                if (visited.contains(nb)) continue;

                Tile t = map.getTile(nb.x, nb.y);
                if (t == null) continue;
                if (!t.getType().isAccessible()) continue;
                if (t.getBuilding() != null) continue;
                if (t.getUnit() != null) continue;

                visited.add(nb);
                parent.put(nb, cur);
                q.addLast(nb);
            }
        }

        BFSResult out = new BFSResult();
        out.parent = parent;
        out.reached = foundGoal;
        out.bestNode = bestNode;
        return out;
    }

    private int minManhattanToGoals(Position p, Set<Position> goals) {
        int best = Integer.MAX_VALUE;
        for (Position g : goals) {
            int d = Math.abs(p.x - g.x) + Math.abs(p.y - g.y);
            if (d < best) best = d;
        }
        return best;
    }

    private List<Position> neighbors4(Position p) {
        return Arrays.asList(
                new Position(p.x + 1, p.y),
                new Position(p.x - 1, p.y),
                new Position(p.x, p.y + 1),
                new Position(p.x, p.y - 1)
        );
    }

    private Position firstStepFromStart(Map<Position, Position> parent, Position start, Position goal) {
        Position step = goal;
        Position prev = parent.get(step);
        while (prev != null && !prev.equals(start)) {
            step = prev;
            prev = parent.get(step);
        }
        if (prev == null) return null;
        return step;
    }
}
