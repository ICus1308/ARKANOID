package gamemanager.manager;

import gameconfig.GameConfig;
import gamemanager.core.LevelManager;
import gameobject.ball.Ball;
import gameobject.brick.Brick;
import gameobject.paddle.Paddle;
import gameobject.powerup.Powerup;
import javafx.scene.layout.Pane;
import userinterface.gamescreen.GameScreen;
import userinterface.gamescreen.BotScreen;
import userinterface.gamescreen.OneVOneScreen;

import java.util.List;

/**
 * CollisionManager - Quản lý tất cả va chạm trong game
 *
 * CHỨC NĂNG CHÍNH:
 * - Va chạm bóng vs paddle (phản xạ góc dựa theo vị trí chạm)
 * - Va chạm bóng vs brick (tính điểm, spawn power-up)
 * - Va chạm bóng vs tường (phản xạ)
 * - Va chạm paddle vs power-up (kích hoạt hiệu ứng)
 */
public class CollisionManager {
    private final LevelManager levelManager;
    private final Pane root;
    private boolean oneshotActive = false; // Chế độ oneshot: bóng phá gạch 1 phát
    private CoinManager coinManager;
    private ScoreManager scoreManager;
    private PowerUpManager powerUpManager; // Quản lý spawn power-up

    public CollisionManager(LevelManager levelManager, Pane root) {
        this.levelManager = levelManager;
        this.root = root;
        this.powerUpManager = new PowerUpManager(root);
    }

    public void setCoinManager(CoinManager coinManager) {
        this.coinManager = coinManager;
    }

    public void setScoreManager(ScoreManager scoreManager) {
        this.scoreManager = scoreManager;
    }

    public PowerUpManager getPowerUpManager() {
        return powerUpManager;
    }

    public void setOneshotActive(boolean active) {
        this.oneshotActive = active;
    }

    public boolean isOneshotActive() {
        return this.oneshotActive;
    }

