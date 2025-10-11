package gamemanager;

import gameconfig.GameConfig;
import gameobject.Ball;
import gameobject.Brick;
import gameobject.Paddle;
import gameobject.Powerup;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;

import java.util.List;

public class CollisionManager {
    private final LevelManager levelManager;
    private final Pane root;
    private boolean oneshotActive = false;

    public CollisionManager(LevelManager levelManager, Pane root) {
        this.levelManager = levelManager;
        this.root = root;
    }

    public void setOneshotActive(boolean active) {
        this.oneshotActive = active;
    }

    public void handlePaddleBallCollision(Paddle paddle, Ball ball) {
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

    public void handleBrickBallCollision(Ball ball, Brick brick, userinterface.UserInterface ui) {
        Point2D ballCenter = new Point2D(ball.getX() + ball.getRadius(), ball.getY() + ball.getRadius());
        double closestX = Math.max(brick.getX(), Math.min(ballCenter.getX(), brick.getRightX()));
        double closestY = Math.max(brick.getY(), Math.min(ballCenter.getY(), brick.getBottomY()));
        double dx = ballCenter.getX() - closestX;
        double dy = ballCenter.getY() - closestY;
        if (dx * dx + dy * dy <= ball.getRadius() * ball.getRadius()) {
            if (Math.abs(dx) > Math.abs(dy)) {
                ball.bounce(GameConfig.WallSideType.EAST);
            } else {
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
                levelManager.removeBrick(brick, root);
            }
        }
    }

    public GameConfig.WallSideType checkWallCollision(Ball ball, double gameWidth, double gameHeight) {
        if (ball.getY() <= 0) {
            ball.bounce(GameConfig.WallSideType.NORTH);
            return GameConfig.WallSideType.NORTH;
        }
        if (ball.getX() <= 0) {
            ball.bounce(GameConfig.WallSideType.WEST);
            return GameConfig.WallSideType.WEST;
        }
        if (ball.getRightX() >= gameWidth) {
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
            if (ball.getRightX() > brick.getX() &&
                    ball.getX() < brick.getRightX() &&
                    ball.getBottomY() > brick.getY() &&
                    ball.getY() < brick.getBottomY()) {
                return brick;
            }
        }
        return null;
    }
}
