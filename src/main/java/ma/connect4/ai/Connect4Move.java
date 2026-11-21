package ma.connect4.ai;

import ma.connect4.model.Board;
import ma.connect4.search.Move;

/**
 * Coup de Puissance 4 : le numéro de la colonne (de 0 à 6) où l'on
 * laisse tomber un pion.
 */
public final class Connect4Move extends Move {

    private final int column;

    public Connect4Move(int column) {
        if (column < 0 || column >= Board.COLUMNS) {
            throw new IllegalArgumentException("Column must be between 0 and 6");
        }
        this.column = column;
    }

    public int getColumn() {
        return column;
    }
}