    /**
     * Xử lý va chạm BÓNG vs PADDLE - PHỨC TẠP NHẤT!
     *
     * THUẬT TOÁN:
     * 1. Tính khoảng cách giữa tâm bóng và tâm paddle (dx, dy)
     * 2. Tính độ chồng lấn (overlap) theo 2 trục X và Y
     * 3. Nếu overlap X < overlap Y → va chạm từ bên trái/phải
     *    → Đẩy bóng ra ngoài theo trục X, đảo vận tốc Vx
     * 4. Nếu overlap Y < overlap X → va chạm từ trên/dưới
     *    → Đẩy bóng ra, đảo vận tốc Vy
     * 5. Điều chỉnh GÓC PHẢN XẠ dựa vào vị trí chạm trên paddle
     *    → Chạm ở rìa → góc lệch nhiều
     *    → Chạm ở giữa → góc thẳng
     *
     * @param paddle Paddle (thanh đỡ)
     * @param ball Bóng
     */
    public void handlePaddleBallCollision(Paddle paddle, Ball ball) {
        javafx.geometry.Bounds paddleBounds = paddle.getNode().getBoundsInParent();

        // Tính tọa độ tâm bóng
        double ballCenterX = ball.getX() + ball.getRadius();
        double ballCenterY = ball.getY() + ball.getRadius();
        double r = ball.getRadius();

        // Tính tọa độ tâm paddle
        double paddleCenterX = paddleBounds.getMinX() + paddleBounds.getWidth() / 2.0;
        double paddleCenterY = paddleBounds.getMinY() + paddleBounds.getHeight() / 2.0;

        // Tính "bán kính" tổng hợp (bán kính bóng + nửa chiều rộng/cao paddle)
        double halfWidths = (r * 2 + paddleBounds.getWidth()) / 2.0;
        double halfHeights = (r * 2 + paddleBounds.getHeight()) / 2.0;

        // Tính khoảng cách giữa 2 tâm
        double dx = ballCenterX - paddleCenterX;
        double dy = ballCenterY - paddleCenterY;

        // Tính độ CHỒNG LẤN (overlap) - phần bóng "chui vào" paddle
        double overlapX = halfWidths - Math.abs(dx);
        double overlapY = halfHeights - Math.abs(dy);

        // Nếu không có overlap → không va chạm
        if (overlapX <= 0 || overlapY <= 0) return;

        // XÁC ĐỊNH HƯỚNG VA CHẠM:
        // Nếu overlapX < overlapY → va chạm từ BÊN (trái/phải)
        if (overlapX < overlapY) {
            // Đẩy bóng ra khỏi paddle theo trục X
            ball.setX(dx > 0 ? ball.getX() + overlapX : ball.getX() - overlapX);
            // Đảo vận tốc X (phản xạ ngang)
            ball.setVx(dx > 0 ? Math.abs(ball.getVx()) : -Math.abs(ball.getVx()));
        } else {
            // Va chạm từ TRÊN/DƯỚI
            // Kiểm tra xem đây có phải paddle ở trên không (chế độ 1v1)
            boolean isTopPaddle = paddle.getY() < 100;

            // Đẩy bóng ra khỏi paddle theo trục Y
            ball.setY(dy > 0 ? ball.getY() + overlapY : ball.getY() - overlapY);
            // Đảo vận tốc Y (phản xạ dọc)
            ball.setVy(isTopPaddle ? Math.abs(ball.getVy()) : -Math.abs(ball.getVy()));

            // ===== ĐIỀU CHỈNH GÓC PHẢN XẠ - QUAN TRỌNG! =====
            // Tính vị trí chạm TƯƠNG ĐỐI trên paddle
            // relativeIntersectX = khoảng cách từ tâm paddle đến điểm chạm
            double relativeIntersectX = (paddle.getX() + paddle.getWidth() / 2.0) - (ball.getX() + ball.getRadius());

            // Chuẩn hóa về [-1, 1]:
            // -1 = chạm rìa trái
            //  0 = chạm chính giữa
            // +1 = chạm rìa phải
            double normalizedRelativeIntersectionX = paddle.getWidth() != 0 ? relativeIntersectX / (paddle.getWidth() / 2.0) : 0;

            // Điều chỉnh Vx dựa vào vị trí chạm
            // Chạm ở rìa → angleAdjustment lớn → góc phản xạ lệch nhiều
            double angleAdjustment = normalizedRelativeIntersectionX * ball.speed;
            ball.setVx(ball.getVx() - angleAdjustment);

            // GIỮ TỐC ĐỘ KHÔNG ĐỔI (chỉ thay đổi hướng)
            // Tính tốc độ hiện tại: v = sqrt(vx^2 + vy^2)
            double currentSpeed = Math.sqrt(ball.getVx() * ball.getVx() + ball.getVy() * ball.getVy());
            if (currentSpeed != 0) {
                // Scale lại vx, vy để tốc độ tổng = ball.speed
                double factor = ball.speed / currentSpeed;
                ball.setVx(ball.getVx() * factor);
                ball.setVy(ball.getVy() * factor);
            }
        }

        // Phát âm thanh va chạm
        SoundManager.getInstance().playSound(SoundManager.SoundType.BALL_PADDLE_HIT);
    }

