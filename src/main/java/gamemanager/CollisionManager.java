package gamemanager;

import gameconfig.GameConfig;
import gameobject.Ball;
import gameobject.Brick;
import gameobject.Paddle;
import gameobject.Powerup;
import javafx.scene.layout.Pane;
import userinterface.GameScreen;

import java.util.List;

public class CollisionManager extends GamePlay {
    private final LevelManager levelManager;
    private final Pane root;
    private boolean oneshotActive = false;
    private CoinManager coinManager;
    private ScoreManager scoreManager;

    public CollisionManager(LevelManager levelManager, Pane root) {
        this.levelManager = levelManager;
        this.root = root;
    }

    public void setCoinManager(CoinManager coinManager) {
        this.coinManager = coinManager;
    }

    public void setScoreManager(ScoreManager scoreManager) {
        this.scoreManager = scoreManager;
    }

    public void setOneshotActive(boolean active) {
        this.oneshotActive = active;
    }

    public boolean isOneshotActive() {
        return this.oneshotActive;
    }

    // Helper: circle-rect intersection test using circle center and radius and rectangle bounds
    private boolean circleIntersectsRect(double cx, double cy, double radius, javafx.geometry.Bounds rect) {
        double closestX = Math.max(rect.getMinX(), Math.min(cx, rect.getMaxX()));
        double closestY = Math.max(rect.getMinY(), Math.min(cy, rect.getMaxY()));
        double dx = cx - closestX;
        double dy = cy - closestY;
        return dx * dx + dy * dy <= radius * radius;
    }

    public void handlePaddleBallCollision(Paddle paddle, Ball ball) {
        javafx.geometry.Bounds paddleBounds = paddle.getNode().getBoundsInParent();
        double ballCenterX = ball.getX() + ball.getRadius();
        double ballCenterY = ball.getY() + ball.getRadius();
        double r = ball.getRadius();

        // Use circle-rect collision resolution similar to brick collision
        double paddleCenterX = paddleBounds.getMinX() + paddleBounds.getWidth() / 2.0;
        double paddleCenterY = paddleBounds.getMinY() + paddleBounds.getHeight() / 2.0;

        double halfWidths = (r * 2 + paddleBounds.getWidth()) / 2.0;
        double halfHeights = (r * 2 + paddleBounds.getHeight()) / 2.0;

        double dx = ballCenterX - paddleCenterX;
        double dy = ballCenterY - paddleCenterY;

        double overlapX = halfWidths - Math.abs(dx);
        double overlapY = halfHeights - Math.abs(dy);

        if (overlapX <= 0 || overlapY <= 0) return; // no collision

        // Determine collision side and respond
        if (overlapX < overlapY) {
            // horizontal collision
            if (dx > 0) {
                ball.setX(ball.getX() + overlapX);
                ball.setVx(Math.abs(ball.getVx()));
            } else {
                ball.setX(ball.getX() - overlapX);
                ball.setVx(-Math.abs(ball.getVx()));
            }
        } else {
            // vertical collision (top of paddle)
            if (dy > 0) {
                ball.setY(ball.getY() + overlapY);
            } else {
                ball.setY(ball.getY() - overlapY);
            }

            // reflect vertically and tweak horizontal velocity based on where the ball hit the paddle
            ball.setVy(-Math.abs(ball.getVy()));

            double relativeIntersectX = (paddle.getX() + (paddle.getWidth() / 2.0)) - (ball.getX() + ball.getRadius());
            double normalizedRelativeIntersectionX = 0;
            if (paddle.getWidth() != 0) {
                normalizedRelativeIntersectionX = relativeIntersectX / (paddle.getWidth() / 2.0);
            }
            double angleAdjustment = normalizedRelativeIntersectionX * ball.speed * 1;
            ball.setVx(ball.getVx() - angleAdjustment);

            // normalize speed to maintain constant ball.speed
            double currentSpeed = Math.sqrt(ball.getVx() * ball.getVx() + ball.getVy() * ball.getVy());
            if (currentSpeed != 0) {
                double factor = ball.speed / currentSpeed;
                ball.setVx(ball.getVx() * factor);
                ball.setVy(ball.getVy() * factor);
            }
        }
    }
    // Handle collision between ball and brick
    public void handleBrickBallCollision(Ball ball, Brick brick, GameScreen ui) {
        javafx.geometry.Bounds brickBounds = brick.getNode().getBoundsInParent();

        double ballCenterX = ball.getX() + ball.getRadius();
        double ballCenterY = ball.getY() + ball.getRadius();
        double brickCenterX = brickBounds.getMinX() + brickBounds.getWidth() / 2.0;
        double brickCenterY = brickBounds.getMinY() + brickBounds.getHeight() / 2.0;

        double halfWidths = (ball.getRadius() * 2 + brickBounds.getWidth()) / 2.0;
        double halfHeights = (ball.getRadius() * 2 + brickBounds.getHeight()) / 2.0;

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
        if (scoreManager != null) {
            score = scoreManager.calculateBrickScore(brick, oneshotActive);
        } else {
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
        }
        ui.increaseScore(score);
        brick.updateDraw();
        if (brick.getHitCount() == 0) {
            if (coinManager != null) {
                coinManager.addCoins(5);
                ui.updateCoins();
            }
            levelManager.removeBrick(brick, root);
        }
    }
    // Check collision between ball and walls
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
        javafx.geometry.Bounds paddleBounds = paddle.getNode().getBoundsInParent();
        double ballCenterX = ball.getX() + ball.getRadius();
        double ballCenterY = ball.getY() + ball.getRadius();
        return circleIntersectsRect(ballCenterX, ballCenterY, ball.getRadius(), paddleBounds);
    }

    public boolean checkPaddlePowerupCollision(Paddle paddle, Powerup powerup) {
        return paddle.getRightX() > powerup.getX() &&
                paddle.getX() < powerup.getRightX() &&
                paddle.getBottomY() > powerup.getY() &&
                paddle.getY() < powerup.getBottomY();
    }

    public Brick checkBrickBallCollision(Ball ball, List<Brick> bricks) {
        double ballCenterX = ball.getX() + ball.getRadius();
        double ballCenterY = ball.getY() + ball.getRadius();
        double r = ball.getRadius();
        for (Brick brick : bricks) {
            javafx.geometry.Bounds brickBounds = brick.getNode().getBoundsInParent();
            if (circleIntersectsRect(ballCenterX, ballCenterY, r, brickBounds)) {
                return brick;
            }
        }
        return null;
    }
}
