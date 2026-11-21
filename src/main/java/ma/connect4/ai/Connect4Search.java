package ma.connect4.ai;

import ma.connect4.engine.WinDetector;
import ma.connect4.model.Board;
import ma.connect4.model.Disc;
import ma.connect4.search.GameSearch;
import ma.connect4.search.Move;
import ma.connect4.search.Position;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Implémentation du Puissance 4 pour le moteur générique {@link GameSearch}.
 *
 * Cette classe fournit les méthodes propres au jeu (qui a gagné, quels
 * coups sont possibles, comment évaluer une position...). L'algorithme
 * lui-même (max_Value / min_Value) est hérité de {@link GameSearch}.
 *
 * Elle implémente aussi {@link SearchStrategy}, ce qui permet au
 * contrôleur de l'utiliser sans aucune modification.
 */
public abstract class Connect4Search extends GameSearch implements SearchStrategy {

    /**
     * Couleur du joueur « PROGRAM » (celui que l'on cherche à faire gagner)
     * pour la recherche en cours. L'adversaire (HUMAN) a la couleur opposée.
     */
    private Disc programDisc;

    protected Connect4Search(boolean alphaBetaEnabled) {
        super(alphaBetaEnabled);
    }

    // ------------------------------------------------------------------
    // Contrat SearchStrategy (utilisé par GameController)
    // ------------------------------------------------------------------

    @Override
    public SearchResult findBestMove(Board originalBoard,
                                     Disc maximizingPlayer,
                                     int depth) {
        Objects.requireNonNull(originalBoard);
        Objects.requireNonNull(maximizingPlayer);

        if (maximizingPlayer == Disc.EMPTY) {
            throw new IllegalArgumentException("Invalid AI player");
        }
        if (depth < 1) {
            throw new IllegalArgumentException("Depth must be at least 1");
        }

        long startTime = System.nanoTime();

        // Le joueur qui cherche son meilleur coup est le joueur PROGRAM (MAX).
        programDisc = maximizingPlayer;

        Connect4Position start = new Connect4Position(originalBoard.copy());
        Outcome outcome = alphaBeta(depth, start, Position.PROGRAM);

        // Aucun coup possible (grille pleine).
        if (outcome.bestPosition() == null) {
            return new SearchResult(null, 0, 1, System.nanoTime() - startTime);
        }

        int column = ((Connect4Position) outcome.bestPosition()).getLastColumn();

        return new SearchResult(
                // Nom complet : ici « Move » désigne la classe abstraite du cours,
                // alors que SearchResult utilise le Move du modèle.
                new ma.connect4.model.Move(column),
                Math.round(outcome.score()),
                getVisitedNodes(),
                System.nanoTime() - startTime
        );
    }

    // ------------------------------------------------------------------
    // Méthodes de GameSearch propres au Puissance 4
    // ------------------------------------------------------------------

    @Override
    public boolean drawnPosition(Position p) {
        // Grille pleine (les victoires sont testées avant dans GameSearch).
        return board(p).isFull();
    }

    @Override
    public boolean wonPosition(Position p, boolean player) {
        return WinDetector.hasWon(board(p), discOf(player));
    }

    @Override
    public float positionEvaluation(Position p, boolean player) {
        return EvaluationFunction.evaluate(board(p), discOf(player));
    }

    @Override
    public Position[] possibleMoves(Position p, boolean player) {
        List<Integer> columns = new ArrayList<>();

        for (ma.connect4.model.Move legal : board(p).getLegalMoves()) {
            columns.add(legal.column());
        }

        // Colonnes centrales d'abord : de meilleures coupures Alpha-Bêta.
        columns.sort(Comparator.comparingInt(c -> Math.abs(c - Board.COLUMNS / 2)));

        Position[] result = new Position[columns.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = makeMove(p, player, new Connect4Move(columns.get(i)));
        }
        return result;
    }

    @Override
    public Position makeMove(Position p, boolean player, Move move) {
        int column = ((Connect4Move) move).getColumn();

        // On joue toujours sur une COPIE : la position d'origine reste intacte.
        Board copy = board(p).copy();
        copy.dropDisc(column, discOf(player));

        return new Connect4Position(copy, column);
    }

    @Override
    public boolean reachedMaxDepth(Position p, int depth) {
        return depth <= 0;
    }

    @Override
    public void printPosition(Position p) {
        Board board = board(p);

        for (int row = 0; row < Board.ROWS; row++) {
            StringBuilder line = new StringBuilder();
            for (int col = 0; col < Board.COLUMNS; col++) {
                line.append(switch (board.getCell(row, col)) {
                    case RED -> 'R';
                    case YELLOW -> 'Y';
                    case EMPTY -> '.';
                });
                line.append(' ');
            }
            System.out.println(line);
        }
        System.out.println();
    }

    // ------------------------------------------------------------------
    // Utilitaires internes
    // ------------------------------------------------------------------

    private static Board board(Position p) {
        return ((Connect4Position) p).getBoard();
    }

    /** Traduit PROGRAM / HUMAN en couleur de pion. */
    private Disc discOf(boolean player) {
        return player == Position.PROGRAM ? programDisc : programDisc.opponent();
    }
}
