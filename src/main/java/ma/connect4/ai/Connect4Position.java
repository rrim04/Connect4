package ma.connect4.ai;

import ma.connect4.model.Board;
import ma.connect4.search.Position;

/**
 * Position du jeu de Puissance 4 pour le moteur de recherche.
 *
 * Elle enveloppe une grille ({@link Board}) et retient la colonne du
 * dernier coup joué, ce qui permet à la recherche de retrouver le coup
 * à jouer à partir de la meilleure position trouvée.
 *
 * La grille contenue ne doit jamais être modifiée : pour jouer un coup,
 * on travaille toujours sur une copie (voir {@code Connect4Search.makeMove}).
 */
public final class Connect4Position extends Position {

    private final Board board;
    private final int lastColumn;

    /** Position de départ : aucun coup n'a encore été joué (colonne -1). */
    public Connect4Position(Board board) {
        this(board, -1);
    }

    public Connect4Position(Board board, int lastColumn) {
        this.board = board;
        this.lastColumn = lastColumn;
    }

    public Board getBoard() {
        return board;
    }

    /** Colonne du coup qui a mené à cette position (-1 pour la position de départ). */
    public int getLastColumn() {
        return lastColumn;
    }
}
