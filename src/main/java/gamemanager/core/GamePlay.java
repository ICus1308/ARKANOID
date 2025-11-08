package gamemanager.core;

import gamemanager.ui.ScreenManager;
import gamemanager.manager.SoundManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import userinterface.screen.*;

import java.util.Optional;

import static gameconfig.GameConfig.*;

/**
 * GamePlay - JavaFX Application wrapper
 * Handles UI screens, input routing, and delegates game logic to GameEngine
 */
public class GamePlay extends Application {
    private Pane root;
    private Scene scene;
    private Stage primaryStage;

    // Game Engine - handles all game logic
    private GameEngine gameEngine;

    // Screen Manager - handles all screen transitions
    private ScreenManager screenManager;

    // UI Screens
    private MenuScreen menuScreen;
    private GameModeScreen gameModeScreen;
    private HighScoreScreen highScoreScreen;
    private SettingScreen settingScreen;
    private GameOverScreen gameOverScreen;
    private ShopScreen shopScreen;
    private PauseScreen pauseScreen;


    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        initializeRoot();
        initializeGameEngine();
        initializeScreenManager();
        initializeScreens();
        setupScene();
        setupStage();

        showMenuScreen();
        gameEngine.initGameLoop();
    }

    private void initializeRoot() {
        root = new Pane();
        root.setPrefSize(GAME_WIDTH, GAME_HEIGHT);
        root.setStyle("-fx-background-color: #2c3e50;");
    }

    private void initializeGameEngine() {
        gameEngine = new GameEngine(root);
        gameEngine.setOnGameOver(this::handleGameOver);
    }

    private void initializeScreenManager() {
        screenManager = new ScreenManager(root);
    }

    private void initializeScreens() {
        menuScreen = new MenuScreen(
            root,
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
            this::startEndlessGame,
            this::showMenuScreen
        );

        highScoreScreen = new HighScoreScreen(
            root,
            this::showMenuScreen,
            gameEngine.getScoreManager()
        );

        settingScreen = new SettingScreen(
            root,
            this::showMenuScreen,
            this::refreshAllScreens
        );

        pauseScreen = new PauseScreen(
            root,
            this::resumeGame,
            this::returnToMenuFromPause
        );

        gameOverScreen = new GameOverScreen(
            root,
            this::retryLevel,
            this::startNewGame,
            this::returnToMenu
        );

        shopScreen = new ShopScreen(
            root,
            gameEngine.getCoinManager(),
            gameEngine::applyPaddleSkin,
            gameEngine::applyBallSkin,
            this::showMenuScreen
        );

        screenManager.registerScreen(GameState.MENU, menuScreen);
        screenManager.registerScreen(GameState.GAME_MODE, gameModeScreen);
        screenManager.registerScreen(GameState.HIGH_SCORE, highScoreScreen);
        screenManager.registerScreen(GameState.SETTING, settingScreen);
        screenManager.registerScreen(GameState.GAME_OVER, gameOverScreen);
        screenManager.registerScreen(GameState.SHOP, shopScreen);
        screenManager.registerScreen(GameState.PAUSED, pauseScreen);
    }

    private void setupScene() {
        scene = new Scene(root);
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case A:
                    if ((gameEngine.isOneVOneMode() || gameEngine.isBotMode()) &&
                        gameEngine.getGameState() == GameState.START &&
                        gameEngine.getIndicator() != null &&
                        gameEngine.getLastScoredPlayer() == 1) {
                        gameEngine.handleIndicatorRotateLeft();
                    } else if (gameEngine.getGameState() == GameState.START &&
                               gameEngine.getIndicator() != null &&
                               !gameEngine.isOneVOneMode() &&
                               !gameEngine.isBotMode()) {
                        gameEngine.handleIndicatorRotateLeft();
                    } else {
                        gameEngine.setMovingLeft(true);
                    }
                    break;
                case D:
                    if ((gameEngine.isOneVOneMode() || gameEngine.isBotMode()) &&
                        gameEngine.getGameState() == GameState.START &&
                        gameEngine.getIndicator() != null &&
                        gameEngine.getLastScoredPlayer() == 1) {
                        gameEngine.handleIndicatorRotateRight();
                    } else if (gameEngine.getGameState() == GameState.START &&
                               gameEngine.getIndicator() != null &&
                               !gameEngine.isOneVOneMode() &&
                               !gameEngine.isBotMode()) {
                        gameEngine.handleIndicatorRotateRight();
                    } else {
                        gameEngine.setMovingRight(true);
                    }
                    break;
                case LEFT:
                    if (gameEngine.isOneVOneMode() &&
                        gameEngine.getGameState() == GameState.START &&
                        gameEngine.getIndicator() != null &&
                        gameEngine.getLastScoredPlayer() == 2) {
                        gameEngine.handleIndicatorRotateLeft();
                    } else if (gameEngine.isOneVOneMode()) {
                        gameEngine.setMovingLeft2(true);
                    } else if (gameEngine.getGameState() == GameState.START &&
                               gameEngine.getIndicator() != null) {
                        gameEngine.handleIndicatorRotateLeft();
                    } else {
                        gameEngine.setMovingLeft(true);
                    }
                    break;
                case RIGHT:
                    if (gameEngine.isOneVOneMode() &&
                        gameEngine.getGameState() == GameState.START &&
                        gameEngine.getIndicator() != null &&
                        gameEngine.getLastScoredPlayer() == 2) {
                        gameEngine.handleIndicatorRotateRight();
                    } else if (gameEngine.isOneVOneMode()) {
                        gameEngine.setMovingRight2(true);
                    } else if (gameEngine.getGameState() == GameState.START &&
                               gameEngine.getIndicator() != null) {
                        gameEngine.handleIndicatorRotateRight();
                    } else {
                        gameEngine.setMovingRight(true);
                    }
                    break;
                case SPACE:
                    if (gameEngine.getGameState() == GameState.START ||
                        gameEngine.getGameState() == GameState.LEVEL_CLEARED) {
                        gameEngine.startGame();
                    }
                    break;
                case ESCAPE:
                    if (gameEngine.getGameState() == GameState.PLAYING) {
                        pauseGame();
                    } else if (gameEngine.getGameState() == GameState.PAUSED) {
                        resumeGame();
                    } else if (gameEngine.getGameState() == GameState.START ||
                               gameEngine.getGameState() == GameState.GAME_OVER) {
                        returnToMenu();
                    }
                    break;
                case T:
                    gameEngine.clearAllBricks();
                    break;
                default:
                    break;
            }
        });

        scene.setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case A:
                    gameEngine.setMovingLeft(false);
                    break;
                case D:
                    gameEngine.setMovingRight(false);
                    break;
                case LEFT:
                    if (gameEngine.isOneVOneMode()) {
                        gameEngine.setMovingLeft2(false);
                    } else {
                        gameEngine.setMovingLeft(false);
                    }
                    break;
                case RIGHT:
                    if (gameEngine.isOneVOneMode()) {
                        gameEngine.setMovingRight2(false);
                    } else {
                        gameEngine.setMovingRight(false);
                    }
                    break;
                case R:
                    gameEngine.spawnDebugBall();
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
        screenManager.showScreen(GameState.MENU);
        SoundManager.getInstance().playMusic(SoundManager.SoundType.MENU_MUSIC, true);
        menuScreen.getStackPane().toFront();
    }

    private void showGameModeScreen() {
        screenManager.showScreen(GameState.GAME_MODE);
    }

    private void showSettingScreen() {
        screenManager.showScreen(GameState.SETTING);
    }

    private void showHighScoreScreen() {
        screenManager.showScreen(GameState.HIGH_SCORE);
    }

    private void showShopScreen() {
        screenManager.showScreen(GameState.SHOP);
    }

    private void refreshAllScreens() {
        screenManager.refreshAllScreens();

        root.setPrefSize(GAME_WIDTH, GAME_HEIGHT);

        settingScreen.refresh();
        settingScreen.show();
    }

    private void startSinglePlayerGame() {
        screenManager.hideAllScreens();
        gameEngine.startSinglePlayerGame();
    }

    private void startBotGame() {
        screenManager.hideAllScreens();
        gameEngine.startBotGame();
    }

    private void startOneVOneGame() {
        screenManager.hideAllScreens();
        gameEngine.startOneVOneGame();
    }

    private void startEndlessGame() {
        screenManager.hideAllScreens();
        gameEngine.startEndlessGame();
    }

    private void retryLevel() {
        gameOverScreen.hide();
        gameEngine.retryLevel();
    }

    private void startNewGame() {
        gameOverScreen.hide();
        gameEngine.startNewGame();
    }

    private void returnToMenu() {
        gameEngine.changeGameState(GameState.MENU);
        gameEngine.cleanupGameObjects();
        showMenuScreen();
    }

    private void pauseGame() {
        gameEngine.pauseGame();
        pauseScreen.show();
    }

    private void resumeGame() {
        pauseScreen.hide();
        gameEngine.resumeGame();
    }

    private void returnToMenuFromPause() {
        pauseScreen.hide();
        returnToMenu();
    }

    public void handleGameOver() {
        if (gameEngine.isOneVOneMode()) {
            gameOverScreen.refresh();
            screenManager.showScreen(GameState.GAME_OVER);
        } else if (gameEngine.isBotMode()) {
            gameOverScreen.refresh();
            screenManager.showScreen(GameState.GAME_OVER);
        } else {
            int finalScore = gameEngine.getFinalScore();
            gameOverScreen.setFinalScore(finalScore);
            gameOverScreen.refresh();
            screenManager.showScreen(GameState.GAME_OVER);
            promptAndSaveScore();
        }
    }

    private void promptAndSaveScore() {
        TextInputDialog dialog = new TextInputDialog("Player");
        dialog.setTitle("Game Over");
        dialog.setHeaderText("You set a new score!");
        dialog.setContentText("Please enter your name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            int finalScore = gameEngine.getFinalScore();
            gameEngine.getScoreManager().addScore(name, finalScore);
        });
    }
}
