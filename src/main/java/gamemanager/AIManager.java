package gamemanager;

import gameobject.Ball;
import gameobject.Paddle;
import gameobject.Powerup;

import java.util.List;
import java.util.Random;

import static gameconfig.GameConfig.*;

/**
 * AIManager handles AI-controlled paddle movement.
 * The AI predicts where the ball will land and moves the paddle accordingly.
 * Features include:
 * - Ball landing prediction with wall bounce simulation
 * - Powerup catching behavior
 * - Confusion state when multiple balls approach
 * - Drift behavior when no threats are present
 */
public class AIManager {
    private static final double DRIFT_SPEED_MULTIPLIER = 0.4;
    private static final double DRIFT_MIN_RANGE = 0.2;
    private static final double DRIFT_MAX_RANGE = 0.8;
    private static final int MISTAKE_RECALC_INTERVAL = 60;
    private static final int CONFUSION_DURATION_FRAMES = 60;
    private static final int DANGEROUS_BALL_THRESHOLD = 2;
    private static final double CONFUSION_CHANCE = 0.10;
    private static final double MISTAKE_FACTOR = 0.4;
    private static final double CATCHABLE_DISTANCE_THRESHOLD = 150.0;
    private static final double MIN_MOVEMENT_THRESHOLD = 1.0;
    private static final double DESTINATION_REACHED_THRESHOLD = 5.0;


    private final Paddle paddle;
    private final Random random;
    private double targetX;
    private double mistakeOffset;
    private int mistakeRecalcCounter;
    private double driftTargetX;
    private boolean isConfused;
    private int confusionDuration;
    private boolean wasInDriftMode;

    /**
     * Constructor for AIManager.
     *
     * @param paddle The paddle controlled by the AI.
     */
    public AIManager(Paddle paddle) {
        this.paddle = paddle;
        this.random = new Random();
        this.targetX = getPaddleCenterX();
        this.mistakeOffset = 0;
        this.mistakeRecalcCounter = 0;
        this.isConfused = false;
        this.confusionDuration = 0;
        this.wasInDriftMode = true;
        calculateDriftTarget();
    }


    public void update(List<Ball> balls, List<Powerup> powerups, double tpf) {
        if (balls.isEmpty()) {
            handleDriftBehavior(tpf);
            return;
        }

        if (handleConfusionState(tpf)) {
            return;
        }

        if (handlePowerupCatching(powerups, tpf)) {
            return;
        }

        if (handleMultipleBallConfusion(balls, tpf)) {
            return;
        }

        Ball dangerousBall = findMostDangerousBall(balls);
        if (dangerousBall == null) {
            handleDriftBehavior(tpf);
            return;
        }

        handleBallTracking(dangerousBall, tpf);
    }


    private boolean handleConfusionState(double tpf) {
        if (!isConfused) {
            return false;
        }

        confusionDuration--;
        if (confusionDuration <= 0) {
            isConfused = false;
        }
        handleDriftBehavior(tpf);
        return true;
    }

    private boolean handlePowerupCatching(List<Powerup> powerups, double tpf) {
        Powerup targetPowerup = findCatchablePowerup(powerups);
        if (targetPowerup == null) {
            return false;
        }

        wasInDriftMode = false;
        double powerupCenterX = targetPowerup.getX() + targetPowerup.getWidth() / 2;
        targetX = clampTargetX(powerupCenterX);
        movePaddleToTarget(tpf);
        return true;
    }

    private boolean handleMultipleBallConfusion(List<Ball> balls, double tpf) {
        int dangerousBallCount = countDangerousBalls(balls);

        if (dangerousBallCount >= DANGEROUS_BALL_THRESHOLD && random.nextDouble() < CONFUSION_CHANCE) {
            triggerConfusion();
            handleDriftBehavior(tpf);
            return true;
        }
        return false;
    }

    private void handleBallTracking(Ball ball, double tpf) {
        if (ball.getVy() <= 0) {
            return;
        }

        wasInDriftMode = false;
        updateMistakeOffset();

        double predictedX = predictBallLandingX(ball) + mistakeOffset;
        targetX = clampTargetX(predictedX);

        movePaddleToTarget(tpf);
    }

    private Ball findMostDangerousBall(List<Ball> balls) {

        Ball mostDangerous = null;
        double closestDistance = Double.MAX_VALUE;

        for (Ball ball : balls) {
            if (isBallMovingDown(ball)) {
                double distance = calculateDistanceToPaddle(ball);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    mostDangerous = ball;
                }
            }
        }

