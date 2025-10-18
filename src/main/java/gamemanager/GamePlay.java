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
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import userinterface.*;

import java.util.Optional;

import static gameconfig.GameConfig.*;


public class GamePlay extends Application {
    private Pane root;
    private GameConfig.GameState gameState = GameConfig.GameState.MENU;
    private LevelManager levelManager;
    private SingleplayerScreen singleplayerScreen;
    private CollisionManager collisionManager;
    private Paddle paddle;
    private final java.util.List<Ball> balls = new java.util.ArrayList<>();
    private PauseTransition oneshotTimer;
    private AnimationTimer gameLoop;
    private boolean isMovingLeft = false;
    private boolean isMovingRight = false;
    private MenuScreen menuScreen;
    private HighScoreScreen highScoreScreen;
    private ScoreManager scoreManager;
    private GameModeScreen gameModeScreen;
    private SettingScreen settingScreen;
    private GameOverScreen gameOverScreen;

    @Override
    public void start(Stage primaryStage) {
        root = new Pane();
        root.setPrefSize(GAME_WIDTH, GAME_HEIGHT);
        root.setStyle("-fx-background-color: #2c3e50;");

        levelManager = new LevelManager();
        collisionManager = new CollisionManager(levelManager, root);
        scoreManager = new ScoreManager();

        highScoreScreen = new HighScoreScreen(root, this::showMenu, scoreManager);
        gameModeScreen = new GameModeScreen(root, this::startSinglePlayerGame, this::showMenu);
        menuScreen = new MenuScreen(this::showGameModeScreen, this::showHighScoreScreen, this::showSettingScreen);
        settingScreen = new SettingScreen(root, this::showMenu, this::refreshAllScreens);

        gameOverScreen = new GameOverScreen();
        gameOverScreen.setOnRetry(this::retryLevel);
        gameOverScreen.setOnNewGame(this::startNewGame);
        gameOverScreen.setOnMainMenu(this::returnToMenu);

        root.getChildren().add(gameOverScreen.getStackPane());
        gameOverScreen.getStackPane().setStyle("-fx-background-color: transparent;");
        gameOverScreen.hide();



        if (gameState == GameConfig.GameState.MENU) {
            root.getChildren().add(menuScreen.getStackPane());
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
                    if (gameState == GameState.PLAYING || gameState == GameState.START || gameState == GameState.GAME_OVER) {
                        returnToMenu();
                    }
                    break;
                case T:
                    levelManager.clearAllBricks(root);
                    if (levelManager.isLevelComplete()) {
                        changeGameState(GameState.LEVEL_CLEARED);
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
                case R:
                    Ball ball = new Ball(GAME_WIDTH / 2, GAME_HEIGHT - 35, BALL_RADIUS, BALL_SPEED);
                    balls.add(ball);
                    root.getChildren().add(ball.getNode());
                    ball.setStuck(false);
                    ball.launch();
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

    private void refreshAllScreens() {
        highScoreScreen.hide();
        gameModeScreen.hide();

        highScoreScreen.refresh();
        gameModeScreen.refresh();

        menuScreen.refresh(this::showGameModeScreen, this::showHighScoreScreen, this::showSettingScreen);

        root.setPrefSize(GAME_WIDTH, GAME_HEIGHT);

        settingScreen.refresh();
        settingScreen.show();

        System.out.println("All screens refreshed with new UI scale: " + UI_SCALE);
    }

    private void initializeGameElements() {
        double paddleX = (GAME_WIDTH - PADDLE_WIDTH) / 2;
        double paddleY = GAME_HEIGHT - 20;

        paddle = new Paddle(paddleX, paddleY, PADDLE_WIDTH, PADDLE_HEIGHT, PADDLE_SPEED);

        double ballX = GAME_WIDTH / 2;
        double ballY = GAME_HEIGHT - 35;

        Ball ball = new Ball(ballX, ballY, BALL_RADIUS, BALL_SPEED);
        balls.add(ball);
        root.getChildren().addAll(paddle.getNode(), ball.getNode());

        System.out.println("Game elements initialized:");
        System.out.println("  GAME_WIDTH: " + GAME_WIDTH + ", GAME_HEIGHT: " + GAME_HEIGHT);
        System.out.println("  Paddle X: " + paddleX + ", Width: " + PADDLE_WIDTH);
        System.out.println("  Ball X: " + ballX + ", Y: " + ballY);
    }

    private void startSinglePlayerGame() {
        gameModeScreen.hide();
        gameOverScreen.hide();
        if (paddle != null && paddle.getNode().getParent() != null) {
            root.getChildren().remove(paddle.getNode());
        }
        for (Ball b : new java.util.ArrayList<>(balls)) {
            if (b.getNode().getParent() != null) {
                root.getChildren().remove(b.getNode());
            }
        }
        balls.clear();

        for (Brick brick : new java.util.ArrayList<>(levelManager.getBricks())) {
            levelManager.removeBrick(brick, root);
        }
        levelManager.clearAllPowerups(root);

        if (singleplayerScreen != null) {
            singleplayerScreen.cleanup();
        }

        initializeGameElements();
        singleplayerScreen = new SingleplayerScreen(root);
        singleplayerScreen.updateLives(3);
        singleplayerScreen.updateScore(0);
        levelManager.loadLevel(1, root);
        changeGameState(GameState.START);
    }

    private void showMenu() {
        settingScreen.hide();
        highScoreScreen.hide();
        gameModeScreen.hide();
        if (!root.getChildren().contains(menuScreen.getStackPane())) {
            root.getChildren().add(menuScreen.getStackPane());
        }
        menuScreen.getStackPane().setVisible(true);
    }

    private void showGameModeScreen() {
        menuScreen.getStackPane().setVisible(false);
        gameModeScreen.show();
    }

    private void showSettingScreen() {
        menuScreen.getStackPane().setVisible(false);
        settingScreen.show();
    }

    private void showHighScoreScreen() {
        menuScreen.getStackPane().setVisible(false);
        highScoreScreen.show();
    }

    private void startGame() {
        for (Ball b : balls) {
            b.setStuck(false);
            b.launch();
        }
        changeGameState(GameConfig.GameState.PLAYING);
        root.getChildren().remove(menuScreen.getStackPane());
    }

    private void retryLevel() {
        // Chơi lại level hiện tại với 3 mạng và score reset
        changeGameState(GameState.MENU);
        resetGame();

        initializeGameElements();
        singleplayerScreen = new SingleplayerScreen(root);
        singleplayerScreen.updateLives(3);
        singleplayerScreen.updateScore(0);
        levelManager.loadLevel(levelManager.currentLevel, root);
        changeGameState(GameState.START);

        gameOverScreen.hide();
    }

    private void startNewGame() {
        // Reset về level 1 và bắt đầu game mới
        changeGameState(GameState.MENU);
        resetGame();
        levelManager.currentLevel = 1;

        initializeGameElements();
        singleplayerScreen = new SingleplayerScreen(root);
        singleplayerScreen.updateLives(3);
        singleplayerScreen.updateScore(0);
        levelManager.loadLevel(1, root);
        changeGameState(GameState.START);

        gameOverScreen.hide();
    }

    private void initGameLoop() {
        gameLoop = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate < 1_000_000_000 / 60.0) { return; }
                lastUpdate = now;

                if (gameState == GameConfig.GameState.PLAYING) {
                    double timeStep = 1.0 / 240.0;
                    processInput(timeStep);
                    updateGame(timeStep);
                    handleCollisions();

                    processInput(timeStep);
                    updateGame(timeStep);
                    handleCollisions();

                    processInput(timeStep);
                    updateGame(timeStep);
                    handleCollisions();

                    processInput(timeStep);
                    updateGame(timeStep);
                    handleCollisions();
                }
            }
        };
        gameLoop.start();
    }

