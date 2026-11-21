
package ma.connect4.model;

/**
 * État d'une partie : en cours, gagnée par l'un des joueurs, ou match nul.
 */
public enum GameState {
    // La partie continue
    IN_PROGRESS,
    // Le rouge a aligné quatre pions
    RED_WON,
    // Le jaune a aligné quatre pions
    YELLOW_WON,
    // Grille pleine sans gagnant
    DRAW;

    /**
     * Vrai si la partie est terminée (victoire ou match nul).
     */
    public boolean isFinished() {
        return this != IN_PROGRESS;
    }
}
