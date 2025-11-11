package gamemanager.core;

import gameobject.ball.Ball;
import gameobject.paddle.Paddle;
import gameobject.powerup.Powerup;

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
     * KHỞI TẠO AI CHO BOT
     * @param paddle Paddle mà bot sẽ điều khiển
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

    /**
     * CẬP NHẬT AI MỖI FRAME - PHƯƠNG THỨC CHÍNH
     *
     * LUỒNG XỬ LÝ (theo thứ tự ưu tiên):
     * 1. Nếu không có bóng → DRIFT (lang thang)
     * 2. Nếu đang CONFUSED → tiếp tục drift
     * 3. Nếu có powerup gần → ưu tiên BẮT POWERUP
     * 4. Nếu có nhiều bóng nguy hiểm → có thể bị CONFUSED
     * 5. Tìm bóng nguy hiểm nhất → TRACK (theo dõi và chặn)
     *
     * @param balls Danh sách bóng trong game
     * @param powerups Danh sách power-up đang rơi
     * @param tpf Time per frame (thời gian giữa 2 frame)
     */
    public void update(List<Ball> balls, List<Powerup> powerups, double tpf) {
        // Không có bóng → lang thang
        if (balls.isEmpty()) {
            handleDriftBehavior(tpf);
            return;
        }

        // Đang bối rối → tiếp tục lang thang
        if (handleConfusionState(tpf)) {
            return;
        }

        // Ưu tiên bắt power-up nếu gần
        if (handlePowerupCatching(powerups, tpf)) {
            return;
        }

        // Kiểm tra có quá nhiều bóng không → có thể bối rối
        if (handleMultipleBallConfusion(balls, tpf)) {
            return;
        }

        // Tìm bóng nguy hiểm nhất và theo dõi
        Ball dangerousBall = findMostDangerousBall(balls);
        if (dangerousBall == null) {
            handleDriftBehavior(tpf);
            return;
        }

        handleBallTracking(dangerousBall, tpf);
    }

    /**
     * XỬ LÝ TRẠNG THÁI BỐI RỐI
     * - Bot sẽ lang thang không mục đích
     * - Giảm dần thời gian bối rối
     */
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

    /**
     * XỬ LÝ BẮT POWER-UP
     * - Tìm power-up gần nhất có thể bắt được
     * - Di chuyển paddle đến vị trí power-up
     *
     * @return true nếu đang bắt power-up, false nếu không có power-up nào
     */
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

    /**
     * XỬ LÝ BỐI RỐI KHI CÓ NHIỀU BÓNG
     * - Đếm số bóng nguy hiểm (đang bay về phía bot)
     * - Nếu ≥ 2 bóng và random < 10% → kích hoạt confusion
     */
    private boolean handleMultipleBallConfusion(List<Ball> balls, double tpf) {
        int dangerousBallCount = countDangerousBalls(balls);

        if (dangerousBallCount >= DANGEROUS_BALL_THRESHOLD && random.nextDouble() < CONFUSION_CHANCE) {
            triggerConfusion();
            handleDriftBehavior(tpf);
            return true;
        }
        return false;
    }

    /**
     * XỬ LÝ THEO DÕI BÓNG - LOGIC CHÍNH!
     *
     * THUẬT TOÁN:
     * 1. Kiểm tra bóng có bay xuống không (vy < 0 = bay xuống)
     * 2. Cập nhật sai số cố ý (mistake offset) mỗi 60 frame
     * 3. Dự đoán điểm rơi của bóng (predictBallLandingX)
     * 4. Thêm sai số vào vị trí dự đoán
     * 5. Di chuyển paddle đến vị trí mục tiêu
     */
    private void handleBallTracking(Ball ball, double tpf) {
        // Nếu bóng bay lên (không nguy hiểm) → bỏ qua
        if (ball.getVy() >= 0) {
            return;
        }

        wasInDriftMode = false;
        updateMistakeOffset(); // Cập nhật sai số

        // DỰ ĐOÁN ĐIỂM RƠI + THÊM SAI SỐ
        double predictedX = predictBallLandingX(ball) + mistakeOffset;
        targetX = clampTargetX(predictedX);

        movePaddleToTarget(tpf);
    }

    /**
     * TÌM BÓNG NGUY HIỂM NHẤT
     * - Bóng nguy hiểm = bóng bay xuống về phía bot
     * - Ưu tiên bóng gần nhất
     *
     * @return Bóng nguy hiểm nhất hoặc null
     */
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

    /**
     * ĐẾM SỐ BÓNG NGUY HIỂM
     */
    private int countDangerousBalls(List<Ball> balls) {
        int count = 0;
        for (Ball ball : balls) {
            if (isBallMovingDown(ball)) {
                count++;
            }
        }
        return count;
    }

    /**
     * KIỂM TRA BÓNG CÓ BAY XUỐNG KHÔNG
     * - vy < 0 = bay xuống (về phía bot)
     * - vy > 0 = bay lên (an toàn)
     */
    private boolean isBallMovingDown(Ball ball) {
        return ball.getVy() < 0;
    }

    /**
     * TÍNH KHOẢNG CÁCH TỪ BÓNG ĐẾN PADDLE
     */
    private double calculateDistanceToPaddle(Ball ball) {

        return paddle.getY() - (ball.getY() + ball.getRadius() * 2);
    }

    /**
     * TÌM POWER-UP CÓ THỂ BẮT ĐƯỢC
     * - Power-up gần nhất trong bán kính 150px
     */
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

    /**
     * KÍCH HOẠT TRẠNG THÁI BỐI RỐI
     */
    private void triggerConfusion() {
        isConfused = true;
        confusionDuration = CONFUSION_DURATION_FRAMES;
        calculateDriftTarget();
    }

    /**
     * XỬ LÝ LANG THANG
     * - Bot di chuyển ngẫu nhiên trong khoảng 20%-80% màn hình
     * - Tốc độ chậm hơn bình thường (40%)
     */
    private void handleDriftBehavior(double tpf) {
        // Nếu chưa có mục tiêu drift → tính toán mục tiêu mới
        if (!wasInDriftMode) {
            calculateDriftTarget();
            wasInDriftMode = true;
        }

        double paddleCenterX = getPaddleCenterX();

        // Đã đến mục tiêu → chọn mục tiêu mới
        if (Math.abs(driftTargetX - paddleCenterX) <= DESTINATION_REACHED_THRESHOLD) {
            calculateDriftTarget();
        }

        movePaddleToDriftTarget(tpf);
    }

    /**
     * TÍNH TOÁN MỤC TIÊU LANG THANG NGẪU NHIÊN
     * - Random vị trí trong khoảng 20%-80% chiều rộng màn hình
     */
    private void calculateDriftTarget() {
        double minX = GAME_WIDTH * DRIFT_MIN_RANGE;
        double maxX = GAME_WIDTH * DRIFT_MAX_RANGE;
        driftTargetX = minX + (random.nextDouble() * (maxX - minX));
    }

    /**
     * DI CHUYỂN PADDLE ĐẾN MỤC TIÊU DRIFT (CHẬM)
     */
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

    /**
     * DỰ ĐOÁN ĐIỂM RƠI CỦA BÓNG - THUẬT TOÁN QUAN TRỌNG NHẤT!
     *
     * CÔNG THỨC VẬT LÝ:
     * 1. Thời gian bóng rơi đến paddle:
     *    t = (paddleY - ballY) / vy
     *
     * 2. Vị trí X dự đoán (chưa tính nảy tường):
     *    predictedX = ballX + vx * t
     *
     * 3. Mô phỏng nảy tường:
     *    - Nếu predictedX < 0 → bóng chạm tường trái → nảy lại
     *    - Nếu predictedX > GAME_WIDTH → chạm tường phải → nảy lại
     *    - Tính toán vị trí sau khi nảy (có thể nảy nhiều lần!)
     *
     * VÍ DỤ:
     * - Bóng ở x=100, vx=5, vy=-10
     * - Paddle ở y=500, bóng ở y=100
     * - Thời gian: t = (500-100)/10 = 40 frame
     * - Dự đoán: x = 100 + 5*40 = 300
     * - Nếu 300 < GAME_WIDTH → không nảy → predictedX = 300
     *
     * @param ball Bóng cần dự đoán
     * @return Vị trí X mà bóng sẽ rơi
     */
    private double predictBallLandingX(Ball ball) {
        double ballX = ball.getX() + ball.getRadius();
        double ballY = ball.getY() + ball.getRadius();
        double vx = ball.getVx();
        double vy = ball.getVy();

        // Bóng bay lên → không dự đoán được
        if (vy >= 0) {
            return ballX;
        }

        // Tính thời gian bóng rơi đến paddle
        double paddleY = paddle.getY();
        double timeToReach = (paddleY - ballY) / vy;

        // Dự đoán vị trí X
        double predictedX = ballX + (vx * timeToReach);

        // Mô phỏng nảy tường
        predictedX = simulateWallBounces(predictedX, vx, ball.getRadius());

        return predictedX;
    }

    /**
     * MÔ PHỎNG PHẢN XẠ TƯỜNG - PHẦN PHỨC TẠP NHẤT!
     *
     * LOGIC:
     * - Bóng bay ra ngoài màn hình → tính vị trí sau khi nảy
     * - CÓ THỂ NẢY NHIỀU LẦN! (vd: vx rất lớn)
     * - Lặp cho đến khi bóng nằm trong màn hình
     *
     * CÔNG THỨC NẢY TƯỜNG TRÁI (x < 0):
     * - Bóng "thừa" ra ngoài: overflow = 0 - x
     * - Vị trí sau nảy: x = 0 + overflow = -x
     * - Đảo vận tốc: vx = -vx
     *
     * CÔNG THỨC NẢY TƯỜNG PHẢI (x > GAME_WIDTH):
     * - Overflow = x - GAME_WIDTH
     * - Vị trí sau nảy: x = GAME_WIDTH - overflow
     *
     * VÍ DỤ:
     * - GAME_WIDTH = 800, predictedX = 850, vx = 10
     * - Overflow = 850 - 800 = 50
     * - Sau nảy: x = 800 - 50 = 750
     * - Đảo vận tốc: vx = -10
     *
     * @param predictedX Vị trí dự đoán ban đầu
     * @param vx Vận tốc ngang của bóng
     * @param radius Bán kính bóng
     * @return Vị trí X sau khi mô phỏng nảy tường
     */
    private double simulateWallBounces(double predictedX, double vx, double radius) {
        // Lặp cho đến khi bóng nằm trong màn hình
        while (predictedX < radius || predictedX > GAME_WIDTH - radius) {
            if (predictedX < radius) {
                // NẢY TƯỜNG TRÁI
                predictedX = radius + (radius - predictedX);
                vx = -vx;
            } else if (predictedX > GAME_WIDTH - radius) {
                // NẢY TƯỜNG PHẢI
                predictedX = (GAME_WIDTH - radius) - (predictedX - (GAME_WIDTH - radius));
                vx = -vx;
            }
        }
        return predictedX;
    }

    /**
     * CẬP NHẬT SAI SỐ CỐ Ý
     * - Tính lại mỗi 60 frame để bot không quá hoàn hảo
     * - Sai số = ±40% chiều rộng paddle
     */
    private void updateMistakeOffset() {
        mistakeRecalcCounter++;
        if (mistakeRecalcCounter >= MISTAKE_RECALC_INTERVAL) {
            calculateMistakeOffset();
            mistakeRecalcCounter = 0;
        }
    }

    /**
     * TÍNH TOÁN SAI SỐ CỐ Ý (để bot không quá giỏi)
     * - Random sai số trong khoảng [-40%, +40%] chiều rộng paddle
     * - Bot sẽ đứng lệch một chút so với vị trí hoàn hảo
     */
    private void calculateMistakeOffset() {
        double maxMistake = paddle.getWidth() * MISTAKE_FACTOR;
        mistakeOffset = (random.nextDouble() * 2 - 1) * maxMistake;
    }

    /**
     * DI CHUYỂN PADDLE ĐẾN VỊ TRÍ MỤC TIÊU
     * - So sánh vị trí hiện tại với mục tiêu
     * - Di chuyển trái/phải tương ứng
     */
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

    /**
     * GIỚI HẠN VỊ TRÍ MỤC TIÊU TRONG MÀN HÌNH
     * - Đảm bảo paddle không ra ngoài màn hình
     */
    private double clampTargetX(double x) {
        double halfPaddleWidth = paddle.getWidth() / 2;
        return Math.max(halfPaddleWidth, Math.min(GAME_WIDTH - halfPaddleWidth, x));
    }

    /**
     * LẤY VỊ TRÍ TRUNG TÂM CỦA PADDLE
     */
    private double getPaddleCenterX() {
        return paddle.getX() + paddle.getWidth() / 2;
    }
}