    private void processInput(double tpf) {
        if (isMovingLeft) { paddle.moveLeft(tpf); }
        if (isMovingRight) { paddle.moveRight(tpf); }
    }

    private void updateGame(double tpf) {
        for (Ball b : new java.util.ArrayList<>(balls)) {
            b.update(tpf, paddle, GAME_WIDTH, GAME_HEIGHT);
        }
        for (Powerup p : new java.util.ArrayList<>(levelManager.getPowerups())) {
            p.update();
        }
    }

    private void handleCollisions() {
        if (gameState != GameConfig.GameState.PLAYING) return;

        java.util.List<Ball> toRemove = new java.util.ArrayList<>();
        for (Ball b : new java.util.ArrayList<>(balls)) {
            GameConfig.WallSideType wallHit = collisionManager.checkWallCollision(b, GAME_WIDTH, GAME_HEIGHT);
            if (wallHit == GameConfig.WallSideType.BOTTOM_HIT) {
                toRemove.add(b);
                continue;
            }

            if (collisionManager.checkPaddleBallCollision(paddle, b)) {
                collisionManager.handlePaddleBallCollision(paddle, b);
            }

            java.util.List<Brick> bricks = levelManager.getBricks();
            Brick hitBrick = collisionManager.checkBrickBallCollision(b, bricks);
            if (hitBrick != null) {
                collisionManager.handleBrickBallCollision(b, hitBrick, singleplayerScreen);
                if (levelManager.isLevelComplete()) {
                    changeGameState(GameConfig.GameState.LEVEL_CLEARED);
                    return;
                }
            }
        }

        for (Ball dead : toRemove) {
            if (dead.getNode() != null && dead.getNode().getParent() != null) {
                root.getChildren().remove(dead.getNode());
            }
            balls.remove(dead);
        }

        if (!toRemove.isEmpty() && balls.isEmpty()) {
            singleplayerScreen.decreaseLives();
            resetBallAndPaddle();
            if (singleplayerScreen.getLives() <= 0) {
                changeGameState(GameConfig.GameState.GAME_OVER);
            }
        }

        for (Powerup p : new java.util.ArrayList<>(levelManager.getPowerups())) {
            if (collisionManager.checkPaddlePowerupCollision(paddle, p)) {
                p.activate(GamePlay.this, paddle);
                if (p.getNode() != null && p.getNode().getParent() != null) {
                    root.getChildren().remove(p.getNode());
                }
                levelManager.removePowerup(p, root);
            }
        }
    }

