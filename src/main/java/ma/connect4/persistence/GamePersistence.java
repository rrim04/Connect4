
package ma.connect4.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Contrat de la sauvegarde d'une partie.
 *
 * Le moteur ne dépend que de cette interface : on peut changer le format de
 * fichier (JSON, binaire...) sans toucher au reste du code.
 */
public interface GamePersistence {

    /**
     * Écrit la sauvegarde dans le fichier (le dossier est créé s'il n'existe pas).
     *
     * @throws IOException en cas d'erreur d'écriture
     */
    void save(GameSave gameSave, Path file) throws IOException;

    /**
     * Lit une sauvegarde.
     *
     * @return la sauvegarde, ou un résultat vide si le fichier n'existe pas
     * @throws IOException si le fichier est illisible ou mal formé
     */
    Optional<GameSave> load(Path file) throws IOException;
}
