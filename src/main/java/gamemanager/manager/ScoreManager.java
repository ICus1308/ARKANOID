package gamemanager.manager;

import gameobject.brick.Brick;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ScoreManager {

    private static final String HIGH_SCORE_FILE = "highscores.dat";
    private static final int MAX_HIGH_SCORES = 10; // Chỉ lưu top 10 điểm cao nhất

    // ObservableList = danh sách "thông minh" của JavaFX
    // Khi thay đổi list này → UI tự động cập nhật (không cần refresh thủ công)
    private final ObservableList<Score> highScores;
    private final BackgroundTaskManager taskManager;

    public ScoreManager() {
        highScores = FXCollections.observableArrayList();
        taskManager = BackgroundTaskManager.getInstance();
        loadScores(); // Đọc điểm từ file khi khởi động game
    }

    public ObservableList<Score> getScores() {
        return highScores;
    }

    public int calculateBrickScore(Brick brick, boolean oneshotActive) {
        int score;
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
        return score;
    }

    /**
     * Thêm điểm vào bảng xếp hạng
     *
     * LOGIC PHỨC TẠP:
     * 1. Thêm điểm mới vào list
     * 2. Sắp xếp lại theo thứ tự giảm dần (cao nhất lên đầu)
     * 3. Chỉ giữ lại 10 điểm cao nhất (xóa điểm thấp)
     * 4. Lưu vào file (BẤT ĐỒNG BỘ để không lag game)
     */
    public void addScore(String playerName, int score) {
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Player";
        }

        highScores.add(new Score(playerName, score));

        // Sắp xếp giảm dần: điểm cao nhất (ví dụ: 5000) lên đầu
        // Comparator.comparingInt(Score::getScore).reversed()
        // = So sánh theo điểm số, đảo ngược (từ lớn → nhỏ)
        highScores.sort(Comparator.comparingInt(Score::getScore).reversed());

        // Nếu có > 10 điểm → xóa bớt
        // Ví dụ: list có 12 điểm → xóa 2 điểm cuối (thấp nhất)
        if (highScores.size() > MAX_HIGH_SCORES) {
            highScores.remove(MAX_HIGH_SCORES, highScores.size());
        }

        saveScoresAsync(); // Lưu file BẤT ĐỒNG BỘ (không block UI)
    }

    /**
     * Đọc điểm từ file (chạy đồng bộ khi khởi động game - chấp nhận được)
     */
    @SuppressWarnings("unchecked")
    private void loadScores() {
        File scoreFile = new File(HIGH_SCORE_FILE);
        if (!scoreFile.exists()) {
            return; // File chưa tồn tại → bỏ qua
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(scoreFile))) {
            // Đọc toàn bộ list từ file (đã serialize trước đó)
            List<Score> loadedList = (List<Score>) ois.readObject();
            highScores.setAll(loadedList);
            System.out.println("High scores loaded successfully: " + highScores.size() + " entries");
        } catch (ClassNotFoundException e) {
            // File bị lỗi format (có thể do version cũ) → xóa đi
            System.err.println("Error loading scores (incompatible version): " + e.getMessage());
            if (scoreFile.delete()) {
                System.out.println("Deleted corrupted high score file.");
            }
        } catch (IOException e) {
            System.err.println("Error loading scores: " + e.getMessage());
        }
    }

    /**
     * Lưu điểm BẤT ĐỒNG BỘ - QUAN TRỌNG!
     *
     * TẠI SAO PHẢI BẤT ĐỒNG BỘ?
     * - Ghi file mất ~50-200ms (tùy ổ cứng)
     * - Nếu chạy đồng bộ trên UI thread → game đơ 200ms (người chơi thấy lag)
     * - Chạy bất đồng bộ → game vẫn mượt, file được lưu nền
     *
     * CÁCH HOẠT ĐỘNG:
     * 1. Tạo SNAPSHOT (bản sao) của list điểm hiện tại
     *    → Tránh lỗi khi list thay đổi trong lúc đang ghi file
     *
     * 2. Gọi BackgroundTaskManager.executeWithCallback:
     *    - Task nền: Ghi snapshot vào file
     *    - Success callback: In "saved successfully"
     *    - Error callback: In "failed to save"
     */
    private void saveScoresAsync() {
        // BƯỚC 1: Tạo snapshot để tránh concurrent modification
        // Nếu không có snapshot → list có thể thay đổi khi đang ghi file → crash!
        final List<Score> scoreSnapshot = new ArrayList<>(highScores);

        taskManager.executeWithCallback(
            // TASK NỀN: Ghi file (chạy trên background thread)
            () -> {
                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(HIGH_SCORE_FILE))) {
                    oos.writeObject(scoreSnapshot); // Serialize list thành binary
                } catch (IOException e) {
                    System.err.println("Error saving scores: " + e.getMessage());
                    throw new RuntimeException(e); // Ném lỗi để trigger error callback
                }
            },
            // SUCCESS CALLBACK: Chạy trên UI thread
            () -> {
                System.out.println("High scores saved successfully");
            },
            // ERROR CALLBACK: Chạy trên UI thread
            () -> {
                System.err.println("Failed to save high scores");
            }
        );
    }

    /**
     * Phương thức cũ (đồng bộ) - giữ lại cho backward compatibility
     * Không nên dùng vì sẽ block UI thread
     */
    @Deprecated
    private void saveScores() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(HIGH_SCORE_FILE))) {
            oos.writeObject(new ArrayList<>(highScores));
        } catch (IOException e) {
            System.err.println("Error saving scores: " + e.getMessage());
        }
    }

    /**
     * Kiểm tra xem điểm có đủ cao để vào top 10 không
     * <p>
     * LOGIC:
     * - Nếu chưa đủ 10 điểm → tất cả đều được vào → return true
     * - Nếu đã đủ 10 điểm → so sánh với điểm thứ 10 (cuối cùng)
     *   → Nếu lớn hơn điểm thứ 10 → return true
     */
    public boolean isHighScore(int score) {
        return highScores.size() < MAX_HIGH_SCORES ||
               score > highScores.get(highScores.size() - 1).getScore();
    }
}
