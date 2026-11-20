
package ma.connect4.ai;

import ma.connect4.model.Board;
import ma.connect4.model.Disc;

/**
 * Heuristic evaluation function for Connect Four.
 */
public final class EvaluationFunction {

    private static final int CENTER_BONUS = 6;
    private static final int THREE_IN_ROW = 100;
    private static final int TWO_IN_ROW = 10;
    private static final int OPPONENT_THREE = 120;

    private EvaluationFunction() {
    }

    public static int evaluate(Board board, Disc player) {
        if (board == null || player == null || player == Disc.EMPTY) {
            throw new IllegalArgumentException("Invalid evaluation arguments");
        }

        Disc opponent = player.opponent();
        Disc[][] cells = board.toArray();

        int score = 0;

        // Prefer controlling the center column.
        int centerColumn = Board.COLUMNS / 2;

        for (int row = 0; row < Board.ROWS; row++) {
            if (cells[row][centerColumn] == player) {
                score += CENTER_BONUS;
            } else if (cells[row][centerColumn] == opponent) {
                score -= CENTER_BONUS;
            }
        }

        // Horizontal windows.
        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col <= Board.COLUMNS - 4; col++) {
                score += evaluateWindow(
                        cells, row, col, 0, 1, player, opponent
                );
            }
        }

        // Vertical windows.
        for (int row = 0; row <= Board.ROWS - 4; row++) {
            for (int col = 0; col < Board.COLUMNS; col++) {
                score += evaluateWindow(
                        cells, row, col, 1, 0, player, opponent
                );
            }
        }

        // Diagonal down-right windows.
        for (int row = 0; row <= Board.ROWS - 4; row++) {
            for (int col = 0; col <= Board.COLUMNS - 4; col++) {
                score += evaluateWindow(
                        cells, row, col, 1, 1, player, opponent
                );
            }
        }

        // Diagonal down-left windows.
        for (int row = 0; row <= Board.ROWS - 4; row++) {
            for (int col = 3; col < Board.COLUMNS; col++) {
                score += evaluateWindow(
                        cells, row, col, 1, -1, player, opponent
                );
            }
        }

        return score;
    }

    private static int evaluateWindow(
            Disc[][] cells,
            int startRow,
            int startCol,
            int deltaRow,
            int deltaCol,
            Disc player,
            Disc opponent
    ) {
        int playerCount = 0;
        int opponentCount = 0;
        int emptyCount = 0;

        for (int i = 0; i < 4; i++) {
            Disc cell = cells[startRow + i * deltaRow]
                    [startCol + i * deltaCol];

            if (cell == player) {
                playerCount++;
            } else if (cell == opponent) {
                opponentCount++;
            } else {
                emptyCount++;
            }
        }

        if (playerCount == 4) return 100_000;
        if (opponentCount == 4) return -100_000;

        if (playerCount == 3 && emptyCount == 1) {
            return THREE_IN_ROW;
        }

        if (playerCount == 2 && emptyCount == 2) {
            return TWO_IN_ROW;
        }

        if (opponentCount == 3 && emptyCount == 1) {
            return -OPPONENT_THREE;
        }

        if (opponentCount == 2 && emptyCount == 2) {
            return -TWO_IN_ROW;
        }

        return 0;
    }
}
