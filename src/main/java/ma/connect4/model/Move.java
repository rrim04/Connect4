
package ma.connect4.model;

/**
 * Coup de Puissance 4 : la colonne (de 0 à 6) dans laquelle on lâche un pion.
 *
 * @param column numéro de colonne, entre 0 (gauche) et 6 (droite)
 */
public record Move(int column) {

    // Constructeur compact : refuse toute colonne hors de la grille.
    public Move {
        if (column < 0 || column >= Board.COLUMNS) {
            throw new IllegalArgumentException("Column must be between 0 and 6");
        }
    }
}
