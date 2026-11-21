package ma.connect4.search;

/**
 * Moteur de recherche générique pour les jeux à deux joueurs
 * (Minimax et Alpha-Bêta).
 *
 * <p>La classe ne connaît rien du Puissance 4 : tout ce qui dépend du jeu
 * est délégué aux méthodes abstraites ci-dessous (drawnPosition,
 * wonPosition, positionEvaluation, possibleMoves, makeMove,
 * reachedMaxDepth, printPosition), qui sont implémentées par une
 * sous-classe (voir {@code Connect4Search}).</p>
 *
 * <p>Conformément à la diapositive 19 du cours, l'ancienne méthode
 * {@code alphaBetaHelper} est remplacée par deux méthodes qui
 * s'appellent mutuellement : {@link #max_Value} et {@link #min_Value}.</p>
 *
 * <p>Le même code sert pour Minimax et pour Alpha-Bêta : l'élagage
 * (coupure alpha/bêta) n'est actif que si {@code alphaBetaEnabled}
 * vaut vrai.</p>
 */
public abstract class GameSearch {

    /** Score d'une position gagnante (on y ajoute la profondeur restante). */
    protected static final float WIN_SCORE = 1_000_000f;

    /** Vrai : élagage Alpha-Bêta. Faux : Minimax pur (aucune coupure). */
    private final boolean alphaBetaEnabled;

    /** Nombre de nœuds explorés pendant la dernière recherche. */
    private long visitedNodes;

    protected GameSearch(boolean alphaBetaEnabled) {
        this.alphaBetaEnabled = alphaBetaEnabled;
    }

    // ------------------------------------------------------------------
    // Méthodes dépendantes du jeu (à implémenter dans la sous-classe)
    // ------------------------------------------------------------------

    /** La position est-elle un match nul ? */
    public abstract boolean drawnPosition(Position p);

    /** Le joueur {@code player} (PROGRAM ou HUMAN) a-t-il gagné ? */
    public abstract boolean wonPosition(Position p, boolean player);

    /** Évalue la position du point de vue du joueur PROGRAM (heuristique). */
    public abstract float positionEvaluation(Position p, boolean player);

    /** Affiche la position (utile pour le débogage). */
    public abstract void printPosition(Position p);

    /** Retourne toutes les positions atteignables en un coup par {@code player}. */
    public abstract Position[] possibleMoves(Position p, boolean player);

    /** Joue le coup {@code move} pour {@code player} et retourne la nouvelle position. */
    public abstract Position makeMove(Position p, boolean player, Move move);

    /** La profondeur maximale de recherche est-elle atteinte ? */
    public abstract boolean reachedMaxDepth(Position p, int depth);

    // ------------------------------------------------------------------
    // Algorithme de recherche
    // ------------------------------------------------------------------

    /**
     * Résultat d'une recherche : la meilleure position suivante
     * (celle qui contient le coup à jouer) et son score.
     */
    public record Outcome(Position bestPosition, float score) {
    }

    /**
     * Point d'entrée de la recherche (niveau racine).
     *
     * <p>On essaie chaque coup possible de {@code player}, puis on appelle
     * min_Value (si player = PROGRAM) ou max_Value (si player = HUMAN)
     * pour évaluer la réponse de l'adversaire.</p>
     *
     * @param depth  profondeur de recherche (nombre de demi-coups)
     * @param p      position de départ
     * @param player joueur dont c'est le tour (PROGRAM ou HUMAN)
     * @return la meilleure position suivante et son score ;
     *         {@code bestPosition} vaut null s'il n'y a aucun coup possible
     */
    public Outcome alphaBeta(int depth, Position p, boolean player) {
        visitedNodes = 0;

        Position[] children = possibleMoves(p, player);
        if (children.length == 0) {
            return new Outcome(null, 0f);
        }

        boolean maximizing = (player == Position.PROGRAM);

        Position best = null;
        float bestScore = maximizing
                ? Float.NEGATIVE_INFINITY
                : Float.POSITIVE_INFINITY;

        // À la racine, une seule des deux bornes évolue :
        // alpha si on maximise, bêta si on minimise.
        float alpha = Float.NEGATIVE_INFINITY;
        float beta = Float.POSITIVE_INFINITY;

        for (Position child : children) {
            if (maximizing) {
                float score = min_Value(child, depth - 1, alpha, beta);
                if (score > bestScore) {          // strict : en cas d'égalité, on garde le 1er coup
                    bestScore = score;
                    best = child;
                }
                alpha = Math.max(alpha, bestScore);
            } else {
                float score = max_Value(child, depth - 1, alpha, beta);
                if (score < bestScore) {
                    bestScore = score;
                    best = child;
                }
                beta = Math.min(beta, bestScore);
            }
        }

        return new Outcome(best, bestScore);
    }

