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


import java.util.ArrayList;
import java.util.List;

import static gameconfig.GameConfig.*;

/**
 * GameEngine - Pure game logic without JavaFX Application dependencies
 * Handles game loop, collision detection, and game state management
 */
public class GameEngine {
    private final Pane root;
    private GameConfig.GameState gameState = GameConfig.GameState.MENU;
    private GameConfig.GameState previousGameState = GameConfig.GameState.MENU;

    // Managers
    private final LevelManager levelManager;
    private final CollisionManager collisionManager;
    private final ScoreManager scoreManager;
    private final CoinManager coinManager;
    private final SoundManager soundManager;

    // Game Objects
    private Paddle paddle;
    private Paddle paddle2;
    private final List<Ball> balls = new ArrayList<>();
    private Indicator indicator;
    private AIManager aiManager;

    // Game Mode State
    private boolean isBotMode = false;
    private boolean isOneVOneMode = false;
    private int lastScoredPlayer = 1;

    // Input State
    private boolean isMovingLeft = false;
    private boolean isMovingRight = false;
    private boolean isMovingLeft2 = false;
    private boolean isMovingRight2 = false;

    // Game Screens
    private SingleplayerScreen singleplayerScreen;
    private BotScreen botScreen;
    private OneVOneScreen oneVOneScreen;
    private EndlessScreen endlessScreen;

    // Timers
    private PauseTransition oneshotTimer;
    private AnimationTimer gameLoop;

    // Callbacks
    private Runnable onGameOver;

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

    // ==================== Initialization ====================

