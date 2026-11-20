
package ma.connect4.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import ma.connect4.controller.GameController;
import ma.connect4.model.Board;
import ma.connect4.model.Disc;
import ma.connect4.model.GameConfig;
import ma.connect4.model.GameState;
import ma.connect4.model.Move;
import ma.connect4.persistence.JsonGamePersistence;

import java.io.IOException;
import java.nio.file.Path;

public final class GameView extends BorderPane {

    private static final int ROWS = 6;
    private static final int COLUMNS = 7;

    private static final String BACKGROUND = "#0B1020";
    private static final String PANEL = "#151D33";
    private static final String CYAN = "#00E5FF";
    private static final String PURPLE = "#A855F7";

    private final GameController controller;

    private final GridPane boardGrid;
    private final Label statusLabel;
    private final Label playerLabel;
    private final Label hintsLabel;
    private final Label hintMessage;

    private final ComboBox<GameConfig.GameMode> modeBox;
    private final ComboBox<GameConfig.SearchStrategyType> computerStrategyBox;
    private final ComboBox<GameConfig.SearchStrategyType> helpStrategyBox;
    private final ComboBox<Integer> depthBox;
    private final ComboBox<Integer> hintsBox;

    private final Button[] columnButtons = new Button[COLUMNS];

    private final Path savePath = Path.of("saves", "connect4-save.json");

    public GameView() {

        controller = new GameController();

        boardGrid = new GridPane();

        statusLabel = new Label("Welcome to Connect 4 AI Arena");
        playerLabel = new Label();
        hintsLabel = new Label();
        hintMessage = new Label();

        modeBox = new ComboBox<>();
        computerStrategyBox = new ComboBox<>();
        helpStrategyBox = new ComboBox<>();
        depthBox = new ComboBox<>();
        hintsBox = new ComboBox<>();

        initializeControls();

        setStyle("-fx-background-color: " + BACKGROUND + ";");
        setPadding(new Insets(25));

        setTop(createHeader());
        setCenter(createBoardPanel());
        setRight(createControlPanel());
        setBottom(createStatusPanel());

        startNewGame();
    }

    private void initializeControls() {

        modeBox.getItems().addAll(GameConfig.GameMode.values());
        modeBox.setValue(GameConfig.GameMode.HUMAN_VS_COMPUTER);

        computerStrategyBox.getItems().addAll(
                GameConfig.SearchStrategyType.values()
        );
        computerStrategyBox.setValue(
                GameConfig.SearchStrategyType.ALPHA_BETA
        );

        helpStrategyBox.getItems().addAll(
                GameConfig.SearchStrategyType.values()
        );
        helpStrategyBox.setValue(
                GameConfig.SearchStrategyType.MINIMAX
        );

        depthBox.getItems().addAll(2, 3, 4, 5, 6);
        depthBox.setValue(4);

        hintsBox.getItems().addAll(0, 1, 3, 5);
        hintsBox.setValue(3);

        configureComboBox(modeBox);
        configureComboBox(computerStrategyBox);
        configureComboBox(helpStrategyBox);
        configureComboBox(depthBox);
        configureComboBox(hintsBox);
    }

    private void configureComboBox(ComboBox<?> comboBox) {
        comboBox.setMaxWidth(Double.MAX_VALUE);
        comboBox.setStyle(
                "-fx-background-color: #222D49;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;"
        );
    }

