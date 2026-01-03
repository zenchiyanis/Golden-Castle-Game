package ui;

import buildings.Building;
import game.Game;
import game.GameContext;
import game.TurnManager;
import map.GameMap;
import map.Position;
import map.Tile;
import resources.ResourceType;
import units.UnitType;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.EnumMap;

public class SwingUI extends JFrame implements GameUI {
	private static final long serialVersionUID = 1L;

    private Game game;

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    private final JPanel menuScreen = new ImageScreenPanel("/assets/ui/menu_bg.png");
    private final JPanel victoryScreen = new ImageScreenPanel("/assets/ui/victory.png");
    private final JPanel defeatScreen = new ImageScreenPanel("/assets/ui/defeat.png");

    private final JPanel gameScreen = new JPanel(new BorderLayout());

    private TurnManager tm;
    private GameContext ctx;

    private final JLabel statusLabel = new JLabel(" ");

    private final JLabel turnLabel = new JLabel("Turn: -");
    private final JLabel goldLabel = new JLabel("Gold: 0");
    private final JLabel woodLabel = new JLabel("Wood: 0");
    private final JLabel stoneLabel = new JLabel("Stone: 0");
    private final JLabel foodLabel = new JLabel("Food: 0");

    private final MapPanel mapPanel = new MapPanel();

    private final JButton trainSoldierBtn = new PressButton("Train Soldier");
    private final JButton trainArcherBtn = new PressButton("Train Archer");
    private final JButton trainCavalryBtn = new PressButton("Train Cavalry");

    private final JButton buildFarmBtn = new PressButton("Build Farm");
    private final JButton buildMineBtn = new PressButton("Build Mine");
    private final JButton buildBarracksBtn = new PressButton("Build Barracks");

    private final JButton collectBtn = new PressButton("Collect");
    private final JButton attackBtn = new PressButton("Attack");

    private final JButton saveBtn = new PressButton("Save");
    private final JButton mainMenuBtn = new PressButton("Main Menu");

    private Position selected;

    private enum ActionMode { NONE, ATTACK, COLLECT, BUILD }
    private ActionMode mode = ActionMode.NONE;

    private Building pendingPlacement;

