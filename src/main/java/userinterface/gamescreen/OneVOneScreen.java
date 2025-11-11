package userinterface.gamescreen;

import gamemanager.ui.UIManager;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;

import java.util.Objects;

import static gameconfig.GameConfig.*;

public class OneVOneScreen extends UIManager implements GameScreen {
    private static final int INITIAL_LIVES = 3;

    private int player1Lives;
    private int player2Lives;
    private int score = 0; // Điểm dùng cho GameScreen interface (ít dùng trong 1v1)
    private Text player1LivesText;
    private Text player2LivesText;
    private ImageView player1LivesIcon;
    private ImageView player2LivesIcon;
    private HBox player1LivesBox;
    private HBox player2LivesBox;

    public OneVOneScreen(Pane root) {
        super(root);
        this.player1Lives = INITIAL_LIVES;
        this.player2Lives = INITIAL_LIVES;
        initializeUI();
    }

    @Override
    protected void initializeUI() {
        // ===== TẠO ICON LIVES CHO PLAYER 1 =====
        // Dùng helper method để tránh lặp code (DRY principle)
        player1LivesIcon = loadLivesIcon("Error loading player 1 lives icon");

        player1LivesText = createStyledText(INITIAL_LIVES + "", 0, 0, UI_FONT, TEXT_COLOR);
        player1LivesBox = new HBox(5); // HBox = container ngang, spacing 5px
        player1LivesBox.setAlignment(Pos.CENTER_LEFT);

        // NULL-SAFE: Chỉ thêm icon nếu load thành công
        // Tránh crash khi file ảnh không tồn tại
        if (player1LivesIcon != null) {
            player1LivesBox.getChildren().addAll(player1LivesIcon, player1LivesText);
        } else {
            player1LivesBox.getChildren().add(player1LivesText); // Chỉ hiển thị text
        }
        player1LivesBox.setLayoutX(10);
        player1LivesBox.setLayoutY(GAME_HEIGHT - 40); // Dưới cùng màn hình

        // ===== TẠO ICON LIVES CHO PLAYER 2 =====
        // Code tương tự player 1 nhưng vị trí khác
        player2LivesIcon = loadLivesIcon("Error loading player 2 lives icon");

        player2LivesText = createStyledText(INITIAL_LIVES + "", 0, 0, UI_FONT, TEXT_COLOR);
        player2LivesBox = new HBox(5);
        player2LivesBox.setAlignment(Pos.CENTER_LEFT);

        if (player2LivesIcon != null) {
            player2LivesBox.getChildren().addAll(player2LivesIcon, player2LivesText);
        } else {
            player2LivesBox.getChildren().add(player2LivesText);
        }
        player2LivesBox.setLayoutX(GAME_WIDTH - 80);
        player2LivesBox.setLayoutY(10); // Trên cùng màn hình

        gameMessage = createStyledText("PRESS SPACE TO START", GAME_WIDTH / 2 - 200, GAME_HEIGHT / 2, MESSAGE_FONT, GOLD_COLOR);

        // Thêm tất cả node vào scene (null-safe)
        if (root != null) {
            root.getChildren().addAll(player1LivesBox, player2LivesBox, gameMessage);
        }
    }

    /**
     * HELPER METHOD: Load icon lives và xử lý lỗi
     *
     * TẠI SAO CẦN HELPER NÀY?
     * - Trước đây code load icon cho player 1 và player 2 GIỐNG HỆT NHAU
     * - Vi phạm DRY principle (Don't Repeat Yourself)
     * - Nếu muốn sửa (ví dụ: đổi kích thước icon) → phải sửa 2 chỗ → dễ quên
     *
     * SAU KHI DÙNG HELPER:
     * - Chỉ cần sửa 1 chỗ
     * - Code gọn hơn, dễ maintain
     *
     * @param errorMessage Thông báo lỗi nếu load thất bại
     * @return ImageView chứa icon, hoặc null nếu load lỗi
     */
    private ImageView loadLivesIcon(String errorMessage) {
        try {
            Image livesImage = new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/imagelive/imagelive.png")));
            ImageView iv = new ImageView(livesImage);
            iv.setFitWidth(25);  // Chiều rộng icon
            iv.setFitHeight(25); // Chiều cao icon
            iv.setPreserveRatio(true); // Giữ nguyên tỉ lệ ảnh (không méo)
            return iv;
        } catch (Exception e) {
            // In lỗi CHI TIẾT (bao gồm e.getMessage()) để dễ debug
            System.err.println(errorMessage + ": " + e.getMessage());
            return null; // Trả về null thay vì crash
        }
    }

