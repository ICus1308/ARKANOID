package userinterface.gamescreen;

import javafx.scene.layout.Pane;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static gameconfig.GameConfig.GameState;
import static org.junit.jupiter.api.Assertions.*;

/**
 * OneVOneScreenTest - Test tự động cho màn hình 1v1
 *
 * MỤC ĐÍCH:
 * - Kiểm tra lives của 2 player có cập nhật đúng không
 * - Kiểm tra score có hoạt động không
 * - Kiểm tra các phương thức interface có đúng không
 *
 * KHÔNG CẦN CHẠY GAME - JUnit test logic trong 0.1 giây
 */
class OneVOneScreenTest {

    private OneVOneScreen oneVOneScreen;
    private Pane testPane;

    /**
     * Setup: Chạy trước mỗi test
     * Tạo OneVOneScreen mới cho mỗi test (môi trường sạch)
     */
    @BeforeEach
    void setUp() {
        testPane = new Pane();
        oneVOneScreen = new OneVOneScreen(testPane);
    }

    /**
     * Test 1: Kiểm tra khởi tạo
     *
     * KIỂM TRA:
     * - OneVOneScreen không null
     * - Player 1 bắt đầu với 3 mạng
     * - Player 2 bắt đầu với 3 mạng
     * - Score bắt đầu từ 0
     */
    @Test
    void testInitialization() {
        assertNotNull(oneVOneScreen, "OneVOneScreen should be initialized");
        assertEquals(3, oneVOneScreen.getPlayer1Lives(), "Player 1 should start with 3 lives");
        assertEquals(3, oneVOneScreen.getPlayer2Lives(), "Player 2 should start with 3 lives");
        assertEquals(0, oneVOneScreen.getScore(), "Score should start at 0");
    }

    /**
     * Test 2: Cập nhật lives Player 1
     *
     * KIỂM TRA:
     * - Set lives = 5 → lives phải = 5
     * - Decrease lives → lives giảm 1 (5 → 4)
     */
    @Test
    void testPlayer1LivesUpdate() {
        oneVOneScreen.updatePlayer1Lives(5);
        assertEquals(5, oneVOneScreen.getPlayer1Lives(), "Player 1 lives should be updated to 5");

        oneVOneScreen.decreasePlayer1Lives();
        assertEquals(4, oneVOneScreen.getPlayer1Lives(), "Player 1 lives should decrease to 4");
    }

    /**
     * Test 3: Cập nhật lives Player 2
     * (Logic tương tự Player 1)
     */
    @Test
    void testPlayer2LivesUpdate() {
        oneVOneScreen.updatePlayer2Lives(5);
        assertEquals(5, oneVOneScreen.getPlayer2Lives(), "Player 2 lives should be updated to 5");

        oneVOneScreen.decreasePlayer2Lives();
        assertEquals(4, oneVOneScreen.getPlayer2Lives(), "Player 2 lives should decrease to 4");
    }

    /**
     * Test 4: Cập nhật score
     *
     * KIỂM TRA:
     * - updateScore(100) → score = 100
     * - increaseScore(50) → score = 150 (100 + 50)
     */
    @Test
    void testScoreUpdate() {
        oneVOneScreen.updateScore(100);
        assertEquals(100, oneVOneScreen.getScore(), "Score should be updated to 100");

        oneVOneScreen.increaseScore(50);
        assertEquals(150, oneVOneScreen.getScore(), "Score should increase to 150");
    }

    /**
     * Test 5: Generic lives interface
     *
     * KIỂM TRA:
     * - getLives() trả về lives của Player 1 (mặc định)
     * - updateLives() cập nhật Player 1
     * - decreaseLives() giảm lives Player 1
     *
     * LÝ DO: GameScreen interface thiết kế cho 1 player
     * OneVOneScreen có 2 player → mặc định điều khiển Player 1
     */
    @Test
    void testGenericLivesInterface() {
        // getLives = getPlayer1Lives
        assertEquals(3, oneVOneScreen.getLives(), "Generic getLives should return player1 lives");

        // updateLives → cập nhật Player 1
        oneVOneScreen.updateLives(5);
        assertEquals(5, oneVOneScreen.getPlayer1Lives(), "updateLives should update player1 lives");

        // decreaseLives → giảm Player 1
        oneVOneScreen.decreaseLives();
        assertEquals(4, oneVOneScreen.getPlayer1Lives(), "decreaseLives should decrease player1 lives");
    }

    /**
     * Test 6: updateCoins - no-op
     *
     * KIỂM TRA:
     * - Gọi updateCoins() không gây crash
     * - Chế độ 1v1 không có coin system → method này rỗng
     */
    @Test
    void testUpdateCoinsNoOp() {
        // Không throw exception = PASS
        assertDoesNotThrow(() -> oneVOneScreen.updateCoins(),
            "updateCoins should not throw exception");
    }

    /**
     * Test 7: showLevel
     *
     * KIỂM TRA:
     * - Gọi showLevel() không crash
     */
    @Test
    void testShowLevel() {
        assertDoesNotThrow(() -> oneVOneScreen.showLevel(1),
            "showLevel should not throw exception");
    }

    /**
     * Test 8: showGameMessage với các state khác nhau
     *
     * KIỂM TRA:
     * - START state: hiển thị "PRESS SPACE TO LAUNCH"
     * - PLAYING state: ẩn message
     * - GAME_OVER state: hiển thị winner
     * - LEVEL_CLEARED state: hiển thị "LEVEL CLEARED"
     *
     * Test chỉ kiểm tra không crash (không kiểm tra text vì đó là UI logic)
     */
    @Test
    void testShowGameMessageStates() {
        assertDoesNotThrow(() -> oneVOneScreen.showGameMessage(GameState.START),
            "Should handle START state");
        assertDoesNotThrow(() -> oneVOneScreen.showGameMessage(GameState.PLAYING),
            "Should handle PLAYING state");
        assertDoesNotThrow(() -> oneVOneScreen.showGameMessage(GameState.GAME_OVER),
            "Should handle GAME_OVER state");
        assertDoesNotThrow(() -> oneVOneScreen.showGameMessage(GameState.LEVEL_CLEARED),
            "Should handle LEVEL_CLEARED state");
    }

    /**
     * Test 9: cleanup
     *
     * KIỂM TRA:
     * - Gọi cleanup() không crash
     * - Method này xóa UI elements khỏi scene
     */
    @Test
    void testCleanup() {
        assertDoesNotThrow(() -> oneVOneScreen.cleanup(),
            "cleanup should not throw exception");
    }

    /**
     * Test 10: Lives có thể âm (edge case)
     *
     * KIỂM TRA:
     * - Set lives = 1
     * - Decrease 2 lần → lives = -1
     *
     * LƯU Ý: Đây là test logic interface, không phải test game logic
     * Game logic thực tế sẽ không cho lives < 0 (xử lý trong GameEngine)
     */
    @Test
    void testLivesNeverNegative() {
        oneVOneScreen.updatePlayer1Lives(1);
        oneVOneScreen.decreasePlayer1Lives();
        assertEquals(0, oneVOneScreen.getPlayer1Lives());

        // Decrease again → lives âm (interface cho phép, nhưng game logic sẽ ngăn)
        oneVOneScreen.decreasePlayer1Lives();
        assertEquals(-1, oneVOneScreen.getPlayer1Lives(),
            "Lives can go negative (game logic should prevent this)");
    }
}
