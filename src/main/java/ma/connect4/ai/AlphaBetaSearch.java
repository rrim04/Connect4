package ma.connect4.ai;

/**
 * Minimax avec élagage Alpha-Bêta : même résultat que Minimax,
 * mais en explorant beaucoup moins de nœuds.
 */
public final class AlphaBetaSearch extends Connect4Search {

    public AlphaBetaSearch() {
        super(true);    // true = coupures alpha-bêta actives
    }

    @Override
    public String getName() {
        return "Alpha-Beta";
    }
}
