
package ma.connect4.engine;

import ma.connect4.model.Board;
import ma.connect4.model.Disc;
import ma.connect4.model.GameState;
import ma.connect4.model.Move;
import ma.connect4.persistence.GamePersistence;
import ma.connect4.persistence.GameSave;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Controls the progression of a Connect Four game.
 * Supports saving and restoring games.
 */
public final class GameEngine {

    private Board board;
    private Disc currentPlayer;
    private GameState gameState;
    private int moveCount;

    public GameEngine() {
        reset();
    }

    /**
     * Plays a move for the current player.
     *
     * @return the updated game state
     */
    public GameState playMove(Move move) {
        Objects.requireNonNull(move, "Move cannot be null");

        if (gameState.isFinished()) {
            throw new IllegalStateException("The game is already finished");
        }

        if (!GameRules.isLegalMove(board, move)) {
            throw new IllegalArgumentException("Illegal move");
        }

        Disc playedDisc = currentPlayer;

        int row = board.dropDisc(move.column(), playedDisc);
        moveCount++;

        gameState = GameRules.determineGameState(
                board,
                playedDisc,
                row,
                move.column()
        );

        if (!gameState.isFinished()) {
            currentPlayer = GameRules.getOpponent(currentPlayer);
        }

        return gameState;
    }

    /**
     * Resets the game to its initial state.
     */
    public void reset() {
        board = new Board();
        currentPlayer = Disc.RED;
        gameState = GameState.IN_PROGRESS;
        moveCount = 0;
    }

    /**
     * Saves the current game.
     */
    public void saveGame(GamePersistence persistence, Path file)
            throws IOException {

        Objects.requireNonNull(persistence, "Persistence cannot be null");
        Objects.requireNonNull(file, "File cannot be null");

        GameSave save = new GameSave(
                board.toArray(),
                currentPlayer,
                gameState,
                moveCount,
                LocalDateTime.now()
        );

        persistence.save(save, file);
    }

    /**
     * Loads a saved game.
     *
     * @return true if a save was found and restored
     */
    public boolean loadGame(GamePersistence persistence, Path file)
            throws IOException {

        Objects.requireNonNull(persistence, "Persistence cannot be null");
        Objects.requireNonNull(file, "File cannot be null");

        Optional<GameSave> loadedSave = persistence.load(file);

        if (loadedSave.isEmpty()) {
            return false;
        }

        restoreGame(loadedSave.get());
        return true;
    }

    /**
     * Restores a game from saved data.
     */
    public void restoreGame(GameSave save) {
        Objects.requireNonNull(save, "Save cannot be null");

        Disc[][] savedCells = save.board();
        Board restoredBoard = new Board();

        int actualMoveCount = 0;

        // Rebuild each column from bottom to top.
        for (int column = 0; column < Board.COLUMNS; column++) {

            boolean emptyCellFound = false;

            for (int row = Board.ROWS - 1; row >= 0; row--) {

                Disc disc = savedCells[row][column];

                if (disc == null) {
                    throw new IllegalArgumentException(
                            "Saved board contains null cells"
                    );
                }

                if (disc == Disc.EMPTY) {
                    emptyCellFound = true;
                } else {
                    if (emptyCellFound) {
                        throw new IllegalArgumentException(
                                "Invalid saved board: floating disc detected"
                        );
                    }

                    restoredBoard.dropDisc(column, disc);
                    actualMoveCount++;
                }
            }
        }

        if (actualMoveCount != save.moveCount()) {
            throw new IllegalArgumentException(
                    "Saved move count does not match the board"
            );
        }

        validateSavedState(restoredBoard, save);

        // Update the engine only after all validations succeed.
        this.board = restoredBoard;
        this.currentPlayer = save.currentPlayer();
        this.gameState = save.gameState();
        this.moveCount = save.moveCount();
    }

    /**
     * Checks that the saved game state matches the board.
     */
    private void validateSavedState(Board restoredBoard, GameSave save) {

        boolean redWon = WinDetector.hasWon(restoredBoard, Disc.RED);
        boolean yellowWon = WinDetector.hasWon(restoredBoard, Disc.YELLOW);

        if (redWon && yellowWon) {
            throw new IllegalArgumentException(
                    "Invalid save: both players cannot win"
            );
        }

        switch (save.gameState()) {

            case IN_PROGRESS -> {
                if (redWon || yellowWon || restoredBoard.isFull()) {
                    throw new IllegalArgumentException(
                            "Invalid saved game state"
                    );
                }

                Disc expectedPlayer = save.moveCount() % 2 == 0
                        ? Disc.RED
                        : Disc.YELLOW;

                if (save.currentPlayer() != expectedPlayer) {
                    throw new IllegalArgumentException(
                            "Invalid current player in save"
                    );
                }
            }

            case RED_WON -> {
                if (!redWon || yellowWon) {
                    throw new IllegalArgumentException(
                            "Invalid RED_WON state"
                    );
                }
            }

            case YELLOW_WON -> {
                if (!yellowWon || redWon) {
                    throw new IllegalArgumentException(
                            "Invalid YELLOW_WON state"
                    );
                }
            }

            case DRAW -> {
                if (!restoredBoard.isFull() || redWon || yellowWon) {
                    throw new IllegalArgumentException(
                            "Invalid DRAW state"
                    );
                }
            }
        }
    }

    /**
     * Returns a defensive copy of the board.
     */
    public Board getBoard() {
        return board.copy();
    }

    public Disc getCurrentPlayer() {
        return currentPlayer;
    }

    public GameState getGameState() {
        return gameState;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public boolean isFinished() {
        return gameState.isFinished();
    }

    public boolean isDraw() {
        return gameState == GameState.DRAW;
    }

    public boolean isWinning(Disc disc) {
        return switch (disc) {
            case RED -> gameState == GameState.RED_WON;
            case YELLOW -> gameState == GameState.YELLOW_WON;
            case EMPTY -> false;
        };
    }
}
