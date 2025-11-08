package userinterface.screen;

import gamemanager.ui.GameButton;
import gamemanager.ui.UIManager;
import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import static gameconfig.GameConfig.*;

public class MenuScreen extends UIManager {
    private StackPane stackPane;
    private Runnable onStart;
    private Runnable onHighScore;
    private Runnable onSetting;
    private Runnable onShop;

    public MenuScreen(Pane root, Runnable onStart, Runnable onHighScore, Runnable onSetting, Runnable onShop) {
        super(root);
        this.onStart = onStart;
        this.onHighScore = onHighScore;
        this.onSetting = onSetting;
        this.onShop = onShop;
        initializeUI();
    }

    @Override
    protected void initializeUI() {
        if (stackPane == null) {
            stackPane = new StackPane();
        }
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
    public void show() {
        if (stackPane == null) {
            initializeUI();
        }
        if (!root.getChildren().contains(stackPane)) {
            root.getChildren().add(stackPane);
        }
        stackPane.setVisible(true);
        stackPane.toFront();
    }

    @Override
    public void hide() {
        if (stackPane != null) {
            stackPane.setVisible(false);
        }
    }

    @Override
    public void refresh() {
        if (stackPane != null) {
            hide();
            stackPane = null;
        }
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