    public SwingUI() {
        super("Golden Castle");

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 720));
        setLocationRelativeTo(null);

        buildMenuScreen();
        buildVictoryScreen();
        buildDefeatScreen();
        buildGameScreen();

        root.add(menuScreen, "MENU");
        root.add(gameScreen, "GAME");
        root.add(victoryScreen, "VICTORY");
        root.add(defeatScreen, "DEFEAT");

        setContentPane(root);
        cards.show(root, "MENU");
    }

    public void attachGame(Game game) {
        this.game = game;
    }

    public void showWindow() {
        pack();
        setVisible(true);
    }

    @Override
    public void bindTurnManager(TurnManager tm) {
        this.tm = tm;
        updateTopBar();
        updateBottomBarButtons();
    }

    @Override
    public void bindContext(GameContext ctx) {
        this.ctx = ctx;
        mapPanel.setContext(ctx);
        selected = null;
        mode = ActionMode.NONE;
        pendingPlacement = null;
        updateTopBar();
        updateBottomBarButtons();
    }

    @Override
    public void showMainMenu() {
        cards.show(root, "MENU");
        repaint();
    }

    @Override
    public void showGame() {
        cards.show(root, "GAME");
        repaint();
    }

    @Override
    public void showVictory() {
        cards.show(root, "VICTORY");
        repaint();
    }

    @Override
    public void showDefeat() {
        cards.show(root, "DEFEAT");
        repaint();
    }

    @Override
    public void render() {
        updateTopBar();
        updateBottomBarButtons();
        mapPanel.repaint();
    }

    @Override
    public void notifyEvent(String msg) {
        statusLabel.setText(msg == null ? " " : msg);
    }

    @Override
    public boolean requestBuildPlacement(Building b) {
        pendingPlacement = b;
        mode = ActionMode.BUILD;
        selected = null;
        updateBottomBarButtons();
        mapPanel.repaint();
        return true;
    }

    private void buildMenuScreen() {
        menuScreen.setLayout(new GridBagLayout());

        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(new EmptyBorder(260, 0, 0, 0));

        AnimatedMenuButton newGame = new AnimatedMenuButton("NEW GAME", "START!", "GOOD LUCK!");
        AnimatedMenuButton load = new AnimatedMenuButton("LOAD", "START!", "GOOD LUCK!");
        AnimatedMenuButton quit = new AnimatedMenuButton("QUIT", "BYE!", "SEE YOU!");

        newGame.setAlignmentX(Component.CENTER_ALIGNMENT);
        load.setAlignmentX(Component.CENTER_ALIGNMENT);
        quit.setAlignmentX(Component.CENTER_ALIGNMENT);

        newGame.addActionListener(e -> {
            newGame.playStartAnimation();
            if (game != null) game.startNewGame();
        });

        load.addActionListener(e -> {
            load.playStartAnimation();
            if (game != null) game.loadLastSave();
        });

        quit.addActionListener(e -> System.exit(0));

        box.add(newGame);
        box.add(Box.createVerticalStrut(14));
        box.add(load);
        box.add(Box.createVerticalStrut(14));
        box.add(quit);

        menuScreen.add(box);
    }

    private void buildVictoryScreen() {
        victoryScreen.setLayout(new GridBagLayout());
        victoryScreen.add(buildEndButtonsPanel());
    }

    private void buildDefeatScreen() {
        defeatScreen.setLayout(new GridBagLayout());
        defeatScreen.add(buildEndButtonsPanel());
    }

    private JPanel buildEndButtonsPanel() {
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(new EmptyBorder(420, 0, 0, 0));

        AnimatedMenuButton playAgain = new AnimatedMenuButton("PLAY AGAIN", "START!", "GOOD LUCK!");
        AnimatedMenuButton quit = new AnimatedMenuButton("QUIT GAME", "BYE!", "SEE YOU!");

        playAgain.setAlignmentX(Component.CENTER_ALIGNMENT);
        quit.setAlignmentX(Component.CENTER_ALIGNMENT);

        playAgain.addActionListener(e -> {
            playAgain.playStartAnimation();
            if (game != null) game.startNewGame();
        });
        quit.addActionListener(e -> System.exit(0));

        box.add(playAgain);
        box.add(Box.createVerticalStrut(14));
        box.add(quit);

        return box;
    }

    private void buildGameScreen() {
        gameScreen.setBackground(new Color(18, 18, 18));

        JPanel topBar = new JPanel();
        topBar.setBackground(new Color(15, 15, 15));
        topBar.setBorder(new EmptyBorder(8, 10, 8, 10));
        topBar.setLayout(new BoxLayout(topBar, BoxLayout.X_AXIS));

        styleHudLabel(turnLabel);
        styleHudLabel(goldLabel);
        styleHudLabel(woodLabel);
        styleHudLabel(stoneLabel);
        styleHudLabel(foodLabel);

        topBar.add(turnLabel);
        topBar.add(Box.createHorizontalStrut(20));
        topBar.add(goldLabel);
        topBar.add(Box.createHorizontalStrut(14));
        topBar.add(woodLabel);
        topBar.add(Box.createHorizontalStrut(14));
        topBar.add(stoneLabel);
        topBar.add(Box.createHorizontalStrut(14));
        topBar.add(foodLabel);

        topBar.add(Box.createHorizontalGlue());

        saveBtn.addActionListener(e -> { if (game != null) game.saveNow(); });
        mainMenuBtn.addActionListener(e -> { if (game != null) game.goToMainMenu(); });

        topBar.add(saveBtn);
        topBar.add(Box.createHorizontalStrut(10));
        topBar.add(mainMenuBtn);

        gameScreen.add(topBar, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(mapPanel);
        scroll.setBorder(null);
        gameScreen.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(new Color(15, 15, 15));
        bottom.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        collectBtn.setVisible(false);
        attackBtn.setVisible(false);

        collectBtn.addActionListener(e -> {
            if (selected == null) return;
            mode = ActionMode.COLLECT;
            notifyEvent("Click a resource tile to collect");
            updateBottomBarButtons();
            mapPanel.repaint();
        });

        attackBtn.addActionListener(e -> {
            if (selected == null) return;
            mode = ActionMode.ATTACK;
            notifyEvent("Click an enemy tile to attack");
            updateBottomBarButtons();
            mapPanel.repaint();
        });

        trainSoldierBtn.addActionListener(e -> { if (game != null) game.trainSoldier(); });
        trainArcherBtn.addActionListener(e -> { if (game != null) game.trainArcher(); });
        trainCavalryBtn.addActionListener(e -> { if (game != null) game.trainCavalry(); });

        buildFarmBtn.addActionListener(e -> { if (game != null) game.buildFarm(); });
        buildMineBtn.addActionListener(e -> { if (game != null) game.buildMine(); });
        buildBarracksBtn.addActionListener(e -> { if (game != null) game.buildBarracks(); });

        left.add(trainSoldierBtn);
        left.add(trainArcherBtn);
        left.add(trainCavalryBtn);

        left.add(Box.createHorizontalStrut(16));

        left.add(buildFarmBtn);
        left.add(buildMineBtn);
        left.add(buildBarracksBtn);

        right.add(collectBtn);
        right.add(attackBtn);

        bottom.add(left, BorderLayout.WEST);
        bottom.add(right, BorderLayout.EAST);

        JPanel status = new JPanel(new BorderLayout());
        status.setBackground(new Color(10, 10, 10));
        status.setBorder(new EmptyBorder(6, 10, 6, 10));
        statusLabel.setForeground(new Color(235, 205, 120));
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 13f));
        status.add(statusLabel, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(bottom, BorderLayout.CENTER);
        south.add(status, BorderLayout.SOUTH);

        gameScreen.add(south, BorderLayout.SOUTH);
    }

    private void styleHudLabel(JLabel l) {
        l.setForeground(new Color(235, 205, 120));
        l.setFont(l.getFont().deriveFont(Font.BOLD, 14f));
    }

    private void updateTopBar() {
        if (tm == null) turnLabel.setText("Turn: -");
        else turnLabel.setText("Turn: " + tm.getTurn() + (tm.isHumanTurn() ? " (You)" : " (Enemy)"));

        if (ctx == null) {
            goldLabel.setText("Gold: 0");
            woodLabel.setText("Wood: 0");
            stoneLabel.setText("Stone: 0");
            foodLabel.setText("Food: 0");
            return;
        }

        EnumMap<ResourceType, Integer> snap = ctx.getHuman().getResources().snapshot();
        goldLabel.setText("Gold: " + snap.getOrDefault(ResourceType.GOLD, 0));
        woodLabel.setText("Wood: " + snap.getOrDefault(ResourceType.WOOD, 0));
        stoneLabel.setText("Stone: " + snap.getOrDefault(ResourceType.STONE, 0));
        foodLabel.setText("Food: " + snap.getOrDefault(ResourceType.FOOD, 0));
    }

    private void updateBottomBarButtons() {
        boolean enabled = (ctx != null && tm != null && tm.isHumanTurn());

        trainSoldierBtn.setEnabled(enabled && mode != ActionMode.BUILD);
        trainArcherBtn.setEnabled(enabled && mode != ActionMode.BUILD);
        trainCavalryBtn.setEnabled(enabled && mode != ActionMode.BUILD);

        buildFarmBtn.setEnabled(enabled && mode != ActionMode.BUILD);
        buildMineBtn.setEnabled(enabled && mode != ActionMode.BUILD);
        buildBarracksBtn.setEnabled(enabled && mode != ActionMode.BUILD);

        saveBtn.setEnabled(ctx != null);
        mainMenuBtn.setEnabled(true);

        boolean hasSelectedHumanUnit = false;
        boolean canCollectNow = false;
        boolean canAttackNow = false;

        if (enabled && ctx != null && selected != null && game != null && mode != ActionMode.BUILD) {
            Tile t = ctx.getMap().getTile(selected.x, selected.y);
            if (t != null && t.getUnit() != null && t.getUnit().getOwner() == ctx.getHuman()) {
                hasSelectedHumanUnit = true;
                canCollectNow = existsCollectTarget(selected);
                canAttackNow = existsAttackTarget(selected);
            }
        }

        collectBtn.setVisible(hasSelectedHumanUnit && canCollectNow);
        attackBtn.setVisible(hasSelectedHumanUnit && canAttackNow);

        collectBtn.setEnabled(hasSelectedHumanUnit && canCollectNow);
        attackBtn.setEnabled(hasSelectedHumanUnit && canAttackNow);

        if (!collectBtn.isVisible() && mode == ActionMode.COLLECT) mode = ActionMode.NONE;
        if (!attackBtn.isVisible() && mode == ActionMode.ATTACK) mode = ActionMode.NONE;
    }

    private boolean existsCollectTarget(Position from) {
        if (game == null || ctx == null || from == null) return false;
        int w = ctx.getMap().getWidth(), h = ctx.getMap().getHeight();
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            if (game.canCollectTarget(from, new Position(x, y))) return true;
        }
        return false;
    }

    private boolean existsAttackTarget(Position from) {
        if (game == null || ctx == null || from == null) return false;
        int w = ctx.getMap().getWidth(), h = ctx.getMap().getHeight();
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            if (game.canAttackTarget(from, new Position(x, y))) return true;
        }
        return false;
    }

    private class MapPanel extends JPanel {
    	private static final long serialVersionUID = 1L;

        private GameContext ctx;
        private final int tileSize = 64;

        private final BufferedImage grass = loadImage("/assets/tiles/grass.png");
        private final BufferedImage water = loadImage("/assets/tiles/water.png");
        private final BufferedImage forest = loadImage("/assets/tiles/forest.png");
        private final BufferedImage mountain = loadImage("/assets/tiles/mountain.png");

        private final BufferedImage castle = loadImage("/assets/buildings/castle.png");
        private final BufferedImage barracks = loadImage("/assets/buildings/barracks.png");
        private final BufferedImage farm = loadImage("/assets/buildings/farm.png");
        private final BufferedImage mine = loadImage("/assets/buildings/mine.png");

        private final BufferedImage soldier = loadImage("/assets/units/soldier.png");
        private final BufferedImage archer = loadImage("/assets/units/archer.png");
        private final BufferedImage cavalry = loadImage("/assets/units/cavalry.png");

        public MapPanel() {
            setBackground(Color.DARK_GRAY);
            setDoubleBuffered(true);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (ctx == null || game == null || tm == null) return;
                    if (!tm.isHumanTurn()) return;

                    int x = e.getX() / tileSize;
                    int y = e.getY() / tileSize;

                    Tile t = ctx.getMap().getTile(x, y);
                    if (t == null) return;

                    Position clicked = new Position(x, y);

                    if (mode == ActionMode.BUILD && pendingPlacement != null) {
                        game.placePendingBuilding(clicked);
                        mode = ActionMode.NONE;
                        pendingPlacement = null;
                        render();
                        return;
                    }

                    if (selected == null) {
                        if (t.getUnit() != null && t.getUnit().getOwner() == ctx.getHuman()) {
                            selected = clicked;
                            mode = ActionMode.NONE;
                            notifyEvent("Selected unit");
                            render();
                        }
                        return;
                    }

                    Tile selT = ctx.getMap().getTile(selected.x, selected.y);
                    if (selT == null || selT.getUnit() == null || selT.getUnit().getOwner() != ctx.getHuman()) {
                        selected = null;
                        mode = ActionMode.NONE;
                        render();
                        return;
                    }

                    if (mode == ActionMode.COLLECT) {
                        if (game.canCollectTarget(selected, clicked)) {
                            game.collectTarget(selected, clicked);
                            selected = null;
                            mode = ActionMode.NONE;
                        } else {
                            notifyEvent("Can't collect that tile");
                        }
                        render();
                        return;
                    }

                    if (mode == ActionMode.ATTACK) {
                        if (game.canAttackTarget(selected, clicked)) {
                            game.attackTarget(selected, clicked);
                            selected = null;
                            mode = ActionMode.NONE;
                        } else {
                            notifyEvent("Can't attack that tile");
                        }
                        render();
                        return;
                    }

                    if (clicked.equals(selected)) {
                        selected = null;
                        mode = ActionMode.NONE;
                        render();
                        return;
                    }

                    if (t.getUnit() != null && t.getUnit().getOwner() == ctx.getHuman()) {
                        selected = clicked;
                        mode = ActionMode.NONE;
                        render();
                        return;
                    }

                    boolean moved = game.moveUnit(selected, clicked);
                    if (moved) {
                        selected = null;
                        mode = ActionMode.NONE;
                    }
                    render();
                }
            });
        }

        public void setContext(GameContext ctx) {
            this.ctx = ctx;
            if (ctx == null) setPreferredSize(new Dimension(800, 600));
            else setPreferredSize(new Dimension(ctx.getMap().getWidth() * tileSize, ctx.getMap().getHeight() * tileSize));
            revalidate();
            repaint();
        }

        private BufferedImage loadImage(String path) {
            try (InputStream in = getClass().getResourceAsStream(path)) {
                if (in == null) return null;
                return ImageIO.read(in);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (ctx == null) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            GameMap map = ctx.getMap();

            for (int yy = 0; yy < map.getHeight(); yy++) {
                for (int xx = 0; xx < map.getWidth(); xx++) {
                    Tile t = map.getTile(xx, yy);
                    if (t == null) continue;

                    BufferedImage tileImg = switch (t.getType()) {
                        case GRASS -> grass;
                        case WATER -> water;
                        case FOREST -> forest;
                        case MOUNTAIN -> mountain;
                    };

                    if (tileImg != null) g2.drawImage(tileImg, xx * tileSize, yy * tileSize, tileSize, tileSize, null);
                    else {
                        g2.setColor(Color.GRAY);
                        g2.fillRect(xx * tileSize, yy * tileSize, tileSize, tileSize);
                    }

                    g2.setColor(new Color(0, 0, 0, 40));
                    g2.drawRect(xx * tileSize, yy * tileSize, tileSize, tileSize);
                }
            }

            for (int yy = 0; yy < map.getHeight(); yy++) {
                for (int xx = 0; xx < map.getWidth(); xx++) {
                    Tile t = map.getTile(xx, yy);
                    if (t == null) continue;

                    Building b = t.getBuilding();
                    if (b != null && b.getPosition() != null && b.getPosition().x == xx && b.getPosition().y == yy) {
                        BufferedImage bi = switch (b.getType()) {
                            case CASTLE -> castle;
                            case BARRACKS -> barracks;
                            case FARM -> farm;
                            case MINE -> mine;
                        };
                        if (bi != null) g2.drawImage(bi, xx * tileSize, yy * tileSize, tileSize * 2, tileSize * 2, null);
                    }
                }
            }

            for (int yy = 0; yy < map.getHeight(); yy++) {
                for (int xx = 0; xx < map.getWidth(); xx++) {
                    Tile t = map.getTile(xx, yy);
                    if (t == null) continue;
                    var u = t.getUnit();
                    if (u == null) continue;

                    BufferedImage ui = switch (u.getType()) {
                        case SOLDIER -> soldier;
                        case ARCHER -> archer;
                        case CAVALRY -> cavalry;
                    };

                    int px = xx * tileSize;
                    int py = yy * tileSize;

                    if (ui != null) g2.drawImage(ui, px, py, tileSize, tileSize, null);
                }
            }

            if (mode == ActionMode.BUILD && pendingPlacement != null) {
                drawBuildPlacements(g2);
            }

            // Green reachable tiles when selecting a unit (normal mode)
            if (selected != null && mode == ActionMode.NONE) {
                drawMovableTargets(g2, selected);
            }

            if (selected != null) {
                g2.setColor(new Color(255, 220, 130, 170));
                g2.setStroke(new BasicStroke(3f));
                g2.drawRoundRect(selected.x * tileSize + 2, selected.y * tileSize + 2, tileSize - 4, tileSize - 4, 14, 14);

                if (mode == ActionMode.ATTACK) drawAttackableTargets(g2, selected);
                else if (mode == ActionMode.COLLECT) drawCollectableTargets(g2, selected);
            }

            g2.dispose();
        }

        private void drawBuildPlacements(Graphics2D g2) {
            if (ctx == null) return;
            g2.setColor(new Color(80, 255, 160, 60));
            for (int y = 0; y < ctx.getMap().getHeight(); y++) {
                for (int x = 0; x < ctx.getMap().getWidth(); x++) {
                    Position p = new Position(x, y);
                    if (ctx.canPlaceBuilding(pendingPlacement, p, ctx.getHuman())) {
                        g2.fillRect(x * tileSize, y * tileSize, tileSize * 2, tileSize * 2);
                    }
                }
            }
        }

        // Highlights valid movement tiles in green
        private void drawMovableTargets(Graphics2D g2, Position from) {
            Tile t = ctx.getMap().getTile(from.x, from.y);
            if (t == null || t.getUnit() == null) return;
            if (t.getUnit().getOwner() != ctx.getHuman()) return;

            int max = (t.getUnit().getType() == UnitType.CAVALRY) ? 2 : 1;
            g2.setColor(new Color(80, 255, 80, 80));

            for (int y = 0; y < ctx.getMap().getHeight(); y++) {
                for (int x = 0; x < ctx.getMap().getWidth(); x++) {
                    int dist = Math.abs(x - from.x) + Math.abs(y - from.y);
                    if (dist < 1 || dist > max) continue;

                    Position p = new Position(x, y);
                    if (ctx.isFreeForUnit(p, false)) {
                        g2.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
                    }
                }
            }
        }

        private void drawAttackableTargets(Graphics2D g2, Position from) {
            g2.setColor(new Color(255, 80, 80, 70));
            for (int y = 0; y < ctx.getMap().getHeight(); y++) {
                for (int x = 0; x < ctx.getMap().getWidth(); x++) {
                    Position p = new Position(x, y);
                    if (game != null && game.canAttackTarget(from, p)) g2.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
                }
            }
        }

        private void drawCollectableTargets(Graphics2D g2, Position from) {
            g2.setColor(new Color(80, 255, 160, 60));
            for (int y = 0; y < ctx.getMap().getHeight(); y++) {
                for (int x = 0; x < ctx.getMap().getWidth(); x++) {
                    Position p = new Position(x, y);
                    if (game != null && game.canCollectTarget(from, p)) g2.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
                }
            }
        }
    }

    private static class PressButton extends JButton {
    	private static final long serialVersionUID = 1L;

        public PressButton(String text) {
            super(text);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setForeground(new Color(250, 235, 180));
            setFont(getFont().deriveFont(Font.BOLD, 16f));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMargin(new Insets(10, 18, 10, 18));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            Color base = new Color(150, 100, 30);
            Color top = new Color(210, 160, 60);
            Color border = new Color(235, 205, 120);

            if (!isEnabled()) {
                base = new Color(70, 60, 40);
                top = new Color(90, 80, 55);
                border = new Color(120, 110, 85);
            } else if (getModel().isPressed()) {
                base = new Color(110, 80, 25);
                top = new Color(170, 130, 50);
            } else if (getModel().isRollover()) {
                base = new Color(175, 120, 35);
                top = new Color(235, 190, 75);
            }

            GradientPaint gp = new GradientPaint(0, 0, top, 0, h, base);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, w - 1, h - 1, 18, 18);

            g2.setColor(border);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, 18, 18);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class ImageScreenPanel extends JPanel {
    	private static final long serialVersionUID = 1L;

        private final BufferedImage bg;

        public ImageScreenPanel(String path) {
            bg = load(path);
            setDoubleBuffered(true);
        }

        private static BufferedImage load(String path) {
            try (InputStream in = ImageScreenPanel.class.getResourceAsStream(path)) {
                if (in == null) return null;
                return ImageIO.read(in);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bg == null) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(bg, 0, 0, getWidth(), getHeight(), null);
            g2.dispose();
        }
    }
}
