package gamemanager.ui;

import javafx.scene.layout.Pane;

import java.util.EnumMap;
import java.util.Map;

import static gameconfig.GameConfig.GameState;

public class ScreenManager {

    private final Map<GameState, UIManager> screens;
    private GameState currentState;

    public ScreenManager(Pane root) {
        this.screens = new EnumMap<>(GameState.class);
        this.currentState = null;
    }

    public void registerScreen(GameState state, UIManager screen) {
        screens.put(state, screen);
    }

    public void showScreen(GameState state) {
        if (state == null || !screens.containsKey(state)) {
            return;
        }

        if (currentState == state) {
            return;
        }

        if (currentState != null) {
            UIManager currentScreen = screens.get(currentState);
            if (currentScreen != null) {
                currentScreen.hide();
            }
        }

        currentState = state;

        UIManager newScreen = screens.get(state);
        if (newScreen != null) {
            newScreen.show();
        }
    }

    public void hideAllScreens() {
        screens.values().forEach(screen -> {
            if (screen != null) {
                screen.hide();
            }
        });
        currentState = null;
    }

    public void refreshAllScreens() {
        screens.values().forEach(screen -> {
            if (screen != null) {
                screen.refresh();
            }
        });
    }
}


