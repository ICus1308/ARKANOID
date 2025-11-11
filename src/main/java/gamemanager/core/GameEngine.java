package gamemanager.core;

import gameconfig.GameConfig;
import gamemanager.manager.CoinManager;
import gamemanager.manager.CollisionManager;
import gamemanager.manager.ScoreManager;
import gamemanager.manager.SoundManager;
import gameobject.ball.Ball;
import gameobject.brick.Brick;
import gameobject.paddle.Indicator;
import gameobject.paddle.Paddle;
import gameobject.powerup.Powerup;
import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import userinterface.gamescreen.*;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;


import java.util.ArrayList;
import java.util.List;

import static gameconfig.GameConfig.*;

public class GameEngine {
    private javafx.scene.shape.Rectangle transitionOverlay;

    private final Pane root;
    private GameConfig.GameState gameState = GameConfig.GameState.MENU;
    private GameConfig.GameState previousGameState = GameConfig.GameState.MENU;

    private final LevelManager levelManager;
    private final CollisionManager collisionManager;
    private final ScoreManager scoreManager;
    private final CoinManager coinManager;
    private final SoundManager soundManager;

    private Paddle paddle;
    private Paddle paddle2;
    private final List<Ball> balls = new ArrayList<>();
    private Indicator indicator;
    private AIManager aiManager;

    private boolean isBotMode = false;
    private boolean isOneVOneMode = false;
    private int lastScoredPlayer = 1;

    private boolean isMovingLeft = false;
    private boolean isMovingRight = false;
    private boolean isMovingLeft2 = false;
    private boolean isMovingRight2 = false;

    private SingleplayerScreen singleplayerScreen;
    private BotScreen botScreen;
    private OneVOneScreen oneVOneScreen;
    private EndlessScreen endlessScreen;

    private PauseTransition oneshotTimer;
    private AnimationTimer gameLoop;

    private Runnable onGameOver;

    private final List<Ball> ballsToRemove = new ArrayList<>();
    private final List<Brick> cachedBricks = new ArrayList<>();
    private final List<Powerup> cachedPowerups = new ArrayList<>();
    private static final double FIXED_TIME_STEP = 1.0 / 240.0;
    private static final long FRAME_TIME_NANOS = 16_666_667L; // ~60 FPS

    public GameEngine(Pane root) {
        this.root = root;
        this.levelManager = new LevelManager();
        this.collisionManager = new CollisionManager(levelManager, root);
        this.scoreManager = new ScoreManager();
        this.coinManager = new CoinManager();
        this.soundManager = SoundManager.getInstance();

        collisionManager.setCoinManager(coinManager);
        collisionManager.setScoreManager(scoreManager);
    }

    private void createTransitionOverlay() {
        if (transitionOverlay == null) {
            transitionOverlay = new javafx.scene.shape.Rectangle(GAME_WIDTH, GAME_HEIGHT);
            transitionOverlay.setFill(javafx.scene.paint.Color.BLACK);
            transitionOverlay.setOpacity(0);
            transitionOverlay.setMouseTransparent(true);
        }
    }

    public void initGameLoop() {
        gameLoop = new AnimationTimer() {
            private long lastUpdate = 0;
            // ========== THÊM: Frame skip for optimization ==========
            private int frameCount = 0;
            private static final int UPDATE_INTERVAL = 1; // Update every frame (can increase to 2 for more performance)

            @Override
            public void handle(long now) {
                // Frame rate limiting
                if (now - lastUpdate < FRAME_TIME_NANOS) {
                    return;
                }

                long elapsed = now - lastUpdate;
                lastUpdate = now;

                // Only update when playing or starting
                if (gameState == GameConfig.GameState.PLAYING ||
                        gameState == GameConfig.GameState.START) {

                    frameCount++;

                    // ========== OPTIMIZATION: Skip some collision checks on alternate frames ==========
                    boolean doFullUpdate = (frameCount % UPDATE_INTERVAL == 0);

                    // Always do input and game update
                    for (int i = 0; i < 2; i++) {
                        processInput(FIXED_TIME_STEP);
                        updateGame(FIXED_TIME_STEP);
                    }

                    // Collision detection - can be slightly delayed
                    if (gameState == GameConfig.GameState.PLAYING && doFullUpdate) {
                        handleCollisions();
                    }
                }
            }
        };
        gameLoop.start();
    }

