package userinterface;

import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import static gameconfig.GameConfig.*;

public class PlayScreen extends UIManager {
    private int lives = 3;
    private  Text scoreText;
    private  Text livesText;

    public PlayScreen(Pane root) {
        super(root);
    }
    
    @Override
    protected void initializeUI() {
        scoreText = createStyledText("Score: 0", 10, 25, UI_FONT, TEXT_COLOR);
        livesText = createStyledText("Lives: 3", GAME_WIDTH - 100, 25, UI_FONT, TEXT_COLOR);
        gameMessage = createStyledText("PRESS SPACE TO START", GAME_WIDTH / 2 - 200, GAME_HEIGHT / 2, MESSAGE_FONT, GOLD_COLOR);

        root.getChildren().addAll(scoreText, livesText, gameMessage);
    }

    public void updateScore(int newScore) {
        this.score = newScore;
        scoreText.setText("Score: " + score);
    }

    public void increaseScore(int delta) { updateScore(this.score + delta); }

    public int getLives() { return lives; }

    public void updateLives(int newLives) {
        this.lives = newLives;
        livesText.setText("Lives: " + lives);
    }

    public void decreaseLives() { updateLives(this.lives - 1); }

    public void showLevel(int level) {
        gameMessage.setText("LEVEL " + level);
        gameMessage.setVisible(true);
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
        root.getChildren().removeAll(scoreText, livesText, gameMessage);
    }
}
