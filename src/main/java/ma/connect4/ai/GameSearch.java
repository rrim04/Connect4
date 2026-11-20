
package ma.connect4.ai;

import ma.connect4.engine.WinDetector;
import ma.connect4.model.Board;
import ma.connect4.model.Disc;
import ma.connect4.model.Move;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Common search engine for Minimax and Alpha-Beta.
 *
 * MAX tries to maximize the score.
 * MIN tries to minimize the score.
 */
public abstract class GameSearch implements SearchStrategy {

    protected static final int WIN_SCORE = 1_000_000;

    private final boolean alphaBetaEnabled;

    private long visitedNodes;

    protected GameSearch(boolean alphaBetaEnabled) {
        this.alphaBetaEnabled = alphaBetaEnabled;
    }

    @Override
    public SearchResult findBestMove(
            Board originalBoard,
            Disc maximizingPlayer,
            int depth
    ) {
        Objects.requireNonNull(originalBoard);
        Objects.requireNonNull(maximizingPlayer);

        if (maximizingPlayer == Disc.EMPTY) {
            throw new IllegalArgumentException("Invalid AI player");
        }

        if (depth < 1) {
            throw new IllegalArgumentException("Depth must be at least 1");
        }

        long startTime = System.nanoTime();
        visitedNodes = 0;

        Board board = originalBoard.copy();
        List<Move> moves = orderedMoves(board);

        if (moves.isEmpty()) {
            return new SearchResult(
                    null, 0, 1, System.nanoTime() - startTime
            );
        }

        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        for (Move move : moves) {
            Board child = board.copy();
            child.dropDisc(move.column(), maximizingPlayer);

            int score = min_Value(
                    child,
                    depth - 1,
                    maximizingPlayer,
                    alpha,
                    beta
            );

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }

            alpha = Math.max(alpha, bestScore);
        }

        return new SearchResult(
                bestMove,
                bestScore,
                visitedNodes,
                System.nanoTime() - startTime
        );
    }

    /**
     * MAX node: selects the highest possible score.
     */
    protected int max_Value(
            Board board,
            int depth,
            Disc maximizingPlayer,
            int alpha,
            int beta
    ) {
        visitedNodes++;

        Integer terminalScore = terminalScore(
                board, maximizingPlayer, depth
        );

        if (terminalScore != null) {
            return terminalScore;
        }

        if (depth == 0) {
            return EvaluationFunction.evaluate(board, maximizingPlayer);
        }

        int value = Integer.MIN_VALUE;

        for (Move move : orderedMoves(board)) {
            Board child = board.copy();
            child.dropDisc(move.column(), maximizingPlayer);

            value = Math.max(
                    value,
                    min_Value(
                            child,
                            depth - 1,
                            maximizingPlayer,
                            alpha,
                            beta
                    )
            );

            alpha = Math.max(alpha, value);

            if (alphaBetaEnabled && alpha >= beta) {
                break;
            }
        }

        return value;
    }

    /**
     * MIN node: selects the lowest possible score.
     */
    protected int min_Value(
            Board board,
            int depth,
            Disc maximizingPlayer,
            int alpha,
            int beta
    ) {
        visitedNodes++;

        Integer terminalScore = terminalScore(
                board, maximizingPlayer, depth
        );

        if (terminalScore != null) {
            return terminalScore;
        }

        if (depth == 0) {
            return EvaluationFunction.evaluate(board, maximizingPlayer);
        }

        int value = Integer.MAX_VALUE;
        Disc opponent = maximizingPlayer.opponent();

        for (Move move : orderedMoves(board)) {
            Board child = board.copy();
            child.dropDisc(move.column(), opponent);

            value = Math.min(
                    value,
                    max_Value(
                            child,
                            depth - 1,
                            maximizingPlayer,
                            alpha,
                            beta
                    )
            );

            beta = Math.min(beta, value);

            if (alphaBetaEnabled && alpha >= beta) {
                break;
            }
        }

        return value;
    }

    private Integer terminalScore(
            Board board,
            Disc maximizingPlayer,
            int depth
    ) {
        if (WinDetector.hasWon(board, maximizingPlayer)) {
            return WIN_SCORE + depth;
        }

        if (WinDetector.hasWon(board, maximizingPlayer.opponent())) {
            return -WIN_SCORE - depth;
        }

        if (board.isFull()) {
            return 0;
        }

        return null;
    }

    /**
     * Searches central columns first to improve move ordering.
     */
    private List<Move> orderedMoves(Board board) {
        List<Move> moves = new ArrayList<>(board.getLegalMoves());

        moves.sort(Comparator.comparingInt(
                move -> Math.abs(move.column() - Board.COLUMNS / 2)
        ));

        return moves;
    }
}
