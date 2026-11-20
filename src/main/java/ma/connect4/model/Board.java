
package ma.connect4.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents the 6x7 Connect Four board.
 * Row 0 is the top row; row 5 is the bottom row.
 */
public final class Board {

    public static final int ROWS = 6;
    public static final int COLUMNS = 7;
    public static final int CONNECT_LENGTH = 4;

    private final Disc[][] cells;

    public Board() {
        cells = new Disc[ROWS][COLUMNS];
        for (Disc[] row : cells) {
            Arrays.fill(row, Disc.EMPTY);
        }
    }

    private Board(Disc[][] source) {
        cells = new Disc[ROWS][COLUMNS];
        for (int row = 0; row < ROWS; row++) {
            cells[row] = source[row].clone();
        }
    }

    public Disc getCell(int row, int column) {
        checkCoordinates(row, column);
        return cells[row][column];
    }

    public boolean isColumnFull(int column) {
        checkColumn(column);
        return cells[0][column] != Disc.EMPTY;
    }

    public boolean isValidMove(Move move) {
        return move != null && !isColumnFull(move.column());
    }

    /**
     * Drops a disc into a column and returns the row where it lands.
     * Returns -1 if the column is full.
     */
    public int dropDisc(int column, Disc disc) {
        checkColumn(column);

        if (disc == null || disc == Disc.EMPTY) {
            throw new IllegalArgumentException("A real player disc is required");
        }

        for (int row = ROWS - 1; row >= 0; row--) {
            if (cells[row][column] == Disc.EMPTY) {
                cells[row][column] = disc;
                return row;
            }
        }

        return -1;
    }

    public List<Move> getLegalMoves() {
        List<Move> moves = new ArrayList<>();

        for (int column = 0; column < COLUMNS; column++) {
            if (!isColumnFull(column)) {
                moves.add(new Move(column));
            }
        }

        return moves;
    }

    public boolean isFull() {
        for (int column = 0; column < COLUMNS; column++) {
            if (!isColumnFull(column)) {
                return false;
            }
        }
        return true;
    }

    public Board copy() {
        return new Board(cells);
    }

    public Disc[][] toArray() {
        Disc[][] result = new Disc[ROWS][COLUMNS];

        for (int row = 0; row < ROWS; row++) {
            result[row] = cells[row].clone();
        }

        return result;
    }

    private void checkCoordinates(int row, int column) {
        if (row < 0 || row >= ROWS ||
                column < 0 || column >= COLUMNS) {
            throw new IndexOutOfBoundsException("Invalid board coordinates");
        }
    }

    private void checkColumn(int column) {
        if (column < 0 || column >= COLUMNS) {
            throw new IndexOutOfBoundsException("Invalid column: " + column);
        }
    }
}
