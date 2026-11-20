
package ma.connect4.ai;

import ma.connect4.model.Move;

/**
 * Stores the result and performance information of an AI search.
 */
public record SearchResult(
        Move bestMove,
        int score,
        long visitedNodes,
        long durationNanos
) {
    public SearchResult {
        if (visitedNodes < 0 || durationNanos < 0) {
            throw new IllegalArgumentException(
                    "Search metrics cannot be negative"
            );
        }
    }

    public double durationMillis() {
        return durationNanos / 1_000_000.0;
    }
}
