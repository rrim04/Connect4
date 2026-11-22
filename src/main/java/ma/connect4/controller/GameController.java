
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
import ma.connect4.persistence.GamePersistence;
import ma.connect4.persistence.GameSave;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Chef d'orchestre de la partie : relie le moteur de jeu, l'intelligence
 * artificielle et les actions du joueur.
 *
 * Elle applique les règles propres à l'application : tour de la machine,
 * nombre d'aides limité, réglages de la partie. L'interface graphique ne parle
 * qu'à cette classe.
 */
public final class GameController {

    // Moteur de jeu (grille, tour, fin de partie).
    private final GameEngine gameEngine;

    // Réglages de la partie en cours (null tant qu'aucune partie n'est lancée).
    private GameConfig gameConfig;

    // Algorithme de la machine et algorithme des aides (peuvent être différents).
    private SearchStrategy computerStrategy;
    private SearchStrategy helpStrategy;

    // Nombre d'aides encore disponibles.
    private int hintsRemaining;

    /**
     * Crée un contrôleur. Il faut appeler {@link #startNewGame} avant de jouer.
     */
    public GameController() {
        this.gameEngine = new GameEngine();
    }

    /**
     * Démarre une nouvelle partie avec les réglages choisis.
     *
     * @param config les réglages (mode, stratégies, profondeur, nombre d'aides)
     */
    public void startNewGame(GameConfig config) {
        Objects.requireNonNull(config, "Game configuration cannot be null");

        this.gameConfig = config;

        // Création des deux algorithmes selon les réglages.
        this.computerStrategy = createStrategy(
                config.computerStrategy()
        );

        this.helpStrategy = createStrategy(
                config.helpStrategy()
        );

        // Les aides sont remises au maximum à chaque nouvelle partie.
        this.hintsRemaining = config.maxHints();

        gameEngine.reset();
    }

    /**
     * Joue le coup d'un joueur humain.
     *
     * @throws IllegalStateException    si aucune partie n'est lancée, si elle est finie,
     *                                  ou si c'est le tour de la machine
     * @throws IllegalArgumentException si le coup est interdit (colonne pleine)
     */
    public GameState playHumanMove(Move move) {
        ensureGameStarted();

        if (gameEngine.isFinished()) {
            throw new IllegalStateException("The game is already finished");
        }

        // Contre la machine : l'humain joue le rouge, la machine joue le jaune.
        // On refuse donc un coup humain pendant le tour du jaune.
        if (gameConfig.mode() == GameConfig.GameMode.HUMAN_VS_COMPUTER
                && gameEngine.getCurrentPlayer() == Disc.YELLOW) {
            throw new IllegalStateException(
                    "It is the computer's turn"
            );
        }

        return gameEngine.playMove(move);
    }

    /**
     * Fait jouer la machine (jaune) : cherche son meilleur coup puis le joue.
     *
     * @return le résultat de la recherche (coup, score, nombre de nœuds explorés, durée)
     * @throws IllegalStateException si ce n'est pas le tour de la machine ou si la partie est finie
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

        // La machine cherche son meilleur coup avec la profondeur choisie (= difficulté).
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
     * Calcule une aide (le meilleur coup suggéré) sans modifier la grille.
     *
     * Chaque aide demandée consomme une des aides autorisées. L'aide est calculée
     * pour le joueur dont c'est le tour, avec la stratégie d'aide choisie.
     *
     * @return le coup conseillé
     * @throws IllegalStateException s'il ne reste plus d'aide ou si la partie est finie
     */
    public Move getHint() {
        ensureGameStarted();

        if (gameEngine.isFinished()) {
            throw new IllegalStateException(
                    "Hints are unavailable after the game ends"
            );
        }

        // Pas d'aide pendant le tour de la machine : le conseil serait calculé
        // pour la machine et non pour le joueur humain.
        if (isComputerTurn()) {
            throw new IllegalStateException(
                    "Hints are unavailable during the computer's turn"
            );
        }

        if (hintsRemaining <= 0) {
            throw new IllegalStateException(
                    "No hints remaining"
            );
        }

        // Même recherche que pour la machine, mais pour le joueur courant et sans jouer le coup.
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

        // Une aide n'est décomptée que si elle a bien été calculée.
        hintsRemaining--;

        return result.bestMove();
    }

    /**
     * Recommence la partie en gardant les mêmes réglages (les aides sont restaurées).
     */
    public void restartGame() {
        ensureGameStarted();

        gameEngine.reset();
        hintsRemaining = gameConfig.maxHints();
    }

