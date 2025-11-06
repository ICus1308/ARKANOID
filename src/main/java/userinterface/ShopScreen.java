package userinterface;

import gamemanager.CoinManager;
import gamemanager.GameButton;
import gamemanager.UIManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.function.Consumer;

import static gameconfig.GameConfig.GAME_HEIGHT;
import static gameconfig.GameConfig.GAME_WIDTH;

public class ShopScreen extends UIManager {
    private StackPane layout;
    private final CoinManager coinManager;
    private final Consumer<String> applyPaddleSkin;
    private final Consumer<String> applyBallSkin;
    private final Runnable onBack;

    public ShopScreen(Pane root, CoinManager coinManager, Consumer<String> applyPaddleSkin, Consumer<String> applyBallSkin, Runnable onBack) {
        super(root);
        this.coinManager = coinManager;
        this.applyPaddleSkin = applyPaddleSkin;
        this.applyBallSkin = applyBallSkin;
        this.onBack = onBack;
    }

    @Override
    protected void initializeUI() {
        layout = new StackPane();
        layout.setPrefSize(GAME_WIDTH, GAME_HEIGHT);
        layout.setStyle("-fx-background-color: rgba(44,62,80,0.95);");

        Text title = createStyledText("SHOP", 0, 0, TITLE_FONT, TEXT_COLOR);

        // Buttons to open sub-screens
        PaddleSkinScreen paddleskinscreen = new PaddleSkinScreen(root, coinManager, applyPaddleSkin, this::showMain);
        BallSkinScreen ballskinscreen = new BallSkinScreen(root, coinManager, applyBallSkin, this::showMain);

        GameButton paddleBtn = createButton("Paddle Skins", () -> {
            hide();
            paddleskinscreen.show();
        });
        GameButton ballBtn = createButton("Ball Skins", () -> {
            hide();
            ballskinscreen.show();
        });

        GameButton backBtn = createButton("Back", () -> {
            paddleskinscreen.hide();
            ballskinscreen.hide();
            onBack.run();
        });

        VBox main = new VBox(20, title, paddleBtn, ballBtn, backBtn);
        main.setAlignment(Pos.CENTER);
        main.setPadding(new Insets(40));

        // Coin display at top-right
        Text coinText = createStyledText("Coins: " + (coinManager == null ? 0 : coinManager.getCoins()), 0, 0, UI_FONT, GOLD_COLOR);
        StackPane.setAlignment(coinText, Pos.TOP_RIGHT);
        StackPane.setMargin(coinText, new Insets(20, 20, 0, 0));

        layout.getChildren().addAll(main, coinText);
    }

    private void showMain() {
        // when sub-screen Back pressed, re-show main menu
        show();
    }

    @Override
    public void show() {
        if (layout == null) initializeUI();
        if (!root.getChildren().contains(layout)) root.getChildren().add(layout);
    }

    @Override
    public void hide() {
        if (layout != null) root.getChildren().remove(layout);
    }

    @Override
    public void refresh() {
        if (layout != null) hide();
        layout = null;
    }
}
