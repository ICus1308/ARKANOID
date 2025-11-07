package userinterface;

import gamemanager.CoinManager;
import gamemanager.UIManager;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import static gameconfig.GameConfig.*;

public class OneVOneScreen extends UIManager implements GameScreen {
    private int player1Lives = 3;
    private int player2Lives = 3;

    private Text player1LivesText;
    private Text player2LivesText;

    public OneVOneScreen(Pane root, CoinManager coinManager) {
        super(root);
        initializeUI();
    }

    @Override
    protected void initializeUI() {
        // Player 1 (bottom) stats - left side
        player1LivesText = createStyledText("P1 Lives: 3", 10, GAME_HEIGHT - 25, UI_FONT, TEXT_COLOR);

        // Player 2 (top) stats - right side
        player2LivesText = createStyledText("P2 Lives: 3", GAME_WIDTH - 150, 25, UI_FONT, TEXT_COLOR);

        gameMessage = createStyledText("PRESS SPACE TO START", GAME_WIDTH / 2 - 200, GAME_HEIGHT / 2, MESSAGE_FONT, GOLD_COLOR);

        root.getChildren().addAll(player1LivesText, player2LivesText, gameMessage);
    }

    // Player 1 methods
    public int getPlayer1Lives() {
        return player1Lives;
    }

    public void updatePlayer1Lives(int newLives) {
        this.player1Lives = newLives;
        player1LivesText.setText("P1 Lives: " + player1Lives);
    }

    public void decreasePlayer1Lives() {
        updatePlayer1Lives(this.player1Lives - 1);
    }

    // Player 2 methods
    public int getPlayer2Lives() {
        return player2Lives;
    }

    public void updatePlayer2Lives(int newLives) {
        this.player2Lives = newLives;
        player2LivesText.setText("P2 Lives: " + player2Lives);
    }

    public void decreasePlayer2Lives() {
        updatePlayer2Lives(this.player2Lives - 1);
    }

    // GameScreen interface methods (not used in 1v1 mode, but required by interface)
    @Override
    public void updateScore(int newScore) {
        // Not used in 1v1 mode - use updatePlayer1Score/updatePlayer2Score instead
    }

    @Override
    public void increaseScore(int delta) {
        // Not used in 1v1 mode - no scoring
    }

    @Override
    public int getScore() {
        // Not used in 1v1 mode - no scoring
        return 0;
    }

    @Override
    public int getLives() {
        // Return player 1's lives by default
        return player1Lives;
    }

    @Override
    public void updateLives(int newLives) {
        // Not used in 1v1 mode - use updatePlayer1Lives/updatePlayer2Lives instead
    }

    @Override
    public void decreaseLives() {
        // Not used in 1v1 mode - use decreasePlayer1Lives/decreasePlayer2Lives instead
    }

    @Override
    public void updateCoins() {
        // No coin display in 1v1 mode
    }

    @Override
    public void showLevel(int level) {
        // Not used in 1v1 mode - no level progression
    }

    public void showGameMessage(GameState state) {
        switch (state) {
            case START:
                // Don't show "PRESS SPACE TO LAUNCH" - indicator shows who is serving
                hideGameMessage();
                break;
            case GAME_OVER:
                String winner = player1Lives > player2Lives ? "PLAYER 1" : "PLAYER 2";
                showGameMessage("GAME OVER | " + winner + " WINS!", RED_COLOR);
                break;
            case PLAYING:
                hideGameMessage();
                break;
        }
    }

    public void cleanup() {
        if (root != null) {
            root.getChildren().removeAll(player1LivesText, player2LivesText, gameMessage);
        }
    }

    @Override
    public void show() {
        // Not needed for this screen type
    }

    @Override
    public void hide() {
        // Not needed for this screen type
    }

    @Override
    public void refresh() {
        // Not needed for this screen type
    }
}


