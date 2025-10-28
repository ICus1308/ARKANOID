package userinterface;

import static gameconfig.GameConfig.GameState;

public interface GameScreen {
    void updateScore(int newScore);
    void increaseScore(int delta);
    int getScore();
    int getLives();
    void updateLives(int newLives);
    void decreaseLives();
    void updateCoins();
    void showLevel(int level);
    void showGameMessage(GameState state);
    void cleanup();
}

