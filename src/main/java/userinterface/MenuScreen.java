package userinterface;

import gamemanager.GameButton;
import gamemanager.UIManager;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import static gameconfig.GameConfig.*;


public class MenuScreen extends UIManager {
    private final StackPane stackPane;

    public MenuScreen(Runnable onStart, Runnable onHighScore) {
        super(null);
        this.stackPane = new StackPane();
        initializeUI(onStart, onHighScore);
    }

    private void initializeUI(Runnable onStart, Runnable onHighScore) {
        Text title = createStyledText("ARKANOID", 0, 0, TITLE_FONT, TEXT_COLOR);

        GameButton startButton = new GameButton("Start");
        startButton.setOnAction(e -> onStart.run());

        GameButton highScoreButton = new GameButton("High Score");
        highScoreButton.setOnAction(e -> onHighScore.run());

        VBox buttonBox = new VBox(20); // Spacing between buttons
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(startButton, highScoreButton);

        stackPane.setStyle("-fx-background-color: rgba(44,62,80,0.9);");
        stackPane.setPrefSize(GAME_WIDTH, GAME_HEIGHT);
        StackPane.setAlignment(title, Pos.TOP_CENTER);
        StackPane.setAlignment(buttonBox, Pos.CENTER);
        stackPane.getChildren().addAll(title, buttonBox);

        this.root = stackPane;
    }
    
    @Override
    protected void initializeUI() {

    }

    public StackPane getStackPane() {
        return stackPane;
    }
}
