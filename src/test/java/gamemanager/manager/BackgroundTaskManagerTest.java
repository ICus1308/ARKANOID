package gamemanager.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BackgroundTaskManagerTest - Test đa luồng (multithreading)
 *
 * MỤC ĐÍCH:
 * - Kiểm tra task có chạy trên background thread không
 * - Kiểm tra callback có hoạt động không
 * - Kiểm tra xử lý lỗi có đúng không
 *
 * LƯU Ý: Các test này có Thread.sleep() → chạy hơi lâu (0.5 giây)
 */
class BackgroundTaskManagerTest {

    private BackgroundTaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = BackgroundTaskManager.getInstance();
    }

    /**
     * Test 1: Singleton pattern
     *
     * KIỂM TRA:
     * - getInstance() gọi 2 lần phải trả về CÙNG 1 instance
     * - Đảm bảo chỉ có 1 thread pool trong toàn bộ game
     */
    @Test
    void testSingletonInstance() {
        BackgroundTaskManager instance1 = BackgroundTaskManager.getInstance();
        BackgroundTaskManager instance2 = BackgroundTaskManager.getInstance();

        // assertSame = so sánh địa chỉ bộ nhớ (phải cùng object)
        assertSame(instance1, instance2, "Should return the same singleton instance");
    }

    /**
     * Test 2: TaskManager có đang chạy không
     *
     * KIỂM TRA:
     * - Ban đầu executor phải running (chưa shutdown)
     */
    @Test
    void testIsRunning() {
        assertTrue(taskManager.isRunning(), "Task manager should be running initially");
    }

    /**
     * Test 3: Execute task null
     *
     * KIỂM TRA:
     * - Truyền null vào executeAsync → không crash
     * - Xử lý null an toàn
     */
    @Test
    void testExecuteAsyncWithNullTask() {
        assertDoesNotThrow(() -> taskManager.executeAsync(null),
            "Should handle null task gracefully");
    }

    /**
     * Test 4: Execute task hợp lệ
     *
     * KIỂM TRA:
     * - Task có chạy trên background thread không
     * - Sử dụng boolean array để theo dõi (vì lambda cần final variable)
     *
     * LƯU Ý:
     * - Thread.sleep(100) để đợi task chạy xong
     * - Task chạy ASYNC → không biết khi nào xong → phải đợi
     */
    @Test
    void testExecuteAsyncWithValidTask() throws InterruptedException {
        final boolean[] taskExecuted = {false}; // Array để bypass final requirement

        taskManager.executeAsync(() -> taskExecuted[0] = true);

        // Đợi 100ms cho task chạy xong
        Thread.sleep(100);

        assertTrue(taskExecuted[0], "Task should be executed in background");
    }

    /**
     * Test 5: Execute với callback
     *
     * KIỂM TRA:
     * - Task chạy trên background thread
     * - Success callback chạy trên UI thread (Platform.runLater)
     *
     * LƯU Ý: Đợi 200ms vì có 2 bước async (task + callback)
     */
    @Test
    void testExecuteWithCallback() throws InterruptedException {
        final boolean[] taskExecuted = {false};
        final boolean[] callbackExecuted = {false};

        taskManager.executeWithCallback(
            () -> taskExecuted[0] = true,      // Task nền
            () -> callbackExecuted[0] = true,  // Success callback
            null                                // Error callback (không cần)
        );

        // Đợi task + callback chạy xong
        Thread.sleep(200);

        assertTrue(taskExecuted[0], "Task should be executed");
        assertTrue(callbackExecuted[0], "Success callback should be executed");
    }

    /**
     * Test 6: Callback khi có lỗi
     *
     * KIỂM TRA:
     * - Task throw exception
     * - Error callback được gọi
     * - Success callback KHÔNG được gọi
     */
    @Test
    void testExecuteWithCallbackOnError() throws InterruptedException {
        final boolean[] errorCallbackExecuted = {false};

        taskManager.executeWithCallback(
            () -> {
                // Task cố ý throw exception
                throw new RuntimeException("Test exception");
            },
            null,                                    // Success callback (không cần)
            () -> errorCallbackExecuted[0] = true   // Error callback
        );

        // Đợi task fail + error callback chạy
        Thread.sleep(200);

        assertTrue(errorCallbackExecuted[0], "Error callback should be executed on exception");
    }

    /**
     * Test 7: Nhiều task cùng lúc
     *
     * KIỂM TRA:
     * - Submit 5 task cùng lúc
     * - Thread pool (2 luồng) xử lý song song
     * - Tất cả 5 task đều chạy xong
     *
     * LƯU Ý:
     * - synchronized(counter) vì nhiều thread cùng truy cập
     * - Đợi 300ms để đảm bảo tất cả task xong
     */
    @Test
    void testMultipleAsyncTasks() throws InterruptedException {
        final int[] counter = {0};

        // Submit 5 task
        for (int i = 0; i < 5; i++) {
            taskManager.executeAsync(() -> {
                synchronized (counter) { // Thread-safe increment
                    counter[0]++;
                }
            });
        }

        // Đợi tất cả task chạy xong
        Thread.sleep(300);

        // Tất cả 5 task đều chạy
        assertEquals(5, counter[0], "All 5 tasks should be executed");
    }
}

