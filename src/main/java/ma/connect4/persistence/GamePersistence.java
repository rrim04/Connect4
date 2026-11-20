
package ma.connect4.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public interface GamePersistence {

    void save(GameSave gameSave, Path file) throws IOException;

    Optional<GameSave> load(Path file) throws IOException;
}
