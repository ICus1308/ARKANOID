import menu.*;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/menu.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setTitle("Arkanoid");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        // Wire keyboard after scene is ready
    }

    public static void main(String[] args) {
        launch(args);
    }
}
