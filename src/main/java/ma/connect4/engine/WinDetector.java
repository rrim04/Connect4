
package ma.connect4.engine;

import ma.connect4.model.Board;
import ma.connect4.model.Disc;

/**
 * Detects winning alignments on the Connect Four board.
 */
public final class WinDetector {

    private static final int[][] DIRECTIONS = {
            {0, 1},   // Horizontal
            {1, 0},   // Vertical
            {1, 1},   // Diagonal down-right
            {1, -1}   // Diagonal down-left
    };

    private WinDetector() {
        // Utility class
    }

    /**
     * Checks whether the given player has four aligned discs.
     */
    public static boolean hasWon(Board board, Disc disc) {
        if (board == null || disc == null || disc == Disc.EMPTY) {
            return false;
        }

        for (int row = 0; row < Board.ROWS; row++) {
            for (int column = 0; column < Board.COLUMNS; column++) {
                if (board.getCell(row, column) != disc) {
                    continue;
                }

                for (int[] direction : DIRECTIONS) {
                    if (countAligned(
                            board,
                            row,
                            column,
                            direction[0],
                            direction[1],
                            disc
                    ) >= Board.CONNECT_LENGTH) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Checks whether the last played move created a winning alignment.
     */
    public static boolean isWinningMove(
            Board board,
            int row,
            int column,
            Disc disc
    ) {
        if (board == null || disc == null || disc == Disc.EMPTY) {
            return false;
        }

        if (row < 0 || row >= Board.ROWS ||
                column < 0 || column >= Board.COLUMNS) {
            return false;
        }

        if (board.getCell(row, column) != disc) {
            return false;
        }

        for (int[] direction : DIRECTIONS) {
            int count = 1;

            count += countInDirection(
                    board, row, column,
                    direction[0], direction[1], disc
            );

            count += countInDirection(
                    board, row, column,
                    -direction[0], -direction[1], disc
            );

            if (count >= Board.CONNECT_LENGTH) {
                return true;
            }
        }

        return false;
    }

    private static int countAligned(
            Board board,
            int row,
            int column,
            int deltaRow,
            int deltaColumn,
            Disc disc
    ) {
        int count = 0;

        while (isInside(row, column)
                && board.getCell(row, column) == disc) {
            count++;
            row += deltaRow;
            column += deltaColumn;
        }

        return count;
    }

    private static int countInDirection(
            Board board,
            int row,
            int column,
            int deltaRow,
            int deltaColumn,
            Disc disc
    ) {
        int count = 0;

        row += deltaRow;
        column += deltaColumn;

        while (isInside(row, column)
                && board.getCell(row, column) == disc) {
            count++;
            row += deltaRow;
            column += deltaColumn;
        }

        return count;
    }

    private static boolean isInside(int row, int column) {
        return row >= 0 && row < Board.ROWS
                && column >= 0 && column < Board.COLUMNS;
    }
}
