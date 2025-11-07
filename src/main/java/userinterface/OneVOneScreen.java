package userinterface;

import gamemanager.CoinManager;
import gamemanager.UIManager;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import static gameconfig.GameConfig.*;

public class OneVOneScreen extends UIManager implements GameScreen {
    private static final int INITIAL_LIVES = 3;

    private int player1Lives;
    private int player2Lives;
    private Text player1LivesText;
    private Text player2LivesText;

    public OneVOneScreen(Pane root, CoinManager coinManager) {
        super(root);
        this.player1Lives = INITIAL_LIVES;
        this.player2Lives = INITIAL_LIVES;
        initializeUI();
    }

    @Override
    protected void initializeUI() {
        player1LivesText = createStyledText("P1 Lives: " + INITIAL_LIVES, 10, GAME_HEIGHT - 25, UI_FONT, TEXT_COLOR);
        player2LivesText = createStyledText("P2 Lives: " + INITIAL_LIVES, GAME_WIDTH - 150, 25, UI_FONT, TEXT_COLOR);
        gameMessage = createStyledText("PRESS SPACE TO START", GAME_WIDTH / 2 - 200, GAME_HEIGHT / 2, MESSAGE_FONT, GOLD_COLOR);
        root.getChildren().addAll(player1LivesText, player2LivesText, gameMessage);
    }

    public int getPlayer1Lives() {
        return player1Lives;
    }

    public void updatePlayer1Lives(int newLives) {
        this.player1Lives = newLives;
        player1LivesText.setText("P1 Lives: " + player1Lives);
    }

    public void decreasePlayer1Lives() {
        updatePlayer1Lives(player1Lives - 1);
    }

    public int getPlayer2Lives() {
        return player2Lives;
    }

    public void updatePlayer2Lives(int newLives) {
        this.player2Lives = newLives;
        player2LivesText.setText("P2 Lives: " + player2Lives);
    }

    public void decreasePlayer2Lives() {
        updatePlayer2Lives(player2Lives - 1);
    }

    @Override
    public void updateScore(int newScore) {}

    @Override
    public void increaseScore(int delta) {}

    @Override
    public int getScore() {
        return 0;
    }

    @Override
    public int getLives() {
        return player1Lives;
    }

    @Override
    public void updateLives(int newLives) {}

    @Override
    public void decreaseLives() {}

    @Override
    public void updateCoins() {}

    @Override
    public void showLevel(int level) {}

    public void showGameMessage(GameState state) {
        switch (state) {
            case START:
                hideGameMessage();
                break;
            case GAME_OVER:
                String winner = player1Lives > player2Lives ? "PLAYER 1" : "PLAYER 2";
                showGameMessage("GAME OVER | " + winner + " WINS!", RED_COLOR);
                break;
        }
    }

    public void cleanup() {
        if (root != null) {
            root.getChildren().removeAll(player1LivesText, player2LivesText, gameMessage);
        }
    }
}


