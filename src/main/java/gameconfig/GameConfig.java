package gameconfig;

public final class GameConfig {
    private GameConfig() {}

    public static double GAME_WIDTH = 800;
    public static final double GAME_HEIGHT = 600;
    public static final int BRICK_ROWS = 14;
    public static final int BRICK_COLS = 14;
    public static final double PADDLE_SPEED = 12.0;
    public static final double PADDLE_WIDTH = 100.0;
    public static final double PADDLE_HEIGHT = 15.0;
    public static final double BALL_RADIUS = 8.0;
    public static final double BALL_SPEED = 10.0;
    public static final double BRICK_WIDTH = 40;
    public static final double BRICK_HEIGHT = 20;

    // UI scaling factors based on resolution
    public static double UI_SCALE_X = 1.0;  // Width scale
    public static double UI_SCALE_Y = 1.0;  // Height scale (always 1.0 since height is fixed)

    // For elements that should scale uniformly, use the smaller scale
    public static double UI_SCALE = 1.0;

    // Calculate UI scale based on current width (800 is base width, height is fixed at 600)
    public static void updateUIScale() {
        UI_SCALE_X = GAME_WIDTH / 800.0;
        UI_SCALE_Y = 1.0;  // Height is always 600, so no Y scaling
        UI_SCALE = UI_SCALE_X;  // Use X scale for uniform elements
    }

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
    public enum PaddleSkin {
        CLASSIC,
        FIRE,
        ICE,
        GOLDEN
    }

    public enum WallSideType {
        NORTH,
        SOUTH,
        EAST,
        WEST,
        HIT_OUT_OF_BOUNDS,
        BOTTOM_HIT
    }

    public enum PowerUpType {
        MULTIPLY,
        ONESHOT,
        EXPAND
    }

    public enum ButtonStyle {
        PRIMARY,
        CATEGORY_UNSELECTED,
        CATEGORY_SELECTED,
        APPLY
    }
}
