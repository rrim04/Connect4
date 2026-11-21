
package ma.connect4.controller;

import ma.connect4.model.Disc;
import ma.connect4.model.GameConfig;
import ma.connect4.model.GameState;
import ma.connect4.model.Move;
import ma.connect4.persistence.GameSave;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

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
}
