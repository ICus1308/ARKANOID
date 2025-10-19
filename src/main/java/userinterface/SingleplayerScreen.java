package userinterface;

import gamemanager.CoinManager;
import gamemanager.UIManager;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import static gameconfig.GameConfig.*;

public class SingleplayerScreen extends UIManager {
    private int lives = 3;
    private int score = 0;
    private Text scoreText;
    private Text livesText;
    private Text coinsText;
    private CoinManager coinManager;

    public SingleplayerScreen(Pane root, CoinManager coinManager) {
        super(root);
        this.coinManager = coinManager;
        initializeUI();
    }

    @Override
    protected void initializeUI() {
        scoreText = createStyledText("Score: 0", 10, 25, UI_FONT, TEXT_COLOR);
        livesText = createStyledText("Lives: 3", GAME_WIDTH - 100, 25, UI_FONT, TEXT_COLOR);
        coinsText = createStyledText("Coins: " + (coinManager != null ? coinManager.getCoins() : 0),
                GAME_WIDTH / 2 - 50, 25, UI_FONT, GOLD_COLOR);
        gameMessage = createStyledText("PRESS SPACE TO START", GAME_WIDTH / 2 - 200, GAME_HEIGHT / 2, MESSAGE_FONT, GOLD_COLOR);

        root.getChildren().addAll(scoreText, livesText, coinsText, gameMessage);
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
        livesText.setText("Lives: " + lives);
    }

    public void decreaseLives() {
        updateLives(this.lives - 1);
    }

    public void updateCoins() {
        if (coinManager != null) {
            coinsText.setText("Coins: " + coinManager.getCoins());
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
            root.getChildren().removeAll(scoreText, livesText, coinsText, gameMessage);
        }
    }
}
