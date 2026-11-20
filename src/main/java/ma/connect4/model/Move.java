
package ma.connect4.model;

/**
 * Represents a move played in a column.
 */
public record Move(int column) {

    public Move {
        if (column < 0 || column >= Board.COLUMNS) {
            throw new IllegalArgumentException("Column must be between 0 and 6");
        }
    }
}
