package gamemanager.manager;

import gameconfig.GameConfig;
import gameobject.brick.Brick;
import gameobject.powerup.Powerup;
import javafx.animation.PauseTransition;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.*;

/**
 * Manager class for handling power-up spawning, tracking active effects, and timers
 * Lớp quản lý việc spawn power-up, theo dõi hiệu ứng đang hoạt động và bộ đếm thời gian
 */
public class PowerUpManager {
    private final Pane root;
    private final Random random;

    // Map lưu các timer đang chạy cho mỗi loại power-up
    // Key: Loại power-up (EXPAND, MULTIPLY, ONESHOT)
    // Value: Timer đang chạy
    private final Map<GameConfig.PowerUpType, PauseTransition> activeTimers;

    // Xác suất spawn power-up khi brick bị phá: 20% = 0.2
    // Nghĩa là cứ 5 viên brick bị phá thì có 1 viên rơi power-up
    private static final double POWERUP_SPAWN_CHANCE = 0.20;

    // Thời gian hiệu lực của power-up EXPAND (giây)
    // Power-up EXPAND làm paddle to ra trong 10 giây
    private static final double EXPAND_DURATION = 10.0;

    public PowerUpManager(Pane root) {
        this.root = root;
        this.random = new Random();
        this.activeTimers = new HashMap<>();
    }

    /**
     * Thử spawn một power-up tại vị trí brick bị phá
     * @param brick Viên gạch vừa bị phá hủy
     * @return Power-up được spawn, hoặc null nếu không spawn
     */
    public Powerup trySpawnPowerup(Brick brick) {
        if (brick == null) return null;

        if (random.nextDouble() > POWERUP_SPAWN_CHANCE) {
            return null; // 80% trường hợp không spawn
        }

        // Chọn NGẪU NHIÊN 1 trong 3 loại power-up
        GameConfig.PowerUpType[] types = GameConfig.PowerUpType.values();
        GameConfig.PowerUpType selectedType = types[random.nextInt(types.length)];

        // Tính toán vị trí spawn: chính giữa brick
        // Trừ đi 1/2 chiều rộng power-up để căn giữa
        double x = brick.getX() + brick.getWidth() / 2 - GameConfig.POWERUP_WIDTH / 2;
        double y = brick.getY() + brick.getHeight() / 2;

        Powerup powerup = new Powerup(x, y, selectedType);

        // Thêm vào scene graph để hiển thị
        if (root != null && powerup.getNode() != null) {
            root.getChildren().add(powerup.getNode());
            SoundManager.getInstance().playSound(SoundManager.SoundType.POWERUP_SPAWN);
        }

        return powerup;
    }

    /**
     * KHỞI ĐỘNG TIMER CHO POWER-UP TẠM THỜI
     *
     * MỤC ĐÍCH: Power-up như EXPAND chỉ có hiệu lực 10 giây
     * Sau đó tự động hết hiệu lực và reset về trạng thái ban đầu
     *
     * HOẠT ĐỘNG:
     * 1. Hủy timer cũ (nếu có) - tránh trùng lặp
     * 2. Tạo PauseTransition với thời gian tương ứng:
     *    - EXPAND: 10 giây
     *    - ONESHOT: 7.5 giây (được xử lý ở GameEngine)
     *    - MULTIPLY: không cần timer (vĩnh viễn)
     * 3. Khi timer hết:
     *    - Chạy callback onExpire (ví dụ: reset paddle size)
     *    - Xóa timer khỏi map activeTimers
     *
     * VÍ DỤ EXPAND:
     * - Người chơi ăn power-up EXPAND
     * - Paddle to ra 1.25 lần
     * - Timer đếm ngược 10 giây
     * - Sau 10 giây: callback chạy → paddle thu nhỏ về bình thường
     *
     * @param type Loại power-up (EXPAND, MULTIPLY, ONESHOT)
     * @param onExpire Hàm callback chạy khi hết thời gian
     */
    public void startPowerUpTimer(GameConfig.PowerUpType type, Runnable onExpire) {
        // Hủy timer cũ (nếu ăn 2 power-up EXPAND liên tiếp)
        cancelPowerUpTimer(type);

        double duration = getDurationForType(type);

        // duration = 0 → power-up vĩnh viễn, không cần timer
        if (duration <= 0) return;

        // Tạo timer đếm ngược
        PauseTransition timer = new PauseTransition(Duration.seconds(duration));

        // Khi timer hết: chạy callback + xóa khỏi map
        timer.setOnFinished(event -> {
            if (onExpire != null) {
                onExpire.run(); // Reset trạng thái (do GameEngine truyền vào)
            }
            activeTimers.remove(type);
        });

        // Bắt đầu đếm
        timer.play();
        activeTimers.put(type, timer);
    }

    /**
     * HỦY TIMER CỦA POWER-UP
     * - Dùng khi ăn power-up mới cùng loại (reset timer)
     * - Hoặc khi cleanup game
     */
    public void cancelPowerUpTimer(GameConfig.PowerUpType type) {
        PauseTransition timer = activeTimers.get(type);
        if (timer != null) {
            timer.stop();
            activeTimers.remove(type);
        }
    }

    /**
     * HỦY TẤT CẢ TIMER
     * - Dùng khi game over hoặc reset game
     */
    public void cancelAllTimers() {
        for (PauseTransition timer : activeTimers.values()) {
            timer.stop();
        }
        activeTimers.clear();
    }

    /**
     * LẤY THỜI GIAN HIỆU LỰC CỦA TỪNG LOẠI POWER-UP
     *
     * @param type Loại power-up
     * @return Thời gian hiệu lực (giây), 0 = vĩnh viễn
     */
    private double getDurationForType(GameConfig.PowerUpType type) {
        return switch (type) {
            case EXPAND -> EXPAND_DURATION;  // 10 giây
            case MULTIPLY -> 0;              // Vĩnh viễn (không cần timer)
            case ONESHOT -> 0;               // Được xử lý riêng ở GameEngine
        };
    }

    /**
     * KIỂM TRA POWER-UP CÓ ĐANG ACTIVE KHÔNG
     */
    public boolean isPowerUpActive(GameConfig.PowerUpType type) {
        return activeTimers.containsKey(type);
    }

    /**
     * LẤY THỜI GIAN CÒN LẠI CỦA POWER-UP
     * @return Thời gian còn lại (giây), hoặc 0 nếu không active
     */
    public double getRemainingTime(GameConfig.PowerUpType type) {
        PauseTransition timer = activeTimers.get(type);
        if (timer == null) return 0;

        // Tính thời gian còn lại = tổng thời gian - thời gian đã trôi qua
        Duration total = timer.getDuration();
        Duration current = timer.getCurrentTime();
        return (total.toSeconds() - current.toSeconds());
    }
}
