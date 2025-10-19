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
    private Runnable onShop;

    public MenuScreen(Runnable onStart, Runnable onHighScore, Runnable onSetting, Runnable onShop) {
        super(null);
        this.onStart = onStart;
        this.onHighScore = onHighScore;
        this.onSetting = onSetting;
        this.onShop = onShop;
        this.stackPane = new StackPane();
        this.root = stackPane;
        initializeUI();
    }

    @Override
    protected void initializeUI() {
        stackPane.getChildren().clear();

        Text title = createStyledText("ARKANOID", 0, 0, TITLE_FONT, TEXT_COLOR);

        GameButton startButton = createButton("Start", onStart);
        GameButton highScoreButton = createButton("High Score", onHighScore);
        GameButton settingButton = createButton("Settings", onSetting);
        GameButton shopButton = createButton("Shop", onShop);

        VBox buttonBox = new VBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(startButton, highScoreButton, settingButton, shopButton);

        stackPane.setStyle("-fx-background-color: rgba(44,62,80,0.9);");
        stackPane.setPrefSize(GAME_WIDTH, GAME_HEIGHT);
        StackPane.setAlignment(title, Pos.TOP_CENTER);
        StackPane.setAlignment(buttonBox, Pos.CENTER);
        stackPane.getChildren().addAll(title, buttonBox);
    }

    public StackPane getStackPane() {
        return stackPane;
    }

    @Override
    public void refresh() {
        initializeUI();
    }

    public void refresh(Runnable onStart, Runnable onHighScore, Runnable onSetting, Runnable onShop) {
        this.onStart = onStart;
        this.onHighScore = onHighScore;
        this.onSetting = onSetting;
        this.onShop = onShop;
        initializeUI();
    }
}
