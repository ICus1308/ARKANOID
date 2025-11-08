package gamemanager.core;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import static gameconfig.GameConfig.GameState;

public record InputHandler(GameEngine gameEngine, Runnable onPause, Runnable onResume, Runnable onReturnToMenu) {

    public void handleKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        GameState state = gameEngine.getGameState();

        switch (code) {
            case A:
                handleAKey(state);
                break;
            case D:
                handleDKey(state);
                break;
            case LEFT:
                handleLeftKey(state);
                break;
            case RIGHT:
                handleRightKey(state);
                break;
            case SPACE:
                handleSpaceKey(state);
                break;
            case ESCAPE:
                handleEscapeKey(state);
                break;
            case T:
                gameEngine.clearAllBricks();
                break;
            default:
                break;
        }
    }

    public void handleKeyReleased(KeyEvent event) {
        KeyCode code = event.getCode();

        switch (code) {
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
    }

    private void handleAKey(GameState state) {
        if (shouldHandleIndicator(state, 1)) {
            gameEngine.handleIndicatorRotateLeft();
        } else if (shouldHandleIndicatorSinglePlayer(state)) {
            gameEngine.handleIndicatorRotateLeft();
        } else {
            gameEngine.setMovingLeft(true);
        }
    }

    private void handleDKey(GameState state) {
        if (shouldHandleIndicator(state, 1)) {
            gameEngine.handleIndicatorRotateRight();
        } else if (shouldHandleIndicatorSinglePlayer(state)) {
            gameEngine.handleIndicatorRotateRight();
        } else {
            gameEngine.setMovingRight(true);
        }
    }

    private void handleLeftKey(GameState state) {
        if (gameEngine.isOneVOneMode() && shouldHandleIndicator(state, 2)) {
            gameEngine.handleIndicatorRotateLeft();
        } else if (gameEngine.isOneVOneMode()) {
            gameEngine.setMovingLeft2(true);
        } else if (shouldHandleIndicatorSinglePlayer(state)) {
            gameEngine.handleIndicatorRotateLeft();
        } else {
            gameEngine.setMovingLeft(true);
        }
    }

    private void handleRightKey(GameState state) {
        if (gameEngine.isOneVOneMode() && shouldHandleIndicator(state, 2)) {
            gameEngine.handleIndicatorRotateRight();
        } else if (gameEngine.isOneVOneMode()) {
            gameEngine.setMovingRight2(true);
        } else if (shouldHandleIndicatorSinglePlayer(state)) {
            gameEngine.handleIndicatorRotateRight();
        } else {
            gameEngine.setMovingRight(true);
        }
    }

    private void handleSpaceKey(GameState state) {
        if (state == GameState.START || state == GameState.LEVEL_CLEARED) {
            gameEngine.startGame();
        }
    }

    private void handleEscapeKey(GameState state) {
        if (state == GameState.PLAYING || state == GameState.START) {
            if (onPause != null) {
                onPause.run();
            }
        } else if (state == GameState.PAUSED) {
            if (onResume != null) {
                onResume.run();
            }
        } else if (state == GameState.GAME_OVER) {
            if (onReturnToMenu != null) {
                onReturnToMenu.run();
            }
        }
    }

    private boolean shouldHandleIndicator(GameState state, int player) {
        return (gameEngine.isOneVOneMode() || gameEngine.isBotMode()) &&
                state == GameState.START &&
                gameEngine.getIndicator() != null &&
                gameEngine.getLastScoredPlayer() == player;
    }

    private boolean shouldHandleIndicatorSinglePlayer(GameState state) {
        return state == GameState.START &&
                gameEngine.getIndicator() != null &&
                !gameEngine.isOneVOneMode() &&
                !gameEngine.isBotMode();
    }
}
