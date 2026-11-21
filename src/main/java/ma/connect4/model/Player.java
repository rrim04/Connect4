
package ma.connect4.model;

import java.util.Objects;

/**
 * Joueur de la partie.
 *
 * @param name     nom du joueur (non vide)
 * @param disc     couleur de ses pions (pas EMPTY)
 * @param computer vrai si le joueur est piloté par la machine
 */
public record Player(
        String name,
        Disc disc,
        boolean computer
) {
    // Constructeur compact : vérifie que le nom et la couleur sont valides.
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
