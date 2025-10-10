package gamemanager;

import gameconfig.GameConfig;
import gameobject.Ball;
import gameobject.Brick;
import gameobject.Paddle;
import gameobject.Powerup;
import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import userinterface.Menu;
import userinterface.PlayScreen;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static gameconfig.GameConfig.*;

public class GamePlay extends Application {
    private Pane root;
    private GameConfig.GameState gameState = GameConfig.GameState.MENU;
    private LevelManager levelManager;
    private PlayScreen playScreen;
    private CollisionManager collisionManager;
    private Paddle paddle;
    private java.util.List<Ball> balls = new java.util.ArrayList<>();
    private PauseTransition oneshotTimer;
    private AnimationTimer gameLoop;
    private boolean isMovingLeft = false;
    private boolean isMovingRight = false;
    private Menu menu;

    @Override
    public void start(Stage primaryStage) {
        root = new Pane();
        root.setPrefSize(GAME_WIDTH, GAME_HEIGHT);
        root.setStyle("-fx-background-color: #2c3e50;");

        levelManager = new LevelManager();
        collisionManager = new CollisionManager(levelManager, root);

        paddle = new Paddle(GAME_WIDTH / 2 - 50, GAME_HEIGHT - 20, 1000, 15, PADDLE_SPEED);
        Ball ball = new Ball(GAME_WIDTH / 2, GAME_HEIGHT - 35, 8, 5.0);
        balls.add(ball);

        // Thêm menu
        menu = new Menu(() -> {
            root.getChildren().remove(menu.getStackPane());
            root.getChildren().addAll(paddle.getNode(), ball.getNode());
            playScreen = new PlayScreen(root);
            playScreen.updateLives(3);
            playScreen.updateScore(0);
            levelManager.loadLevel(1, root);
            changeGameState(GameState.START);
        });
        if (gameState == GameConfig.GameState.MENU) {
            root.getChildren().add(menu.getStackPane());
        }

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
                case ESCAPE:
                    if (gameState == GameState.PLAYING || gameState == GameState.START) {
                        returnToMenu();
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

        primaryStage.setTitle("Arkanoid");
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
        // Ẩn menu nếu còn trên root
        root.getChildren().remove(menu.getStackPane());
    }

    private void initGameLoop() {
        gameLoop = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                double FPS = 60.0;
                if (now - lastUpdate < 1_000_000_000 / FPS) { return; }
                lastUpdate = now;
                if (gameState == GameConfig.GameState.PLAYING) {
                    double TPF = 1.0 / FPS;
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
                            collisionManager.handleBrickBallCollision(b, hitBrick, playScreen);
                            if (levelManager.getBricks().isEmpty()) { changeGameState(GameConfig.GameState.LEVEL_CLEARED); }
                        }
                    }
                    for (Ball dead : toRemove) {
                        root.getChildren().remove(dead.getNode());
                        balls.remove(dead);
                    }
                    if (!toRemove.isEmpty() && balls.isEmpty()) {
                        playScreen.decreaseLives();
                        resetBallAndPaddle();
                        if (playScreen.getLives() <= 0) { changeGameState(GameConfig.GameState.GAME_OVER); }
                    }
                    java.util.List<Powerup> powerups = levelManager.getPowerups();
                    for (Powerup p : new java.util.ArrayList<>(powerups)) {
                        p.move();
                        if (collisionManager.checkPaddlePowerupCollision(paddle, p)) {
                            p.activate(GamePlay.this, paddle);
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
        Ball ball = new Ball(paddle.getX() + paddle.getWidth() / 2, paddle.getY() - 8, 8, 10.0);
        balls.add(ball);
        root.getChildren().add(ball.getNode());
        gameState = GameState.START;
        for (Ball b : balls) { b.setStuck(true); }
    }

    private void returnToMenu() {
        changeGameState(GameState.MENU);

        root.getChildren().remove(paddle.getNode());
        for (Ball b : balls) {
            root.getChildren().remove(b.getNode());
        }
        balls.clear();

        for (Brick brick : new java.util.ArrayList<>(levelManager.getBricks())) {
            levelManager.removeBrick(brick, root);
        }

        levelManager.clearAllPowerups(root);

        if (playScreen != null) {
            playScreen.cleanup();
            playScreen = null;
        }

        if (menu != null) {
            if (!root.getChildren().contains(menu.getStackPane())) {
                root.getChildren().add(menu.getStackPane());
            }
        }
    }

    private void changeGameState(GameConfig.GameState newState) {
        this.gameState = newState;
        if (playScreen != null) {
            playScreen.showGameMessage(newState);
        }

        if (newState == GameConfig.GameState.LEVEL_CLEARED) {
            levelManager.currentLevel++;
            if (levelManager.currentLevel <= levelManager.maxLevel) {
                resetBallAndPaddle();
                levelManager.loadLevel(levelManager.currentLevel, root);
                playScreen.showLevel(levelManager.currentLevel);
            } else {
                changeGameState(GameConfig.GameState.GAME_OVER);
            }
        }
        if (newState != GameConfig.GameState.PLAYING) { gameLoop.stop(); } else { gameLoop.start(); }
    }

    public void spawnExtraBall() {
        if (balls.isEmpty()) return;
        int size = balls.size();
        for (int i = 0; i < size; i++){
            Ball ref = balls.get(i);
            Ball newBall = new Ball(ref.getX() + ref.getRadius(), ref.getY() + ref.getRadius(), ref.getRadius(), ref.speed);
            newBall.setVx(ref.getVx()*(new Random().nextDouble(2.01) - 1));
            newBall.setVy(ref.getVy()*(new Random().nextInt(3) - 1));
            if (newBall.getVy() < 0.1) {
                newBall.setVy((newBall.getVy()) + ThreadLocalRandom.current().nextDouble(-1, 2));
            }
            newBall.setStuck(false);
            balls.add(newBall);
            root.getChildren().add(newBall.getNode());
        }
    }

    public void enableOneshot() {
        collisionManager.setOneshotActive(true);
        if (oneshotTimer != null) {
            oneshotTimer.stop();
        }
        oneshotTimer = new PauseTransition(Duration.seconds(7.5));
        oneshotTimer.setOnFinished(event -> collisionManager.setOneshotActive(false));
        oneshotTimer.playFromStart();
    }
}
