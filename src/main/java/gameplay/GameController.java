package gameplay;

import config.GameConfig;
import entities.Paddle;
import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class GameController implements Initializable {
    @FXML public Pane gameRoot;

    private boolean leftHeld = false;
    private boolean rightHeld = false;

    private Paddle paddle;

    private long lastNs = 0;
    private AnimationTimer loop;

    public void initialize(URL url, ResourceBundle rb) {
        paddle = new Paddle((GameConfig.WIDTH - GameConfig.PADDLE_W) / 2,
                GameConfig.HEIGHT - GameConfig.PADDLE_H - 30,
                GameConfig.PADDLE_W, GameConfig.PADDLE_H);

        if (gameRoot != null) {
            gameRoot.getChildren().add(paddle);
        }
    }

    public void attachInput(Scene scene) {
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case LEFT, A -> leftHeld = true;
                case RIGHT, D -> rightHeld = true;
                default -> {}
            }
        });
        scene.setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case LEFT, A -> leftHeld = false;
                case RIGHT, D -> rightHeld = false;
                default -> {}
            }
        });
    }

    public void startGameLoop() {
        loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastNs == 0) { lastNs = now; return; }
                double dt = (now - lastNs) / 1_000_000_000.0;
                lastNs = now;
                update(dt);
            }
        };
        loop.start();
    }

    private void update(double dt) {
        if (leftHeld) {
            paddle.setTranslateX(Math.max(0, paddle.getTranslateX() - GameConfig.PADDLE_SPEED * dt));
        }
        if (rightHeld) {
            paddle.setTranslateX(Math.min(GameConfig.WIDTH - GameConfig.PADDLE_W,
                    paddle.getTranslateX() + GameConfig.PADDLE_SPEED * dt));
        }
    }

    public void onStart(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/game.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, GameConfig.WIDTH, GameConfig.HEIGHT));
            stage.setTitle("Arkanoid");
            stage.setResizable(false);
            stage.show();

            GameController controller = loader.getController();
            controller.attachInput(stage.getScene());
            controller.startGameLoop();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