    /**
     * Nœud MAX : c'est au joueur PROGRAM de jouer, il choisit le
     * score le plus ÉLEVÉ parmi les réponses possibles.
     *
     * @param p     position du nœud
     * @param depth profondeur restante
     * @param alpha meilleure valeur déjà garantie pour MAX sur ce chemin
     * @param beta  meilleure valeur déjà garantie pour MIN sur ce chemin
     */
    protected float max_Value(Position p, int depth, float alpha, float beta) {
        visitedNodes++;

        // 1) Position terminale (victoire, défaite, nul) ?
        Float terminal = terminalScore(p, depth);
        if (terminal != null) {
            return terminal;
        }

        // 2) Limite de profondeur : on utilise l'heuristique.
        if (reachedMaxDepth(p, depth)) {
            return positionEvaluation(p, Position.PROGRAM);
        }

        // 3) Sinon : on essaie tous les coups de MAX et on garde le meilleur.
        float value = Float.NEGATIVE_INFINITY;

        for (Position child : possibleMoves(p, Position.PROGRAM)) {
            value = Math.max(value, min_Value(child, depth - 1, alpha, beta));
            alpha = Math.max(alpha, value);

            // Coupure bêta : MIN ne laissera jamais MAX atteindre cette branche.
            if (alphaBetaEnabled && alpha >= beta) {
                break;
            }
        }

        return value;
    }

    /**
     * Nœud MIN : c'est à l'adversaire de jouer, il choisit le
     * score le plus BAS parmi les réponses possibles.
     *
     * @param p     position du nœud
     * @param depth profondeur restante
     * @param alpha meilleure valeur déjà garantie pour MAX sur ce chemin
     * @param beta  meilleure valeur déjà garantie pour MIN sur ce chemin
     */
    protected float min_Value(Position p, int depth, float alpha, float beta) {
        visitedNodes++;

        Float terminal = terminalScore(p, depth);
        if (terminal != null) {
            return terminal;
        }

        if (reachedMaxDepth(p, depth)) {
            return positionEvaluation(p, Position.PROGRAM);
        }

        float value = Float.POSITIVE_INFINITY;

        for (Position child : possibleMoves(p, Position.HUMAN)) {
            value = Math.min(value, max_Value(child, depth - 1, alpha, beta));
            beta = Math.min(beta, value);

            // Coupure alpha : MAX a déjà mieux ailleurs, inutile de continuer.
            if (alphaBetaEnabled && alpha >= beta) {
                break;
            }
        }

        return value;
    }

    /**
     * Score d'une position terminale, ou null si la partie continue.
     * Ajouter la profondeur restante favorise les victoires rapides
     * et retarde les défaites.
     */
    private Float terminalScore(Position p, int depth) {
        if (wonPosition(p, Position.PROGRAM)) {
            return WIN_SCORE + depth;
        }
        if (wonPosition(p, Position.HUMAN)) {
            return -WIN_SCORE - depth;
        }
        if (drawnPosition(p)) {
            return 0f;
        }
        return null;
    }

    /** Nombre de nœuds explorés lors de la dernière recherche. */
    public long getVisitedNodes() {
        return visitedNodes;
    }
}