    private void resetBallAndPaddle() {
        for (Ball b : balls) {
            root.getChildren().remove(b.getNode());
        }
        balls.clear();
        levelManager.clearAllPowerups(root);
        paddle.reset();
        Ball ball = new Ball(paddle.getX() + paddle.getWidth() / 2, paddle.getY() - 8, BALL_RADIUS, BALL_SPEED);
        balls.add(ball);
        root.getChildren().add(ball.getNode());
        gameState = GameState.START;
        for (Ball b : balls) { b.setStuck(true); }
    }

    private void returnToMenu() {
        changeGameState(GameState.MENU);
        resetGame();
        // Ensure menu is properly shown with current resolution
        if (!root.getChildren().contains(menuScreen.getStackPane())) {
            root.getChildren().add(menuScreen.getStackPane());
        }
        menuScreen.getStackPane().setVisible(true);
        menuScreen.getStackPane().toFront();
        gameOverScreen.hide();
    }

    private void resetGame() {
        if (paddle != null && paddle.getNode().getParent() != null) {
            root.getChildren().remove(paddle.getNode());
        }

        for (Ball b : balls) {
            if (b.getNode().getParent() != null) {
                root.getChildren().remove(b.getNode());
            }
        }
        balls.clear();

        for (Brick brick : new java.util.ArrayList<>(levelManager.getBricks())) {
            levelManager.removeBrick(brick, root);
        }

        levelManager.clearAllPowerups(root);

        if (singleplayerScreen != null) {
            singleplayerScreen.cleanup();
            singleplayerScreen = null;
        }
    }

    private void changeGameState(GameConfig.GameState newState) {
        this.gameState = newState;
        if (singleplayerScreen != null) {
            singleplayerScreen.showGameMessage(newState);
        }

        if (newState == GameConfig.GameState.LEVEL_CLEARED) {
            levelManager.currentLevel++;
            if (levelManager.currentLevel <= levelManager.maxLevel) {
                resetBallAndPaddle();
                levelManager.loadLevel(levelManager.currentLevel, root);
                singleplayerScreen.showLevel(levelManager.currentLevel);
            } else {
                changeGameState(GameConfig.GameState.GAME_OVER);
            }
        }
        else if (newState == GameConfig.GameState.GAME_OVER) {

                // Ẩn thông báo từ SingleplayerScreen
                if (singleplayerScreen != null) {
                    singleplayerScreen.hideGameMessage();
                }

                // Cập nhật điểm số cho GameOverScreen
                if (singleplayerScreen != null) {
                    int finalScore = singleplayerScreen.getScore();
                    gameOverScreen.setFinalScore(finalScore);
                    System.out.println("Final Score: " + finalScore);
                }

                // Nhờ nhật UI cho GameOverScreen
                gameOverScreen.refresh();

                // Hiển thị GameOverScreen
                gameOverScreen.show();
                System.out.println("GameOverScreen showed");

                // Lưu điểm số
                promptAndSaveScore();
            }
        if (newState != GameConfig.GameState.PLAYING) { gameLoop.stop(); } else { gameLoop.start(); }
    }

    public void spawnExtraBall() {
        if (balls.isEmpty() || balls.size() > 300) return;
        int size = balls.size();
        for (int i = 0; i < size; i++){
            Ball ref = balls.get(i);
            Ball newBall = new Ball(ref.getX() + ref.getRadius(), ref.getY() + ref.getRadius(), ref.getRadius(), ref.speed);
            newBall.setVx(-ref.getVx());
            newBall.setVy(ref.getVy());
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

    private void promptAndSaveScore() {
        TextInputDialog dialog = new TextInputDialog("Player");
        dialog.setTitle("Game Over");
        dialog.setHeaderText("You set a new score!");
        dialog.setContentText("Please enter your name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            int finalScore = singleplayerScreen.getScore();
            scoreManager.addScore(name, finalScore);
        });
    }
}
