
package ma.connect4.ai;

import ma.connect4.model.Move;

/**
 * Résultat d'une recherche de l'IA, avec ses statistiques de performance.
 *
 * @param bestMove     meilleur coup trouvé (null s'il n'y a aucun coup possible)
 * @param score        score du coup (positif : avantageux pour le joueur qui cherche)
 * @param visitedNodes nombre de positions explorées (sert à comparer Minimax et Alpha-Bêta)
 * @param durationNanos durée de la recherche en nanosecondes
 */
public record SearchResult(
        Move bestMove,
        int score,
        long visitedNodes,
        long durationNanos
) {
    public SearchResult {
        // Les statistiques ne peuvent pas être négatives.
        if (visitedNodes < 0 || durationNanos < 0) {
            throw new IllegalArgumentException(
                    "Search metrics cannot be negative"
            );
        }
    }

    /**
     * Durée de la recherche convertie en millisecondes.
     */
    public double durationMillis() {
        return durationNanos / 1_000_000.0;
    }
}
