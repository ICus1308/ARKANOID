package userinterface.gamescreen;

import gamemanager.manager.CoinManager;
import gamemanager.ui.UIManager;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;

import java.util.Objects;

import static gameconfig.GameConfig.*;

public class SingleplayerScreen extends UIManager implements GameScreen {
    private int lives = 3;
    private int score = 0;
    private Text scoreText;
    private Text livesText;
    private Text coinsText;
    private ImageView livesIcon;
    private ImageView coinsIcon;
    private HBox livesBox;
    private HBox coinsBox;
    private final CoinManager coinManager;

    public SingleplayerScreen(Pane root, CoinManager coinManager) {
        super(root);
        this.coinManager = coinManager;
        initializeUI();
    }

    @Override
    protected void initializeUI() {
        scoreText = createStyledText("Score: 0", 10, 25, UI_FONT, TEXT_COLOR);

        // Create lives display with icon
        try {
            Image livesImage = new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/imagelive/imagelive.png")));
            livesIcon = new ImageView(livesImage);
            livesIcon.setFitWidth(25);
            livesIcon.setFitHeight(25);
            livesIcon.setPreserveRatio(true);
        } catch (Exception e) {
            System.err.println("Error loading lives icon");
        }

        livesText = createStyledText("3", 0, 0, UI_FONT, TEXT_COLOR);
        livesBox = new HBox(5);
        livesBox.setAlignment(Pos.CENTER_LEFT);
        livesBox.getChildren().addAll(livesIcon, livesText);
        livesBox.setLayoutX(GAME_WIDTH - 80);
        livesBox.setLayoutY(10);

        // Create coins display with icon
        try {
            Image coinsImage = new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/imagecoin/coin.png")));
            coinsIcon = new ImageView(coinsImage);
            coinsIcon.setFitWidth(25);
            coinsIcon.setFitHeight(25);
            coinsIcon.setPreserveRatio(true);
        } catch (Exception e) {
            System.err.println("Error loading coins icon");
        }

        coinsText = createStyledText((coinManager != null ? coinManager.getCoins() : 0) + "", 0, 0, UI_FONT, GOLD_COLOR);
        coinsBox = new HBox(5);
        coinsBox.setAlignment(Pos.CENTER_LEFT);
        coinsBox.getChildren().addAll(coinsIcon, coinsText);
        coinsBox.setLayoutX(GAME_WIDTH / 2 - 50);
        coinsBox.setLayoutY(10);

        gameMessage = createStyledText("PRESS SPACE TO START", GAME_WIDTH / 2 - 200, GAME_HEIGHT / 2, MESSAGE_FONT, GOLD_COLOR);

        root.getChildren().addAll(scoreText, livesBox, coinsBox, gameMessage);
    }

    public void updateScore(int newScore) {
        this.score = newScore;
        scoreText.setText("Score: " + score);
    }

    public void increaseScore(int delta) {
        updateScore(this.score + delta);
    }

    public int getScore() {
        return score;
    }

    public int getLives() {
        return lives;
    }

    public void updateLives(int newLives) {
        this.lives = newLives;
        livesText.setText(lives + "");
    }

    public void decreaseLives() {
        updateLives(this.lives - 1);
    }

    public void updateCoins() {
        if (coinManager != null) {
            coinsText.setText(coinManager.getCoins() + "");
        }
    }

    public void showLevel(int level) {
        showGameMessage("LEVEL " + level, GOLD_COLOR);
    }

    public void showGameMessage(GameState state) {
        switch (state) {
            case START:
                showGameMessage("PRESS SPACE TO LAUNCH", GOLD_COLOR);
                break;
            case GAME_OVER:
                showGameMessage("GAME OVER | Final Score: " + score, RED_COLOR);
                break;
            case LEVEL_CLEARED:
                showGameMessage("LEVEL CLEARED! | Press SPACE for Next Level", GREEN_COLOR);
                break;
            case PLAYING:
                hideGameMessage();
                break;
        }
    }

    public void cleanup() {
        if (root != null) {
            root.getChildren().removeAll(scoreText, livesBox, coinsBox, gameMessage);
        }
    }
}