    /**
     * Xử lý va chạm BÓNG vs BRICK
     *
     * LOGIC:
     * 1. Tính overlap theo 2 trục X, Y (giống paddle)
     * 2. Đẩy bóng ra, đảo vận tốc
     * 3. Tính điểm dựa vào loại brick
     * 4. Nếu brick bị phá hủy:
     *    - Phát âm thanh
     *    - Cộng coin
     *    - **SPAWN POWER-UP** (20% xác suất)
     *    - Xóa brick khỏi màn hình
     */
    public void handleBrickBallCollision(Ball ball, Brick brick, GameScreen ui) {
        javafx.geometry.Bounds brickBounds = brick.getNode().getBoundsInParent();

        // Tính tâm bóng và tâm brick
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

        // Xác định hướng va chạm và phản xạ
        if (overlapX < overlapY) {
            // Va chạm từ bên
            if (dx > 0) {
                ball.setX(ball.getX() + overlapX);
            } else {
                ball.setX(ball.getX() - overlapX);
            }
            ball.bounce(GameConfig.WallSideType.EAST);
        } else {
            // Va chạm từ trên/dưới
            if (dy > 0) {
                ball.setY(ball.getY() + overlapY);
            } else {
                ball.setY(ball.getY() - overlapY);
            }
            ball.bounce(GameConfig.WallSideType.NORTH);
        }

        // TÍNH ĐIỂM
        int score;
        if (scoreManager != null) {
            score = scoreManager.calculateBrickScore(brick, oneshotActive);
        } else {
            // Fallback logic nếu không có ScoreManager
            if (oneshotActive) {
                if (brick.getHitCount() > 0) {
                    score = 10;
                    brick.destroy();
                } else {
                    score = 0;
                }
            } else {
                score = brick.hit(); // Giảm HP của brick, trả về điểm
            }
        }

        // Cập nhật UI
        ui.increaseScore(score);
        brick.updateDraw(); // Cập nhật hình ảnh brick (đổi màu khi bị hit)

        // NẾU BRICK BỊ PHÁ HỦY HOÀN TOÀN:
        if (brick.getHitCount() == 0) {
            // Phát âm thanh phá hủy (chạy ASYNC để không block game)
            javafx.application.Platform.runLater(() -> {
                SoundManager.getInstance().playSound(SoundManager.SoundType.BRICK_BREAK);
            });

            // Cộng coin cho người chơi
            if (coinManager != null) {
                coinManager.addCoins(5);
                ui.updateCoins();
            }

            // ========== SPAWN POWER-UP (20% XÁC SUẤT) ==========
            // Đây là TÍNH NĂNG MỚI được thêm vào!
            if (powerUpManager != null) {
                Powerup spawnedPowerup = powerUpManager.trySpawnPowerup(brick);
                if (spawnedPowerup != null) {
                    levelManager.addPowerup(spawnedPowerup); // Thêm vào list quản lý
                }
            }

            // Xóa brick khỏi màn hình
            levelManager.removeBrick(brick, root);
        } else {
            // Brick chưa bị phá → phát âm thanh hit (chạm thường)
            javafx.application.Platform.runLater(() -> {
                SoundManager.getInstance().playSound(SoundManager.SoundType.BALL_BRICK_HIT);
            });
        }
    }

    public void handleBrickBallCollision(Ball ball, Brick brick, BotScreen ui, int player) {
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
            ball.setX(dx > 0 ? ball.getX() + overlapX : ball.getX() - overlapX);
            ball.bounce(GameConfig.WallSideType.EAST);
        } else {
            ball.setY(dy > 0 ? ball.getY() + overlapY : ball.getY() - overlapY);
            ball.bounce(GameConfig.WallSideType.NORTH);
        }

