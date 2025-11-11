package gamemanager.manager;

import javafx.concurrent.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manager for handling background tasks (audio, I/O) off the JavaFX UI thread
 * Quản lý các tác vụ chạy nền (âm thanh, đọc/ghi file) NGOÀI luồng UI của JavaFX
 *
 * TẠI SAO CẦN CLASS NÀY?
 * - JavaFX UI chạy trên 1 luồng duy nhất (JavaFX Application Thread)
 * - Nếu làm việc nặng (lưu file, play sound) trên luồng UI → game bị LAG, đơ màn hình
 * - Class này tạo luồng riêng để xử lý công việc nặng → UI mượt mà
 */
public class BackgroundTaskManager {
    private static BackgroundTaskManager instance;

    // ExecutorService = bể chứa các luồng (thread pool)
    // Thay vì tạo luồng mới mỗi lần cần → tái sử dụng 2 luồng có sẵn
    private final ExecutorService executor;

    private BackgroundTaskManager() {
        // Tạo thread pool với 2 luồng worker
        // VÍ DỤ: Luồng 1 đang lưu file, luồng 2 có thể play sound cùng lúc
        this.executor = Executors.newFixedThreadPool(2, runnable -> {
            Thread thread = new Thread(runnable);

            // setDaemon(true) = luồng "ma"
            // Giải thích: Khi đóng game, các luồng daemon tự động tắt
            // Không cần phải đợi chúng chạy xong → đóng game nhanh hơn
            thread.setDaemon(true);

            thread.setName("BackgroundTask-" + thread.getId());
            return thread;
        });
    }

    // SINGLETON PATTERN: Chỉ có 1 instance duy nhất trong toàn bộ game
    // Tránh tạo nhiều thread pool → tốn tài nguyên
    public static synchronized BackgroundTaskManager getInstance() {
        if (instance == null) {
            instance = new BackgroundTaskManager();
        }
        return instance;
    }

    /**
     * Chạy 1 task trên luồng nền (background thread)
     *
     * VÍ DỤ THỰC TẾ:
     * executeAsync(() -> {
     *     // Code này chạy trên luồng NỀN, KHÔNG block UI
     *     saveFile("highscores.dat");
     *     // Trong lúc lưu file, game vẫn chạy bình thường!
     * });
     *
     * @param task Công việc cần chạy nền (Runnable = hàm không có return value)
     */
    public void executeAsync(Runnable task) {
        if (task != null && !executor.isShutdown()) {
            executor.submit(() -> {
                try {
                    task.run(); // Chạy công việc
                } catch (Exception e) {
                    // Bắt lỗi để tránh crash cả thread pool
                    System.err.println("Background task error: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * Chạy JavaFX Task với khả năng theo dõi tiến trình
     * (Nâng cao - hiện tại chưa dùng, dành cho tương lai)
     */
    public <T> void executeTask(Task<T> task) {
        if (task != null && !executor.isShutdown()) {
            executor.submit(task);
        }
    }

    /**
     * Chạy task với callback - PHỨC TẠP NHƯNG RẤT HỮU ÍCH
     *
     * VÍ DỤ THỰC TẾ: Lưu highscore
     *
     * executeWithCallback(
     *     // BƯỚC 1: Chạy trên LUỒNG NỀN (không block UI)
     *     () -> {
     *         saveHighScoreToFile(); // Ghi file (mất 100ms)
     *     },
     *     // BƯỚC 2: Nếu THÀNH CÔNG → chạy trên LUỒNG UI
     *     () -> {
     *         showNotification("Lưu thành công!"); // Hiển thị thông báo
     *     },
     *     // BƯỚC 3: Nếu LỖI → chạy trên LUỒNG UI
     *     () -> {
     *         showError("Lưu thất bại!"); // Hiển thị lỗi
     *     }
     * );
     *
     * LƯU Ý QUAN TRỌNG:
     * - Callback SUCCESS/ERROR chạy trên JavaFX UI thread (dùng Platform.runLater)
     * - Vì vậy có thể cập nhật UI an toàn (thay đổi text, button, etc.)
     *
     * @param task Công việc chạy nền
     * @param onSuccess Hàm gọi khi thành công (chạy trên UI thread)
     * @param onError Hàm gọi khi có lỗi (chạy trên UI thread)
     */
    public void executeWithCallback(Runnable task, Runnable onSuccess, Runnable onError) {
        executeAsync(() -> {
            try {
                task.run(); // Chạy task trên luồng nền

                // Nếu thành công → gọi callback trên UI thread
                if (onSuccess != null) {
                    // Platform.runLater = đưa code vào hàng đợi của JavaFX UI thread
                    // Đảm bảo cập nhật UI an toàn, tránh lỗi "Not on FX application thread"
                    javafx.application.Platform.runLater(onSuccess);
                }
            } catch (Exception e) {
                System.err.println("Task failed: " + e.getMessage());

                // Nếu lỗi → gọi callback lỗi trên UI thread
                if (onError != null) {
                    javafx.application.Platform.runLater(onError);
                }
            }
        });
    }

    /**
     * Tắt executor khi thoát game
     * Gọi hàm này trong phương thức cleanup của game
     */
    public void shutdown() {
        if (!executor.isShutdown()) {
            executor.shutdown();
        }
    }

    /**
     * Kiểm tra executor có đang chạy không
     */
    public boolean isRunning() {
        return !executor.isShutdown();
    }
}
