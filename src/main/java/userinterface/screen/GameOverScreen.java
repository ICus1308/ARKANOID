package userinterface.screen;

import gamemanager.ui.GameButton;
import gamemanager.ui.UIManager;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import static gameconfig.GameConfig.*;

public class GameOverScreen extends UIManager {
    private StackPane layout;
    private final Runnable onRetry;
    private final Runnable onNewGame;
    private final Runnable onMainMenu;

    private Text scoreText;
    private int currentScore = 0;

    public GameOverScreen(Pane root, Runnable onRetry, Runnable onNewGame, Runnable onMainMenu) {
        super(root);
        this.onRetry = onRetry;
        this.onNewGame = onNewGame;
        this.onMainMenu = onMainMenu;
    }

    @Override
    protected void initializeUI() {
        layout = new StackPane();
        layout.setPrefSize(GAME_WIDTH, GAME_HEIGHT);
        layout.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

        Rectangle containerBg = new Rectangle(1000 * UI_SCALE_X, 600);
        containerBg.setFill(Color.WHITE);
        containerBg.setStroke(Color.BLACK);
        containerBg.setStrokeWidth(3);

        VBox contentBox = new VBox(40);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPrefWidth(500 * UI_SCALE_X);
        contentBox.setPrefHeight(400);
        contentBox.setStyle("-fx-padding: 30;");

        Text gameOverText = createStyledText("GAME OVER", 0, 0, TITLE_FONT, Color.BLACK);
        scoreText = createStyledText("SCORE: " + currentScore, 0, 0, MESSAGE_FONT, Color.BLACK);

        HBox buttonBox = createButtonBox();

        contentBox.getChildren().addAll(gameOverText, scoreText, buttonBox);

        StackPane container = new StackPane(containerBg, contentBox);
        layout.getChildren().add(container);
    }

    private HBox createButtonBox() {
        HBox box = new HBox(30);
        box.setAlignment(Pos.CENTER);

        if (onRetry != null) {
            GameButton replayButton = createButton("REPLAY", onRetry);
            box.getChildren().add(replayButton);
        }

        if (onNewGame != null) {
            GameButton newGameButton = createButton("NEW GAME", onNewGame);
            box.getChildren().add(newGameButton);
        }

        if (onMainMenu != null) {
            GameButton menuButton = createButton("MENU", onMainMenu);
            box.getChildren().add(menuButton);
        }

        GameButton exitButton = createButton("EXIT", () -> {
            Stage stage = (Stage) root.getScene().getWindow();
            if (stage != null) {
                stage.close();
            }
        });
        box.getChildren().add(exitButton);

        return box;
    }

    public void setFinalScore(int score) {
        this.currentScore = score;
        if (scoreText != null) {
            scoreText.setText("SCORE: " + score);
        }
    }

    @Override
    public void show() {
        if (layout == null) {
            initializeUI();
        }
        if (!root.getChildren().contains(layout)) {
            root.getChildren().add(layout);
        }
        layout.setVisible(true);
        layout.toFront();
    }

    @Override
    public void hide() {
        if (layout != null) {
            root.getChildren().remove(layout);
        }
    }

    @Override
    public void refresh() {
        if (layout != null) {
            hide();
        }
        layout = null;
        initializeUI();
    }
}
