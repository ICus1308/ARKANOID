package gamemanager;

import gameconfig.GameConfig;
import gameobject.*;
import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import userinterface.*;

import java.awt.*;
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
    private SoundManager soundManager;


    private MenuScreen menuScreen;
    private GameModeScreen gameModeScreen;
    private HighScoreScreen highScoreScreen;
    private SettingScreen settingScreen;
    private SingleplayerScreen singleplayerScreen;
    private BotScreen botScreen;
    private OneVOneScreen oneVOneScreen;
    private GameOverScreen gameOverScreen;
    private ShopScreen shopScreen;
    private PauseScreen pauseScreen;

    private Paddle paddle;
    private Paddle paddle2; // Second paddle for 1v1 mode
    private final java.util.List<Ball> balls = new java.util.ArrayList<>();
    private Indicator indicator;
    private AIManager aiManager;
    private boolean isBotMode = false;
    private boolean isOneVOneMode = false;
    private int lastScoredPlayer = 1; // Track who lost the last point (1 = player1, 2 = player2)

    private boolean isMovingLeft2 = false; // For paddle2 in 1v1 mode
    private boolean isMovingRight2 = false; // For paddle2 in 1v1 mode
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
        soundManager = SoundManager.getInstance();
        collisionManager.setCoinManager(coinManager);
        collisionManager.setScoreManager(scoreManager);
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
            this::startOneVOneGame,
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

        pauseScreen = new PauseScreen(
            root,
            this ::resumeGame,
            this ::returnToMenuFromPause
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
            this::applyPaddleSkin,
            this::applyBallSkin,
            this::showMenuScreen
        );
    }

    // Apply handlers called from shop sub-screens
    private void applyPaddleSkin(String skinId) {
        if (skinId == null) return;
        if (coinManager != null) coinManager.setSelectedPaddleSkin(skinId);
        if (paddle != null) paddle.applySkin(skinId);
    }

    private void applyBallSkin(String skinId) {
        if (skinId == null) return;
        if (coinManager != null) coinManager.setSelectedBallSkin(skinId);
        String res = skinIdToBallResource(skinId);
        for (Ball b : balls) {
            b.applySkin(res);
        }
    }

    private void setupScene() {
        scene = new Scene(root);
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case A:
                    if (isOneVOneMode && gameState == GameState.START && indicator != null && lastScoredPlayer == 1) {
                        // Player 1 is serving, can rotate indicator
                        indicator.rotateLeft(0.05);
                    } else if (gameState == GameState.START && indicator != null && !isOneVOneMode) {
                        indicator.rotateLeft(0.05);
                    } else {
                        isMovingLeft = true;
                    }
                    break;
                case D:
                    if (isOneVOneMode && gameState == GameState.START && indicator != null && lastScoredPlayer == 1) {
                        // Player 1 is serving, can rotate indicator
                        indicator.rotateRight(0.05);
                    } else if (gameState == GameState.START && indicator != null && !isOneVOneMode) {
                        indicator.rotateRight(0.05);
                    } else {
                        isMovingRight = true;
                    }
                    break;
                case LEFT:
                    if (isOneVOneMode && gameState == GameState.START && indicator != null && lastScoredPlayer == 2) {
                        // Player 2 is serving, can rotate indicator
                        indicator.rotateLeft(0.05);
                    } else if (isOneVOneMode) {
                        isMovingLeft2 = true;
                    } else if (gameState == GameState.START && indicator != null) {
                        indicator.rotateLeft(0.05);
                    } else {
                        isMovingLeft = true;
                    }
                    break;
                case RIGHT:
                    if (isOneVOneMode && gameState == GameState.START && indicator != null && lastScoredPlayer == 2) {
                        // Player 2 is serving, can rotate indicator
                        indicator.rotateRight(0.05);
                    } else if (isOneVOneMode) {
                        isMovingRight2 = true;
                    } else if (gameState == GameState.START && indicator != null) {
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
                    if (gameState == GameState.PLAYING) {
                        pauseGame();
                    } else if (gameState == GameState.PAUSED) {
                        resumeGame();
                    } else if (gameState == GameState.START || gameState == GameState.GAME_OVER) {
                        returnToMenu();
                    }
                    break;
                case T:
                    levelManager.clearAllBricks(root);
                    if (levelManager.isLevelComplete()) {
                        changeGameState(GameState.LEVEL_CLEARED);
                    }
                    break;
                case Y:
                    levelManager.loadRandomLevel(0.6, root);
                    resetBallAndPaddle();
                    break;
                default:
                    break;
            }
        });

        scene.setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case A:
                    isMovingLeft = false;
                    break;
                case D:
                    isMovingRight = false;
                    break;
                case LEFT:
                    if (isOneVOneMode) {
                        isMovingLeft2 = false;
                    } else {
                        isMovingLeft = false;
                    }
                    break;
                case RIGHT:
                    if (isOneVOneMode) {
                        isMovingRight2 = false;
                    } else {
                        isMovingRight = false;
                    }
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
        soundManager.playMusic(SoundManager.SoundType.MENU_MUSIC, true);
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
        pauseScreen.hide();
    }

    private void refreshAllScreens() {
            highScoreScreen.refresh();
            gameModeScreen.refresh();
            menuScreen.refresh(this::showGameModeScreen, this::showHighScoreScreen, this::showSettingScreen, this::showShopScreen);
            gameOverScreen.refresh();
            shopScreen.refresh();
            pauseScreen.refresh();

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
         // apply selected skins from CoinManager if available
         if (coinManager != null) {
            String pSkin = coinManager.getSelectedPaddleSkin();
            String bSkin = coinManager.getSelectedBallSkin();
            if (pSkin != null) paddle.applySkin(pSkin);
            if (bSkin != null) ball.applySkin(skinIdToBallResource(bSkin));
            // if oneshot active, store pre-oneshot skin then apply oneshot
            if (collisionManager != null && collisionManager.isOneshotActive()) {
                ball.storeSkin();
                ball.applyOneshotSkin();
            }
         }
         balls.add(ball);

        indicator = new Indicator(ballX, ballY);
        indicator.pointAtBall(ball);

        root.getChildren().addAll(paddle.getNode(), ball.getNode(), indicator.getNode());
    }

    private void startSinglePlayerGame() {
        hideAllScreens();
        cleanupGameObjects();
        soundManager.playMusic(SoundManager.SoundType.GAME_MUSIC, true);
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
        soundManager.playMusic(SoundManager.SoundType.GAME_MUSIC, true);
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

    private void startOneVOneGame() {
        hideAllScreens();
        cleanupGameObjects();
        soundManager.playMusic(SoundManager.SoundType.GAME_MUSIC, true);

        isOneVOneMode = true;
        isBotMode = false;
        lastScoredPlayer = 1; // Player 1 starts with the ball

        // Initialize both paddles
        double paddleX = (GAME_WIDTH - PADDLE_WIDTH) / 2;
        paddle = new Paddle(paddleX, GAME_HEIGHT - 20, PADDLE_WIDTH, PADDLE_HEIGHT, PADDLE_SPEED);
        paddle2 = new Paddle(paddleX, 30, PADDLE_WIDTH, PADDLE_HEIGHT, PADDLE_SPEED);

        // Apply different colors to distinguish paddles
        paddle.applySkin("blue");  // Player 1 (bottom)
        paddle2.applySkin("red");  // Player 2 (top)

        // Initialize ball at bottom paddle (Player 1 serves first)
        double ballX = paddle.getX() + paddle.getWidth() / 2;
        double ballY = paddle.getY() - 8;
        Ball ball = new Ball(ballX, ballY, BALL_RADIUS, BALL_SPEED);
        ball.setStuck(true); // Ball is stuck to paddle initially

        if (coinManager != null) {
            String bSkin = coinManager.getSelectedBallSkin();
            if (bSkin != null) ball.applySkin(skinIdToBallResource(bSkin));
        }
        balls.add(ball);

        indicator = new Indicator(ballX, ballY);
        indicator.setTopPaddle(false); // Player 1 serves first from bottom
        indicator.pointAtBall(ball);

        root.getChildren().addAll(paddle.getNode(), paddle2.getNode(), ball.getNode(), indicator.getNode());

        // Initialize 1v1 screen
        oneVOneScreen = new OneVOneScreen(root, coinManager);
        oneVOneScreen.updatePlayer1Lives(3);
        oneVOneScreen.updatePlayer2Lives(3);

        // Load 1v1 level with brick pattern
        levelManager.loadOneVOneLevel(root);
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

    private void pauseGame() {
        gameState = GameState.PAUSED;
        gameLoop.stop();
        pauseScreen.show();
    }

    private void resumeGame() {
        pauseScreen.hide();
        gameState = GameState.PLAYING;
        gameLoop.start();
    }

    private void returnToMenuFromPause() {
        pauseScreen.hide();
        returnToMenu();
    }


    private void cleanupGameObjects() {
        if (paddle != null && paddle.getNode().getParent() != null) {
            root.getChildren().remove(paddle.getNode());
        }

        if (paddle2 != null && paddle2.getNode().getParent() != null) {
            root.getChildren().remove(paddle2.getNode());
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

        if (oneVOneScreen != null) {
            oneVOneScreen.cleanup();
            oneVOneScreen = null;
        }

        // Reset mode flags
        isOneVOneMode = false;
        isBotMode = false;
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

                if (gameState == GameConfig.GameState.PLAYING || gameState == GameConfig.GameState.START) {
                    double timeStep = 1.0 / 240.0;

                    for (int i = 0; i < 4; i++) {
                        processInput(timeStep);
                        updateGame(timeStep);
                        if (gameState == GameConfig.GameState.PLAYING) {
                            handleCollisions();
                        }
                    }
                }
            }
        };
        gameLoop.start();
    }

    private void processInput(double tpf) {
        if (isBotMode && aiManager != null) {
            aiManager.update(balls, levelManager.getPowerups(), tpf);
        } else if (isOneVOneMode) {
            // In 1v1 mode, lock paddles until ball is served (START state)
            if (gameState == GameState.START) {
                // Paddles are locked - only the serving player can aim with indicator
                // No paddle movement allowed
            } else {
                // Game is playing - control both paddles
                // Player 1 (bottom paddle): A/D keys
                if (isMovingLeft) {
                    paddle.moveLeft(tpf);
                }
                if (isMovingRight) {
                    paddle.moveRight(tpf);
                }
                // Player 2 (top paddle): Arrow keys
                if (isMovingLeft2) {
                    paddle2.moveLeft(tpf);
                }
                if (isMovingRight2) {
                    paddle2.moveRight(tpf);
                }
            }
        } else {
            // Don't move paddle in START state when indicator is active
            if (gameState != GameState.START || indicator == null) {
                if (isMovingLeft) {
                    paddle.moveLeft(tpf);
                }
                if (isMovingRight) {
                    paddle.moveRight(tpf);
                }
            }
        }
    }

    private void updateGame(double tpf) {
        for (Ball b : new java.util.ArrayList<>(balls)) {
            // In 1v1 mode, ball should follow the paddle of whoever is serving
            if (isOneVOneMode && gameState == GameState.START) {
                if (lastScoredPlayer == 2) {
                    b.update(tpf, paddle2, true); // Player 2 is serving from top - ball below paddle
                } else {
                    b.update(tpf, paddle, false); // Player 1 is serving from bottom - ball above paddle
                }
            } else {
                b.update(tpf, paddle, false); // Normal mode - always follow bottom paddle with ball above
            }
        }

        if (indicator != null && !balls.isEmpty() && gameState == GameState.START) {
            Ball firstBall = balls.get(0);
            indicator.updatePosition(firstBall.getX() + firstBall.getRadius(), firstBall.getY());
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

            // In 1v1 mode, handle top and bottom hits differently
            if (isOneVOneMode) {
                if (wallHit == GameConfig.WallSideType.BOTTOM_HIT) {
                    // Player 1 (bottom) loses a life
                    toRemove.add(b);
                    lastScoredPlayer = 1; // Player 1 will serve next
                    continue;
                } else if (wallHit == GameConfig.WallSideType.NORTH) {
                    // Player 2 (top) loses a life - need to check if ball is actually past the paddle
                    if (b.getY() < paddle2.getY() + paddle2.getHeight()) {
                        toRemove.add(b);
                        lastScoredPlayer = 2; // Player 2 will serve next
                        continue;
                    }
                }
            } else {
                // Normal mode - only bottom hit causes ball loss
                if (wallHit == GameConfig.WallSideType.BOTTOM_HIT) {
                    toRemove.add(b);
                    continue;
                }
            }

            // Check paddle collisions
            if (collisionManager.checkPaddleBallCollision(paddle, b)) {
                collisionManager.handlePaddleBallCollision(paddle, b);
            }

            // In 1v1 mode, also check second paddle
            if (isOneVOneMode && paddle2 != null) {
                if (collisionManager.checkPaddleBallCollision(paddle2, b)) {
                    collisionManager.handlePaddleBallCollision(paddle2, b);
                }
            }

            // Check brick collisions
            java.util.List<Brick> bricks = levelManager.getBricks();
            Brick hitBrick = collisionManager.checkBrickBallCollision(b, bricks);
            if (hitBrick != null) {
                if (isOneVOneMode && oneVOneScreen != null) {
                    // In 1v1 mode, award points based on which half of the screen the brick is in
                    if (hitBrick.getY() < GAME_HEIGHT / 2) {
                        // Top half - Player 1 scores
                        collisionManager.handleBrickBallCollision(b, hitBrick, oneVOneScreen, 1);
                    } else {
                        // Bottom half - Player 2 scores
                        collisionManager.handleBrickBallCollision(b, hitBrick, oneVOneScreen, 2);
                    }
                } else if (isBotMode && botScreen != null) {
                    collisionManager.handleBrickBallCollision(b, hitBrick, botScreen);
                } else if (singleplayerScreen != null) {
                    collisionManager.handleBrickBallCollision(b, hitBrick, singleplayerScreen);
                }
            }
        }

        // Remove dead balls
        for (Ball dead : toRemove) {
            if (dead.getNode() != null && dead.getNode().getParent() != null) {
                root.getChildren().remove(dead.getNode());
            }
            balls.remove(dead);
        }

        // Handle life loss
        if (!toRemove.isEmpty() && balls.isEmpty()) {
            if (isOneVOneMode && oneVOneScreen != null) {
                if (lastScoredPlayer == 1) {
                    // Player 1 lost the ball
                    oneVOneScreen.decreasePlayer1Lives();
                    if (oneVOneScreen.getPlayer1Lives() <= 0) {
                        changeGameState(GameConfig.GameState.GAME_OVER);
                        return;
                    }
                } else {
                    // Player 2 lost the ball
                    oneVOneScreen.decreasePlayer2Lives();
                    if (oneVOneScreen.getPlayer2Lives() <= 0) {
                        changeGameState(GameConfig.GameState.GAME_OVER);
                        return;
                    }
                }
                resetBallAndPaddleOneVOne();
            } else if (isBotMode && botScreen != null) {
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

        // Handle powerups (not in 1v1 mode)
        if (!isOneVOneMode) {
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
    }

    private void resetBallAndPaddle() {
        for (Ball b : balls) {
            root.getChildren().remove(b.getNode());
        }
        balls.clear();

        levelManager.clearAllPowerups(root);

        paddle.reset();

        Ball ball = new Ball(paddle.getX() + paddle.getWidth() / 2, paddle.getY() - 8, BALL_RADIUS, BALL_SPEED);
        // apply selected skins
        if (coinManager != null) {
            String pSkin = coinManager.getSelectedPaddleSkin();
            String bSkin = coinManager.getSelectedBallSkin();
            if (pSkin != null) paddle.applySkin(pSkin);
            if (bSkin != null) {
                ball.applySkin(skinIdToBallResource(bSkin));
                if (collisionManager != null && collisionManager.isOneshotActive()) {
                    ball.storeSkin();
                    ball.applyOneshotSkin();
                }
            }
        }
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

    private void resetBallAndPaddleOneVOne() {
        for (Ball b : balls) {
            root.getChildren().remove(b.getNode());
        }
        balls.clear();

        // Reset both paddles to center
        paddle.reset();
        paddle2.reset();

        // Reapply colors
        paddle.applySkin("blue");
        paddle2.applySkin("red");

        // Ball spawns at the paddle of the player who lost the point (who will serve)
        Ball ball;
        if (lastScoredPlayer == 1) {
            // Player 1 lost point, so Player 1 serves from bottom - ball above paddle
            ball = new Ball(paddle.getX() + paddle.getWidth() / 2, paddle.getY() - BALL_RADIUS * 2 - 2, BALL_RADIUS, BALL_SPEED);
        } else {
            // Player 2 lost point, so Player 2 serves from top - ball below paddle (toward center)
            ball = new Ball(paddle2.getX() + paddle2.getWidth() / 2, paddle2.getY() + paddle2.getHeight() + 2, BALL_RADIUS, BALL_SPEED);
        }

        ball.setStuck(true);

        if (coinManager != null) {
            String bSkin = coinManager.getSelectedBallSkin();
            if (bSkin != null) {
                ball.applySkin(skinIdToBallResource(bSkin));
            }
        }
        balls.add(ball);
        root.getChildren().add(ball.getNode());

        if (indicator != null && indicator.getNode().getParent() != null) {
            root.getChildren().remove(indicator.getNode());
        }
        indicator = new Indicator(ball.getX() + ball.getRadius(), ball.getY());
        indicator.setTopPaddle(lastScoredPlayer == 2); // Set to true if Player 2 is serving
        indicator.pointAtBall(ball);
        root.getChildren().add(indicator.getNode());

        changeGameState(GameState.START);
    }

    private void changeGameState(GameConfig.GameState newState) {
        this.gameState = newState;

        if (isOneVOneMode && oneVOneScreen != null) {
            oneVOneScreen.showGameMessage(newState);
        } else if (isBotMode && botScreen != null) {
            botScreen.showGameMessage(newState);
        } else if (singleplayerScreen != null) {
            singleplayerScreen.showGameMessage(newState);
        }

        switch (newState) {
            case LEVEL_CLEARED:
                soundManager.playSound(SoundManager.SoundType.LEVEL_COMPLETE);
                handleLevelCleared();
                break;
            case GAME_OVER:
                soundManager.playSound(SoundManager.SoundType.GAME_OVER);
                handleGameOver();
                break;
            case PLAYING:
            case START:
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
        if (isOneVOneMode && oneVOneScreen != null) {
            // 1v1 mode - show winner message
            oneVOneScreen.hideGameMessage();
            // Don't set score for game over screen - it's not relevant for 1v1
            gameOverScreen.refresh();
            gameOverScreen.show();
            System.out.println("1v1 Game Over");
        } else if (isBotMode && botScreen != null) {
            botScreen.hideGameMessage();
            int finalScore = botScreen.getScore();
            gameOverScreen.setFinalScore(finalScore);
            System.out.println("AI Final Score: " + finalScore);
            gameOverScreen.refresh();
            gameOverScreen.show();
        } else if (singleplayerScreen != null) {
            singleplayerScreen.hideGameMessage();
            int finalScore = singleplayerScreen.getScore();
            gameOverScreen.setFinalScore(finalScore);
            System.out.println("Final Score: " + finalScore);
            gameOverScreen.refresh();
            gameOverScreen.show();
        }

        System.out.println("GameOverScreen showed");

        if (!isBotMode && !isOneVOneMode) {
            promptAndSaveScore();
        }
    }

    // Map logical skin id (selected in shop/coin manager) to a ball image resource path.
    private String skinIdToBallResource(String skinId) {
        if (skinId == null) return "/imageball/default.png";
        return switch (skinId) {
            case "skin1" -> "/imageball/skin1.png";
            case "skin2" -> "/imageball/skin2.png";
            case "oneshot" -> "/imageball/oneshot.png";
            case "default" -> "/imageball/default.png";
            default -> "/imageball/default.png";
        };
    }

    public void spawnExtraBall() {
        if (balls.isEmpty() || balls.size() > 300) return;

        int size = balls.size();
        for (int i = 0; i < size; i++) {
            Ball ref = balls.get(i);
            Ball newBall = new Ball(ref.getX() + ref.getRadius(), ref.getY() + ref.getRadius(), ref.getRadius(), ref.speed);
            newBall.setVx(-ref.getVx());
            newBall.setVy(ref.getVy());
            newBall.setStuck(false);

            if (collisionManager != null && collisionManager.isOneshotActive()) {
                // if oneshot is active, set pre-oneshot skin from selectedBallSkin and apply oneshot
                String sel = coinManager == null ? null : coinManager.getSelectedBallSkin();
                newBall.applySkin(skinIdToBallResource(sel));
                newBall.storeSkin();
                newBall.applyOneshotSkin();
            } else {
                // inherit skin from reference ball
                newBall.applySkin(ref.getCurrentSkinResource());
            }

            balls.add(newBall);
            root.getChildren().add(newBall.getNode());
        }
    }

    public void enableOneshot() {
        collisionManager.setOneshotActive(true);
        // store current skin and apply oneshot skin to all existing balls
        for (Ball b : balls) {
            b.storeSkin();
            b.applyOneshotSkin();
        }

        if (oneshotTimer != null) {
            oneshotTimer.stop();
        }
        oneshotTimer = new PauseTransition(Duration.seconds(7.5));
        oneshotTimer.setOnFinished(event -> {
            collisionManager.setOneshotActive(false);
            // restore each ball's stored skin (or default if none)
            for (Ball b : balls) {
                b.restoreSkin();
            }
            // also ensure paddle shows selected paddle skin
            if (coinManager != null && paddle != null) {
                String sel = coinManager.getSelectedPaddleSkin();
                paddle.applySkin(sel);
            }
        });
        oneshotTimer.playFromStart();
    }

    private void spawnDebugBall() {
        Ball ball = new Ball(GAME_WIDTH / 2, GAME_HEIGHT - 35, BALL_RADIUS, BALL_SPEED);
        // inherit skin from existing first ball if there is one; otherwise use selected ball skin
        if (!balls.isEmpty()) {
            ball.applySkin(balls.get(0).getCurrentSkinResource());
        } else if (coinManager != null) {
            String sel = coinManager.getSelectedBallSkin();
            ball.applySkin(skinIdToBallResource(sel));
            if (collisionManager != null && collisionManager.isOneshotActive()) {
                ball.storeSkin();
                ball.applyOneshotSkin();
            }
            if (paddle != null) paddle.applySkin(coinManager.getSelectedPaddleSkin());
        }
        balls.add(ball);
        root.getChildren().add(ball.getNode());
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
        if (skinId == null) return;
        // Apply to paddle
        if (paddle != null) paddle.applySkin(skinId);
        // Also apply to balls if this is a ball skin id
        String res = skinIdToBallResource(skinId);
        for (Ball b : balls) {
            b.applySkin(res);
        }
    }

}
