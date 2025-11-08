package gamemanager.ui;

import javafx.scene.layout.Pane;

import java.util.HashMap;
import java.util.Map;

import static gameconfig.GameConfig.GameState;

/**
 * ScreenManager - Centralized management of screen transitions and visibility
 * Handles showing, hiding, and refreshing all game screens based on GameState
 */
public class ScreenManager {

    private final Map<GameState, UIManager> screens;
    private GameState currentState;

    public ScreenManager(Pane root) {
        this.screens = new HashMap<>();
        this.currentState = null;
    }

    /**
     * Register a screen with the manager for a specific game state
     */
    public void registerScreen(GameState state, UIManager screen) {
        screens.put(state, screen);
    }

    /**
     * Get a registered screen by game state
     */
    public UIManager getScreen(GameState state) {
        return screens.get(state);
    }

    /**
     * Show a specific screen based on game state and hide all others
     */
    public void showScreen(GameState state) {
        hideAllScreens();
        UIManager screen = screens.get(state);
        if (screen != null) {
            screen.show();
            currentState = state;
        }
    }

    /**
     * Hide all registered screens
     */
    public void hideAllScreens() {
        for (UIManager screen : screens.values()) {
            if (screen != null) {
                screen.hide();
            }
        }
    }

    /**
     * Refresh all screens (useful after settings changes)
     */
    public void refreshAllScreens() {
        for (UIManager screen : screens.values()) {
            if (screen != null) {
                screen.refresh();
            }
        }
    }

    /**
     * Refresh a specific screen
     */
    public void refreshScreen(GameState state) {
        UIManager screen = screens.get(state);
        if (screen != null) {
            screen.refresh();
        }
    }

    /**
     * Get the current game state
     */
    public GameState getCurrentState() {
        return currentState;
    }

    /**
     * Check if a specific state's screen is currently shown
     */
    public boolean isStateShowing(GameState state) {
        return currentState == state;
    }
}


