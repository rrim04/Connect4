
package ma.connect4.engine;

import ma.connect4.model.Disc;
import ma.connect4.model.GameState;
import ma.connect4.model.Move;
import ma.connect4.persistence.JsonGamePersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class GameEnginePersistenceTest {

    @TempDir
    Path tempDirectory;

    private GameEngine engine;
    private JsonGamePersistence persistence;

    @BeforeEach
    void setUp() {
        engine = new GameEngine();
        persistence = new JsonGamePersistence();
    }

    @Test
    void shouldSaveAndRestoreGame() throws IOException {
        engine.playMove(new Move(0));
        engine.playMove(new Move(1));
        engine.playMove(new Move(2));

        Path file = tempDirectory.resolve("game.json");

        engine.saveGame(persistence, file);

        GameEngine restoredEngine = new GameEngine();

        assertTrue(restoredEngine.loadGame(persistence, file));

        assertEquals(3, restoredEngine.getMoveCount());
        assertEquals(Disc.YELLOW, restoredEngine.getCurrentPlayer());
        assertEquals(GameState.IN_PROGRESS,
                restoredEngine.getGameState());

        assertEquals(
                engine.getBoard().getCell(5, 0),
                restoredEngine.getBoard().getCell(5, 0)
        );
    }

    @Test
    void shouldReturnFalseWhenSaveDoesNotExist() throws IOException {
        Path file = tempDirectory.resolve("missing.json");

        assertFalse(engine.loadGame(persistence, file));
    }

    @Test
    void shouldContinuePlayingAfterRestoration() throws IOException {
        engine.playMove(new Move(0));
        engine.playMove(new Move(1));

        Path file = tempDirectory.resolve("game.json");
        engine.saveGame(persistence, file);

        GameEngine restoredEngine = new GameEngine();
        restoredEngine.loadGame(persistence, file);

        restoredEngine.playMove(new Move(2));

        assertEquals(3, restoredEngine.getMoveCount());
        assertEquals(Disc.YELLOW, restoredEngine.getCurrentPlayer());
    }
}
