
package ma.connect4.ai;

import ma.connect4.model.Board;
import ma.connect4.model.Disc;

/**
 * Contrat commun des stratégies de recherche de l'intelligence artificielle
 * (Minimax, Alpha-Bêta).
 *
 * Le contrôleur ne dépend que de cette interface : on peut donc changer
 * d'algorithme sans modifier le reste de l'application.
 */
public interface SearchStrategy {

    /**
     * Cherche le meilleur coup à jouer dans la position donnée.
     *
     * @param board            la grille actuelle (elle n'est pas modifiée)
     * @param maximizingPlayer couleur du joueur pour lequel on cherche le coup
     * @param depth            profondeur de recherche (au moins 1)
     * @return le coup choisi, son score et des statistiques de la recherche
     */
    SearchResult findBestMove(
            Board board,
            Disc maximizingPlayer,
            int depth
    );

    /**
     * Nom lisible de l'algorithme (par exemple « Minimax »).
     */
    String getName();
}
