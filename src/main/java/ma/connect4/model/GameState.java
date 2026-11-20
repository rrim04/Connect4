
package ma.connect4.model;

/**
 * Represents the current state of a game.
 */
public enum GameState {
    IN_PROGRESS,
    RED_WON,
    YELLOW_WON,
    DRAW;

    public boolean isFinished() {
        return this != IN_PROGRESS;
    }
}
