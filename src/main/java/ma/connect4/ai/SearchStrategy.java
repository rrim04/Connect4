
package ma.connect4.ai;

import ma.connect4.model.Board;
import ma.connect4.model.Disc;

/**
 * Common contract for Connect Four AI search strategies.
 */
public interface SearchStrategy {

    SearchResult findBestMove(
            Board board,
            Disc maximizingPlayer,
            int depth
    );

    String getName();
}
