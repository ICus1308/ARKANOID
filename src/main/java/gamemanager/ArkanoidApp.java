package gamemanager;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import gameconfig.GameConfig;
import gameobject.*;
import javafx.util.Duration;
import userinterface.UserInterface;
import static gameconfig.GameConfig.*;

public class ArkanoidApp extends Application {
    private Pane root;
    private GameConfig.GameState gameState = GameConfig.GameState.START;
    private LevelManager levelManager;
    private UserInterface userInterface;
    private CollisionManager collisionManager;
    private Paddle paddle;
    private java.util.List<Ball> balls = new java.util.ArrayList<>();
    private boolean oneshotActive = false;
    private PauseTransition oneshotTimer;
    private AnimationTimer gameLoop;
    private boolean isMovingLeft = false;
    private boolean isMovingRight = false;
    private boolean deadReset = false;

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
        Ball ball = new Ball(GAME_WIDTH / 2, GAME_HEIGHT - 35, 8, 5.0);
        balls.add(ball);
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
        for (Ball b : balls) {
            b.setStuck(false);
            b.launch();
        }
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
                    java.util.List<Ball> toRemove = new java.util.ArrayList<>();
                    for (Ball b : new java.util.ArrayList<>(balls)) {
                        b.update(TPF, paddle, GAME_WIDTH, GAME_HEIGHT);
                        GameConfig.WallSideType wallHit = collisionManager.checkWallCollision(b, GAME_WIDTH, GAME_HEIGHT);
                        if (wallHit == GameConfig.WallSideType.BOTTOM_HIT) {
                            toRemove.add(b);
                        }
                        if (collisionManager.checkPaddleBallCollision(paddle, b)) {
                            collisionManager.handlePaddleBallCollision(paddle, b);
                        }
                        java.util.List<Brick> bricks = levelManager.getBricks();
                        Brick hitBrick = collisionManager.checkBrickBallCollision(b, bricks);
                        if (hitBrick != null) {
                            collisionManager.handleBrickBallCollision(b, hitBrick, userInterface);
                            if (levelManager.getBricks().isEmpty()) { changeGameState(GameConfig.GameState.LEVEL_CLEARED); }
                        }
                    }
                    for (Ball dead : toRemove) {
                        root.getChildren().remove(dead.getNode());
                        balls.remove(dead);
                    }
                    if (!toRemove.isEmpty() && balls.isEmpty()) {
                        userInterface.decreaseLives();
                        resetBallAndPaddle();
                        if (userInterface.getLives() <= 0) { changeGameState(GameConfig.GameState.GAME_OVER); }
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
        for (Ball b : balls) {
            root.getChildren().remove(b.getNode());
        }
        balls.clear();
        levelManager.clearAllPowerups(root);
        paddle.reset();
        Ball ball = new Ball(paddle.getX() + paddle.getWidth() / 2, paddle.getY() - 8, 8, 5.0);
        balls.add(ball);
        root.getChildren().add(ball.getNode());
        gameState = GameConfig.GameState.START;
        deadReset = true;
        for (Ball b : balls) { b.setStuck(true); }
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

    public void spawnExtraBall() {
        if (balls.isEmpty()) return;
        Ball ref = balls.getFirst();
        Ball newBall = new Ball(ref.getX() + ref.getRadius(), ref.getY() + ref.getRadius(), ref.getRadius(), ref.speed);
        newBall.setVx(-ref.getVx());
        newBall.setVy(ref.getVy());
        newBall.setStuck(false);
        balls.add(newBall);
        root.getChildren().add(newBall.getNode());
    }

    public void enableOneshot() {
        oneshotActive = true;
        collisionManager.setOneshotActive(true);
        if (oneshotTimer != null) {
            oneshotTimer.stop();
        }
        oneshotTimer = new PauseTransition(Duration.seconds(7.5));
        oneshotTimer.setOnFinished(event -> {
            oneshotActive = false;
            collisionManager.setOneshotActive(false);
        });
        oneshotTimer.playFromStart();
    }
}

