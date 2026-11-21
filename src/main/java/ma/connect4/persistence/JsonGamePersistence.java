
package ma.connect4.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Sauvegarde et chargement d'une partie au format JSON, grâce à la bibliothèque Jackson.
 *
 * Le fichier produit est lisible (indenté) et peut être ouvert avec un éditeur de texte.
 */
public final class JsonGamePersistence implements GamePersistence {

    private final ObjectMapper mapper;

    /**
     * Configure Jackson : gestion des dates et sortie indentée.
     */
    public JsonGamePersistence() {
        this.mapper = new ObjectMapper();
        // Nécessaire pour écrire/lire la date (LocalDateTime) de la sauvegarde.
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
            // Crée le dossier de sauvegarde (par exemple « saves ») s'il n'existe pas.
            Files.createDirectories(parent);
        }

        // Écriture de l'objet de sauvegarde dans le fichier JSON.
        mapper.writeValue(absolutePath.toFile(), gameSave);
    }

    @Override
    public Optional<GameSave> load(Path file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }

        // Fichier absent : ce n'est pas une erreur, on renvoie simplement « rien ».
        if (!Files.exists(file) || !Files.isRegularFile(file)) {
            return Optional.empty();
        }

        // Lecture du JSON et reconstruction de l'objet (le constructeur re-vérifie les données).
        GameSave save = mapper.readValue(file.toFile(), GameSave.class);
        return Optional.of(save);
    }
}
