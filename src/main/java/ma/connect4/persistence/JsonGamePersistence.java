
package ma.connect4.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class JsonGamePersistence implements GamePersistence {

    private final ObjectMapper mapper;

    public JsonGamePersistence() {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public void save(GameSave gameSave, Path file) throws IOException {
        if (gameSave == null || file == null) {
            throw new IllegalArgumentException("Save data and file cannot be null");
        }

        Path absolutePath = file.toAbsolutePath();
        Path parent = absolutePath.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        mapper.writeValue(absolutePath.toFile(), gameSave);
    }

    @Override
    public Optional<GameSave> load(Path file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }

        if (!Files.exists(file) || !Files.isRegularFile(file)) {
            return Optional.empty();
        }

        GameSave save = mapper.readValue(file.toFile(), GameSave.class);
        return Optional.of(save);
    }
}