        return mostDangerous;
    }

    private int countDangerousBalls(List<Ball> balls) {
        int count = 0;
        for (Ball ball : balls) {
            if (isBallMovingDown(ball)) {
                count++;
            }
        }
        return count;
    }

    private boolean isBallMovingDown(Ball ball) {
        return ball.getVy() > 0;
    }

    private double calculateDistanceToPaddle(Ball ball) {

        return paddle.getY() - (ball.getY() + ball.getRadius() * 2);
    }

    private Powerup findCatchablePowerup(List<Powerup> powerups) {
        if (powerups == null || powerups.isEmpty()) {
            return null;
        }

        Powerup bestPowerup = null;
        double closestDistance = Double.MAX_VALUE;

        for (Powerup powerup : powerups) {
            double distance = paddle.getY() - powerup.getY();

            if (isPowerupCatchable(distance) && distance < closestDistance) {
                closestDistance = distance;
                bestPowerup = powerup;
            }
        }

        return bestPowerup;
    }

    private boolean isPowerupCatchable(double distance) {
        return distance > 0 && distance < CATCHABLE_DISTANCE_THRESHOLD;
    }

    private void triggerConfusion() {
        isConfused = true;
        confusionDuration = CONFUSION_DURATION_FRAMES;
        calculateDriftTarget();
    }

    private void handleDriftBehavior(double tpf) {
        if (!wasInDriftMode) {
            calculateDriftTarget();
            wasInDriftMode = true;
        }

        double paddleCenterX = getPaddleCenterX();

        if (Math.abs(driftTargetX - paddleCenterX) <= DESTINATION_REACHED_THRESHOLD) {
            calculateDriftTarget();
        }

        movePaddleToDriftTarget(tpf);
    }

    private void calculateDriftTarget() {
        double minX = GAME_WIDTH * DRIFT_MIN_RANGE;
        double maxX = GAME_WIDTH * DRIFT_MAX_RANGE;
        driftTargetX = minX + (random.nextDouble() * (maxX - minX));
    }

    private void movePaddleToDriftTarget(double tpf) {
        double paddleCenterX = getPaddleCenterX();
        double difference = driftTargetX - paddleCenterX;

        if (Math.abs(difference) > MIN_MOVEMENT_THRESHOLD) {
            double driftSpeed = PADDLE_SPEED * DRIFT_SPEED_MULTIPLIER * tpf * 60;

            if (difference < 0) {
                double newX = Math.max(0, paddle.getX() - driftSpeed);
                paddle.setX(newX);
            } else {
                double newX = Math.min(GAME_WIDTH - paddle.getWidth(), paddle.getX() + driftSpeed);
                paddle.setX(newX);
            }
        }
    }

    private double predictBallLandingX(Ball ball) {
        double ballX = ball.getX() + ball.getRadius();
        double ballY = ball.getY() + ball.getRadius();
        double vx = ball.getVx();
        double vy = ball.getVy();

        if (vy <= 0) {
            return ballX;
        }

        double paddleY = paddle.getY();
        double timeToReach = (paddleY - ballY) / vy;
        double predictedX = ballX + (vx * timeToReach);

        predictedX = simulateWallBounces(predictedX, vx, ball.getRadius());

        return predictedX;
    }

    private double simulateWallBounces(double predictedX, double vx, double radius) {

        while (predictedX < radius || predictedX > GAME_WIDTH - radius) {
            if (predictedX < radius) {
                predictedX = radius + (radius - predictedX);
                vx = -vx;
            } else if (predictedX > GAME_WIDTH - radius) {
                predictedX = (GAME_WIDTH - radius) - (predictedX - (GAME_WIDTH - radius));
                vx = -vx;
            }
        }
        return predictedX;
    }

    private void updateMistakeOffset() {
        mistakeRecalcCounter++;
        if (mistakeRecalcCounter >= MISTAKE_RECALC_INTERVAL) {
            calculateMistakeOffset();
            mistakeRecalcCounter = 0;
        }
    }

    private void calculateMistakeOffset() {
        double maxMistake = paddle.getWidth() * MISTAKE_FACTOR;
        mistakeOffset = (random.nextDouble() * 2 - 1) * maxMistake;
    }

    private void movePaddleToTarget(double tpf) {

        double paddleCenterX = getPaddleCenterX();
        double difference = targetX - paddleCenterX;

        if (Math.abs(difference) > MIN_MOVEMENT_THRESHOLD) {
            if (difference < 0) {
                paddle.moveLeft(tpf);
            } else {
                paddle.moveRight(tpf);
            }
        }
    }

    private double clampTargetX(double x) {
        double halfPaddleWidth = paddle.getWidth() / 2;
        return Math.max(halfPaddleWidth, Math.min(GAME_WIDTH - halfPaddleWidth, x));
    }

    private double getPaddleCenterX() {
        return paddle.getX() + paddle.getWidth() / 2;
    }
}

