package gameplay;

public final class GameConfig {
    private GameConfig() {}
    public static final double WIDTH = 550;
    public static final double HEIGHT = 700;

    // optional

    public static final double PADDLE_W = 120;
    public static final double PADDLE_H = 16;
    public static final double PADDLE_Y = HEIGHT - 40;
    public static final double PADDLE_SPEED = 360; // px/s

    public static final double BALL_R = 8;
    public static final double BALL_SPEED = 260; // base px/s

    public static final int BRICK_ROWS = 6;
    public static final int BRICK_COLS = 10;
    public static final double BRICK_W = 50;
    public static final double BRICK_H = 30;
    public static final double BRICK_GAP = 3;
    public static final double BRICK_OFFSET_X = (WIDTH - (BRICK_W * BRICK_COLS + BRICK_GAP * (BRICK_COLS - 1))) / 2;  // căn giữa đẹp
    public static final double BRICK_OFFSET_Y = 60;
}
