package gameplay;

import config.GameConfig;
import entities.Paddle;
import ball.*;
import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import level.LevelManager;
import javafx.scene.canvas.Canvas;
import java.net.URL;
import java.util.ResourceBundle;

public class GameController implements Initializable {
    @FXML public Pane gameRoot;

    private boolean leftHeld = false;
    private boolean rightHeld = false;

    private Paddle paddle;

    private Ball ball = new Ball(GameConfig.WIDTH / 2, GameConfig.HEIGHT / 2, GameConfig.BALL_R);

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
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastNs == 0) { lastNs = now; return; }
                double dt = (now - lastNs) / 1_000_000_000.0;
                lastNs = now;
                update(dt);
                ball.move(dt, GameConfig.WIDTH, GameConfig.HEIGHT);

                gc.clearRect(0, 0, GameConfig.WIDTH, GameConfig.HEIGHT);
                if (levelManager != null) {
                    levelManager.draw(gc);
                }
                ball.draw(gc);
            }
        };
        loop.start();
    }

    @FXML
    private Canvas gameCanvas;

    private LevelManager levelManager;

    @FXML
    public void initialize() {
        System.out.println("Initialize called. gameCanvas=" + gameCanvas);
        // KHÔNG gọi initGame() ở đây nữa vì gameCanvas có thể null
    }

    private void initGame() {
        System.out.println("initGame called. gameCanvas=" + gameCanvas);
        if (gameCanvas == null) {
            System.out.println("ERROR: gameCanvas is null in initGame!");
            return;
        }

        levelManager = new LevelManager();
        levelManager.loadLevel("/mapturtorial.txt");
        draw();
    }

    private void draw() {
        System.out.println("draw() called. gameCanvas=" + gameCanvas);
        if (gameCanvas == null) {
            System.out.println("gameCanvas is null!");
            return;
        }
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, GameConfig.WIDTH, GameConfig.HEIGHT);
        levelManager.draw(gc);
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
            controller.initGame(); // Gọi initGame() tại đây
            controller.startGameLoop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
