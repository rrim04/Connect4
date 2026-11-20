
package ma.connect4.controller;

import ma.connect4.ai.AlphaBetaSearch;
import ma.connect4.ai.MinimaxSearch;
import ma.connect4.ai.SearchResult;
import ma.connect4.ai.SearchStrategy;
import ma.connect4.engine.GameEngine;
import ma.connect4.model.Disc;
import ma.connect4.model.GameConfig;
import ma.connect4.model.GameState;
import ma.connect4.model.Move;

import java.util.Objects;

/**
 * Coordinates the game engine, artificial intelligence
 * and player actions.
 */
public final class GameController {

    private final GameEngine gameEngine;

    private GameConfig gameConfig;

    private SearchStrategy computerStrategy;
    private SearchStrategy helpStrategy;

    private int hintsRemaining;

    public GameController() {
        this.gameEngine = new GameEngine();
    }

    /**
     * Starts a new game using the selected configuration.
     */
    public void startNewGame(GameConfig config) {
        Objects.requireNonNull(config, "Game configuration cannot be null");

        this.gameConfig = config;

        this.computerStrategy = createStrategy(
                config.computerStrategy()
        );

        this.helpStrategy = createStrategy(
                config.helpStrategy()
        );

        this.hintsRemaining = config.maxHints();

        gameEngine.reset();
    }

    /**
     * Plays a move made by a human player.
     */
    public GameState playHumanMove(Move move) {
        ensureGameStarted();

        if (gameEngine.isFinished()) {
            throw new IllegalStateException("The game is already finished");
        }

        if (gameConfig.mode() == GameConfig.GameMode.HUMAN_VS_COMPUTER
                && gameEngine.getCurrentPlayer() == Disc.YELLOW) {
            throw new IllegalStateException(
                    "It is the computer's turn"
            );
        }

        return gameEngine.playMove(move);
    }

    /**
     * Plays the computer's move.
     */
    public SearchResult playComputerMove() {
        ensureGameStarted();

        if (gameConfig.mode() != GameConfig.GameMode.HUMAN_VS_COMPUTER) {
            throw new IllegalStateException(
                    "Computer opponent is not enabled"
            );
        }

        if (gameEngine.isFinished()) {
            throw new IllegalStateException("The game is already finished");
        }

        if (gameEngine.getCurrentPlayer() != Disc.YELLOW) {
            throw new IllegalStateException(
                    "It is not the computer's turn"
            );
        }

        SearchResult result = computerStrategy.findBestMove(
                gameEngine.getBoard(),
                Disc.YELLOW,
                gameConfig.searchDepth()
        );

        if (result == null || result.bestMove() == null) {
            throw new IllegalStateException(
                    "The computer could not find a valid move"
            );
        }

        gameEngine.playMove(result.bestMove());

        return result;
    }

    /**
     * Calculates a hint without changing the game board.
     */
    public Move getHint() {
        ensureGameStarted();

        if (gameEngine.isFinished()) {
            throw new IllegalStateException(
                    "Hints are unavailable after the game ends"
            );
        }

        if (hintsRemaining <= 0) {
            throw new IllegalStateException(
                    "No hints remaining"
            );
        }

        SearchResult result = helpStrategy.findBestMove(
                gameEngine.getBoard(),
                gameEngine.getCurrentPlayer(),
                gameConfig.searchDepth()
        );

        if (result == null || result.bestMove() == null) {
            throw new IllegalStateException(
                    "No hint is available"
            );
        }

        hintsRemaining--;

        return result.bestMove();
    }

    /**
     * Resets the current game while keeping its configuration.
     */
    public void restartGame() {
        ensureGameStarted();

        gameEngine.reset();
        hintsRemaining = gameConfig.maxHints();
    }

    /**
     * Creates the requested AI search strategy.
     */
    private SearchStrategy createStrategy(
            GameConfig.SearchStrategyType strategyType) {

        return switch (strategyType) {
            case MINIMAX -> new MinimaxSearch();
            case ALPHA_BETA -> new AlphaBetaSearch();
        };
    }

    private void ensureGameStarted() {
        if (gameConfig == null) {
            throw new IllegalStateException(
                    "Start a game before performing this action"
            );
        }
    }

    public GameEngine getGameEngine() {
        return gameEngine;
    }

    public GameConfig getGameConfig() {
        return gameConfig;
    }

    public int getHintsRemaining() {
        return hintsRemaining;
    }

    public boolean isGameFinished() {
        return gameEngine.isFinished();
    }

    public GameState getGameState() {
        return gameEngine.getGameState();
    }

    public Disc getCurrentPlayer() {
        return gameEngine.getCurrentPlayer();
    }
}
