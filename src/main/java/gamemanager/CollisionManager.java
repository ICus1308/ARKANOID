package gamemanager;

import gameconfig.GameConfig;
import gameobject.Ball;
import gameobject.Brick;
import gameobject.Paddle;
import gameobject.Powerup;
import javafx.scene.layout.Pane;
import userinterface.SingleplayerScreen;

import java.util.List;

public class CollisionManager {
    private final LevelManager levelManager;
    private final Pane root;
    private boolean oneshotActive = false;
    private CoinManager coinManager; // optional

    public CollisionManager(LevelManager levelManager, Pane root) {
        this.levelManager = levelManager;
        this.root = root;
    }

    public void setCoinManager(CoinManager coinManager) {
        this.coinManager = coinManager;
    }

    public void setOneshotActive(boolean active) {
        this.oneshotActive = active;
    }

    public void handlePaddleBallCollision(Paddle paddle, Ball ball) {
        javafx.geometry.Bounds ballBounds = ball.getShape().getBoundsInParent();
        javafx.geometry.Bounds paddleBounds = paddle.getNode().getBoundsInParent();

        double overlapY = ballBounds.getMaxY() - paddleBounds.getMinY();

        ball.setY(ball.getY() - overlapY);

        double relativeIntersectX = (paddle.getX() + (paddle.getWidth() / 2)) - (ball.getX() + ball.getRadius());
        double normalizedRelativeIntersectionX = relativeIntersectX / (paddle.getWidth() / 2);
        double angleAdjustment = normalizedRelativeIntersectionX * ball.speed * 1;
        ball.setVy(-Math.abs(ball.getVy()));
        ball.setVx(ball.getVx() - angleAdjustment);
        double currentSpeed = Math.sqrt(ball.getVx() * ball.getVx() + ball.getVy() * ball.getVy());
        double factor = ball.speed / currentSpeed;
        ball.setVx(ball.getVx() * factor);
        ball.setVy(ball.getVy() * factor);
    }

    public void handleBrickBallCollision(Ball ball, Brick brick, SingleplayerScreen ui) {
        javafx.geometry.Bounds ballBounds = ball.getShape().getBoundsInParent();
        javafx.geometry.Bounds brickBounds = brick.getNode().getBoundsInParent();


        double ballCenterX = ballBounds.getMinX() + ballBounds.getWidth() / 2;
        double ballCenterY = ballBounds.getMinY() + ballBounds.getHeight() / 2;
        double brickCenterX = brickBounds.getMinX() + brickBounds.getWidth() / 2;
        double brickCenterY = brickBounds.getMinY() + brickBounds.getHeight() / 2;


        double halfWidths = (ballBounds.getWidth() + brickBounds.getWidth()) / 2;
        double halfHeights = (ballBounds.getHeight() + brickBounds.getHeight()) / 2;


        double dx = ballCenterX - brickCenterX;
        double dy = ballCenterY - brickCenterY;

        double overlapX = halfWidths - Math.abs(dx);
        double overlapY = halfHeights - Math.abs(dy);

        if (overlapX < overlapY) {
            if (dx > 0) {
                ball.setX(ball.getX() + overlapX);
            } else {
                ball.setX(ball.getX() - overlapX);
            }
            ball.bounce(GameConfig.WallSideType.EAST);
        } else {
            if (dy > 0) {
                ball.setY(ball.getY() + overlapY);
            } else {
                ball.setY(ball.getY() - overlapY);
            }
            ball.bounce(GameConfig.WallSideType.NORTH);
        }

        int score;
        if (oneshotActive) {
            if (brick.getHitCount() > 0) {
                score = 10;
                brick.destroy();
            } else {
                score = 0;
            }
        } else {
            score = brick.hit();
        }
        ui.increaseScore(score);
        brick.updateDraw();
        if (brick.getHitCount() == 0) {
            // award coins when a brick is destroyed
            if (coinManager != null) {
                coinManager.addCoins(5);
            }
            levelManager.removeBrick(brick, root);
        }
    }

    public GameConfig.WallSideType checkWallCollision(Ball ball, double gameWidth, double gameHeight) {
        if (ball.getY() <= 0) {
            ball.setY(0);
            ball.bounce(GameConfig.WallSideType.NORTH);
            return GameConfig.WallSideType.NORTH;
        }
        if (ball.getX() <= 0) {
            ball.setX(0);
            ball.bounce(GameConfig.WallSideType.WEST);
            return GameConfig.WallSideType.WEST;
        }
        if (ball.getRightX() >= gameWidth) {
            ball.setX(gameWidth - ball.getWidth());
            ball.bounce(GameConfig.WallSideType.EAST);
            return GameConfig.WallSideType.EAST;
        }
        if (ball.getY() >= gameHeight) {
            return GameConfig.WallSideType.BOTTOM_HIT;
        }
        return null;
    }

    public boolean checkPaddleBallCollision(Paddle paddle, Ball ball) {
        return ball.getBottomY() >= paddle.getY() &&
                ball.getX() < paddle.getRightX() &&
                ball.getRightX() > paddle.getX() &&
                ball.getBottomY() < paddle.getBottomY() + ball.getVy() * 60;
    }

    public boolean checkPaddlePowerupCollision(Paddle paddle, Powerup powerup) {
        return paddle.getRightX() > powerup.getX() &&
                paddle.getX() < powerup.getRightX() &&
                paddle.getBottomY() > powerup.getY() &&
                paddle.getY() < powerup.getBottomY();
    }

    public Brick checkBrickBallCollision(Ball ball, List<Brick> bricks) {
        for (Brick brick : bricks) {
            if (ball.getShape().getBoundsInParent().intersects(brick.getNode().getBoundsInParent())) {
                return brick;
            }
        }
        return null;
    }
}
