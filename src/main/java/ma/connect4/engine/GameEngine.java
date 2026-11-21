
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
 * Moteur de la partie : applique les coups, change de joueur et détecte la fin.
 *
 * Il gère aussi la sauvegarde et la restauration d'une partie. Il ne sait rien
 * de l'interface ni de l'intelligence artificielle (voir {@code GameController}).
 */
public final class GameEngine {

    // État courant de la partie :
    // la grille, le joueur dont c'est le tour, l'état (en cours / gagnée / nulle)
    // et le nombre de coups déjà joués.
    private Board board;
    private Disc currentPlayer;
    private GameState gameState;
    private int moveCount;

    /**
     * Crée un moteur avec une partie neuve.
     */
    public GameEngine() {
        reset();
    }

    /**
     * Joue un coup pour le joueur courant.
     *
     * @param move le coup à jouer
     * @return l'état de la partie après le coup
     * @throws IllegalStateException    si la partie est déjà terminée
     * @throws IllegalArgumentException si le coup est interdit (colonne pleine)
     */
    public GameState playMove(Move move) {
        Objects.requireNonNull(move, "Move cannot be null");

        // Refus : on ne joue plus une fois la partie terminée.
        if (gameState.isFinished()) {
            throw new IllegalStateException("The game is already finished");
        }

        // Refus : colonne pleine.
        if (!GameRules.isLegalMove(board, move)) {
            throw new IllegalArgumentException("Illegal move");
        }

        // On mémorise qui joue : currentPlayer peut changer plus bas.
        Disc playedDisc = currentPlayer;

        int row = board.dropDisc(move.column(), playedDisc);
        moveCount++;

        // Victoire, nul ou partie en cours ?
        gameState = GameRules.determineGameState(
                board,
                playedDisc,
                row,
                move.column()
        );

        // Le tour passe à l'adversaire seulement si la partie continue.
        if (!gameState.isFinished()) {
            currentPlayer = GameRules.getOpponent(currentPlayer);
        }

        return gameState;
    }

    /**
     * Remet la partie à zéro : grille vide, le rouge commence.
     */
    public void reset() {
        board = new Board();
        currentPlayer = Disc.RED;
        gameState = GameState.IN_PROGRESS;
        moveCount = 0;
    }

    /**
     * Sauvegarde la partie en cours dans un fichier.
     *
     * @param persistence le mécanisme de sauvegarde (par exemple JSON)
     * @param file        le fichier de destination
     * @throws IOException en cas d'erreur d'écriture
     */
    public void saveGame(GamePersistence persistence, Path file)
            throws IOException {

        Objects.requireNonNull(persistence, "Persistence cannot be null");
        Objects.requireNonNull(file, "File cannot be null");

        // On rassemble l'état de la partie dans un objet de sauvegarde.
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
     * Charge une partie sauvegardée.
     *
     * @return vrai si un fichier a été trouvé et la partie restaurée, faux si le fichier n'existe pas
     * @throws IOException              en cas d'erreur de lecture
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

        restoreGame(loadedSave.get());
        return true;
    }

    /**
     * Restaure une partie à partir de données sauvegardées.
     *
     * Le fichier n'est pas cru sur parole : la grille est reconstruite pion par pion,
     * puis on vérifie sa cohérence (pas de pion en l'air, bon nombre de coups, état
     * et joueur cohérents). Si une vérification échoue, la partie en cours est conservée.
     */
    public void restoreGame(GameSave save) {
        Objects.requireNonNull(save, "Save cannot be null");

        Disc[][] savedCells = save.board();
        Board restoredBoard = new Board();

        int actualMoveCount = 0;

        // On reconstruit chaque colonne du bas vers le haut.
        for (int column = 0; column < Board.COLUMNS; column++) {

            // Une fois une case vide trouvée, aucun pion ne doit se trouver plus haut.
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
                    // Pion au-dessus d'une case vide : impossible dans une vraie partie.
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

        // Le nombre de pions sur la grille doit égaler le nombre de coups annoncé.
        if (actualMoveCount != save.moveCount()) {
            throw new IllegalArgumentException(
                    "Saved move count does not match the board"
            );
        }

        // Vérifie la cohérence entre la grille, l'état annoncé et le joueur courant.
        validateSavedState(restoredBoard, save);

        // On ne modifie le moteur qu'une fois toutes les vérifications réussies.
        this.board = restoredBoard;
        this.currentPlayer = save.currentPlayer();
        this.gameState = save.gameState();
        this.moveCount = save.moveCount();
    }

    /**
     * Vérifie que l'état annoncé dans la sauvegarde correspond bien à la grille.
     *
     * @throws IllegalArgumentException si la sauvegarde est incohérente
     */
    private void validateSavedState(Board restoredBoard, GameSave save) {

        // Qui a réellement un alignement de quatre sur la grille restaurée ?
        boolean redWon = WinDetector.hasWon(restoredBoard, Disc.RED);
        boolean yellowWon = WinDetector.hasWon(restoredBoard, Disc.YELLOW);

        if (redWon && yellowWon) {
            throw new IllegalArgumentException(
                    "Invalid save: both players cannot win"
            );
        }

        switch (save.gameState()) {

            // En cours : personne ne doit avoir gagné, la grille ne doit pas être pleine,
            // et le joueur courant doit être celui prévu par la parité du nombre de coups
            // (rouge si nombre pair, jaune si impair).
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

            // Victoire annoncée : elle doit exister sur la grille, et être la seule.
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

            // Match nul annoncé : grille pleine et aucun gagnant.
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
     * Retourne une copie défensive de la grille : l'appelant ne peut pas modifier
     * la grille interne du moteur.
     */
    public Board getBoard() {
        return board.copy();
    }

    /**
     * Joueur dont c'est le tour.
     */
    public Disc getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * État actuel de la partie.
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * Nombre de coups déjà joués.
     */
    public int getMoveCount() {
        return moveCount;
    }

    /**
     * Vrai si la partie est terminée.
     */
    public boolean isFinished() {
        return gameState.isFinished();
    }

    /**
     * Vrai si la partie s'est terminée par un match nul.
     */
    public boolean isDraw() {
        return gameState == GameState.DRAW;
    }

    /**
     * Vrai si la couleur donnée a gagné la partie.
     */
    public boolean isWinning(Disc disc) {
        return switch (disc) {
            case RED -> gameState == GameState.RED_WON;
            case YELLOW -> gameState == GameState.YELLOW_WON;
            case EMPTY -> false;
        };
    }
}
