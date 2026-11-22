
package ma.connect4.controller;

import ma.connect4.model.Disc;
import ma.connect4.model.GameConfig;
import ma.connect4.model.GameState;
import ma.connect4.model.Move;
import ma.connect4.persistence.GamePersistence;
import ma.connect4.persistence.GameSave;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GameControllerTest {

    private GameController controller;

    @BeforeEach
    void setUp() {
        controller = new GameController();
    }

    private GameConfig createConfig() {
        return new GameConfig(
                GameConfig.GameMode.HUMAN_VS_COMPUTER,
                GameConfig.SearchStrategyType.ALPHA_BETA,
                GameConfig.SearchStrategyType.MINIMAX,
                4,
                3
        );
    }

    @Test
    void shouldStartNewGame() {
        controller.startNewGame(createConfig());

        assertEquals(GameState.IN_PROGRESS, controller.getGameState());
        assertEquals(Disc.RED, controller.getCurrentPlayer());
        assertEquals(3, controller.getHintsRemaining());
    }

    @Test
    void shouldAllowHumanToPlay() {
        controller.startNewGame(createConfig());

        controller.playHumanMove(new Move(3));

        assertEquals(1, controller.getGameEngine().getMoveCount());
        assertEquals(Disc.YELLOW, controller.getCurrentPlayer());
    }

    @Test
    void shouldRejectHumanMoveDuringComputerTurn() {
        controller.startNewGame(createConfig());

        controller.playHumanMove(new Move(3));

        assertThrows(
                IllegalStateException.class,
                () -> controller.playHumanMove(new Move(2))
        );
    }

    @Test
    void shouldAllowComputerToPlay() {
        controller.startNewGame(createConfig());

        controller.playHumanMove(new Move(3));

        assertNotNull(controller.playComputerMove());
        assertEquals(2, controller.getGameEngine().getMoveCount());
        assertEquals(Disc.RED, controller.getCurrentPlayer());
    }

    @Test
    void shouldDecreaseHintsAfterUsingHint() {
        controller.startNewGame(createConfig());

        Move hint = controller.getHint();

        assertNotNull(hint);
        assertEquals(2, controller.getHintsRemaining());
        assertEquals(0, controller.getGameEngine().getMoveCount());
    }

    @Test
    void shouldRestoreHintsAfterRestart() {
        controller.startNewGame(createConfig());

        controller.getHint();
        controller.restartGame();

        assertEquals(3, controller.getHintsRemaining());
        assertEquals(0, controller.getGameEngine().getMoveCount());
    }

    @Test
    void shouldRejectHintDuringComputerTurn() {
        controller.startNewGame(createConfig());
        controller.playHumanMove(new Move(3)); // c'est maintenant au jaune (la machine)

        assertThrows(
                IllegalStateException.class,
                () -> controller.getHint()
        );

        // Une aide refusée ne doit pas consommer une des aides autorisées.
        assertEquals(3, controller.getHintsRemaining());
    }

    @Test
    void shouldTellWhenItIsComputerTurn() {
        controller.startNewGame(createConfig());
        assertFalse(controller.isComputerTurn());      // le rouge (humain) commence

        controller.playHumanMove(new Move(3));
        assertTrue(controller.isComputerTurn());       // puis c'est au jaune (machine)

        controller.playComputerMove();
        assertFalse(controller.isComputerTurn());      // retour à l'humain

        // Jamais de tour « machine » dans une partie à deux joueurs.
        controller.startNewGame(new GameConfig(
                GameConfig.GameMode.HUMAN_VS_HUMAN,
                GameConfig.SearchStrategyType.ALPHA_BETA,
                GameConfig.SearchStrategyType.MINIMAX,
                4,
                3
        ));
        controller.playHumanMove(new Move(3));
        assertFalse(controller.isComputerTurn());
    }

    @Test
    void shouldLetComputerPlayAfterLoadingGameAtItsTurn() {
        // Partie sauvegardée en plein tour de la machine :
        // le rouge a joué en colonne 3, c'est au jaune de jouer.
        controller.startNewGame(createConfig());
        controller.playHumanMove(new Move(3));

        GameSave save = new GameSave(
                controller.getGameEngine().getBoard().toArray(),
                controller.getGameEngine().getCurrentPlayer(),
                controller.getGameState(),
                controller.getGameEngine().getMoveCount(),
                LocalDateTime.now()
        );

        // Nouvelle partie, puis chargement de la sauvegarde.
        controller.startNewGame(createConfig());
        controller.getGameEngine().restoreGame(save);

        // La vue doit détecter ce cas et faire jouer la machine
        // (sinon l'humain est refusé et la machine ne joue pas : partie bloquée).
        assertTrue(controller.isComputerTurn());
        assertThrows(
                IllegalStateException.class,
                () -> controller.playHumanMove(new Move(2))
        );

        controller.playComputerMove();

        assertEquals(2, controller.getGameEngine().getMoveCount());
        assertEquals(Disc.RED, controller.getCurrentPlayer());
        assertFalse(controller.isComputerTurn());
    }

    // ------------------------------------------------------------------
    // Sauvegarde complète (grille + réglages + aides restantes)
    // ------------------------------------------------------------------

    /** Sauvegarde « en mémoire » : évite d'écrire un vrai fichier dans les tests. */
    private static final class InMemoryPersistence implements GamePersistence {

        private GameSave stored;

        @Override
        public void save(GameSave gameSave, Path file) {
            stored = gameSave;
        }

        @Override
        public Optional<GameSave> load(Path file) {
            return Optional.ofNullable(stored);
        }
    }

    private GameConfig otherConfig() {
        return new GameConfig(
                GameConfig.GameMode.HUMAN_VS_HUMAN,
                GameConfig.SearchStrategyType.MINIMAX,
                GameConfig.SearchStrategyType.ALPHA_BETA,
                2,
                5
        );
    }

    @Test
    void shouldSaveAndRestoreConfigurationAndRemainingHints() throws IOException {
        InMemoryPersistence persistence = new InMemoryPersistence();
        Path file = Path.of("memory.json");

        // Partie A : on joue un coup, la machine répond, puis on utilise une aide.
        controller.startNewGame(createConfig());
        controller.playHumanMove(new Move(3));
        controller.playComputerMove();
        controller.getHint();
        assertEquals(2, controller.getHintsRemaining());

        controller.saveGame(persistence, file);

        // Autre contrôleur, avec des réglages complètement différents.
        GameController other = new GameController();
        other.startNewGame(otherConfig());

        assertTrue(other.loadGame(persistence, file));

        // Les réglages et les aides viennent maintenant de la sauvegarde.
        assertEquals(createConfig(), other.getGameConfig());
        assertEquals(2, other.getHintsRemaining());
        assertEquals(2, other.getGameEngine().getMoveCount());
        assertEquals(Disc.RED, other.getCurrentPlayer());
    }

    @Test
    void shouldKeepCurrentConfigurationWhenLoadingOldSaveWithoutSettings() throws IOException {
        InMemoryPersistence persistence = new InMemoryPersistence();

        // Ancien format : aucune configuration dans la sauvegarde.
        Disc[][] emptyBoard = new Disc[6][7];
        for (Disc[] row : emptyBoard) {
            java.util.Arrays.fill(row, Disc.EMPTY);
        }
        persistence.save(new GameSave(
                emptyBoard,
                Disc.RED,
                GameState.IN_PROGRESS,
                0,
                LocalDateTime.now()
        ), Path.of("memory.json"));

        controller.startNewGame(otherConfig());

        assertTrue(controller.loadGame(persistence, Path.of("memory.json")));

        // On garde les réglages et les aides actuels.
        assertEquals(otherConfig(), controller.getGameConfig());
        assertEquals(5, controller.getHintsRemaining());
    }

    @Test
    void shouldRefuseOldSaveWithoutSettingsWhenNoGameIsStarted() {
        InMemoryPersistence persistence = new InMemoryPersistence();

        Disc[][] emptyBoard = new Disc[6][7];
        for (Disc[] row : emptyBoard) {
            java.util.Arrays.fill(row, Disc.EMPTY);
        }
        persistence.save(new GameSave(
                emptyBoard,
                Disc.RED,
                GameState.IN_PROGRESS,
                0,
                LocalDateTime.now()
        ), Path.of("memory.json"));

        // Aucune partie lancée : pas de réglages sur lesquels se rabattre.
        assertThrows(
                IllegalStateException.class,
                () -> controller.loadGame(persistence, Path.of("memory.json"))
        );
    }

    @Test
    void shouldReturnFalseWhenThereIsNothingToLoad() throws IOException {
        controller.startNewGame(createConfig());

        assertFalse(controller.loadGame(new InMemoryPersistence(), Path.of("memory.json")));
        assertEquals(3, controller.getHintsRemaining());
    }

    @Test
    void shouldRejectSaveWhenNoGameIsStarted() {
        assertThrows(
                IllegalStateException.class,
                () -> controller.saveGame(new InMemoryPersistence(), Path.of("memory.json"))
        );
    }

    @Test
    void shouldRejectSavedHintsAboveConfiguredMaximum() {
        Disc[][] emptyBoard = new Disc[6][7];
        for (Disc[] row : emptyBoard) {
            java.util.Arrays.fill(row, Disc.EMPTY);
        }

        // Le maximum est de 3 aides : en annoncer 4 est incohérent.
        assertThrows(
                IllegalArgumentException.class,
                () -> new GameSave(
                        emptyBoard,
                        Disc.RED,
                        GameState.IN_PROGRESS,
                        0,
                        LocalDateTime.now(),
                        createConfig(),
                        4
                )
        );
    }

    @Test
    void shouldNotChangeSettingsWhenSavedGameIsInconsistent() {
        InMemoryPersistence persistence = new InMemoryPersistence();

        // Grille impossible : un pion « en l'air » en haut de la colonne 0.
        Disc[][] floatingDisc = new Disc[6][7];
        for (Disc[] row : floatingDisc) {
            java.util.Arrays.fill(row, Disc.EMPTY);
        }
        floatingDisc[0][0] = Disc.RED;

        persistence.save(new GameSave(
                floatingDisc,
                Disc.YELLOW,
                GameState.IN_PROGRESS,
                1,
                LocalDateTime.now(),
                otherConfig(),
                5
        ), Path.of("memory.json"));

        controller.startNewGame(createConfig());

        assertThrows(
                IllegalArgumentException.class,
                () -> controller.loadGame(persistence, Path.of("memory.json"))
        );

        // Rien n'a changé : ni la grille, ni les réglages, ni les aides.
        assertEquals(createConfig(), controller.getGameConfig());
        assertEquals(3, controller.getHintsRemaining());
        assertEquals(0, controller.getGameEngine().getMoveCount());
    }
}
