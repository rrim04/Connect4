
package ma.connect4;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import ma.connect4.view.GameView;

/**
 * Point d'entrée de l'application JavaFX « Connect 4 - AI Arena ».
 *
 * Cette classe se contente de créer la fenêtre et d'y placer la vue
 * principale ({@code GameView}). Toute la logique du jeu se trouve dans
 * les autres packages (model, engine, ai, controller...).
 */
public class Main extends Application {

    /**
     * Appelée par JavaFX au démarrage : construit et affiche la fenêtre.
     *
     * @param stage la fenêtre principale fournie par JavaFX
     */
    @Override
    public void start(Stage stage) {

        // Toute l'interface (plateau, réglages, boutons) est construite dans GameView.
        GameView gameView = new GameView();

        // Scène de 1150 x 800 pixels dont le contenu est la vue du jeu.
        Scene scene = new Scene(gameView, 1150, 800);

        stage.setTitle("Connect 4 - AI Arena");
        stage.setScene(scene);
        // Tailles minimales : le plateau et le panneau de réglages doivent rester visibles.
        stage.setMinWidth(1050);
        stage.setMinHeight(750);
        stage.setResizable(true);

        stage.show();
    }

    /**
     * Méthode principale : lance l'application JavaFX.
     *
     * @param args arguments de la ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
