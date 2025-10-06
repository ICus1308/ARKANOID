package gamemanager;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import gameconfig.GameConfig;
import gameobject.*;
import userinterface.UserInterface;
import static gameconfig.GameConfig.*;

public class ArkanoidApp extends Application {
    private Pane root;
    private GameConfig.GameState gameState = GameConfig.GameState.START;
    private LevelManager levelManager;
    private UserInterface userInterface;
    private CollisionManager collisionManager;
    private Paddle paddle;
    private Ball ball;
    private AnimationTimer gameLoop;
    private boolean isMovingLeft = false;
    private boolean isMovingRight = false;

    @Override
    public void start(Stage primaryStage) {
        root = new Pane();
        root.setPrefSize(GAME_WIDTH, GAME_HEIGHT);
        root.setStyle("-fx-background-color: #2c3e50;");

        levelManager = new LevelManager();
        collisionManager = new CollisionManager(levelManager, root);
        userInterface = new UserInterface(root);
        userInterface.updateLives(3);
        userInterface.updateScore(0);

        paddle = new Paddle(GAME_WIDTH / 2 - 50, GAME_HEIGHT - 20, 100, 15, PADDLE_SPEED);
        ball = new Ball(GAME_WIDTH / 2, GAME_HEIGHT - 35, 8, 5.0);
        root.getChildren().addAll(paddle.getNode(), ball.getNode());

        Scene scene = new Scene(root);
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case A:
                case LEFT:
                    isMovingLeft = true;
                    break;
                case D:
                case RIGHT:
                    isMovingRight = true;
                    break;
                case SPACE:
                    if (gameState == GameState.START || gameState == GameState.LEVEL_CLEARED) {
                        startGame();
                    }
                    break;
                default:
                    break;
            }
        });
        scene.setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case A:
                case LEFT:
                    isMovingLeft = false;
                    break;
                case D:
                case RIGHT:
                    isMovingRight = false;
                    break;
                default:
                    break;
            }
        });

        levelManager.loadLevel(1, root);

        primaryStage.setTitle("Arkanoid UML Game");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        initGameLoop();
    }

    private void startGame() {
        ball.setStuck(false);
        ball.launch();
        changeGameState(GameConfig.GameState.PLAYING);
    }

    private void initGameLoop() {
        gameLoop = new AnimationTimer() {
            private long lastUpdate = 0;
            private final double FPS = 60.0;
            private final double TPF = 1.0 / FPS;

            @Override
            public void handle(long now) {
                if (now - lastUpdate < 1_000_000_000 / FPS) { return; }
                lastUpdate = now;
                if (gameState == GameConfig.GameState.PLAYING) {
                    if (isMovingLeft) { paddle.moveLeft(TPF); }
                    if (isMovingRight) { paddle.moveRight(TPF); }
                    ball.update(TPF, paddle, GAME_WIDTH, GAME_HEIGHT);
                    GameConfig.WallSideType wallHit = collisionManager.checkWallCollision(ball, GAME_WIDTH, GAME_HEIGHT);
                    if (wallHit == GameConfig.WallSideType.BOTTOM_HIT) {
                        userInterface.decreaseLives();
                        resetBallAndPaddle();
                        if (userInterface.getLives() <= 0) { changeGameState(GameConfig.GameState.GAME_OVER); }
                    }
                    if (collisionManager.checkPaddleBallCollision(paddle, ball)) {
                        collisionManager.handlePaddleBallCollision(paddle, ball);
                    }
                    java.util.List<Brick> bricks = levelManager.getBricks();
                    Brick hitBrick = collisionManager.checkBrickBallCollision(ball, bricks);
                    if (hitBrick != null) {
                        collisionManager.handleBrickBallCollision(ball, hitBrick, userInterface);
                        if (levelManager.getBricks().isEmpty()) { changeGameState(GameConfig.GameState.LEVEL_CLEARED); }
                    }
                    java.util.List<Powerup> powerups = levelManager.getPowerups();
                    for (Powerup p : new java.util.ArrayList<>(powerups)) {
                        p.move();
                        if (collisionManager.checkPaddlePowerupCollision(paddle, p)) {
                            p.activate(ArkanoidApp.this, paddle);
                            levelManager.removePowerup(p, root);
                        }
                    }
                }
            }
        };
        gameLoop.start();
    }

    private void resetBallAndPaddle() {
        ball.reset(paddle.getX() + paddle.getWidth() / 2, paddle.getY() - ball.getRadius());
        paddle.reset();
        gameState = GameConfig.GameState.START;
        ball.setStuck(true);
    }

    private void changeGameState(GameConfig.GameState newState) {
        this.gameState = newState;
        userInterface.showGameMessage(newState);
        if (newState == GameConfig.GameState.LEVEL_CLEARED) {
            levelManager.currentLevel++;
            if (levelManager.currentLevel <= levelManager.maxLevel) {
                resetBallAndPaddle();
                levelManager.loadLevel(levelManager.currentLevel, root);
                userInterface.showLevel(levelManager.currentLevel);
            } else {
                changeGameState(GameConfig.GameState.GAME_OVER);
            }
        }
        if (newState != GameConfig.GameState.PLAYING) { gameLoop.stop(); } else { gameLoop.start(); }
    }
}

