
package ma.connect4.ai;

/**
 * Minimax search optimized with Alpha-Beta pruning.
 */
public final class AlphaBetaSearch extends GameSearch {

    public AlphaBetaSearch() {
        super(true);
    }

    @Override
    public String getName() {
        return "Alpha-Beta";
    }
}
