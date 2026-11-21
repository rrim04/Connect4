package ma.connect4.ai;

/**
 * Minimax classique, sans élagage : explore tous les nœuds
 * jusqu'à la profondeur demandée.
 */
public final class MinimaxSearch extends Connect4Search {

    public MinimaxSearch() {
        super(false);   // false = pas de coupure alpha-bêta
    }

    @Override
    public String getName() {
        return "Minimax";
    }
}
