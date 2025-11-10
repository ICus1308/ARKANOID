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

public class OneVOneScreen extends UIManager implements GameScreen {
    private static final int INITIAL_LIVES = 3;

    private int player1Lives;
    private int player2Lives;
    private Text player1LivesText;
    private Text player2LivesText;
    private ImageView player1LivesIcon;
    private ImageView player2LivesIcon;
    private HBox player1LivesBox;
    private HBox player2LivesBox;

    public OneVOneScreen(Pane root) {
        super(root);
        this.player1Lives = INITIAL_LIVES;
        this.player2Lives = INITIAL_LIVES;
        initializeUI();
    }

    @Override
    protected void initializeUI() {
        // Create player 1 lives display with icon
        try {
            Image livesImage = new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/imagelive/imagelive.png")));
            player1LivesIcon = new ImageView(livesImage);
            player1LivesIcon.setFitWidth(25);
            player1LivesIcon.setFitHeight(25);
            player1LivesIcon.setPreserveRatio(true);
        } catch (Exception e) {
            System.err.println("Error loading player 1 lives icon");
        }

        player1LivesText = createStyledText(INITIAL_LIVES + "", 0, 0, UI_FONT, TEXT_COLOR);
        player1LivesBox = new HBox(5);
        player1LivesBox.setAlignment(Pos.CENTER_LEFT);
        player1LivesBox.getChildren().addAll(player1LivesIcon, player1LivesText);
        player1LivesBox.setLayoutX(10);
        player1LivesBox.setLayoutY(GAME_HEIGHT - 40);

        // Create player 2 lives display with icon
        try {
            Image livesImage = new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/imagelive/imagelive.png")));
            player2LivesIcon = new ImageView(livesImage);
            player2LivesIcon.setFitWidth(25);
            player2LivesIcon.setFitHeight(25);
            player2LivesIcon.setPreserveRatio(true);
        } catch (Exception e) {
            System.err.println("Error loading player 2 lives icon");
        }

        player2LivesText = createStyledText(INITIAL_LIVES + "", 0, 0, UI_FONT, TEXT_COLOR);
        player2LivesBox = new HBox(5);
        player2LivesBox.setAlignment(Pos.CENTER_LEFT);
        player2LivesBox.getChildren().addAll(player2LivesIcon, player2LivesText);
        player2LivesBox.setLayoutX(GAME_WIDTH - 80);
        player2LivesBox.setLayoutY(10);

        gameMessage = createStyledText("PRESS SPACE TO START", GAME_WIDTH / 2 - 200, GAME_HEIGHT / 2, MESSAGE_FONT, GOLD_COLOR);
        root.getChildren().addAll(player1LivesBox, player2LivesBox, gameMessage);
    }

    public int getPlayer1Lives() {
        return player1Lives;
    }

    public void updatePlayer1Lives(int newLives) {
        this.player1Lives = newLives;
        player1LivesText.setText(player1Lives + "");
    }

    public void decreasePlayer1Lives() {
        updatePlayer1Lives(player1Lives - 1);
    }

    public int getPlayer2Lives() {
        return player2Lives;
    }

    public void updatePlayer2Lives(int newLives) {
        this.player2Lives = newLives;
        player2LivesText.setText(player2Lives + "");
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
            root.getChildren().removeAll(player1LivesBox, player2LivesBox, gameMessage);
        }
    }
}
