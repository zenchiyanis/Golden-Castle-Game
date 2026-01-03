package game;

import ai.SimpleAI;
import buildings.*;
import combat.CombatService;
import combat.DamageCalculator;
import map.GameMap;
import map.Position;
import map.Tile;
import map.TileType;
import map.generator.ProceduralMapGenerator;
import player.Faction;
import player.Player;
import resources.EconomyService;
import resources.ResourceStock;
import save.SaveData;
import save.SaveManager;
import ui.GameUI;
import units.Unit;
import units.UnitType;

public class Game {
    private final GameUI ui;

    private GameState state = GameState.MENU;
    private TurnManager tm;
    private GameContext ctx;

    private Building pendingBuild;

    private final SaveManager saveManager = new SaveManager();

    public Game(GameUI ui) {
        this.ui = ui;
    }

    public void startNewGame() {
        tm = new TurnManager();

        // 16x8 map
        GameMap map = new ProceduralMapGenerator(16, 8).generate();
        forceCornersGrass(map);

        Player human = new Player("You", Faction.HUMAN, new ResourceStock(GameContext.initialResources()));
        Player enemy = new Player("Enemy", Faction.ENEMY, new ResourceStock(GameContext.initialResources()));

        EconomyService economy = new EconomyService();
        CombatService combat = new CombatService(new DamageCalculator());

        ctx = new GameContext(map, human, enemy, economy, combat, new SimpleAI());
        placeStartingBuildings();

        ui.bindTurnManager(tm);
        ui.bindContext(ctx);

        pendingBuild = null;

        state = GameState.RUNNING;
        ui.showGame();
        ui.notifyEvent("New game started");
        ui.render();
    }

    public void loadLastSave() {
        SaveData d = saveManager.loadFromDisk();
        if (d == null) {
            startNewGame();
            ui.notifyEvent("No save found. Started new game.");
            ui.render();
            return;
        }

        tm = new TurnManager();
        while (tm.getTurn() < d.turn) tm.next();
        if (tm.isHumanTurn() != d.humanTurn) tm.next();

        GameMap map = new GameMap(d.width, d.height);
        int k = 0;
        for (int y = 0; y < d.height; y++) {
            for (int x = 0; x < d.width; x++) {
                map.setTile(x, y, new Tile(new Position(x, y), d.tiles[k++]));
            }
        }

        Player human = new Player("You", Faction.HUMAN, new ResourceStock(d.humanRes));
        Player enemy = new Player("Enemy", Faction.ENEMY, new ResourceStock(d.enemyRes));

        EconomyService economy = new EconomyService();
        CombatService combat = new CombatService(new DamageCalculator());

        ctx = new GameContext(map, human, enemy, economy, combat, new SimpleAI());

        for (var br : d.buildings) {
            Player owner = br.human ? human : enemy;
            Building b = switch (br.type) {
                case CASTLE -> new Castle(owner);
                case BARRACKS -> new Barracks(owner);
                case FARM -> new Farm(owner);
                case MINE -> new Mine(owner);
            };
            b.setHitsRemaining(br.hits);
            ctx.placeBuilding(b, new Position(br.x, br.y), owner);
        }

        for (var ur : d.units) {
            Player owner = ur.human ? human : enemy;
            Unit u = ctx.createUnit(ur.type, owner);
            u.setHitsRemaining(ur.hits);

            Tile t = map.getTile(ur.x, ur.y);
            if (t != null && t.getUnit() == null && t.getBuilding() == null && t.getType().isAccessible()) {
                t.setUnit(u);
                u.setPos(new Position(ur.x, ur.y));
                owner.addUnit(u);
            }
        }

        pendingBuild = null;
        state = GameState.RUNNING;

        ui.bindTurnManager(tm);
        ui.bindContext(ctx);

        ui.showGame();
        ui.notifyEvent("Loaded save");
        ui.render();
    }

