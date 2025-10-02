package gameplay;

import config.GameConfig;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.Stage;
import level.LevelManager;

public class GameController {

    @FXML
    private Canvas gameCanvas;

    private LevelManager levelManager;

    @FXML
    public void initialize() {
        System.out.println("Initialize called. gameCanvas=" + gameCanvas);
        // KHÔNG gọi initGame() ở đây nữa vì gameCanvas có thể null
    }

    private void initGame() {
        System.out.println("initGame called. gameCanvas=" + gameCanvas);
        if (gameCanvas == null) {
            System.out.println("ERROR: gameCanvas is null in initGame!");
            return;
        }

        levelManager = new LevelManager();
        levelManager.loadLevel("/mapturtorial.txt");
        draw();
    }

    private void draw() {
        System.out.println("draw() called. gameCanvas=" + gameCanvas);
        if (gameCanvas == null) {
            System.out.println("gameCanvas is null!");
            return;
        }
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, GameConfig.WIDTH, GameConfig.HEIGHT);
        levelManager.draw(gc);
    }

    public void onStart(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/game.fxml"));
            Parent root = loader.load();

            // Lấy controller của game scene
            GameController gameController = loader.getController();

            // Đổi scene
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, GameConfig.WIDTH, GameConfig.HEIGHT));
            stage.setTitle("Arkanoid");
            stage.setResizable(false);
            stage.show();

            // Gọi initGame SAU KHI scene đã show
            gameController.initGame();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
