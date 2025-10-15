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
    private Runnable onStart;
    private Runnable onHighScore;
    private Runnable onSetting;

    public MenuScreen(Runnable onStart, Runnable onHighScore, Runnable onSetting) {
        super(null);
        this.onStart = onStart;
        this.onHighScore = onHighScore;
        this.onSetting = onSetting;
        this.stackPane = new StackPane();
        this.root = stackPane;
        initializeUI();
    }

    @Override
    protected void initializeUI() {
        stackPane.getChildren().clear();

        Text title = createStyledText("ARKANOID", 0, 0, TITLE_FONT, TEXT_COLOR);

        GameButton startButton = createMenuButton("Start", onStart);
        GameButton highScoreButton = createMenuButton("High Score", onHighScore);
        GameButton settingButton = createMenuButton("Settings", onSetting);

        VBox buttonBox = new VBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(startButton, highScoreButton, settingButton);

        stackPane.setStyle("-fx-background-color: rgba(44,62,80,0.9);");
        stackPane.setPrefSize(GAME_WIDTH, GAME_HEIGHT);
        StackPane.setAlignment(title, Pos.TOP_CENTER);
        StackPane.setAlignment(buttonBox, Pos.CENTER);
        stackPane.getChildren().addAll(title, buttonBox);
    }

    private GameButton createMenuButton(String text, Runnable action) {
        GameButton button = new GameButton(text);
        button.setOnAction(e -> action.run());
        return button;
    }

    public StackPane getStackPane() {
        return stackPane;
    }

    @Override
    public void refresh() {
        initializeUI();
    }

    public void refresh(Runnable onStart, Runnable onHighScore, Runnable onSetting) {
        this.onStart = onStart;
        this.onHighScore = onHighScore;
        this.onSetting = onSetting;
        initializeUI();
    }
}