    public void saveNow() {
        if (ctx == null || tm == null) return;

        SaveData d = new SaveData();
        d.width = ctx.getMap().getWidth();
        d.height = ctx.getMap().getHeight();

        d.turn = tm.getTurn();
        d.humanTurn = tm.isHumanTurn();

        d.humanRes.putAll(ctx.getHuman().getResources().snapshot());
        d.enemyRes.putAll(ctx.getEnemy().getResources().snapshot());

        d.tiles = new TileType[d.width * d.height];
        int k = 0;
        for (int y = 0; y < d.height; y++) {
            for (int x = 0; x < d.width; x++) {
                d.tiles[k++] = ctx.getMap().getTile(x, y).getType();
            }
        }

        for (var b : ctx.getHuman().getBuildings()) {
            if (b.getPosition() == null) continue;
            SaveData.BuildingRec br = new SaveData.BuildingRec();
            br.human = true;
            br.type = b.getType();
            br.x = b.getPosition().x;
            br.y = b.getPosition().y;
            br.hits = b.getHitsRemaining();
            d.buildings.add(br);
        }

        for (var b : ctx.getEnemy().getBuildings()) {
            if (b.getPosition() == null) continue;
            SaveData.BuildingRec br = new SaveData.BuildingRec();
            br.human = false;
            br.type = b.getType();
            br.x = b.getPosition().x;
            br.y = b.getPosition().y;
            br.hits = b.getHitsRemaining();
            d.buildings.add(br);
        }

        for (var u : ctx.getHuman().getUnits()) {
            if (u.getPos() == null) continue;
            SaveData.UnitRec ur = new SaveData.UnitRec();
            ur.human = true;
            ur.type = u.getType();
            ur.x = u.getPos().x;
            ur.y = u.getPos().y;
            ur.hits = u.getHitsRemaining();
            d.units.add(ur);
        }

        for (var u : ctx.getEnemy().getUnits()) {
            if (u.getPos() == null) continue;
            SaveData.UnitRec ur = new SaveData.UnitRec();
            ur.human = false;
            ur.type = u.getType();
            ur.x = u.getPos().x;
            ur.y = u.getPos().y;
            ur.hits = u.getHitsRemaining();
            d.units.add(ur);
        }

        saveManager.saveToDisk(d);
        ui.notifyEvent("Saved");
        ui.render();
    }

    public void goToMainMenu() {
        state = GameState.MENU;
        pendingBuild = null;
        ui.showMainMenu();
        ui.render();
    }

    private boolean isHumanTurnRunning() {
        return state == GameState.RUNNING && tm != null && tm.isHumanTurn() && ctx != null;
    }

    private void forceCornersGrass(GameMap map) {
        int w = map.getWidth(), h = map.getHeight();
        for (int y = 0; y < 4; y++) for (int x = 0; x < 4; x++) map.getTile(x, y).setType(TileType.GRASS);
        for (int y = h - 4; y < h; y++) for (int x = w - 4; x < w; x++) map.getTile(x, y).setType(TileType.GRASS);
    }

    private void placeStartingBuildings() {
        int w = ctx.getMap().getWidth();
        int h = ctx.getMap().getHeight();

        Position humanCastle = new Position(w - 2, h - 2);
        Position humanBarr = new Position(w - 4, h - 2);

        Position enemyCastle = new Position(0, 0);
        Position enemyBarr = new Position(2, 0);

        ctx.placeBuilding(new Castle(ctx.getHuman()), humanCastle, ctx.getHuman());
        ctx.placeBuilding(new Barracks(ctx.getHuman()), humanBarr, ctx.getHuman());

        ctx.placeBuilding(new Castle(ctx.getEnemy()), enemyCastle, ctx.getEnemy());
        ctx.placeBuilding(new Barracks(ctx.getEnemy()), enemyBarr, ctx.getEnemy());
    }

    public Building getPendingBuild() { return pendingBuild; }

    public boolean moveUnit(Position from, Position to) {
        if (!isHumanTurnRunning()) return false;

        Unit u = ctx.unitAt(from);
        if (u == null || u.getOwner() != ctx.getHuman()) return false;

        int dist = Math.abs(to.x - from.x) + Math.abs(to.y - from.y);
        int max = (u.getType() == UnitType.CAVALRY) ? 2 : 1;
        if (dist < 1 || dist > max) return false;

        boolean ok = ctx.moveUnit(ctx.getHuman(), from, to);
        if (ok) endHumanTurn();
        ui.render();
        return ok;
    }

    public boolean canAttackTarget(Position from, Position target) {
        if (!isHumanTurnRunning()) return false;
        return ctx.canAttackTarget(ctx.getHuman(), from, target);
    }

    public void attackTarget(Position from, Position target) {
        if (!canAttackTarget(from, target)) return;
        ctx.attackTargetAndMoveIfKilled(ctx.getHuman(), from, target);
        checkCastleWinLose();
        endHumanTurn();
        ui.render();
    }

    public boolean canCollectTarget(Position from, Position target) {
        if (!isHumanTurnRunning()) return false;
        return ctx.canCollectTarget(from, target);
    }

    public void collectTarget(Position from, Position target) {
        if (!canCollectTarget(from, target)) return;
        ctx.collectTargetAndMove(from, target);
        endHumanTurn();
        ui.render();
    }

