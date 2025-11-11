package gameobject.powerup;

import gamemanager.manager.GameObject;
import gamemanager.core.GameEngine;
import gamemanager.manager.SoundManager;
import gameobject.paddle.Paddle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import gameconfig.GameConfig;

import java.util.Objects;

import static gameconfig.GameConfig.*;

/**
 * Powerup - Power-up rơi xuống từ brick bị phá
 *
 * CÓ 3 LOẠI POWER-UP:
 * 1. MULTIPLY (doubleup.png) - Spawn thêm 1 bóng
 * 2. ONESHOT (onehit.png) - Bóng phá brick 1 hit trong 1 lượt
 * 3. EXPAND (shield.png) - Paddle to ra trong 10 giây
 */
public class Powerup extends GameObject {
    private final GameConfig.PowerUpType powerupType;
    private final ImageView imageView;

    /**
     * Constructor: Tạo power-up tại vị trí (x, y)
     *
     * @param x Tọa độ X (tâm brick)
     * @param y Tọa độ Y (tâm brick)
     * @param type Loại power-up (MULTIPLY, ONESHOT, EXPAND)
     */
    public Powerup(double x, double y, GameConfig.PowerUpType type) {
        super(x, y, POWERUP_WIDTH, POWERUP_HEIGHT);
        this.powerupType = type;

        // CHỌN ẢNH DựA VÀO LOẠI POWER-UP
        // Switch expression (Java 14+) trả về giá trị trực tiếp
        String imagePath = switch (type) {
            case MULTIPLY -> "/iamgepowerup/doubleup.png";   // Icon 2 bóng
            case ONESHOT -> "/iamgepowerup/onehit.png";      // Icon búa/sấm sét
            case EXPAND -> "/iamgepowerup/shield.png";       // Icon khiên/mở rộng
        };

        try {
            // Load ảnh từ resources
            Image image = new Image(Objects.requireNonNull(
                getClass().getResourceAsStream(imagePath)));
            this.imageView = new ImageView(image);
            this.imageView.setFitWidth(width);
            this.imageView.setFitHeight(height);
            this.imageView.setPreserveRatio(true); // Giữ tỉ lệ ảnh

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load powerup image: " + imagePath);
        }

        // Đặt vị trí ban đầu
        setX(x);
        setY(y);
    }

    @Override
    public javafx.scene.Node getNode() {
        return imageView;
    }

    /**
     * Cập nhật vị trí power-up (rơi xuống)
     * Được gọi mỗi frame bởi GameEngine
     */
    public void update() {
        // Rơi xuống với tốc độ POWERUP_FALL_SPEED (từ GameConfig)
        setY(y + POWERUP_FALL_SPEED);
    }

    /**
     * Kích hoạt power-up khi paddle chạm
     *
     * LOGIC ACTIVATION:
     * - EXPAND: Paddle to ra (gọi paddle.applyPowerup)
     * - MULTIPLY: Spawn thêm bóng (gọi engine.spawnExtraBall)
     * - ONESHOT: Bật chế độ oneshot (gọi engine.enableOneshot)
     *
     * @param game GameEngine để gọi các method spawn/enable
     * @param paddle Paddle để áp dụng hiệu ứng (EXPAND)
     */
    public void activate(Object game, Paddle paddle) {
        // Phát âm thanh thu thập power-up
        SoundManager.getInstance().playSound(SoundManager.SoundType.POWERUP_COLLECT);

        // Xử lý theo từng loại power-up
        switch (powerupType) {
            case EXPAND -> {
                // EXPAND: Làm paddle to ra
                // paddle.applyPowerup sẽ tăng chiều rộng paddle
                // PowerUpManager sẽ tự động reset sau 10 giây
                paddle.applyPowerup(GameConfig.PowerUpType.EXPAND);
            }

            case MULTIPLY -> {
                // MULTIPLY: Spawn thêm bóng
                // Kiểm tra xem game có phải GameEngine không (type safety)
                if (game instanceof GameEngine engine) {
                    // Spawn thêm 1 bóng tại vị trí paddle
                    // Bóng mới sẽ bay theo hướng ngẫu nhiên
                    engine.spawnExtraBall();
                }
            }

            case ONESHOT -> {
                // ONESHOT: Bật chế độ phá brick 1 hit
                if (game instanceof GameEngine engine) {
                    // Bật chế độ oneshot
                    // Tất cả brick (kể cả brick 2 hit) sẽ bị phá trong 1 lần chạm
                    // Hiệu ứng kéo dài đến hết lượt (hoặc cho đến khi mất mạng)
                    engine.enableOneshot();
                }
            }
        }
    }
}
