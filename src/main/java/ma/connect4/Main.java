
package ma.connect4;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import ma.connect4.view.GameView;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        GameView gameView = new GameView();

        Scene scene = new Scene(gameView, 1150, 800);

        stage.setTitle("Connect 4 - AI Arena");
        stage.setScene(scene);
        stage.setMinWidth(1050);
        stage.setMinHeight(750);
        stage.setResizable(true);

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
