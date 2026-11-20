
package ma.connect4.persistence;

import ma.connect4.model.Disc;
import ma.connect4.model.GameState;

import java.time.LocalDateTime;
import java.util.Objects;

public record GameSave(
        Disc[][] board,
        Disc currentPlayer,
        GameState gameState,
        int moveCount,
        LocalDateTime savedAt
) {
    public GameSave {
        Objects.requireNonNull(board, "Board cannot be null");
        Objects.requireNonNull(currentPlayer, "Current player cannot be null");
        Objects.requireNonNull(gameState, "Game state cannot be null");
        Objects.requireNonNull(savedAt, "Save date cannot be null");

        if (board.length != 6) {
            throw new IllegalArgumentException("Board must have 6 rows");
        }

        Disc[][] copy = new Disc[6][7];

        for (int row = 0; row < 6; row++) {
            if (board[row] == null || board[row].length != 7) {
                throw new IllegalArgumentException("Board must have 7 columns");
            }

            copy[row] = board[row].clone();

            for (Disc disc : copy[row]) {
                Objects.requireNonNull(disc, "Board cannot contain null discs");
            }
        }

        if (currentPlayer == Disc.EMPTY) {
            throw new IllegalArgumentException("Current player cannot be EMPTY");
        }

        if (moveCount < 0 || moveCount > 42) {
            throw new IllegalArgumentException("Invalid move count");
        }

        board = copy;
    }

    @Override
    public Disc[][] board() {
        Disc[][] copy = new Disc[6][7];

        for (int row = 0; row < 6; row++) {
            copy[row] = board[row].clone();
        }

        return copy;
    }
}
