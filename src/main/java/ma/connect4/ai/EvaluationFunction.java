
package ma.connect4.ai;

import ma.connect4.model.Board;
import ma.connect4.model.Disc;

/**
 * Fonction d'évaluation (heuristique) du Puissance 4.
 *
 * Quand la recherche atteint la profondeur maximale sans que la partie soit
 * terminée, cette fonction donne une note à la grille du point de vue du joueur
 * demandé : positive si la position lui est favorable, négative sinon.
 *
 * Principe : on examine toutes les « fenêtres » de 4 cases alignées
 * (horizontales, verticales et diagonales) et on note chacune selon le nombre
 * de pions de chaque camp qu'elle contient, plus un bonus pour la colonne centrale.
 */
public final class EvaluationFunction {

    // Barème de l'heuristique :
    //  - CENTER_BONUS   : points par pion dans la colonne centrale (elle fait partie du
    //                     plus grand nombre de lignes gagnantes possibles) ;
    //  - THREE_IN_ROW   : fenêtre avec 3 de nos pions et 1 case vide (menace de victoire) ;
    //  - TWO_IN_ROW     : fenêtre avec 2 de nos pions et 2 cases vides ;
    //  - OPPONENT_THREE : même chose pour l'adversaire, pénalisée un peu plus fort
    //                     (120 > 100) pour pousser l'IA à bloquer ses menaces.
    private static final int CENTER_BONUS = 6;
    private static final int THREE_IN_ROW = 100;
    private static final int TWO_IN_ROW = 10;
    private static final int OPPONENT_THREE = 120;

    private EvaluationFunction() {
    }

    /**
     * Évalue une grille du point de vue de {@code player}.
     *
     * @param board  la grille à évaluer
     * @param player le joueur pour lequel on calcule le score (pas EMPTY)
     * @return le score : plus il est élevé, meilleure est la position pour {@code player}
     * @throws IllegalArgumentException si un argument est invalide
     */
    public static int evaluate(Board board, Disc player) {
        if (board == null || player == null || player == Disc.EMPTY) {
            throw new IllegalArgumentException("Invalid evaluation arguments");
        }

        Disc opponent = player.opponent();
        // Copie de la grille : cells[ligne][colonne], la ligne 0 étant le haut.
        Disc[][] cells = board.toArray();

        int score = 0;

        // Bonus / malus pour la colonne centrale : chaque pion au centre augmente le score,
        // chaque pion adverse au centre le diminue.
        int centerColumn = Board.COLUMNS / 2;

        for (int row = 0; row < Board.ROWS; row++) {
            if (cells[row][centerColumn] == player) {
                score += CENTER_BONUS;
            } else if (cells[row][centerColumn] == opponent) {
                score -= CENTER_BONUS;
            }
        }

        // Fenêtres horizontales (4 cases de gauche à droite).
        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col <= Board.COLUMNS - 4; col++) {
                score += evaluateWindow(
                        cells, row, col, 0, 1, player, opponent
                );
            }
        }

        // Fenêtres verticales (4 cases de haut en bas).
        for (int row = 0; row <= Board.ROWS - 4; row++) {
            for (int col = 0; col < Board.COLUMNS; col++) {
                score += evaluateWindow(
                        cells, row, col, 1, 0, player, opponent
                );
            }
        }

        // Fenêtres diagonales descendantes vers la droite.
        for (int row = 0; row <= Board.ROWS - 4; row++) {
            for (int col = 0; col <= Board.COLUMNS - 4; col++) {
                score += evaluateWindow(
                        cells, row, col, 1, 1, player, opponent
                );
            }
        }

        // Fenêtres diagonales descendantes vers la gauche (départ à partir de la colonne 3).
        for (int row = 0; row <= Board.ROWS - 4; row++) {
            for (int col = 3; col < Board.COLUMNS; col++) {
                score += evaluateWindow(
                        cells, row, col, 1, -1, player, opponent
                );
            }
        }

        return score;
    }

    /**
     * Note une fenêtre de 4 cases alignées.
     *
     * @param startRow    ligne de la première case
     * @param startCol    colonne de la première case
     * @param deltaRow    pas vertical entre deux cases (0, 1)
     * @param deltaCol    pas horizontal entre deux cases (-1, 0, 1)
     * @param player      joueur pour lequel on évalue
     * @param opponent    son adversaire
     * @return le score de la fenêtre (0 si elle est mixte ou peu intéressante)
     */
    private static int evaluateWindow(
            Disc[][] cells,
            int startRow,
            int startCol,
            int deltaRow,
            int deltaCol,
            Disc player,
            Disc opponent
    ) {
        // On compte les pions de chaque camp et les cases vides dans la fenêtre.
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

        // Quatre alignés : victoire (ou défaite) ; sert surtout à la cohérence
        // car la recherche détecte déjà les victoires avant d'appeler l'heuristique.
        if (playerCount == 4) return 100_000;
        if (opponentCount == 4) return -100_000;

        // Menaces et potentiels : 3 pions + 1 vide est la configuration la plus précieuse.
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
