
package ma.connect4.persistence;

import ma.connect4.model.Disc;
import ma.connect4.model.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JsonGamePersistenceTest {

    @TempDir
    Path tempDirectory;

    private JsonGamePersistence persistence;

    @BeforeEach
    void setUp() {
        persistence = new JsonGamePersistence();
    }

    @Test
    void shouldSaveAndLoadGameSuccessfully() throws IOException {
        Disc[][] board = createEmptyBoard();

        board[5][0] = Disc.RED;
        board[5][1] = Disc.YELLOW;

        GameSave original = new GameSave(
                board,
                Disc.RED,
                GameState.IN_PROGRESS,
                2,
                LocalDateTime.now().withNano(0)
        );

        Path file = tempDirectory.resolve("test-save.json");

        persistence.save(original, file);

        assertTrue(file.toFile().exists());

        Optional<GameSave> loaded = persistence.load(file);

        assertTrue(loaded.isPresent());

        GameSave restored = loaded.get();

        assertEquals(Disc.RED, restored.currentPlayer());
        assertEquals(GameState.IN_PROGRESS, restored.gameState());
        assertEquals(2, restored.moveCount());
        assertEquals(Disc.RED, restored.board()[5][0]);
        assertEquals(Disc.YELLOW, restored.board()[5][1]);
    }

    @Test
    void shouldReturnEmptyWhenSaveFileDoesNotExist() throws IOException {
        Path file = tempDirectory.resolve("missing.json");

        Optional<GameSave> loaded = persistence.load(file);

        assertTrue(loaded.isEmpty());
    }

    @Test
    void shouldCreateParentDirectoriesWhenSaving() throws IOException {
        Disc[][] board = createEmptyBoard();

        GameSave save = new GameSave(
                board,
                Disc.RED,
                GameState.IN_PROGRESS,
                0,
                LocalDateTime.now().withNano(0)
        );

        Path file = tempDirectory
                .resolve("saves")
                .resolve("games")
                .resolve("game.json");

        persistence.save(save, file);

        assertTrue(file.toFile().exists());
        assertTrue(persistence.load(file).isPresent());
    }

    private Disc[][] createEmptyBoard() {
        Disc[][] board = new Disc[6][7];

        for (int row = 0; row < 6; row++) {
            for (int column = 0; column < 7; column++) {
                board[row][column] = Disc.EMPTY;
            }
        }

        return board;
    }
}
