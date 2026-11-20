
package ma.connect4.controller;

import ma.connect4.model.Disc;
import ma.connect4.model.GameConfig;
import ma.connect4.model.GameState;
import ma.connect4.model.Move;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}