    public void trainSoldier() { train(UnitType.SOLDIER); }
    public void trainArcher() { train(UnitType.ARCHER); }
    public void trainCavalry() { train(UnitType.CAVALRY); }

    private void train(UnitType type) {
        if (!isHumanTurnRunning()) return;

        Position spawn = ctx.findBarracksSpawn(ctx.getHuman(), true);
        if (spawn == null) {
            ui.notifyEvent("No spawn space near Barracks");
            ui.render();
            return; // no end turn
        }

        Unit u = ctx.createUnit(type, ctx.getHuman());
        if (!ctx.getEconomy().trySpend(ctx.getHuman(), u.getCost())) {
            ui.notifyEvent("Not enough resources to train " + type);
            ui.render();
            return; // no end turn
        }

        Tile t = ctx.getMap().getTile(spawn.x, spawn.y);
        if (t == null || t.getUnit() != null || t.getBuilding() != null || !t.getType().isAccessible()) {
            ui.notifyEvent("Invalid spawn tile");
            ui.render();
            return; // no end turn
        }

        t.setUnit(u);
        u.setPos(spawn);
        ctx.getHuman().addUnit(u);

        ui.notifyEvent("Trained " + type);
        endHumanTurn();
        ui.render();
    }

    public void buildFarm() { beginBuild(BuildingType.FARM); }
    public void buildMine() { beginBuild(BuildingType.MINE); }
    public void buildBarracks() { beginBuild(BuildingType.BARRACKS); }

    private void beginBuild(BuildingType type) {
        if (!isHumanTurnRunning()) return;
        if (pendingBuild != null) return;

        int farms = countBuildings(ctx.getHuman(), BuildingType.FARM);
        int mines = countBuildings(ctx.getHuman(), BuildingType.MINE);
        int barr = countBuildings(ctx.getHuman(), BuildingType.BARRACKS);

        if (type == BuildingType.FARM && farms >= 1) {
            ui.notifyEvent("You already have a Farm");
            ui.render();
            return;
        }
        if (type == BuildingType.MINE && mines >= 1) {
            ui.notifyEvent("You already have a Mine");
            ui.render();
            return;
        }
        if (type == BuildingType.BARRACKS && barr >= 2) {
            ui.notifyEvent("Max Barracks reached");
            ui.render();
            return;
        }

        pendingBuild = switch (type) {
            case FARM -> new Farm(ctx.getHuman());
            case MINE -> new Mine(ctx.getHuman());
            case BARRACKS -> new Barracks(ctx.getHuman());
            default -> null;
        };
        if (pendingBuild == null) return;

        ui.requestBuildPlacement(pendingBuild);
        ui.notifyEvent("Click a 2x2 empty GRASS area");
        ui.render();
    }

    public void placePendingBuilding(Position topLeft) {
        if (!isHumanTurnRunning()) return;
        if (pendingBuild == null) return;

        if (!ctx.canPlaceBuilding(pendingBuild, topLeft, ctx.getHuman())) {
            ui.notifyEvent("Invalid place (need 2x2 GRASS empty)");
            ui.render();
            pendingBuild = null; // cancel build so player can do other actions
            return;
        }

        if (!ctx.getEconomy().trySpend(ctx.getHuman(), pendingBuild.getCost())) {
            ui.notifyEvent("Not enough resources to build");
            ui.render();
            pendingBuild = null; // cancel build so player can do other actions
            return;
        }

        ctx.placeBuilding(pendingBuild, topLeft, ctx.getHuman());
        pendingBuild = null;

        endHumanTurn();
        ui.render();
    }

    private int countBuildings(Player p, BuildingType type) {
        int c = 0;
        for (Building b : p.getBuildings()) if (b.getType() == type) c++;
        return c;
    }

    private void endHumanTurn() {
        if (state != GameState.RUNNING) return;

        ctx.grantAutoBuildingIncome(ctx.getHuman());

        tm.next();
        ui.render();

        ctx.getAI().playTurn(ctx);

        ctx.grantAutoBuildingIncome(ctx.getEnemy());
        checkCastleWinLose();

        tm.next();
        ui.render();
    }

    private void checkCastleWinLose() {
        boolean enemyCastleAlive = ctx.findBuildingTopLeft(ctx.getEnemy(), BuildingType.CASTLE) != null;
        boolean humanCastleAlive = ctx.findBuildingTopLeft(ctx.getHuman(), BuildingType.CASTLE) != null;

        if (!enemyCastleAlive) {
            state = GameState.GAME_OVER;
            ui.showVictory();
        } else if (!humanCastleAlive) {
            state = GameState.GAME_OVER;
            ui.showDefeat();
        }
    }
}
