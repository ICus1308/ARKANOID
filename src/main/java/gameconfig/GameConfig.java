package gameconfig;

public final class GameConfig {
    private GameConfig() {}

    public static double GAME_WIDTH = 1280;
    public static double GAME_HEIGHT = 720;
    public static final int BRICK_ROWS = 14;
    public static final int BRICK_COLS = 14;

    // Base values for 1280x720 resolution
    private static final double BASE_PADDLE_SPEED = 12.0;
    private static final double BASE_PADDLE_WIDTH = 140.0;
    private static final double BASE_PADDLE_HEIGHT = 15.0;
    private static final double BASE_BALL_RADIUS = 12.0;
    private static final double BASE_BALL_SPEED = 10.0;

    // Scaled values (will be updated when resolution changes)
    public static double PADDLE_SPEED = BASE_PADDLE_SPEED;
    public static double PADDLE_WIDTH = BASE_PADDLE_WIDTH;
    public static double PADDLE_HEIGHT = BASE_PADDLE_HEIGHT;
    public static double BALL_RADIUS = BASE_BALL_RADIUS;
    public static double BALL_SPEED = BASE_BALL_SPEED;

    public static final double BRICK_HEIGHT = 20;

    // Ball skin paths
    public static final String BALL_DEFAULT_SKIN = "/imageball/default.png";
    public static final String BALL_ONESHOT_SKIN = "/imageball/oneshot.png";

    // Brick properties
    public static final int INDESTRUCTIBLE_HIT_COUNT = -1;
    public static final String INDESTRUCTIBLE_COLOR = "#7f8c8d";
    public static final int EXPLODING_HIT_COUNT = 1;
    public static final String EXPLODING_COLOR = "#e67e22";
    public static final int EXPLOSION_RANGE = 1;

    // Indicator properties
    public static final double INDICATOR_TRIANGLE_SIZE = 20.0;
    public static final double INDICATOR_OFFSET_DISTANCE = 50.0;
    public static final double INDICATOR_MIN_ANGLE_BOTTOM = -Math.PI;
    public static final double INDICATOR_MAX_ANGLE_BOTTOM = 0.0;
    public static final double INDICATOR_MIN_ANGLE_TOP = 0.0;
    public static final double INDICATOR_MAX_ANGLE_TOP = Math.PI;

    // Powerup dimensions
    public static final double POWERUP_WIDTH = 20;
    public static final double POWERUP_HEIGHT = 15;
    public static final double POWERUP_FALL_SPEED = 1.0;

    // UI scaling factors based on resolution
    public static double UI_SCALE_X = 1.0;  // Width scale
    public static double UI_SCALE_Y = 1.0;  // Height scale

    // For elements that should scale uniformly, use the smaller scale
    public static double UI_SCALE = 1.0;

    // Calculate UI scale based on current dimensions (1280x720 is base resolution)
    public static void updateUIScale() {
        UI_SCALE_X = GAME_WIDTH / 1280.0;
        UI_SCALE_Y = GAME_HEIGHT / 720.0;
        UI_SCALE = Math.min(UI_SCALE_X, UI_SCALE_Y);  // Use minimum for uniform elements

        // Update paddle and ball dimensions based on scale
        PADDLE_WIDTH = BASE_PADDLE_WIDTH * UI_SCALE;
        PADDLE_HEIGHT = BASE_PADDLE_HEIGHT * UI_SCALE;
        PADDLE_SPEED = BASE_PADDLE_SPEED * UI_SCALE;
        BALL_RADIUS = BASE_BALL_RADIUS * UI_SCALE;
        BALL_SPEED = BASE_BALL_SPEED * UI_SCALE;
    }

    public enum GameState {
        MENU,
        GAME_MODE,
        HIGH_SCORE,
        SETTING,
        SHOP,
        START,
        PLAYING,
        PAUSED,
        GAME_OVER,
        LEVEL_CLEARED,
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
