package game;

public class TurnManager {
    private int turn = 1;
    private boolean humanTurn = true;

    public int getTurn() { return turn; }
    public boolean isHumanTurn() { return humanTurn; }

    public void next() {
        humanTurn = !humanTurn;
        if (humanTurn) turn++;
    }
}
