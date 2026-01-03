package main;

import game.Game;
import ui.SwingUI;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SwingUI ui = new SwingUI();
            Game game = new Game(ui);
            ui.attachGame(game);
            ui.showWindow();
        });
    }
}