    /**
     * Sauvegarde la partie COMPLÈTE : la grille, le joueur courant, l'état,
     * mais aussi les réglages (mode, stratégies, profondeur) et les aides restantes.
     *
     * @param persistence le mécanisme de sauvegarde (par exemple JSON)
     * @param file        le fichier de destination
     * @throws IllegalStateException si aucune partie n'est lancée
     * @throws IOException           en cas d'erreur d'écriture
     */
    public void saveGame(GamePersistence persistence, Path file)
            throws IOException {

        Objects.requireNonNull(persistence, "Persistence cannot be null");
        Objects.requireNonNull(file, "File cannot be null");
        ensureGameStarted();

        GameSave save = new GameSave(
                gameEngine.getBoard().toArray(),
                gameEngine.getCurrentPlayer(),
                gameEngine.getGameState(),
                gameEngine.getMoveCount(),
                LocalDateTime.now(),
                gameConfig,
                hintsRemaining
        );

        persistence.save(save, file);
    }

    /**
     * Charge une partie sauvegardée : la grille ET, si le fichier les contient,
     * les réglages et les aides restantes.
     *
     * Si la sauvegarde est invalide ou incohérente, une exception est lancée
     * et la partie en cours n'est pas modifiée. Une ancienne sauvegarde sans
     * réglages est acceptée : on garde alors les réglages et les aides actuels.
     *
     * @return vrai si une sauvegarde a été trouvée et restaurée, faux si le fichier n'existe pas
     * @throws IOException           en cas d'erreur de lecture
     * @throws IllegalStateException si l'ancienne sauvegarde n'a pas de réglages
     *                               et qu'aucune partie n'est lancée
     * @throws IllegalArgumentException si le contenu du fichier est incohérent
     */
    public boolean loadGame(GamePersistence persistence, Path file)
            throws IOException {

        Objects.requireNonNull(persistence, "Persistence cannot be null");
        Objects.requireNonNull(file, "File cannot be null");

        Optional<GameSave> loadedSave = persistence.load(file);

        // Pas de fichier de sauvegarde : rien n'est modifié.
        if (loadedSave.isEmpty()) {
            return false;
        }

        GameSave save = loadedSave.get();

        // Ancienne sauvegarde sans réglages : il faut déjà avoir des réglages
        // (donc une partie lancée) pour pouvoir continuer à jouer.
        if (save.config() == null) {
            ensureGameStarted();
        }

        // Vérifie la cohérence et restaure la grille. En cas de problème, une
        // exception est lancée ICI, avant que les réglages ne soient modifiés.
        gameEngine.restoreGame(save);

        // La grille est valide : on adopte aussi les réglages sauvegardés.
        if (save.config() != null) {
            this.gameConfig = save.config();
            this.computerStrategy = createStrategy(save.config().computerStrategy());
            this.helpStrategy = createStrategy(save.config().helpStrategy());
            this.hintsRemaining = save.hintsRemaining();
        }

        return true;
    }

    /**
     * Crée l'algorithme de recherche correspondant au choix de l'utilisateur.
     */
    private SearchStrategy createStrategy(
            GameConfig.SearchStrategyType strategyType) {

        return switch (strategyType) {
            case MINIMAX -> new MinimaxSearch();
            case ALPHA_BETA -> new AlphaBetaSearch();
        };
    }

    /**
     * Refuse toute action tant qu'aucune partie n'a été lancée.
     */
    private void ensureGameStarted() {
        if (gameConfig == null) {
            throw new IllegalStateException(
                    "Start a game before performing this action"
            );
        }
    }

    /**
     * Moteur de jeu (utilisé par la vue pour afficher la grille et sauvegarder).
     */
    public GameEngine getGameEngine() {
        return gameEngine;
    }

    /**
     * Réglages de la partie en cours.
     */
    public GameConfig getGameConfig() {
        return gameConfig;
    }

    /**
     * Nombre d'aides restantes.
     */
    public int getHintsRemaining() {
        return hintsRemaining;
    }

    /**
     * Vrai si la partie est terminée.
     */
    public boolean isGameFinished() {
        return gameEngine.isFinished();
    }

    /**
     * État actuel de la partie.
     */
    public GameState getGameState() {
        return gameEngine.getGameState();
    }

    /**
     * Joueur dont c'est le tour.
     */
    public Disc getCurrentPlayer() {
        return gameEngine.getCurrentPlayer();
    }

    /**
     * Vrai si c'est à la machine de jouer : mode humain contre machine,
     * partie non terminée, et tour du jaune (la machine joue toujours le jaune).
     *
     * Sert à la vue pour savoir si la machine doit jouer, notamment après le
     * chargement d'une partie sauvegardée au milieu d'un tour de la machine.
     */
    public boolean isComputerTurn() {
        return gameConfig != null
                && gameConfig.mode() == GameConfig.GameMode.HUMAN_VS_COMPUTER
                && !gameEngine.isFinished()
                && gameEngine.getCurrentPlayer() == Disc.YELLOW;
    }
}
