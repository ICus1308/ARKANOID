package userinterface;

import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import static gameconfig.GameConfig.*;

public class Menu extends StackPane {
    private final Button startButton;

    public Menu(Runnable onStart) {
        Text title = new Text("ARKANOID");
        title.setFont(Font.font("Inter", 48));
        startButton = new Button("Start");
        startButton.setFont(Font.font("Inter", 24));
        startButton.setOnAction(e -> onStart.run());

        setStyle("-fx-background-color: rgba(44,62,80,0.9);");
        setPrefSize(GAME_WIDTH, GAME_HEIGHT); // Có thể điều chỉnh theo GameConfig
        setAlignment(title, javafx.geometry.Pos.TOP_CENTER);
        setAlignment(startButton, javafx.geometry.Pos.CENTER);
        getChildren().addAll(title, startButton);
    }
}
