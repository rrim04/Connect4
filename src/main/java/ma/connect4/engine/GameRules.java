
package ma.connect4.engine;

import ma.connect4.model.Board;
import ma.connect4.model.Disc;
import ma.connect4.model.GameState;
import ma.connect4.model.Move;

/**
 * Contains the official Connect Four game rules.
 */
public final class GameRules {

    private GameRules() {
        // Utility class
    }

    /**
     * Checks whether a move can legally be played.
     */
    public static boolean isLegalMove(Board board, Move move) {
        return board != null && board.isValidMove(move);
    }

    /**
     * Determines the game state after a move.
     */
    public static GameState determineGameState(
            Board board,
            Disc lastPlayer,
            int lastRow,
            int lastColumn
    ) {
        if (board == null || lastPlayer == null
                || lastPlayer == Disc.EMPTY) {
            throw new IllegalArgumentException(
                    "Board and valid player disc are required"
            );
        }

        if (WinDetector.isWinningMove(
                board, lastRow, lastColumn, lastPlayer)) {
            return lastPlayer == Disc.RED
                    ? GameState.RED_WON
                    : GameState.YELLOW_WON;
        }

        if (board.isFull()) {
            return GameState.DRAW;
        }

        return GameState.IN_PROGRESS;
    }

    /**
     * Returns the opponent of the current player.
     */
    public static Disc getOpponent(Disc currentPlayer) {
        if (currentPlayer == null || currentPlayer == Disc.EMPTY) {
            throw new IllegalArgumentException("Invalid player disc");
        }

        return currentPlayer.opponent();
    }
}
