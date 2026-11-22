
package ma.connect4.persistence;

import com.fasterxml.jackson.annotation.JsonCreator;
import ma.connect4.model.Disc;
import ma.connect4.model.GameConfig;
import ma.connect4.model.GameState;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Données d'une partie sauvegardée.
 *
 * @param board         la grille [6 lignes][7 colonnes]
 * @param currentPlayer le joueur dont c'est le tour
 * @param gameState     l'état de la partie (en cours, gagnée, nulle)
 * @param moveCount     le nombre de coups joués (entre 0 et 42)
 * @param savedAt       la date et l'heure de la sauvegarde
 * @param config        les réglages de la partie (mode, stratégies, profondeur, aides) ;
 *                      null dans les anciennes sauvegardes qui ne les contenaient pas
 * @param hintsRemaining le nombre d'aides encore disponibles (0 si {@code config} est null)
 */
public record GameSave(
        Disc[][] board,
        Disc currentPlayer,
        GameState gameState,
        int moveCount,
        LocalDateTime savedAt,
        GameConfig config,
        int hintsRemaining
) {
    /**
     * Constructeur « ancien format » : sauvegarde sans réglages.
     * Il sert aux anciennes sauvegardes et garde le code existant compatible.
     */
    public GameSave(Disc[][] board,
                    Disc currentPlayer,
                    GameState gameState,
                    int moveCount,
                    LocalDateTime savedAt) {
        this(board, currentPlayer, gameState, moveCount, savedAt, null, 0);
    }

    // Constructeur compact : vérifie les données et fait une copie de la grille,
    // pour que la sauvegarde ne change plus si le tableau d'origine est modifié.
    @JsonCreator
    public GameSave {
        Objects.requireNonNull(board, "Board cannot be null");
        Objects.requireNonNull(currentPlayer, "Current player cannot be null");
        Objects.requireNonNull(gameState, "Game state cannot be null");
        Objects.requireNonNull(savedAt, "Save date cannot be null");

        if (board.length != 6) {
            throw new IllegalArgumentException("Board must have 6 rows");
        }

        // Copie défensive de la grille, case par case.
        Disc[][] copy = new Disc[6][7];

        for (int row = 0; row < 6; row++) {
            if (board[row] == null || board[row].length != 7) {
                throw new IllegalArgumentException("Board must have 7 columns");
            }

            copy[row] = board[row].clone();

            for (Disc disc : copy[row]) {
                Objects.requireNonNull(disc, "Board cannot contain null discs");
            }
        }

        if (currentPlayer == Disc.EMPTY) {
            throw new IllegalArgumentException("Current player cannot be EMPTY");
        }

        // 6 x 7 = 42 cases : on ne peut pas avoir joué plus de 42 coups.
        if (moveCount < 0 || moveCount > 42) {
            throw new IllegalArgumentException("Invalid move count");
        }

        // Les aides restantes ne peuvent pas dépasser le maximum des réglages.
        // (config peut valoir null pour une ancienne sauvegarde.)
        if (hintsRemaining < 0
                || (config != null && hintsRemaining > config.maxHints())) {
            throw new IllegalArgumentException("Invalid number of remaining hints");
        }

        board = copy;
    }

    /**
     * Retourne une COPIE de la grille (la sauvegarde reste inchangée).
     */
    @Override
    public Disc[][] board() {
        Disc[][] copy = new Disc[6][7];

        for (int row = 0; row < 6; row++) {
            copy[row] = board[row].clone();
        }

        return copy;
    }
}
