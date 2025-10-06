package userinterface;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import static gameconfig.GameConfig.*;

public class UserInterface {
    private int score = 0;
    private int lives = 3;
    private final Text scoreText;
    private final Text livesText;
    private final Text gameMessage;

    public UserInterface(Pane root) {
        scoreText = new Text(10, 25, "Score: 0");
        scoreText.setFont(Font.font("Inter", 20));
        scoreText.setFill(Color.WHITE);

        livesText = new Text(GAME_WIDTH - 100, 25, "Lives: 3");
        livesText.setFont(Font.font("Inter", 20));
        livesText.setFill(Color.WHITE);

        gameMessage = new Text(GAME_WIDTH / 2 - 200, GAME_HEIGHT / 2, "PRESS SPACE TO START");
        gameMessage.setFont(Font.font("Inter", 40));
        gameMessage.setFill(Color.GOLD);

        root.getChildren().addAll(scoreText, livesText, gameMessage);
    }

    public int getLives() { return lives; }

    public void updateScore(int newScore) {
        this.score = newScore;
        scoreText.setText("Score: " + score);
    }

    public void increaseScore(int delta) { updateScore(this.score + delta); }

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
                gameMessage.setText("PRESS SPACE TO LAUNCH");
                gameMessage.setFill(Color.GOLD);
                gameMessage.setVisible(true);
                break;
            case GAME_OVER:
                gameMessage.setText("GAME OVER | Final Score: " + score);
                gameMessage.setFill(Color.RED);
                gameMessage.setVisible(true);
                break;
            case LEVEL_CLEARED:
                gameMessage.setText("LEVEL CLEARED! | Press SPACE for Next Level");
                gameMessage.setFill(Color.LIMEGREEN);
                gameMessage.setVisible(true);
                break;
            case PLAYING:
                gameMessage.setVisible(false);
                break;
        }
    }
}

