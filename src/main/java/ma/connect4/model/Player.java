
package ma.connect4.model;

import java.util.Objects;

/**
 * Represents a player in the game.
 */
public record Player(
        String name,
        Disc disc,
        boolean computer
) {
    public Player {
        Objects.requireNonNull(name, "Player name cannot be null");
        Objects.requireNonNull(disc, "Player disc cannot be null");

        if (name.isBlank()) {
            throw new IllegalArgumentException("Player name cannot be blank");
        }

        if (disc == Disc.EMPTY) {
            throw new IllegalArgumentException("A player cannot use EMPTY disc");
        }
    }
}
