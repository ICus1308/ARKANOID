package userinterface;

import gamemanager.GameButton;
import gamemanager.UIManager;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import static gameconfig.GameConfig.*;


public class GameOverScreen extends UIManager {
    private final StackPane stackPane;

    // Callbacks - có thể null nếu chế độ chơi không hỗ trợ chức năng đó
    private Runnable onRetry;          // Nút REPLAY - chơi lại
    private Runnable onNewGame;        // Nút NEW GAME - bắt đầu mới
    private Runnable onMainMenu;       // Nút MENU - về menu chính

    private Text gameOverText;         // Text "GAME OVER"
    private Text scoreText;            // Text hiển thị điểm số
    private VBox contentBox;           // Container chứa nội dung chính


    public GameOverScreen() {
        super(null);
        this.stackPane = new StackPane();
        this.root = stackPane;
        initializeUI();
    }

    /**
     * Khởi tạo giao diện Game Over
     * - Nền đen mờ phía sau
     * - Hộp trắng với viền đen ở giữa
     * - Tiêu đề, điểm số, và các nút
     */
    @Override
    protected void initializeUI() {
        stackPane.getChildren().clear();
        stackPane.setPrefSize(GAME_WIDTH, GAME_HEIGHT);
        stackPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0);");

        // Nền tối (black overlay) để làm nổi bật hộp dialog
        Rectangle bgOverlay = new Rectangle(GAME_WIDTH, GAME_HEIGHT);
        bgOverlay.setFill(Color.web("rgba(0, 0, 0, 0.7)"));
        stackPane.getChildren().add(bgOverlay);

        // Hộp trắng với viền đen - phần chính của dialog
        Rectangle containerBg = new Rectangle(800 * UI_SCALE_X, 600);
        containerBg.setFill(Color.WHITE);
        containerBg.setStroke(Color.BLACK);
        containerBg.setStrokeWidth(3);

        // Container chính - chứa tất cả nội dung (title, score, buttons)
        contentBox = new VBox(40);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPrefWidth(500 * UI_SCALE_X);
        contentBox.setPrefHeight(400);
        contentBox.setStyle("-fx-padding: 30;");

        // Text "GAME OVER" - màu đen
        gameOverText = createStyledText("GAME OVER", 0, 0, TITLE_FONT, Color.BLACK);

        // Text hiển thị điểm số - màu đen
        scoreText = createStyledText("SCORE: 0", 0, 0, MESSAGE_FONT, Color.BLACK);

        // Tạo các nút (REPLAY, NEW GAME, MENU, EXIT)
        HBox buttonBox = createButtonBox();

        // Thêm tất cả vào container chính
        contentBox.getChildren().addAll(gameOverText, scoreText, buttonBox);

        // Căn giữa container
        StackPane container = new StackPane(containerBg, contentBox);
        stackPane.getChildren().add(container);
    }

    /**
     * Tạo hộp chứa các nút
     * - Hiển thị nút dựa trên callbacks (nếu callback là null thì ẩn nút)
     * - Nút EXIT luôn hiển thị
     * - Dễ dàng thêm hình ảnh bằng cách chỉnh ButtonStyleManager
     */
    private HBox createButtonBox() {
        HBox box = new HBox(30);
        box.setAlignment(Pos.CENTER);

        // Nút REPLAY - chỉ hiển thị nếu onRetry không null
        // Nút REPLAY
        if (onRetry != null) {
            GameButton replayButton = new GameButton("REPLAY");
            replayButton.setOnAction(e -> onRetry.run());
            // replayButton.setImage("/images/buttons/replay.png");  // Thêm hình ảnh sau nếu có
            box.getChildren().add(replayButton);
        }

// Nút NEW GAME
        if (onNewGame != null) {
            GameButton newGameButton = new GameButton("NEW GAME");
            newGameButton.setOnAction(e -> onNewGame.run());
            // newGameButton.setImage("/images/buttons/newgame.png");
            box.getChildren().add(newGameButton);
        }

// Nút MENU
        if (onMainMenu != null) {
            GameButton menuButton = new GameButton("MENU");
            menuButton.setOnAction(e -> onMainMenu.run());
            // menuButton.setImage("/images/buttons/menu.png");
            box.getChildren().add(menuButton);
        }

// Nút EXIT
        GameButton exitButton = new GameButton("EXIT");
        exitButton.setOnAction(e -> {
            Stage stage = (Stage) stackPane.getScene().getWindow();
            if (stage != null) {
                stage.close();
            }
        });
        box.getChildren().add(exitButton);

        return box;
    }
    /**
     * Cập nhật điểm số hiển thị
     * @param score Điểm số cuối cùng
     */
    public void setFinalScore(int score) {
        System.out.println("setFinalScore() called with: " + score);
        if (scoreText != null) {
            scoreText.setText("SCORE: " + score);
            System.out.println("Score text updated to: SCORE: " + score);
        } else {
            System.out.println("scoreText is NULL!");
        }
    }

    /**
     * Thiết lập callback cho nút REPLAY
     * @param onRetry Hàm gọi khi nhấn REPLAY (null để ẩn nút)
     */
    public void setOnRetry(Runnable onRetry) {
        this.onRetry = onRetry;
    }

    /**
     * Thiết lập callback cho nút NEW GAME
     * @param onNewGame Hàm gọi khi nhấn NEW GAME (null để ẩn nút)
     */
    public void setOnNewGame(Runnable onNewGame) {
        this.onNewGame = onNewGame;
    }

    /**
     * Thiết lập callback cho nút MENU
     * @param onMainMenu Hàm gọi khi nhấn MENU (null để ẩn nút)
     */
    public void setOnMainMenu(Runnable onMainMenu) {
        this.onMainMenu = onMainMenu;
    }

    /**
     * Lấy StackPane chính của màn hình Game Over
     */
    public StackPane getStackPane() {
        return stackPane;
    }

    public void setGameOverMessage(String message) {
        if (gameOverText != null) {
            gameOverText.setText(message);
        }
    }

    /**
     * Hiển thị màn hình Game Over
     */
    @Override
    public void show() {

        if (stackPane != null) {
            stackPane.setVisible(true);
            stackPane.toFront();
        }
    }

    /**
     * Ẩn màn hình Game Over
     */
    @Override
    public void hide() {
        if (stackPane != null) {
            stackPane.setVisible(false);
        }
    }

    /**
     * Làm mới giao diện (gọi lại initializeUI)
     */
    @Override
    public void refresh() {
        initializeUI();
    }
}
