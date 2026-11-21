
package ma.connect4.engine;

import ma.connect4.model.Board;
import ma.connect4.model.Disc;

/**
 * Détecte les alignements gagnants (4 pions de la même couleur) sur la grille.
 *
 * Deux méthodes publiques : {@link #hasWon} parcourt toute la grille, tandis que
 * {@link #isWinningMove} ne regarde que autour du dernier pion posé (plus rapide).
 */
public final class WinDetector {

    // Les 4 directions à tester, sous la forme {pas de ligne, pas de colonne}.
    // Les directions opposées (gauche, haut...) sont couvertes en comptant dans les deux sens.
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
     * Vérifie si le joueur a quatre pions alignés quelque part sur la grille.
     *
     * Pour chaque pion du joueur, on compte les pions consécutifs dans les 4 directions.
     */
    public static boolean hasWon(Board board, Disc disc) {
        if (board == null || disc == null || disc == Disc.EMPTY) {
            return false;
        }

        for (int row = 0; row < Board.ROWS; row++) {
            for (int column = 0; column < Board.COLUMNS; column++) {
                // On ne part que des cases contenant un pion du joueur.
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
     * Vérifie si le dernier pion posé a créé un alignement gagnant.
     *
     * Pour chaque direction, on additionne : le pion lui-même (1) + les pions identiques
     * d'un côté + ceux de l'autre côté. Un total d'au moins 4 signifie la victoire.
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
            // Le pion qui vient d'être posé compte pour 1.
            int count = 1;

            // Pions identiques dans le sens de la direction...
            count += countInDirection(
                    board, row, column,
                    direction[0], direction[1], disc
            );

            // ... puis dans le sens opposé.
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

    /**
     * Compte les pions consécutifs de la couleur {@code disc} en partant de la case
     * (row, column) INCLUSE et en avançant dans la direction (deltaRow, deltaColumn).
     */
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

    /**
     * Comme {@code countAligned}, mais la case de départ est EXCLUE : on commence
     * à la case voisine. Sert à compter autour du dernier pion posé.
     */
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

    /**
     * Vrai si la case (ligne, colonne) est dans la grille.
     */
    private static boolean isInside(int row, int column) {
        return row >= 0 && row < Board.ROWS
                && column >= 0 && column < Board.COLUMNS;
    }
}