    private VBox createHeader() {

        Label title = new Label("CONNECT 4");
        title.setStyle(
                "-fx-font-size: 32px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + CYAN + ";"
        );

        Label subtitle = new Label("AI ARENA  |  STRATEGY GAME");
        subtitle.setStyle(
                "-fx-font-size: 11px;" +
                        "-fx-text-fill: #929DB8;" +
                        "-fx-letter-spacing: 3px;"
        );

        VBox header = new VBox(5, title, subtitle);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 25, 0));

        return header;
    }

    private VBox createBoardPanel() {

        Label boardTitle = new Label("GAME BOARD");
        boardTitle.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 17px;" +
                        "-fx-font-weight: bold;"
        );

        HBox dropButtons = new HBox(8);
        dropButtons.setAlignment(Pos.CENTER);

        for (int column = 0; column < COLUMNS; column++) {

            final int selectedColumn = column;

            Button button = new Button("↓");
            button.setPrefSize(58, 38);
            button.setStyle(
                    "-fx-background-color: #202C48;" +
                            "-fx-text-fill: " + CYAN + ";" +
                            "-fx-font-size: 20px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 8px;" +
                            "-fx-cursor: hand;"
            );

            button.setOnAction(event -> playColumn(selectedColumn));

            columnButtons[column] = button;
            dropButtons.getChildren().add(button);
        }

        boardGrid.setAlignment(Pos.CENTER);
        boardGrid.setHgap(8);
        boardGrid.setVgap(8);
        boardGrid.setPadding(new Insets(15));

        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {

                Circle disc = new Circle(25);
                disc.setFill(Color.web("#26334F"));
                disc.setStroke(Color.web("#344563"));
                disc.setStrokeWidth(1.5);

                StackPane cell = new StackPane(disc);
                cell.setPrefSize(58, 58);

                boardGrid.add(cell, column, row);
            }
        }

        VBox boardContainer = new VBox(
                15,
                boardTitle,
                dropButtons,
                boardGrid
        );

        boardContainer.setAlignment(Pos.CENTER);
        boardContainer.setPadding(new Insets(20));
        boardContainer.setStyle(
                "-fx-background-color: " + PANEL + ";" +
                        "-fx-background-radius: 18px;" +
                        "-fx-border-color: #263653;" +
                        "-fx-border-radius: 18px;"
        );

        VBox.setVgrow(boardGrid, Priority.ALWAYS);

        return boardContainer;
    }

    private VBox createControlPanel() {

        Label settingsTitle = new Label("GAME SETTINGS");
        settingsTitle.setStyle(
                "-fx-text-fill: " + PURPLE + ";" +
                        "-fx-font-size: 17px;" +
                        "-fx-font-weight: bold;"
        );

        Button newGameButton = createButton("NEW GAME", PURPLE);
        newGameButton.setOnAction(event -> startNewGame());

        Button hintButton = createButton("GET A HINT", CYAN);
        hintButton.setOnAction(event -> showHint());

        Button restartButton = createButton("RESTART", "#344563");
        restartButton.setOnAction(event -> restartGame());

        Button saveButton = createButton("SAVE GAME", "#344563");
        saveButton.setOnAction(event -> saveGame());

        Button loadButton = createButton("LOAD GAME", "#344563");
        loadButton.setOnAction(event -> loadGame());

        Label modeTitle = createFieldLabel("Game mode");
        Label computerTitle = createFieldLabel("Computer strategy");
        Label helpTitle = createFieldLabel("Hint strategy");
        Label depthTitle = createFieldLabel("AI search depth");
        Label hintsTitle = createFieldLabel("Number of hints");

        playerLabel.setStyle(
                "-fx-text-fill: " + CYAN + ";" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;"
        );

        hintsLabel.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;"
        );

        hintMessage.setWrapText(true);
        hintMessage.setStyle(
                "-fx-text-fill: " + CYAN + ";" +
                        "-fx-font-size: 12px;"
        );

        VBox controls = new VBox(
                12,
                settingsTitle,
                modeTitle,
                modeBox,
                computerTitle,
                computerStrategyBox,
                helpTitle,
                helpStrategyBox,
                depthTitle,
                depthBox,
                hintsTitle,
                hintsBox,
                newGameButton,
                hintButton,
                restartButton,
                saveButton,
                loadButton,
                playerLabel,
                hintsLabel,
                hintMessage
        );

        controls.setPadding(new Insets(20));
        controls.setPrefWidth(260);
        controls.setStyle(
                "-fx-background-color: " + PANEL + ";" +
                        "-fx-background-radius: 18px;" +
                        "-fx-border-color: #263653;" +
                        "-fx-border-radius: 18px;"
        );

        return controls;
    }

    private VBox createStatusPanel() {

        statusLabel.setWrapText(true);
        statusLabel.setAlignment(Pos.CENTER);
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        statusLabel.setMinHeight(40);
        statusLabel.setStyle(
                "-fx-text-fill: " + CYAN + ";" +
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;"
        );

        VBox panel = new VBox(statusLabel);
        panel.setAlignment(Pos.CENTER);
        panel.setMinHeight(65);
        panel.setPadding(new Insets(10, 15, 0, 15));
        panel.setStyle(
                "-fx-background-color: " + PANEL + ";" +
                        "-fx-background-radius: 10px;"
        );

        return panel;
    }

    private Label createFieldLabel(String text) {
        Label label = new Label(text);
        label.setStyle(
                "-fx-text-fill: #AAB6D3;" +
                        "-fx-font-size: 12px;"
        );
        return label;
    }

    private Button createButton(String text, String color) {

        Button button = new Button(text);

        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(38);

        button.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-cursor: hand;"
        );

        return button;
    }

    private void startNewGame() {

        try {
            GameConfig config = new GameConfig(
                    modeBox.getValue(),
                    computerStrategyBox.getValue(),
                    helpStrategyBox.getValue(),
                    depthBox.getValue(),
                    hintsBox.getValue()
            );

            controller.startNewGame(config);

            hintMessage.setText("");
            statusLabel.setText("New game started!");

            refreshBoard();

        } catch (RuntimeException exception) {
            statusLabel.setText(
                    "Unable to start game: " + exception.getMessage()
            );
        }
    }

    private void playColumn(int column) {

        try {
            if (controller.isGameFinished()) {
                statusLabel.setText("The game is already finished.");
                return;
            }

            controller.playHumanMove(new Move(column));

            if (!controller.isGameFinished()
                    && controller.getGameConfig().mode()
                    == GameConfig.GameMode.HUMAN_VS_COMPUTER) {

                controller.playComputerMove();
            }

            hintMessage.setText("");
            refreshBoard();

            GameState state = controller.getGameState();

            if (state == GameState.RED_WON) {
                statusLabel.setText("RED WINS!");
                showGameResult("RED WINS!", "Congratulations! Red wins the game.");
            } else if (state == GameState.YELLOW_WON) {
                statusLabel.setText("YELLOW WINS!");
                showGameResult("YELLOW WINS!", "Congratulations! Yellow wins the game.");
            } else if (state == GameState.DRAW) {
                statusLabel.setText("IT'S A DRAW!");
                showGameResult("IT'S A DRAW!", "The board is full. Nobody wins this game.");
            } else {
                statusLabel.setText("Move played successfully.");
            }

        } catch (RuntimeException exception) {
            statusLabel.setText(exception.getMessage());
        }
    }

    private void showGameResult(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle("Connect 4 - Game Over");
        alert.setHeaderText(title);
        alert.setContentText(message);

        alert.getDialogPane().setStyle(
                "-fx-background-color: " + PANEL + ";"
        );

        alert.showAndWait();
    }

    private void showHint() {

        try {
            Move hint = controller.getHint();

            hintMessage.setText(
                    "Suggested column: " + (hint.column() + 1)
            );

            statusLabel.setText("Hint generated successfully.");
            refreshBoard();

        } catch (RuntimeException exception) {
            hintMessage.setText(exception.getMessage());
            statusLabel.setText("Unable to generate hint.");
        }
    }

    private void restartGame() {

        try {
            controller.restartGame();

            hintMessage.setText("");
            statusLabel.setText("Game restarted.");

            refreshBoard();

        } catch (RuntimeException exception) {
            statusLabel.setText(exception.getMessage());
        }
    }

    private void saveGame() {

        try {
            controller.getGameEngine().saveGame(
                    new JsonGamePersistence(),
                    savePath
            );

            statusLabel.setText(
                    "Game saved successfully: " + savePath
            );

        } catch (IOException | RuntimeException exception) {
            statusLabel.setText(
                    "Save failed: " + exception.getMessage()
            );
        }
    }

    private void loadGame() {

        try {
            boolean loaded = controller.getGameEngine().loadGame(
                    new JsonGamePersistence(),
                    savePath
            );

            if (loaded) {
                hintMessage.setText("");
                statusLabel.setText("Game loaded successfully.");
                refreshBoard();

                GameState state = controller.getGameState();

                if (state == GameState.RED_WON) {
                    statusLabel.setText("RED WINS!");
                } else if (state == GameState.YELLOW_WON) {
                    statusLabel.setText("YELLOW WINS!");
                } else if (state == GameState.DRAW) {
                    statusLabel.setText("IT'S A DRAW!");
                }

            } else {
                statusLabel.setText("No saved game was found.");
            }

        } catch (IOException | RuntimeException exception) {
            statusLabel.setText(
                    "Load failed: " + exception.getMessage()
            );
        }
    }

    private void refreshBoard() {

        Board board = controller.getGameEngine().getBoard();

        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {

                StackPane cell = (StackPane) getCell(row, column);
                Circle disc = (Circle) cell.getChildren().get(0);

                Disc value = board.getCell(row, column);

                switch (value) {
                    case RED -> disc.setFill(Color.web("#FF3864"));
                    case YELLOW -> disc.setFill(Color.web("#FFD43B"));
                    case EMPTY -> disc.setFill(Color.web("#26334F"));
                }
            }
        }

        Disc current = controller.getCurrentPlayer();

        playerLabel.setText("Current player: " + current);

        hintsLabel.setText(
                "Hints remaining: " + controller.getHintsRemaining()
        );

        boolean finished = controller.isGameFinished();

        for (Button button : columnButtons) {
            button.setDisable(finished);
        }
    }

    private javafx.scene.Node getCell(int row, int column) {

        for (javafx.scene.Node node : boardGrid.getChildren()) {

            Integer nodeRow = GridPane.getRowIndex(node);
            Integer nodeColumn = GridPane.getColumnIndex(node);

            int actualRow = nodeRow == null ? 0 : nodeRow;
            int actualColumn = nodeColumn == null ? 0 : nodeColumn;

            if (actualRow == row && actualColumn == column) {
                return node;
            }
        }

        throw new IllegalStateException("Board cell not found");
    }
}
