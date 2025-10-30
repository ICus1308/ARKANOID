package gamemanager;

import gameconfig.GameConfig;
import gameobject.Ball;
import gameobject.Brick;
import gameobject.Indicator;
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
    private Scene scene;
    private Stage primaryStage;
    private GameConfig.GameState gameState = GameConfig.GameState.MENU;

    private LevelManager levelManager;
    private CollisionManager collisionManager;
    private ScoreManager scoreManager;
    private CoinManager coinManager;

    private MenuScreen menuScreen;
    private GameModeScreen gameModeScreen;
    private HighScoreScreen highScoreScreen;
    private SettingScreen settingScreen;
    private SingleplayerScreen singleplayerScreen;
    private BotScreen botScreen;
    private GameOverScreen gameOverScreen;
    private ShopScreen shopScreen;

    private Paddle paddle;
    private final java.util.List<Ball> balls = new java.util.ArrayList<>();
    private Indicator indicator;
    private AIManager aiManager;
    private boolean isBotMode = false;

    private PauseTransition oneshotTimer;
    private AnimationTimer gameLoop;
    private boolean isMovingLeft = false;
    private boolean isMovingRight = false;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        initializeRoot();
        initializeManagers();
        initializeScreens();
        setupScene();
        setupStage();

        showMenuScreen();
        initGameLoop();
    }

    private void initializeRoot() {
        root = new Pane();
        root.setPrefSize(GAME_WIDTH, GAME_HEIGHT);
        root.setStyle("-fx-background-color: #2c3e50;");
    }

    private void initializeManagers() {
        levelManager = new LevelManager();
        collisionManager = new CollisionManager(levelManager, root);
        scoreManager = new ScoreManager();
        coinManager = new CoinManager();
        collisionManager.setCoinManager(coinManager);
    }

    private void initializeScreens() {
        menuScreen = new MenuScreen(
            this::showGameModeScreen,
            this::showHighScoreScreen,
            this::showSettingScreen,
            this::showShopScreen
        );

        gameModeScreen = new GameModeScreen(
            root,
            this::startSinglePlayerGame,
            this::startBotGame,
            this::showMenuScreen
        );

        highScoreScreen = new HighScoreScreen(
            root,
            this::showMenuScreen,
            scoreManager
        );

        settingScreen = new SettingScreen(
            root,
            this::showMenuScreen,
            this::refreshAllScreens
        );

        gameOverScreen = new GameOverScreen(
            root,
            this::retryLevel,
            this::startNewGame,
            this::returnToMenu
        );

        shopScreen = new ShopScreen(
            root,
            coinManager,
            this::applySkin,
            this::showMenuScreen
        );
    }

    private void setupScene() {
        scene = new Scene(root);
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case A:
                case LEFT:
                    if (gameState == GameState.START && indicator != null) {
                        indicator.rotateLeft(0.05);
                    } else {
                        isMovingLeft = true;
                    }
                    break;
                case D:
                case RIGHT:
                    if (gameState == GameState.START && indicator != null) {
                        indicator.rotateRight(0.05);
                    } else {
                        isMovingRight = true;
                    }
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
                    spawnDebugBall();
                    break;
                default:
                    break;
            }
        });
    }

    private void setupStage() {
        primaryStage.setTitle("Arkanoid");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void showMenuScreen() {
        hideAllScreens();
        if (!root.getChildren().contains(menuScreen.getStackPane())) {
            root.getChildren().add(menuScreen.getStackPane());
        }
        menuScreen.getStackPane().setVisible(true);
        menuScreen.getStackPane().toFront();
    }

    private void showGameModeScreen() {
        hideAllScreens();
        gameModeScreen.show();
    }

    private void showSettingScreen() {
        hideAllScreens();
        settingScreen.show();
    }

    private void showHighScoreScreen() {
        hideAllScreens();
        highScoreScreen.show();
    }

    private void showShopScreen() {
        hideAllScreens();
        shopScreen.show();
    }

    private void hideAllScreens() {
        if (menuScreen != null && menuScreen.getStackPane() != null) {
            menuScreen.getStackPane().setVisible(false);
        }
        gameModeScreen.hide();
        highScoreScreen.hide();
        settingScreen.hide();
        gameOverScreen.hide();
        shopScreen.hide();
    }

    private void refreshAllScreens() {
            highScoreScreen.refresh();
            gameModeScreen.refresh();
            menuScreen.refresh(this::showGameModeScreen, this::showHighScoreScreen, this::showSettingScreen, this::showShopScreen);
            gameOverScreen.refresh();
            shopScreen.refresh();

        root.setPrefSize(GAME_WIDTH, GAME_HEIGHT);

            settingScreen.refresh();
            settingScreen.show();
    }


    private void initializeGameElements() {
        double paddleX = (GAME_WIDTH - PADDLE_WIDTH) / 2;
        double paddleY = GAME_HEIGHT - 20;
        paddle = new Paddle(paddleX, paddleY, PADDLE_WIDTH, PADDLE_HEIGHT, PADDLE_SPEED);

        double ballX = GAME_WIDTH / 2;
        double ballY = GAME_HEIGHT - 35;
        Ball ball = new Ball(ballX, ballY, BALL_RADIUS, BALL_SPEED);
        balls.add(ball);

        indicator = new Indicator(ballX, ballY);
        indicator.pointAtBall(ball);

        root.getChildren().addAll(paddle.getNode(), ball.getNode(), indicator.getNode());
    }

    private void startSinglePlayerGame() {
        hideAllScreens();
        cleanupGameObjects();
        initializeGameElements();
        isBotMode = false;

        singleplayerScreen = new SingleplayerScreen(root, coinManager);
        singleplayerScreen.updateLives(3);
        singleplayerScreen.updateScore(0);
        singleplayerScreen.updateCoins();

        levelManager.loadLevel(1, root);
        changeGameState(GameState.START);
    }

    private void startBotGame() {
        hideAllScreens();
        cleanupGameObjects();
        initializeGameElements();

        isBotMode = true;
        aiManager = new AIManager(paddle);

        botScreen = new BotScreen(root, coinManager);
        botScreen.updateLives(3);
        botScreen.updateScore(0);
        botScreen.updateCoins();

        levelManager.loadLevel(1, root);
        changeGameState(GameState.START);
    }

    private void startGame() {
        double[] launchDirection = null;
        if (indicator != null) {
            launchDirection = indicator.getLaunchDirection();
        }

        for (Ball b : balls) {
            b.setStuck(false);
            if (launchDirection != null) {
                b.launch(launchDirection[0], launchDirection[1]);
            } else {
                b.launch();
            }
        }

        if (indicator != null) {
            if (indicator.getNode().getParent() != null) {
                root.getChildren().remove(indicator.getNode());
            }
            indicator = null;
        }

        changeGameState(GameConfig.GameState.PLAYING);
    }

    private void retryLevel() {
        changeGameState(GameState.MENU);
        cleanupGameObjects();
        gameOverScreen.hide();

        initializeGameElements();
        singleplayerScreen = new SingleplayerScreen(root, coinManager);
        singleplayerScreen.updateLives(3);
        singleplayerScreen.updateScore(0);
        singleplayerScreen.updateCoins();

        levelManager.loadLevel(levelManager.currentLevel, root);
        changeGameState(GameState.START);
    }

    private void startNewGame() {
        changeGameState(GameState.MENU);
        cleanupGameObjects();
        gameOverScreen.hide();

        levelManager.currentLevel = 1;
        initializeGameElements();
        singleplayerScreen = new SingleplayerScreen(root, coinManager);
        singleplayerScreen.updateLives(3);
        singleplayerScreen.updateScore(0);
        singleplayerScreen.updateCoins();

        levelManager.loadLevel(1, root);
        changeGameState(GameState.START);
    }

    private void returnToMenu() {
        changeGameState(GameState.MENU);
        cleanupGameObjects();
        showMenuScreen();
    }

    private void cleanupGameObjects() {
        if (paddle != null && paddle.getNode().getParent() != null) {
            root.getChildren().remove(paddle.getNode());
        }

        for (Ball b : new java.util.ArrayList<>(balls)) {
            if (b.getNode().getParent() != null) {
                root.getChildren().remove(b.getNode());
            }
        }
        balls.clear();

        if (indicator != null) {
            if (indicator.getNode().getParent() != null) {
                root.getChildren().remove(indicator.getNode());
            }
            indicator = null;
        }

        for (Brick brick : new java.util.ArrayList<>(levelManager.getBricks())) {
            levelManager.removeBrick(brick, root);
        }

        levelManager.clearAllPowerups(root);

        if (singleplayerScreen != null) {
            singleplayerScreen.cleanup();
            singleplayerScreen = null;
        }

        if (botScreen != null) {
            botScreen.cleanup();
            botScreen = null;
        }
    }


    private void initGameLoop() {
        gameLoop = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate < 1_000_000_000 / 60.0) {
                    return;
                }
                lastUpdate = now;

                if (gameState == GameConfig.GameState.PLAYING) {
                    double timeStep = 1.0 / 240.0;

                    for (int i = 0; i < 4; i++) {
                        processInput(timeStep);
                        updateGame(timeStep);
                        handleCollisions();
                    }
                }
            }
        };
        gameLoop.start();
    }

    private void processInput(double tpf) {
        if (isBotMode && aiManager != null) {
            aiManager.update(balls, levelManager.getPowerups(), tpf);
        } else {
            if (isMovingLeft) {
                paddle.moveLeft(tpf);
            }
            if (isMovingRight) {
                paddle.moveRight(tpf);
            }
        }
    }

    private void updateGame(double tpf) {
        for (Ball b : new java.util.ArrayList<>(balls)) {
            b.update(tpf, paddle, GAME_WIDTH, GAME_HEIGHT);
        }

        if (indicator != null && !balls.isEmpty() && gameState == GameState.START) {
            indicator.pointAtBall(balls.get(0));
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
                if (isBotMode && botScreen != null) {
                    collisionManager.handleBrickBallCollision(b, hitBrick, botScreen);
                } else if (singleplayerScreen != null) {
                    collisionManager.handleBrickBallCollision(b, hitBrick, singleplayerScreen);
                }
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
            if (isBotMode && botScreen != null) {
                botScreen.decreaseLives();
                resetBallAndPaddle();
                if (botScreen.getLives() <= 0) {
                    changeGameState(GameConfig.GameState.GAME_OVER);
                }
            } else if (singleplayerScreen != null) {
                singleplayerScreen.decreaseLives();
                resetBallAndPaddle();
                if (singleplayerScreen.getLives() <= 0) {
                    changeGameState(GameConfig.GameState.GAME_OVER);
                }
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

        if (indicator != null && indicator.getNode().getParent() != null) {
            root.getChildren().remove(indicator.getNode());
        }
        indicator = new Indicator(ball.getX() + ball.getRadius(), ball.getY());
        indicator.pointAtBall(ball);
        root.getChildren().add(indicator.getNode());

        gameState = GameState.START;
        for (Ball b : balls) {
            b.setStuck(true);
        }
    }

    private void changeGameState(GameConfig.GameState newState) {
        this.gameState = newState;

        if (isBotMode && botScreen != null) {
            botScreen.showGameMessage(newState);
        } else if (singleplayerScreen != null) {
            singleplayerScreen.showGameMessage(newState);
        }

        switch (newState) {
            case LEVEL_CLEARED:
                handleLevelCleared();
                break;
            case GAME_OVER:
                handleGameOver();
                break;
            case PLAYING:
                gameLoop.start();
                break;
            default:
                gameLoop.stop();
                break;
        }
    }

    private void handleLevelCleared() {
        levelManager.currentLevel++;
        if (levelManager.currentLevel <= levelManager.maxLevel) {
            resetBallAndPaddle();
            levelManager.loadLevel(levelManager.currentLevel, root);
            if (isBotMode && botScreen != null) {
                botScreen.showLevel(levelManager.currentLevel);
            } else if (singleplayerScreen != null) {
                singleplayerScreen.showLevel(levelManager.currentLevel);
            }
        } else {
            changeGameState(GameConfig.GameState.GAME_OVER);
        }
    }

    private void handleGameOver() {
        if (isBotMode && botScreen != null) {
            botScreen.hideGameMessage();
            int finalScore = botScreen.getScore();
            gameOverScreen.setFinalScore(finalScore);
            System.out.println("AI Final Score: " + finalScore);
        } else if (singleplayerScreen != null) {
            singleplayerScreen.hideGameMessage();
            int finalScore = singleplayerScreen.getScore();
            gameOverScreen.setFinalScore(finalScore);
            System.out.println("Final Score: " + finalScore);
        }

        gameOverScreen.refresh();
        gameOverScreen.show();
        System.out.println("GameOverScreen showed");

        if (!isBotMode) {
            promptAndSaveScore();
        }
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


    private void spawnDebugBall() {
        Ball ball = new Ball(GAME_WIDTH / 2, GAME_HEIGHT - 35, BALL_RADIUS, BALL_SPEED);
        balls.add(ball);
        root.getChildren().add(ball.getNode());
        ball.setStuck(false);
        ball.launch();
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

    private void applySkin(String skinId) {
        // Apply the skin to the paddle if it exists
        if (paddle != null) {
            paddle.applySkin(skinId);
        }
    }
}
