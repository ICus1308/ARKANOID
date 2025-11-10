package userinterface.gamescreen;

import gamemanager.ui.UIManager;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;

import java.util.Objects;

import static gameconfig.GameConfig.*;

public class BotScreen extends UIManager implements GameScreen {
    private static final int INITIAL_LIVES = 3;

    private int playerLives;
    private int botLives;
    private Text playerLivesText;
    private Text botLivesText;
    private ImageView playerLivesIcon;
    private ImageView botLivesIcon;
    private HBox playerLivesBox;
    private HBox botLivesBox;

    public BotScreen(Pane root) {
        super(root);
        this.playerLives = INITIAL_LIVES;
        this.botLives = INITIAL_LIVES;
        initializeUI();
    }

    @Override
    protected void initializeUI() {
        // Create player lives display with icon
        try {
            Image livesImage = new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/imagelive/imagelive.png")));
            playerLivesIcon = new ImageView(livesImage);
            playerLivesIcon.setFitWidth(25);
            playerLivesIcon.setFitHeight(25);
            playerLivesIcon.setPreserveRatio(true);
        } catch (Exception e) {
            System.err.println("Error loading player lives icon");
        }

        playerLivesText = createStyledText(INITIAL_LIVES + "", 0, 0, UI_FONT, TEXT_COLOR);
        playerLivesBox = new HBox(5);
        playerLivesBox.setAlignment(Pos.CENTER_LEFT);
        playerLivesBox.getChildren().addAll(playerLivesIcon, playerLivesText);
        playerLivesBox.setLayoutX(10);
        playerLivesBox.setLayoutY(GAME_HEIGHT - 40);

        // Create bot lives display with icon
        try {
            Image livesImage = new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/imagelive/imagelive.png")));
            botLivesIcon = new ImageView(livesImage);
            botLivesIcon.setFitWidth(25);
            botLivesIcon.setFitHeight(25);
            botLivesIcon.setPreserveRatio(true);
        } catch (Exception e) {
            System.err.println("Error loading bot lives icon");
        }

        botLivesText = createStyledText(INITIAL_LIVES + "", 0, 0, UI_FONT, TEXT_COLOR);
        botLivesBox = new HBox(5);
        botLivesBox.setAlignment(Pos.CENTER_LEFT);
        botLivesBox.getChildren().addAll(botLivesIcon, botLivesText);
        botLivesBox.setLayoutX(GAME_WIDTH - 80);
        botLivesBox.setLayoutY(10);

        gameMessage = createStyledText("PRESS SPACE TO START", GAME_WIDTH / 2 - 200, GAME_HEIGHT / 2, MESSAGE_FONT, GOLD_COLOR);
        root.getChildren().addAll(playerLivesBox, botLivesBox, gameMessage);
    }

    public int getPlayerLives() {
        return playerLives;
    }

    public void updatePlayerLives(int newLives) {
        this.playerLives = newLives;
        playerLivesText.setText(playerLives + "");
    }

    public void decreasePlayerLives() {
        updatePlayerLives(playerLives - 1);
    }

    public int getBotLives() {
        return botLives;
    }

    public void updateBotLives(int newLives) {
        this.botLives = newLives;
        botLivesText.setText(botLives + "");
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
            root.getChildren().removeAll(playerLivesBox, botLivesBox, gameMessage);
        }
    }
}
