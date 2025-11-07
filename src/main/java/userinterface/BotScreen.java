package userinterface;

import gamemanager.UIManager;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import static gameconfig.GameConfig.*;

public class BotScreen extends UIManager implements GameScreen {
    private static final int INITIAL_LIVES = 3;

    private int playerLives;
    private int botLives;
    private Text playerLivesText;
    private Text botLivesText;

    public BotScreen(Pane root) {
        super(root);
        this.playerLives = INITIAL_LIVES;
        this.botLives = INITIAL_LIVES;
        initializeUI();
    }

    @Override
    protected void initializeUI() {
        playerLivesText = createStyledText("Player Lives: " + INITIAL_LIVES, 10, GAME_HEIGHT - 25, UI_FONT, TEXT_COLOR);
        botLivesText = createStyledText("Bot Lives: " + INITIAL_LIVES, GAME_WIDTH - 150, 25, UI_FONT, TEXT_COLOR);
        gameMessage = createStyledText("PRESS SPACE TO START", GAME_WIDTH / 2 - 200, GAME_HEIGHT / 2, MESSAGE_FONT, GOLD_COLOR);
        root.getChildren().addAll(playerLivesText, botLivesText, gameMessage);
    }

    public int getPlayerLives() {
        return playerLives;
    }

    public void updatePlayerLives(int newLives) {
        this.playerLives = newLives;
        playerLivesText.setText("Player Lives: " + playerLives);
    }

    public void decreasePlayerLives() {
        updatePlayerLives(playerLives - 1);
    }

    public int getBotLives() {
        return botLives;
    }

    public void updateBotLives(int newLives) {
        this.botLives = newLives;
        botLivesText.setText("Bot Lives: " + botLives);
    }

    public void decreaseBotLives() {
        updateBotLives(botLives - 1);
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
        return playerLives;
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
                String winner = playerLives > botLives ? "PLAYER" : "BOT";
                showGameMessage("GAME OVER | " + winner + " WINS!", RED_COLOR);
                break;
        }
    }

    public void cleanup() {
        if (root != null) {
            root.getChildren().removeAll(playerLivesText, botLivesText, gameMessage);
        }
    }
}

