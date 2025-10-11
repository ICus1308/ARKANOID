package gameconfig;

public final class GameConfig {
    private GameConfig() {}

    public static final double GAME_WIDTH = 800;
    public static final double GAME_HEIGHT = 600;
    public static final int BRICK_ROWS = 10;
    public static final int BRICK_COLS = 10;
    public static final double PADDLE_SPEED = 8.0;

    public enum GameState {
        MENU,
        START,
        PLAYING,
        PAUSED,
        GAME_OVER,
        LEVEL_CLEARED,
        TUTORIAL,
        SHOP
    }

    public enum WallSideType {
        NORTH, SOUTH, EAST, WEST, HIT_OUT_OF_BOUNDS, BOTTOM_HIT
    }

    public enum PowerupType {
        MULTIPLY, ONESHOT, EXPAND
    }
}
