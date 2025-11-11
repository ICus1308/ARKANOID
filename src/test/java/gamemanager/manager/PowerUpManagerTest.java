package gamemanager.manager;

import gameconfig.GameConfig;
import gameobject.brick.Brick;
import gameobject.brick.StandardBrick;
import gameobject.powerup.Powerup;
import javafx.scene.layout.Pane;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PowerUpManagerTest - Test tự động cho PowerUpManager
 *
 * MỤC ĐÍCH:
 * - Kiểm tra power-up có spawn đúng không
 * - Kiểm tra timer có hoạt động không
 * - Kiểm tra cancel timer có đúng không
 *
 * KHÔNG CẦN CHẠY GAME - chỉ cần chạy test (2 giây)
 */
class PowerUpManagerTest {

    private PowerUpManager powerUpManager;
    private Pane testPane;

    /**
     * Setup: Chạy TRƯỚC MỖI TEST
     * Tạo môi trường test sạch (PowerUpManager mới)
     */
    @BeforeEach
    void setUp() {
        testPane = new Pane();
        powerUpManager = new PowerUpManager(testPane);
    }

    /**
     * Test 1: Kiểm tra khởi tạo
     *
     * KIỂM TRA:
     * - PowerUpManager không null
     * - Ban đầu không có power-up nào active
     */
    @Test
    void testPowerUpManagerInitialization() {
        assertNotNull(powerUpManager, "PowerUpManager should be initialized");
        assertEquals(0, powerUpManager.getActiveCount(), "Should have no active power-ups initially");
    }

    /**
     * Test 2: Spawn với brick null
     *
     * KIỂM TRA:
     * - Truyền null vào trySpawnPowerup → phải trả về null (không crash)
     */
    @Test
    void testTrySpawnPowerupWithNullBrick() {
        Powerup result = powerUpManager.trySpawnPowerup(null);
        assertNull(result, "Should return null when brick is null");
    }

    /**
     * Test 3: Spawn với brick hợp lệ
     *
     * KIỂM TRA:
     * - Thử spawn 50 lần
     * - Với xác suất 20%, ít nhất phải spawn được 1 lần
     * - Power-up spawn phải có node (ImageView)
     *
     * LƯU Ý: Test này có thể FAIL nếu RẤT KHÔN NGOAN (xác suất < 0.001%)
     */
    @Test
    void testTrySpawnPowerupWithValidBrick() {
        Brick testBrick = new StandardBrick(100, 100, 50, 20);

        // Thử spawn nhiều lần để test xác suất
        boolean spawnedAtLeastOnce = false;
        for (int i = 0; i < 50; i++) {
            Powerup result = powerUpManager.trySpawnPowerup(testBrick);
            if (result != null) {
                spawnedAtLeastOnce = true;
                assertNotNull(result.getNode(), "Spawned power-up should have a node");
                break; // Tìm thấy rồi → dừng vòng lặp
            }
        }

        // Với 50 lần thử và xác suất 20%, gần như chắc chắn spawn được
        assertTrue(spawnedAtLeastOnce, "Should spawn at least one power-up in 50 tries");
    }

    /**
     * Test 4: Bắt đầu timer
     *
     * KIỂM TRA:
     * - Bắt đầu timer cho EXPAND
     * - Kiểm tra power-up có đang hoạt động không
     */
    @Test
    void testPowerUpTimerStart() {
        final boolean[] callbackExecuted = {false};

        // Bắt đầu timer với callback (callback sẽ chạy sau 10 giây - nhưng test không đợi)
        powerUpManager.startPowerUpTimer(
            GameConfig.PowerUpType.EXPAND,
            () -> callbackExecuted[0] = true
        );

        // Kiểm tra timer đã được bắt đầu (power-up đang hoạt động)
        assertTrue(powerUpManager.isPowerUpActive(GameConfig.PowerUpType.EXPAND),
            "Power-up should be active after starting timer");
    }

    /**
     * Test 5: Hủy timer
     *
     * KIỂM TRA:
     * - Bắt đầu timer
     * - Hủy timer
     * - Kiểm tra power-up không còn hoạt động
     */
    @Test
    void testPowerUpTimerCancel() {
        powerUpManager.startPowerUpTimer(GameConfig.PowerUpType.EXPAND, () -> {});
        assertTrue(powerUpManager.isPowerUpActive(GameConfig.PowerUpType.EXPAND));

        // Hủy timer
        powerUpManager.cancelPowerUpTimer(GameConfig.PowerUpType.EXPAND);

        // Power-up không còn hoạt động
        assertFalse(powerUpManager.isPowerUpActive(GameConfig.PowerUpType.EXPAND),
            "Power-up should not be active after canceling timer");
    }

    /**
     * Test 6: Hủy tất cả timer
     *
     * KIỂM TRA:
     * - Bắt đầu nhiều timer
     * - Hủy tất cả
     * - Kiểm tra không còn timer nào đang hoạt động
     */
    @Test
    void testCancelAllTimers() {
        // Bắt đầu 2 timer
        powerUpManager.startPowerUpTimer(GameConfig.PowerUpType.EXPAND, () -> {});
        powerUpManager.startPowerUpTimer(GameConfig.PowerUpType.MULTIPLY, () -> {});

        assertTrue(powerUpManager.getActiveCount() > 0, "Should have active timers");

        // Hủy tất cả
        powerUpManager.cancelAllTimers();

        // Không còn timer nào
        assertEquals(0, powerUpManager.getActiveCount(),
            "Should have no active timers after cancel all");
    }

    /**
     * Test 7: Đếm số timer đang hoạt động
     *
     * KIỂM TRA:
     * - Ban đầu: 0 timer
     * - Bắt đầu EXPAND: 1 timer (EXPAND có duration 10s)
     * - Bắt đầu ONESHOT: vẫn 1 timer (ONESHOT duration = 0, không tạo thêm timer)
     */
    @Test
    void testGetActiveCount() {
        assertEquals(0, powerUpManager.getActiveCount());

        // EXPAND có duration → tạo timer
        powerUpManager.startPowerUpTimer(GameConfig.PowerUpType.EXPAND, () -> {});
        assertEquals(1, powerUpManager.getActiveCount());

        // ONESHOT không có duration → không tạo thêm timer
        powerUpManager.startPowerUpTimer(GameConfig.PowerUpType.ONESHOT, () -> {});
        assertEquals(1, powerUpManager.getActiveCount(),
            "ONESHOT has 0 duration, should not add to active count");
    }

    /**
     * Test 8: Kiểm tra power-up có đang hoạt động không
     *
     * KIỂM TRA:
     * - Ban đầu: không hoạt động
     * - Sau khi bắt đầu: hoạt động
     */
    @Test
    void testIsPowerUpActive() {
        assertFalse(powerUpManager.isPowerUpActive(GameConfig.PowerUpType.EXPAND));

        powerUpManager.startPowerUpTimer(GameConfig.PowerUpType.EXPAND, () -> {});

        assertTrue(powerUpManager.isPowerUpActive(GameConfig.PowerUpType.EXPAND));
    }
}
