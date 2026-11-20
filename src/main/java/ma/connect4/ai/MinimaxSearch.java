
package ma.connect4.ai;

/**
 * Standard Minimax search without pruning.
 */
public final class MinimaxSearch extends GameSearch {

    public MinimaxSearch() {
        super(false);
    }

    @Override
    public String getName() {
        return "Minimax";
    }
}
