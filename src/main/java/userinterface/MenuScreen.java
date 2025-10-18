package userinterface;

import gamemanager.GameButton;
import gamemanager.UIManager;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.function.Supplier;

import static gameconfig.GameConfig.*;


public class MenuScreen extends UIManager {
    private final StackPane stackPane;
    private Runnable onStart;
    private Runnable onHighScore;
    private Runnable onSetting;
    private Runnable onShop;
    private Supplier<Integer> coinSupplier;
    private Text coinText;

    public MenuScreen(Runnable onStart, Runnable onHighScore, Runnable onSetting, Runnable onShop, Supplier<Integer> coinSupplier) {
        super(null);
        this.onStart = onStart;
        this.onHighScore = onHighScore;
        this.onSetting = onSetting;
        this.onShop = onShop;
        this.coinSupplier = coinSupplier;
        this.stackPane = new StackPane();
        this.root = stackPane;
        initializeUI();
    }

    @Override
    protected void initializeUI() {
        stackPane.getChildren().clear();

        Text title = createStyledText("ARKANOID", 0, 0, TITLE_FONT, TEXT_COLOR);

        GameButton startButton = createMenuButton("Start", onStart);
        GameButton shopButton = createMenuButton("Shop", onShop);
        GameButton highScoreButton = createMenuButton("High Score", onHighScore);
        GameButton settingButton = createMenuButton("Settings", onSetting);

        VBox buttonBox = new VBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(startButton, shopButton, highScoreButton, settingButton);

//        coinText = createStyledText("Coins: " + (coinSupplier == null ? 0 : coinSupplier.get()), GAME_WIDTH - 180, 30, UI_FONT, GOLD_COLOR);

        stackPane.setStyle("-fx-background-color: rgba(44,62,80,0.9);");
        stackPane.setPrefSize(GAME_WIDTH, GAME_HEIGHT);
        StackPane.setAlignment(title, Pos.TOP_CENTER);
        StackPane.setAlignment(buttonBox, Pos.CENTER);
//        StackPane.setAlignment(coinText, Pos.TOP_RIGHT);
        stackPane.getChildren().addAll(title, buttonBox/*, coinText*/);
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

    public void refresh(Runnable onStart, Runnable onHighScore, Runnable onSetting, Runnable onShop, Supplier<Integer> coinSupplier) {
        this.onStart = onStart;
        this.onHighScore = onHighScore;
        this.onSetting = onSetting;
        this.onShop = onShop;
        this.coinSupplier = coinSupplier;
        initializeUI();
    }
}
