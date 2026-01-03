package ui;

import buildings.Building;
import game.GameContext;
import game.TurnManager;

public interface GameUI {
    void bindTurnManager(TurnManager tm);
    void bindContext(GameContext ctx);

    void showMainMenu();
    void showGame();
    void showVictory();
    void showDefeat();

    void render();
    void notifyEvent(String msg);

    boolean requestBuildPlacement(Building b);
}
