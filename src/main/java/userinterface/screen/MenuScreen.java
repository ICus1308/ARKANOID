package userinterface.screen;

import gamemanager.ui.GameButton;
import gamemanager.ui.UIManager;
import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.layout.Region;

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

        // Use a transparent stack pane and add a semi-transparent overlay behind the UI
        stackPane.setStyle("-fx-background-color: transparent;");
        stackPane.setPrefSize(GAME_WIDTH, GAME_HEIGHT);

        // Create a full-screen semi-transparent overlay so the background video remains visible
        Region backgroundOverlay = new Region();
        backgroundOverlay.setPrefSize(GAME_WIDTH, GAME_HEIGHT);
        backgroundOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.35);"); // adjust alpha to taste (0.0 - 1.0)
        // Allow overlay to resize if GAME_WIDTH/GAME_HEIGHT change by binding to stackPane size
        backgroundOverlay.prefWidthProperty().bind(stackPane.widthProperty());
        backgroundOverlay.prefHeightProperty().bind(stackPane.heightProperty());
        // Make overlay ignore mouse events so controls above can receive clicks without interference
        backgroundOverlay.setMouseTransparent(true);
        backgroundOverlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        StackPane.setAlignment(title, Pos.TOP_CENTER);
        StackPane.setAlignment(buttonBox, Pos.CENTER);
        // Add overlay first so it's behind title and buttons
        stackPane.getChildren().addAll(backgroundOverlay, title, buttonBox);
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
