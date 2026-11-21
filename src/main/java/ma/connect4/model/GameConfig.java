
package ma.connect4.model;

import java.util.Objects;

/**
 * Réglages choisis avant le début d'une partie.
 *
 * @param mode             deux joueurs humains ou humain contre machine
 * @param computerStrategy algorithme utilisé par la machine pour jouer
 * @param helpStrategy     algorithme utilisé pour calculer les aides
 * @param searchDepth      profondeur de recherche = niveau de difficulté (1 à 10)
 * @param maxHints         nombre d'aides autorisées pendant la partie
 */
public record GameConfig(
        GameMode mode,
        SearchStrategyType computerStrategy,
        SearchStrategyType helpStrategy,
        int searchDepth,
        int maxHints
) {
    // Constructeur compact : vérifie la validité des réglages à la création.
    public GameConfig {
        Objects.requireNonNull(mode);
        Objects.requireNonNull(computerStrategy);
        Objects.requireNonNull(helpStrategy);

        if (searchDepth < 1 || searchDepth > 10) {
            throw new IllegalArgumentException("Search depth must be between 1 and 10");
        }

        if (maxHints < 0) {
            throw new IllegalArgumentException("Number of hints cannot be negative");
        }
    }

    /**
     * Mode de jeu.
     */
    public enum GameMode {
        // Deux joueurs humains
        HUMAN_VS_HUMAN,
        // Un humain (rouge) contre la machine (jaune)
        HUMAN_VS_COMPUTER
    }

    /**
     * Algorithme de recherche disponible pour la machine et pour l'aide.
     */
    public enum SearchStrategyType {
        // Minimax sans élagage
        MINIMAX,
        // Minimax avec élagage Alpha-Bêta
        ALPHA_BETA
    }
}