    public int getPlayer1Lives() {
        return player1Lives;
    }

    public void updatePlayer1Lives(int newLives) {
        this.player1Lives = newLives;
        // NULL-SAFE: Chỉ cập nhật text nếu nó tồn tại
        if (player1LivesText != null) player1LivesText.setText(player1Lives + "");
    }

    public void decreasePlayer1Lives() {
        updatePlayer1Lives(player1Lives - 1);
    }

    public int getPlayer2Lives() {
        return player2Lives;
    }

    public void updatePlayer2Lives(int newLives) {
        this.player2Lives = newLives;
        if (player2LivesText != null) player2LivesText.setText(player2Lives + "");
    }

    public void decreasePlayer2Lives() {
        updatePlayer2Lives(player2Lives - 1);
    }

    // ===== IMPLEMENT GAMESCREEN INTERFACE =====
    // Các phương thức này BẮT BUỘC phải có vì OneVOneScreen implement GameScreen
    // Giải thích từng method:

    @Override
    public void updateScore(int newScore) {
        this.score = newScore;
        // Trong 1v1 mode không hiển thị score chung
        // Nhưng vẫn phải implement method này vì GameScreen interface yêu cầu
    }

    @Override
    public void increaseScore(int delta) {
        updateScore(this.score + delta);
    }

    @Override
    public int getScore() {
        return score;
    }

    /**
     * CÁC PHƯƠNG THỨC LIVES "GENERIC"
     *
     * Vì GameScreen interface được thiết kế cho singleplayer (chỉ 1 người chơi)
     * Nhưng OneVOneScreen có 2 người chơi → phải quyết định method này áp dụng cho ai?
     *
     * GIẢI PHÁP: Mặc định áp dụng cho Player 1
     * - Nếu muốn điều khiển Player 2 → dùng updatePlayer2Lives(), decreasePlayer2Lives()
     * - Các method generic (getLives, updateLives, decreaseLives) → điều khiển Player 1
     */
    @Override
    public int getLives() {
        return player1Lives; // Trả về lives của Player 1
    }

    @Override
    public void updateLives(int newLives) {
        updatePlayer1Lives(newLives); // Cập nhật Player 1
    }

    @Override
    public void decreaseLives() {
        decreasePlayer1Lives(); // Giảm lives của Player 1
    }

    @Override
    public void updateCoins() {
        // No-op: Chế độ 1v1 không có coin system
        // Để trống vì GameScreen interface bắt buộc phải có method này
    }

    @Override
    public void showLevel(int level) {
        showGameMessage("LEVEL " + level, GOLD_COLOR);
    }

    public void showGameMessage(GameState state) {
        switch (state) {
            case START:
                showGameMessage("PRESS SPACE TO LAUNCH", GOLD_COLOR);
                break;
            case GAME_OVER:
                // Xác định người thắng dựa vào số mạng còn lại
                String winner = player1Lives > player2Lives ? "PLAYER 1" : "PLAYER 2";
                showGameMessage("GAME OVER | " + winner + " WINS!", RED_COLOR);
                break;
            case LEVEL_CLEARED:
                showGameMessage("LEVEL CLEARED! | Press SPACE for Next Level", GREEN_COLOR);
                break;
            case PLAYING:
                hideGameMessage(); // Ẩn message khi đang chơi
                break;
        }
    }

    /**
     * Dọn dẹp UI khi thoát màn hình 1v1
     *
     * NULL-SAFE CLEANUP:
     * - Kiểm tra từng node trước khi xóa
     * - Tránh crash khi node chưa được tạo (ví dụ: lỗi trong initializeUI)
     */
    public void cleanup() {
        if (root != null) {
            // Xóa từng node riêng lẻ thay vì removeAll(list)
            // → An toàn hơn khi một số node có thể null
            if (player1LivesBox != null) root.getChildren().remove(player1LivesBox);
            if (player2LivesBox != null) root.getChildren().remove(player2LivesBox);
            if (gameMessage != null) root.getChildren().remove(gameMessage);
        }
    }
}
