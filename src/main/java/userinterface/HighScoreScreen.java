package userinterface;

import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import static gameconfig.GameConfig.*;

public class HighScoreScreen extends UIManager {

    private VBox layout;
    private Runnable onBack;

    public HighScoreScreen(Pane root, Runnable onBack) {
        super(root);
        this.onBack = onBack;
    }

    @Override
    protected void initializeUI() {
        layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: rgba(44, 62, 80, 0.9);");
        layout.setPrefSize(GAME_WIDTH, GAME_HEIGHT);

        Text title = createStyledText("High Scores", 0, 0, TITLE_FONT, TEXT_COLOR);
        Text placeholder = createStyledText("No high scores yet!", 0, 0, UI_FONT, TEXT_COLOR);

        GameButton backButton = new GameButton("Back");
        if (onBack != null) {
            backButton.setOnAction(e -> onBack.run());
        }

        layout.getChildren().addAll(title, placeholder, backButton);
    }

    public void show() {
        if (layout == null) {
            initializeUI();
        }
        if (!root.getChildren().contains(layout)) {
            root.getChildren().add(layout);
        }
    }

    public void hide() {
        if (layout != null) {
            root.getChildren().remove(layout);
        }
    }
}