    public void initGameLoop() {
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

    // ==================== Game Mode Initialization ====================

    public void initializeGameElements() {
        double paddleX = (GAME_WIDTH - PADDLE_WIDTH) / 2;
        double paddleY = GAME_HEIGHT - 20;
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

        root.getChildren().addAll(paddle.getNode(), ball.getNode(), indicator.getNode());
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
        paddle = new Paddle(paddleX, GAME_HEIGHT - 20, PADDLE_WIDTH, PADDLE_HEIGHT, PADDLE_SPEED);
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

        root.getChildren().addAll(paddle.getNode(), paddle2.getNode(), ball.getNode(), indicator.getNode());

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
        paddle = new Paddle(paddleX, GAME_HEIGHT - 20, PADDLE_WIDTH, PADDLE_HEIGHT, PADDLE_SPEED);
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

        root.getChildren().addAll(paddle.getNode(), paddle2.getNode(), ball.getNode(), indicator.getNode());

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

    // ==================== Game Control ====================

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

    // ==================== Game Loop Components ====================

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
        for (Ball b : new ArrayList<>(balls)) {
            if ((isOneVOneMode || isBotMode) && gameState == GameState.START) {
                b.update(tpf, lastScoredPlayer == 2 ? paddle2 : paddle, lastScoredPlayer == 2);
            } else {
                b.update(tpf, paddle, false);
            }
        }

        if (indicator != null && !balls.isEmpty() && gameState == GameState.START) {
            Ball firstBall = balls.get(0);
            indicator.updatePosition(firstBall.getX() + firstBall.getRadius(), firstBall.getY());
        }

        for (Powerup p : new ArrayList<>(levelManager.getPowerups())) {
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

    // ==================== Reset Methods ====================

    private void resetBallAndPaddle() {
        for (Ball b : balls) {
            root.getChildren().remove(b.getNode());
        }
        balls.clear();

        levelManager.clearAllPowerups(root);

        paddle.reset();

        Ball ball = new Ball(paddle.getX() + paddle.getWidth() / 2, paddle.getY() - 8, BALL_RADIUS, BALL_SPEED);
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
        indicator.setRotation(-90);
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

        paddle.reset();
        paddle2.reset();
        paddle.applySkin("blue");
        paddle2.applySkin("red");

        Ball ball = lastScoredPlayer == 1
            ? new Ball(paddle.getX() + paddle.getWidth() / 2, paddle.getY() - BALL_RADIUS * 2 - 2, BALL_RADIUS, BALL_SPEED)
            : new Ball(paddle2.getX() + paddle2.getWidth() / 2, paddle2.getY() + paddle2.getHeight() + 2, BALL_RADIUS, BALL_SPEED);

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
        indicator.setTopPaddle(lastScoredPlayer == 2);
        indicator.pointAtBall(ball);
        root.getChildren().add(indicator.getNode());

        changeGameState(GameState.START);
    }

    private void resetBallAndPaddleBot() {
        for (Ball b : balls) {
            root.getChildren().remove(b.getNode());
        }
        balls.clear();

        paddle.reset();
        paddle2.reset();
        paddle.applySkin("blue");
        paddle2.applySkin("red");

        Ball ball = lastScoredPlayer == 1
            ? new Ball(paddle.getX() + paddle.getWidth() / 2, paddle.getY() - BALL_RADIUS * 2 - 2, BALL_RADIUS, BALL_SPEED)
            : new Ball(paddle2.getX() + paddle2.getWidth() / 2, paddle2.getY() + paddle2.getHeight() + 2, BALL_RADIUS, BALL_SPEED);

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
        indicator.setTopPaddle(lastScoredPlayer == 2);
        indicator.pointAtBall(ball);
        root.getChildren().add(indicator.getNode());

        changeGameState(GameState.START);
    }

    // ==================== State Management ====================

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
        levelManager.currentLevel++;
        if (levelManager.currentLevel <= levelManager.maxLevel) {
            resetBallAndPaddle();
            levelManager.loadLevel(levelManager.currentLevel, root);
            if (singleplayerScreen != null) {
                singleplayerScreen.showLevel(levelManager.currentLevel);
            }
        } else {
            changeGameState(GameConfig.GameState.GAME_OVER);
        }
    }

    // ==================== Cleanup ====================

    public void cleanupGameObjects() {
        if (paddle != null && paddle.getNode().getParent() != null) {
            root.getChildren().remove(paddle.getNode());
        }

        if (paddle2 != null && paddle2.getNode().getParent() != null) {
            root.getChildren().remove(paddle2.getNode());
        }

        for (Ball b : new ArrayList<>(balls)) {
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

        for (Brick brick : new ArrayList<>(levelManager.getBricks())) {
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

        if (endlessScreen != null) {
            endlessScreen.cleanup();
            endlessScreen = null;
        }

        isOneVOneMode = false;
        isBotMode = false;
    }

    public void hideAllGameObjects() {
        // Ẩn paddle
        if (paddle != null) {
            paddle.getNode().setVisible(false);
        }
        if (paddle2 != null) {
            paddle2.getNode().setVisible(false);
        }

        // Ẩn tất cả balls
        for (Ball ball : balls) {
            ball.getNode().setVisible(false);
        }

        // Ẩn tất cả bricks
        for (Brick brick : levelManager.getBricks()) {
            brick.getNode().setVisible(false);
        }

    }
    // ==================== Powerup Methods (called by Powerup.activate) ====================

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
            root.getChildren().add(newBall.getNode());
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

    // ==================== Debug Methods ====================

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
        root.getChildren().add(ball.getNode());
    }

    public void clearAllBricks() {
        levelManager.clearAllBricks(root);
        if (levelManager.isLevelComplete()) {
            changeGameState(GameState.LEVEL_CLEARED);
        }
    }

    // ==================== Skin Management ====================

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

    // ==================== Input Handlers ====================

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

    // ==================== Getters ====================

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

    /**
     * Update paddle widths based on debug multiplier
     */
    public void updatePaddleDebugWidth() {
        if (paddle != null) {
            paddle.updateDebugWidth(DEBUG_PADDLE_LENGTH_MULTIPLIER);
        }
        if (paddle2 != null) {
            paddle2.updateDebugWidth(DEBUG_PADDLE_LENGTH_MULTIPLIER);
        }
    }

    // ==================== Input Setters ====================

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
}

