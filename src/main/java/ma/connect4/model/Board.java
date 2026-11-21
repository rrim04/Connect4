
package ma.connect4.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Grille de Puissance 4 : 6 lignes et 7 colonnes.
 *
 * La ligne 0 est le HAUT de la grille et la ligne 5 le BAS : un pion lâché
 * dans une colonne tombe donc vers les numéros de ligne les plus grands.
 * (Les cases sont stockées dans un tableau {@code cells[ligne][colonne]}.)
 */
public final class Board {

    // Dimensions de la grille et longueur de l'alignement gagnant.
    public static final int ROWS = 6;
    public static final int COLUMNS = 7;
    public static final int CONNECT_LENGTH = 4;

    // Contenu de chaque case : EMPTY, RED ou YELLOW.
    private final Disc[][] cells;

    /**
     * Crée une grille vide.
     */
    public Board() {
        cells = new Disc[ROWS][COLUMNS];
        for (Disc[] row : cells) {
            Arrays.fill(row, Disc.EMPTY);
        }
    }

    /**
     * Constructeur privé utilisé par {@link #copy()} : copie la grille source
     * ligne par ligne, pour que les deux grilles soient indépendantes.
     */
    private Board(Disc[][] source) {
        cells = new Disc[ROWS][COLUMNS];
        for (int row = 0; row < ROWS; row++) {
            cells[row] = source[row].clone();
        }
    }

    /**
     * Retourne le contenu d'une case.
     *
     * @throws IndexOutOfBoundsException si les coordonnées sont hors de la grille
     */
    public Disc getCell(int row, int column) {
        checkCoordinates(row, column);
        return cells[row][column];
    }

    /**
     * Une colonne est pleine si sa case du haut (ligne 0) est occupée.
     */
    public boolean isColumnFull(int column) {
        checkColumn(column);
        return cells[0][column] != Disc.EMPTY;
    }

    /**
     * Un coup est valide s'il n'est pas null et que sa colonne n'est pas pleine.
     */
    public boolean isValidMove(Move move) {
        return move != null && !isColumnFull(move.column());
    }

    /**
     * Laisse tomber un pion dans une colonne et retourne la ligne où il se pose.
     * Retourne -1 si la colonne est pleine (aucun pion n'est alors posé).
     *
     * @throws IllegalArgumentException si le pion est null ou EMPTY
     * @throws IndexOutOfBoundsException si la colonne n'existe pas
     */
    public int dropDisc(int column, Disc disc) {
        checkColumn(column);

        if (disc == null || disc == Disc.EMPTY) {
            throw new IllegalArgumentException("A real player disc is required");
        }

        // On part du bas de la grille : le pion s'arrête sur la première case vide rencontrée.
        for (int row = ROWS - 1; row >= 0; row--) {
            if (cells[row][column] == Disc.EMPTY) {
                cells[row][column] = disc;
                return row;
            }
        }

        return -1;
    }

    /**
     * Liste les coups jouables : une entrée par colonne qui n'est pas pleine.
     */
    public List<Move> getLegalMoves() {
        List<Move> moves = new ArrayList<>();

        for (int column = 0; column < COLUMNS; column++) {
            if (!isColumnFull(column)) {
                moves.add(new Move(column));
            }
        }

        return moves;
    }

    /**
     * La grille est pleine quand toutes les colonnes sont pleines (match nul s'il n'y a pas de gagnant).
     */
    public boolean isFull() {
        for (int column = 0; column < COLUMNS; column++) {
            if (!isColumnFull(column)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Retourne une copie indépendante (modifier la copie ne change pas l'original).
     */
    public Board copy() {
        return new Board(cells);
    }

    /**
     * Retourne une COPIE du contenu de la grille sous forme de tableau [ligne][colonne].
     */
    public Disc[][] toArray() {
        Disc[][] result = new Disc[ROWS][COLUMNS];

        for (int row = 0; row < ROWS; row++) {
            result[row] = cells[row].clone();
        }

        return result;
    }

    /**
     * Vérifie que la case (ligne, colonne) existe.
     */
    private void checkCoordinates(int row, int column) {
        if (row < 0 || row >= ROWS ||
                column < 0 || column >= COLUMNS) {
            throw new IndexOutOfBoundsException("Invalid board coordinates");
        }
    }

    /**
     * Vérifie que la colonne existe (entre 0 et 6).
     */
    private void checkColumn(int column) {
        if (column < 0 || column >= COLUMNS) {
            throw new IndexOutOfBoundsException("Invalid column: " + column);
        }
    }
}
