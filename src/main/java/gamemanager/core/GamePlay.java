package gamemanager.core;

import gamemanager.manager.SoundManager;
import gamemanager.ui.ScreenManager;
import gamemanager.ui.VideoBackgroundManager;
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

    // Input Handler - handles all keyboard input
    private InputHandler inputHandler;

    // UI Screens
    private MenuScreen menuScreen;
    private SettingScreen settingScreen;
    private GameOverScreen gameOverScreen;
    private PauseScreen pauseScreen;
    private VideoBackgroundManager videoBackgroundManager;


    private void initializeRoot() {
        root = new Pane();
        root.setPrefSize(GAME_WIDTH, GAME_HEIGHT);
        root.setStyle("-fx-background-color: #2c3e50;");
    }

    private void initializeGameEngine() {
        gameEngine = new GameEngine(root);
        gameEngine.setOnGameOver(this::handleGameOver);
        inputHandler = new InputHandler(gameEngine, this::pauseGame, this::resumeGame, this::returnToMenu);
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
            this::showShopScreen,
            this::showTutorialScreen
        );

        GameModeScreen gameModeScreen = new GameModeScreen(
                root,
                this::startSinglePlayerGame,
                this::startOneVOneGame,
                this::startBotGame,
                this::startEndlessGame,
                this::showMenuScreen
        );

        HighScoreScreen highScoreScreen = new HighScoreScreen(
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

        ShopScreen shopScreen = new ShopScreen(
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
        scene.setOnKeyPressed(e -> inputHandler.handleKeyPressed(e));
        scene.setOnKeyReleased(e -> inputHandler.handleKeyReleased(e));
    }

    private void setupStage() {
        primaryStage.setTitle("Arkanoid");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.NO_MATCH);  // Disable ESC to exit fullscreen
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

    private void showTutorialScreen() {
        // TODO: Implement tutorial screen logic
        System.out.println("Tutorial screen clicked - Implementation pending");
    }


    private void startSinglePlayerGame() {
        screenManager.hideAllScreens();
        gameEngine.cleanupGameObjects(); // ========== THÊM ==========
        gameEngine.startSinglePlayerGame();
    }

    private void startBotGame() {
        screenManager.hideAllScreens();
        gameEngine.cleanupGameObjects(); // ========== THÊM ==========
        gameEngine.startBotGame();
    }

    private void startOneVOneGame() {
        screenManager.hideAllScreens();
        gameEngine.cleanupGameObjects(); // ========== THÊM ==========
        gameEngine.startOneVOneGame();
    }

    private void startEndlessGame() {
        screenManager.hideAllScreens();
        gameEngine.cleanupGameObjects(); // ========== THÊM ==========
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
        SoundManager.getInstance().stopMusic(); // ========== THÊM: Stop music first ==========
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
        // Ẩn tất cả game objects
        gameEngine.hideAllGameObjects();

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

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        initializeRoot();

        // Initialize video background and keep reference so we can update size later
        this.videoBackgroundManager = new VideoBackgroundManager(root, "/background/videobackground.mp4");

        initializeGameEngine();
        initializeScreenManager();
        initializeScreens();
        setupScene();
        setupStage();

        showMenuScreen();
        gameEngine.initGameLoop();
    }

    private void refreshAllScreens() {
        // ========== THÊM: Cleanup thoroughly before refresh ==========
        gameEngine.cleanupGameObjects();

        // Hide and refresh all screens
        screenManager.hideAllScreens();
        screenManager.refreshAllScreens();

        // Update root size
        root.setPrefSize(GAME_WIDTH, GAME_HEIGHT);

        // Update video size khi đổi resolution
        if (videoBackgroundManager != null) {
            videoBackgroundManager.updateSize();
        }

        // ========== THÊM: Recreate setting screen ==========
        settingScreen = new SettingScreen(
                root,
                this::showMenuScreen,
                this::refreshAllScreens
        );
        screenManager.registerScreen(GameState.SETTING, settingScreen);

        settingScreen.show();

        // ========== THÊM: Force garbage collection ==========
        System.gc();
    }
}
