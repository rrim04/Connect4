
package ma.connect4;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.Screen;
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

        // Taille de la fenêtre adaptée à l'écran : sur un écran peu haut (portable),
        // une fenêtre de 800 pixels dépasserait le bas de l'écran et cacherait la barre
        // de messages ainsi que le texte de l'aide.
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        double width = Math.min(1150, screen.getWidth());
        double height = Math.min(800, screen.getHeight() - 40);   // marge pour la barre de titre

        // La vue est placée dans un ScrollPane : si la fenêtre est plus petite que
        // son contenu, des barres de défilement apparaissent au lieu de couper l'interface.
        ScrollPane root = new ScrollPane(gameView);
        root.setFitToWidth(true);
        root.setFitToHeight(true);
        root.setStyle("-fx-background: #0B1020; -fx-background-color: #0B1020;");

        Scene scene = new Scene(root, width, height);

        stage.setTitle("Connect 4 - AI Arena");
        stage.setScene(scene);
        // Tailles minimales, limitées à la taille de l'écran.
        stage.setMinWidth(Math.min(1050, screen.getWidth()));
        stage.setMinHeight(Math.min(550, screen.getHeight() - 40));
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
