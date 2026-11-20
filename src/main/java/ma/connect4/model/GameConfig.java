
package ma.connect4.model;

import java.util.Objects;

/**
 * Stores the configuration selected before starting a game.
 */
public record GameConfig(
        GameMode mode,
        SearchStrategyType computerStrategy,
        SearchStrategyType helpStrategy,
        int searchDepth,
        int maxHints
) {
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

    public enum GameMode {
        HUMAN_VS_HUMAN,
        HUMAN_VS_COMPUTER
    }

    public enum SearchStrategyType {
        MINIMAX,
        ALPHA_BETA
    }
}