    // ========== THÊM METHOD: Pause game loop during heavy operations ==========
    public void pauseGameLoopTemporarily() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }

    public void resumeGameLoop() {
        if (gameLoop != null) {
            gameLoop.start();
        }
    }


    public void initializeGameElements() {
        double paddleX = (GAME_WIDTH - PADDLE_WIDTH) / 2;
        double paddleY = GAME_HEIGHT - 50;
        paddle = new Paddle(paddleX, paddleY, PADDLE_WIDTH, PADDLE_HEIGHT, PADDLE_SPEED);
        paddle.updateDebugWidth(DEBUG_PADDLE_LENGTH_MULTIPLIER);

        double ballX = GAME_WIDTH / 2;
        double ballY = GAME_HEIGHT - 35;
        Ball ball = new Ball(ballX, ballY, BALL_RADIUS, BALL_SPEED);

        if (coinManager != null) {
            String pSkin = coinManager.getSelectedPaddleSkin();
            String bSkin = coinManager.getSelectedBallSkin();
            if (pSkin != null) paddle.applySkin(pSkin);
            if (bSkin != null) ball.applySkin(skinIdToBallResource(bSkin));
            if (collisionManager != null && collisionManager.isOneshotActive()) {
                ball.storeSkin();
                ball.applyOneshotSkin();
            }
        }
        balls.add(ball);

        indicator = new Indicator(ballX, ballY);
        indicator.pointAtBall(ball);
        indicator.setRotation(-90);

        root.getChildren().addAll(paddle.getNode(), ball.getTrailGroup(),
                ball.getNode(), indicator.getNode());
    }

    public void startSinglePlayerGame() {
        cleanupGameObjects();
        soundManager.playMusic(SoundManager.SoundType.GAME_MUSIC, true);
        initializeGameElements();
        isBotMode = false;
        isOneVOneMode = false;

        singleplayerScreen = new SingleplayerScreen(root, coinManager);
        singleplayerScreen.updateLives(3);
        singleplayerScreen.updateScore(0);
        singleplayerScreen.updateCoins();

        levelManager.loadLevel(1, root);
        changeGameState(GameState.START);
    }

    public void startBotGame() {
        cleanupGameObjects();
        soundManager.playMusic(SoundManager.SoundType.GAME_MUSIC, true);

        isBotMode = true;
        isOneVOneMode = false;
        lastScoredPlayer = 1;

        double paddleX = (GAME_WIDTH - PADDLE_WIDTH) / 2;
        paddle = new Paddle(paddleX, GAME_HEIGHT - 50, PADDLE_WIDTH, PADDLE_HEIGHT, PADDLE_SPEED);
        paddle2 = new Paddle(paddleX, 30, PADDLE_WIDTH, PADDLE_HEIGHT, PADDLE_SPEED);
        paddle.updateDebugWidth(DEBUG_PADDLE_LENGTH_MULTIPLIER);
        paddle2.updateDebugWidth(DEBUG_PADDLE_LENGTH_MULTIPLIER);
        paddle.applySkin("blue");
        paddle2.applySkin("red");

        aiManager = new AIManager(paddle2);

        double ballX = paddle.getX() + paddle.getWidth() / 2;
        double ballY = paddle.getY() - 8;
        Ball ball = new Ball(ballX, ballY, BALL_RADIUS, BALL_SPEED);
        ball.setStuck(true);

        if (coinManager != null) {
            String bSkin = coinManager.getSelectedBallSkin();
            if (bSkin != null) ball.applySkin(skinIdToBallResource(bSkin));
        }
        balls.add(ball);

        indicator = new Indicator(ballX, ballY);
        indicator.setTopPaddle(false);
        indicator.pointAtBall(ball);

        root.getChildren().addAll(paddle.getNode(), paddle2.getNode(),
                ball.getTrailGroup(), ball.getNode(), indicator.getNode());

        botScreen = new BotScreen(root);
        botScreen.updatePlayerLives(3);
        botScreen.updateBotLives(3);

        levelManager.loadOneVOneLevel(root);
        changeGameState(GameState.START);
    }

    public void startOneVOneGame() {
        cleanupGameObjects();
        soundManager.playMusic(SoundManager.SoundType.GAME_MUSIC, true);

        isOneVOneMode = true;
        isBotMode = false;
        lastScoredPlayer = 1;

        double paddleX = (GAME_WIDTH - PADDLE_WIDTH) / 2;
        paddle = new Paddle(paddleX, GAME_HEIGHT - 50, PADDLE_WIDTH, PADDLE_HEIGHT, PADDLE_SPEED);
        paddle2 = new Paddle(paddleX, 30, PADDLE_WIDTH, PADDLE_HEIGHT, PADDLE_SPEED);
        paddle.updateDebugWidth(DEBUG_PADDLE_LENGTH_MULTIPLIER);
        paddle2.updateDebugWidth(DEBUG_PADDLE_LENGTH_MULTIPLIER);
        paddle.applySkin("blue");
        paddle2.applySkin("red");

        double ballX = paddle.getX() + paddle.getWidth() / 2;
        double ballY = paddle.getY() - 8;
        Ball ball = new Ball(ballX, ballY, BALL_RADIUS, BALL_SPEED);
        ball.setStuck(true);

        if (coinManager != null) {
            String bSkin = coinManager.getSelectedBallSkin();
            if (bSkin != null) ball.applySkin(skinIdToBallResource(bSkin));
        }
        balls.add(ball);

        indicator = new Indicator(ballX, ballY);
        indicator.setTopPaddle(false);
        indicator.pointAtBall(ball);

        root.getChildren().addAll(paddle.getNode(), paddle2.getNode(),
                ball.getTrailGroup(), ball.getNode(), indicator.getNode());

        oneVOneScreen = new OneVOneScreen(root);
        oneVOneScreen.updatePlayer1Lives(3);
        oneVOneScreen.updatePlayer2Lives(3);

        levelManager.loadOneVOneLevel(root);
        changeGameState(GameState.START);
    }

    public void startEndlessGame() {
        cleanupGameObjects();
        soundManager.playMusic(SoundManager.SoundType.GAME_MUSIC, true);
        initializeGameElements();
        isBotMode = false;
        isOneVOneMode = false;

        endlessScreen = new EndlessScreen(root, coinManager);
        endlessScreen.updateLives(3);
        endlessScreen.updateScore(0);
        endlessScreen.updateCoins();
        endlessScreen.showLevel(1);

        levelManager.generateEndlessLevel(root);
        changeGameState(GameState.START);
    }

    public void startGame() {
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

    public void pauseGame() {
        previousGameState = gameState;
        gameState = GameState.PAUSED;
        gameLoop.stop();
    }

    public void resumeGame() {
        gameState = previousGameState;
        gameLoop.start();
    }

    public void retryLevel() {
        changeGameState(GameState.MENU);
        cleanupGameObjects();

        initializeGameElements();
        singleplayerScreen = new SingleplayerScreen(root, coinManager);
        singleplayerScreen.updateLives(3);
        singleplayerScreen.updateScore(0);
        singleplayerScreen.updateCoins();

        levelManager.loadLevel(levelManager.currentLevel, root);
        changeGameState(GameState.START);
    }

    public void startNewGame() {
        changeGameState(GameState.MENU);
        cleanupGameObjects();

        levelManager.currentLevel = 1;
        initializeGameElements();
        singleplayerScreen = new SingleplayerScreen(root, coinManager);
        singleplayerScreen.updateLives(3);
        singleplayerScreen.updateScore(0);
        singleplayerScreen.updateCoins();

        levelManager.loadLevel(1, root);
        changeGameState(GameState.START);
    }

    private void processInput(double tpf) {
        if (isBotMode && aiManager != null) {
            if (gameState == GameState.START) {
                return;
            }
            aiManager.update(balls, levelManager.getPowerups(), tpf);
            if (isMovingLeft) paddle.moveLeft(tpf);
            if (isMovingRight) paddle.moveRight(tpf);
        } else if (isOneVOneMode) {
            if (gameState == GameState.START) {
                return;
            }
            if (isMovingLeft) paddle.moveLeft(tpf);
            if (isMovingRight) paddle.moveRight(tpf);
            if (isMovingLeft2) paddle2.moveLeft(tpf);
            if (isMovingRight2) paddle2.moveRight(tpf);
        } else {
            if (gameState != GameState.START || indicator == null) {
                if (isMovingLeft) paddle.moveLeft(tpf);
                if (isMovingRight) paddle.moveRight(tpf);
            }
        }
    }

    private void updateGame(double tpf) {
        int ballCount = balls.size();
        for (int i = 0; i < ballCount; i++) {
            Ball b = balls.get(i);
            if ((isOneVOneMode || isBotMode) && gameState == GameState.START) {
                b.update(tpf, lastScoredPlayer == 2 ? paddle2 : paddle, lastScoredPlayer == 2);
            } else {
                b.update(tpf, paddle, false);
            }
        }

        if (indicator != null && ballCount > 0 && gameState == GameState.START) {
            Ball firstBall = balls.get(0);
            indicator.updatePosition(firstBall.getX() + firstBall.getRadius(), firstBall.getY());
        }

        cachedPowerups.clear();
        cachedPowerups.addAll(levelManager.getPowerups());
        for (Powerup p : cachedPowerups) {
            p.update();
        }
    }

    private void handleCollisions() {
        if (gameState != GameConfig.GameState.PLAYING) return;

        List<Ball> toRemove = new ArrayList<>();

        for (Ball b : new ArrayList<>(balls)) {
            GameConfig.WallSideType wallHit = collisionManager.checkWallCollision(b, GAME_WIDTH, GAME_HEIGHT);

            if (isOneVOneMode || isBotMode) {
                if (wallHit == GameConfig.WallSideType.BOTTOM_HIT) {
                    toRemove.add(b);
                    lastScoredPlayer = 1;
                    continue;
                } else if (wallHit == GameConfig.WallSideType.NORTH) {
                    if (b.getY() < paddle2.getY() + paddle2.getHeight()) {
                        toRemove.add(b);
                        lastScoredPlayer = 2;
                        continue;
                    }
                }
            } else {
                if (wallHit == GameConfig.WallSideType.BOTTOM_HIT) {
                    toRemove.add(b);
                    continue;
                }
            }

            if (collisionManager.checkPaddleBallCollision(paddle, b)) {
                collisionManager.handlePaddleBallCollision(paddle, b);
            }

            if ((isOneVOneMode || isBotMode) && paddle2 != null) {
                if (collisionManager.checkPaddleBallCollision(paddle2, b)) {
                    collisionManager.handlePaddleBallCollision(paddle2, b);
                }
            }

            List<Brick> bricks = levelManager.getBricks();
            Brick hitBrick = collisionManager.checkBrickBallCollision(b, bricks);
            if (hitBrick != null) {
                if (isOneVOneMode && oneVOneScreen != null) {
                    int player = hitBrick.getY() < GAME_HEIGHT / 2 ? 1 : 2;
                    collisionManager.handleBrickBallCollision(b, hitBrick, oneVOneScreen, player);
                } else if (isBotMode && botScreen != null) {
                    collisionManager.handleBrickBallCollision(b, hitBrick, botScreen, 0);
                } else if (endlessScreen != null) {
                    collisionManager.handleBrickBallCollision(b, hitBrick, endlessScreen);
                } else if (singleplayerScreen != null) {
                    collisionManager.handleBrickBallCollision(b, hitBrick, singleplayerScreen);
                }

                if (!isOneVOneMode && !isBotMode && levelManager.isLevelComplete()) {
                    if (endlessScreen != null) {
                        levelManager.generateEndlessLevel(root);
                        resetBallAndPaddle();
                        int currentEndlessLevel = (endlessScreen.getScore() / 1000) + 1;
                        endlessScreen.showLevel(currentEndlessLevel);
                    } else {
                        changeGameState(GameState.LEVEL_CLEARED);
                    }
                    return;
                }
            }
        }

        // Remove dead balls
        for (Ball dead : toRemove) {
            // Hiệu ứng mờ dần cho trail
            if (dead.getTrailGroup() != null) {
                FadeTransition trailFade = new FadeTransition(Duration.millis(400), dead.getTrailGroup());
                trailFade.setFromValue(1.0);
                trailFade.setToValue(0.0);
                trailFade.setOnFinished(e -> {
                    root.getChildren().remove(dead.getTrailGroup());
                });
                trailFade.play();
            }

            // Hiệu ứng mờ dần cho bóng
            if (dead.getNode() != null) {
                FadeTransition ballFade = new FadeTransition(Duration.millis(300), dead.getNode());
                ballFade.setFromValue(1.0);
                ballFade.setToValue(0.0);
                ballFade.setOnFinished(e -> {
                    root.getChildren().remove(dead.getNode());
                    balls.remove(dead);
                });
                ballFade.play();
            } else {
                balls.remove(dead);
            }

            if (dead.getNode() != null && dead.getNode().getParent() != null) {
                root.getChildren().remove(dead.getNode());
            }
            balls.remove(dead);
        }

        if (!toRemove.isEmpty() && balls.isEmpty()) {
            if (isOneVOneMode && oneVOneScreen != null) {
                if (lastScoredPlayer == 1) {
                    oneVOneScreen.decreasePlayer1Lives();
                    if (oneVOneScreen.getPlayer1Lives() <= 0) {
                        changeGameState(GameConfig.GameState.GAME_OVER);
                        return;
                    }
                } else {
                    oneVOneScreen.decreasePlayer2Lives();
                    if (oneVOneScreen.getPlayer2Lives() <= 0) {
                        changeGameState(GameConfig.GameState.GAME_OVER);
                        return;
                    }
                }
                resetBallAndPaddleOneVOne();
            } else if (isBotMode && botScreen != null) {
                if (lastScoredPlayer == 1) {
                    botScreen.decreasePlayerLives();
                    if (botScreen.getPlayerLives() <= 0) {
                        changeGameState(GameConfig.GameState.GAME_OVER);
                        return;
                    }
                } else {
                    botScreen.decreaseBotLives();
                    if (botScreen.getBotLives() <= 0) {
                        changeGameState(GameConfig.GameState.GAME_OVER);
                        return;
                    }
                }
                resetBallAndPaddleBot();
            } else if (endlessScreen != null) {
                endlessScreen.decreaseLives();
                resetBallAndPaddle();
                if (endlessScreen.getLives() <= 0) {
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

        if (!isOneVOneMode && !isBotMode) {
            for (Powerup p : new ArrayList<>(levelManager.getPowerups())) {
                if (collisionManager.checkPaddlePowerupCollision(paddle, p)) {
                    p.activate(this, paddle);
                    if (p.getNode() != null && p.getNode().getParent() != null) {
                        root.getChildren().remove(p.getNode());
                    }
                    levelManager.removePowerup(p, root);
                }
            }
        }
    }


    private void resetBallAndPaddle() {
        // ========== THÊM: Batch removal ==========
        List<javafx.scene.Node> nodesToRemove = new ArrayList<>();

        // Collect all ball-related nodes
        for (Ball b : balls) {
            if (b.getTrailGroup() != null && b.getTrailGroup().getParent() != null) {
                nodesToRemove.add(b.getTrailGroup());
            }
            if (b.getNode() != null && b.getNode().getParent() != null) {
                nodesToRemove.add(b.getNode());
            }
        }
        balls.clear();

        // Collect powerup nodes
        for (Powerup p : levelManager.getPowerups()) {
            if (p.getNode() != null && p.getNode().getParent() != null) {
                nodesToRemove.add(p.getNode());
            }
        }
        levelManager.getPowerups().clear();

        // Collect indicator node
        if (indicator != null && indicator.getNode().getParent() != null) {
            nodesToRemove.add(indicator.getNode());
        }

        // ========== BATCH REMOVE: Much faster than individual removes ==========
        root.getChildren().removeAll(nodesToRemove);

        // Reset paddle
        paddle.reset();

        // Create new ball
        Ball ball = new Ball(paddle.getX() + paddle.getWidth() / 2, paddle.getY() - 8, BALL_RADIUS, BALL_SPEED);

        // Apply skins
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

        // Create new indicator
        indicator = new Indicator(ball.getX() + ball.getRadius(), ball.getY());
        indicator.pointAtBall(ball);
        indicator.setRotation(-90);

        // ========== BATCH ADD: Add all new nodes at once ==========
        root.getChildren().addAll(ball.getTrailGroup(), ball.getNode(), indicator.getNode());

        // Update game state
        gameState = GameState.START;
        for (Ball b : balls) {
            b.setStuck(true);
        }
    }

    private void resetBallAndPaddleOneVOne() {
        List<javafx.scene.Node> nodesToRemove = new ArrayList<>();

        // Collect all ball-related nodes
        for (Ball b : balls) {
            if (b.getTrailGroup() != null && b.getTrailGroup().getParent() != null) {
                nodesToRemove.add(b.getTrailGroup());
            }
            if (b.getNode() != null && b.getNode().getParent() != null) {
                nodesToRemove.add(b.getNode());
            }
        }
        balls.clear();

        // Collect powerup nodes
        for (Powerup p : levelManager.getPowerups()) {
            if (p.getNode() != null && p.getNode().getParent() != null) {
                nodesToRemove.add(p.getNode());
            }
        }
        levelManager.getPowerups().clear();

        // Collect indicator node
        if (indicator != null && indicator.getNode().getParent() != null) {
            nodesToRemove.add(indicator.getNode());
        }

        // ========== BATCH REMOVE: Much faster than individual removes ==========
        root.getChildren().removeAll(nodesToRemove);

        // Reset paddle
        paddle.reset();

        // Create new ball
        Ball ball = new Ball(paddle.getX() + paddle.getWidth() / 2, paddle.getY() - 8, BALL_RADIUS, BALL_SPEED);

        // Apply skins
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

        // Create new indicator
        indicator = new Indicator(ball.getX() + ball.getRadius(), ball.getY());
        indicator.pointAtBall(ball);
        indicator.setRotation(-90);

        // ========== BATCH ADD: Add all new nodes at once ==========
        root.getChildren().addAll(ball.getTrailGroup(), ball.getNode(), indicator.getNode());

        // Update game state
        gameState = GameState.START;
        for (Ball b : balls) {
            b.setStuck(true);
        }
    }

    private void resetBallAndPaddleBot() {
        List<javafx.scene.Node> nodesToRemove = new ArrayList<>();

        // Collect all ball-related nodes
        for (Ball b : balls) {
            if (b.getTrailGroup() != null && b.getTrailGroup().getParent() != null) {
                nodesToRemove.add(b.getTrailGroup());
            }
            if (b.getNode() != null && b.getNode().getParent() != null) {
                nodesToRemove.add(b.getNode());
            }
        }
        balls.clear();

        // Collect powerup nodes
        for (Powerup p : levelManager.getPowerups()) {
            if (p.getNode() != null && p.getNode().getParent() != null) {
                nodesToRemove.add(p.getNode());
            }
        }
        levelManager.getPowerups().clear();

        // Collect indicator node
        if (indicator != null && indicator.getNode().getParent() != null) {
            nodesToRemove.add(indicator.getNode());
        }

        // ========== BATCH REMOVE: Much faster than individual removes ==========
        root.getChildren().removeAll(nodesToRemove);

        // Reset paddle
        paddle.reset();

        // Create new ball
        Ball ball = new Ball(paddle.getX() + paddle.getWidth() / 2, paddle.getY() - 8, BALL_RADIUS, BALL_SPEED);

        // Apply skins
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

        // Create new indicator
        indicator = new Indicator(ball.getX() + ball.getRadius(), ball.getY());
        indicator.pointAtBall(ball);
        indicator.setRotation(-90);

        // ========== BATCH ADD: Add all new nodes at once ==========
        root.getChildren().addAll(ball.getTrailGroup(), ball.getNode(), indicator.getNode());

        // Update game state
        gameState = GameState.START;
        for (Ball b : balls) {
            b.setStuck(true);
        }
    }

    public void changeGameState(GameConfig.GameState newState) {
        this.gameState = newState;

        if (isOneVOneMode && oneVOneScreen != null) {
            oneVOneScreen.showGameMessage(newState);
        } else if (isBotMode && botScreen != null) {
            botScreen.showGameMessage(newState);
        } else if (endlessScreen != null) {
            endlessScreen.showGameMessage(newState);
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
                if (onGameOver != null) {
                    onGameOver.run();
                }
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
        // Stop game loop
        if (gameLoop != null) {
            gameLoop.stop();
        }

        levelManager.currentLevel++;

        if (levelManager.currentLevel <= levelManager.maxLevel) {
            // Create smooth fade transition
            createTransitionOverlay();

            if (!root.getChildren().contains(transitionOverlay)) {
                root.getChildren().add(transitionOverlay);
                transitionOverlay.toFront();
            }

            // Fade to black
            javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(
                    javafx.util.Duration.millis(200), transitionOverlay
            );
            fadeOut.setFromValue(0);
            fadeOut.setToValue(0.8);

            fadeOut.setOnFinished(e -> {
                // Quick cleanup during fade
                quickCleanupForLevelTransition();

                // Small pause for cleanup to complete
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(
                        javafx.util.Duration.millis(50)
                );

                pause.setOnFinished(p -> {
                    // Load new level
                    levelManager.loadLevel(levelManager.currentLevel, root);
                    resetBallAndPaddle();

                    if (singleplayerScreen != null) {
                        singleplayerScreen.showLevel(levelManager.currentLevel);
                    }

                    // Fade from black
                    javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(
                            javafx.util.Duration.millis(200), transitionOverlay
                    );
                    fadeIn.setFromValue(0.8);
                    fadeIn.setToValue(0);

                    fadeIn.setOnFinished(f -> {
                        root.getChildren().remove(transitionOverlay);

                        // Resume game loop
                        if (gameLoop != null) {
                            gameLoop.start();
                        }
                    });

                    fadeIn.play();
                });

                pause.play();
            });

            fadeOut.play();

        } else {
            changeGameState(GameConfig.GameState.GAME_OVER);
        }
    }

    // THÊM METHOD MỚI: Quick cleanup for level transitions
    private void quickCleanupForLevelTransition() {
        // Batch collection for removal
        List<javafx.scene.Node> nodesToRemove = new ArrayList<>();

        // Clear balls
        for (Ball b : new java.util.ArrayList<>(balls)) {
            b.clearTrail();
            if (b.getTrailGroup() != null && b.getTrailGroup().getParent() != null) {
                nodesToRemove.add(b.getTrailGroup());
            }
            if (b.getNode() != null && b.getNode().getParent() != null) {
                nodesToRemove.add(b.getNode());
            }
        }
        balls.clear();

        // Clear indicator
        if (indicator != null && indicator.getNode().getParent() != null) {
            nodesToRemove.add(indicator.getNode());
        }
        indicator = null;

        // Clear powerups
        for (Powerup p : levelManager.getPowerups()) {
            if (p.getNode() != null && p.getNode().getParent() != null) {
                nodesToRemove.add(p.getNode());
            }
        }
        levelManager.getPowerups().clear();

        // Batch remove
        root.getChildren().removeAll(nodesToRemove);
    }

    public void cleanupGameObjects() {
        // ========== THÊM: Stop game loop first ==========
        if (gameLoop != null) {
            gameLoop.stop();
        }

        // ========== THÊM: Stop oneshot timer if active ==========
        if (oneshotTimer != null) {
            oneshotTimer.stop();
            oneshotTimer = null;
        }

        // Cleanup paddles
        if (paddle != null && paddle.getNode().getParent() != null) {
            root.getChildren().remove(paddle.getNode());
        }
        paddle = null; // ========== THÊM: Set null ==========

        if (paddle2 != null && paddle2.getNode().getParent() != null) {
            root.getChildren().remove(paddle2.getNode());
        }
        paddle2 = null; // ========== THÊM: Set null ==========

        // ========== SỬA: Cleanup balls - CRITICAL FIX ==========
        ballsToRemove.clear();
        ballsToRemove.addAll(balls);
        for (Ball b : ballsToRemove) {
            // ========== THÊM: Disable trail to clear all trail nodes ==========
            b.setTrailEnabled(false);

            // ========== THÊM: Clear trail group properly ==========
            if (b.getTrailGroup() != null) {
                if (b.getTrailGroup().getParent() != null) {
                    root.getChildren().remove(b.getTrailGroup());
                }
                b.getTrailGroup().getChildren().clear();
            }

            // Remove ball node
            if (b.getNode() != null && b.getNode().getParent() != null) {
                root.getChildren().remove(b.getNode());
            }
        }
        balls.clear();
        ballsToRemove.clear();

        // Cleanup indicator
        if (indicator != null) {
            if (indicator.getNode().getParent() != null) {
                root.getChildren().remove(indicator.getNode());
            }
            indicator = null;
        }

        // Cleanup bricks
        cachedBricks.clear();
        cachedBricks.addAll(levelManager.getBricks());
        for (Brick brick : cachedBricks) {
            // ========== SỬA: Remove brick node properly ==========
            if (brick.getNode() != null && brick.getNode().getParent() != null) {
                root.getChildren().remove(brick.getNode());
            }
        }
        levelManager.getBricks().clear();
        cachedBricks.clear();

        // Cleanup powerups
        levelManager.clearAllPowerups(root);
        cachedPowerups.clear();

        // Cleanup UI screens
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

        if (endlessScreen != null) {
            endlessScreen.cleanup();
            endlessScreen = null;
        }

        // ========== THÊM: Reset flags ==========
        isOneVOneMode = false;
        isBotMode = false;
        lastScoredPlayer = 1;

        // ========== THÊM: Reset collision manager state ==========
        collisionManager.setOneshotActive(false);

        // ========== THÊM: Force garbage collection hint ==========
        System.gc();
    }

    public void hideAllGameObjects() {
        if (paddle != null) {
            paddle.getNode().setVisible(false);
        }
        if (paddle2 != null) {
            paddle2.getNode().setVisible(false);
        }

        for (Ball ball : balls) {
            ball.getNode().setVisible(false);
        }

        for (Brick brick : levelManager.getBricks()) {
            brick.getNode().setVisible(false);
        }
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
                String sel = coinManager == null ? null : coinManager.getSelectedBallSkin();
                newBall.applySkin(skinIdToBallResource(sel));
                newBall.storeSkin();
                newBall.applyOneshotSkin();
            } else {
                newBall.applySkin(ref.getCurrentSkinResource());
            }

            balls.add(newBall);
            root.getChildren().addAll(newBall.getTrailGroup(), newBall.getNode());
        }
    }

    public void enableOneshot() {
        collisionManager.setOneshotActive(true);
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
            for (Ball b : balls) {
                b.restoreSkin();
            }
            if (coinManager != null && paddle != null) {
                String sel = coinManager.getSelectedPaddleSkin();
                paddle.applySkin(sel);
            }
        });
        oneshotTimer.playFromStart();
    }

    public void spawnDebugBall() {
        Ball ball = new Ball(GAME_WIDTH / 2, GAME_HEIGHT - 35, BALL_RADIUS, BALL_SPEED);
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
        ball.launch();
        root.getChildren().addAll(ball.getTrailGroup(), ball.getNode());
    }

    public void clearAllBricks() {
        levelManager.clearAllBricks(root);
        if (levelManager.isLevelComplete()) {
            changeGameState(GameState.LEVEL_CLEARED);
        }
    }

    private String skinIdToBallResource(String skinId) {
        if (skinId == null) return "/imageball/default.png";
        return switch (skinId) {
            case "skin1" -> "/imageball/skin1.png";
            case "skin2" -> "/imageball/skin2.png";
            case "oneshot" -> "/imageball/oneshot.png";
            default -> "/imageball/default.png";
        };
    }

    public void applyPaddleSkin(String skinId) {
        if (skinId == null) return;
        if (coinManager != null) coinManager.setSelectedPaddleSkin(skinId);
        if (paddle != null) paddle.applySkin(skinId);
    }

    public void applyBallSkin(String skinId) {
        if (skinId == null) return;
        if (coinManager != null) coinManager.setSelectedBallSkin(skinId);
        String res = skinIdToBallResource(skinId);
        for (Ball b : balls) {
            b.applySkin(res);
        }
    }

    public void handleIndicatorRotateLeft() {
        if (indicator != null) {
            indicator.rotateLeft(0.05);
        }
    }

    public void handleIndicatorRotateRight() {
        if (indicator != null) {
            indicator.rotateRight(0.05);
        }
    }

    public GameConfig.GameState getGameState() {
        return gameState;
    }

    public boolean isBotMode() {
        return isBotMode;
    }

    public boolean isOneVOneMode() {
        return isOneVOneMode;
    }

    public int getLastScoredPlayer() {
        return lastScoredPlayer;
    }

    public Indicator getIndicator() {
        return indicator;
    }

    public ScoreManager getScoreManager() {
        return scoreManager;
    }

    public CoinManager getCoinManager() {
        return coinManager;
    }

    public int getFinalScore() {
        if (endlessScreen != null) {
            return endlessScreen.getScore();
        } else if (singleplayerScreen != null) {
            return singleplayerScreen.getScore();
        }
        return 0;
    }

    public void setOnGameOver(Runnable onGameOver) {
        this.onGameOver = onGameOver;
    }

    public void setMovingLeft(boolean moving) {
        this.isMovingLeft = moving;
    }

    public void setMovingRight(boolean moving) {
        this.isMovingRight = moving;
    }

    public void setMovingLeft2(boolean moving) {
        this.isMovingLeft2 = moving;
    }

    public void setMovingRight2(boolean moving) {
        this.isMovingRight2 = moving;
    }

    /**
     * Restart game loop - useful when resolution changes
     */
    public void restartGameLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        initGameLoop();
    }

    private void fadeOutTransition(Runnable afterFade) {
        Rectangle fadeRect = new Rectangle(GAME_WIDTH, GAME_HEIGHT, Color.BLACK);
        fadeRect.setOpacity(0);
        root.getChildren().add(fadeRect);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(700), fadeRect);
        fadeOut.setFromValue(0.0);
        fadeOut.setToValue(1.0);
        fadeOut.setInterpolator(Interpolator.EASE_IN);

        fadeOut.setOnFinished(e -> {
            afterFade.run();
            FadeTransition fadeIn = new FadeTransition(Duration.millis(700), fadeRect);
            fadeIn.setFromValue(1.0);
            fadeIn.setToValue(0.0);
            fadeIn.setInterpolator(Interpolator.EASE_OUT);
            fadeIn.setOnFinished(ev -> root.getChildren().remove(fadeRect));
            fadeIn.play();
        });

        fadeOut.play();
    }

}
