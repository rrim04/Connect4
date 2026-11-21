
package ma.connect4.model;

/**
 * Contenu possible d'une case de la grille : vide, pion rouge ou pion jaune.
 */
public enum Disc {
    // Case vide
    EMPTY,
    // Pion rouge (le rouge joue en premier)
    RED,
    // Pion jaune
    YELLOW;

    /**
     * Retourne la couleur adverse ({@code EMPTY} n'a pas d'adversaire et reste {@code EMPTY}).
     */
    public Disc opponent() {
        return switch (this) {
            case RED -> YELLOW;
            case YELLOW -> RED;
            case EMPTY -> EMPTY;
        };
    }
}