        brick.updateDraw();
        SoundManager.getInstance().playSound(SoundManager.SoundType.BALL_BRICK_HIT);
    }


    public void handleBrickBallCollision(Ball ball, Brick brick, OneVOneScreen ui, int player) {
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
            ball.setX(dx > 0 ? ball.getX() + overlapX : ball.getX() - overlapX);
            ball.bounce(GameConfig.WallSideType.EAST);
        } else {
            ball.setY(dy > 0 ? ball.getY() + overlapY : ball.getY() - overlapY);
            ball.bounce(GameConfig.WallSideType.NORTH);
        }

        brick.updateDraw();
        SoundManager.getInstance().playSound(SoundManager.SoundType.BALL_BRICK_HIT);
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

    /**
     * Kiểm tra va chạm bóng vs tường
     *
     * @return WallSideType nếu có va chạm, null nếu không
     * BOTTOM_HIT = bóng rơi xuống dưới → mất mạng
     */
    public GameConfig.WallSideType checkWallCollision(Ball ball, double gameWidth, double gameHeight) {
        // Tường trên
        if (ball.getY() <= 0) {
            ball.setY(0);
            ball.bounce(GameConfig.WallSideType.NORTH);
            SoundManager.getInstance().playSound(SoundManager.SoundType.BALL_WALL_HIT);
            return GameConfig.WallSideType.NORTH;
        }

        // Tường trái
        if (ball.getX() <= 0) {
            ball.setX(0);
            ball.bounce(GameConfig.WallSideType.WEST);
            SoundManager.getInstance().playSound(SoundManager.SoundType.BALL_WALL_HIT);
            return GameConfig.WallSideType.WEST;
        }

        // Tường phải
        if (ball.getRightX() >= gameWidth) {
            ball.setX(gameWidth - ball.getWidth());
            ball.bounce(GameConfig.WallSideType.EAST);
            SoundManager.getInstance().playSound(SoundManager.SoundType.BALL_WALL_HIT);
            return GameConfig.WallSideType.EAST;
        }

        // Tường dưới = MẤT MẠNG (trừ khi bật chế độ debug invincible)
        if (ball.getY() >= (gameHeight - 20)) {
            if (GameConfig.DEBUG_INVINCIBLE_MODE) {
                // Chế độ debug: bóng nảy lại như tường bình thường
                ball.setY(gameHeight - ball.getHeight());
                ball.bounce(GameConfig.WallSideType.SOUTH);
                SoundManager.getInstance().playSound(SoundManager.SoundType.BALL_WALL_HIT);
                return GameConfig.WallSideType.SOUTH;
            }
            // Bình thường: trả về BOTTOM_HIT để game engine xử lý mất mạng
            return GameConfig.WallSideType.BOTTOM_HIT;
        }

        return null; // Không có va chạm
    }

    /**
     * Kiểm tra va chạm bóng vs brick (dùng AABB - Axis-Aligned Bounding Box)
     *
     * THUẬT TOÁN TỐI ƯU:
     * 1. Tính bounding box của bóng (hình chữ nhật bao quanh)
     * 2. Duyệt qua tất cả brick:
     *    - Bỏ qua brick đã bị phá (hitCount = 0)
     *    - Kiểm tra overlap bounding box (nhanh)
     *    - Nếu overlap → kiểm tra va chạm tròn-chữ nhật (chính xác)
     *
     * @return Brick bị va chạm, hoặc null nếu không có
     */
    public Brick checkBrickBallCollision(Ball ball, List<Brick> bricks) {
        double ballCenterX = ball.getX() + ball.getRadius();
        double ballCenterY = ball.getY() + ball.getRadius();
        double r = ball.getRadius();

        // Tính bounding box của bóng
        double ballMinX = ballCenterX - r;
        double ballMaxX = ballCenterX + r;
        double ballMinY = ballCenterY - r;
        double ballMaxY = ballCenterY + r;

        for (Brick brick : bricks) {
            // Bỏ qua brick đã bị phá
            if (brick.getHitCount() == 0) continue;

            javafx.geometry.Bounds brickBounds = brick.getNode().getBoundsInParent();

            // KIỂM TRA OVERLAP BOUNDING BOX (nhanh, loại bỏ 90% trường hợp không va chạm)
            if (ballMaxX < brickBounds.getMinX() ||
                    ballMinX > brickBounds.getMaxX() ||
                    ballMaxY < brickBounds.getMinY() ||
                    ballMinY > brickBounds.getMaxY()) {
                continue; // Không overlap → bỏ qua
            }

            // KIỂM TRA VA CHẠM TRÒN-CHỮ NHẬT (chính xác)
            if (circleIntersectsRect(ballCenterX, ballCenterY, r, brickBounds)) {
                return brick; // Tìm thấy brick bị va chạm
            }
        }

        return null; // Không có brick nào bị va chạm
    }

    /**
     * Kiểm tra va chạm TRÒN (bóng) vs CHỮ NHẬT (brick/paddle)
     *
     * THUẬT TOÁN:
     * 1. Tìm điểm GẦN NHẤT trên chữ nhật với tâm hình tròn
     * 2. Tính khoảng cách từ tâm tròn đến điểm đó
     * 3. Nếu khoảng cách <= bán kính → có va chạm
     *
     * @param cx Tâm X của hình tròn
     * @param cy Tâm Y của hình tròn
     * @param radius Bán kính
     * @param rect Bounds của chữ nhật
     * @return true nếu có va chạm
     */
    private boolean circleIntersectsRect(double cx, double cy, double radius, javafx.geometry.Bounds rect) {
        // Tìm điểm gần nhất trên chữ nhật
        // Clamp tọa độ tâm tròn vào phạm vi chữ nhật
        double closestX = Math.max(rect.getMinX(), Math.min(cx, rect.getMaxX()));
        double closestY = Math.max(rect.getMinY(), Math.min(cy, rect.getMaxY()));

        // Tính khoảng cách từ tâm tròn đến điểm gần nhất
        double dx = cx - closestX;
        double dy = cy - closestY;

        // Va chạm nếu khoảng cách <= bán kính
        return (dx * dx + dy * dy) <= (radius * radius);
    }
}
