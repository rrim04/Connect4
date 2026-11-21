
package ma.connect4.engine;

import ma.connect4.model.Board;
import ma.connect4.model.Disc;
import ma.connect4.model.GameState;
import ma.connect4.model.Move;

/**
 * Règles officielles du Puissance 4 : validité d'un coup, fin de partie, adversaire.
 *
 * Classe utilitaire (méthodes statiques) : on ne l'instancie jamais.
 */
public final class GameRules {

    private GameRules() {
        // Utility class
    }

    /**
     * Vérifie qu'un coup est autorisé : la grille existe et la colonne visée n'est pas pleine.
     */
    public static boolean isLegalMove(Board board, Move move) {
        return board != null && board.isValidMove(move);
    }

    /**
     * Détermine l'état de la partie après un coup.
     *
     * Ordre des tests : victoire du joueur qui vient de jouer, puis grille pleine
     * (match nul), sinon la partie continue.
     *
     * @param lastPlayer couleur du joueur qui vient de jouer
     * @param lastRow    ligne où son pion est tombé
     * @param lastColumn colonne où son pion est tombé
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

        // Seul le dernier pion posé peut avoir créé un alignement : on ne teste que lui.
        if (WinDetector.isWinningMove(
                board, lastRow, lastColumn, lastPlayer)) {
            return lastPlayer == Disc.RED
                    ? GameState.RED_WON
                    : GameState.YELLOW_WON;
        }

        // Grille pleine et personne n'a gagné : match nul.
        if (board.isFull()) {
            return GameState.DRAW;
        }

        return GameState.IN_PROGRESS;
    }

    /**
     * Retourne la couleur de l'adversaire du joueur donné.
     */
    public static Disc getOpponent(Disc currentPlayer) {
        if (currentPlayer == null || currentPlayer == Disc.EMPTY) {
            throw new IllegalArgumentException("Invalid player disc");
        }

        return currentPlayer.opponent();
    }
}
